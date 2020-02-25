/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface IDelete
{
    /**
     * Deletes this entity and provides {@code true} if the delete returned a successful response.
     * <br>If the response was a {@code 404: Not Found} this will provide {@code false},
     * indicating that the delete was not successful but the entity has already been deleted.
     *
     * @return {@link RestAction} - Type: {@code boolean}
     *         The response will be true if the delete actually deleted the entity, and false if it was already deleted
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Boolean> delete();
}
