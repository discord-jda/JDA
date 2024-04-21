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
package net.dv8tion.jda.api

/**
 * Contains information to this specific build of JDA.
 */
object JDAInfo {
    @JvmField
    val DISCORD_GATEWAY_VERSION: Int = 10
    val DISCORD_REST_VERSION: Int = 10
    @JvmField
    val AUDIO_GATEWAY_VERSION: Int = 4
    val GITHUB: String = "https://github.com/discord-jda/JDA"
    val VERSION_MAJOR: String = "@versionMajor@"
    val VERSION_MINOR: String = "@versionMinor@"
    val VERSION_REVISION: String = "@versionRevision@"
    val VERSION_CLASSIFIER: String? = "!@versionClassifier@!"
    val COMMIT_HASH: String? = "@commitHash@"
    @JvmField
    val VERSION: String = String.format(
        "%s.%s.%s%s%s", VERSION_MAJOR, VERSION_MINOR, VERSION_REVISION,
        if (VERSION_CLASSIFIER == null) "" else "-" + VERSION_CLASSIFIER,
        if (COMMIT_HASH == null) "" else "_" + COMMIT_HASH
    )
}
