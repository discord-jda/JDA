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

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import de.undercouch.gradle.tasks.download.Download
import net.dv8tion.jda.tasks.*
import nl.littlerobots.vcu.plugin.resolver.VersionSelectors
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    environment
    artifacts
    `java-library`
    `jda-publish`

    alias(libs.plugins.shadow)
    alias(libs.plugins.versions)
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.download)
}


////////////////////////////////////
//                                //
//     Project Configuration      //
//                                //
////////////////////////////////////

projectEnvironment {
    version = Version(major = "6", minor = "1", revision = "1", classifier = null)
}

artifactFilters {
    additionalAudioExclusions.addAll("com/google/crypto/tink/**", "com/google/gson/**", "com/google/protobuf/**", "google/protobuf/**")
}

// Use normal version string for new releases and commitHash for other builds
if (projectEnvironment.canPublish) {
    project.version = projectEnvironment.version.get().toString()
} else {
    project.version = "${projectEnvironment.version.get()}_${projectEnvironment.commitHash}"
}

project.group = "net.dv8tion"

base {
    archivesName.set("JDA")
}

configure<SourceSetContainer> {
    register("examples") {
        java.srcDir("src/examples/java")
        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    }
}


////////////////////////////////////
//                                //
//    Dependency Configuration    //
//                                //
////////////////////////////////////

val mockitoAgent by configurations.creating

repositories {
    mavenCentral()
}

dependencies {
    /* ABI dependencies */

    //Code safety
    compileOnly(libs.findbugs)
    compileOnly(libs.jetbrains.annotations)

    //Logger
    api(libs.slf4j)

    //Web Connection Support
    api(libs.websocket.client)
    api(libs.okhttp)

    //Collections Utility
    api(libs.commons.collections)

    /* Internal dependencies */

    //General Utility
    implementation(libs.trove4j)
    implementation(libs.bundles.jackson)

    //Audio crypto libraries
    implementation(libs.tink)

    //Sets the dependencies for the examples
    configurations["examplesImplementation"].withDependencies {
        addAll(configurations["api"].allDependencies)
        addAll(configurations["implementation"].allDependencies)
        addAll(configurations["compileOnly"].allDependencies)

        add(project(":opus-jna"))
    }

    testImplementation(libs.bundles.junit)
    testImplementation(libs.reflections)
    testImplementation(libs.mockito)
    testImplementation(libs.assertj)
    testImplementation(libs.commons.lang3)
    testImplementation(libs.logback.classic)
    testImplementation(libs.archunit)

    mockitoAgent(libs.mockito) {
        isTransitive = false
    }

    // OpenRewrite
    // Import Rewrite's bill of materials.
    testImplementation(platform("org.openrewrite.recipe:rewrite-recipe-bom:3.6.1"))

    // rewrite-java dependencies only necessary for Java Recipe development
    testImplementation("org.openrewrite:rewrite-java")
    testImplementation("org.openrewrite.recipe:rewrite-java-dependencies")

    // This is supposed to only be the version that corresponds to the current Java version,
    // but as there are no toolchain, we include all, they can coexist safely tho.
    testRuntimeOnly("org.openrewrite:rewrite-java-8")
    testRuntimeOnly("org.openrewrite:rewrite-java-11")
    testRuntimeOnly("org.openrewrite:rewrite-java-17")

    // For authoring tests for any kind of Recipe
    testImplementation("org.openrewrite:rewrite-test")
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }

    gradleReleaseChannel = "current"
}

versionCatalogUpdate {
    versionSelector(VersionSelectors.STABLE)
}


////////////////////////////////////
//                                //
//    Build Task Configuration    //
//                                //
////////////////////////////////////

val jar by tasks.getting(Jar::class) {
    archiveBaseName.set(project.name)
    manifest.attributes("Implementation-Version" to project.version, "Automatic-Module-Name" to "net.dv8tion.jda")
}

val shadowJar by tasks.getting(ShadowJar::class) {
    archiveClassifier.set("withDependencies")
    exclude("*.pom")
}

