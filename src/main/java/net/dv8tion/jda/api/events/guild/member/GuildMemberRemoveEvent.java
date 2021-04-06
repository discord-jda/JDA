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

package net.dv8tion.jda.api.events.guild.member;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a user was removed from a {@link Guild}. This includes kicks, bans, and leaves respectively.
 * <br>This can be fired for uncached members and cached members alike.
 * If the member was not cached by JDA, due to the {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
 * or disabled member chunking, then {@link #getMember()} will return {@code null}.
 *
 * <p>Can be used to detect when a member is removed from a guild, either by leaving or being kicked/banned.
 *
 * <h2>Requirements</h2>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 */
public class GuildMemberRemoveEvent extends GenericGuildEvent
{
    private final User user;
    private final Member member;

    public GuildMemberRemoveEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nonnull User user, @Nullable Member member)
    {
        super(api, responseNumber, guild);
        this.user = user;
        this.member = member;
    }

    /**
     * The corresponding user who was removed from the guild.
     *
     * @return The user who was removed
     */
    @Nonnull
    public User getUser()
    {
        return user;
    }

    /**
     * The member instance for this user, if it was cached at the time.
     * <br>Discord does not provide the member meta-data when a remove event is dispatched.
     *
     * @return Possibly-null member
     */
    @Nullable
    public Member getMember()
    {
        return member;
    }
}
