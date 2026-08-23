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

package net.dv8tion.jda.recipe

import org.openrewrite.*
import org.openrewrite.java.JavaIsoVisitor
import org.openrewrite.java.JavaTemplate
import org.openrewrite.java.tree.J
import org.openrewrite.java.tree.TypeUtils
import org.openrewrite.marker.SearchResult
import java.time.Duration

class AddKotlinMutabilityAnnotation : Recipe() {
    override fun getDisplayName(): @NlsRewrite.DisplayName String {
        return "Add collection mutability info for Kotlin consumers"
    }

    override fun getDescription(): @NlsRewrite.Description String {
        return "Adds a Kotlin-specific mutability annotation on all methods returning collections"
    }

    override fun getEstimatedEffortPerOccurrence(): Duration {
        return Duration.ofSeconds(1)
    }

    override fun getVisitor(): TreeVisitor<*, ExecutionContext> {
        return Preconditions.check(ApiPackagePreconditionVisitor(), AddKotlinMutabilityAnnotationVisitor())
    }

    private class ApiPackagePreconditionVisitor : JavaIsoVisitor<ExecutionContext>() {
        override fun visitPackage(pkg: J.Package, p: ExecutionContext): J.Package {
            if (pkg.expression.toString().startsWith("net.dv8tion.jda.api")) {
                return SearchResult.found(pkg)!!
            }

            return super.visitPackage(pkg, p)
        }
    }
}

private val collectionNames = listOf(
        "java.util.Collection",
        "java.util.List",
        "java.util.Set",
        "java.util.Map",
)

class AddKotlinMutabilityAnnotationVisitor : NullableJavaIsoVisitor<ExecutionContext>() {

    override fun visitClassDeclaration(classDecl: J.ClassDeclaration, p: ExecutionContext): J.ClassDeclaration {
        cursor.putMessage("declaringClass", classDecl)
        return super.visitClassDeclaration(classDecl, p)
    }

    override fun visitMethodDeclaration(method: J.MethodDeclaration, ctx: ExecutionContext): J.MethodDeclaration {
        val classDeclaration: J.ClassDeclaration = cursor.getNearestMessage("declaringClass")!!

        // Modify only public methods returning collection interfaces
        if (!isEffectivelyPublic(classDeclaration, method) && !method.hasModifier(J.Modifier.Type.Protected)) {
            return super.visitMethodDeclaration(method, ctx)
        } else if (TypeUtils.asFullyQualified(method.returnTypeExpression?.type)?.fullyQualifiedName !in collectionNames) {
            return super.visitMethodDeclaration(method, ctx)
        }

        val isUnmodifiable = method.leadingAnnotations
                .any { annotation -> annotation.simpleName == "Unmodifiable" || annotation.simpleName == "UnmodifiableView" }

        cursor.putMessage("isUnmodifiable", isUnmodifiable)

        var method = method
        if (isUnmodifiable) {
            if (method.leadingAnnotations.none { it.simpleName == "ReadOnly" }) {
                maybeAddImport("kotlin.annotations.jvm.ReadOnly", false)
                method = JavaTemplate.apply("@ReadOnly", this.cursor, method.coordinates.addAnnotation(AnnotationPlacementByLengthComparator))
            }
        } else {
            if (method.leadingAnnotations.none { it.simpleName == "Mutable" }) {
                maybeAddImport("kotlin.annotations.jvm.Mutable", false)
                method = JavaTemplate.apply("@Mutable", this.cursor, method.coordinates.addAnnotation(AnnotationPlacementByLengthComparator))
            }
        }

        return super.visitMethodDeclaration(method, ctx)
    }

    override fun visitAnnotation(annotation: J.Annotation, p: ExecutionContext): J.Annotation? {
        val isUnmodifiable: Boolean? = cursor.getNearestMessage("isUnmodifiable")

        if (isUnmodifiable == true && annotation.simpleName == "Mutable") {
            // Remove @Mutable if unmodifiable
            return null
        } else if (isUnmodifiable == false && annotation.simpleName == "ReadOnly") {
            // Remove @ReadOnly if assumed mutable
            return null
        }

        return super.visitAnnotation(annotation, p)
    }

    private fun isEffectivelyPublic(clazz: J.ClassDeclaration, method: J.MethodDeclaration): Boolean {
        return method.hasModifier(J.Modifier.Type.Public)
                // Interface method's explicit modifiers only include private (public is redundant)
                || (clazz.kind == J.ClassDeclaration.Kind.Type.Interface && !method.hasModifier(J.Modifier.Type.Private))
    }
}

private object AnnotationPlacementByLengthComparator : Comparator<J.Annotation> {
    override fun compare(o1: J.Annotation, o2: J.Annotation): Int {
        return o1.simpleName.length.compareTo(o2.simpleName.length)
    }
}
