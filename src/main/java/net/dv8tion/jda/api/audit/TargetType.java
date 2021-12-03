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

package net.dv8tion.jda.api.audit;

/**
 * TargetType for an {@link net.dv8tion.jda.api.audit.ActionType ActionType}
 * <br>This describes what kind of Discord entity is being targeted by an auditable action!
 *
 * <p>This can be found via {@link net.dv8tion.jda.api.audit.ActionType#getTargetType() ActionType.getTargetType()}
 * or {@link net.dv8tion.jda.api.audit.AuditLogEntry#getTargetType() AuditLogEntry.getTargetType()}.
 * <br>This helps to decide what entity type the target id of an AuditLogEntry refers to.
 *
 * <h2>Example</h2>
 * If {@code entry.getTargetType()} is type {@link #GUILD}
 * <br>Then the target id returned by {@code entry.getTargetId()} and {@code entry.getTargetIdLong()}
 * can be used with {@link net.dv8tion.jda.api.JDA#getGuildById(long) JDA.getGuildById(id)}
 */
public enum TargetType
{
    GUILD,
    CHANNEL,
    ROLE,
    MEMBER,
    INVITE,
    WEBHOOK,
    EMOTE,
    INTEGRATION,
    STAGE_INSTANCE,
    THREAD,
    UNKNOWN
}
