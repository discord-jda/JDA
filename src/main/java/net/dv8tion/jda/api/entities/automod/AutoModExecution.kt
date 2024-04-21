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

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion
import javax.annotation.Nonnull

/**
 * Event triggered by an [AutoModRule] activation.
 */
interface AutoModExecution {
    @JvmField
    @get:Nonnull
    val guild: Guild?

    /**
     * The [GuildMessageChannelUnion] that this execution occurred in.
     *
     *
     * This might be `null` if the execution occurred by future event types.
     *
     * @return The [GuildMessageChannelUnion]
     */
    @JvmField
    val channel: GuildMessageChannelUnion?

    @JvmField
    @get:Nonnull
    val response: AutoModResponse?

    @JvmField
    @get:Nonnull
    val triggerType: AutoModTriggerType?

    /**
     * The id of the user that triggered this execution.
     *
     * @return The id of the user
     */
    @JvmField
    val userIdLong: Long

    @get:Nonnull
    val userId: String?
        /**
         * The id of the user that triggered this execution.
         *
         * @return The id of the user
         */
        get() = java.lang.Long.toUnsignedString(userIdLong)

    /**
     * The id of the [AutoModRule] which has been triggered.
     *
     * @return The id of the rule
     */
    @JvmField
    val ruleIdLong: Long

    @get:Nonnull
    val ruleId: String?
        /**
         * The id of the [AutoModRule] which has been triggered.
         *
         * @return The id of the rule
         */
        get() = java.lang.Long.toUnsignedString(ruleIdLong)

    /**
     * The id of the [Message][net.dv8tion.jda.api.entities.Message] which triggered the rule.
     *
     * @return The id of the message, or 0 if the message has been blocked
     */
    @JvmField
    val messageIdLong: Long
    val messageId: String?
        /**
         * The id of the [Message][net.dv8tion.jda.api.entities.Message] which triggered the rule.
         *
         * @return The id of the message, or `null` if the message has been blocked
         */
        get() {
            val id = messageIdLong
            return if (id == 0L) null else java.lang.Long.toUnsignedString(messageIdLong)
        }

    /**
     * The id of the alert [Message][net.dv8tion.jda.api.entities.Message] sent to the alert channel.
     *
     * @return The id of the alert message, or 0 if [AutoModResponse.getType] is not [AutoModResponse.Type.SEND_ALERT_MESSAGE]
     */
    @JvmField
    val alertMessageIdLong: Long
    val alertMessageId: String?
        /**
         * The id of the alert [Message][net.dv8tion.jda.api.entities.Message] sent to the alert channel.
         *
         * @return The id of the alert message, or `null` if [AutoModResponse.getType] is not [AutoModResponse.Type.SEND_ALERT_MESSAGE]
         */
        get() {
            val id = alertMessageIdLong
            return if (id == 0L) null else java.lang.Long.toUnsignedString(alertMessageIdLong)
        }

    @JvmField
    @get:Nonnull
    val content: String?

    /**
     * The substring match of the user content that triggered this rule.
     *
     *
     * This is empty if [GatewayIntent.MESSAGE_CONTENT] is not enabled.
     * However, you can still use [.getMatchedKeyword] regardless.
     *
     * @return The user content substring
     */
    @JvmField
    val matchedContent: String?

    /**
     * The keyword that was found in the [.getContent].
     *
     * @return The keyword that was found in the content
     */
    @JvmField
    val matchedKeyword: String?
}
