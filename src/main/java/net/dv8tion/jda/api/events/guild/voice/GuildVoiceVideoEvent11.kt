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
package net.dv8tion.jda.api.events.guild.voice

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import javax.annotation.Nonnull

/**
 * Indicates that a [Member][net.dv8tion.jda.api.entities.Member] turned on their camera.
 *
 *
 * Can be used to detect when a user starts/stops sending video.
 * <br></br>This does not include streams! Use [GuildVoiceStreamEvent][net.dv8tion.jda.api.events.guild.voice.GuildVoiceStreamEvent] for that!
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [VOICE_STATE][net.dv8tion.jda.api.utils.cache.CacheFlag.VOICE_STATE] CacheFlag to be enabled, which requires
 * the [GUILD_VOICE_STATES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES] intent.
 *
 * <br></br>[createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disables that CacheFlag by default!
 *
 *
 * Additionally, this event requires the [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
class GuildVoiceVideoEvent(
    @Nonnull api: JDA, responseNumber: Long, @Nonnull member: Member,
    /**
     * True if this user started sending video. False if the user stopped sending video.
     *
     * @return True, if the user started sending video
     */
    val isSendingVideo: Boolean
) : GenericGuildVoiceEvent(api, responseNumber, member)
