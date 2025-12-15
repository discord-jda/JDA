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

package net.dv8tion.jda.gradle.spec.parser

import com.fasterxml.jackson.annotation.JsonAlias
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.annotation.JsonDeserialize

class ComponentSchemaDeserializer : ValueDeserializer<ComponentSchema>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ComponentSchema {
        val node = p.readValueAsTree<JsonNode>()

        if (node.hasNonNull($$"$ref")) {
            return ComponentSchema.ComponentSchemaReference(node[$$"$ref"].asString())
        }

        return when (node["type"]?.asString()) {
            "string" -> ComponentSchema.StringComponentSchema(
                    StringPropertyFormat.parse(node["format"]),
                    node["oneOf"]?.values()?.map(StringVariant::parse))

            "integer" -> ComponentSchema.IntegerComponentSchema(
                    IntegerPropertyFormat.parse(node["format"]),
                    node["oneOf"]?.values()?.map(LongVariant::parse))

            "object" -> ComponentSchema.ObjectComponentSchema(
                    node["properties"]?.let { parseProperties(ctxt, it) },
                    node["required"]?.values()?.map { it.asString() },
                    if (node["additionalProperties"]?.isObject == true) {
                        ctxt.readTreeAsValue(node["additionalProperties"], PropertySchema::class.java)
                    } else {
                        null
                    },
                    node["oneOf"]?.values()?.map { it.asString() })

            else -> {
                if (node.hasNonNull("oneOf") && node["type"] == null) {
                    return ComponentSchema.UnionComponentSchema
                }

                throw IllegalStateException("Unknown type found for component schema: ${node.required("type").asString()}\n${node.toPrettyString()}")
            }
        }
    }

    private fun parseProperties(ctxt: DeserializationContext, node: JsonNode): Map<String, PropertySchema> {
        val map = mutableMapOf<String, PropertySchema>()

        for (entry in node.properties()) {
            // Usually broken type declarations in an api schema on discord's end
            if (entry.value.properties().isEmpty()) {
                continue
            }

            map[entry.key] = ctxt.readTreeAsValue(entry.value, PropertySchema::class.java)
        }

        return map
    }
}

@JsonDeserialize(using = ComponentSchemaDeserializer::class)
sealed interface ComponentSchema {
    val type: ComponentSchemaType

    data class ComponentSchemaReference(val `$ref`: String) : ComponentSchema {
        override val type: ComponentSchemaType = ComponentSchemaType.REFERENCE
    }

    data class StringComponentSchema(val format: StringPropertyFormat?, val oneOf: List<StringVariant>?) : ComponentSchema {
        override val type: ComponentSchemaType = ComponentSchemaType.STRING
    }

    data class IntegerComponentSchema(val format: IntegerPropertyFormat?, val oneOf: List<LongVariant>?) : ComponentSchema {
        override val type: ComponentSchemaType = ComponentSchemaType.INTEGER
    }

    data class ObjectComponentSchema(
            val properties: Map<String, PropertySchema>?,
            val required: List<String>?,
            val additionalProperties: PropertySchema?,
            val oneOf: List<Any>?,
    ) : ComponentSchema {
        override val type: ComponentSchemaType = ComponentSchemaType.OBJECT
    }

    // Omitted for now
    object UnionComponentSchema : ComponentSchema {
        override val type: ComponentSchemaType = ComponentSchemaType.UNION
    }
}

enum class ComponentSchemaType {
    REFERENCE,

    UNION,

    @JsonAlias("string")
    STRING,

    @JsonAlias("integer")
    INTEGER,

    @JsonAlias("object")
    OBJECT,
}
