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

import net.dv8tion.jda.api.audit.AuditLogKey
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer
import net.dv8tion.jda.api.entities.channel.concrete.*
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel
import net.dv8tion.jda.internal.utils.EntityString
import javax.annotation.Nonnull

/**
 * This enum represents the attributes of a channel that can be modified by events.
 * <br></br>**Most** of these changes are tracked and reflected by [Audit Log Entries][AuditLogKey].
 * <br></br>
 * Values of this enum without an [AuditLogKey] are not tracked by the Audit Log.
 *
 * @see net.dv8tion.jda.api.events.channel.GenericChannelEvent
 *
 * @see AuditLogKey
 */
enum class ChannelField(@JvmField @get:Nonnull val fieldName: String, val auditLogKey: AuditLogKey?) {
    //Generic
    /**
     * The [type][ChannelType] of the channel.
     *
     * @see Channel.getType
     */
    TYPE("type", AuditLogKey.CHANNEL_TYPE),

    /**
     * The name of the channel.
     *
     * @see Channel.getName
     */
    NAME("name", AuditLogKey.CHANNEL_NAME),

    /**
     * The flags of the channel.
     *
     * @see Channel.getFlags
     */
    FLAGS("flags", AuditLogKey.CHANNEL_FLAGS),

    /**
     * The [parent][Category] of the channel.
     *
     *
     * Limited to [Categorizable Channels][net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel] (and implementations).
     *
     * @see net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel.getParentCategory
     */
    PARENT("parent", AuditLogKey.CHANNEL_PARENT),

    /**
     * The position of this channel relative to other channels in the guild.
     *
     * @see net.dv8tion.jda.api.entities.channel.attribute.IPositionableChannel.getPosition
     */
    POSITION("position", null),
    //Discord doesn't track Channel position changes in AuditLog.
    /**
     * The default slowmode applied to threads in a [ThreadContainer][net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer].
     *
     * @see IThreadContainer.getDefaultThreadSlowmode
     */
    DEFAULT_THREAD_SLOWMODE("default_thread_slowmode", AuditLogKey.CHANNEL_DEFAULT_THREAD_SLOWMODE),

    /**
     * The default reaction emoji used in a [ForumChannel].
     *
     * @see ForumChannel.getDefaultReaction
     */
    DEFAULT_REACTION_EMOJI("default_reaction_emoji", AuditLogKey.CHANNEL_DEFAULT_REACTION_EMOJI),
    //Text Specific
    /**
     * The topic of the channel.
     *
     *
     * Limited to [NewsChannels][NewsChannel], [TextChannels][TextChannel], and [ForumChannels][ForumChannel].
     *
     * @see StandardGuildMessageChannel.getTopic
     * @see ForumChannel.getTopic
     */
    TOPIC("topic", AuditLogKey.CHANNEL_TOPIC),

    /**
     * The NSFW state of the channel.
     *
     *
     * Limited to [IAgeRestrictedChannels][IAgeRestrictedChannel] (and implementations).
     *
     * @see IAgeRestrictedChannel.isNSFW
     */
    NSFW("nsfw", AuditLogKey.CHANNEL_NSFW),

    /**
     * The state of slow mode in the channel.
     * <br></br>This defines the minimum time between message sends.
     *
     *
     * Limited to [ISlowmodeChannels][ISlowmodeChannel] (and implementations).
     *
     * @see ISlowmodeChannel.getSlowmode
     */
    SLOWMODE("slowmode", AuditLogKey.CHANNEL_SLOWMODE),

    /**
     * The applied tags of a [ForumChannel].
     *
     * @see ForumChannel.getAvailableTags
     */
    AVAILABLE_TAGS("available_tags", AuditLogKey.CHANNEL_AVAILABLE_TAGS),
    //Voice Specific
    /**
     * The bitrate (in bits per second) of the audio in this channel.
     *
     *
     * For standard channels this is between 8000 and 96000.
     *
     *
     * VIP servers extend this limit to 128000.
     * <br></br>
     * The bitrates of boost tiers may be found in [the boost tiers][Guild.BoostTier].
     *
     *
     * Limited to [Audio Channels][AudioChannel].
     *
     * @see AudioChannel.getBitrate
     */
    BITRATE("bitrate", AuditLogKey.CHANNEL_BITRATE),

