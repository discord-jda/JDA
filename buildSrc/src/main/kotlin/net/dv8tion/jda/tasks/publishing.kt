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
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

fun MavenPom.populate(moduleName: String, moduleDescription: String, moduleUrl: String) {
    packaging = "jar"
    name.set(moduleName)
    description.set(moduleDescription)
    url.set(moduleUrl)
    scm {
        url.set("https://github.com/discord-jda/JDA")
        connection.set("scm:git:git://github.com/discord-jda/JDA")
        developerConnection.set("scm:git:ssh:git@github.com:discord-jda/JDA")
    }
    licenses {
        license {
            name.set("The Apache Software License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            distribution.set("repo")
        }
    }
    developers {
        developer {
            id.set("Minn")
            name.set("Florian Spieß")
            email.set("business@minn.dev")
        }
        developer {
            id.set("DV8FromTheWorld")
            name.set("Austin Keener")
            email.set("keeneraustin@yahoo.com")
        }
    }
}

/**
 * Registers a [MavenPublication] named `Release` with the following configuration:
 *
 * - The group ID and version is copied from the current [Project] and **must** be strings
 * - The artifact ID is set to the provided [name]
 * - The POM is populated with the provided [name], [description] and [url] in addition to the common properties
 *
 * The [block] must attach inputs (`from(...)`, `artifact(...)`).
 */
fun Project.registerPublication(name: String, description: String, url: String, block: MavenPublication.() -> Unit) {
    extensions.configure<PublishingExtension> {
        publications {
            register<MavenPublication>("Release") {
                artifactId = name
                groupId = project.group as String
                version = project.version as String

                pom.populate(name, description, url)

                block()
            }
        }
    }
}
