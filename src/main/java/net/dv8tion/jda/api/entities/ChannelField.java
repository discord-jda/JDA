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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.audit.AuditLogKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * This enum represents the attributes of a channel that can be modified by events.
 * <br><b>Most</b> of these changes are tracked and reflected by {@link AuditLogKey Audit Log Entries}.
 *
 * @see net.dv8tion.jda.api.events.channel.GenericChannelEvent
 * @see AuditLogKey
 */
public enum ChannelField
{
    //TODO-v5: Should these be the REST JSON names (camelCase), the AuditLogKey names (snake_case), or JDA's generic naming (Event.IDENTIFIER)
    //TODO-v5: Current using JDA's generic namings


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
     * The position of this channel relative to other channels in the guild.
     *
     * @see IPositionableChannel#getPosition()
     * //todo-v5 if the calculation of IPositionableChannel#getPosition() changes, this may need modification too.
     */
    POSITION("position", null), //Discord doesn't track Channel position changes in AuditLog.


    /**
     * The topic of the channel.
     *
     * Limited to {@link BaseGuildMessageChannel Base Guild Channels} (and implementations).
     *
     * @see BaseGuildMessageChannel#getTopic()
     */
    TOPIC("topic", AuditLogKey.CHANNEL_TOPIC),

    /**
     * The NSFW state of the channel.
     *
     * Limited to {@link BaseGuildMessageChannel Base Guild Channels} (and implementations).
     *
     * @see BaseGuildMessageChannel#isNSFW()
     */
    NSFW("nsfw", AuditLogKey.CHANNEL_NSFW),

    /**
     * The state of slow mode in the channel.  This defines the minimum time between message sends.
     *
     * Limited to {@link TextChannel Text Channels}.
     *
     * @see TextChannel#getSlowmode()
     */
    SLOWMODE("slowmode", AuditLogKey.CHANNEL_SLOWMODE),


    /**
     * The {@link Category parent} of the channel.
     *
     * Limited to {@link BaseGuildMessageChannel Base Guild Channels} (and implementations).
     *
     * @see BaseGuildMessageChannel#getParentCategory()
     */
    PARENT("parent", AuditLogKey.CHANNEL_PARENT),


    /**
     * The bitrate (in bits per second) of the audio in this channel.
     *
     * For standard channels this is between 8000 and 96000.
     *
     * VIP servers extend this limit to 128000.
     *
     * Limited to {@link AudioChannel Audio Channels}.
     *
     * @see AudioChannel#getBitrate()
     */
    BITRATE("bitrate", AuditLogKey.CHANNEL_BITRATE),

    /**
     * The region of the channel.
     *
     * Limited to {@link AudioChannel Audio Channels}.
     *
     * @see AudioChannel#getRegion()
     * @see net.dv8tion.jda.api.Region
     */
    REGION("region", null), //TODO-v5: JDA needs to add support for channel-specific audit log tracking

    /**
     * The maximum user count of this channel.
     *
     * Limited to {@link VoiceChannel Voice Channels}.
     *
     * @see VoiceChannel#getUserLimit()
     */
    USER_LIMIT("userlimit", AuditLogKey.CHANNEL_USER_LIMIT),

    //Thread Specific
    AUTO_ARCHIVE_DURATION("autoArchiveDuration", AuditLogKey.THREAD_AUTO_ARCHIVE_DURATION),
    ARCHIVED("archived", AuditLogKey.THREAD_ARCHIVED),
    ARCHIVED_TIMESTAMP("archiveTimestamp", null),
    LOCKED("locked", AuditLogKey.THREAD_LOCKED),
    INVITABLE("invitable", AuditLogKey.THREAD_INVITABLE)
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

    public String toString()
    {
        return "ChannelField." + name() + '(' + fieldName + ')';
    }
}
