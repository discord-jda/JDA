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

package net.dv8tion.jda.api.entities.channel;

import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * This enum represents the attributes of a channel that can be modified by events.
 * <br><b>Most</b> of these changes are tracked and reflected by {@link AuditLogKey Audit Log Entries}.
 * <br>
 * Values of this enum without an {@link AuditLogKey} are not tracked by the Audit Log.
 *
 * @see net.dv8tion.jda.api.events.channel.GenericChannelEvent
 * @see AuditLogKey
 */
public enum ChannelField
{
    //Generic

    /**
     * The {@link ChannelType type} of the channel.
     *
     * @see Channel#getType()
     */
    TYPE("type", AuditLogKey.CHANNEL_TYPE),

    /**
     * The name of the channel.
     *
     * @see Channel#getName()
     */
    NAME("name", AuditLogKey.CHANNEL_NAME),

    /**
     * The flags of the channel.
     *
     * @see Channel#getFlags()
     */
    FLAGS("flags", AuditLogKey.CHANNEL_FLAGS),

    /**
     * The {@link Category parent} of the channel.
     *
     * <p>Limited to {@link net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel Categorizable Channels} (and implementations).
     *
     * @see net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel#getParentCategory()
     */
    PARENT("parent", AuditLogKey.CHANNEL_PARENT),

    /**
     * The position of this channel relative to other channels in the guild.
     *
     * @see net.dv8tion.jda.api.entities.channel.attribute.IPositionableChannel#getPosition()
     *
     */
    POSITION("position", null), //Discord doesn't track Channel position changes in AuditLog.

    /**
     * The default slowmode applied to threads in a {@link net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer ThreadContainer}.
     *
     * @see IThreadContainer#getDefaultThreadSlowmode()
     */
    DEFAULT_THREAD_SLOWMODE("default_thread_slowmode", AuditLogKey.CHANNEL_DEFAULT_THREAD_SLOWMODE),

    /**
     * The default reaction emoji used in a {@link ForumChannel}.
     *
     * @see ForumChannel#getDefaultReaction()
     */
    DEFAULT_REACTION_EMOJI("default_reaction_emoji", AuditLogKey.CHANNEL_DEFAULT_REACTION_EMOJI),

    //Text Specific
    /**
     * The topic of the channel.
     *
     * <p>Limited to {@link NewsChannel NewsChannels}, {@link TextChannel TextChannels}, and {@link ForumChannel ForumChannels}.
     *
     * @see StandardGuildMessageChannel#getTopic()
     * @see ForumChannel#getTopic()
     */
    TOPIC("topic", AuditLogKey.CHANNEL_TOPIC),

    /**
     * The NSFW state of the channel.
     *
     * <p>Limited to {@link IAgeRestrictedChannel IAgeRestrictedChannels} (and implementations).
     *
     * @see IAgeRestrictedChannel#isNSFW()
     */
    NSFW("nsfw", AuditLogKey.CHANNEL_NSFW),

    /**
     * The state of slow mode in the channel.
     * <br>This defines the minimum time between message sends.
     *
     * <p>Limited to {@link ISlowmodeChannel ISlowmodeChannels} (and implementations).
     *
     * @see ISlowmodeChannel#getSlowmode()
     */
    SLOWMODE("slowmode", AuditLogKey.CHANNEL_SLOWMODE),

    /**
     * The applied tags of a {@link ForumChannel}.
     *
     * @see ForumChannel#getAvailableTags()
     */
    AVAILABLE_TAGS("available_tags", AuditLogKey.CHANNEL_AVAILABLE_TAGS),


    //Voice Specific

    /**
     * The bitrate (in bits per second) of the audio in this channel.
     *
     * <p>For standard channels this is between 8000 and 96000.
     *
     * <p>VIP servers extend this limit to 128000.
     * <br>
     * The bitrates of boost tiers may be found in {@link Guild.BoostTier the boost tiers}.
     *
     * <p>Limited to {@link AudioChannel Audio Channels}.
     *
     * @see AudioChannel#getBitrate()
     */
    BITRATE("bitrate", AuditLogKey.CHANNEL_BITRATE),

    /**
     * The region of the channel.
     *
     * <p>Limited to {@link AudioChannel Audio Channels}.
     *
     * @see AudioChannel#getRegion()
     * @see net.dv8tion.jda.api.Region
     */
    REGION("region", null),

