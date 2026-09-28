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

import java.lang.classfile.Signature;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class MissingTypeAnnotationPrinter {
    private final UnknownMutabilities.Problems problems;

    private final Deque<Integer> currentChain = new ArrayDeque<>();
    private final StringBuilder builder = new StringBuilder();

    private MissingTypeAnnotationPrinter(UnknownMutabilities.Problems problems) {
        this.problems = problems;
    }

    public static String print(Signature signature, UnknownMutabilities.Problems problems) {
        var printer = new MissingTypeAnnotationPrinter(problems);
        printer.print(signature);
        return printer.builder.toString();
    }

    private void print(Signature signature) {
        switch (signature) {
            case Signature.ClassTypeSig classTypeSig -> {
                // Ignore root annotations as we want it displayed above the method declaration,
                // not on the return type
                if (!currentChain.isEmpty() && problems.contains(currentChain)) {
                    builder.append(Ansi.boldUnderline("[HERE]")).append(' ');
                }
                builder.append(classTypeSig.classDesc().displayName());

                // Recursion on type arguments (e.g. a list's element type)
                List<Signature.TypeArg> typeArgs = classTypeSig.typeArgs();
                if (!typeArgs.isEmpty()) {
                    builder.append("<");
                }
                for (int i = 0, typeArgsSize = typeArgs.size(); i < typeArgsSize; i++) {
                    var typeArg = typeArgs.get(i);
                    if (!(typeArg instanceof Signature.TypeArg.Bounded boundedTypeArg)) {
                        continue;
                    }

                    try {
                        currentChain.add(i);
                        print(boundedTypeArg.boundType());
                        if (i + 1 != typeArgsSize) {
                            builder.append(", ");
                        }
                    } finally {
                        currentChain.removeLast();
                    }
                }
                if (!typeArgs.isEmpty()) {
                    builder.append(">");
                }
            }
            case Signature.ArrayTypeSig arrayTypeSig -> {
                print(arrayTypeSig.componentSignature());
                builder.append("[]");
            }
            case Signature.BaseTypeSig baseTypeSig -> builder.append(baseTypeSig.signatureString());
            case Signature.TypeVarSig typeVarSig -> builder.append(typeVarSig.identifier());
            default -> throw new UnsupportedOperationException("Unsupported signature: " + signature);
        }
    }
}
