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

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.getting
import kotlin.math.max

fun Project.configureJavadoc(
        targetVersion: JavaVersion,
        failOnError: Boolean,
        overviewFile: String?,
) = tasks.getting(Javadoc::class) {
    val currentJavaVersion = JavaVersion.current().majorVersion

    isFailOnError = failOnError

    (options as? StandardJavadocDocletOptions)?.apply {
        memberLevel = JavadocMemberLevel.PUBLIC
        encoding = "UTF-8"

        author()
        tags("incubating:a:Incubating:")
        links("https://docs.oracle.com/en/java/javase/$currentJavaVersion/docs/api/", "https://takahikokawasaki.github.io/nv-websocket-client/")

        addBooleanOption("html5", true) // Adds search bar
        addStringOption("-release", targetVersion.majorVersion)
        addBooleanOption("Xdoclint:all,-missing", true)

        overview = overviewFile
    }

    exclude {
        it.file.absolutePath.contains("internal", ignoreCase = false)
    }
}
