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
package net.dv8tion.jda.api.events.guild

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.audit.AuditLogEntry
import javax.annotation.Nonnull

/**
 * Indicates that an [AuditLogEntry] was added to a [Guild].
 *
 *
 * This never provides a [responsible user][AuditLogEntry.getUser] instance.
 * You can use [AuditLogEntry.getUserIdLong] instead.
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [GUILD_MODERATION][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MODERATION] intent to be enabled.
 */
class GuildAuditLogEntryCreateEvent(
    @Nonnull api: JDA, responseNumber: Long,
    /**
     * The [AuditLogEntry] that was added to the [Guild]
     *
     * @return The added entry
     */
    @get:Nonnull
    @param:Nonnull val entry: AuditLogEntry
) : GenericGuildEvent(api, responseNumber, entry.getGuild())
