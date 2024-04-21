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
package net.dv8tion.jda.api.entities.channel.attribute

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import javax.annotation.Nonnull

/**
 * Represents a [GuildChannel] that is capable of containing members.
 *
 *
 * Implementations interpret this meaning as best applies to them:
 *
 *
 * For example,
 *
 *  * [TextChannels][TextChannel] implement this as the [members][net.dv8tion.jda.api.entities.Member] that have [net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
 *  * [VoiceChannels][VoiceChannel] implement this as what [members][net.dv8tion.jda.api.entities.Member] are currently connected to the channel.
 *
 *
 *
 * @see IMemberContainer.getMembers
 */
interface IMemberContainer : GuildChannel {
    @JvmField
    @get:Nonnull
    val members: List<Member?>
}
