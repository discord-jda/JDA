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

package net.dv8tion.jda.internal.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import net.dv8tion.jda.api.exceptions.ParsingException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class SerializationUtil {
    private static final String TRUNCATED_ARRAY = "[…truncated array…]";
    private static final String TRUNCATED_OBJECT = "{…truncated object…}";

    private static final ObjectMapper mapper;
    private static final SimpleModule module;
    private static final MapType mapType;
    private static final CollectionType listType;

    static {
        mapper = new ObjectMapper();
        module = new SimpleModule();
        module.addAbstractTypeMapping(Map.class, HashMap.class);
        module.addAbstractTypeMapping(List.class, ArrayList.class);
        mapper.registerModule(module);
        mapType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
        listType = mapper.getTypeFactory().constructRawCollectionType(ArrayList.class);
    }

    @Nonnull
    public static MapType getMapType() {
        return mapType;
    }

    @Nonnull
    public static CollectionType getListType() {
        return listType;
    }

    @Nonnull
    public static byte[] toJson(@Nonnull Object data) {
        Checks.notNull(data, "Data");
        try {
            return mapper.writeValueAsBytes(data);
        } catch (IOException ex) {
            throw new ParsingException(ex);
        }
    }

    @Nonnull
    public static String toJsonString(@Nonnull Object data, boolean pretty) {
        Checks.notNull(data, "Data");

        try {
            ObjectWriter writer = getObjectWriter(pretty);
            return writer.writeValueAsString(data);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Nonnull
    public static ObjectWriter getObjectWriter(boolean pretty) {
        return !pretty
                ? mapper.writer()
                : mapper.writerWithDefaultPrettyPrinter()
                        .with(SerializationFeature.INDENT_OUTPUT)
                        .with(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    }

    @Nonnull
    public static <T> T fromJson(@Nonnull Class<T> clazz, @Nonnull byte[] data) {
        Checks.notNull(clazz, "Class");
        return fromJson(mapper.constructType(clazz), data);
    }

    @Nonnull
    public static <T> T fromJson(@Nonnull JavaType type, @Nonnull byte[] data) {
        Checks.notNull(type, "Type");
        Checks.notNull(data, "Data");

        try {
            return mapper.readValue(data, type);
        } catch (IOException ex) {
            throw new ParsingException(ex);
        }
    }

    @Nonnull
    public static <T> T fromJson(@Nonnull JavaType type, @Nonnull InputStream data) {
        Checks.notNull(type, "Type");
        Checks.notNull(data, "Data");

        try {
            return mapper.readValue(data, type);
        } catch (IOException ex) {
            throw new ParsingException(ex);
        }
    }

    @Nonnull
    public static <T> T fromJson(@Nonnull JavaType type, @Nonnull Reader data) {
        Checks.notNull(type, "Type");
        Checks.notNull(data, "Data");

        try {
            return mapper.readValue(data, type);
        } catch (IOException ex) {
            throw new ParsingException(ex);
        }
    }

    @Nonnull
    public static <T> T fromJson(@Nonnull JavaType type, @Nonnull String data) {
        Checks.notNull(type, "Type");
        Checks.notNull(data, "Data");

        try {
            return mapper.readValue(data, type);
        } catch (IOException ex) {
            throw new ParsingException(ex);
        }
    }

    @Nonnull
    public static String toShallowJsonString(@Nonnull Object object) throws JsonProcessingException {
        JsonNode root = mapper.valueToTree(object);
        JsonNode shallowRoot = pruneOneLevel(root);
        return mapper.writer()
                .with(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .writeValueAsString(shallowRoot);
    }

    private static JsonNode pruneOneLevel(JsonNode n) {
        if (n.isObject()) {
            ObjectNode out = mapper.createObjectNode();
            for (Map.Entry<String, JsonNode> e : n.properties()) {
                JsonNode v = e.getValue();
                if (v.isValueNode()) {
                    out.set(e.getKey(), v);
                } else if (v.isArray()) {
                    out.put(e.getKey(), TRUNCATED_ARRAY);
                } else {
                    out.put(e.getKey(), TRUNCATED_OBJECT);
                }
            }
            return out;
        } else if (n.isArray()) {
            ArrayNode out = mapper.createArrayNode();
            n.values().forEachRemaining(v -> {
                if (v.isValueNode()) {
                    out.add(v);
                } else if (v.isArray()) {
                    out.add(TRUNCATED_ARRAY);
                } else {
                    out.add(TRUNCATED_OBJECT);
                }
            });
            return out;
        }
        return n;
    }
}
