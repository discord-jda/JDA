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

package net.dv8tion.jda.gradle.spec.generator

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.palantir.javapoet.AnnotationSpec
import com.palantir.javapoet.ClassName
import com.palantir.javapoet.CodeBlock
import com.palantir.javapoet.FieldSpec
import com.palantir.javapoet.JavaFile
import com.palantir.javapoet.MethodSpec
import com.palantir.javapoet.ParameterizedTypeName
import com.palantir.javapoet.TypeName
import com.palantir.javapoet.TypeSpec
import net.dv8tion.jda.gradle.spec.parser.ComponentSchema
import net.dv8tion.jda.gradle.spec.parser.IntegerPropertyFormat
import net.dv8tion.jda.gradle.spec.parser.ParserContext
import net.dv8tion.jda.gradle.spec.parser.PropertySchema
import net.dv8tion.jda.gradle.spec.parser.getSimpleNameFromRef
import java.util.Locale
import javax.annotation.Nonnull
import javax.lang.model.element.Modifier

const val nullableType = "MaybeNull"

class ModelSourceCodeGenerator(val packageName: String, val context: ParserContext, val typeNameModifier: (name: String) -> String = { it }) {
    val includeNonNull: AnnotationSpec
        = AnnotationSpec
            .builder(JsonInclude::class.java)
            .addMember("value", "JsonInclude.Include.NON_NULL")
            .build()

    val modelCache: MutableMap<String, JavaFile> = mutableMapOf()

    fun generate(name: String): JavaFile {
        modelCache[name]?.let {
            return@generate it
        }

        val schema = context.schemas.getValue(name)

        return when (schema) {
            is ComponentSchema.StringComponentSchema -> {
                if (schema.isEnum) {
                    generateStringEnum(name, schema)
                } else {
                    TODO()
                }
            }
            is ComponentSchema.IntegerComponentSchema -> {
                if (schema.isEnum) {
                    generateIntegerEnum(name, schema)
                } else {
                    TODO()
                }
            }
            is ComponentSchema.ObjectComponentSchema -> generateObject(name, schema)
            is ComponentSchema.ComponentSchemaReference -> TODO()
            ComponentSchema.UnionComponentSchema -> TODO()
        }.also { modelCache[name] = it }
    }

