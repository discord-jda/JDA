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
package net.dv8tion.jda.api.entities.automod

import net.dv8tion.jda.annotations.Incubating
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.internal.entities.automod.AutoModResponseImpl
import net.dv8tion.jda.internal.utils.Checks
import java.time.Duration
import java.util.*
import javax.annotation.Nonnull

/**
 * An automated response to an [AutoModRule].
 */
interface AutoModResponse : SerializableData {
    @JvmField
    @get:Nonnull
    val type: Type?

    /**
     * The channel to send the alert message to.
     *
     * @return The channel to send the alert message to, or null if this is not a [Type.SEND_ALERT_MESSAGE] response
     */
    val channel: GuildMessageChannel?

    /**
     * The custom message to send to the user.
     *
     * @return The custom message to send to the user, or null if this is not a [Type.BLOCK_MESSAGE] response
     */
    val customMessage: String?

    /**
     * The duration to timeout the user for.
     *
     * @return The duration to timeout the user for, or null if this is not a [Type.TIMEOUT] response
     */
    val timeoutDuration: Duration?

    /**
     * The type of response.
     */
    enum class Type {
        /**
         * Blocks the message from being sent.
         */
        BLOCK_MESSAGE(
            1,
            EnumSet.of(
                AutoModTriggerType.KEYWORD,
                AutoModTriggerType.KEYWORD_PRESET,
                AutoModTriggerType.SPAM,
                AutoModTriggerType.MENTION_SPAM
            )
        ),

        /**
         * Sends an alert message to the specified channel.
         */
        SEND_ALERT_MESSAGE(
            2,
            EnumSet.of(
                AutoModTriggerType.KEYWORD,
                AutoModTriggerType.KEYWORD_PRESET,
                AutoModTriggerType.SPAM,
                AutoModTriggerType.MENTION_SPAM
            )
        ),

        /**
         * Times out the user for the specified duration.
         *
         *
         * To create a rule with this response, the creator must also have the [MODERATE_MEMBERS][net.dv8tion.jda.api.Permission.MODERATE_MEMBERS] permission.
         */
        TIMEOUT(3, EnumSet.of(AutoModTriggerType.KEYWORD, AutoModTriggerType.MENTION_SPAM)),

        /**
         * Blocks the member from interacting with the guild until they update the offending content.
         *
         * @incubating This has not been officially released yet
         */
        @Incubating
        BLOCK_MEMBER_INTERACTION(4, EnumSet.of(AutoModTriggerType.MEMBER_PROFILE_KEYWORD)),

        /**
         * Placeholder for unknown types.
         */
        UNKNOWN(-1, EnumSet.noneOf(AutoModTriggerType::class.java));

        /**
         * The raw value used by Discord to represent this type.
         *
         * @return The raw value
         */
        @JvmField
        val key: Int
        private val supportedTypes: EnumSet<AutoModTriggerType>

        constructor(key: Int) {
            this.key = key
            supportedTypes = EnumSet.complementOf(EnumSet.of(AutoModTriggerType.UNKNOWN))
        }

        constructor(key: Int, supportedTypes: EnumSet<AutoModTriggerType>) {
            this.key = key
            this.supportedTypes = supportedTypes
        }

        /**
         * The [AutoModTriggerTypes][AutoModTriggerType] that this response supports.
         *
         * @return The supported trigger types
         */
        @Nonnull
        fun getSupportedTypes(): EnumSet<AutoModTriggerType> {
            return EnumSet.copyOf(supportedTypes)
        }

        /**
         * Whether this response supports the provided trigger type.
         *
         * @param  type
         * The trigger type
         *
         * @throws IllegalArgumentException
         * If the provided trigger type is `null`
         *
         * @return True, if this response supports the provided trigger type
         */
        fun isSupportedTrigger(@Nonnull type: AutoModTriggerType): Boolean {
            Checks.notNull(type, "AutoModTriggerType")
            return supportedTypes.contains(type)
        }

        companion object {
            /**
             * The [Type] represented by the provided key.
             *
             * @param  key
             * The raw key
             *
             * @return The [Type] or [.UNKNOWN]
             */
            @JvmStatic
            @Nonnull
            fun fromKey(key: Int): Type {
                for (type in entries) {
                    if (type.key == key) return type
                }
                return UNKNOWN
            }
        }
    }

    companion object {
        /**
         * Create a response that will block the message.
         *
         * You can optionally pass a custom message to send to the user.
         *
         * @return The response instance
         *
         * @see .blockMessage
         */
        @Nonnull
        fun blockMessage(): AutoModResponse? {
            return blockMessage(null)
        }

        /**
         * Create a response that will block the message.
         *
         * @param  customMessage
         * The custom message to send to the user, or null to send the default message
         *
         * @throws IllegalArgumentException
         * If the provided custom message is longer than {@value #MAX_CUSTOM_MESSAGE_LENGTH} characters
         *
         * @return The response instance
         */
        @Nonnull
        fun blockMessage(customMessage: String?): AutoModResponse? {
            return AutoModResponseImpl(Type.BLOCK_MESSAGE, customMessage)
        }

        /**
         * Create a response that will send an alert message to the specified channel.
         *
         * @param  channel
         * The channel to send the alert message to
         *
         * @throws IllegalArgumentException
         * If the provided channel is `null`
         *
         * @return The response instance
         */
        @Nonnull
        fun sendAlert(@Nonnull channel: GuildMessageChannel?): AutoModResponse? {
            Checks.notNull(channel, "Channel")
            return AutoModResponseImpl(Type.SEND_ALERT_MESSAGE, channel)
        }

        /**
         * Create a response that will timeout the user for the specified duration.
         *
         *
         * To create a rule with this response, the creator must also have the [MODERATE_MEMBERS][net.dv8tion.jda.api.Permission.MODERATE_MEMBERS] permission.
         *
         * @param  duration
         * The duration to timeout the user for
         *
         * @throws IllegalArgumentException
         * If the provided duration is not positive or longer than {@value net.dv8tion.jda.api.entities.Member#MAX_TIME_OUT_LENGTH} days
         *
         * @return The response instance
         */
        @Nonnull
        fun timeoutMember(@Nonnull duration: Duration): AutoModResponse? {
            Checks.notNull(duration, "Duration")
            Checks.check(!duration.isNegative && !duration.isZero, "Duration must be positive")
            return AutoModResponseImpl(Type.TIMEOUT, duration)
        }

        /**
         * Create a response that will prevent the member from interacting with anything in the guild until the offending content is removed.
         *
         * @return The response instance
         *
         * @incubating This has not been officially released yet
         */
        @Nonnull
        @Incubating
        fun blockMemberInteraction(): AutoModResponse? {
            return AutoModResponseImpl(Type.BLOCK_MEMBER_INTERACTION)
        }

        /**
         * The maximum length of a custom message. ({@value})
         */
        const val MAX_CUSTOM_MESSAGE_LENGTH = 150
    }
}
