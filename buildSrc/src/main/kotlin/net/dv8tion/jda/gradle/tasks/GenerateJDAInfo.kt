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
 *
 */

package net.dv8tion.jda.gradle.tasks

import net.dv8tion.jda.gradle.Version
import net.dv8tion.jda.gradle.nullableReplacement
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.InputStream

@CacheableTask
abstract class GenerateJDAInfo : DefaultTask() {

    @get:Input
    abstract val version: Property<Version>

    @get:Input
    abstract val commitHash: Property<String>

    @get:OutputDirectory
    val outputDir = project.layout.buildDirectory.dir("generated/sources/JDAInfo")

    @TaskAction
    fun generate() {
        val classData = javaClass.getResourceAsStream("/JDAInfo.java")!!
                .use(InputStream::readBytes)
                .decodeToString()

        val version = version.get()
        val attributes = mapOf(
                "versionMajor" to version.major,
                "versionMinor" to version.minor,
                "versionRevision" to version.revision,
                "versionClassifier" to nullableReplacement(version.classifier),
                "commitHash" to commitHash.get()
        )

        // Allow for setting null on some strings without breaking the source
        // for this, we have special tokens marked with "!@...@!" which are replaced to @...@
        val filledClassData = classData
                .replace(Regex("\"!@|@!\""), "@")
                .replaceTokens(attributes)

        val jdaInfoFile = outputDir.get().file("net/dv8tion/jda/api/JDAInfo.java").asFile
        jdaInfoFile.parentFile.mkdirs()
        jdaInfoFile.writeText(filledClassData)
    }

    private fun String.replaceTokens(map: Map<String, String>): String {
        return map.entries.fold(this) { current, (key, value) -> current.replace("@$key@", value) }
    }
}
