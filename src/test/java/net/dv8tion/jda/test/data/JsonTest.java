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
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.test.PrettyRepresentation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonTest
{
    private static final String TEST_TIME_STRING = "2024-01-01T12:34:56.789Z";
    private static final OffsetDateTime TEST_TIME = OffsetDateTime.parse(TEST_TIME_STRING);
    private static final String testJson = jsonOf(
        kv("int", 10),
        kv("long", 100),
        kv("boolean", true),
        kv("string", "test"),
        kv("double", 4.2),
        kv("time", TEST_TIME_STRING)
    );
    private static final String testJsonArray = Stream
            .of(10, 100L, true, "\"test\"", 4.2, 5.2f)
            .map(String::valueOf)
            .collect(Collectors.joining(",\n", "[", "]"));

    private static final byte[] simpleEtfArray = {-125, 108, 0, 0, 0, 6, 97, 10, 97, 100, 100, 0, 4, 116, 114, 117, 101, 109, 0, 0, 0, 4, 116, 101, 115, 116, 70, 64, 16, -52, -52, -52, -52, -52, -51, 70, 64, 20, -52, -52, -52, -52, -52, -51, 106};
    private static final byte[] complexEtfArray = {-125, 108, 0, 0, 0, 6, 109, 0, 0, 0, 3, 111, 110, 101, 97, 2, 70, 64, 11, -103, -103, -103, -103, -103, -102, 97, 7, 116, 0, 0, 0, 2, 109, 0, 0, 0, 5, 102, 105, 114, 115, 116, 109, 0, 0, 0, 5, 101, 105, 103, 104, 116, 109, 0, 0, 0, 6, 115, 101, 99, 111, 110, 100, 106, 108, 0, 0, 0, 2, 109, 0, 0, 0, 4, 110, 105, 110, 101, 116, 0, 0, 0, 1, 109, 0, 0, 0, 3, 107, 101, 121, 109, 0, 0, 0, 3, 116, 101, 110, 106, 106};

    @Nested
    class DataObjectTest
    {
        @Test
        void testParse()
        {
            DataObject object = DataObject.fromJson(testJson);
            assertThat(object.getInt("int")).isEqualTo(10);
            assertThat(object.getLong("long")).isEqualTo(100);
            assertThat(object.getDouble("double")).isEqualTo(4.2);
            assertThat(object.getBoolean("boolean")).isTrue();
            assertThat(object.getString("string")).isEqualTo("test");
            assertThat(object.getOffsetDateTime("time")).isEqualTo(TEST_TIME);
        }

        @Test
        void testCoerce()
        {
            DataObject data = DataObject.empty()
                    .put("stringified_int", "42")
                    .put("stringified_boolean", "true")
                    .put("stringified_long", "86699011792191488")
                    .put("stringified_datetime", TEST_TIME_STRING)
                    .put("stringified_double", "123.456");

            assertThat(data.toMap()).containsOnly(
                entry("stringified_int", "42"),
                entry("stringified_boolean", "true"),
                entry("stringified_long", "86699011792191488"),
                entry("stringified_datetime", TEST_TIME_STRING),
                entry("stringified_double", "123.456")
            );

            assertThat(data.getInt("stringified_int")).isEqualTo(42);
            assertThat(data.getBoolean("stringified_boolean")).isTrue();
            assertThat(data.getLong("stringified_long")).isEqualTo(86699011792191488L);
            assertThat(data.getUnsignedLong("stringified_long")).isEqualTo(86699011792191488L);
            assertThat(data.getDouble("stringified_double")).isEqualTo(123.456);
            assertThat(data.getString("stringified_datetime")).isEqualTo(TEST_TIME_STRING);
        }

        @Test
        void testFallback()
        {
            DataObject data = DataObject.fromJson(jsonOf());

            assertThat(data).isEqualTo(DataObject.empty());
            assertThat(data).hasToString("{}");

            assertThat(data.isNull("key")).isTrue();
            assertThat(data.hasKey("key")).isFalse();

            assertThat(data.getDouble("key", 5.3)).isEqualTo(5.3);
            assertThat(data.getInt("key", 4)).isEqualTo(4);
            assertThat(data.getUnsignedInt("key", 7)).isEqualTo(7);
            assertThat(data.getLong("key", 123L)).isEqualTo(123);
            assertThat(data.getUnsignedLong("key", 321L)).isEqualTo(321L);
            assertThat(data.getBoolean("key")).isFalse();
            assertThat(data.getBoolean("key", true)).isTrue();
            assertThat(data.getOffsetDateTime("key", TEST_TIME)).isEqualTo(TEST_TIME);
            assertThat(data.opt("key")).isEmpty();
            assertThat(data.optObject("key")).isEmpty();
            assertThat(data.optArray("key")).isEmpty();
        }

        @Test
        void testJsonToString()
        {
            DataObject object = DataObject.fromJson(testJson);
            String result = object.toString();
            DataObject symmetric = DataObject.fromJson(result);

            assertThat(symmetric.toMap()).isNotSameAs(object.toMap());
            assertThat(symmetric.toMap()).isEqualTo(object.toMap());
            assertThat(symmetric.toMap()).hasSize(6);
            assertThat(symmetric.toMap()).containsOnly(
                entry("int", 10),
                entry("long", 100),
                entry("boolean", true),
                entry("string", "test"),
                entry("double", 4.2),
                entry("time", TEST_TIME_STRING)
            );
        }

        @Test
        void testFactories()
        {
            DataObject reference = DataObject.fromJson(testJson);

            assertThat(DataObject.fromJson(testJson.getBytes(StandardCharsets.UTF_8)))
                .withRepresentation(new PrettyRepresentation())
                .isEqualTo(reference);
            assertThat(DataObject.fromJson(new StringReader(testJson)))
                .withRepresentation(new PrettyRepresentation())
                .isEqualTo(reference);
            assertThat(DataObject.fromJson(new ByteArrayInputStream(testJson.getBytes(StandardCharsets.UTF_8))))
                .withRepresentation(new PrettyRepresentation())
                .isEqualTo(reference);
        }
    }

    @Nested
    class DataArrayTest
    {
        @Test
        void testParse()
        {
            DataArray object = DataArray.fromJson(testJsonArray);
            assertThat(object.getInt(0)).isEqualTo(10);
            assertThat(object.getLong(1)).isEqualTo(100);
            assertThat(object.getBoolean(2)).isTrue();
            assertThat(object.getString(3)).isEqualTo("test");
            assertThat(object.getDouble(4)).isEqualTo(4.2);
            assertThat(object.getDouble(5)).isEqualTo(5.2);
        }

        @Test
        void testCoerce()
        {
            DataArray array = DataArray.empty()
                    .add("42")
                    .add("true")
                    .add("86699011792191488")
                    .add(TEST_TIME_STRING)
                    .add("123.456");

            assertThat(array.toList()).containsExactly(
                "42", "true", "86699011792191488", TEST_TIME_STRING, "123.456"
            );

            assertThat(array.getInt(0)).isEqualTo(42);
            assertThat(array.getBoolean(1)).isTrue();
            assertThat(array.getLong(2)).isEqualTo(86699011792191488L);
            assertThat(array.getUnsignedLong(2)).isEqualTo(86699011792191488L);
            assertThat(array.getString(3)).isEqualTo(TEST_TIME_STRING);
            assertThat(array.getDouble(4)).isEqualTo(123.456);
        }

        @Test
        void testFallback()
        {
            DataArray data = DataArray.fromJson("[]");

            assertThat(data).isEqualTo(DataArray.empty());
            assertThat(data).hasToString("[]");

            assertThat(data.isNull(0)).isTrue();
            assertThat(data.length()).isEqualTo(0);
            assertThat(data.isEmpty()).isTrue();

            assertThat(data.getDouble(0, 5.3)).isEqualTo(5.3);
            assertThat(data.getInt(0, 4)).isEqualTo(4);
            assertThat(data.getUnsignedInt(0, 7)).isEqualTo(7);
            assertThat(data.getLong(0, 123L)).isEqualTo(123);
            assertThat(data.getUnsignedLong(0, 321L)).isEqualTo(321L);
            assertThat(data.getBoolean(0)).isFalse();
            assertThat(data.getBoolean(0, true)).isTrue();
            assertThat(data.getOffsetDateTime(0, TEST_TIME)).isEqualTo(TEST_TIME);
        }

        @Test
        void testJsonToString()
        {
            DataArray object = DataArray.fromJson(testJsonArray);
            String result = object.toString();
            DataArray symmetric = DataArray.fromJson(result);

            assertThat(symmetric.toList()).isNotSameAs(object.toList());
            assertThat(symmetric.toList()).isEqualTo(object.toList());
            assertThat(symmetric.toList()).hasSize(6);
            assertThat(symmetric.toList()).containsExactly(
                10, 100, true, "test", 4.2, 5.2
            );
        }

        @Test
        void testStream()
        {
            DataArray intArray = IntStream.range(0, 3).boxed().collect(Helpers.toDataArray());
            assertThat(intArray.stream(DataArray::getInt))
                .containsExactly(0, 1, 2);
            assertThat(intArray.stream(DataArray::getLong))
                .containsExactly(0L, 1L, 2L);

            DataArray doubleArray = DoubleStream.of(0.1, 0.5, 1.2, 4.2).boxed().collect(Helpers.toDataArray());
            assertThat(doubleArray.stream(DataArray::getDouble))
                .containsExactly(0.1, 0.5, 1.2, 4.2);

            DataArray stringArray = DataArray.empty().add("foo").add("bar");
            assertThat(stringArray.stream(DataArray::getString))
                .containsExactly("foo", "bar");

            DataArray polyTypedArray = DataArray.empty().add(1).add(2.3).add("four");
            assertThatThrownBy(() -> polyTypedArray.stream(DataArray::getInt).toArray())
                .isInstanceOf(NumberFormatException.class);
            assertThatThrownBy(() -> polyTypedArray.stream(DataArray::getDouble).toArray())
                .isInstanceOf(NumberFormatException.class);
            assertThatThrownBy(() -> polyTypedArray.stream(DataArray::getObject).toArray())
                .isInstanceOf(ParsingException.class)
                .hasMessage("Cannot parse value for index 0 into type Map: 1 instance of Integer");
            assertThat(polyTypedArray.stream(DataArray::getString))
                .containsExactly("1", "2.3", "four");

            DataArray objectArray = DataArray.empty()
                .add(DataObject.empty().put("foo", 1))
                .add(DataObject.empty().put("foo", 2));
            assertThat(objectArray.stream(DataArray::getObject).map(obj -> obj.getInt("foo")))
                .containsExactly(1, 2);

            objectArray.add(DataArray.empty());
            assertThatThrownBy(() ->
                objectArray.stream(DataArray::getObject).map(obj -> obj.getInt("foo")).toArray()
            )
                .isInstanceOf(ParsingException.class)
                .hasMessage("Cannot parse value for index 2 into type Map: [] instance of ArrayList");
        }

        @Test
        void testFactories()
        {
            assertThat(DataArray.fromJson(new StringReader(testJsonArray)))
                .withRepresentation(new PrettyRepresentation())
                .containsExactly(10, 100, true, "test", 4.2, 5.2);
            assertThat(DataArray.fromJson(new ByteArrayInputStream(testJsonArray.getBytes(StandardCharsets.UTF_8))))
                .withRepresentation(new PrettyRepresentation())
                .containsExactly(10, 100, true, "test", 4.2, 5.2);
            assertThat(DataArray.fromJson(testJsonArray))
                .withRepresentation(new PrettyRepresentation())
                .containsExactly(10, 100, true, "test", 4.2, 5.2);

            List<?> inputList = Arrays.asList(10, 100L, true, "test", 4.2, 5.2f);
            DataArray array = DataArray.fromCollection(inputList);
            assertThat(array)
                .withRepresentation(new PrettyRepresentation())
                .as("Do not lose types from input collections")
                .containsExactly(10, 100L, true, "test", 4.2, 5.2f);
            assertThat(array.toList()).isNotSameAs(inputList);
            assertThat(array.toList()).isEqualTo(inputList);
            assertThat(array.add("foo").toList()).isNotEqualTo(inputList);

            assertThat(DataArray.fromETF(simpleEtfArray))
                .withRepresentation(new PrettyRepresentation())
                .containsExactly(10, 100, true, "test", 4.2, 5.2);
            assertThat(DataArray.fromETF(simpleEtfArray).toETF())
                .isEqualTo(simpleEtfArray);
        }

        @Test
        void testExTerm()
        {
            DataArray array = DataArray.empty()
                    .add("one")
                    .add(2)
                    .add(3.45)
                    .add(7L)
                    .add(DataObject.empty()
                        .put("first", "eight")
                        .put("second", DataArray.empty()))
                    .add(DataArray.empty()
                        .add("nine")
                        .add(DataObject.empty()
                            .put("key", "ten")));

            assertThat(array).hasSize(6);
            assertThat(array.toETF()).isEqualTo(complexEtfArray);
            assertThat(DataArray.fromETF(complexEtfArray).toETF())
                .isEqualTo(complexEtfArray);
            assertThat(DataArray.fromETF(complexEtfArray).toPrettyString())
                .isEqualToIgnoringWhitespace(array.toPrettyString());
        }
    }

    private static <K, V> Map.Entry<K, V> entry(K key, V value)
    {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    private static String kv(String key, Object value)
    {
        return String.format(Locale.ROOT, "\"%s\": %s", key, value);
    }

    private static String kv(String key, String value)
    {
        return String.format(Locale.ROOT, "\"%s\": \"%s\"", key, value);
    }

    private static String jsonOf(String... keyValueMapping)
    {
        return Stream.<String>of(keyValueMapping)
                .collect(Collectors.joining(",\n", "{", "}"));
    }
}
