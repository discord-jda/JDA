/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.events.channel.voice.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.VoiceChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}'s parent changed.
 *
 * <p>Can be used to get the affected voice channel, guild and old parent.
 *
 * <p>Identifier: {@code parent}
 */
public class VoiceChannelUpdateParentEvent extends GenericVoiceChannelUpdateEvent<Category>
{
    public static final String IDENTIFIER = "parent";

    public VoiceChannelUpdateParentEvent(@Nonnull JDA api, long responseNumber, @Nonnull VoiceChannel channel, @Nullable Category oldParent)
    {
        super(api, responseNumber, channel, oldParent, channel.getParent(), IDENTIFIER);
    }

    /**
     * The old parent {@link net.dv8tion.jda.api.entities.Category Category}
     *
     * @return The old parent, or null
     */
    @Nullable
    public Category getOldParent()
    {
        return getOldValue();
    }

    /**
     * The new parent {@link net.dv8tion.jda.api.entities.Category Category}
     *
     * @return The new parent, or null
     */
    @Nullable
    public Category getNewParent()
    {
        return getNewValue();
    }
}
