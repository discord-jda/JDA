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

package net.dv8tion.jda.api.requests;

import net.dv8tion.jda.annotations.DeprecatedSince;
import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.automod.AutoModExecutionEvent;
import net.dv8tion.jda.api.events.automod.GenericAutoModRuleEvent;
import net.dv8tion.jda.api.events.emoji.GenericEmojiEvent;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.invite.GenericGuildInviteEvent;
import net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.GenericScheduledEventUpdateEvent;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.sticker.GenericGuildStickerEvent;
import net.dv8tion.jda.api.events.user.UserTypingEvent;
import net.dv8tion.jda.api.events.user.update.GenericUserPresenceEvent;
import net.dv8tion.jda.api.events.user.update.GenericUserUpdateEvent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

/**
 * Flags which enable or disable specific events from the discord gateway.
 *
 * <p>The way to use these is very simple. Go through each intent in the following list and decide whether your bot
 * will need these events or not.
 *
 * <ol>
 *     <li><b>GUILD_MEMBERS</b> - This is a <b>privileged</b> gateway intent that is used to update user information and join/leaves (including kicks). This is required to cache all members of a guild (including chunking)</li>
 *     <li><b>GUILD_MODERATION</b> - This will only track guild moderation events, such as bans, unbans, and audit-logs.</li>
 *     <li><b>GUILD_EMOJIS</b> - This will only track custom emoji create/modify/delete. Most bots don't need this since they just use the emoji id anyway.</li>
 *     <li><b>GUILD_WEBHOOKS</b> - This will only track guild webhook create/update/delete. Most bots don't need this since related events don't contain any useful information about webhook changes.</li>
 *     <li><b>GUILD_INVITES</b> - This will only track invite create/delete. Most bots don't make use of invites since they are added through OAuth2 authorization by administrators.</li>
 *     <li><b>GUILD_VOICE_STATES</b> - Required to properly get information of members in voice channels and cache them. <u>You cannot connect to a voice channel without this intent</u>.</li>
 *     <li><b>GUILD_PRESENCES</b> - This is a <b>privileged</b> gateway intent this is only used to track activity and online-status of a user.</li>
 *     <li><b>GUILD_MESSAGES</b> - This is used to receive incoming messages in guilds (servers), most bots will need this for commands.</li>
 *     <li><b>GUILD_MESSAGE_REACTIONS</b> - This is used to track reactions on messages in guilds (servers). Can be useful to make a paginated embed or reaction role management.</li>
 *     <li><b>GUILD_MESSAGE_TYPING</b> - This is used to track when a user starts typing in guilds (servers). Almost no bot will have a use for this.</li>
 *     <li><b>DIRECT_MESSAGES</b> - This is used to receive incoming messages in private channels (DMs). You can still send private messages without this intent.</li>
 *     <li><b>DIRECT_MESSAGE_REACTIONS</b> - This is used to track reactions on messages in private channels (DMs).</li>
 *     <li><b>DIRECT_MESSAGE_TYPING</b> - This is used to track when a user starts typing in private channels (DMs). Almost no bot will have a use for this.</li>
 *     <li><b>MESSAGE_CONTENT</b> - This is a <b>privileged</b> gateway intent this is only used to enable access to the user content in messages (also including embeds/attachments/components).</li>
 *     <li><b>SCHEDULED_EVENTS</b> - This is used to keep track of scheduled events in guilds.</li>
 *     <li><b>AUTO_MODERATION_CONFIGURATION</b> - This is used to keep track of auto-mod rule changes in guilds.</li>
 *     <li><b>AUTO_MODERATION_EXECUTION</b> - This is used to receive events related to auto-mod response actions.</li>
 * </ol>
 *
 * If an intent is not specifically mentioned to be <b>privileged</b>, it is not required to be on the whitelist to use it (and its related events).
 * To get whitelisted you either need to contact discord support (for bots in more than 100 guilds)
 * or enable it in the developer dashboard of your application.
 *
 * <p>You must use {@link net.dv8tion.jda.api.utils.ChunkingFilter#NONE ChunkingFilter.NONE} if {@link #GUILD_MEMBERS} is disabled.
 * To enable chunking the discord api requires the privileged {@link #GUILD_MEMBERS} intent.
 *
 * @see net.dv8tion.jda.api.JDABuilder#disableIntents(GatewayIntent, GatewayIntent...)
 * @see net.dv8tion.jda.api.JDABuilder#enableIntents(GatewayIntent, GatewayIntent...)
 * @see net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder#disableIntents(GatewayIntent, GatewayIntent...)
 * @see net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder#enableIntents(GatewayIntent, GatewayIntent...)
 */
