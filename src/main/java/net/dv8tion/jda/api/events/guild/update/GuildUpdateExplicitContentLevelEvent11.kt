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

import Guild.ExplicitContentLevel
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import javax.annotation.Nonnull

/**
 * Indicates that the [ExplicitContentLevel][net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel] of a [Guild][net.dv8tion.jda.api.entities.Guild] changed.
 *
 *
 * Can be used to detect when an ExplicitContentLevel changes and retrieve the old one
 *
 *
 * Identifier: `explicit_content_filter`
 */
class GuildUpdateExplicitContentLevelEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull guild: Guild,
    @Nonnull oldLevel: ExplicitContentLevel?
) : GenericGuildUpdateEvent<ExplicitContentLevel?>(
    api,
    responseNumber,
    guild,
    oldLevel,
    guild.getExplicitContentLevel(),
    IDENTIFIER
) {
    @get:Nonnull
    val oldLevel: ExplicitContentLevel?
        /**
         * The old [ExplicitContentLevel][net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel] for the
         * [Guild][net.dv8tion.jda.api.entities.Guild] prior to this event.
         *
         * @return The old explicit content level
         */
        get() = oldValue

    @get:Nonnull
    val newLevel: ExplicitContentLevel?
        /**
         * The new [ExplicitContentLevel][net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel] for the
         * [Guild][net.dv8tion.jda.api.entities.Guild] after to this event.
         *
         * @return The new explicit content level
         */
        get() = newValue

    @get:Nonnull
    override val oldValue: T?
        get() = super.getOldValue()

    @get:Nonnull
    override val newValue: T?
        get() = super.getNewValue()

    companion object {
        const val IDENTIFIER = "explicit_content_filter"
    }
}
