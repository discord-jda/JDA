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

package net.dv8tion.jda.api.events.interaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class GenericChannelInteractionCreateEvent extends GenericInteractionCreateEvent
{
    private final MessageChannel channel;

    public GenericChannelInteractionCreateEvent(@NotNull JDA api, long responseNumber, int type, @Nonnull String token, long interactionId, @Nullable Guild guild, @Nullable Member member, @Nullable User user, @Nonnull MessageChannel channel)
    {
        super(api, responseNumber, type, token, interactionId, guild, member, user);
        this.channel = channel;
    }

    @Nonnull
    public MessageChannel getChannel()
    {
        return channel;
    }

    @Nonnull
    public TextChannel getTextChannel()
    {
        if (!isFromGuild())
            throw new IllegalStateException("This interaction was not used in a TextChannel!");
        return (TextChannel) getChannel();
    }

    @Nonnull
    public PrivateChannel getPrivateChannel()
    {
        if (isFromGuild())
            throw new IllegalStateException("This interaction was not used in a PrivateChannel!");
        return (PrivateChannel) getChannel();
    }

    @Nonnull
    public ChannelType getChannelType()
    {
        return getChannel().getType();
    }

    public boolean isFromGuild()
    {
        return getChannelType().isGuild();
    }
}
