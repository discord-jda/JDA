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

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.type.ClassOrInterfaceType
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class FilterGeneratedTypesTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:SkipWhenEmpty
    @get:IgnoreEmptyDirectories
    abstract val directory: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val suffix: Property<String>

    @get:Input
    @get:Optional
    abstract val includes: ListProperty<String>

    @TaskAction
    fun apply() {
        if (!includes.isPresent || includes.get().isEmpty()) {
            return
        }

        val packageName = "net.dv8tion.jda.internal.generated"

        val dir = directory.get().asFile.resolve(packageName.replace(".", File.separator))
        val typeSuffix = suffix.getOrElse("")

        val inclusions = includes.get().map { "$it$typeSuffix" }
        val filesToKeep = mutableSetOf<String>()

        val parser = JavaParser()

        fun parseDependencies(file: File) {
            if (!filesToKeep.add(file.name)) {
                return
            }

            val parsed = parser.parse(file)
            val compilationUnit = parsed.result.orElseThrow {
                IllegalArgumentException("Could not parse file ${file.absolutePath}")
            }

            val types = compilationUnit.findAll(ClassOrInterfaceType::class.java)

            for (typeDeclaration in types) {
                val typeName = typeDeclaration.name.asString()
                val dependencyFile = dir.resolve("$typeName.java")
                if (dependencyFile.exists()) {
                    parseDependencies(dependencyFile)
                }
            }
        }

        for (inclusion in inclusions) {
            val fileName = "$inclusion.java"
            val file = dir.resolve(fileName)
            if (file.exists() && file.canRead()) {
                parseDependencies(file)
            }
        }

        for (file in dir.listFiles()) {
            if (!filesToKeep.contains(file.name)) {
                file.delete()
            }
        }
    }
}
