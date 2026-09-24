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
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Fails if anything in the baseline has disappeared from the public API.
 *
 * This is the migration's ABI guardrail: it does not require a bytecode library, so it keeps working
 * at JVM 25 where `binary-compatibility-validator` does not. Additions are allowed; removals and
 * signature changes are not.
 */
@CacheableTask
abstract class VerifyPublicApi : DefaultTask()
{
    @get:Input
    abstract val packagePrefix: Property<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val classes: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val classpath: ConfigurableFileCollection

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val baseline: RegularFileProperty

    @TaskAction
    fun verify()
    {
        val baselineFile = baseline.get().asFile

        if (!baselineFile.exists())
        {
            throw GradleException(
                "No API baseline at ${baselineFile.path}. Generate one with `./gradlew apiDump`."
            )
        }

        val expected = PublicApi.parseDump(baselineFile.readText())
        val names = PublicApi.classNames(classes.files, packagePrefix.get())
        val actual = PublicApi.extract(names, classpath.files.toList())

        val problems = mutableListOf<String>()

        for ((className, expectedMembers) in expected)
        {
            val actualMembers = actual[className]

            if (actualMembers == null)
            {
                problems += "class removed: $className"
                continue
            }

            val actualSet = actualMembers.toSet()
            for (member in expectedMembers)
            {
                if (member !in actualSet)
                {
                    problems += "member removed or changed in $className: $member"
                }
            }
        }

        if (problems.isNotEmpty())
        {
            val report = problems.sorted().joinToString("\n") { "\t- $it" }
            throw GradleException(
                "Public API compatibility check failed. ${problems.size} breaking change(s):\n$report\n\n" +
                    "If the change is intentional, run `./gradlew apiDump` and review the baseline diff."
            )
        }

        val added = actual.keys - expected.keys
        if (added.isNotEmpty())
        {
            logger.lifecycle("Public API check passed with ${added.size} new class(es). Run `./gradlew apiDump` to update the baseline.")
        }
        else
        {
            logger.lifecycle("Public API check passed for ${expected.size} classes.")
        }
    }
}
