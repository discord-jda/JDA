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

package net.dv8tion.jda.api.entities.automod;

import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This enum represents the attributes of an auto moderation rule that can be modified by events.
 * <br><b>Most</b> of these changes are tracked and reflected by {@link AuditLogKey Audit Log Entries}.
 * <br>
 * Values of this enum without an {@link AuditLogKey} are not tracked by the Audit Log.
 *
 * @see net.dv8tion.jda.api.events.automod.GenericAutoModerationEvent
 * @see AuditLogKey
 */
public enum AutoModerationField
{
    /**
     * Returns the {@link Guild} the rule is for.
     *
     * @see AutoModerationRule#getGuild()
     */
    GUILD("guild_id", AuditLogKey.AUTO_MODERATION_GUILD),

    /**
     * Returns the {@link String name} of the rule.
     *
     * @see AutoModerationRule#getName()
     */
    NAME("name", AuditLogKey.AUTO_MODERATION_RULE_NAME),

    /**
     * Returns the {@link User} who created the rule.
     *
     * @see AutoModerationRule#getUser()
     */
    USER("user_id", AuditLogKey.AUTO_MODERATION_CREATOR),

    /**
     * Returns the {@link EventType event} that will cause the auto moderation system to check for a specific trigger.
     *
     * @see AutoModerationRule#getEventType()
     */
    EVENT_TYPE("event_type", AuditLogKey.AUTO_MODERATION_EVENT_TYPE),

    /**
     * Returns the {@link TriggerType} that caused the auto moderation system trigger.
     *
     * @see AutoModerationRule#getTriggerType()
     */
    TRIGGER_TYPE("trigger_type", AuditLogKey.AUTO_MODERATION_TRIGGER_TYPE),

    /**
     * Returns the {@link TriggerMetadata} is additional data used whether a rule should be executed or not.
     *
     * @see AutoModerationRule#getTriggerMetadata()
     */
    TRIGGER_METADATA("trigger_metadata", AuditLogKey.AUTO_MODERATION_TRIGGER_METADATA),

    /**
     * Returns the list of {@link AutoModerationAction actions} that should be executed when the rule is triggered.
     *
     * @see AutoModerationRule#getActions()
     */
    ACTIONS("action", AuditLogKey.AUTO_MODERATION_ACTIONS),

    /**
     * Whether the rule is enabled or not.
     *
     * @see AutoModerationRule#isEnabled()
     */
    ENABLED("enabled", AuditLogKey.AUTO_MODERATION_ENABLED),

    /**
     * Returns the exempt List of {@link Role roles} for this rule.
     *
     * @see AutoModerationRule#getExemptRoles()
     */
    EXEMPT_ROLES("exempt_roles", AuditLogKey.AUTO_MODERATION_EXEMPT_ROLES),

    /**
     * Returns the exempt List of {@link GuildChannel guild channels} for this rule.
     *
     * @see AutoModerationRule#getExemptChannels()
     */
    EXEMPT_CHANNELS("exempt_channels", AuditLogKey.AUTO_MODERATION_EXEMPT_CHANNELS);

    private final String fieldName;
    private final AuditLogKey auditLogKey;

    AutoModerationField(String fieldName, AuditLogKey auditLogKey)
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
        return "AutoModerationField." + name() + '(' + fieldName + ')';
    }
}
