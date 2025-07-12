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

plugins {
    java

    id("org.openrewrite.build.recipe-library") version "2.0.3"
}

repositories {
    mavenCentral()
}

dependencies {
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

recipeDependencies {
    parserClasspath("net.dv8tion:JDA:5.6.1")
}

val addRewriteToResources by tasks.registering(Copy::class) {
    from("rewrite.yml")
    into("src/main/resources/META-INF/rewrite")
}

tasks.named("downloadRecipeDependencies").configure {
    dependsOn(addRewriteToResources)
}

tasks.withType<ProcessResources>().configureEach {
    dependsOn(tasks.named("downloadRecipeDependencies"))
}
