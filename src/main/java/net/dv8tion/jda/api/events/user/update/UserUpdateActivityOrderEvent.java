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

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.internal.JDAImpl;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Indicates that the {@link net.dv8tion.jda.api.entities.Activity Activity} order of a {@link net.dv8tion.jda.api.entities.User User} changes.
 * <br>As with any presence updates this happened for a {@link net.dv8tion.jda.api.entities.Member Member} in a Guild!
 * <p>Can be used to retrieve the User who changed their Activities and their previous Activities.
 *
 * <p>Identifier: {@code activity_order}
 *
 * <h2>Requirements</h2>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Additionally, this event also requires the {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
public class UserUpdateActivityOrderEvent extends GenericUserUpdateEvent<List<Activity>> implements GenericUserPresenceEvent
{
    public static final String IDENTIFIER = "activity_order";

    private final Member member;

    public UserUpdateActivityOrderEvent(@Nonnull JDAImpl api, long responseNumber, @Nonnull List<Activity> previous, @Nonnull Member member)
    {
        super(api, responseNumber, member.getUser(), previous, member.getActivities(), IDENTIFIER);
        this.member = member;
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return member.getGuild();
    }

    @Nonnull
    @Override
    public Member getMember()
    {
        return member;
    }

    @Nonnull
    @Override
    public List<Activity> getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public List<Activity> getNewValue()
    {
        return super.getNewValue();
    }
}
