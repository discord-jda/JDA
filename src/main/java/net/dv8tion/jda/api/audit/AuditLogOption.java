/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.audit;

/**
 * Enum constants for possible options
 * <br>Providing detailed description of possible occasions and expected types.
 *
 * <p>The expected types are not guaranteed to be accurate!
 */
public enum AuditLogOption
{
    /**
     * Possible detail for {@link net.dv8tion.jda.api.audit.ActionType#MESSAGE_DELETE ActionType.MESSAGE_DELETE}
     * describing the amount of deleted messages.
     *
     * <p>Expected type: <b>Integer</b>
     */
    COUNT("count"),

    /**
     * Possible message id for actions of type {@link ActionType#MESSAGE_PIN} and {@link ActionType#MESSAGE_UNPIN}.
     * <br>Use with {@link net.dv8tion.jda.api.entities.MessageChannel#retrieveMessageById(String)}.
     *
     * <p>Expected type: <b>String</b>
     */
    MESSAGE("message_id"),

    /**
     * Possible secondary target of an {@link net.dv8tion.jda.api.audit.ActionType ActionType}
     * such as {@link net.dv8tion.jda.api.audit.ActionType#CHANNEL_OVERRIDE_CREATE ActionType.CHANNEL_OVERRIDE_CREATE}
     * <br>Use with {@link net.dv8tion.jda.api.entities.Guild#getTextChannelById(String) Guild.getTextChannelById(String)}
     * or similar method for {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels}
     *
     * <p>Expected type: <b>String</b>
     */
    CHANNEL("channel_id"),

    /**
     * Possible secondary target of an {@link net.dv8tion.jda.api.audit.ActionType ActionType}
     * such as {@link net.dv8tion.jda.api.audit.ActionType#CHANNEL_OVERRIDE_CREATE ActionType.CHANNEL_OVERRIDE_CREATE}
     * <br>Use with {@link net.dv8tion.jda.api.JDA#getUserById(String) JDA.getUserById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    USER("user_id"),

    /**
     * Possible secondary target of an {@link net.dv8tion.jda.api.audit.ActionType ActionType}
     * such as {@link net.dv8tion.jda.api.audit.ActionType#CHANNEL_OVERRIDE_CREATE ActionType.CHANNEL_OVERRIDE_CREATE}
     * <br>Use with {@link net.dv8tion.jda.api.entities.Guild#getRoleById(String) Guild.getRoleById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    ROLE("role_id"),

    /**
     * Possible name of the role if the target type is {@link TargetType#ROLE}
     *
     *
     * <p>Expected type: <b>String</b>
     */
    ROLE_NAME("role_name"),

    /**
     * Possible option indicating the type of an entity.
     * <br>Maybe for {@link net.dv8tion.jda.api.audit.ActionType#CHANNEL_OVERRIDE_CREATE ActionType.CHANNEL_OVERRIDE_CREATE}
     * or {@link net.dv8tion.jda.api.audit.ActionType#CHANNEL_CREATE ActionType.CHANNEL_CREATE}.
     *
     * <p>Expected type: <b>String</b> or <b>Integer</b>
     * <br>This type depends on the action taken place.
     */
    TYPE("type"),

    /**
     * This is sometimes visible for {@link net.dv8tion.jda.api.audit.ActionType ActionTypes}
     * which create a new entity.
     * <br>Use with designated {@code getXById} method.
     *
     * <p>Expected type: <b>String</b>
     */
    ID("id"),

    /**
     * Possible option of {@link net.dv8tion.jda.api.audit.ActionType#PRUNE ActionType.PRUNE}
     * describing the period of inactivity for that prune.
     *
     * <p>Expected type: <b>int</b>
     */
    DELETE_MEMBER_DAYS("delete_member_days"),

    /**
     * Possible option of {@link net.dv8tion.jda.api.audit.ActionType#PRUNE ActionType.PRUNE}
     * describing the amount of kicked members for that prune.
     *
     * <p>Expected type: <b>int</b>
     */
    MEMBERS_REMOVED("members_removed");

    private final String key;

    AuditLogOption(String key)
    {
        this.key = key;
    }

    /**
     * Key used in {@link AuditLogEntry#getOptionByName(String) AuditLogEntry.getOptionByName(String)}
     *
     * @return Key for this option
     */
    public String getKey()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return name() + '(' + key + ')';
    }
}
