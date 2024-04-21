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
package net.dv8tion.jda.api.events.channel

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.unions.ChannelUnion
import net.dv8tion.jda.api.events.Event
import javax.annotation.Nonnull

/**
 * Top-level channel event type
 * <br></br>All channel events JDA fires are derived from this class.
 *
 *
 * Can be used to check if an Object is a JDA event in [EventListener][net.dv8tion.jda.api.hooks.EventListener] implementations to distinguish what event is being fired.
 * <br></br>Adapter implementation: [ListenerAdapter][net.dv8tion.jda.api.hooks.ListenerAdapter]
 */
open class GenericChannelEvent(@Nonnull api: JDA, responseNumber: Long, protected val channel: Channel) :
    Event(api, responseNumber) {
    val isFromGuild: Boolean
        /**
         * Whether this channel event happened in a [Guild][net.dv8tion.jda.api.entities.Guild].
         * <br></br>If this is `false` then [.getGuild] will throw an [java.lang.IllegalStateException].
         *
         * @return True, if [.getChannelType].[isGuild()][ChannelType.isGuild] is true.
         */
        get() = channelType!!.isGuild

    @get:Nonnull
    val channelType: ChannelType?
        /**
         * The [ChannelType] of the channel the event was fired from.
         *
         * @return The [ChannelType] of the channel the event was fired from.
         */
        get() = channel.type

    /**
     * Used to determine if this event was received from a [Channel]
     * of the [ChannelType][net.dv8tion.jda.api.entities.channel.ChannelType] specified.
     *
     *
     * Useful for restricting functionality to a certain type of channels.
     *
     * @param  type
     * The [ChannelType] to check against.
     *
     * @return True if the [ChannelType][net.dv8tion.jda.api.entities.channel.ChannelType] which this message was received
     * from is the same as the one specified by `type`.
     */
    fun isFromType(type: ChannelType): Boolean {
        return channelType == type
    }

    /**
     * The [Channel] the event was fired from.
     *
     * @return The [ChannelType] of the channel the event was fired from.
     */
    @Nonnull
    fun getChannel(): ChannelUnion {
        return channel as ChannelUnion
    }

    @get:Nonnull
    val guild: Guild
        /**
         * The [Guild][net.dv8tion.jda.api.entities.Guild] in which this channel event happened.
         * <br></br>If this channel event was not received in a [GuildChannel][net.dv8tion.jda.api.entities.channel.middleman.GuildChannel],
         * this will throw an [java.lang.IllegalStateException].
         *
         * @throws java.lang.IllegalStateException
         * If this channel event did not happen in a [net.dv8tion.jda.api.entities.channel.middleman.GuildChannel].
         *
         * @return The Guild in which this channel event happened
         *
         * @see .isFromType
         * @see .getChannelType
         */
        get() {
            check(isFromGuild) { "This channel event did not happen in a guild" }
            return (channel as GuildChannel).guild
        }
}
