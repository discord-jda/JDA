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

import de.undercouch.gradle.tasks.download.Download
import net.dv8tion.jda.gradle.plugins.ApiModelGenerator
import net.dv8tion.jda.gradle.tasks.GenerateApiModelsTask

plugins {
    id("de.undercouch.download")
}

val apiModelGenerator = project.extensions.create<ApiModelGenerator>("apiModelGenerator")

val taskGroup = "model generator"

val downloadApiSpec by tasks.registering(Download::class) {
    group = taskGroup
    src(apiModelGenerator.apiSpecDownloadUrl)
    dest(apiModelGenerator.apiSpecFile)
}

val generateApiModels by tasks.registering(GenerateApiModelsTask::class) {
    group = taskGroup

    suffix.set(apiModelGenerator.generatorSuffix)
    apiSpecFile.set(apiModelGenerator.apiSpecFile)
    outputDirectory.set(apiModelGenerator.outputDirectory)
}

project.tasks.named("compileJava") {
    dependsOn(generateApiModels)
}

val sourceSets = the<SourceSetContainer>()

sourceSets.named("main").configure {
    java.srcDir(apiModelGenerator.outputDirectory)
}
