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

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import net.dv8tion.jda.annotations.UnknownNullability;
import net.dv8tion.jda.api.managers.Manager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.IOBiConsumer;
import net.dv8tion.jda.api.utils.IOFunction;
import org.jetbrains.annotations.Contract;
import org.junit.jupiter.api.Test;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

public class ArchUnitComplianceTest
{
    final JavaClasses apiClasses = new ClassFileImporter().importPackages("net.dv8tion.jda.api");

    @Test
    void testMethodsThatReturnRestActionHaveCorrectAnnotations()
    {
        methods()
            .that()
            .haveRawReturnType(assignableTo(RestAction.class))
            .and()
            .arePublic()
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
            .and()
            .arePublic()
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

    @Test
    void testMethodsThatAcceptObjectShouldHaveNullabilityAnnotations()
    {
        methods()
            .that()
            .arePublic()
            .and().doNotHaveName("equals")
            .and().doNotHaveName("valueOf")
            .and().doNotHaveName("accept")
            .and().doNotHaveName("test")
            .and().doNotHaveName("formatTo")
            .and().areNotDeclaredIn(IOFunction.class)
            .and().areNotDeclaredIn(IOBiConsumer.class)
            .should(haveNonPrimitiveParametersAnnotatedWithNullability())
            .check(apiClasses);
    }

    @Test
    void testMethodsThatReturnPrimitivesShouldNotHaveNullabilityAnnotations()
    {
        methods()
            .that()
            .haveRawReturnType(describe("primitive", JavaClass::isPrimitive))
            .and()
            .arePublic()
            .should()
            .notBeAnnotatedWith(Nonnull.class)
            .andShould()
            .notBeAnnotatedWith(Nullable.class)
            .check(apiClasses);
    }

    @Test
    void testRestActionClassesFollowNamePattern()
    {
        classes()
            .that()
            .areAssignableTo(RestAction.class)
            .and()
            .areNotAssignableTo(Manager.class)
            .and()
            .arePublic()
            .should()
            .haveSimpleNameEndingWith("Action")
            .check(apiClasses);
    }

    @Test
    void testManagerClassesFollowNamePattern()
    {
        classes()
            .that()
            .areAssignableTo(Manager.class)
            .and()
            .arePublic()
            .should()
            .haveSimpleNameEndingWith("Manager")
            .check(apiClasses);
    }

    @Test
    void testInternalClassesAreNotInApiPackage()
    {
        classes()
            .that()
            .arePublic()
            .and()
            .haveSimpleNameEndingWith("Impl")
            .should()
            .resideOutsideOfPackage("net.dv8tion.jda.api..")
            .allowEmptyShould(true)
            .check(apiClasses);
    }

    private ArchCondition<JavaMethod> haveNonPrimitiveParametersAnnotatedWithNullability()
    {
        return new ArchCondition<JavaMethod>("have non-primitive parameters annotated with @Nonnull or @Nullable")
        {
            @Override
            public void check(JavaMethod method, ConditionEvents events)
            {
                method.getParameters()
                    .stream()
                    .filter(parameter -> !parameter.getRawType().isPrimitive())
                    .filter(parameter -> !parameter.getRawType().isArray())
                    .filter(parameter -> !parameter.isAnnotatedWith(Nonnull.class) && !parameter.isAnnotatedWith(Nullable.class) && !parameter.isAnnotatedWith(CheckForNull.class))
                    .forEach(parameter ->
                        events.add(SimpleConditionEvent.violated(method, parameter.getDescription() + " is not annotated with @Nonnull or @Nullable"))
                    );
            }
        };
    }
}
