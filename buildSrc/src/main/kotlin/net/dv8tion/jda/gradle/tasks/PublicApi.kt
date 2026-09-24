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

package net.dv8tion.jda.gradle.tasks

import java.io.File

/**
 * Extracts and compares the public API surface of the public API packages.
 *
 * Deliberately built on the JDK's own `javap` rather than a bytecode library. The usual candidate,
 * `binary-compatibility-validator`, bundles an ASM that rejects major version 69 ("Unsupported class
 * file major version 69"), so it cannot even read this project's output. `javap` ships with the JDK
 * that produced the classes and always understands them, at the cost of parsing its text output.
 *
 * The comparison is asymmetric on purpose:
 * - a class or member present in the baseline but missing now is **breaking** and fails the build;
 * - something newly added is allowed, because Kotlin introduces synthetic members and
 *   `DefaultImpls`/`Companion` holders that Java consumers never see as API.
 *
 * `Compiled from "X.java"` is dropped from the signature, since converting a class to Kotlin changes
 * it to `X.kt` without any change to the API.
 */
internal object PublicApi
{
    private const val BATCH_SIZE = 200

    fun javapExecutable(): String = File(System.getProperty("java.home"), "bin/javap").absolutePath

    /**
     * Maps the compiled class roots to binary names under [packagePrefix], excluding `package-info`.
     *
     * A root may be a directory (the Java output of a source set) or an individual `.class` file.
     * Both shapes occur in practice: a `FileTree` filtered to class files, such as the Kotlin
     * compile output, resolves to loose files rather than a directory, so skipping non-directories
     * would silently drop every Kotlin class from the comparison.
     */
    fun classNames(roots: Iterable<File>, packagePrefix: String): List<String>
    {
        val names = mutableSetOf<String>()

        fun add(relative: String)
        {
            val name = relative.removeSuffix(".class").replace('/', '.')
            val simpleName = name.substringAfterLast('.')

            if (name.startsWith(packagePrefix) && simpleName != "package-info")
            {
                names += name
            }
        }

        for (root in roots)
        {
            if (root.isDirectory)
            {
                root.walkTopDown()
                    .filter { it.isFile && it.name.endsWith(".class") }
                    .forEach { file ->
                        add(file.relativeTo(root).path.replace(File.separatorChar, '/'))
                    }
            }
            else if (root.isFile && root.name.endsWith(".class"))
            {
                // Binary names are resolved against the classpath rather than a source root, so the
                // file's location on disk is irrelevant; its own name is enough for javap.
                add(root.name)
            }
        }

        return names.sorted()
    }

    /** Runs javap over [classNames] and returns `binary name -> normalized member lines`. */
    fun extract(classNames: List<String>, classpath: List<File>): Map<String, List<String>>
    {
        if (classNames.isEmpty()) return emptyMap()

        val result = sortedMapOf<String, List<String>>()
        val classpathArg = classpath.joinToString(File.pathSeparator) { it.absolutePath }

        for (batch in classNames.chunked(BATCH_SIZE))
        {
            val command = mutableListOf(javapExecutable(), "-public", "-classpath", classpathArg)
            command += batch

            val process = ProcessBuilder(command)
                .redirectErrorStream(false)
                .start()

            val stdout = process.inputStream.bufferedReader().readText()
            val stderr = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            if (exitCode != 0)
            {
                throw IllegalStateException("javap failed with exit code $exitCode:\n$stderr")
            }

            result.putAll(parse(stdout))
        }

        return result
    }

    /** Parses javap output for one or more classes. */
    fun parse(text: String): Map<String, List<String>>
    {
        val result = sortedMapOf<String, List<String>>()
        var declaration: String? = null
        var currentClass: String? = null
        val members = mutableListOf<String>()

        fun flush()
        {
            val header = declaration
            val name = currentClass
            if (header != null && name != null)
            {
                result[name] = (members + header).sorted()
            }

            declaration = null
            currentClass = null
            members.clear()
        }

        for (rawLine in text.lineSequence())
        {
            val line = rawLine.trim()

            if (line.isEmpty()) continue
            if (line.startsWith("Compiled from")) continue
            if (line.startsWith("Warning:")) continue

            if (line == "}")
            {
                flush()
                continue
            }

            if (currentClass == null)
            {
                // First braces of a class block: the header ends the first line of a javap entry.
                if (line.endsWith("{"))
                {
                    val name = binaryName(line)
                    if (name != null)
                    {
                        declaration = normalize(line.removeSuffix("{"))
                        currentClass = name
                        continue
                    }
                }
                // Anything else before a declaration is not part of a signature.
                continue
            }

            members += normalize(line)
        }

        return result
    }

    private val classDeclaration = Regex("""\b(?:class|interface|enum)\s+([\w.$]+)""")

    private fun binaryName(headerLine: String): String? =
        classDeclaration.find(headerLine)?.groupValues?.get(1)

    private fun normalize(line: String): String =
        line.removeSuffix("{").trim().replace(Regex("\\s+"), " ")

    /** Renders an extracted API map in the stable on-disk format. */
    fun render(api: Map<String, List<String>>): String = buildString {
        for ((name, lines) in api.toSortedMap())
        {
            append(name).append('\n')
            for (line in lines.sorted())
            {
                append("  ").append(line).append('\n')
            }
            append('\n')
        }
    }

    /** Parses the on-disk format written by [render]. */
    fun parseDump(text: String): Map<String, List<String>>
    {
        val result = sortedMapOf<String, List<String>>()
        var current: String? = null
        val members = mutableListOf<String>()

        for (rawLine in text.lineSequence())
        {
            if (rawLine.isBlank()) continue

            if (!rawLine.startsWith("  "))
            {
                current?.let { result[it] = members.toList() }
                members.clear()
                current = rawLine.trim()
                continue
            }

            members += rawLine.trim()
        }

        current?.let { result[it] = members.toList() }

        return result
    }
}
