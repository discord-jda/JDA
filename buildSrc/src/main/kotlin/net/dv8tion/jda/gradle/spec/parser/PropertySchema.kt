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

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.annotation.JsonDeserialize

class PropertySchemaDeserializer : ValueDeserializer<PropertySchema>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PropertySchema {
        val node = p.readValueAs(JsonNode::class.java)

        if (node.hasNonNull($$"$ref")) {
            return PropertySchema.ReferenceProperty(node.required($$"$ref").asString(), false)
        }

        var nullable = false
        val type: JsonNode? = node["type"]


        val typeId: String? = if (type?.isArray == true) {
            val arrayItems = type.values()
            nullable = arrayItems.any { it.isString && it.asString() == "null" }
            val actualType = arrayItems.find { it.isString && it.asString() != "null" }

            if (arrayItems.size != 2 || !nullable || actualType === null) {
                throw IllegalStateException("Unexpected array for property type $type\n${node.toPrettyString()}")
            }

            actualType.requireNonNull<JsonNode>().asString()
        } else {
            type?.asString()
        }

        return when (typeId) {
            "string" -> PropertySchema.StringProperty(nullable, format = StringPropertyFormat.parse(node["format"]))
            "integer" -> PropertySchema.IntegerProperty(nullable, format = IntegerPropertyFormat.parse(node["format"]))
            "number" -> PropertySchema.NumberProperty(nullable, format = NumberPropertyFormat.parse(node["format"]))
            "boolean" -> PropertySchema.BooleanProperty(nullable)
            "object" -> PropertySchema.ObjectProperty(nullable, additionalProperties = ctxt.readTreeAsValue(node["additionalProperties"], PropertySchema::class.java))
            "array" -> PropertySchema.ArrayProperty(nullable, items = ctxt.readTreeAsValue(node["items"], PropertySchema::class.java))
            "null" -> PropertySchema.NullProperty
            else -> {
                if (typeId == null && node.hasNonNull("oneOf")) {
                    val oneOf = node["oneOf"].values()

                    if (oneOf.size == 2) {
                        val nullable = oneOf.any { it["type"]?.asString() == "null" }
                        val ref = oneOf.find { it.hasNonNull($$"$ref") }?.required($$"$ref")?.asString()
                        if (ref != null) {
                            return PropertySchema.ReferenceProperty(ref, nullable)
                        }
                    }
                }
                if (typeId == null && (node.hasNonNull("oneOf") || node.hasNonNull("anyOf"))) {
                    return PropertySchema.UnionProperty(nullable)
                }

                throw IllegalStateException("Unexpected property type $typeId\n${node.toPrettyString()}")
            }
        }
    }
}

@JsonDeserialize(using = PropertySchemaDeserializer::class)
sealed class PropertySchema(
        val type: PropertyType,
        open val nullable: Boolean
) {
    data class ReferenceProperty(val `$ref`: String, override val nullable: Boolean)
        : PropertySchema(PropertyType.REFERENCE, false)

    data class StringProperty(
        override val nullable: Boolean,
        val format: StringPropertyFormat?,
    ) : PropertySchema(PropertyType.STRING, nullable)

    data class IntegerProperty(
        override val nullable: Boolean,
        val format: IntegerPropertyFormat?,
    ) : PropertySchema(PropertyType.INTEGER, nullable)

    data class NumberProperty(
        override val nullable: Boolean,
        val format: NumberPropertyFormat?,
    ) : PropertySchema(PropertyType.NUMBER, nullable)

    data class BooleanProperty(override val nullable: Boolean)
        : PropertySchema(PropertyType.BOOLEAN, nullable)

    data class ObjectProperty(
        override val nullable: Boolean,
        val additionalProperties: PropertySchema?,
    ) : PropertySchema(PropertyType.OBJECT, nullable)

    data class ArrayProperty(
        override val nullable: Boolean,
        val items: PropertySchema
    ) : PropertySchema(PropertyType.ARRAY, nullable)

    object NullProperty
        : PropertySchema(PropertyType.NULL, true)

    data class UnionProperty(override val nullable: Boolean)
        : PropertySchema(PropertyType.UNION, nullable)
}

enum class PropertyType {
    REFERENCE,
    STRING,
    INTEGER,
    NUMBER,
    BOOLEAN,
    OBJECT,
    ARRAY,
    NULL,

    UNION,
}

enum class StringPropertyFormat {
    SNOWFLAKE,
    URI,
    DATE_TIME,
    NONCE,
    ;

    companion object {
        fun parse(node: JsonNode?): StringPropertyFormat? {
            if (node == null || node.isNull || !node.isString) {
                return null
            }

            return when (node.asString()) {
                "snowflake" -> SNOWFLAKE
                "uri" -> URI
                "date-time" -> DATE_TIME
                "nonce" -> NONCE
                else -> throw IllegalStateException("Unexpected string property format: $node")
            }
        }
    }
}

enum class IntegerPropertyFormat {
    INT,
    LONG,
    ;

    companion object {
        fun parse(node: JsonNode?): IntegerPropertyFormat? {
            if (node == null || node.isNull || !node.isString) {
                return null
            }

            return when (node.asString()) {
                "int32" -> INT
                "int64" -> LONG
                else -> throw IllegalStateException("Unexpected integer property format: $node")
            }
        }
    }
}

enum class NumberPropertyFormat {
    DOUBLE,
    ;

    companion object {
        fun parse(node: JsonNode?): NumberPropertyFormat? {
            if (node == null || node.isNull || !node.isString) {
                return null
            }

            return when (node.asString()) {
                "double" -> DOUBLE
                else -> throw IllegalStateException("Unexpected number property format: $node")
            }
        }
    }
}

