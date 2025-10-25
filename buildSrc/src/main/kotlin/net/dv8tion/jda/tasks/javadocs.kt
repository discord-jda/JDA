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
    isFailOnError = failOnError

    (options as? StandardJavadocDocletOptions)?.apply {
        memberLevel = JavadocMemberLevel.PUBLIC
        encoding = "UTF-8"

        author()
        tags("incubating:a:Incubating:")
        // We compile to Java 8 but JDK 8 docs don't seem to supply `element-list` anymore, failing the build
        // so we'll be using the closest valid link (JDK 11) instead.
        // Failing: https://docs.oracle.com/javase/8/docs/api/element-list
        val effectiveJdkDocsVersion = max(11, targetVersion.majorVersion.toInt())
        links("https://docs.oracle.com/en/java/javase/${effectiveJdkDocsVersion}/docs/api/", "https://takahikokawasaki.github.io/nv-websocket-client/")

        addBooleanOption("html5", true) // Adds search bar
        addStringOption("-release", targetVersion.majorVersion)
        addBooleanOption("Xdoclint:all,-missing", true)

        overview = overviewFile
    }

    exclude {
        it.file.absolutePath.contains("internal", ignoreCase = false)
    }
}
