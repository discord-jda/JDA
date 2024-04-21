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

/**
 * TargetType for an [ActionType][net.dv8tion.jda.api.audit.ActionType]
 * <br></br>This describes what kind of Discord entity is being targeted by an auditable action!
 *
 *
 * This can be found via [ActionType.getTargetType()][net.dv8tion.jda.api.audit.ActionType.getTargetType]
 * or [AuditLogEntry.getTargetType()][net.dv8tion.jda.api.audit.AuditLogEntry.getTargetType].
 * <br></br>This helps to decide what entity type the target id of an AuditLogEntry refers to.
 *
 *
 * **Example**<br></br>
 * If `entry.getTargetType()` is type [.GUILD]
 * <br></br>Then the target id returned by `entry.getTargetId()` and `entry.getTargetIdLong()`
 * can be used with [JDA.getGuildById(id)][net.dv8tion.jda.api.JDA.getGuildById]
 */
enum class TargetType {
    GUILD,
    CHANNEL,
    ROLE,
    MEMBER,
    INVITE,
    WEBHOOK,
    EMOJI,
    INTEGRATION,
    STAGE_INSTANCE,
    STICKER,
    THREAD,
    SCHEDULED_EVENT,
    AUTO_MODERATION_RULE,
    UNKNOWN
}
