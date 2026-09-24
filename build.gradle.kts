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
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import de.undercouch.gradle.tasks.download.Download
import net.dv8tion.jda.gradle.Version
import net.dv8tion.jda.gradle.plugins.applyAudioExclusions
import net.dv8tion.jda.gradle.plugins.applyOpusExclusions
import net.dv8tion.jda.gradle.tasks.GeneratePublicApiDump
import net.dv8tion.jda.gradle.tasks.VerifyBytecodeVersion
import net.dv8tion.jda.gradle.tasks.VerifyPublicApi
import net.ltgt.gradle.errorprone.errorprone
import nl.littlerobots.vcu.plugin.resolver.VersionSelectors
import org.jetbrains.gradle.ext.Gradle as GradleRunConfiguration
import org.jetbrains.gradle.ext.JUnit as JUnitRunConfiguration
import org.jetbrains.gradle.ext.copyright
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openrewrite.gradle.AbstractRewriteTask

plugins {
    artifacts
    environment
    idea
    `model-generator`
    `java-library`
    `maven-publish`
    signing

    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.spotless)
    alias(libs.plugins.errorprone)
    alias(libs.plugins.openrewrite)
    alias(libs.plugins.ideax)
    alias(libs.plugins.nmcp)
    alias(libs.plugins.nmcp.aggregation)
}


////////////////////////////////////
//                                //
//     Project Configuration      //
//                                //
////////////////////////////////////

val javaVersion = JavaLanguageVersion.of(25)

projectEnvironment {
    version = Version(major = "6", minor = "7", revision = "0", classifier = null)
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
    includes = listOf(
            "AvailableLocalesEnum",
            "CreateRoleRequest",
            "MessageType",
            "ChannelTypes",
            "AuditLogActionTypes",
            "InviteTypes",
            "WebhookTypes",
    )
}

idea {
    project {
        settings {
            copyright {
                val jdaCopyrightProfileName = "JDA"

                useDefault = jdaCopyrightProfileName

                profiles.create(jdaCopyrightProfileName) {
                    notice = file("gradle/copyright-header.txt").readText(Charsets.UTF_8)
                }
            }

            runConfigurations {
                defaults(JUnitRunConfiguration::class.java) {
                    vmParameters = listOf(
                            "-ea",
                            "-Duser.timezone=GMT",
                            "-Duser.language=en",
                            "-Duser.country=US",
                            "-Dfile.encoding=utf-8",
                            "-DupdateSnapshots",
                    ).joinToString(" ")
                }

                register<GradleRunConfiguration>("format") {
                    taskNames = listOf("format")
                }
            }
        }
    }
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

val examples = sourceSets.create("examples") {
    java.srcDir("src/examples/java")
    compileClasspath += sourceSets["main"].output
    runtimeClasspath += sourceSets["main"].output
}

java {
    withJavadocJar()
    withSourcesJar()

    toolchain {
        languageVersion.set(javaVersion)
    }
}

kotlin {
    jvmToolchain(25)

    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
        // Keeps interface default methods as real default methods in bytecode, with DefaultImpls
        // retained, so Java implementors of converted interfaces are unaffected.
        // Kotlin 2.2 renamed -Xjvm-default=all-compatibility to -jvm-default=enable.
        freeCompilerArgs.add("-jvm-default=enable")
        allWarningsAsErrors.set(true)
    }
}

