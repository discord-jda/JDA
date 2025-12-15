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

package net.dv8tion.jda.gradle.spec.parser

import tools.jackson.databind.json.JsonMapper
import java.io.File

class ApiSpecParser(val specFile: File) {
    val jsonMapper = JsonMapper()

    fun parse(): ParserContext {
        val parsed = jsonMapper.readValue(specFile, ParsedApiSpec::class.java)
        return ParserContext(parsed.components.schemas)
    }
}

class ParserContext(
    val schemas: Map<String, ComponentSchema>
) {
    fun resolveRef(ref: String): ComponentSchema {
        return schemas.getValue(getSimpleNameFromRef(ref))
    }
}

fun getSimpleNameFromRef(ref: String): String {
    return ref.substringAfterLast("/")
}
