/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.utils.Checks;

import java.util.function.Consumer;

public interface DisposingState<T extends DisposingState<T>>
{
    boolean isDisposed();

    @SuppressWarnings("unchecked")
    default T ifDisposed(Consumer<T> then)
    {
        Checks.notNull(then, "Consumer");
        T self = (T) this;
        if (isDisposed())
            then.accept(self);
        return self;
    }

    @SuppressWarnings("unchecked")
    default T ifNotDisposed(Consumer<T> then)
    {
        Checks.notNull(then, "Consumer");
        T self = (T) this;
        if (!isDisposed())
            then.accept(self);
        return self;
    }
}