    /**
     * The maximum user count of this channel.
     *
     * <p>Limited to {@link VoiceChannel Voice Channels}.
     *
     * @see VoiceChannel#getUserLimit()
     */
    USER_LIMIT("userlimit", AuditLogKey.CHANNEL_USER_LIMIT),
    /**
     * The status of the channel.
     *
     * <p>Limited to {@link VoiceChannel Voice Channels}.
     *
     * @see VoiceChannel#getStatus()
     */
    VOICE_STATUS("status", AuditLogKey.CHANNEL_VOICE_STATUS),


    //Thread Specific

    /**
     * The auto archive duration of this channel.
     *
     * <p>If the thread is inactive for this long, it becomes auto-archived.
     *
     * <p>Limited to {@link ThreadChannel Thread Channels}.
     *
     * @see ThreadChannel#getAutoArchiveDuration()
     * @see ThreadChannel.AutoArchiveDuration
     */
    AUTO_ARCHIVE_DURATION("autoArchiveDuration", AuditLogKey.THREAD_AUTO_ARCHIVE_DURATION),

    /**
     * The archive state of this channel.
     *
     * <p>If the channel is archived, this is true.
     *
     * <p>Limited to {@link ThreadChannel Thread Channels}.
     *
     * @see ThreadChannel#isArchived()
     */
    ARCHIVED("archived", AuditLogKey.THREAD_ARCHIVED),

    /**
     * The time this channel's archival information was last updated.
     *
     * <p>This timestamp will be updated when any of the following happens:
     * <ul>
     *     <li>The channel is archived</li>
     *     <li>The channel is unarchived</li>
     *     <li>The AUTO_ARCHIVE_DURATION is changed.</li>
     * </ul>
     *
     * Limited to {@link ThreadChannel Thread Channels}.
     *
     * @see ThreadChannel#getTimeArchiveInfoLastModified()
     */
    ARCHIVED_TIMESTAMP("archiveTimestamp", null),

    /**
     * The locked state of this channel.
     *
     * <p>If the channel is locked, this is true.
     *
     * <p>Limited to {@link ThreadChannel Thread Channels}.
     *
     * @see ThreadChannel#isLocked()
     */
    LOCKED("locked", AuditLogKey.THREAD_LOCKED),

    /**
     * The invite state of this channel.
     *
     * <p>If the channel is invitable, this is true.
     *
     * <p>Limited to {@link ThreadChannel Thread Channels}.
     *
     * @see ThreadChannel#isInvitable()
     */
    INVITABLE("invitable", AuditLogKey.THREAD_INVITABLE),

    /**
     * The tags applied to a forum post thread.
     *
     * <p>Limited to {@link ThreadChannel ThreadChannels} inside {@link ForumChannel ForumChannels}
     *
     * @see ThreadChannel#getAppliedTags()
     */
    APPLIED_TAGS("applied_tags", AuditLogKey.THREAD_APPLIED_TAGS),

    /**
     * The default layout of a forum channel.
     *
     * <p>Limited to {@link ForumChannel Forum Channels}.
     *
     * @see ForumChannel#getDefaultLayout()
     */
    DEFAULT_FORUM_LAYOUT("default_forum_layout", AuditLogKey.DEFAULT_FORUM_LAYOUT),

    /**
     * The default sort order of a forum channel.
     *
     * <p>Limited to {@link ForumChannel Forum Channels} and {@link MediaChannel Media Channels}.
     *
     * @see ForumChannel#getDefaultSortOrder()
     */
    DEFAULT_SORT_ORDER("default_sort_order", AuditLogKey.CHANNEL_DEFAULT_SORT_ORDER)
    ;

    private final String fieldName;
    private final AuditLogKey auditLogKey;

    ChannelField(String fieldName, AuditLogKey auditLogKey)
    {
        this.fieldName = fieldName;
        this.auditLogKey = auditLogKey;
    }

    @Nonnull
    public String getFieldName()
    {
        return fieldName;
    }

    @Nullable
    public AuditLogKey getAuditLogKey()
    {
        return auditLogKey;
    }

    @Nonnull
    @Override
    public String toString()
    {
        return new EntityString(this)
                .setType(this)
                .addMetadata("fieldName", fieldName)
                .toString();
    }
}
