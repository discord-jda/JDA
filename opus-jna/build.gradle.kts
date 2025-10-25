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

import net.dv8tion.jda.tasks.registerPublication

plugins {
    `java-library`
    `jda-publish`
}

////////////////////////////////////
//                                //
//      Module Configuration      //
//                                //
////////////////////////////////////

group = rootProject.group
version = rootProject.version
val fullProjectName = "${rootProject.name}-${project.name}"

base {
    archivesName.set(fullProjectName)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withSourcesJar()
}


////////////////////////////////////
//                                //
//    Dependency Configuration    //
//                                //
////////////////////////////////////

repositories {
    mavenCentral()
}

dependencies {

    /* Internal dependencies */

    // JDA
    implementation(rootProject)

    //Logger
    implementation(libs.slf4j)

    //Opus library support
    implementation(libs.opus)

    //we use this only together with opus-java
    // if that dependency is excluded it also doesn't need jna anymore
    // since jna is a transitive runtime dependency of opus-java we don't include it explicitly as dependency
    implementation(libs.jna)

    /* Annotations */

    //Code safety
    compileOnly(libs.findbugs)
}

////////////////////////////////////
//                                //
//    Build Task Configuration    //
//                                //
////////////////////////////////////

val jar by tasks.getting(Jar::class) {
    archiveBaseName.set(fullProjectName)
    manifest.attributes("Implementation-Version" to project.version, "Automatic-Module-Name" to "net.dv8tion.jda")
}

val javadocJar by tasks.registering(Jar::class) {
    this.archiveBaseName.set(fullProjectName)
    archiveClassifier.set("javadoc")
    // Empty by design
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    val args = mutableListOf("-Xlint:deprecation", "-Xlint:unchecked")

    if (JavaVersion.current().isJava9Compatible) {
        args.add("--release")
        args.add("8")
    }

    options.compilerArgs.addAll(args)
}

////////////////////////////////////
//                                //
//    Publishing And Signing      //
//                                //
////////////////////////////////////

registerPublication(
        name = fullProjectName,
        description = "Opus support for JDA, based on JNA",
        url = "https://github.com/discord-jda/JDA/tree/master/opus-jna",
) {
    from(components["java"])

    artifact(javadocJar)
}
