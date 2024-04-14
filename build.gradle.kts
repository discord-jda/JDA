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

//to build everything:             "gradlew build"
//to build and upload everything:  "gradlew release"

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.github.gradlenexus.publishplugin.AbstractNexusStagingRepositoryTask
import org.apache.tools.ant.filters.ReplaceTokens
import java.time.Duration

plugins {
    signing
    `java-library`
    `maven-publish`

    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val javaVersion = JavaVersion.current()
val versionObj = Version(major = "5", minor = "0", revision = "0", classifier = "beta.22")
val isCI = System.getProperty("BUILD_NUMBER") != null // jenkins
        || System.getenv("BUILD_NUMBER") != null
        || System.getProperty("GIT_COMMIT") != null // jitpack
        || System.getenv("GIT_COMMIT") != null
        || System.getProperty("GITHUB_ACTION") != null // Github Actions
        || System.getenv("GITHUB_ACTION") != null

// Check the commit hash and version information
val commitHash: String by lazy {
    val commit = System.getenv("GIT_COMMIT") ?: System.getProperty("GIT_COMMIT") ?: System.getenv("GITHUB_SHA")
    // We only set the commit hash on CI builds since we don't want dirty local repos to set a wrong commit
    if (isCI && commit != null)
        commit.substring(0, 7)
    else
        "DEV"
}

val previousVersion: Version by lazy {
    val file = File(".version")
    if (file.canRead())
        Version.parse(file.readText().trim())
    else
        versionObj
}

val isNewVersion = previousVersion != versionObj
// Use normal version string for new releases and commitHash for other builds
project.version = "$versionObj" + if (isNewVersion) "" else "_$commitHash"

project.group = "net.dv8tion"

val archivesBaseName = "JDA"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

configure<SourceSetContainer> {
    register("examples") {
        java.srcDir("src/examples/java")
        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    }
}


repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    /* ABI dependencies */

    //Code safety
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("org.jetbrains:annotations:23.0.0")

    //Logger
    api("org.slf4j:slf4j-api:1.7.36")

    //Web Connection Support
    api("com.neovisionaries:nv-websocket-client:2.14")
    api("com.squareup.okhttp3:okhttp:4.12.0")

    //Opus library support
    api("club.minnced:opus-java:1.1.1")

    //Collections Utility
    api("org.apache.commons:commons-collections4:4.4")

    //we use this only together with opus-java
    // if that dependency is excluded it also doesn't need jna anymore
    // since jna is a transitive runtime dependency of opus-java we don't include it explicitly as dependency
    compileOnly("net.java.dev.jna:jna:4.4.0")

    /* Internal dependencies */

    //General Utility
    implementation("net.sf.trove4j:trove4j:3.0.3")
    implementation("com.fasterxml.jackson.core:jackson-core:2.16.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")

    //Sets the dependencies for the examples
    configurations["examplesImplementation"].withDependencies {
        addAll(configurations["api"].allDependencies)
        addAll(configurations["implementation"].allDependencies)
        addAll(configurations["compileOnly"].allDependencies)
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.reflections:reflections:0.10.2")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.assertj:assertj-core:3.25.3")
}

val compileJava: JavaCompile by tasks
val shadowJar: ShadowJar by tasks
val javadoc: Javadoc by tasks
val jar: Jar by tasks
val build: Task by tasks
val clean: Task by tasks
val test: Test by tasks
val check: Task by tasks

shadowJar.archiveClassifier.set("withDependencies")

fun nullable(string: String?): String {
    return if (string == null) "null"
           else "\"$string\""
}

val sourcesForRelease = task<Copy>("sourcesForRelease") {
    from("src/main/java") {
        include("**/JDAInfo.java")
        val tokens = mapOf(
                "versionMajor" to versionObj.major,
                "versionMinor" to versionObj.minor,
                "versionRevision" to versionObj.revision,
                "versionClassifier" to nullable(versionObj.classifier),
                "commitHash" to commitHash
        )
        // Allow for setting null on some strings without breaking the source
        // for this, we have special tokens marked with "!@...@!" which are replaced to @...@
        filter { it.replace(Regex("\"!@|@!\""), "@") }
        // Then we can replace the @...@ with the respective values here
        filter<ReplaceTokens>(mapOf("tokens" to tokens))
    }
    into("build/filteredSrc")

    includeEmptyDirs = false
}

