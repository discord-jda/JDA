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
package net.dv8tion.jda.api;

/**
 * Contains information to this specific build of JDA.
 */
public class JDAInfo
{
    public static final int DISCORD_GATEWAY_VERSION = 9;
    public static final int DISCORD_REST_VERSION = 9;
    public static final int AUDIO_GATEWAY_VERSION = 4;
    public static final String GITHUB = "https://github.com/DV8FromTheWorld/JDA";
    public static final String VERSION_MAJOR = "@versionMajor@";
    public static final String VERSION_MINOR = "@versionMinor@";
    public static final String VERSION_REVISION = "@versionRevision@";
    public static final String VERSION_CLASSIFIER = "!@versionClassifier@!";
    public static final String COMMIT_HASH = "@commitHash@";
    public static final String VERSION = String.format("%s.%s.%s%s%s", VERSION_MAJOR, VERSION_MINOR, VERSION_REVISION,
            VERSION_CLASSIFIER == null ? "" : "-" + VERSION_CLASSIFIER,
            COMMIT_HASH == null ? "" : "_" + COMMIT_HASH);
}
