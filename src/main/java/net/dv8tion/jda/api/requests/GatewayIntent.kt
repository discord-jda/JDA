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
package net.dv8tion.jda.api.requests

import net.dv8tion.jda.annotations.DeprecatedSince
import net.dv8tion.jda.annotations.ForRemoval
import net.dv8tion.jda.annotations.ReplaceWith
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.automod.AutoModExecutionEvent
import net.dv8tion.jda.api.events.automod.GenericAutoModRuleEvent
import net.dv8tion.jda.api.events.emoji.GenericEmojiEvent
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent
import net.dv8tion.jda.api.events.guild.invite.GenericGuildInviteEvent
import net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.guild.scheduledevent.update.GenericScheduledEventUpdateEvent
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent
import net.dv8tion.jda.api.events.message.GenericMessageEvent
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.events.sticker.GenericGuildStickerEvent
import net.dv8tion.jda.api.events.user.UserTypingEvent
import net.dv8tion.jda.api.events.user.update.GenericUserPresenceEvent
import net.dv8tion.jda.api.events.user.update.GenericUserUpdateEvent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.dv8tion.jda.internal.utils.*
import java.util.*
import javax.annotation.Nonnull

/**
 * Flags which enable or disable specific events from the discord gateway.
 *
 *
 * The way to use these is very simple. Go through each intent in the following list and decide whether your bot
 * will need these events or not.
 *
 *
 *  1. **GUILD_MEMBERS** - This is a **privileged** gateway intent that is used to update user information and join/leaves (including kicks). This is required to cache all members of a guild (including chunking)
 *  1. **GUILD_MODERATION** - This will only track guild moderation events, such as bans, unbans, and audit-logs.
 *  1. **GUILD_EMOJIS** - This will only track custom emoji create/modify/delete. Most bots don't need this since they just use the emoji id anyway.
 *  1. **GUILD_WEBHOOKS** - This will only track guild webhook create/update/delete. Most bots don't need this since related events don't contain any useful information about webhook changes.
 *  1. **GUILD_INVITES** - This will only track invite create/delete. Most bots don't make use of invites since they are added through OAuth2 authorization by administrators.
 *  1. **GUILD_VOICE_STATES** - Required to properly get information of members in voice channels and cache them. <u>You cannot connect to a voice channel without this intent</u>.
 *  1. **GUILD_PRESENCES** - This is a **privileged** gateway intent this is only used to track activity and online-status of a user.
 *  1. **GUILD_MESSAGES** - This is used to receive incoming messages in guilds (servers), most bots will need this for commands.
 *  1. **GUILD_MESSAGE_REACTIONS** - This is used to track reactions on messages in guilds (servers). Can be useful to make a paginated embed or reaction role management.
 *  1. **GUILD_MESSAGE_TYPING** - This is used to track when a user starts typing in guilds (servers). Almost no bot will have a use for this.
 *  1. **DIRECT_MESSAGES** - This is used to receive incoming messages in private channels (DMs). You can still send private messages without this intent.
 *  1. **DIRECT_MESSAGE_REACTIONS** - This is used to track reactions on messages in private channels (DMs).
 *  1. **DIRECT_MESSAGE_TYPING** - This is used to track when a user starts typing in private channels (DMs). Almost no bot will have a use for this.
 *  1. **MESSAGE_CONTENT** - This is a **privileged** gateway intent this is only used to enable access to the user content in messages (also including embeds/attachments/components).
 *  1. **SCHEDULED_EVENTS** - This is used to keep track of scheduled events in guilds.
 *  1. **AUTO_MODERATION_CONFIGURATION** - This is used to keep track of auto-mod rule changes in guilds.
 *  1. **AUTO_MODERATION_EXECUTION** - This is used to receive events related to auto-mod response actions.
 *
 *
 * If an intent is not specifically mentioned to be **privileged**, it is not required to be on the whitelist to use it (and its related events).
 * To get whitelisted you either need to contact discord support (for bots in more than 100 guilds)
 * or enable it in the developer dashboard of your application.
 *
 *
 * You must use [ChunkingFilter.NONE][net.dv8tion.jda.api.utils.ChunkingFilter.NONE] if [.GUILD_MEMBERS] is disabled.
 * To enable chunking the discord api requires the privileged [.GUILD_MEMBERS] intent.
 *
 * @see net.dv8tion.jda.api.JDABuilder.disableIntents
 * @see net.dv8tion.jda.api.JDABuilder.enableIntents
 * @see net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder.disableIntents
 * @see net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder.enableIntents
 */
