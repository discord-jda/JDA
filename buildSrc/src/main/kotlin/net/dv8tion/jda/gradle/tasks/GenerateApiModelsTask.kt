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

import net.dv8tion.jda.gradle.spec.generator.ModelSourceCodeGenerator
import net.dv8tion.jda.gradle.spec.parser.ApiSpecParser
import net.dv8tion.jda.gradle.spec.parser.ComponentSchemaType
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@CacheableTask
abstract class GenerateApiModelsTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val apiSpecFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val suffix: Property<String>

    @TaskAction
    fun generate() {
        val output = outputDirectory.get().asFile.toPath()
        val specFile = apiSpecFile.get().asFile
        val generatorSuffix = suffix.getOrElse("")

        if (output.exists()) {
            output.toFile().deleteRecursively()
        }

        output.createDirectories()

        val context = ApiSpecParser(specFile).parse()
        val generator = ModelSourceCodeGenerator("net.dv8tion.jda.internal.generated", context, {
            "$it$generatorSuffix"
        })

        for (entry in context.schemas.entries.filter { it.value.type == ComponentSchemaType.OBJECT }) {
            val javaFile = generator.generate(entry.key)
            javaFile.writeToPath(output)
        }
    }
}
