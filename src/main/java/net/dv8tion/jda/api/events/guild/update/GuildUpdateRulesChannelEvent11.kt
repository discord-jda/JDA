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

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import javax.annotation.Nonnull

/**
 * Indicates that the rules channel of a [Guild] changed.
 *
 *
 * Can be used to detect when a guild rule channel changes and retrieve the old one
 *
 *
 * Identifier: `rules_channel`
 */
class GuildUpdateRulesChannelEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull guild: Guild,
    oldRulesChannel: TextChannel?
) : GenericGuildUpdateEvent<TextChannel?>(api, responseNumber, guild, oldRulesChannel, guild.rulesChannel, IDENTIFIER) {
    val oldRulesChannel: TextChannel?
        /**
         * The previous rules channel.
         *
         * @return The previous rules channel
         */
        get() = oldValue
    val newRulesChannel: TextChannel?
        /**
         * The new rules channel.
         *
         * @return The new rules channel
         */
        get() = newValue

    companion object {
        const val IDENTIFIER = "rules_channel"
    }
}
