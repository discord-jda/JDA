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
package net.dv8tion.jda.api.entities.channel

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.utils.MiscUtil
import java.util.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Abstract Channel interface for all [ChannelTypes][ChannelType].
 */
interface Channel : IMentionable {
    @get:Nonnull
    val flags: EnumSet<ChannelFlag?>?
        /**
         * The flags configured for this channel.
         * <br></br>This feature is currently primarily used for [ForumChannels][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel].
         *
         * @return [EnumSet] of the configured [ChannelFlags][ChannelFlag], changes to this enum set are not reflected in the API.
         */
        get() = EnumSet.noneOf(ChannelFlag::class.java)

    @JvmField
    @get:Nonnull
    val name: String

    @JvmField
    @get:Nonnull
    val type: ChannelType?

    @JvmField
    @get:Nonnull
    val jDA: JDA?

    /**
     * Deletes this Channel.
     *
     *
     * Possible ErrorResponses include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>If this channel was already deleted
     *
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun delete(): RestAction<Void?>?
    @Nonnull
    override fun getAsMention(): String {
        return "<#$id>"
    }

    override fun formatTo(formatter: Formatter, flags: Int, width: Int, precision: Int) {
        val leftJustified = flags and FormattableFlags.LEFT_JUSTIFY == FormattableFlags.LEFT_JUSTIFY
        val upper = flags and FormattableFlags.UPPERCASE == FormattableFlags.UPPERCASE
        val alt = flags and FormattableFlags.ALTERNATE == FormattableFlags.ALTERNATE
        val out: String
        out = if (alt) "#" + (if (upper) name.uppercase(formatter.locale()) else name) else asMention
        MiscUtil.appendTo(formatter, width, precision, leftJustified, out)
    }

    companion object {
        /**
         * The maximum length a channel name can be. ({@value #MAX_NAME_LENGTH})
         */
        const val MAX_NAME_LENGTH = 100
    }
}
