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

//TODO-v5: Needs docs
public enum ChannelField
{
    //TODO-v5: Should these be the REST JSON names (camelCase), the AuditLogKey names (snake_case), or JDA's generic naming (Event.IDENTIFIER)
    //TODO-v5: Current using JDA's generic namings

    //TODO-v5: All of these need explicit docs
    TYPE("type", AuditLogKey.CHANNEL_TYPE),
    NAME("name", AuditLogKey.CHANNEL_NAME),

    POSITION("position", null), //Discord doesn't track Channel position changes in AuditLog.

    TOPIC("topic", AuditLogKey.CHANNEL_TOPIC),
    NSFW("nsfw", AuditLogKey.CHANNEL_NSFW),
    SLOWMODE("slowmode", AuditLogKey.CHANNEL_SLOWMODE),

    PARENT("parent", AuditLogKey.CHANNEL_PARENT),

    BITRATE("bitrate", AuditLogKey.CHANNEL_BITRATE),
    REGION("region", null), //TODO-v5: JDA needs to add support for channel-specific audit log tracking
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
