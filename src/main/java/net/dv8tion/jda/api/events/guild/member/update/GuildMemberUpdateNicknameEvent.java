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

package net.dv8tion.jda.api.events.guild.member.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.Member Member} updated their {@link net.dv8tion.jda.api.entities.Guild Guild} nickname.
 *
 * <p>Can be used to retrieve members who change their nickname, triggering guild, the old nick and the new nick.
 *
 * <p>Identifier: {@code nick}
 */
public class GuildMemberUpdateNicknameEvent extends GenericGuildMemberUpdateEvent<String>
{
    public static final String IDENTIFIER = "nick";

    public GuildMemberUpdateNicknameEvent(@Nonnull JDA api, long responseNumber, @Nonnull Member member, @Nullable String oldNick)
    {
        super(api, responseNumber, member, oldNick, member.getNickname(), IDENTIFIER);
    }

    /**
     * The old nickname
     *
     * @return The old nickname
     */
    @Nullable
    public String getOldNickname()
    {
        return getOldValue();
    }

    /**
     * The new nickname
     *
     * @return The new nickname
     */
    @Nullable
    public String getNewNickname()
    {
        return getNewValue();
    }
}
