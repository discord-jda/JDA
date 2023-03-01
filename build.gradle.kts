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
//to build and upload everything:  "gradlew publish"

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    signing
    `java-library`
    `maven-publish`

    id("com.github.ben-manes.versions") version "0.19.0"
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

val versionObj = Version(major = "4", minor = "4", revision = "1")

project.group = "net.dv8tion"
project.version = "$versionObj"
val archivesBaseName = "JDA"

val s3PublishingUrl = "s3://m2.dv8tion.net/releases"

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
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    /* ABI dependencies */

    //Code safety
    api("com.google.code.findbugs:jsr305:3.0.2")
    api("org.jetbrains:annotations:16.0.1")

    //Logger
    api("org.slf4j:slf4j-api:1.7.25")

    //Web Connection Support
    api("com.neovisionaries:nv-websocket-client:2.14")
    api("com.squareup.okhttp3:okhttp:3.13.0")

    //Opus library support
    api("club.minnced:opus-java:1.1.0@pom") {
        isTransitive = true
    }

    //Collections Utility
    api("org.apache.commons:commons-collections4:4.1")

    //we use this only together with opus-java
    // if that dependency is excluded it also doesn't need jna anymore
    // since jna is a transitive runtime dependency of opus-java we don't include it explicitly as dependency
    compileOnly("net.java.dev.jna:jna:4.4.0")

    /* Internal dependencies */

    //General Utility
    implementation("net.sf.trove4j:trove4j:3.0.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.1")

    //Sets the dependencies for the examples
    configurations["examplesImplementation"].withDependencies {
        addAll(configurations["api"].allDependencies)
        addAll(configurations["implementation"].allDependencies)
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.0")
}

val compileJava: JavaCompile by tasks
val shadowJar: ShadowJar by tasks
val javadoc: Javadoc by tasks
val jar: Jar by tasks
val build: Task by tasks
val clean: Task by tasks
val test: Test by tasks
val check: Task by tasks

shadowJar.classifier = "withDependencies"

val sourcesForRelease = task<Copy>("sourcesForRelease") {
    from("src/main/java") {
        include("**/JDAInfo.java")
        val tokens = mapOf(
                "versionMajor" to versionObj.major,
                "versionMinor" to versionObj.minor,
                "versionRevision" to versionObj.revision,
                "versionBuild" to getBuild()
        )
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
    classifier = shadowJar.classifier + "-no-opus"

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
    classifier = shadowJar.classifier + "-min"
    configurations = shadowJar.configurations
    from(sourceSets["main"].output)
    exclude("natives/**")     // ~2 MB
    exclude("com/sun/jna/**") // ~1 MB
    exclude("club/minnced/opus/util/*")
    exclude("tomp2p/opuswrapper/*")
    manifest.inheritFrom(jar.manifest)
}

val sourcesJar = task<Jar>("sourcesJar") {
    classifier = "sources"
    from("src/main/java") {
        exclude("**/JDAInfo.java")
    }
    from(sourcesForRelease.destinationDir)

    dependsOn(sourcesForRelease)
}

val javadocJar = task<Jar>("javadocJar") {
    dependsOn(javadoc)
    classifier = "javadoc"
    from(javadoc.destinationDir)
}

tasks.withType<ShadowJar> {
    exclude("*.pom")
}

tasks.withType<JavaCompile> {
    val arguments = mutableListOf("-Xlint:deprecation", "-Xlint:unchecked")
    options.encoding = "UTF-8"
    options.isIncremental = true
    if (JavaVersion.current().isJava9Compatible) doFirst {
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
    baseName = project.name
    manifest.attributes(mapOf(
            "Implementation-Version" to version,
            "Automatic-Module-Name" to "net.dv8tion.jda"))
}

javadoc.apply {
    isFailOnError = false
    options.memberLevel = JavadocMemberLevel.PUBLIC
    options.encoding = "UTF-8"

    if (options is StandardJavadocDocletOptions) {
        val opt = options as StandardJavadocDocletOptions
        opt.author()
        opt.tags("incubating:a:Incubating:")
        opt.links(
                "https://docs.oracle.com/javase/8/docs/api/",
                "https://takahikokawasaki.github.io/nv-websocket-client/",
                "https://square.github.io/okhttp/3.x/okhttp/")
        if (JavaVersion.current().isJava9Compatible) {
            opt.addBooleanOption("html5", true)
            opt.addStringOption("-release", "8")
        }
        if (JavaVersion.current().isJava11Compatible) {
            opt.addBooleanOption("-no-module-directories", true)
        }
    }

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
    javadocJar.mustRunAfter(jar)
    sourcesJar.mustRunAfter(javadocJar)
    shadowJar.mustRunAfter(sourcesJar)
}

test.apply {
    useJUnitPlatform()
    failFast = true
}

publishing {
    publications {
        create<MavenPublication>("S3Release") {
            from(components["java"])

            artifactId = archivesBaseName
            groupId = project.group as String
            version = project.version as String

            artifact(javadocJar)
            artifact(sourcesJar)

            repositories {
                maven {
                    url = uri(s3PublishingUrl)
                    credentials(AwsCredentials::class) {
                        accessKey = getProjectProperty("awsAccessKey")
                        secretKey = getProjectProperty("awsSecretKey")
                    }
                }
            }
        }
    }
}

val publishS3ReleasePublicationToMavenRepository: Task by tasks
publishS3ReleasePublicationToMavenRepository.apply {
    onlyIf { getProjectProperty("awsAccessKey").isNotEmpty() }
    onlyIf { getProjectProperty("awsSecretKey").isNotEmpty() }
    onlyIf { System.getenv("BUILD_NUMBER") != null }

    dependsOn(clean)
    dependsOn(build)
    build.mustRunAfter(clean)
}

fun getProjectProperty(propertyName: String): String {
    var property = ""
    if (hasProperty(propertyName)) {
        property = project.properties[propertyName] as? String ?: ""
    }
    return property
}

fun getBuild(): String {
    return System.getenv("BUILD_NUMBER")
            ?: System.getProperty("BUILD_NUMBER")
            ?: System.getenv("GIT_COMMIT")?.substring(0, 7)
            ?: System.getProperty("GIT_COMMIT")?.substring(0, 7)
            ?: "DEV"
}

class Version(
        val major: String,
        val minor: String,
        val revision: String) {
    override fun toString() = "$major.$minor.${revision}_${getBuild()}"
}
