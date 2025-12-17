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

package net.dv8tion.jda.internal.generated;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.dv8tion.jda.internal.utils.EntityString;

import java.util.Objects;

import javax.annotation.Nonnull;

@JsonSerialize(using = MaybeNullSerializer.class)
@JsonDeserialize(using = MaybeNullDeserializer.class)
public class MaybeNull<T> {
    private static final MaybeNull<?> empty = new MaybeNull<>(null);

    private final T value;

    public MaybeNull(T value) {
        this.value = value;
    }

    public T value() {
        return this.value;
    }

    public boolean isPresent() {
        return this.value != null;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T> MaybeNull<T> empty() {
        return (MaybeNull<T>) empty;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MaybeNull)) {
            return false;
        }
        MaybeNull<?> other = (MaybeNull<?>) obj;
        return Objects.equals(this.value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }

    @Override
    public String toString() {
        return new EntityString(this).addMetadata("value", value).toString();
    }
}
