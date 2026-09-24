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

package net.dv8tion.jda.test.kotlin

import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.utils.Checks

/**
 * Proves that Java and Kotlin compile against each other inside one source set.
 *
 * This file is consumed by [JavaSeesKotlinProbe], a Java class in `src/test/java`, which exercises
 * the members below through ordinary Java call syntax. The mechanics it covers are exactly the ones
 * Phase 2 depends on:
 *
 * - a `class` whose companion functions only stay callable as `Probe.say(value)` because of
 *   `@JvmStatic`, which is what converted `static` interface methods will rely on;
 * - an `interface` with a body method, which is only a real `default` method for Java implementors
 *   because the build sets `-jvm-default=enable`;
 * - a Kotlin call into Java (here [Checks] / [DataObject]) in the other direction.
 *
 * The Java half is what makes this a gate rather than a lint. Kotlin-only tests would keep passing
 * even if these members stopped being visible to Java.
 */
@Suppress("UtilityClassWithPublicConstructor")
class KotlinJavaInteropProbe {
    companion object {
        @JvmStatic
        fun say(value: String): String = "probe:$value"
    }
}

/**
 * Implemented from Java in [JavaSeesKotlinProbe]; [describe] must be inherited rather than stubbed.
 */
interface KotlinDefaultMethodProbe {
    fun label(): String

    fun describe(): String = "default:${label()}"
}

/** Calls into Java from Kotlin, which has to resolve against the Java classes compiled in the same source set. */
fun buildJavaDataObject(value: String): DataObject = DataObject.empty().put("probe", value)

/** Uses a Java static that throws, so the Kotlin side observes the Java contract rather than reimplementing it. */
fun requireNonNull(value: String?): String {
    Checks.notNull(value, "value")
    return value!!
}