val generateJavaSources = task<SourceTask>("generateJavaSources") {
    val javaSources = sourceSets["main"].allJava.filter {
        it.name != "JDAInfo.java"
    }.asFileTree

    source = javaSources + fileTree(sourcesForRelease.destinationDir)
    dependsOn(sourcesForRelease)
}

val noOpusJar = task<ShadowJar>("noOpusJar") {
    dependsOn(shadowJar)
    archiveClassifier.set(shadowJar.archiveClassifier.get() + "-no-opus")

    configurations = shadowJar.configurations
    from(sourceSets["main"].output)
    exclude("natives/**")     // ~2 MB
    exclude("com/sun/jna/**") // ~1 MB
    exclude("club/minnced/opus/util/*")
    exclude("tomp2p/opuswrapper/*")

    manifest.inheritFrom(jar.manifest)
}

val minimalJar = task<ShadowJar>("minimalJar") {
    dependsOn(shadowJar)
    minimize()
    archiveClassifier.set(shadowJar.archiveClassifier.get() + "-min")

    configurations = shadowJar.configurations
    from(sourceSets["main"].output)
    exclude("natives/**")     // ~2 MB
    exclude("com/sun/jna/**") // ~1 MB
    exclude("club/minnced/opus/util/*")
    exclude("tomp2p/opuswrapper/*")
    manifest.inheritFrom(jar.manifest)
}

val sourcesJar = task<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from("src/main/java") {
        exclude("**/JDAInfo.java")
    }
    from(sourcesForRelease.destinationDir)

    dependsOn(sourcesForRelease)
}

val javadocJar = task<Jar>("javadocJar") {
    dependsOn(javadoc)
    archiveClassifier.set("javadoc")
    from(javadoc.destinationDir)
}

tasks.withType<ShadowJar> {
    exclude("*.pom")
}

tasks.withType<JavaCompile> {
    val arguments = mutableListOf("-Xlint:deprecation", "-Xlint:unchecked")
    options.encoding = "UTF-8"
    options.isIncremental = true
    if (javaVersion.isJava9Compatible) doFirst {
        arguments += "--release"
        arguments += "8"
    }
    doFirst {
        options.compilerArgs = arguments
    }
}

compileJava.apply {
    source = generateJavaSources.source
    dependsOn(generateJavaSources)
}

jar.apply {
    archiveBaseName.set(project.name)
    manifest.attributes(mapOf(
            "Implementation-Version" to project.version,
            "Automatic-Module-Name" to "net.dv8tion.jda"))
}

javadoc.apply {
    isFailOnError = isCI
    options.memberLevel = JavadocMemberLevel.PUBLIC
    options.encoding = "UTF-8"

    (options as? StandardJavadocDocletOptions)?.let { opt ->
        opt.author()
        opt.tags("incubating:a:Incubating:")
        opt.links(
                "https://docs.oracle.com/javase/8/docs/api/",
                "https://takahikokawasaki.github.io/nv-websocket-client/")
        if (JavaVersion.VERSION_1_8 < javaVersion) {
            opt.addBooleanOption("html5", true) // Adds search bar
            opt.addStringOption("-release", "8")
        }
        // Fix for https://stackoverflow.com/questions/52326318/maven-javadoc-search-redirects-to-undefined-url
        if (javaVersion in JavaVersion.VERSION_11..JavaVersion.VERSION_12) {
            opt.addBooleanOption("-no-module-directories", true)
        }
        // Java 13 changed accessibility rules.
        // On versions less than Java 13, we simply ignore the errors.
        // Both of these remove "no comment" warnings.
        if (javaVersion >= JavaVersion.VERSION_13) {
            opt.addBooleanOption("Xdoclint:all,-missing", true)
        } else {
            opt.addBooleanOption("Xdoclint:all,-missing,-accessibility", true)
        }

        opt.overview = "$projectDir/overview.html"
    }

    dependsOn(sourcesJar)
    source = sourcesJar.source.asFileTree
    exclude("MANIFEST.MF")

    //### excludes ###

    //jda internals
    exclude("net/dv8tion/jda/internal")

    //voice crypto
    exclude("com/iwebpp/crypto")
}