public enum GatewayIntent
{
    //GUILDS(0), we currently don't support to disable this one as its required to get a good base cache
    /**
     * <b>PRIVILEGED INTENT</b> Events which inform us about member update/leave/join of a guild.
     * <br>This is required to chunk all members of a guild. Without this enabled you have to use {@link net.dv8tion.jda.api.utils.ChunkingFilter#NONE ChunkingFilter.NONE}!
     *
     * <p>This will also update user information such as name/avatar.
     */
    GUILD_MEMBERS(1),
    /**
     * Ban events.
     */
    @Deprecated
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
     * <b>PRIVILEGED INTENT</b> Presence updates. This is used to lazy load members and update user properties such as name/avatar.
     * <br>This is a very heavy intent! Presence updates are 99% of traffic the bot will receive. To get user update events you should consider using {@link #GUILD_MEMBERS} instead.
     *
     * <p>This intent is primarily used to track {@link Member#getOnlineStatus()} and {@link Member#getActivities()}.
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
     * <b>PRIVILEGED INTENT</b> Access to message content.
     *
     * <p>This specifically affects messages received through the message history of a channel, or through {@link GenericMessageEvent Message Events}.
     * The content restriction does not apply if the message <b>mentions the bot directly</b> (using @username), sent by the bot itself,
     * or if the message is a direct message from a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel}.
     * Affected are all user-generated content fields of a message, such as:
     * <ul>
     *     <li>{@link Message#getContentRaw()}, {@link Message#getContentDisplay()}, {@link Message#getContentStripped()}</li>
     *     <li>{@link Message#getEmbeds()}</li>
     *     <li>{@link Message#getAttachments()}</li>
     *     <li>{@link Message#getActionRows()}, {@link Message#getButtons()}</li>
     * </ul>
     *
     * @see <a href="https://support-dev.discord.com/hc/en-us/articles/4404772028055-Message-Content-Privileged-Intent-FAQ" target="_blank">Message Content Privileged Intent FAQ</a>
     */
    MESSAGE_CONTENT(15),

    /**
     * Scheduled Events events.
     */
    SCHEDULED_EVENTS(16),

    /**
     * Events related to {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule} changes.
     */
    AUTO_MODERATION_CONFIGURATION(20),

    /**
     * Events related to {@link net.dv8tion.jda.api.entities.automod.AutoModResponse AutoModResponse} triggers.
     */
    AUTO_MODERATION_EXECUTION(21),

    ;

    /**
     * Bitmask with all intents enabled.
     *
     * <p>To use all intents in your own code you should use {@code EnumSet.allOf(GatewayIntent.class)} instead.
     * This value only represents the raw bitmask used in JDA.
     * <br>You can use {@code EnumSet.noneOf(GatewayIntent.class)} to achieve the opposite.
     */
    public static final int ALL_INTENTS = 1 | getRaw(EnumSet.allOf(GatewayIntent.class));

    /**
     * All intents with some disabled:
     *
     * <ul>
     *     <li>GUILD_MEMBERS (because its privileged)</li>
     *     <li>GUILD_PRESENCES (because its privileged)</li>
     *     <li>MESSAGE_CONTENT (because its privileged)</li>
     *     <li>GUILD_WEBHOOKS because its not useful for most bots</li>
     *     <li>GUILD_MESSAGE_TYPING because its not useful for most bots</li>
     *     <li>DIRECT_MESSAGE_TYPING because its not useful for most bots</li>
     * </ul>
     *
     * To use these intents you have to pass no other intents to {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(token)}
     * or {@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(token)}.
     * You can further configure intents by using {@link net.dv8tion.jda.api.JDABuilder#enableIntents(GatewayIntent, GatewayIntent...) enableIntents(intents)}
     * and {@link net.dv8tion.jda.api.JDABuilder#disableIntents(GatewayIntent, GatewayIntent...) disableIntents(intents)}.
     */
    public static final int DEFAULT = ALL_INTENTS & ~getRaw(GUILD_MEMBERS, GUILD_PRESENCES, MESSAGE_CONTENT, GUILD_WEBHOOKS, GUILD_MESSAGE_TYPING, DIRECT_MESSAGE_TYPING);

    private final int rawValue;
    private final int offset;

    GatewayIntent(int offset)
    {
        this.offset = offset;
        this.rawValue = 1 << offset;
    }

    /**
     * The raw bitmask value for this intent
     *
     * @return The raw bitmask value
     */
    public int getRawValue()
    {
        return rawValue;
    }

    /**
     * The offset of the intent flag within a bitmask
     * <br>This means {@code getRawValue() == 1 << getOffset()}
     *
     * @return The offset
     */
    public int getOffset()
    {
        return offset;
    }

    /**
     * Converts a bitmask into an {@link EnumSet} of enum values.
     *
     * @param  raw
     *         The raw bitmask
     *
     * @return {@link EnumSet} of intents
     */
    @Nonnull
    public static EnumSet<GatewayIntent> getIntents(int raw)
    {
        EnumSet<GatewayIntent> set = EnumSet.noneOf(GatewayIntent.class);
        for (GatewayIntent intent : values())
        {
            if ((intent.getRawValue() & raw) != 0)
                set.add(intent);
        }
        return set;
    }

    /**
     * Converts the given intents to a bitmask
     *
     * @param  set
     *         The {@link Collection} of intents
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The bitmask for this set of intents
     */
    public static int getRaw(@Nonnull Collection<GatewayIntent> set)
    {
        int raw = 0;
        for (GatewayIntent intent : set)
            raw |= intent.rawValue;
        return raw;
    }

    /**
     * Converts the given intents to a bitmask
     *
     * @param  intent
     *         The first intent
     * @param  set
     *         The remaining intents
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The bitmask for this set of intents
     */
    public static int getRaw(@Nonnull GatewayIntent intent, @Nonnull GatewayIntent... set)
    {
        Checks.notNull(intent, "Intent");
        Checks.notNull(set, "Intent");
        return getRaw(EnumSet.of(intent, set));
    }

    /**
     * Parse the required GatewayIntents from the provided {@link CacheFlag CacheFlags}.
     * <br>This creates an {@link EnumSet} based on {@link CacheFlag#getRequiredIntent()}.
     *
     * @param  flag
     *         The first cache flag
     * @param  other
     *         Any additional cache flags
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link EnumSet} for the required intents
     */
    @Nonnull
    public static EnumSet<GatewayIntent> fromCacheFlags(@Nonnull CacheFlag flag, @Nonnull CacheFlag... other)
    {
        Checks.notNull(flag, "CacheFlag");
        Checks.noneNull(other, "CacheFlag");
        return fromCacheFlags(EnumSet.of(flag, other));
    }

    /**
     * Parse the required GatewayIntents from the provided {@link CacheFlag CacheFlags}.
     * <br>This creates an {@link EnumSet} based on {@link CacheFlag#getRequiredIntent()}.
     *
     * @param  flags
     *         The cache flags
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link EnumSet} for the required intents
     */
    @Nonnull
    public static EnumSet<GatewayIntent> fromCacheFlags(@Nonnull Collection<CacheFlag> flags)
    {
        EnumSet<GatewayIntent> intents = EnumSet.noneOf(GatewayIntent.class);
        for (CacheFlag flag : flags)
        {
            Checks.notNull(flag, "CacheFlag");
            GatewayIntent intent = flag.getRequiredIntent();
            if (intent != null)
                intents.add(intent);
        }

        return intents;
    }

    /**
     * Parse the required GatewayIntents from the provided {@link GenericEvent Event Types}.
     *
     * @param  events
     *         The event types
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link EnumSet} for the required intents
     */
    @Nonnull
    @SafeVarargs
    public static EnumSet<GatewayIntent> fromEvents(@Nonnull Class<? extends GenericEvent>... events)
    {
        Checks.noneNull(events, "Event");
        return fromEvents(Arrays.asList(events));
    }

    /**
     * Parse the required GatewayIntents from the provided {@link GenericEvent Event Types}.
     *
     * @param  events
     *         The event types
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link EnumSet} for the required intents
     */
    @Nonnull
    public static EnumSet<GatewayIntent> fromEvents(@Nonnull Collection<Class<? extends GenericEvent>> events)
    {
        EnumSet<GatewayIntent> intents = EnumSet.noneOf(GatewayIntent.class);
        for (Class<? extends GenericEvent> event : events)
        {
            Checks.notNull(event, "Event");

            if (GenericUserPresenceEvent.class.isAssignableFrom(event))
                intents.add(GUILD_PRESENCES);
            else if (GenericUserUpdateEvent.class.isAssignableFrom(event) || GenericGuildMemberEvent.class.isAssignableFrom(event) || GuildMemberRemoveEvent.class.isAssignableFrom(event))
                intents.add(GUILD_MEMBERS);

            else if (GuildBanEvent.class.isAssignableFrom(event) || GuildUnbanEvent.class.isAssignableFrom(event) || GuildAuditLogEntryCreateEvent.class.isAssignableFrom(event))
                intents.add(GUILD_MODERATION);
            else if (GenericEmojiEvent.class.isAssignableFrom(event) || GenericGuildStickerEvent.class.isAssignableFrom(event))
                intents.add(GUILD_EMOJIS_AND_STICKERS);
            else if (GenericScheduledEventUpdateEvent.class.isAssignableFrom(event))
                intents.add(SCHEDULED_EVENTS);
            else if (GenericGuildInviteEvent.class.isAssignableFrom(event))
                intents.add(GUILD_INVITES);
            else if (GenericGuildVoiceEvent.class.isAssignableFrom(event))
                intents.add(GUILD_VOICE_STATES);

            else if (MessageBulkDeleteEvent.class.isAssignableFrom(event))
                intents.add(GUILD_MESSAGES);

            else if (GenericMessageReactionEvent.class.isAssignableFrom(event))
                Collections.addAll(intents, GUILD_MESSAGE_REACTIONS, DIRECT_MESSAGE_REACTIONS);

            else if (GenericMessageEvent.class.isAssignableFrom(event))
                Collections.addAll(intents, GUILD_MESSAGES, DIRECT_MESSAGES);

            else if (UserTypingEvent.class.isAssignableFrom(event))
                Collections.addAll(intents, GUILD_MESSAGE_TYPING, DIRECT_MESSAGE_TYPING);

            else if (AutoModExecutionEvent.class.isAssignableFrom(event))
                intents.add(AUTO_MODERATION_EXECUTION);
            else if (GenericAutoModRuleEvent.class.isAssignableFrom(event))
                intents.add(AUTO_MODERATION_CONFIGURATION);
        }
        return intents;
    }

    /**
     * Parse the required GatewayIntents from the provided {@link GenericEvent Event Types} and {@link CacheFlag CacheFlags}.
     *
     * @param  events
     *         The event types
     * @param  flags
     *         The cache flags
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link EnumSet} for the required intents
     */
    @Nonnull
    public static EnumSet<GatewayIntent> from(@Nonnull Collection<Class<? extends GenericEvent>> events, @Nonnull Collection<CacheFlag> flags)
    {
        EnumSet<GatewayIntent> intents = fromEvents(events);
        intents.addAll(fromCacheFlags(flags));
        return intents;
    }
}
