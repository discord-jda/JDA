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

import com.diffplug.spotless.LineEnding
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import de.undercouch.gradle.tasks.download.Download
import net.dv8tion.jda.gradle.Version
import net.dv8tion.jda.gradle.nullableReplacement
import net.dv8tion.jda.gradle.plugins.applyAudioExclusions
import net.dv8tion.jda.gradle.plugins.applyOpusExclusions
import net.dv8tion.jda.gradle.tasks.VerifyBytecodeVersion
import nl.littlerobots.vcu.plugin.resolver.VersionSelectors
import org.apache.tools.ant.filters.ReplaceTokens
import org.jreleaser.gradle.plugin.tasks.AbstractJReleaserTask
import org.jreleaser.model.Active
import org.openrewrite.gradle.AbstractRewriteTask

plugins {
    environment
    artifacts
    `model-generator`
    `java-library`
    `maven-publish`

    alias(libs.plugins.shadow)
    alias(libs.plugins.versions)
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.jreleaser)
    alias(libs.plugins.spotless)
    alias(libs.plugins.openrewrite)
}


////////////////////////////////////
//                                //
//     Project Configuration      //
//                                //
////////////////////////////////////

projectEnvironment {
    version = Version(major = "6", minor = "2", revision = "0", classifier = null)
}

artifactFilters {
    opusExclusions.addAll("natives/**", "com/sun/jna/**", "club/minnced/opus/util/*", "tomp2p/opuswrapper/*")
    additionalAudioExclusions.addAll("com/google/crypto/tink/**", "com/google/gson/**", "com/google/protobuf/**", "google/protobuf/**")
}

apiModelGenerator {
    outputDirectory = layout.buildDirectory.dir("generated/rest-api-models")
    apiSpecFile = file("discord-rest-openapi.json")
    apiSpecDownloadUrl = "https://raw.githubusercontent.com/discord/discord-api-spec/refs/heads/main/specs/openapi.json"

    generatorSuffix = "Dto"
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

val testJava8 by sourceSets.creating {
    java.srcDir("src/test-java8/java")
    resources.srcDir("src/test-java8/resources")
    compileClasspath += sourceSets["main"].output
    runtimeClasspath += sourceSets["main"].output
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

val java8Toolchain = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(8))
    vendor.set(JvmVendorSpec.ADOPTIUM)
}


////////////////////////////////////
//                                //
//    Dependency Configuration    //
//                                //
////////////////////////////////////

val currentJavaVersion = JavaVersion.current().majorVersion

val mockitoAgent by configurations.creating

val testJava8Implementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

val testJava8RuntimeOnly by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}

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

    //Opus library support
    api(libs.opus)

    //Collections Utility
    api(libs.commons.collections)

    //we use this only together with opus-java
    // if that dependency is excluded it also doesn't need jna anymore
    // since jna is a transitive runtime dependency of opus-java we don't include it explicitly as dependency
    compileOnly(libs.jna)

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
    }

    testImplementation(libs.bundles.junit)
    testImplementation(libs.reflections)
    testImplementation(libs.mockito)
    testImplementation(libs.assertj)
    testImplementation(libs.commons.lang3)
    testImplementation(libs.logback.classic)
    testImplementation(libs.archunit)

    testJava8Implementation(libs.bundles.junit.java8)
    testJava8Implementation(libs.assertj)

    mockitoAgent(libs.mockito) {
        isTransitive = false
    }

    // OpenRewrite
    testImplementation(platform(libs.openrewrite.bom))
    rewrite(platform(libs.openrewrite.bom))

    // rewrite-java dependencies only necessary for Java Recipe development
    testImplementation("org.openrewrite:rewrite-java")
    testImplementation("org.openrewrite.recipe:rewrite-java-dependencies")

    testRuntimeOnly("org.openrewrite:rewrite-java-${currentJavaVersion}")

    // For authoring tests for any kind of Recipe
    testImplementation("org.openrewrite:rewrite-test")

    // Needed for rewrite gradle tasks
    rewrite("org.openrewrite.recipe:rewrite-static-analysis")
    rewrite("net.dv8tion.jda:formatter-recipes")
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
//    Formatting and Linting      //
//                                //
////////////////////////////////////

rewrite {
    failOnDryRunResults = true
    throwOnParseFailures = true

    activeRecipe("org.openrewrite.staticanalysis.NeedBraces")
    activeRecipe("org.openrewrite.staticanalysis.NoFinalizedLocalVariables")
    activeRecipe("net.dv8tion.jda.recipe.JavadocFormatter")
    activeRecipe("MigrateToJavaxAnnotations")

    exclusion("*.kts", "**/*.kts", "**/*.kt")
}

