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

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Writes the current public API surface to a baseline file.
 *
 * Run after an intentional API change, then review the diff before committing. A change here is a
 * change to what Java consumers compile against.
 */
@CacheableTask
abstract class GeneratePublicApiDump : DefaultTask()
{
    @get:Input
    abstract val packagePrefix: Property<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val classes: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val classpath: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate()
    {
        val names = PublicApi.classNames(classes.files, packagePrefix.get())
        val api = PublicApi.extract(names, classpath.files.toList())

        if (api.isEmpty())
        {
            throw GradleException("No public API classes were found under ${packagePrefix.get()}; refusing to write an empty baseline.")
        }

        val output = outputFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(PublicApi.render(api))

        logger.lifecycle("Wrote ${api.size} public API classes to ${output.path}")
    }
}
