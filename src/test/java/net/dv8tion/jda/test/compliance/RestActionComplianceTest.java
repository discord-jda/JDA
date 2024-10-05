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

package net.dv8tion.jda.test.compliance;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import net.dv8tion.jda.annotations.UnknownNullability;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Contract;
import org.junit.jupiter.api.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

public class RestActionComplianceTest
{
    final JavaClasses apiClasses = new ClassFileImporter().importPackages("net.dv8tion.jda.api");

    @Test
    void testMethodsThatReturnRestActionHaveCorrectAnnotations()
    {
        methods()
            .that()
            .haveRawReturnType(assignableTo(RestAction.class))
            .should()
            .beAnnotatedWith(CheckReturnValue.class)
            .andShould()
            .beAnnotatedWith(Nonnull.class)
            .check(apiClasses);
    }

    @Test
    void testMethodsThatReturnCompletableFutureHaveCorrectAnnotations()
    {
        methods()
            .that()
            .haveRawReturnType(assignableTo(CompletableFuture.class))
            .should()
            .beAnnotatedWith(CheckReturnValue.class)
            .andShould()
            .beAnnotatedWith(Nonnull.class)
            .check(apiClasses);
    }

    @Test
    void testMethodsThatReturnObjectShouldHaveNullabilityAnnotations()
    {
        methods()
            .that()
            .haveRawReturnType(assignableTo(Object.class))
            .and()
            .arePublic()
            .and()
            .doNotHaveName("getNewValue")
            .and()
            .doNotHaveName("getOldValue")
            .and()
            .doNotHaveName("valueOf")
            .and()
            .doNotHaveName("toString")
            .should()
            .beAnnotatedWith(Nonnull.class)
            .orShould()
            .beAnnotatedWith(Nullable.class)
            .orShould()
            .beAnnotatedWith(Contract.class)
            .orShould()
            .beAnnotatedWith(UnknownNullability.class)
            .check(apiClasses);
    }
}