spotless {
    encoding("UTF-8")
    lineEndings = LineEnding.GIT_ATTRIBUTES_FAST_ALLSAME

    kotlinGradle {
        target("*.gradle.kts", "buildSrc/*.gradle.kts", "buildSrc/src/**/*.kt*")

        trimTrailingWhitespace()
        leadingTabsToSpaces()
    }

    java {
        palantirJavaFormat("2.80.0")
            .formatJavadoc(false)

        licenseHeaderFile("spotless/licence-header.txt")

        target("src/**/*.java")

        removeUnusedImports()
        importOrder("",  "java", "javax", "\\#")
        trimTrailingWhitespace()
    }
}

tasks.named("spotlessJavaCheck").configure {
    dependsOn(tasks.named("rewriteDryRun"))
}

tasks.named("spotlessJavaApply").configure {
    dependsOn(tasks.named("rewriteRun"))
}

tasks.register("format") {
    group = "verification"
    dependsOn(tasks.named("spotlessApply"))
    dependsOn(tasks.named("versionCatalogFormat"))
}

val checkFormat by tasks.registering {
    group = "verification"
    dependsOn(tasks.named("spotlessCheck"))
    dependsOn(tasks.named("rewriteDryRun"))
}

tasks.named("check").configure {
    dependsOn(checkFormat)
}

tasks.named("versionCatalogFormat").configure {
    val versionCatalogFile = file("$projectDir/gradle/libs.versions.toml")

    inputs.file(versionCatalogFile)
    outputs.file(versionCatalogFile)
}

tasks.withType(AbstractRewriteTask::class).configureEach {
    inputs.files(fileTree("src") {
        include("**/*.java")
    })
    outputs.upToDateWhen { true }
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

val noOpusJar by tasks.registering(ShadowJar::class) {
    dependsOn(shadowJar)
    archiveClassifier.set(shadowJar.archiveClassifier.get() + "-no-opus")

    configurations = shadowJar.configurations
    from(sourceSets["main"].output)
    applyOpusExclusions(artifactFilters)
    manifest.from(jar.manifest)
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

val javadoc by tasks.getting(Javadoc::class) {
    isFailOnError = projectEnvironment.isGithubAction

    (options as? StandardJavadocDocletOptions)?.apply {
        memberLevel = JavadocMemberLevel.PUBLIC
        encoding = "UTF-8"

        author()
        tags("incubating:a:Incubating:")
        links("https://docs.oracle.com/en/java/javase/$currentJavaVersion/docs/api/", "https://takahikokawasaki.github.io/nv-websocket-client/")

        addStringOption("-release", "8")
        addBooleanOption("Xdoclint:all,-missing", true)

        overview = "$projectDir/overview.html"
    }

    dependsOn(generateJavaSources)
    source = generateJavaSources.get().source

    exclude {
        it.file.absolutePath.contains("internal", ignoreCase = false)
    }
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(javadoc)
    archiveClassifier.set("javadoc")
    from(javadoc.destinationDir)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    val args = mutableListOf("-Xlint:deprecation", "-Xlint:unchecked")

    options.compilerArgs.addAll(args)
}

val compileJava by tasks.getting(JavaCompile::class) {
    dependsOn(generateJavaSources)
    source = generateJavaSources.get().source

    options.release = 8
}

tasks.named<JavaCompile>("compileTestJava8Java") {
    options.release = 8
}

tasks.build.configure {
    dependsOn(jar)
    dependsOn(javadocJar)
    dependsOn(sourcesJar)
    dependsOn(shadowJar)
    dependsOn(noOpusJar)
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

tasks.test {
    useJUnitPlatform()
    failFast = false

    jvmArgs = listOf("-javaagent:${mockitoAgent.asPath}")

    testLogging {
        events("failed")
    }
    reports {
        junitXml.required = projectEnvironment.isGithubAction
        html.required = true
    }
}

val testJava8Compatibility by tasks.registering(Test::class) {
    group = "verification"

    useJUnitPlatform()
    failFast = true

    testClassesDirs = testJava8.output.classesDirs
    classpath = testJava8.runtimeClasspath

    javaLauncher = java8Toolchain.get()
}

tasks.named("check").configure {
    dependsOn(testJava8Compatibility)
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


// Generate pom file for maven central

fun MavenPom.populate() {
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

shadow {
    addShadowVariantIntoJavaComponent = false
}

val stagingDirectory = layout.buildDirectory.dir("staging-deploy").get()

publishing {
    publications {
        register<MavenPublication>("Release") {
            from(components["java"])

            artifactId = project.name
            groupId = project.group as String
            version = project.version as String

            artifact(sourcesJar)
            artifact(javadocJar)

            pom.populate()
        }
    }

    repositories.maven {
        url = stagingDirectory.asFile.toURI()
    }
}

jreleaser {
    project {
        versionPattern = "CUSTOM"
    }

    release {
        github {
            enabled = false
        }
    }

    signing {
        active = Active.RELEASE
        armored = true
    }

    deploy {
        maven {
            mavenCentral {
                register("sonatype") {
                    active = Active.RELEASE
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository(stagingDirectory.asFile.relativeTo(projectDir).path)
                }
            }
        }
    }
}

tasks.withType<AbstractJReleaserTask>().configureEach {
    mustRunAfter(tasks.named("publish"))
}
