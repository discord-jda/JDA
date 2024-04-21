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
package net.dv8tion.jda.api.audit

import net.dv8tion.jda.internal.utils.EntityString

/**
 * Enum constants for possible options
 * <br></br>Providing detailed description of possible occasions and expected types.
 *
 *
 * The expected types are not guaranteed to be accurate!
 *
 * @see [Optional Audit Entry Info](https://discord.com/developers/docs/resources/audit-log.audit-log-entry-object-optional-audit-entry-info)
 */
enum class AuditLogOption(
    /**
     * Key used in [AuditLogEntry.getOptionByName(String)][AuditLogEntry.getOptionByName]
     *
     * @return Key for this option
     */
    val key: String
) {
    /**
     * Possible detail for
     *
     *  * [ActionType.MESSAGE_DELETE]
     *  * [ActionType.MESSAGE_BULK_DELETE]
     *  * [ActionType.MEMBER_VOICE_KICK]
     *  * [ActionType.MEMBER_VOICE_MOVE]
     *
     * describing the amount of targeted entities.
     * <br></br>Use with [Integer.parseInt].
     *
     *
     * Expected type: **String**
     */
    COUNT("count"),

    /**
     * Possible message id for actions of type [ActionType.MESSAGE_PIN] and [ActionType.MESSAGE_UNPIN].
     * <br></br>Use with [net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.retrieveMessageById].
     *
     *
     * Expected type: **String**
     */
    MESSAGE("message_id"),

    /**
     * Possible secondary target of an [ActionType][net.dv8tion.jda.api.audit.ActionType]
     * such as:
     *
     *  * [ActionType.MEMBER_VOICE_MOVE]
     *  * [ActionType.MESSAGE_PIN]
     *  * [ActionType.MESSAGE_UNPIN]
     *  * [ActionType.MESSAGE_DELETE]
     *
     * Use with [Guild.getGuildChannelById(String)][net.dv8tion.jda.api.entities.Guild.getGuildChannelById].
     *
     *
     * Expected type: **String**
     */
    CHANNEL("channel_id"),

    /**
     * Possible secondary target of an [ActionType][net.dv8tion.jda.api.audit.ActionType]
     * such as [ActionType.CHANNEL_OVERRIDE_CREATE][net.dv8tion.jda.api.audit.ActionType.CHANNEL_OVERRIDE_CREATE]
     * <br></br>Use with [JDA.getUserById(String)][net.dv8tion.jda.api.JDA.getUserById]
     *
     *
     * Expected type: **String**
     */
    USER("user_id"),

    /**
     * Possible secondary target of an [ActionType][net.dv8tion.jda.api.audit.ActionType]
     * such as [ActionType.CHANNEL_OVERRIDE_CREATE][net.dv8tion.jda.api.audit.ActionType.CHANNEL_OVERRIDE_CREATE]
     * <br></br>Use with [Guild.getRoleById(String)][net.dv8tion.jda.api.entities.Guild.getRoleById]
     *
     *
     * Expected type: **String**
     */
    ROLE("role_id"),

    /**
     * Possible name of the role if the target type is [TargetType.ROLE]
     *
     *
     *
     * Expected type: **String**
     */
    ROLE_NAME("role_name"),

    /**
     * Possible option indicating the type of an entity.
     * <br></br>Maybe for [ActionType.CHANNEL_OVERRIDE_CREATE][net.dv8tion.jda.api.audit.ActionType.CHANNEL_OVERRIDE_CREATE]
     * or [ActionType.CHANNEL_CREATE][net.dv8tion.jda.api.audit.ActionType.CHANNEL_CREATE].
     *
     *
     * Expected type: **String** or **Integer**
     * <br></br>This type depends on the action taken place.
     */
    TYPE("type"),

    /**
     * This is sometimes visible for [ActionTypes][net.dv8tion.jda.api.audit.ActionType]
     * which create a new entity.
     * <br></br>Use with designated `getXById` method.
     *
     *
     * Expected type: **String**
     */
    ID("id"),

    /**
     * Possible option of [ActionType.PRUNE][net.dv8tion.jda.api.audit.ActionType.PRUNE]
     * describing the period of inactivity for that prune.
     *
     *
     * Expected type: **int**
     */
    DELETE_MEMBER_DAYS("delete_member_days"),

    /**
     * Possible option of [ActionType.PRUNE][net.dv8tion.jda.api.audit.ActionType.PRUNE]
     * describing the amount of kicked members for that prune.
     *
     *
     * Expected type: **int**
     */
    MEMBERS_REMOVED("members_removed");

    override fun toString(): String {
        return EntityString(this)
            .setType(this)
            .addMetadata("key", key)
            .toString()
    }
}
