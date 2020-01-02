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

package net.dv8tion.jda.api.events.message.guild.react;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.MessageReaction MessageReaction} was added to a Message in a Guild
 *
 * <p>Can be used to detect when a reaction is added in a guild
 */
public class GuildMessageReactionAddEvent extends GenericGuildMessageReactionEvent
{
    public GuildMessageReactionAddEvent(@Nonnull JDA api, long responseNumber, @Nonnull Member member, @Nonnull MessageReaction reaction)
    {
        super(api, responseNumber, member, reaction, member.getIdLong());
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public User getUser()
    {
        return super.getUser();
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public Member getMember()
    {
        return super.getMember();
    }
}
