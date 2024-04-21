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
package net.dv8tion.jda.api.events.guild.update

import Guild.NotificationLevel
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import javax.annotation.Nonnull

/**
 * Indicates that the [NotificationLevel][net.dv8tion.jda.api.entities.Guild.NotificationLevel] of a [Guild][net.dv8tion.jda.api.entities.Guild] changed.
 *
 *
 * Can be used to detect when a NotificationLevel changes and retrieve the old one
 *
 *
 * Identifier: `notification_level`
 */
class GuildUpdateNotificationLevelEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull guild: Guild,
    @Nonnull oldNotificationLevel: NotificationLevel?
) : GenericGuildUpdateEvent<NotificationLevel?>(
    api,
    responseNumber,
    guild,
    oldNotificationLevel,
    guild.getDefaultNotificationLevel(),
    IDENTIFIER
) {
    @get:Nonnull
    val oldNotificationLevel: NotificationLevel?
        /**
         * The old [NotificationLevel][net.dv8tion.jda.api.entities.Guild.NotificationLevel]
         *
         * @return The old NotificationLevel
         */
        get() = oldValue

    @get:Nonnull
    val newNotificationLevel: NotificationLevel?
        /**
         * The new [NotificationLevel][net.dv8tion.jda.api.entities.Guild.NotificationLevel]
         *
         * @return The new NotificationLevel
         */
        get() = newValue

    @get:Nonnull
    override val oldValue: T?
        get() = super.getOldValue()

    @get:Nonnull
    override val newValue: T?
        get() = super.getNewValue()

    companion object {
        const val IDENTIFIER = "notification_level"
    }
}
