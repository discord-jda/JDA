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

package net.dv8tion.jda.api.events.stage;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.entities.StageInstance;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.StageInstance StageInstance} was created/deleted/changed.
 * <br>Every StageInstanceEvent is derived from this event and can be casted.
 *
 * <p>Can be used to detect any StageInstanceEvent.
 */
public abstract class GenericStageInstanceEvent extends GenericGuildEvent
{
    protected final StageInstance instance;

    public GenericStageInstanceEvent(@Nonnull JDA api, long responseNumber, @Nonnull StageInstance stageInstance)
    {
        super(api, responseNumber, stageInstance.getGuild());
        this.instance = stageInstance;
    }

    /**
     * The affected {@link StageInstance}
     *
     * @return The {@link StageInstance}
     */
    @Nonnull
    public StageInstance getInstance()
    {
        return instance;
    }

    /**
     * The {@link StageChannel} this instance belongs to
     *
     * @return The StageChannel
     */
    @Nonnull
    public StageChannel getChannel()
    {
        return instance.getChannel();
    }
}
