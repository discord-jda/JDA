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

package net.dv8tion.jda.test.kotlin;

import net.dv8tion.jda.api.utils.data.DataObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * The Java half of the interop gate. Never convert this file: it exists to keep proving that Java
 * call sites still compile and behave against Kotlin declarations.
 *
 * @see KotlinJavaInteropProbe
 */
class JavaSeesKotlinProbe {
    @Test
    void callsKotlinCompanionMemberAsStatic() {
        assertThat(KotlinJavaInteropProbe.say("hi")).isEqualTo("probe:hi");
    }

    @Test
    void implementsKotlinInterfaceWithoutOverridingDefaultMethod() {
        KotlinDefaultMethodProbe probe = () -> "java";

        assertThat(probe.label()).isEqualTo("java");
        assertThat(probe.describe()).isEqualTo("default:java");
    }

    @Test
    void kotlinCallsIntoJava() {
        DataObject data = KotlinJavaInteropProbeKt.buildJavaDataObject("ok");

        assertThat(data.getString("probe")).isEqualTo("ok");
    }

    @Test
    void javaContractSurvivesKotlinCall() {
        assertThat(KotlinJavaInteropProbeKt.requireNonNull("ok")).isEqualTo("ok");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> KotlinJavaInteropProbeKt.requireNonNull(null))
                .withMessage("value may not be null");
    }
}
