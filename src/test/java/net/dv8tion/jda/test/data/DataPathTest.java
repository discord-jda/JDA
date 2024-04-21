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

package net.dv8tion.jda.test.data;

import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataPath;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class DataPathTest
{
    @Test
    void testSimple()
    {
        DataObject object = DataObject.empty()
                .put("foo", "10"); // string to also test parsing

        assertThat(DataPath.getInt(object, "foo")).isEqualTo(10);

        DataArray array = DataArray.empty().add("20");
        assertThat(DataPath.getInt(array, "[0]")).isEqualTo(20);
    }

    @Test
    void testSimpleMissing()
    {
        DataObject object = DataObject.empty();

        assertThat(DataPath.getLong(object, "foo?", 0)).isEqualTo(0L);
        assertThatThrownBy(() -> DataPath.getLong(object, "foo"))
            .hasMessage("Unable to resolve value with key foo to type long: null")
            .isInstanceOf(ParsingException.class);

        DataArray array = DataArray.empty();

        assertThat(DataPath.getBoolean(array, "[0]?", true)).isTrue();
        assertThatThrownBy(() -> DataPath.getObject(array, "[0]"))
            .hasMessage("Could not resolve value of type Object at path \"[0]\"")
            .isInstanceOf(ParsingException.class);
    }

    @Test
    void testObjectInArray()
    {
        DataObject object = DataObject.empty().put("foo", 10.0);
        DataArray array = DataArray.empty().add(object);

        assertThat(DataPath.getDouble(array, "[0].foo")).isEqualTo(10.0);
        assertThat(DataPath.getDouble(array, "[1]?.foo", 20.0)).isEqualTo(20.0);

        assertThatIndexOutOfBoundsException()
            .isThrownBy(() -> DataPath.getDouble(array, "[1].foo"));
    }

    @Test
    void testArrayInObject()
    {
        DataArray array = DataArray.empty().add("hello");
        DataObject object = DataObject.empty().put("foo", array);

        assertThat(DataPath.getString(object, "foo[0]")).isEqualTo("hello");
        assertThat(DataPath.getString(object, "foo[1]?", "world")).isEqualTo("world");
        assertThatIndexOutOfBoundsException()
            .isThrownBy(() -> DataPath.getString(object, "foo[1]"));
    }

    @Test
    void testArrayInArray()
    {
        DataArray array = DataArray.empty().add(DataArray.empty().add("10"));

        assertThat(DataPath.getUnsignedInt(array, "[0][0]")).isEqualTo(10);
        assertThat(DataPath.getUnsignedInt(array, "[0][1]?", 20)).isEqualTo(20);
        assertThat(DataPath.getUnsignedInt(array, "[1]?[0]", 20)).isEqualTo(20);
        assertThatIndexOutOfBoundsException().isThrownBy(() -> DataPath.getUnsignedInt(array, "[0][1]"));
        assertThatIndexOutOfBoundsException().isThrownBy(() -> DataPath.getUnsignedInt(array, "[1][0]"));
        assertThatThrownBy(() -> DataPath.getUnsignedInt(array, "[0][1]?"))
            .hasMessage("Could not resolve value of type unsigned int at path \"[0][1]?\"")
            .isInstanceOf(ParsingException.class);
        assertThatThrownBy(() -> DataPath.getUnsignedInt(array, "[1]?[0]"))
            .hasMessage("Could not resolve value of type unsigned int at path \"[1]?[0]\"")
            .isInstanceOf(ParsingException.class);
    }

    @Test
    void testComplex()
    {
        DataObject object = DataObject.empty()
                .put("array", DataArray.empty()
                    .add(DataObject.empty()
                        .put("foo", DataObject.empty()
                            .put("bar", "hello"))));

        assertThat(DataPath.getString(object, "array[0].foo.bar")).isEqualTo("hello");
        assertThat(DataPath.getString(object, "array[0].wrong?.bar", "world")).isEqualTo("world");
        assertThatThrownBy(() -> DataPath.getString(object, "array[0].wrong?.bar"))
            .hasMessage("Could not resolve value of type String at path \"array[0].wrong?.bar\"")
            .isInstanceOf(ParsingException.class);
    }
}
