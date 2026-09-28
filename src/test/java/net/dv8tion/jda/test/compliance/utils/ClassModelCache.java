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

import com.tngtech.archunit.core.domain.JavaClass;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ClassModelCache {
    private final Map<String, ClassModel> classModelCache = new HashMap<>();

    public ClassModel loadClassModel(JavaClass javaClass) {
        return classModelCache.computeIfAbsent(javaClass.getFullName(), _ -> {
            Path classLocation = getClassLocation(javaClass);
            try {
                return ClassFile.of().parse(classLocation);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static Path getClassLocation(JavaClass javaClass) {
        var source = javaClass.getSource().orElseThrow(() -> new AssertionError("No source for class " + javaClass));
        return Path.of(source.getUri());
    }
}
