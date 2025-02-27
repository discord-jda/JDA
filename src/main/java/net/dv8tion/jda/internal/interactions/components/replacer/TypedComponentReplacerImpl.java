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

package net.dv8tion.jda.internal.interactions.components.replacer;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Predicate;

public class TypedComponentReplacerImpl<T extends Component> implements ComponentReplacer
{
    private final Class<? super T> type;
    private final Predicate<? super T> filter;
    private final Function<? super T, ? extends Component> updater;

    public TypedComponentReplacerImpl(Class<? super T> type, Predicate<? super T> filter, Function<? super T, ? extends Component> updater)
    {
        this.type = type;
        this.filter = filter;
        this.updater = updater;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public Component apply(@Nonnull Component oldComponent)
    {
        if (!type.isInstance(oldComponent))
            return oldComponent;

        if (filter.test((T) oldComponent))
            return updater.apply((T) oldComponent);

        return oldComponent;
    }
}