enum class GatewayIntent(
    /**
     * The offset of the intent flag within a bitmask
     * <br></br>This means `getRawValue() == 1 << getOffset()`
     *
     * @return The offset
     */
    val offset: Int
) {
    //GUILDS(0), we currently don't support to disable this one as its required to get a good base cache
    /**
     * **PRIVILEGED INTENT** Events which inform us about member update/leave/join of a guild.
     * <br></br>This is required to chunk all members of a guild. Without this enabled you have to use [ChunkingFilter.NONE][net.dv8tion.jda.api.utils.ChunkingFilter.NONE]!
     *
     *
     * This will also update user information such as name/avatar.
     */
    GUILD_MEMBERS(1),

    /**
     * Ban events.
     */
    @Deprecated("")
    @ForRemoval
    @DeprecatedSince("5.0.0-beta.4")
    @ReplaceWith("GUILD_MODERATION")
    GUILD_BANS(2),

    /**
     * Moderation events, such as ban/unban/audit-log.
     */
    GUILD_MODERATION(2),

    /**
     * Custom emoji and sticker add/update/delete events.
     */
    GUILD_EMOJIS_AND_STICKERS(3),
    //    /**
    //     * Integration events. (unused)
    //     */
    //    GUILD_INTEGRATIONS(4),
    /**
     * Webhook events.
     */
    GUILD_WEBHOOKS(5),

    /**
     * Invite events.
     */
    GUILD_INVITES(6),

    /**
     * Voice state events. This is used to determine which members are connected to a voice channel.
     */
    GUILD_VOICE_STATES(7),

    /**
     * **PRIVILEGED INTENT** Presence updates. This is used to lazy load members and update user properties such as name/avatar.
     * <br></br>This is a very heavy intent! Presence updates are 99% of traffic the bot will receive. To get user update events you should consider using [.GUILD_MEMBERS] instead.
     *
     *
     * This intent is primarily used to track [Member.getOnlineStatus] and [Member.getActivities].
     */
    GUILD_PRESENCES(8),

    /**
     * Message events from text channels in guilds.
     */
    GUILD_MESSAGES(9),

    /**
     * Message reaction events in guilds.
     */
    GUILD_MESSAGE_REACTIONS(10),

    /**
     * Typing start events in guilds.
     */
    GUILD_MESSAGE_TYPING(11),

    /**
     * Message events in private channels.
     */
    DIRECT_MESSAGES(12),

    /**
     * Message reaction events in private channels.
     */
    DIRECT_MESSAGE_REACTIONS(13),

    /**
     * Typing events in private channels.
     */
    DIRECT_MESSAGE_TYPING(14),

    /**
     * **PRIVILEGED INTENT** Access to message content.
     *
     *
     * This specifically affects messages received through the message history of a channel, or through [Message Events][GenericMessageEvent].
     * The content restriction does not apply if the message **mentions the bot directly** (using @username), sent by the bot itself,
     * or if the message is a direct message from a [PrivateChannel][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel].
     * Affected are all user-generated content fields of a message, such as:
     *
     *  * [Message.getContentRaw], [Message.getContentDisplay], [Message.getContentStripped]
     *  * [Message.getEmbeds]
     *  * [Message.getAttachments]
     *  * [Message.getActionRows], [Message.getButtons]
     *
     *
     * @see [Message Content Privileged Intent FAQ](https://support-dev.discord.com/hc/en-us/articles/4404772028055-Message-Content-Privileged-Intent-FAQ)
     */
    MESSAGE_CONTENT(15),

    /**
     * Scheduled Events events.
     */
    SCHEDULED_EVENTS(16),

    /**
     * Events related to [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule] changes.
     */
    AUTO_MODERATION_CONFIGURATION(20),

    /**
     * Events related to [AutoModResponse][net.dv8tion.jda.api.entities.automod.AutoModResponse] triggers.
     */
    AUTO_MODERATION_EXECUTION(21);

    /**
     * The raw bitmask value for this intent
     *
     * @return The raw bitmask value
     */
    @JvmField
    val rawValue: Int

    init {
        rawValue = 1 shl offset
    }

    companion object {
        /**
         * Bitmask with all intents enabled.
         *
         *
         * To use all intents in your own code you should use `EnumSet.allOf(GatewayIntent.class)` instead.
         * This value only represents the raw bitmask used in JDA.
         * <br></br>You can use `EnumSet.noneOf(GatewayIntent.class)` to achieve the opposite.
         */
        @JvmField
        val ALL_INTENTS = 1 or getRaw(EnumSet.allOf(GatewayIntent::class.java))

        /**
         * All intents with some disabled:
         *
         *
         *  * GUILD_MEMBERS (because its privileged)
         *  * GUILD_PRESENCES (because its privileged)
         *  * MESSAGE_CONTENT (because its privileged)
         *  * GUILD_WEBHOOKS because its not useful for most bots
         *  * GUILD_MESSAGE_TYPING because its not useful for most bots
         *  * DIRECT_MESSAGE_TYPING because its not useful for most bots
         *
         *
         * To use these intents you have to pass no other intents to [createLight(token)][net.dv8tion.jda.api.JDABuilder.createLight]
         * or [createDefault(token)][net.dv8tion.jda.api.JDABuilder.createDefault].
         * You can further configure intents by using [enableIntents(intents)][net.dv8tion.jda.api.JDABuilder.enableIntents]
         * and [disableIntents(intents)][net.dv8tion.jda.api.JDABuilder.disableIntents].
         */
        @JvmField
        val DEFAULT = ALL_INTENTS and getRaw(
            GUILD_MEMBERS,
            GUILD_PRESENCES,
            MESSAGE_CONTENT,
            GUILD_WEBHOOKS,
            GUILD_MESSAGE_TYPING,
            DIRECT_MESSAGE_TYPING
        ).inv()

        /**
         * Converts a bitmask into an [EnumSet] of enum values.
         *
         * @param  raw
         * The raw bitmask
         *
         * @return [EnumSet] of intents
         */
        @JvmStatic
        @Nonnull
        fun getIntents(raw: Int): EnumSet<GatewayIntent> {
            val set = EnumSet.noneOf(GatewayIntent::class.java)
            for (intent in entries) {
                if (intent.rawValue and raw != 0) set.add(intent)
            }
            return set
        }

        /**
         * Converts the given intents to a bitmask
         *
         * @param  set
         * The [Collection] of intents
         *
         * @throws IllegalArgumentException
         * If null is provided
         *
         * @return The bitmask for this set of intents
         */
        fun getRaw(@Nonnull set: Collection<GatewayIntent>): Int {
            var raw = 0
            for (intent in set) raw = raw or intent.rawValue
            return raw
        }

        /**
         * Converts the given intents to a bitmask
         *
         * @param  intent
         * The first intent
         * @param  set
         * The remaining intents
         *
         * @throws IllegalArgumentException
         * If null is provided
         *
         * @return The bitmask for this set of intents
         */
        @JvmStatic
        fun getRaw(@Nonnull intent: GatewayIntent, @Nonnull vararg set: GatewayIntent?): Int {
            Checks.notNull(intent, "Intent")
            Checks.notNull(set, "Intent")
            return getRaw(EnumSet.of(intent, *set))
        }

        /**
         * Parse the required GatewayIntents from the provided [CacheFlags][CacheFlag].
         * <br></br>This creates an [EnumSet] based on [CacheFlag.getRequiredIntent].
         *
         * @param  flag
         * The first cache flag
         * @param  other
         * Any additional cache flags
         *
         * @throws IllegalArgumentException
         * If provided with null
         *
         * @return [EnumSet] for the required intents
         */
        @Nonnull
        fun fromCacheFlags(@Nonnull flag: CacheFlag, @Nonnull vararg other: CacheFlag?): EnumSet<GatewayIntent> {
            Checks.notNull(flag, "CacheFlag")
            Checks.noneNull(other, "CacheFlag")
            return fromCacheFlags(EnumSet.of(flag, *other))
        }

        /**
         * Parse the required GatewayIntents from the provided [CacheFlags][CacheFlag].
         * <br></br>This creates an [EnumSet] based on [CacheFlag.getRequiredIntent].
         *
         * @param  flags
         * The cache flags
         *
         * @throws IllegalArgumentException
         * If provided with null
         *
         * @return [EnumSet] for the required intents
         */
        @Nonnull
        fun fromCacheFlags(@Nonnull flags: Collection<CacheFlag>): EnumSet<GatewayIntent> {
            val intents = EnumSet.noneOf(GatewayIntent::class.java)
            for (flag in flags) {
                Checks.notNull(flag, "CacheFlag")
                val intent = flag.requiredIntent
                if (intent != null) intents.add(intent)
            }
            return intents
        }

        /**
         * Parse the required GatewayIntents from the provided [Event Types][GenericEvent].
         *
         * @param  events
         * The event types
         *
         * @throws IllegalArgumentException
         * If provided with null
         *
         * @return [EnumSet] for the required intents
         */
        @Nonnull
        @SafeVarargs
        fun fromEvents(@Nonnull vararg events: Class<out GenericEvent?>?): EnumSet<GatewayIntent> {
            Checks.noneNull(events, "Event")
            return fromEvents(Arrays.asList(*events))
        }

        /**
         * Parse the required GatewayIntents from the provided [Event Types][GenericEvent].
         *
         * @param  events
         * The event types
         *
         * @throws IllegalArgumentException
         * If provided with null
         *
         * @return [EnumSet] for the required intents
         */
        @Nonnull
        fun fromEvents(@Nonnull events: Collection<Class<out GenericEvent?>?>): EnumSet<GatewayIntent> {
            val intents = EnumSet.noneOf(GatewayIntent::class.java)
            for (event in events) {
                Checks.notNull(event, "Event")
                if (GenericUserPresenceEvent::class.java.isAssignableFrom(event)) intents.add(GUILD_PRESENCES) else if (GenericUserUpdateEvent::class.java.isAssignableFrom(
                        event
                    ) || GenericGuildMemberEvent::class.java.isAssignableFrom(event) || GuildMemberRemoveEvent::class.java.isAssignableFrom(
                        event
                    )
                ) intents.add(
                    GUILD_MEMBERS
                ) else if (GuildBanEvent::class.java.isAssignableFrom(event) || GuildUnbanEvent::class.java.isAssignableFrom(
                        event
                    ) || GuildAuditLogEntryCreateEvent::class.java.isAssignableFrom(event)
                ) intents.add(
                    GUILD_MODERATION
                ) else if (GenericEmojiEvent::class.java.isAssignableFrom(event) || GenericGuildStickerEvent::class.java.isAssignableFrom(
                        event
                    )
                ) intents.add(
                    GUILD_EMOJIS_AND_STICKERS
                ) else if (GenericScheduledEventUpdateEvent::class.java.isAssignableFrom(event)) intents.add(
                    SCHEDULED_EVENTS
                ) else if (GenericGuildInviteEvent::class.java.isAssignableFrom(event)) intents.add(GUILD_INVITES) else if (GenericGuildVoiceEvent::class.java.isAssignableFrom(
                        event
                    )
                ) intents.add(
                    GUILD_VOICE_STATES
                ) else if (MessageBulkDeleteEvent::class.java.isAssignableFrom(event)) intents.add(GUILD_MESSAGES) else if (GenericMessageReactionEvent::class.java.isAssignableFrom(
                        event
                    )
                ) Collections.addAll(
                    intents,
                    GUILD_MESSAGE_REACTIONS,
                    DIRECT_MESSAGE_REACTIONS
                ) else if (GenericMessageEvent::class.java.isAssignableFrom(event)) Collections.addAll(
                    intents,
                    GUILD_MESSAGES,
                    DIRECT_MESSAGES
                ) else if (UserTypingEvent::class.java.isAssignableFrom(event)) Collections.addAll(
                    intents,
                    GUILD_MESSAGE_TYPING,
                    DIRECT_MESSAGE_TYPING
                ) else if (AutoModExecutionEvent::class.java.isAssignableFrom(event)) intents.add(
                    AUTO_MODERATION_EXECUTION
                ) else if (GenericAutoModRuleEvent::class.java.isAssignableFrom(event)) intents.add(
                    AUTO_MODERATION_CONFIGURATION
                )
            }
            return intents
        }

        /**
         * Parse the required GatewayIntents from the provided [Event Types][GenericEvent] and [CacheFlags][CacheFlag].
         *
         * @param  events
         * The event types
         * @param  flags
         * The cache flags
         *
         * @throws IllegalArgumentException
         * If provided with null
         *
         * @return [EnumSet] for the required intents
         */
        @Nonnull
        fun from(
            @Nonnull events: Collection<Class<out GenericEvent?>?>,
            @Nonnull flags: Collection<CacheFlag>
        ): EnumSet<GatewayIntent> {
            val intents = fromEvents(events)
            intents.addAll(fromCacheFlags(flags))
            return intents
        }
    }
}
