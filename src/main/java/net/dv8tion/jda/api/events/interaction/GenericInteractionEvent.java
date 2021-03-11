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

package net.dv8tion.jda.api.events.interaction;

import net.dv8tion.jda.annotations.Incubating;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

@Incubating
public class GenericInteractionEvent extends Event
{
    private final String token;
    private final long interactionId;
    private final Guild guild;
    private final Member member;
    private final User user;
    private final MessageChannel channel;

    public GenericInteractionEvent(@NotNull JDA api, long responseNumber, @Nonnull String token, long interactionId, @Nullable Guild guild, @Nullable Member member, @Nullable User user, @Nonnull MessageChannel channel)
    {
        super(api, responseNumber);
        this.token = token;
        this.interactionId = interactionId;
        this.guild = guild;
        this.member = member;
        this.user = user;
        this.channel = channel;
    }

    public String getInteractionToken()
    {
        return token;
    }

    public long getInteractionIdLong()
    {
        return interactionId;
    }

    public String getInteractionId()
    {
        return Long.toUnsignedString(interactionId);
    }

    @Nullable
    public Guild getGuild()
    {
        return guild;
    }

    @Nullable
    public Member getMember()
    {
        return member;
    }

    @Nonnull
    public User getUser()
    {
        return user;
    }

    @Nonnull
    public MessageChannel getChannel()
    {
        return channel;
    }
}
