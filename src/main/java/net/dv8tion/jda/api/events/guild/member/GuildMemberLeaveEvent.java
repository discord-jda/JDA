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

import net.dv8tion.jda.annotations.DeprecatedSince;
import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;

import javax.annotation.Nonnull;

/**
 * Indicates a {@link net.dv8tion.jda.api.entities.Member Member} left a {@link net.dv8tion.jda.api.entities.Guild Guild}.
 *
 * <p>This event is only fired if the member was cached.
 * You can use {@link GuildMemberRemoveEvent} to detect any member removes, regardless of cache state.
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Can be used to retrieve members who leave a guild.
 *
 * @deprecated This was deprecated in favour of {@link GuildMemberRemoveEvent}
 */
@Deprecated
@ForRemoval(deadline = "5.0.0")
@DeprecatedSince("4.2.0")
@ReplaceWith("GuildMemberRemoveEvent")
public class GuildMemberLeaveEvent extends GenericGuildMemberEvent
{
    public GuildMemberLeaveEvent(@Nonnull JDA api, long responseNumber, @Nonnull Member member)
    {
        super(api, responseNumber, member);
    }
}
