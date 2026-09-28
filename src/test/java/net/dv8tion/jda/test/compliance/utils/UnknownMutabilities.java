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

package net.dv8tion.jda.test.compliance.utils;

import org.jetbrains.annotations.Unmodifiable;

import java.lang.classfile.Signature;
import java.lang.classfile.TypeAnnotation;
import java.util.*;

import javax.annotation.Nonnull;

public class UnknownMutabilities {

    private static final List<String> MUTABLE_TYPES = List.of(
            InternalNames.ITERABLE,
            InternalNames.ITERATOR,
            InternalNames.COLLECTION,
            InternalNames.LIST,
            InternalNames.SET,
            InternalNames.MAP,
            InternalNames.MAP_ENTRY);

    private final List<TypeAnnotation> typeAnnotations;

    private final List<List<Integer>> erroneousChains = new ArrayList<>();
    private final Deque<Integer> currentChain = new ArrayDeque<>();

    private UnknownMutabilities(List<TypeAnnotation> typeAnnotations) {
        this.typeAnnotations = typeAnnotations;
    }

    public static Problems findIn(Signature signature, List<TypeAnnotation> typeAnnotations) {
        var instance = new UnknownMutabilities(typeAnnotations);
        instance.findIn(signature);
        return new Problems(instance.erroneousChains);
    }

    private void findIn(Signature signature) {
        if (signature instanceof Signature.ClassTypeSig classTypeSig) {
            if (isMutableType(classTypeSig) && !isCurrentTypeAnnotated(typeAnnotations)) {
                erroneousChains.add(new ArrayList<>(currentChain));
            }

            // Recursion on type arguments (e.g. a list's element type)
            List<Signature.TypeArg> typeArgs = classTypeSig.typeArgs();
            for (int i = 0, typeArgsSize = typeArgs.size(); i < typeArgsSize; i++) {
                var typeArg = typeArgs.get(i);
                if (!(typeArg instanceof Signature.TypeArg.Bounded boundedTypeArg)) {
                    continue;
                }

                try {
                    currentChain.add(i);
                    findIn(boundedTypeArg.boundType());
                } finally {
                    currentChain.removeLast();
                }
            }
        }
    }

    private static boolean isMutableType(Signature.ClassTypeSig classTypeSig) {
        return MUTABLE_TYPES.contains(classTypeSig.className());
    }

    private boolean isCurrentTypeAnnotated(List<TypeAnnotation> typeAnnotations) {
        for (var typeAnnotation : typeAnnotations) {
            if (isMutabilityAnnotation(typeAnnotation)) {
                // There is at least one mutability annotation,
                // but it needs to be applied on the expected (sub)signature

                var pathComponents = typeAnnotation.targetPath();

                // If not all path components are type arguments,
                // the built path would be incompatible until the walker supports it
                if (!pathComponents.stream().allMatch(UnknownMutabilities::isTypeArgumentComponent)) {
                    continue;
                }

                List<Integer> typeArgumentPath = pathComponents.stream()
                        .map(TypeAnnotation.TypePathComponent::typeArgumentIndex)
                        .toList();

                if (new ArrayList<>(currentChain).equals(typeArgumentPath)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isMutabilityAnnotation(TypeAnnotation typeAnnotation) {
        var annotationName = typeAnnotation.annotation().className();
        return annotationName.equalsString(Descriptors.UNMODIFIABLE)
                || annotationName.equalsString(Descriptors.UNMODIFIABLE_VIEW)
                || annotationName.equalsString(Descriptors.MUTABLE);
    }

    private static boolean isTypeArgumentComponent(TypeAnnotation.TypePathComponent pathComponent) {
        return pathComponent.typePathKind() == TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT;
    }

    public static class Problems {
        private final List<List<Integer>> erroneousChains;

        private Problems(List<List<Integer>> erroneousChains) {
            this.erroneousChains = List.copyOf(erroneousChains);
        }

        @Nonnull
        public Problems excludingRoots() {
            return new Problems(
                    erroneousChains.stream().filter(chain -> !chain.isEmpty()).toList());
        }

        public boolean hasProblems() {
            return !erroneousChains.isEmpty();
        }

        @Nonnull
        @Unmodifiable
        public List<List<Integer>> getErroneousChains() {
            return erroneousChains;
        }

        public boolean hasRootProblem() {
            return contains(Collections.emptyList());
        }

        public boolean contains(Collection<Integer> chain) {
            for (List<Integer> erroneousChain : erroneousChains) {
                if (chainMatches(erroneousChain, chain)) {
                    return true;
                }
            }
            return false;
        }

        private boolean chainMatches(List<Integer> a, Collection<Integer> b) {
            return a.size() == b.size() && a.equals(new ArrayList<>(b));
        }
    }
}
