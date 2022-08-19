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
package net.dv8tion.jda.api.events.guild.scheduledevent;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildScheduledEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.internal.requests.CompletedRestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.User User} is no longer interested or has unsubscribed from a {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent}.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_SCHEDULED_EVENTS GUILD_SCHEDULED_EVENTS} intent to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * Can be used to detect when someone has indicated that they are no longer interested in an event and also retrieve their
 * {@link net.dv8tion.jda.api.entities.User User} object as well as the {@link GuildScheduledEvent}.
 */
public class GuildScheduledEventUserRemoveEvent extends GenericGuildScheduledEventGatewayEvent
{
    private final long userId;

    public GuildScheduledEventUserRemoveEvent(@Nonnull JDA api, long responseNumber, @Nonnull GuildScheduledEvent guildScheduledEvent, long userId)
    {
        super(api, responseNumber, guildScheduledEvent);
        this.userId = userId;
    }

    /**
     * The id of the user that indicated that they are no longer interested in the event.
     *
     * @return The long user id
     */
    public long getUserIdLong()
    {
        return userId;
    }

    /**
     * The id of the user that indicated that they are no longer interested in the event.
     *
     * @return The string user id
     */
    @Nonnull
    public String getUserId()
    {
        return Long.toUnsignedString(userId);
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.User User} who was removed from the {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent}.
     * <br>This might be missing if the user was not cached.
     * Use {@link #retrieveUser()} to load the user.
     *
     * @return The removed user or null if this information is missing
     */
    @Nullable
    public User getUser()
    {
        return api.getUserById(userId);
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.Member Member} instance for the removed user
     * or {@code null} if the user is not in this guild.
     * <br>This will also be {@code null} if the member is not available in the cache.
     * Use {@link #retrieveMember()} to load the member.
     *
     * @return Member of the removed user or null if they are no longer member of this guild
     */
    @Nullable
    public Member getMember()
    {
        return guild.getMemberById(userId);
    }

    /**
     * Retrieves the {@link User} who was removed from the {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent}.
     * <br>If a user is known, this will return {@link #getUser()}.
     *
     * @return {@link RestAction} - Type: {@link User}
     */
    @Nonnull
    @CheckReturnValue
    public CacheRestAction<User> retrieveUser()
    {
        return getJDA().retrieveUserById(getUserIdLong());
    }

    /**
     * Retrieves the {@link Member} who was removed from the {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent}.
     * <br>If a member is known, this will return {@link #getMember()}.
     *
     * @return {@link RestAction} - Type: {@link Member}
     */
    @Nonnull
    @CheckReturnValue
    public CacheRestAction<Member> retrieveMember()
    {
        return getGuild().retrieveMemberById(getUserIdLong());
    }
}