detekt {
    config.setFrom(files("gradle/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
}

val detektSourceRoots = fileTree(projectDir) {
    include("src/main/kotlin/**/*.kt", "src/test/kotlin/**/*.kt")
}

tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
    setSource(detektSourceRoots)
    reports {
        html.required.set(true)
        sarif.required.set(false)
    }

    // detekt has no work to do until Kotlin sources exist outside the build scripts.
    onlyIf { !detektSourceRoots.isEmpty }
}

////////////////////////////////////
//                                //
//    Dependency Configuration    //
//                                //
////////////////////////////////////

val currentJavaVersion = JavaVersion.current().majorVersion

val mockitoAgent = configurations.create("mockitoAgent")

val examplesImplementation = configurations.getByName("examplesImplementation") {
    extendsFrom(configurations.implementation.get())
}

repositories {
    mavenCentral()
}

dependencies {
    /* ABI dependencies */

    //Code safety
    compileOnly(libs.findbugs)
    compileOnly(libs.jetbrains.annotations)

    //Kotlin
    // Required at runtime by converted classes: Kotlin emits calls to kotlin.jvm.internal.Intrinsics
    // for parameter null checks, so consumers need the stdlib on their classpath. It previously
    // arrived transitively through okhttp, which is not a guarantee we control.
    // `kotlin.stdlib.default.dependency=false` in gradle.properties still suppresses the plugin's
    // own implicit `implementation` edge; this is the deliberate, published replacement for it.
    api(libs.kotlin.stdlib)

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

    examplesImplementation(libs.jdave)

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

    // Linting & Formatting
    errorprone(libs.errorprone.core)

    // Publishing
    nmcpAggregation(rootProject)
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
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

    val copyrightHeader = file("gradle/copyright-header.txt")
            .readText(Charsets.UTF_8)
            .trim()
            .prependIndent(" * ")

    kotlinGradle {
        target("*.gradle.kts", "buildSrc/*.gradle.kts", "buildSrc/src/**/*.kt*")

        trimTrailingWhitespace()
        leadingTabsToSpaces()
    }

    kotlin {
        target("src/**/*.kt")

        ktlint("1.6.0")
        licenseHeader("/*\n$copyrightHeader\n */\n\n")
        trimTrailingWhitespace()
    }

    java {
        palantirJavaFormat("2.84.0")
                .formatJavadoc(false)

        licenseHeader("/*\n$copyrightHeader\n */\n\n")

        target("src/**/*.java")

        removeUnusedImports()
        importOrder("", "java", "javax", "\\#")
        trimTrailingWhitespace()
    }
}

tasks.named("spotlessJavaCheck").configure {
    dependsOn(tasks.named("rewriteDryRun"))
}

tasks.named("spotlessJavaApply").configure {
    dependsOn(tasks.named("rewriteRun"))
}

val enableErrorpronePatching = tasks.register("enableErrorpronePatching") {
    group = "verification"

    doFirst {
        tasks.withType<JavaCompile>().configureEach {
            options.errorprone {
                errorproneArgs.add("-XepPatchChecks:MissingOverride")
                errorproneArgs.add("-XepPatchLocation:IN_PLACE")
            }
        }
    }
}

tasks.register("format") {
    group = "verification"
    dependsOn(enableErrorpronePatching)
    dependsOn(tasks.named("spotlessApply"))
    dependsOn(tasks.named("versionCatalogFormat"))
}

val checkFormat = tasks.register("checkFormat") {
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

val jar = tasks.getByName<Jar>("jar") {
    archiveBaseName.set(project.name)
    manifest.attributes("Implementation-Version" to project.version, "Automatic-Module-Name" to "net.dv8tion.jda")
}

val shadowJar = tasks.getByName<ShadowJar>("shadowJar") {
    archiveClassifier.set("withDependencies")
    exclude("*.pom")
}

val noOpusJar = tasks.register<ShadowJar>("noOpusJar") {
    dependsOn(shadowJar)
    archiveClassifier.set(shadowJar.archiveClassifier.get() + "-no-opus")

    from(sourceSets["main"].output)
    applyOpusExclusions(artifactFilters)
}

val minimalJar = tasks.register<ShadowJar>("minimalJar") {
    dependsOn(shadowJar)
    minimize()
    archiveClassifier.set(shadowJar.archiveClassifier.get() + "-min")

    from(sourceSets["main"].output)
    applyAudioExclusions(artifactFilters)
}

tasks.withType<ShadowJar>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.FAIL
    mergeServiceFiles()

    exclude("**/LICENSE*")
    exclude("**/LICENCE*")
    exclude("**/README*")
    exclude("**/NOTICE*")

    if (this != shadowJar) {
        manifest.from(shadowJar.manifest)
        configurations = shadowJar.configurations
        excludes.addAll(shadowJar.excludes)
    }
}

val javadoc = tasks.getByName<Javadoc>("javadoc") {
    isFailOnError = projectEnvironment.isGithubAction

    (options as? StandardJavadocDocletOptions)?.apply {
        memberLevel = JavadocMemberLevel.PUBLIC
        encoding = "UTF-8"
        locale = "en_US"

        author()
        tags("incubating:a:Incubating:")
        links("https://docs.oracle.com/en/java/javase/$currentJavaVersion/docs/api/", "https://takahikokawasaki.github.io/nv-websocket-client/")

        addStringOption("-link-modularity-mismatch", "info")
        addStringOption("-release", javaVersion.asInt().toString())
        addBooleanOption("-syntax-highlight", true)
        addBooleanOption("Xdoclint:all,-missing", true)

        overview = "$projectDir/overview.html"
    }

    exclude {
        it.file.absolutePath.contains("internal", ignoreCase = false)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.isIncremental = true

    options.compilerArgs.addAll(listOf(
            "-Werror",
            "-Xlint:all",
            // warnings for overriding finalize(), which is deprecated for removal but still supported (Error Prone's Finalize check is disabled too)
            "-Xlint:-removal",
            // warnings for missing serialVersionUID in exceptions (we don't intend for exceptions to be serialized)
            "-Xlint:-serial",
            // warnings for calling member methods in constructor, which we do for argument checks
            "-Xlint:-this-escape",
            // warnings for unused resource in try-with-resources (we use them for locks)
            "-Xlint:-try",
            // warnings for potentially unsafe varargs, this is already handled by @SafeVarargs
            "-Xlint:-varargs",
    ))

    options.errorprone {
        disable(
                "AssignmentExpression",
                "ByteBufferBackingArray",
                "CheckReturnValue",
                "DoubleCheckedLocking",
                "EffectivelyPrivate",
                "EmptyCatch",
                "EnumOrdinal",
                "Finalize",
                "FutureReturnValueIgnored",
                "InvalidBlockTag",
                "JavaDurationGetSecondsToToSeconds",
                "JavaTimeDefaultTimeZone",
                "MathAbsoluteNegative",
                "MixedMutabilityReturnType",
                "OperatorPrecedence",
                "PatternMatchingInstanceof",
                "StatementSwitchToExpressionSwitch",
                "StringConcatToTextBlock",
                "StringSplitter",
                "TypeParameterUnusedInFormals",
                "UnnamedVariable",
                "UnnecessaryLambda",
                "UnusedMethod",
        )
    }

    mustRunAfter(enableErrorpronePatching)
}

val compileJava = tasks.getByName<JavaCompile>("compileJava") {
    options.release = javaVersion.asInt()
}

tasks.named<JavaCompile>("compileExamplesJava") {
    options.errorprone {
        disableAllChecks.set(true)
    }
}

tasks.build.configure {
    dependsOn(jar)
    dependsOn(shadowJar)
    dependsOn(noOpusJar)
    dependsOn(minimalJar)

    jar.mustRunAfter(tasks.clean)
}


////////////////////////////////////
//                                //
//       Test Configuration       //
//                                //
////////////////////////////////////


val downloadRecipeClasspath = tasks.register<Download>("downloadRecipeClasspath") {
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

    jvmArgs(
            "-javaagent:${mockitoAgent.asPath}",
            // https://github.com/raphw/byte-buddy/issues/1803
            "-Dnet.bytebuddy.safe=true"
    )

    testLogging {
        events("failed")
    }
    reports {
        junitXml.required = projectEnvironment.isGithubAction
        html.required = true
    }
}

val kotlinClasses = tasks.named<KotlinCompile>("compileKotlin").map { task ->
    task.outputs.files.asFileTree.matching {
        include("**/*.class")
    }
}

// Directory form of the compile output. `javap` resolves a binary name only against directory or
// jar classpath entries, so the loose-.class `kotlinClasses` FileTree above cannot be used for the
// API tasks: javap would fail to resolve every Kotlin class and report it as removed.
val mainClassesDirs = sourceSets.main.get().output.classesDirs

val verifyBytecodeVersion = tasks.register<VerifyBytecodeVersion>("verifyBytecodeVersion") {
    group = "verification"

    expectedMajorVersion = 69
    classes.from(compileJava.outputs.files.asFileTree.matching {
        include("**/*.class")
    })
    // Kotlin output has to clear the same gate; without this, the first converted file would
    // silently escape the check until someone noticed downstream.
    classes.from(kotlinClasses)
}

compileJava.finalizedBy(verifyBytecodeVersion)
tasks.named<KotlinCompile>("compileKotlin") {
    finalizedBy(verifyBytecodeVersion)
}

////////////////////////////////////////////////////////////////////////////
//                                                                        //
//    Public API compatibility (net.dv8tion.jda.api only)                 //
//                                                                        //
//    Implemented with the JDK's javap rather than a bytecode library,    //
//    because binary-compatibility-validator cannot read major 69.        //
//                                                                        //
////////////////////////////////////////////////////////////////////////////

val publicApiPrefix = "net.dv8tion.jda.api."
val publicApiBaseline = layout.projectDirectory.file("api/JDA.api")

val apiClasspath = sourceSets.main.get().compileClasspath

val apiDump = tasks.register<GeneratePublicApiDump>("apiDump") {
    group = "verification"
    description = "Regenerates the public API baseline. Review the diff before committing."

    packagePrefix.set(publicApiPrefix)
    classes.from(mainClassesDirs)
    classpath.from(apiClasspath, mainClassesDirs)
    outputFile.set(publicApiBaseline)
}

val apiCheck = tasks.register<VerifyPublicApi>("apiCheck") {
    group = "verification"
    description = "Fails if the public API baseline loses classes or members."

    packagePrefix.set(publicApiPrefix)
    classes.from(mainClassesDirs)
    classpath.from(apiClasspath, mainClassesDirs)
    baseline.set(publicApiBaseline)
}

tasks.named("check") {
    dependsOn(apiCheck)
    dependsOn(tasks.named("detekt"))
}

// apiDump writes the file apiCheck reads, so Gradle's implicit-dependency validation rejects a
// combined `./gradlew check apiDump`. Order them explicitly: validate the committed baseline first,
// then regenerate it. The reverse ordering would make apiCheck compare against a baseline it had
// just rewritten, which always passes and silently defeats the gate.
apiDump.configure {
    mustRunAfter(apiCheck)
}

tasks.withType<Test>().configureEach {
    systemProperties.putAll(mapOf(
            "user.timezone" to "GMT",
            "user.language" to "en",
            "user.country" to "US",
            "file.encoding" to "utf-8",
    ))
}


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

val mavenCentralUsername: String? = System.getenv("MAVENCENTRAL_USERNAME")?.takeIf { it.isNotBlank() }
val mavenCentralPassword: String? = System.getenv("MAVENCENTRAL_TOKEN")?.takeIf { it.isNotBlank() }
val gpgSecretKey: String? = System.getenv("GPG_SECRET_KEY")?.takeIf { it.isNotBlank() }
val gpgPassphrase: String? = System.getenv("GPG_PASSPHRASE")?.takeIf { it.isNotBlank() }

val stagingDirectory = layout.buildDirectory.dir("staging-deploy").get()

publishing {
    publications {
        register<MavenPublication>("Release") {
            from(components["java"])

            artifactId = project.name
            groupId = project.group as String
            version = project.version as String

            pom.populate()
        }
    }
}

if (gpgSecretKey != null) {
    signing {
        useInMemoryPgpKeys(gpgSecretKey, gpgPassphrase ?: "")
        sign(publishing.publications)
    }
}

nmcpAggregation {
    localRepository {
        name = "staging-deploy"
        path = stagingDirectory.asFile.path
    }

    centralPortal {
        username.set(mavenCentralUsername)
        password.set(mavenCentralPassword)
    }
}
