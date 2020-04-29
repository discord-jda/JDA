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

package net.dv8tion.jda.api.events.guild.voice;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class GenericGuildVoiceUpdateEvent extends GenericGuildVoiceEvent implements GuildVoiceUpdateEvent
{
    protected final VoiceChannel joined, left;

    public GenericGuildVoiceUpdateEvent(
        @Nonnull JDA api, long responseNumber, @Nonnull Member member, @Nullable VoiceChannel left, @Nullable VoiceChannel joined)
    {
        super(api, responseNumber, member);
        this.left = left;
        this.joined = joined;
    }

    @Nullable
    @Override
    public VoiceChannel getChannelLeft()
    {
        return left;
    }

    @Nullable
    @Override
    public VoiceChannel getChannelJoined()
    {
        return joined;
    }

    @Nonnull
    @Override
    public String getPropertyIdentifier()
    {
        return IDENTIFIER;
    }

    @Nonnull
    @Override
    public Member getEntity()
    {
        return getMember();
    }

    @Nullable
    @Override
    public VoiceChannel getOldValue()
    {
        return getChannelLeft();
    }

    @Nullable
    @Override
    public VoiceChannel getNewValue()
    {
        return getChannelJoined();
    }

    @Override
    public String toString()
    {
        return "MemberVoiceUpdate[" + getPropertyIdentifier() + "](" + getOldValue() + "->" + getNewValue() + ')';
    }
}
