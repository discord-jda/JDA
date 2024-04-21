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
package net.dv8tion.jda.api.events.interaction

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Entitlement
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction
import javax.annotation.Nonnull

/**
 * Indicates that an [Interaction] was created.
 * <br></br>Every interaction event is derived from this event.
 *
 *
 * **Requirements**<br></br>
 * To receive these events, you must unset the **Interactions Endpoint URL** in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the [Discord Developers Portal](https://discord.com/developers/applications).
 *
 * @see Interaction
 */
open class GenericInteractionCreateEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @param:Nonnull private val interaction: Interaction
) : Event(api, responseNumber), Interaction {
    /**
     * The [Interaction] instance.
     * <br></br>Note that this event is a delegate which implements the same interface.
     *
     * @return The [Interaction]
     */
    @Nonnull
    open fun getInteraction(): Interaction? {
        return interaction
    }

    @Nonnull
    override fun getToken(): String {
        return interaction.token
    }

    override fun getTypeRaw(): Int {
        return interaction.typeRaw
    }

    override fun getGuild(): Guild? {
        return interaction.guild
    }

    override fun getChannel(): Channel? {
        return interaction.channel
    }

    override fun getChannelIdLong(): Long {
        return interaction.channelIdLong
    }

    @Nonnull
    override fun getUserLocale(): DiscordLocale {
        return interaction.userLocale
    }

    @Nonnull
    override fun getGuildLocale(): DiscordLocale {
        return interaction.getGuildLocale()
    }

    override fun getMember(): Member? {
        return interaction.member
    }

    @Nonnull
    override fun getUser(): User {
        return interaction.user
    }

    @Nonnull
    override fun getEntitlements(): List<Entitlement> {
        return interaction.entitlements
    }

    override val idLong: Long
        get() = interaction.idLong

    override fun isAcknowledged(): Boolean {
        return interaction.isAcknowledged
    }
}
