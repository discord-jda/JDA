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

package net.dv8tion.jda.api.events.annotations;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.lang.annotation.*;

/**
 * Annotates this event as requiring a cached member to fire,
 * or a method requiring a cached member to return appropriate results.
 *
 * <p>For a member/user to be cached, {@link GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} needs to be enabled,
 * and the {@link MemberCachePolicy} configured to allow some, if not all, members to be cached.
 * <br>Assuming the cache policy allows a member to be cached, the member will be loaded in the cache when either:
 * <ul>
 *     <li>JDA loads it on startup, if a {@link ChunkingFilter} is configured</li>
 *     <li>It is loaded explicitly, for example, using {@link Guild#retrieveMemberById(long)}</li>
 *     <li>An event containing a member is received, such as {@link SlashCommandInteractionEvent}</li>
 * </ul>
 *
 * @see MemberCachePolicy
 * @see ChunkingFilter
 * @see JDABuilder#setMemberCachePolicy(MemberCachePolicy)
 * @see JDABuilder#setChunkingFilter(ChunkingFilter)
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresCachedMember
{
}
