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

package net.dv8tion.jda.api.events.stage.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.StageInstance;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.events.stage.GenericStageInstanceEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.StageInstance StageInstance} was updated.
 * <br>Every StageInstanceUpdateEvent is derived from this event and can be casted.
 *
 * <p>Can be used to detect any StageInstanceUpdateEvent.
 */
public abstract class GenericStageInstanceUpdateEvent<T> extends GenericStageInstanceEvent implements UpdateEvent<StageInstance, T>
{
    protected final T previous;
    protected final T next;
    protected final String identifier;

    public GenericStageInstanceUpdateEvent(@Nonnull JDA api, long responseNumber, @Nonnull StageInstance stageInstance, T previous, T next, String identifier)
    {
        super(api, responseNumber, stageInstance);
        this.previous = previous;
        this.next = next;
        this.identifier = identifier;
    }

    @Nonnull
    @Override
    public String getPropertyIdentifier()
    {
        return identifier;
    }

    @Nonnull
    @Override
    public StageInstance getEntity()
    {
        return getInstance();
    }

    @Nullable
    @Override
    public T getOldValue()
    {
        return previous;
    }

    @Nullable
    @Override
    public T getNewValue()
    {
        return next;
    }
}
