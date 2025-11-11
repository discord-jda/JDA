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

import org.openrewrite.ExecutionContext
import org.openrewrite.NlsRewrite
import org.openrewrite.Recipe
import org.openrewrite.java.JavaIsoVisitor
import org.openrewrite.java.tree.J
import org.openrewrite.java.tree.Javadoc
import org.openrewrite.java.tree.Space
import org.openrewrite.marker.Markers
import java.time.Duration
import java.util.UUID

class JavadocFormatter : Recipe() {
    override fun getDisplayName(): @NlsRewrite.DisplayName String {
        return "Reformat Javadoc in JDA style"
    }

    override fun getDescription(): @NlsRewrite.Description String {
        return "This recipe will apply the JDA javadoc style to all provided sources"
    }

    override fun getEstimatedEffortPerOccurrence(): Duration {
        return Duration.ofSeconds(1)
    }

    override fun getVisitor(): JavadocFormatVisitor {
        return JavadocFormatVisitor()
    }
}

class JavadocFormatVisitor : JavaIsoVisitor<ExecutionContext>() {
    override fun visitClassDeclaration(classDecl: J.ClassDeclaration, ctx: ExecutionContext): J.ClassDeclaration {
        val modified = classDecl.withPrefix(updateJavadocs(classDecl.prefix))
        return super.visitClassDeclaration(modified, ctx)
    }

    override fun visitMethodDeclaration(method: J.MethodDeclaration, ctx: ExecutionContext): J.MethodDeclaration {
        val modified = method.withPrefix(updateJavadocs(method.prefix))
        return super.visitMethodDeclaration(modified, ctx)
    }

    fun updateJavadocs(prefix: Space): Space {
        val javadocs = getJavadocs(prefix)

        if (javadocs !== null) {
            val mutableComments = ArrayList(prefix.comments)
            val index = mutableComments.indexOf(javadocs)
            mutableComments[index] = formatJavadoc(javadocs)
            return prefix.withComments(mutableComments)
        }

        return prefix
    }

    fun getJavadocs(space: Space): Javadoc.DocComment? {
        return space.comments.find { comment -> comment is Javadoc.DocComment } as Javadoc.DocComment?
    }

    fun formatJavadoc(javadocs: Javadoc.DocComment): Javadoc.DocComment {
        val parsedJavadocs = parseJavadocs(javadocs)
        val recompiledJavadocs = compileJavadocs(parsedJavadocs)

        return javadocs.withBody(recompiledJavadocs)
    }

    fun parseJavadocs(javadocs: Javadoc.DocComment): ParsedJavadocs {
        val iterator = javadocs.body.iterator()

        var isDescription = true
        var indentation = ""

        val description = mutableListOf<Javadoc>()
        val groupedTags = mutableMapOf<JavadocTagVariant, MutableList<Javadoc>>()

        while (iterator.hasNext()) {
            val element = iterator.next()
            val tagVariant = getJavadocTagVariant(element)

            if (element is Javadoc.LineBreak) {
                indentation = element.margin.takeWhile { it != '*' }
            }

            if (tagVariant != null) {
                isDescription = false
                val group = groupedTags.computeIfAbsent(tagVariant) { mutableListOf() }
                group.add(element)
            } else if (isDescription) {
                description.add(element)
            }
        }

        return ParsedJavadocs(description, groupedTags, indentation)
    }

    fun compileJavadocs(parsed: ParsedJavadocs): MutableList<Javadoc> {
        val (description, tags, indentation) = parsed
        val docs = mutableListOf<Javadoc>()

        trimWhitespace(description)

        docs.add(lineBreak(indentation))
        docs.add(space())
        docs.addAll(description.map { it })
        for (entry in JavadocTagVariant.entries) {
            if (entry === JavadocTagVariant.SINCE) {
                continue
            }

            tags[entry]?.let { group ->
                docs.add(lineBreak(indentation))
                group.forEach { element ->
                    docs.add(lineBreak(indentation))
                    docs.add(space())

                    if (element is Javadoc.See) {
                        val reference = element.reference.toMutableList()
                        reference.removeIf { it is Javadoc.LineBreak }
                        docs.add(element.withReference(reference))
                    } else {
                        docs.add(element)
                    }
                }
            }
        }

        docs.add(endLineBreak(indentation))
        return docs
    }

    fun getJavadocTagVariant(comment: Javadoc): JavadocTagVariant? {
        return JavadocTagVariant.entries.find { it.instanceType.isInstance(comment) }
    }

    fun lineBreak(indentation: String): Javadoc.LineBreak {
        return Javadoc.LineBreak(UUID.randomUUID(), "$indentation*", Markers.EMPTY)
    }

    fun endLineBreak(indentation: String): Javadoc.LineBreak {
        return Javadoc.LineBreak(UUID.randomUUID(), indentation, Markers.EMPTY)
    }

    fun space(): Javadoc.Text {
        return Javadoc.Text(UUID.randomUUID(), Markers.EMPTY, " ")
    }

    fun trimWhitespace(description: MutableList<Javadoc>) {
        removeLeadingWhitespace(description)
        removeTrailingWhitespace(description)
    }

    fun removeLeadingWhitespace(description: MutableList<Javadoc>) {
        while (description.isNotEmpty() && isBlank(description.first())) {
            description.removeFirst()
        }
    }

    fun removeTrailingWhitespace(description: MutableList<Javadoc>) {
        while (description.isNotEmpty() && isBlank(description.last())) {
            description.removeLast()
        }
    }

    fun isBlank(comment: Javadoc): Boolean {
        return when (comment) {
            is Javadoc.LineBreak -> true
            is Javadoc.Text -> comment.text.isBlank()
            else -> false
        }
    }
}

data class ParsedJavadocs(
        val description: MutableList<Javadoc>,
        val tags: Map<JavadocTagVariant, List<Javadoc>>,
        val indentation: String
)

// Ordered by preferred order in javadocs
enum class JavadocTagVariant(val instanceType: Class<out Javadoc>) {
    PARAM(Javadoc.Parameter::class.java),
    THROWS(Javadoc.Throws::class.java),
    RETURN(Javadoc.Return::class.java),
    DEPRECATION(Javadoc.Deprecated::class.java),
    SINCE(Javadoc.Since::class.java),
    SEE(Javadoc.See::class.java),
    AUTHOR(Javadoc.Author::class.java),

    // for example @incubating
    UNKNOWN(Javadoc.UnknownBlock::class.java),
}
