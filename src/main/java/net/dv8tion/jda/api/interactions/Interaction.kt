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
package net.dv8tion.jda.api.interactions

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.interactions.components.Component.Type.Companion.fromKey
import net.dv8tion.jda.internal.utils.ChannelUtil
import javax.annotation.Nonnull

/**
 * Abstract representation for any kind of Discord interaction.
 * <br></br>This includes things such as [Slash-Commands][net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction],
 * [Buttons][net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction] or [Modals][ModalInteraction].
 *
 *
 * To properly handle an interaction you must acknowledge it.
 * Each interaction has different callbacks which acknowledge the interaction. These are added by the individual `I...Callback` interfaces:
 *
 *  * [IReplyCallback]
 * <br></br>Which supports direct message replies and deferred message replies via [IReplyCallback.reply] and [IReplyCallback.deferReply]
 *  * [IMessageEditCallback]
 * <br></br>Which supports direct message edits and deferred message edits (or no-operation) via [IMessageEditCallback.editMessage] and [IMessageEditCallback.deferEdit]
 *  * [IAutoCompleteCallback]
 * <br></br>Which supports choice suggestions for auto-complete interactions via [IAutoCompleteCallback.replyChoices]
 *  * [IModalCallback]
 * <br></br>Which supports replying using a [Modal] via [IModalCallback.replyModal]
 *  * [IPremiumRequiredReplyCallback]
 * <br></br>Which will reply stating that an [Entitlement] is required
 *
 *
 *
 * Once the interaction is acknowledged, you can not reply with these methods again. If the interaction is a [deferrable][IDeferrableCallback],
 * you can use [IDeferrableCallback.getHook] to send additional messages or update the original reply.
 * When using [IReplyCallback.deferReply] the first message sent to the [InteractionHook] will be identical to using [InteractionHook.editOriginal].
 * You must decide whether your reply will be ephemeral or not before calling [IReplyCallback.deferReply]. So design your code flow with that in mind!
 *
 *
 * **You can only acknowledge an interaction once!** Any additional calls to reply/deferReply will result in exceptions.
 * You can use [.isAcknowledged] to check whether the interaction has been acknowledged already.
 */
interface Interaction : ISnowflake {
    /**
     * The raw interaction type.
     * <br></br>It is recommended to use [.getType] instead.
     *
     * @return The raw interaction type
     */
    val typeRaw: Int

    @get:Nonnull
    val type: InteractionType?
        /**
         * The [InteractionType] for this interaction.
         *
         * @return The [InteractionType] or [InteractionType.UNKNOWN]
         */
        get() = InteractionType.Companion.fromKey(typeRaw)

    @get:Nonnull
    val token: String?

    /**
     * The [Guild] this interaction happened in.
     * <br></br>This is null in direct messages.
     *
     * @return The [Guild] or null
     */
    val guild: Guild?
    val isFromGuild: Boolean
        /**
         * Whether this interaction came from a [Guild].
         * <br></br>This is identical to `getGuild() != null`
         *
         * @return True, if this interaction happened in a guild
         */
        get() = guild != null

    @get:Nonnull
    val channelType: ChannelType?
        /**
         * The [ChannelType] for the channel this interaction came from.
         * <br></br>If [.getChannel] is null, this returns [ChannelType.UNKNOWN].
         *
         * @return The [ChannelType]
         */
        get() {
            val channel = channel
            return if (channel != null) channel.type else ChannelType.UNKNOWN
        }

    @get:Nonnull
    val user: User?

    /**
     * The [Member] who caused this interaction.
     * <br></br>This is null if the interaction is not from a guild.
     *
     * @return The [Member]
     */
    val member: Member?

    /**
     * Whether this interaction has already been acknowledged.
     * <br></br>**Each interaction can only be acknowledged once.**
     *
     * @return True, if this interaction has already been acknowledged
     */
    val isAcknowledged: Boolean

    /**
     * The channel this interaction happened in.
     *
     * @return The channel or null if the channel is not provided
     */
    val channel: Channel?

    /**
     * The ID of the channel this interaction happened in.
     * <br></br>This might be 0 if no channel context is provided in future interaction types.
     *
     * @return The channel ID, or 0 if no channel context is provided
     */
    val channelIdLong: Long
    val channelId: String?
        /**
         * The ID of the channel this interaction happened in.
         * <br></br>This might be null if no channel context is provided in future interaction types.
         *
         * @return The channel ID, or null if no channel context is provided
         */
        get() {
            val id = channelIdLong
            return if (id != 0L) java.lang.Long.toUnsignedString(channelIdLong) else null
        }

    @get:Nonnull
    val guildChannel: GuildChannel?
        /**
         * The [net.dv8tion.jda.api.entities.channel.middleman.GuildChannel] this interaction happened in.
         * <br></br>If [.getChannelType] is not a guild type, this throws [IllegalStateException]!
         *
         * @throws IllegalStateException
         * If [.getChannel] is not a guild channel
         *
         * @return The [net.dv8tion.jda.api.entities.channel.middleman.GuildChannel]
         */
        get() = ChannelUtil.safeChannelCast(channel, GuildChannel::class.java)

    @get:Nonnull
    val messageChannel: MessageChannel?
        /**
         * The [net.dv8tion.jda.api.entities.channel.middleman.MessageChannel] this interaction happened in.
         * <br></br>If [.getChannelType] is not a message channel type, this throws [IllegalStateException]!
         *
         * @throws IllegalStateException
         * If [.getChannel] is not a message channel
         *
         * @return The [net.dv8tion.jda.api.entities.channel.middleman.MessageChannel]
         */
        get() = ChannelUtil.safeChannelCast(channel, MessageChannel::class.java)

    @get:Nonnull
    val userLocale: DiscordLocale?

    @get:Nonnull
    val guildLocale: DiscordLocale?
        /**
         * Returns the preferred language of the Guild.
         * <br></br>This is identical to `getGuild().getLocale()`.
         *
         * @throws IllegalStateException
         * If this interaction is not from a guild. (See [.isFromGuild])
         *
         * @return The preferred language of the Guild
         */
        get() {
            check(isFromGuild) { "This interaction did not happen in a guild" }
            return guild!!.locale
        }

    @get:Nonnull
    val entitlements: List<Entitlement?>?

    @get:Nonnull
    val jDA: JDA?
}
