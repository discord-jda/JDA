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
package net.dv8tion.jda.api.events.guild;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.User User} was unbanned from a {@link net.dv8tion.jda.api.entities.Guild Guild}.
 *
 * <p>Can be used to retrieve the user who was unbanned (if available) and the guild which they were unbanned from.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MODERATION GUILD_MODERATION} intent to be enabled.
 */
public class GuildUnbanEvent extends GenericGuildEvent
{
    private final User user;

    public GuildUnbanEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nonnull User user)
    {
        super(api, responseNumber, guild);
        this.user = user;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.User User} who was unbanned
     *
     * @return The unbanned user
     */
    @Nonnull
    public User getUser()
    {
        return user;
    }
}