val sourcesForRelease by tasks.registering(Copy::class) {
    from("src/main/java") {
        include("**/JDAInfo.java")
        val version = projectEnvironment.version.get()

        val tokens = mapOf(
            "versionMajor" to version.major,
            "versionMinor" to version.minor,
            "versionRevision" to version.revision,
            "versionClassifier" to nullableReplacement(version.classifier),
            "commitHash" to projectEnvironment.commitHash
        )
        // Allow for setting null on some strings without breaking the source
        // for this, we have special tokens marked with "!@...@!" which are replaced to @...@
        filter { it.replace(Regex("\"!@|@!\""), "@") }
        // Then we can replace the @...@ with the respective values here
        filter<ReplaceTokens>("tokens" to tokens)
    }
    into("build/filteredSrc")

    includeEmptyDirs = false
}

val generateJavaSources by tasks.registering(SourceTask::class) {
    val javaSources = sourceSets["main"].allJava.filter {
        it.name != "JDAInfo.java"
    }.asFileTree

    source = javaSources + fileTree(sourcesForRelease.get().destinationDir)
    dependsOn(sourcesForRelease)
}

val minimalJar by tasks.registering(ShadowJar::class) {
    dependsOn(shadowJar)
    minimize()
    archiveClassifier.set(shadowJar.archiveClassifier.get() + "-min")

    configurations = shadowJar.configurations
    from(sourceSets["main"].output)
    applyAudioExclusions(artifactFilters)
    manifest.from(jar.manifest)
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from("src/main/java") {
        exclude("**/JDAInfo.java")
    }
    from(sourcesForRelease.get().destinationDir)

    dependsOn(sourcesForRelease)
}

val javadoc by configureJavadoc(
        failOnError = projectEnvironment.isGithubAction,
        overviewFile = "$projectDir/overview.html",
)

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(javadoc)
    archiveClassifier.set("javadoc")
    from(javadoc.destinationDir)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    val args = mutableListOf("-Xlint:deprecation", "-Xlint:unchecked")

    options.release = 8
    options.compilerArgs.addAll(args)
}

val compileJava by tasks.getting(JavaCompile::class) {
    dependsOn(generateJavaSources)
    source = generateJavaSources.get().source
}

tasks.build.configure {
    dependsOn(jar)
    dependsOn(javadocJar)
    dependsOn(sourcesJar)
    dependsOn(shadowJar)
    dependsOn(minimalJar)

    jar.mustRunAfter(tasks.clean)
    shadowJar.mustRunAfter(sourcesJar)
}


////////////////////////////////////
//                                //
//       Test Configuration       //
//                                //
////////////////////////////////////


val downloadRecipeClasspath by tasks.registering(Download::class) {
    val targetVersion = "5.6.1"
    src("https://repo.maven.apache.org/maven2/net/dv8tion/JDA/$targetVersion/JDA-$targetVersion.jar")
    dest("src/test/resources/META-INF/rewrite/classpath/JDA-$targetVersion.jar")
    overwrite(false)
}

tasks.named("processTestResources").configure {
    dependsOn(downloadRecipeClasspath)
}


tasks.register<Test>("updateTestSnapshots") {
    group = "verification"
    useJUnitPlatform()

    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    systemProperty("updateSnapshots", "true")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    failFast = false

    if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_21)) {
        jvmArgs = listOf("-javaagent:${mockitoAgent.asPath}")
    }
}

tasks.test {
    testLogging {
        events("passed", "skipped", "failed")
    }
    reports {
        junitXml.required = projectEnvironment.isGithubAction
        html.required = projectEnvironment.isGithubAction
    }
}

val verifyBytecodeVersion by tasks.registering(VerifyBytecodeVersion::class) {
    group = "verification"

    expectedMajorVersion = 52
    classes.from(compileJava.outputs.files.asFileTree.matching {
        include("**/*.class")
    })
}

compileJava.finalizedBy(verifyBytecodeVersion)


////////////////////////////////////
//                                //
//    Publishing And Signing      //
//                                //
////////////////////////////////////

shadow {
    addShadowVariantIntoJavaComponent = false
}

registerPublication(
        name = project.name,
        description = "Java wrapper for the popular chat & VOIP service: Discord https://discord.com",
        url = "https://github.com/discord-jda/JDA",
) {
    from(components["java"])

    artifact(sourcesJar)
    artifact(javadocJar)
}
