/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.parquet.writer.valuewriter;

import io.trino.spi.block.Fixed12Block;
import io.trino.spi.block.ValueBlock;
import org.apache.parquet.column.statistics.Statistics;
import org.apache.parquet.schema.PrimitiveType;

import static io.trino.spi.type.DateTimeEncoding.unpackMillisUtc;
import static io.trino.spi.type.Timestamps.NANOSECONDS_PER_MILLISECOND;
import static io.trino.spi.type.Timestamps.PICOSECONDS_PER_NANOSECOND;
import static io.trino.spi.type.Timestamps.roundDiv;
import static java.lang.Math.addExact;
import static java.lang.Math.floorDiv;
import static java.lang.Math.floorMod;
import static java.lang.Math.multiplyExact;
import static java.lang.Math.toIntExact;

public class TimestampTzNanosValueWriter
        extends PrimitiveValueWriter
{
    private static final long MIN_NANO_EPOCH_MILLIS = floorDiv(Long.MIN_VALUE, NANOSECONDS_PER_MILLISECOND);
    private static final int MIN_NANO_OF_MILLI = toIntExact(floorMod(Long.MIN_VALUE, NANOSECONDS_PER_MILLISECOND));

    public TimestampTzNanosValueWriter(ValuesWriter valuesWriter, PrimitiveType parquetType)
    {
        super(parquetType, valuesWriter);
    }

    @Override
    protected void writeValueBlock(ValueBlock block)
    {
        ValuesWriter valuesWriter = getValuesWriter();
        Statistics<?> statistics = getStatistics();
        Fixed12Block fixed12Block = (Fixed12Block) block;
        boolean mayHaveNull = block.mayHaveNull();
        for (int i = 0; i < block.getPositionCount(); i++) {
            if (!mayHaveNull || !block.isNull(i)) {
                long nanos = toNanos(fixed12Block, i);
                valuesWriter.writeLong(nanos);
                statistics.updateStats(nanos);
            }
        }
    }

    @Override
    protected void writeRepeated(ValueBlock block, int count)
    {
        ValuesWriter valuesWriter = getValuesWriter();
        Statistics<?> statistics = getStatistics();
        long nanos = toNanos((Fixed12Block) block, 0);
        for (int i = 0; i < count; i++) {
            valuesWriter.writeLong(nanos);
        }
        statistics.updateStats(nanos);
    }

    @Override
    protected void writePositions(ValueBlock block, int[] positions, int offset, int length)
    {
        ValuesWriter valuesWriter = getValuesWriter();
        Statistics<?> statistics = getStatistics();
        Fixed12Block fixed12Block = (Fixed12Block) block;
        boolean mayHaveNull = block.mayHaveNull();
        for (int index = 0; index < length; index++) {
            int position = positions[offset + index];
            if (!mayHaveNull || !block.isNull(position)) {
                long nanos = toNanos(fixed12Block, position);
                valuesWriter.writeLong(nanos);
                statistics.updateStats(nanos);
            }
        }
    }

    private static long toNanos(Fixed12Block block, int position)
    {
        long epochMillis = unpackMillisUtc(block.getFixed12First(position));
        long nanosOfMilli = roundDiv(block.getFixed12Second(position), PICOSECONDS_PER_NANOSECOND);
        if (epochMillis == MIN_NANO_EPOCH_MILLIS) {
            return addExact(Long.MIN_VALUE, nanosOfMilli - MIN_NANO_OF_MILLI);
        }
        return addExact(multiplyExact(epochMillis, NANOSECONDS_PER_MILLISECOND), nanosOfMilli);
    }
}
