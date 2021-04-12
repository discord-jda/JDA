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

package net.dv8tion.jda.api.events.guild.voice;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

public class GuildVoiceRequestToSpeakEvent extends GenericGuildVoiceEvent
{
    private final OffsetDateTime oldTime, newTime;

    public GuildVoiceRequestToSpeakEvent(@Nonnull JDA api, long responseNumber, @Nonnull Member member,
                                         @Nullable OffsetDateTime oldTime, @Nullable OffsetDateTime newTime)
    {
        super(api, responseNumber, member);
        this.oldTime = oldTime;
        this.newTime = newTime;
    }

    @Nullable
    public OffsetDateTime getOldTime()
    {
        return oldTime;
    }

    @Nullable
    public OffsetDateTime getNewTime()
    {
        return newTime;
    }

    @Nonnull
    @CheckReturnValue
    public RestAction<Void> approve()
    {
        return getVoiceState().approveSpeaker();
    }

    @Nonnull
    @CheckReturnValue
    public RestAction<Void> decline()
    {
        return getVoiceState().declineSpeaker();
    }
}
