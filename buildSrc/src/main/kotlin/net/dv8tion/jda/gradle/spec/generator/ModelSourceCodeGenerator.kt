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
import javax.annotation.Nullable
import javax.lang.model.element.Modifier

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
        val classBuilder = TypeSpec
                .classBuilder(typeNameModifier(name))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(includeNonNull)

        if (schema.properties != null) {
            for ((name, value) in schema.properties.entries) {
                when (value) {
                    is PropertySchema.ReferenceProperty -> classBuilder.generateReferenceProperty(schema, name, value)
                    is PropertySchema.StringProperty -> classBuilder.generateStringProperty(schema, name)
                    is PropertySchema.IntegerProperty -> classBuilder.generateIntegerProperty(schema, name, value.format)
                    is PropertySchema.NumberProperty -> classBuilder.generateNumberProperty(schema, name)
                    is PropertySchema.BooleanProperty -> classBuilder.generateBooleanProperty(schema, name)
                    // TODO
                    is PropertySchema.ObjectProperty -> classBuilder.generateObjectProperty(schema, name)
                    is PropertySchema.ArrayProperty -> classBuilder.generateArrayProperty(schema, name, value.items)
                    is PropertySchema.UnionProperty -> classBuilder.generateObjectProperty(schema, name)
                    PropertySchema.NullProperty -> classBuilder.generateObjectProperty(schema, name)
                }
            }
        }

        return JavaFile.builder(packageName, classBuilder.build())
                .skipJavaLangImports(true).build()
    }

    private fun TypeSpec.Builder.generateReferenceProperty(schema: ComponentSchema.ObjectComponentSchema, name: String, property: PropertySchema.ReferenceProperty) {
        val nullable = schema.isNullable(name)

        val ref = property.`$ref`
        val referencedSchema = context.resolveRef(ref)

        when (referencedSchema) {
            is ComponentSchema.StringComponentSchema -> return generateStringProperty(schema, name)
            is ComponentSchema.IntegerComponentSchema -> return generateIntegerProperty(schema, name, referencedSchema.format)
            is ComponentSchema.ObjectComponentSchema -> {}
            else -> throw IllegalStateException("Referenced schema $referencedSchema is not supported as reference property.\n$schema")
        }

        val typeName = resolveReferencedTypeName(ref)

        addField(generateField(name, typeName, nullable))
        addMethods(generateEncapsulation(name, typeName))
    }

    private fun TypeSpec.Builder.generateStringProperty(schema: ComponentSchema.ObjectComponentSchema, name: String) {
        val nullable = schema.isNullable(name)
        addField(generateField(name, String::class.java, nullable))
        addMethods(generateEncapsulation(name, String::class.java, nullable))
    }

    private fun TypeSpec.Builder.generateIntegerProperty(schema: ComponentSchema.ObjectComponentSchema, name: String, format: IntegerPropertyFormat?) {
        val nullable = schema.isNullable(name)

        val type = if (format == IntegerPropertyFormat.INT) {
            Int::class.java
        } else {
            Long::class.java
        }

        addField(generateField(name, type, nullable))
        addMethods(generateEncapsulation(name, type, nullable))
    }

    private fun TypeSpec.Builder.generateNumberProperty(schema: ComponentSchema.ObjectComponentSchema, name: String) {
        val nullable = schema.isNullable(name)
        addField(generateField(name, Double::class.java, nullable))
        addMethods(generateEncapsulation(name, Double::class.java, nullable))
    }

    private fun TypeSpec.Builder.generateBooleanProperty(schema: ComponentSchema.ObjectComponentSchema, name: String) {
        val nullable = schema.isNullable(name)
        addField(generateField(name, Boolean::class.java, nullable))
        addMethods(generateEncapsulation(name, Boolean::class.java, nullable))
    }

    private fun TypeSpec.Builder.generateObjectProperty(schema: ComponentSchema.ObjectComponentSchema, name: String) {
        val nullable = schema.isNullable(name)
        // TODO: Properly resolve Maps and such
        addField(generateField(name, Object::class.java, nullable))
        addMethods(generateEncapsulation(name, Object::class.java, nullable))
    }

    private fun TypeSpec.Builder.generateArrayProperty(schema: ComponentSchema.ObjectComponentSchema, name: String, items: PropertySchema) {
        val nullable = schema.isNullable(name)

        val itemType = when (items) {
            is PropertySchema.StringProperty -> getTypeName(String::class.java, true)
            is PropertySchema.IntegerProperty -> when (items.format) {
                IntegerPropertyFormat.INT -> getTypeName(Int::class.java, true)
                else -> getTypeName(Long::class.java, true)
            }
            is PropertySchema.NumberProperty -> getTypeName(Double::class.java, true)
            is PropertySchema.BooleanProperty -> getTypeName(Boolean::class.java, true)
            is PropertySchema.ReferenceProperty -> resolveReferencedTypeName(items.`$ref`)
            is PropertySchema.UnionProperty -> {
                // TODO
                return
            }
            else -> {
                throw IllegalStateException("Unexpected property type $items")
            }
        }

        val typeName = ParameterizedTypeName.get(ClassName.get(List::class.java), itemType)

        addField(generateField(name, typeName, nullable))
        addMethods(generateEncapsulation(name, typeName))
    }

    private fun getNormalizedIdentifier(name: String): String {
        return "_$name"
    }

    private fun generateField(name: String, type: Class<*>, nullable: Boolean): FieldSpec {
        return generateField(name, getTypeName(type, nullable), nullable)
    }

    private fun generateField(name: String, typeName: TypeName, nullable: Boolean): FieldSpec {
        return FieldSpec
                .builder(typeName, getNormalizedIdentifier(name), Modifier.PRIVATE)
                .generateAnnotations(name, nullable)
                .build()
    }

    private fun FieldSpec.Builder.generateAnnotations(name: String, nullable: Boolean): FieldSpec.Builder {
        addAnnotation(
            AnnotationSpec
                .builder(JsonProperty::class.java)
                .addMember("value", $$"$S", name)
                .build()
        )

        if (nullable) {
            addAnnotation(Nullable::class.java)
        } else {
            addAnnotation(Nonnull::class.java)
        }

        return this
    }

    private fun generateEncapsulation(name: String, type: Class<*>, nullable: Boolean): List<MethodSpec> {
        return generateEncapsulation(name, getTypeName(type, nullable))
    }

    private fun generateEncapsulation(name: String, typeName: TypeName): List<MethodSpec> {
        val camelCaseName = name.camelCase()
        val fieldName = getNormalizedIdentifier(name)

        val getterPrefix = if (typeName == TypeName.BOOLEAN || typeName == TypeName.BOOLEAN.box()) {
            "is"
        } else {
            "get"
        }

        return listOf(
            MethodSpec.methodBuilder("$getterPrefix$camelCaseName")
                .addAnnotation(JsonIgnore::class.java)
                .addModifiers(Modifier.PUBLIC)
                .returns(typeName)
                .addCode("return this.$fieldName;")
                .build(),
            MethodSpec.methodBuilder("set$camelCaseName")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(typeName, fieldName)
                .returns(Void.TYPE)
                .addCode("this.$fieldName = $fieldName;")
                .build(),
        )
    }

    private fun ComponentSchema.ObjectComponentSchema.isNullable(name: String): Boolean {
        return required?.contains(name) != true || properties?.getValue(name)?.nullable == true
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
                        IntegerPropertyFormat.INT -> getTypeName(Int::class.java, true)
                        else -> getTypeName(Long::class.java, true)
                    }
                }
            }

            is ComponentSchema.StringComponentSchema -> {
                if (resolved.isEnum) {
                    val resolvedReferenceType = typeNameModifier(getSimpleNameFromRef(ref))
                    ClassName.get(packageName, resolvedReferenceType)
                } else {
                    getTypeName(String::class.java, true)
                }
            }
            is ComponentSchema.ComponentSchemaReference -> TODO()
            ComponentSchema.UnionComponentSchema -> TODO()
        }
    }
}

fun String.enumConstantIdentifier() = replace("-", "_").uppercase(Locale.ROOT)

fun String.camelCase() = replace(Regex("\\b\\w|_\\w")) {
    it.value.removePrefix("_").uppercase(Locale.ROOT)
}

fun getTypeName(type: Class<*>, nullable: Boolean): TypeName {
    return if (nullable) {
        TypeName.get(type).box()
    } else {
        TypeName.get(type)
    }
}
