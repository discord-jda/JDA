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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public class SerializationUtil
{
    private static final String TRUNCATED_ARRAY = "[…truncated array…]";
    private static final String TRUNCATED_OBJECT = "{…truncated object…}";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toShallowJsonString(Object object) throws JsonProcessingException
    {
        JsonNode root = objectMapper.valueToTree(object);
        JsonNode shallowRoot = pruneOneLevel(root);
        return objectMapper
                .writer()
                .with(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .writeValueAsString(shallowRoot);
    }

    private static JsonNode pruneOneLevel(JsonNode n)
    {
        if (n.isObject())
        {
            ObjectNode out = objectMapper.createObjectNode();
            for (Map.Entry<String, JsonNode> e : n.properties())
            {
                JsonNode v = e.getValue();
                if (v.isValueNode())
                    out.set(e.getKey(), v);
                else if (v.isArray())
                    out.put(e.getKey(), TRUNCATED_ARRAY);
                else
                    out.put(e.getKey(), TRUNCATED_OBJECT);
            }
            return out;
        }
        else if (n.isArray())
        {
            ArrayNode out = objectMapper.createArrayNode();
            n.values().forEachRemaining(v ->
            {
                if (v.isValueNode())
                    out.add(v);
                else if (v.isArray())
                    out.add(TRUNCATED_ARRAY);
                else
                    out.add(TRUNCATED_OBJECT);

            });
            return out;
        }
        return n;
    }
}
