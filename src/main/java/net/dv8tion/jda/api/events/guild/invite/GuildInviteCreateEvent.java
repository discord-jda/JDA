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

package net.dv8tion.jda.api.events.guild.invite;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Invite;

import javax.annotation.Nonnull;

/**
 * Indicates that an {@link Invite Invite} was created in a {@link net.dv8tion.jda.api.entities.Invite.Guild Guild}.
 *
 * <p>Can be used to track invites for moderation purposes.
 */
public class GuildInviteCreateEvent extends GenericGuildInviteEvent
{
    private final Invite invite;

    public GuildInviteCreateEvent(@Nonnull JDA api, long responseNumber, @Nonnull Invite invite, @Nonnull GuildChannel channel)
    {
        super(api, responseNumber, invite.getCode(), channel);
        this.invite = invite;
    }

    /**
     * The invite which was created.
     *
     * @return {@link Invite}
     */
    @Nonnull
    public Invite getInvite()
    {
        return invite;
    }
}