build.apply {
    dependsOn(jar)
    dependsOn(javadocJar)
    dependsOn(sourcesJar)
    dependsOn(shadowJar)
    dependsOn(noOpusJar)
    dependsOn(minimalJar)

    jar.mustRunAfter(clean)
    shadowJar.mustRunAfter(sourcesJar)
}

test.apply {
    useJUnitPlatform()
    failFast = true
}


fun getProjectProperty(name: String) = project.properties[name] as? String

class Version(
    val major: String,
    val minor: String,
    val revision: String,
    val classifier: String? = null
) {
    companion object {
        fun parse(string: String): Version {
            val (major, minor, revision) = string.substringBefore("-").split(".")
            val classifier = if ("-" in string) string.substringAfter("-") else null
            return Version(major, minor, revision, classifier)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Version) return false
        return major == other.major
            && minor == other.minor
            && revision == other.revision
            && classifier == other.classifier
    }

    override fun toString(): String {
        return "$major.$minor.$revision" + if (classifier != null) "-$classifier" else ""
    }
}


////////////////////////////////////////
////////////////////////////////////////
////                                ////
////     Publishing And Signing     ////
////                                ////
////////////////////////////////////////
////////////////////////////////////////

// Generate pom file for maven central

fun generatePom(): MavenPom.() -> Unit = {
    packaging = "jar"
    name.set(project.name)
    description.set("Java wrapper for the popular chat & VOIP service: Discord https://discord.com")
    url.set("https://github.com/discord-jda/JDA")
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


// Publish

// Skip fat jar publication (See https://github.com/johnrengelman/shadow/issues/586)
components.java.withVariantsFromConfiguration(configurations.shadowRuntimeElements.get()) { skip() }
val SoftwareComponentContainer.java
    get() = components.getByName("java") as AdhocComponentWithVariants

publishing {
    publications {
        register<MavenPublication>("Release") {
            from(components["java"])

            artifactId = project.name
            groupId = project.group as String
            version = project.version as String

            artifact(sourcesJar)
            artifact(javadocJar)

            pom.apply(generatePom())
        }
    }
}

val canSign = getProjectProperty("signing.keyId") != null
if (canSign) {
    signing {
        sign(publishing.publications.getByName("Release"))
    }
}

nexusPublishing {
    repositories.sonatype {
        username.set(getProjectProperty("ossrhUser"))
        password.set(getProjectProperty("ossrhPassword"))
        stagingProfileId.set(getProjectProperty("stagingProfileId"))
    }

    connectTimeout.set(Duration.ofMinutes(1))
    clientTimeout.set(Duration.ofMinutes(10))

    transitionCheckOptions {
        maxRetries.set(100)
        delayBetween.set(Duration.ofSeconds(5))
    }
}

val ossrhConfigured = getProjectProperty("ossrhUser") != null
val shouldPublish = isNewVersion && canSign && ossrhConfigured

val rebuild = tasks.create("rebuild") {
    group = "build"

    dependsOn(build)
    dependsOn(tasks.clean)
    build.mustRunAfter(tasks.clean)
}

val publishingTasks = tasks.withType<PublishToMavenRepository> {
    enabled = shouldPublish
    mustRunAfter(rebuild)
    dependsOn(rebuild)
}

tasks.withType<AbstractNexusStagingRepositoryTask> {
    enabled = shouldPublish
}

val release = tasks.create("release") {
    dependsOn(publishingTasks)
}

afterEvaluate {
    tasks["closeAndReleaseSonatypeStagingRepository"].apply {
        release.dependsOn(this)
        mustRunAfter(publishingTasks)
    }
}
