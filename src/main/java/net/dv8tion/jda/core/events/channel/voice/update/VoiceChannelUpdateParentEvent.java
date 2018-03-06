/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.events.channel.voice.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.VoiceChannel;

/**
 * Indicates that a {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}'s parent changed.
 *
 * <p>Can be used to get the affected voice channel, guild and old parent.
 *
 * <p>Identifier: {@code parent}
 */
public class VoiceChannelUpdateParentEvent extends GenericVoiceChannelUpdateEvent<Category>
{
    public static final String IDENTIFIER = "parent";

    private final Category oldParent;
    private final Category newParent;

    public VoiceChannelUpdateParentEvent(JDA api, long responseNumber, VoiceChannel channel, Category oldParent)
    {
        super(api, responseNumber, channel);
        this.oldParent = oldParent;
        this.newParent = channel.getParent();
    }

    /**
     * The old parent {@link net.dv8tion.jda.core.entities.Category Category}
     *
     * @return The old parent, or null
     */
    public Category getOldParent()
    {
        return oldParent;
    }

    /**
     * The new parent {@link net.dv8tion.jda.core.entities.Category Category}
     *
     * @return The new parent, or null
     */
    public Category getNewParent()
    {
        return newParent;
    }

    @Override
    public String getPropertyIdentifier()
    {
        return IDENTIFIER;
    }

    @Override
    public Category getOldValue()
    {
        return oldParent;
    }

    @Override
    public Category getNewValue()
    {
        return newParent;
    }
}
