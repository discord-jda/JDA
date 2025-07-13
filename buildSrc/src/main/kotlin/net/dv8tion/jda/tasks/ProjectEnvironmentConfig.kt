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

import org.gradle.api.Project
import org.gradle.api.provider.Property


abstract class ProjectEnvironmentConfig(
    val project: Project,
) {
    abstract val version: Property<Version>

    val isCI = System.getenv("GITHUB_ACTION") != null
    val commitHash: String by lazy {
        val commit = System.getenv("GIT_COMMIT") ?: System.getProperty("GIT_COMMIT") ?: System.getenv("GITHUB_SHA")
        // We only set the commit hash on CI builds since we don't want dirty local repos to set a wrong commit
        if (isCI && commit != null)
            commit.take(7)
        else
            "DEV"
    }

    val mavenCredentials = run {
        val user = this.project.findProperty("ossrhUser")
        val token = this.project.findProperty("ossrhPassword")
        val stagingProfileId = this.project.findProperty("stagingProfile") as? String

        if (user is String && token is String)
            MavenCredentials(user, SecretString(token), stagingProfileId)
        else
            null
    }

    val signingKey = run {
        val keyValue = this.project.findProperty("signingKey")
        val keyId = this.project.findProperty("signingKeyId")

        if (keyId is String && keyValue is String)
            SigningKey(keyId, SecretString(keyValue))
        else
            null
    }

    val canSign: Boolean get() = signingKey != null
    val canPublish: Boolean get() = isCI && canSign && mavenCredentials != null
}
