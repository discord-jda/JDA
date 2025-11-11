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

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import net.dv8tion.jda.api.components.Component;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class ComponentConsistencyComplianceTest
{
    @Test
    void testComponentMethodsThatThemselvesAreOverridden()
    {
        classes()
            .that(areComponents())
            .should(overrideSupertypeMethodsWhichReturnsTheirDeclaringClass())
            .check(SourceSets.getApiClasses());
    }

    private DescribedPredicate<JavaClass> areComponents()
    {
        return DescribedPredicate.describe(
                "Is a Component",
                clazz -> clazz.isAssignableTo(Component.class)
        );
    }

    private ArchCondition<JavaClass> overrideSupertypeMethodsWhichReturnsTheirDeclaringClass()
    {
        return new ArchCondition<>("Overrides supertype methods which returns their declaring class")
        {
            @Override
            public void check(JavaClass item, ConditionEvents events)
            {
                final List<JavaMethod> supertypeMethodsReturningDeclClass = getComponentSupertypes(item)
                        // Methods declared by the supertypes
                        .flatMap(c -> c.getMethods().stream())
                        // Only keep root declarations
                        .filter(this::isRootDeclaration)
                        // Methods that return the class they are defined in
                        .filter(m -> m.getRawReturnType().getFullName().equals(m.getOwner().getFullName()))
                        .toList();

                // The method may exist but have a diff return type, or it may not be overridden
                for (JavaMethod supertypeMethodReturningDeclClass : supertypeMethodsReturningDeclClass)
                {
                    final Optional<JavaMethod> optDeclaredMethod = item.tryGetMethod(supertypeMethodReturningDeclClass.getName(), getParameterTypeNames(supertypeMethodReturningDeclClass));
                    if (!optDeclaredMethod.isPresent())
                    {
                        events.add(SimpleConditionEvent.violated(item, item.getFullName() + " does not override " + supertypeMethodReturningDeclClass.getFullName()));
                        continue;
                    }

                    final JavaMethod declaredMethod = optDeclaredMethod.get();
                    if (!declaredMethod.getRawReturnType().getFullName().equals(declaredMethod.getOwner().getFullName()))
                    {
                        events.add(SimpleConditionEvent.violated(declaredMethod, declaredMethod.getFullName() + " must override return type with " + declaredMethod.getOwner().getFullName()));
                        continue;
                    }
                }
            }

            private Stream<JavaClass> getComponentSupertypes(JavaClass clazz)
            {
                return Stream.concat(clazz.getAllRawSuperclasses().stream(), clazz.getAllRawInterfaces().stream())
                        .filter(c -> c.isAssignableTo(Component.class));
            }

            private String[] getParameterTypeNames(JavaMethod method)
            {
                return method.getRawParameterTypes().stream()
                        .map(JavaClass::getFullName)
                        .toArray(String[]::new);
            }

            private boolean isRootDeclaration(JavaMethod method)
            {
                // return true if no supertype has the same method
                return getComponentSupertypes(method.getOwner())
                        .flatMap(c -> c.getMethods().stream())
                        .noneMatch(m -> m.getName().equals(method.getName()) && m.getRawParameterTypes().equals(method.getRawParameterTypes()));
            }
        };
    }
}
