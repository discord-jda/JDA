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

import net.dv8tion.jda.gradle.Version
import org.gradle.api.Project
import org.gradle.api.provider.Property

val publishingEnvironmentKeys = setOf(
    "MAVENCENTRAL_USERNAME",
    "MAVENCENTRAL_TOKEN",
    "GPG_PUBLIC_KEY",
    "GPG_SECRET_KEY",
)

abstract class ProjectEnvironmentConfig(
    val project: Project,
) {
    abstract val version: Property<Version>

    val isGithubAction = System.getenv("GITHUB_ACTION") != null
    val isJitpack = System.getenv("JITPACK") == "true"
    val isCI = isGithubAction || isJitpack

    val commitHash: String by lazy {
        val commit = System.getenv("GIT_COMMIT") ?: System.getenv("GITHUB_SHA")
        // We only set the commit hash on CI builds since we don't want dirty local repos to set a wrong commit
        if (isCI && commit != null)
            commit.take(7)
        else
            "DEV"
    }

    val hasReleaseCredentials: Boolean
        get() = publishingEnvironmentKeys.all { System.getenv(it) != null }

    val canPublish: Boolean get() = isCI && hasReleaseCredentials
}
