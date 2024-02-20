/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataPathTest
{
    @Test
    void testSimple()
    {
        DataObject object = DataObject.empty()
                .put("foo", "10"); // string to also test parsing

        assertEquals(10, DataPath.getInt(object, "foo"));

        DataArray array = DataArray.empty().add("20");
        assertEquals(20, DataPath.getInt(array, "[0]"));
    }

    @Test
    void testSimpleMissing()
    {
        DataObject object = DataObject.empty();

        assertEquals(0L, DataPath.getLong(object, "foo?", 0));
        assertThrows(ParsingException.class, () -> DataPath.getLong(object, "foo"));

        DataArray array = DataArray.empty();

        assertTrue(DataPath.getBoolean(array, "[0]?", true));
        assertThrows(ParsingException.class, () -> DataPath.getObject(array, "[0]"));
    }

    @Test
    void testObjectInArray()
    {
        DataObject object = DataObject.empty().put("foo", 10.0);
        DataArray array = DataArray.empty().add(object);

        assertEquals(10.0, DataPath.getDouble(array, "[0].foo"));
        assertEquals(20.0, DataPath.getDouble(array, "[1]?.foo", 20.0));
        assertThrows(IndexOutOfBoundsException.class, () -> DataPath.getDouble(array, "[1].foo"));
    }

    @Test
    void testArrayInObject()
    {
        DataArray array = DataArray.empty().add("hello");
        DataObject object = DataObject.empty().put("foo", array);

        assertEquals("hello", DataPath.getString(object, "foo[0]"));
        assertEquals("world", DataPath.getString(object, "foo[1]?", "world"));
        assertThrows(IndexOutOfBoundsException.class, () -> DataPath.getString(object, "foo[1]"));
    }

    @Test
    void testArrayInArray()
    {
        DataArray array = DataArray.empty().add(DataArray.empty().add("10"));

        assertEquals(10, DataPath.getUnsignedInt(array, "[0][0]"));
        assertEquals(20, DataPath.getUnsignedInt(array, "[0][1]?", 20));
        assertEquals(20, DataPath.getUnsignedInt(array, "[1]?[0]", 20));
        assertThrows(IndexOutOfBoundsException.class, () -> DataPath.getUnsignedInt(array, "[0][1]"));
        assertThrows(IndexOutOfBoundsException.class, () -> DataPath.getUnsignedInt(array, "[1][0]"));
        assertThrows(ParsingException.class, () -> DataPath.getUnsignedInt(array, "[0][1]?"));
        assertThrows(ParsingException.class, () -> DataPath.getUnsignedInt(array, "[1]?[0]"));
    }

    @Test
    void testComplex()
    {
        DataObject object = DataObject.empty()
                .put("array", DataArray.empty()
                    .add(DataObject.empty()
                        .put("foo", DataObject.empty()
                            .put("bar", "hello"))));

        assertEquals("hello", DataPath.getString(object, "array[0].foo.bar"));
        assertEquals("world", DataPath.getString(object, "array[0].wrong?.bar", "world"));
        assertThrows(ParsingException.class, () -> DataPath.getString(object, "array[0].wrong?.bar"));
    }
}
