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
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.SourceCodeLocation;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import net.dv8tion.jda.test.compliance.utils.Ansi;
import net.dv8tion.jda.test.compliance.utils.ClassModelCache;
import net.dv8tion.jda.test.compliance.utils.MissingTypeAnnotationPrinter;
import net.dv8tion.jda.test.compliance.utils.UnknownMutabilities;
import org.junit.jupiter.api.Test;

import java.lang.classfile.*;
import java.lang.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute;
import java.lang.classfile.attribute.SignatureAttribute;
import java.lang.constant.ClassDesc;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

public class KotlinCollectionsInteroperabilityTest {
    @Test
    void testReturnedCollectionsHaveMutabilityAnnotation() {
        methods()
                .that()
                .arePublic()
                .or()
                .areProtected()
                .and(DescribedPredicate.alwaysTrue().as("return mutable types"))
                .and()
                // Overrides with different return/parameter types makes javac generate synthetic bridges,
                // ArchUnit picks them up as it reads the bytecode,
                // we can ignore those as they are inaccessible without reflection.
                .doNotHaveModifier(JavaModifier.SYNTHETIC)
                .should(haveUnmodifiableOrKotlinMutableAnnotation())
                .check(SourceSets.getApiClasses());
    }

    private static ArchCondition<JavaMethod> haveUnmodifiableOrKotlinMutableAnnotation() {
        return new ArchCondition<>("have @Unmodifiable(View) or Kotlin's @Mutable annotation") {

            private static final ClassModelCache classModelCache = new ClassModelCache();

            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                var classModel = classModelCache.loadClassModel(method.getOwner());
                var methodModel = findMethodModel(classModel, method);

                // Collection types has type arguments so it always carries a Signature attribute
                var signature = methodModel
                        .findAttribute(Attributes.signature())
                        .map(SignatureAttribute::asMethodSignature)
                        .orElse(null);
                if (signature == null) {
                    return;
                }

                var problems = UnknownMutabilities.findIn(signature.result(), getTypeAnnotations(methodModel));

                if (problems.hasProblems()) {
                    events.add(SimpleConditionEvent.violated(
                            method, methodDeclarationWithHints(method, methodModel, signature, problems)));
                }
            }

            @Nonnull
            private static MethodModel findMethodModel(ClassModel classModel, JavaMethod method) {
                return classModel.methods().stream()
                        .filter(m -> m.methodName().equalsString(method.getName())
                                && m.methodType().equalsString(method.getDescriptor()))
                        .findAny()
                        .orElseThrow(() -> new AssertionError("Could not find matching MethodModel for " + method));
            }

            @Nonnull
            private static String methodDeclarationWithHints(
                    JavaMethod method,
                    MethodModel methodModel,
                    MethodSignature signature,
                    UnknownMutabilities.Problems problems) {
                String modifiers = methodModel.flags().flags().stream()
                        .filter(AccessFlag::sourceModifier)
                        .map(accessFlag -> Modifier.toString(accessFlag.mask()))
                        .collect(Collectors.joining(" "));
                String returnTypeWithHints = MissingTypeAnnotationPrinter.print(signature.result(), problems);
                String parameterDisplayNames = methodModel.methodTypeSymbol().parameterList().stream()
                        .map(ClassDesc::displayName)
                        .collect(Collectors.joining(", "));
                SourceCodeLocation location = method.getSourceCodeLocation();

                return "Method is missing one or more @Unmodifiable(View) / @Mutable => %s%s %s %s.%s(%s) (%s:%s)"
                        .formatted(
                                problems.hasRootProblem() ? Ansi.boldUnderline("[HERE]") + ' ' : "",
                                modifiers,
                                returnTypeWithHints,
                                method.getOwner().getSimpleName(),
                                method.getName(),
                                parameterDisplayNames,
                                location.getSourceFileName(),
                                location.getLineNumber());
            }
        };
    }

    @Test
    void testSupertypesHaveMutabilityAnnotation() {
        classes().should(haveMutabilityAnnotations()).check(SourceSets.getApiClasses());
    }

    @Nonnull
    private static ArchCondition<JavaClass> haveMutabilityAnnotations() {
        return new ArchCondition<>("have mutability annotations") {

            private static final ClassModelCache classModelCache = new ClassModelCache();

            @Override
            public void check(JavaClass item, ConditionEvents events) {
                var classModel = classModelCache.loadClassModel(item);

                // Collection types has type arguments so it always carries a Signature attribute
                var signature = classModel.findAttribute(Attributes.signature()).orElse(null);
                if (signature == null) {
                    return;
                }

                var typeAnnotations = getTypeAnnotations(classModel);
                var classSignature = signature.asClassSignature();

                checkSuperclass(item, events, typeAnnotations, classSignature);
                checkSuperinterfaces(item, events, classSignature, typeAnnotations);
            }

            private static void checkSuperclass(
                    JavaClass item,
                    ConditionEvents events,
                    List<TypeAnnotation> typeAnnotations,
                    ClassSignature classSignature) {
                var problems = UnknownMutabilities.findIn(classSignature.superclassSignature(), typeAnnotations);

                if (problems.hasProblems()) {
                    events.add(SimpleConditionEvent.violated(
                            item, extendingClassDeclarationWithHints(item, classSignature, problems)));
                }
            }

            @Nonnull
            private static String extendingClassDeclarationWithHints(
                    JavaClass item, ClassSignature signature, UnknownMutabilities.Problems problems) {
                String declType = item.isInterface() ? "interface" : "class";
                String superclassWithHints =
                        MissingTypeAnnotationPrinter.print(signature.superclassSignature(), problems);
                String sourceFileName = item.getSourceCodeLocation().getSourceFileName();

                return "%s %s extends %s ... (add @Unmodifiable(View) / @Mutable appropriately) (%s:0)"
                        .formatted(declType, item.getSimpleName(), superclassWithHints, sourceFileName);
            }

            private static void checkSuperinterfaces(
                    JavaClass item,
                    ConditionEvents events,
                    ClassSignature classSignature,
                    List<TypeAnnotation> typeAnnotations) {
                for (var superinterfaceSignature : classSignature.superinterfaceSignatures()) {
                    // We want to ignore root problems because mutability annotations don't work on roots of supertypes,
                    // but still works on type arguments
                    var problems = UnknownMutabilities.findIn(superinterfaceSignature, typeAnnotations)
                            .excludingRoots();

                    if (problems.hasProblems()) {
                        events.add(SimpleConditionEvent.violated(
                                item, implementingClassDeclarationWithHints(item, superinterfaceSignature, problems)));
                    }
                }
            }

            @Nonnull
            private static String implementingClassDeclarationWithHints(
                    JavaClass javaClass, Signature.ClassTypeSig signature, UnknownMutabilities.Problems problems) {
                String declType = javaClass.isInterface() ? "interface" : "class";
                String extensionKeyword = javaClass.isInterface() ? "extends" : "implements";
                String superinterfaceWithHints = MissingTypeAnnotationPrinter.print(signature, problems);
                String sourceFileName = javaClass.getSourceCodeLocation().getSourceFileName();

                return "%s %s %s %s ... (add @Unmodifiable(View) / @Mutable appropriately) (%s:0)"
                        .formatted(
                                declType,
                                javaClass.getSimpleName(),
                                extensionKeyword,
                                superinterfaceWithHints,
                                sourceFileName);
            }
        };
    }

    @Nonnull
    private static List<TypeAnnotation> getTypeAnnotations(AttributedElement element) {
        return element.findAttribute(Attributes.runtimeInvisibleTypeAnnotations())
                .map(RuntimeInvisibleTypeAnnotationsAttribute::annotations)
                .orElse(Collections.emptyList());
    }
}