    /**
     * The region of the channel.
     *
     *
     * Limited to [Audio Channels][AudioChannel].
     *
     * @see AudioChannel.getRegion
     * @see net.dv8tion.jda.api.Region
     */
    REGION("region", null),

    /**
     * The maximum user count of this channel.
     *
     *
     * Limited to [Voice Channels][VoiceChannel].
     *
     * @see VoiceChannel.getUserLimit
     */
    USER_LIMIT("userlimit", AuditLogKey.CHANNEL_USER_LIMIT),

    /**
     * The status of the channel.
     *
     *
     * Limited to [Voice Channels][VoiceChannel].
     *
     * @see VoiceChannel.getStatus
     */
    VOICE_STATUS("status", AuditLogKey.CHANNEL_VOICE_STATUS),
    //Thread Specific
    /**
     * The auto archive duration of this channel.
     *
     *
     * If the thread is inactive for this long, it becomes auto-archived.
     *
     *
     * Limited to [Thread Channels][ThreadChannel].
     *
     * @see ThreadChannel.getAutoArchiveDuration
     * @see ThreadChannel.AutoArchiveDuration
     */
    AUTO_ARCHIVE_DURATION("autoArchiveDuration", AuditLogKey.THREAD_AUTO_ARCHIVE_DURATION),

    /**
     * The archive state of this channel.
     *
     *
     * If the channel is archived, this is true.
     *
     *
     * Limited to [Thread Channels][ThreadChannel].
     *
     * @see ThreadChannel.isArchived
     */
    ARCHIVED("archived", AuditLogKey.THREAD_ARCHIVED),

    /**
     * The time this channel's archival information was last updated.
     *
     *
     * This timestamp will be updated when any of the following happens:
     *
     *  * The channel is archived
     *  * The channel is unarchived
     *  * The AUTO_ARCHIVE_DURATION is changed.
     *
     *
     * Limited to [Thread Channels][ThreadChannel].
     *
     * @see ThreadChannel.getTimeArchiveInfoLastModified
     */
    ARCHIVED_TIMESTAMP("archiveTimestamp", null),

    /**
     * The locked state of this channel.
     *
     *
     * If the channel is locked, this is true.
     *
     *
     * Limited to [Thread Channels][ThreadChannel].
     *
     * @see ThreadChannel.isLocked
     */
    LOCKED("locked", AuditLogKey.THREAD_LOCKED),

    /**
     * The invite state of this channel.
     *
     *
     * If the channel is invitable, this is true.
     *
     *
     * Limited to [Thread Channels][ThreadChannel].
     *
     * @see ThreadChannel.isInvitable
     */
    INVITABLE("invitable", AuditLogKey.THREAD_INVITABLE),

    /**
     * The tags applied to a forum post thread.
     *
     *
     * Limited to [ThreadChannels][ThreadChannel] inside [ForumChannels][ForumChannel]
     *
     * @see ThreadChannel.getAppliedTags
     */
    APPLIED_TAGS("applied_tags", AuditLogKey.THREAD_APPLIED_TAGS),

    /**
     * The default layout of a forum channel.
     *
     *
     * Limited to [Forum Channels][ForumChannel].
     *
     * @see ForumChannel.getDefaultLayout
     */
    DEFAULT_FORUM_LAYOUT("default_forum_layout", AuditLogKey.DEFAULT_FORUM_LAYOUT),

    /**
     * The default sort order of a forum channel.
     *
     *
     * Limited to [Forum Channels][ForumChannel] and [Media Channels][MediaChannel].
     *
     * @see ForumChannel.getDefaultSortOrder
     */
    DEFAULT_SORT_ORDER("default_sort_order", AuditLogKey.CHANNEL_DEFAULT_SORT_ORDER);

    @Nonnull
    override fun toString(): String {
        return EntityString(this)
            .setType(this)
            .addMetadata("fieldName", fieldName)
            .toString()
    }
}
