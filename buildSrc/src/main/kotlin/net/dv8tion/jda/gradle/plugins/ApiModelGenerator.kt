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

package net.dv8tion.jda.gradle.plugins

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 * Custom plugin to parse the official Discord OpenAPI Spec.
 *
 * This plugin handles all the special quirks of the spec, such as weird union types and extensions that are used throughout the api spec.
 *
 * We only generate models for databind, clients are currently out of scope.
 */
interface ApiModelGenerator {
    val outputDirectory: DirectoryProperty
    val apiSpecFile: RegularFileProperty
    val apiSpecDownloadUrl: Property<String>
    val generatorSuffix: Property<String>
    val includes: ListProperty<String>
}