    private fun generateStringEnum(name: String, schema: ComponentSchema.StringComponentSchema): JavaFile {
        val classBuilder = TypeSpec
                .enumBuilder(typeNameModifier(name))
                .addModifiers(Modifier.PUBLIC)

        requireNotNull(schema.oneOf)

        classBuilder.addField(
            FieldSpec
                .builder(String::class.java, "id")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build()
        )

        classBuilder.addMethod(
            MethodSpec.methodBuilder("getId")
                .addAnnotation(JsonValue::class.java)
                .addAnnotation(Nonnull::class.java)
                .returns(String::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addCode("return this.id;")
                .build()
        )

        classBuilder.addMethod(
            MethodSpec
                .constructorBuilder()
                .addParameter(String::class.java, "id")
                .addCode("this.id = id;")
                .build()
        )

        for (variant in schema.oneOf) {
            classBuilder.addEnumConstant(
                    variant.title.enumConstantIdentifier(),
                    TypeSpec.anonymousClassBuilder($$"$S", variant.const).build())
        }

        return JavaFile.builder(packageName, classBuilder.build())
                .skipJavaLangImports(true).build()
    }

    private fun generateIntegerEnum(name: String, schema: ComponentSchema.IntegerComponentSchema): JavaFile {
        val classBuilder = TypeSpec
            .enumBuilder(typeNameModifier(name))
            .addModifiers(Modifier.PUBLIC)

        requireNotNull(schema.oneOf)

        classBuilder.addField(
            FieldSpec
                .builder(Int::class.java, "id")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build()
        )

        classBuilder.addMethod(
            MethodSpec.methodBuilder("getId")
                .addAnnotation(JsonValue::class.java)
                .returns(Int::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addCode("return this.id;")
                .build()
        )

        classBuilder.addMethod(
            MethodSpec
                .constructorBuilder()
                .addParameter(Int::class.java, "id")
                .addCode("this.id = id;")
                .build()
        )

        for (variant in schema.oneOf) {
            classBuilder.addEnumConstant(
                    variant.title.enumConstantIdentifier(),
                    TypeSpec.anonymousClassBuilder($$"$L", variant.const).build())
        }

        return JavaFile.builder(packageName, classBuilder.build())
                .skipJavaLangImports(true).build()
    }

    private fun generateObject(name: String, schema: ComponentSchema.ObjectComponentSchema): JavaFile {
        val modifiedClassName = typeNameModifier(name)
        val className = ClassName.get(packageName, modifiedClassName)

        val classBuilder = TypeSpec
                .classBuilder(modifiedClassName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(includeNonNull)

        if (schema.properties != null) {
            for ((name, value) in schema.properties.entries) {
                val config = FieldConfig(schema, value, name)
                config.generate(classBuilder, className)
            }
        }

        return JavaFile.builder(packageName, classBuilder.build())
                .skipJavaLangImports(true).build()
    }

    private fun resolvePropertyTypeName(property: PropertySchema): TypeName = when (property) {
        is PropertySchema.StringProperty -> TypeName.get(String::class.java)
        is PropertySchema.BooleanProperty -> TypeName.get(Boolean::class.java)
        is PropertySchema.NumberProperty -> TypeName.get(Double::class.java)
        is PropertySchema.IntegerProperty -> TypeName.get(when (property.format) {
            IntegerPropertyFormat.INT -> Int::class.java
            else -> Long::class.java
        })
        is PropertySchema.ObjectProperty -> TypeName.get(Object::class.java)
        is PropertySchema.ReferenceProperty -> resolveReferencedTypeName(property.`$ref`)
        is PropertySchema.ArrayProperty -> resolveArrayTypeName(property)
        is PropertySchema.UnionProperty -> TypeName.get(Object::class.java)
        PropertySchema.NullProperty -> TypeName.get(Object::class.java)
    }

    private fun resolveReferencedTypeName(ref: String): TypeName {
        val resolved = context.resolveRef(ref)
        return when (resolved) {
            is ComponentSchema.ObjectComponentSchema -> {
                val resolvedReferenceType = typeNameModifier(getSimpleNameFromRef(ref))
                return ClassName.get(packageName, resolvedReferenceType)
            }

            is ComponentSchema.IntegerComponentSchema -> {
                if (resolved.isEnum) {
                    val resolvedReferenceType = typeNameModifier(getSimpleNameFromRef(ref))
                    ClassName.get(packageName, resolvedReferenceType)
                } else {
                    when (resolved.format) {
                        IntegerPropertyFormat.INT -> TypeName.get(Int::class.java)
                        else -> TypeName.get(Long::class.java)
                    }
                }
            }

            is ComponentSchema.StringComponentSchema -> {
                if (resolved.isEnum) {
                    val resolvedReferenceType = typeNameModifier(getSimpleNameFromRef(ref))
                    ClassName.get(packageName, resolvedReferenceType)
                } else {
                    TypeName.get(String::class.java)
                }
            }
            is ComponentSchema.ComponentSchemaReference -> TODO()
            ComponentSchema.UnionComponentSchema -> TODO()
        }
    }

    private fun resolveArrayTypeName(property: PropertySchema.ArrayProperty): TypeName {
        val itemType = when (property.items) {
            is PropertySchema.StringProperty -> TypeName.get(String::class.java)
            is PropertySchema.IntegerProperty -> when (property.items.format) {
                IntegerPropertyFormat.INT -> TypeName.get(Int::class.java)
                else -> TypeName.get(Long::class.java)
            }

            is PropertySchema.NumberProperty -> TypeName.get(Double::class.java)
            is PropertySchema.BooleanProperty -> TypeName.get(Boolean::class.java)
            is PropertySchema.ReferenceProperty -> resolveReferencedTypeName(property.items.`$ref`)
            is PropertySchema.UnionProperty -> TypeName.get(Object::class.java)
            is PropertySchema.ArrayProperty -> resolveArrayTypeName(property.items)
            is PropertySchema.ObjectProperty -> TypeName.get(Object::class.java)
            PropertySchema.NullProperty -> TypeName.get(Object::class.java)
        }

        return ParameterizedTypeName.get(ClassName.get(List::class.java), itemType)
    }

    inner class FieldConfig(
        private val name: String,
        val nullable: Boolean,
        val optional: Boolean,
        private val type: TypeName,
    ) {
        constructor(schema: ComponentSchema.ObjectComponentSchema, property: PropertySchema, name: String) : this(
                name = name,
            nullable = property.nullable,
            optional = schema.required?.contains(name) != true,
                type = resolvePropertyTypeName(property)
        )

        val normalizedIdentifier: String = "_$name"
        val camelCaseName: String = name.camelCase()
        val getterPrefix: String get() = when {
            type.box() == TypeName.BOOLEAN.box() -> "is"
            else -> "get"
        }

        fun getFieldType(): TypeName {
            return if (nullable) {
                ParameterizedTypeName.get(
                        ClassName.get(packageName, nullableType),
                        type.box())
            } else if (optional) {
                type.box()
            } else {
                type
            }
        }

        fun getContentType(): TypeName {
            return if (optional || nullable) {
                type.box()
            } else {
                type
            }
        }

        fun generateGetter(): MethodSpec {
            return MethodSpec.methodBuilder("${getterPrefix}${this.camelCaseName}")
                .addAnnotation(JsonIgnore::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addCode(this.getterCodeBlock())
                .returns(getContentType())
                .build()
        }

        fun generateSetter(className: TypeName): MethodSpec {
            return MethodSpec.methodBuilder("set${this.camelCaseName}")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(getContentType(), this.normalizedIdentifier)
                .addCode(this.setterCodeBlock())
                .returns(className)
                .build()
        }

        fun generateField(): FieldSpec {
            val spec = FieldSpec.builder(getFieldType(), this.normalizedIdentifier, Modifier.PRIVATE)

            spec.addAnnotation(
                AnnotationSpec
                    .builder(JsonProperty::class.java)
                    .addMember("value", $$"$S", name)
                    .build()
            )

            return spec.build()
        }

        fun generate(classBuilder: TypeSpec.Builder, className: ClassName) {
            classBuilder.addField(generateField())
            classBuilder.addMethod(generateGetter())
            classBuilder.addMethod(generateSetter(className))
        }

        fun getterCodeBlock(): CodeBlock = when {
            nullable -> CodeBlock.of("return this.$normalizedIdentifier == null ? null : this.$normalizedIdentifier.value();")
            else -> CodeBlock.of("return this.$normalizedIdentifier;")
        }

        fun setterCodeBlock(): CodeBlock = when {
            nullable -> CodeBlock.of("this.$normalizedIdentifier = new $nullableType<>($normalizedIdentifier);\nreturn this;")
            else -> CodeBlock.of("this.$normalizedIdentifier = $normalizedIdentifier;\nreturn this;")
        }
    }
}

fun String.enumConstantIdentifier() = replace("-", "_").uppercase(Locale.ROOT)

fun String.camelCase() = replace(Regex("\\b\\w|_\\w")) {
    it.value.removePrefix("_").uppercase(Locale.ROOT)
}
