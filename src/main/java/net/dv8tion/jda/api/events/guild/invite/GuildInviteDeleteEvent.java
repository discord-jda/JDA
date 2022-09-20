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

package net.dv8tion.jda.api.events.guild.invite;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import javax.annotation.Nonnull;

/**
 * Indicates than an {@link Invite} was deleted from a {@link Guild}.
 *
 * <p>Can be used to track invite deletion for moderation purposes.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_INVITES GUILD_INVITES} intent to be enabled.
 * <br>This event will only fire for invites deleted in channels where you can {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL}.
 */
public class GuildInviteDeleteEvent extends GenericGuildInviteEvent
{
    public GuildInviteDeleteEvent(@Nonnull JDA api, long responseNumber, @Nonnull String code, @Nonnull GuildChannel channel)
    {
        super(api, responseNumber, code, channel);
    }
}
