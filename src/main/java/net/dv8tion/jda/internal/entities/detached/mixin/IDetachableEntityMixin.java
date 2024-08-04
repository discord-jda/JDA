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

package net.dv8tion.jda.internal.entities.detached.mixin;

import net.dv8tion.jda.api.entities.detached.IDetachableEntity;
import net.dv8tion.jda.api.exceptions.DetachedEntityException;

import javax.annotation.Nonnull;

public interface IDetachableEntityMixin extends IDetachableEntity
{
    default void checkAttached()
    {
        if (isDetached())
            throw detachedException();
    }

    @Nonnull
    default DetachedEntityException detachedException()
    {
        return new DetachedEntityException();
    }

    @Nonnull
    default DetachedEntityException detachedRequiresChannelException()
    {
        return new DetachedEntityException("Getting/checking permissions requires a GuildChannel");
    }
}
