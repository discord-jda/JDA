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

package net.dv8tion.jda.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class VerifyBytecodeVersion : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val classes: ConfigurableFileCollection

    @get:Input
    abstract val expectedMajorVersion: Property<Int>

    @TaskAction
    fun verify() {
        val badFiles = classes.filter { it.readBytecodeVersion() != expectedMajorVersion.get() }.toList()

        if (badFiles.isNotEmpty()) {
            println("Found ${badFiles.size} files that did not have the bytecode version ${expectedMajorVersion.get()}")
            println("The following classes have the wrong bytecode version:")
            for (file in badFiles) {
                println("\t- ${file.path} has version ${file.readBytecodeVersion()}")
            }

            throw GradleException("Some compiled classes have the wrong bytecode version.")
        }
    }
}

private val validClassHeaderBytes = listOf(0xCA.toByte(), 0xFE.toByte(), 0xBA.toByte(), 0xBE.toByte())

private fun File.readBytecodeVersion(): Int? = inputStream().buffered().use { stream ->
    val header = stream.readNBytes(8)
    if (header.size < 8) {
        return null
    }

    if (header.take(4) != validClassHeaderBytes) {
        return null
    }

    // Big-endian major version: bytes[6..7]
    return ((header[6].toInt() and 0xFF) shl 8) or (header[7].toInt() and 0xFF)
}
