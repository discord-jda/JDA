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

import java.util.*;

class InternalNames {
    static final String ITERABLE = toInternalName(Iterable.class);
    static final String ITERATOR = toInternalName(Iterator.class);
    static final String COLLECTION = toInternalName(Collection.class);
    static final String LIST = toInternalName(List.class);
    static final String SET = toInternalName(Set.class);
    static final String MAP = toInternalName(Map.class);
    static final String MAP_ENTRY = toInternalName(Map.Entry.class);

    private static String toInternalName(Class<?> clazz) {
        var desc = clazz.descriptorString();
        return desc.substring(1, desc.length() - 1);
    }
}
