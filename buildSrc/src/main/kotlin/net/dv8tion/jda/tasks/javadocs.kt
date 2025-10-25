package net.dv8tion.jda.tasks

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.getting

fun Project.configureJavadoc(failOnError: Boolean, overviewFile: String?) = tasks.getting(Javadoc::class) {
    isFailOnError = failOnError

    (options as? StandardJavadocDocletOptions)?.apply {
        memberLevel = JavadocMemberLevel.PUBLIC
        encoding = "UTF-8"

        author()
        tags("incubating:a:Incubating:")
        links("https://docs.oracle.com/en/java/javase/17/docs/api/", "https://takahikokawasaki.github.io/nv-websocket-client/")

        val javaVersion = JavaVersion.current()
        if (JavaVersion.VERSION_1_8 < javaVersion) {
            addBooleanOption("html5", true) // Adds search bar
            addStringOption("-release", "8")
        }

        // Fix for https://stackoverflow.com/questions/52326318/maven-javadoc-search-redirects-to-undefined-url
        if (javaVersion in JavaVersion.VERSION_11..JavaVersion.VERSION_12) {
            addBooleanOption("-no-module-directories", true)
        }

        // Java 13 changed accessibility rules.
        // On versions less than Java 13, we simply ignore the errors.
        // Both of these remove "no comment" warnings.
        if (javaVersion >= JavaVersion.VERSION_13) {
            addBooleanOption("Xdoclint:all,-missing", true)
        } else {
            addBooleanOption("Xdoclint:all,-missing,-accessibility", true)
        }

        overview = overviewFile
    }

    exclude {
        it.file.absolutePath.contains("internal", ignoreCase = false)
    }
}
