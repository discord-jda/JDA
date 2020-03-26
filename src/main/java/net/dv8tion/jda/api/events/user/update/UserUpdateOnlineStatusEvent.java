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

package net.dv8tion.jda.api.events.user.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import javax.annotation.Nonnull;

/**
 * Indicates that the {@link OnlineStatus OnlineStatus} of a {@link net.dv8tion.jda.api.entities.User User} changed.
 * <br>As with any presence updates this happened for a {@link net.dv8tion.jda.api.entities.Member Member} in a Guild!
 * <p>Can be used to retrieve the User who changed their status and their previous status.
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Identifier: {@code status}
 */
public class UserUpdateOnlineStatusEvent extends GenericUserUpdateEvent<OnlineStatus> implements GenericUserPresenceEvent
{
    public static final String IDENTIFIER = "status";

    private final Guild guild;
    private final Member member;

    public UserUpdateOnlineStatusEvent(@Nonnull JDA api, long responseNumber, @Nonnull Member member, @Nonnull OnlineStatus oldOnlineStatus)
    {
        super(api, responseNumber, member.getUser(), oldOnlineStatus, member.getOnlineStatus(), IDENTIFIER);
        this.guild = member.getGuild();
        this.member = member;
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Nonnull
    @Override
    public Member getMember()
    {
        return member;
    }

    /**
     * The old status
     *
     * @return The old status
     */
    @Nonnull
    public OnlineStatus getOldOnlineStatus()
    {
        return getOldValue();
    }

    /**
     * The new status
     *
     * @return The new status
     */
    @Nonnull
    public OnlineStatus getNewOnlineStatus()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    public OnlineStatus getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public OnlineStatus getNewValue() {
        return super.getNewValue();
    }
}
