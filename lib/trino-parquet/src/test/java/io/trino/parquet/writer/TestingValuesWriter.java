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
package io.trino.parquet.writer;

import io.trino.parquet.writer.valuewriter.ColumnDescriptorValuesWriter;
import io.trino.parquet.writer.valuewriter.ValuesWriter;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.apache.parquet.bytes.BytesInput;
import org.apache.parquet.column.Encoding;

import java.util.List;

class TestingValuesWriter
        extends ValuesWriter
        implements ColumnDescriptorValuesWriter
{
    private final IntList values = new IntArrayList();
    private final LongList longValues = new LongArrayList();

    @Override
    public long getBufferedSize()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public BytesInput getBytes()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Encoding getEncoding()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getAllocatedSize()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String memUsageString(String prefix)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeInteger(int v)
    {
        values.add(v);
    }

    @Override
    public void writeRepeatInteger(int value, int valueRepetitions)
    {
        for (int i = 0; i < valueRepetitions; i++) {
            values.add(value);
        }
    }

    @Override
    public void writeLong(long value)
    {
        longValues.add(value);
    }

    List<Integer> getWrittenValues()
    {
        return values;
    }

    List<Long> getWrittenLongValues()
    {
        return longValues;
    }
}
