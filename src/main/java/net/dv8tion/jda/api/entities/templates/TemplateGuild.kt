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
package net.dv8tion.jda.api.entities.templates

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.Guild.*
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.utils.ImageProxy
import java.util.*
import javax.annotation.Nonnull

/**
 * POJO for the guild information provided by a template.
 *
 * @see Template.getGuild
 */
class TemplateGuild(
    override val idLong: Long,
    /**
     * The name of this guild.
     *
     * @return The guild's name
     */
    @get:Nonnull val name: String,
    private val description: String,
    /**
     * The icon id of this guild.
     *
     * @return The guild's icon id
     *
     * @see .getIconUrl
     */
    val iconId: String?,
    /**
     * Returns the [VerificationLevel][net.dv8tion.jda.api.entities.Guild.VerificationLevel] of this guild.
     *
     * @return the verification level of the guild
     */
    @get:Nonnull val verificationLevel: VerificationLevel,
    /**
     * Returns the [NotificationLevel][net.dv8tion.jda.api.entities.Guild.NotificationLevel] of this guild.
     *
     * @return the notification level of the guild
     */
    @get:Nonnull val defaultNotificationLevel: NotificationLevel,
    /**
     * Returns the [ExplicitContentLevel][net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel] of this guild.
     *
     * @return the explicit content level of the guild
     */
    @get:Nonnull val explicitContentLevel: ExplicitContentLevel,
    /**
     * The preferred locale for this guild.
     *
     * @return The preferred [DiscordLocale] for this guild
     */
    @get:Nonnull val locale: DiscordLocale,
    /**
     * Returns the [AFK Timeout][net.dv8tion.jda.api.entities.Guild.Timeout] for this guild.
     *
     * @return the afk timeout for this guild
     */
    @get:Nonnull val afkTimeout: Guild.Timeout,
    private val afkChannel: TemplateChannel,
    private val systemChannel: TemplateChannel,
    roles: List<TemplateRole>?,
    channels: List<TemplateChannel>?
) : ISnowflake {

    /**
     * Gets all [Roles][net.dv8tion.jda.api.entities.templates.TemplateRole] in this [Guild][net.dv8tion.jda.api.entities.templates.TemplateGuild].
     *
     * @return An immutable List of [Roles][net.dv8tion.jda.api.entities.templates.TemplateRole].
     */
    @get:Nonnull
    val roles: List<TemplateRole>

    /**
     * Gets all [Channels][net.dv8tion.jda.api.entities.templates.TemplateChannel] in this [Guild][net.dv8tion.jda.api.entities.templates.TemplateGuild].
     *
     * @return An immutable List of [Channels][net.dv8tion.jda.api.entities.templates.TemplateChannel].
     */
    @get:Nonnull
    val channels: List<TemplateChannel>

    init {
        this.roles = Collections.unmodifiableList(roles)
        this.channels = Collections.unmodifiableList(channels)
    }

    /**
     * The description for this guild.
     * <br></br>This is displayed in the server browser below the guild name for verified guilds.
     *
     * @return The description
     */
    fun getDescription(): String? {
        return description
    }

    val iconUrl: String?
        /**
         * The icon url of this guild.
         *
         * @return The guild's icon url
         *
         * @see .getIconId
         */
        get() = if (iconId == null) null else String.format(
            Guild.Companion.ICON_URL,
            idLong,
            iconId,
            if (iconId.startsWith("a_")) "gif" else "png"
        )
    val icon: ImageProxy?
        /**
         * Returns an [ImageProxy] for this template guild's icon.
         *
         * @return Possibly-null [ImageProxy] of this template guild's icon
         *
         * @see .getIconUrl
         */
        get() {
            val iconUrl = iconUrl
            return iconUrl?.let { ImageProxy(it) }
        }

    /**
     * Provides the [TemplateChannel][net.dv8tion.jda.api.entities.templates.TemplateChannel] that has been set as the channel
     * which [Members][net.dv8tion.jda.api.entities.Member] will be moved to after they have been inactive in a
     * [VoiceChannel][net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel] for longer than [.getAfkTimeout].
     * <br></br>If no channel has been set as the AFK channel, this returns `null`.
     *
     * @return Possibly-null [TemplateChannel][net.dv8tion.jda.api.entities.templates.TemplateChannel] that is the AFK Channel.
     */
    fun getAfkChannel(): TemplateChannel? {
        return afkChannel
    }

    /**
     * Provides the [TemplateChannel][net.dv8tion.jda.api.entities.templates.TemplateChannel] that has been set as the channel
     * which newly joined [Members][net.dv8tion.jda.api.entities.Member] will be announced in.
     * <br></br>If no channel has been set as the system channel, this returns `null`.
     *
     * @return Possibly-null [TemplateChannel][net.dv8tion.jda.api.entities.templates.TemplateChannel] that is the system Channel.
     */
    fun getSystemChannel(): TemplateChannel? {
        return systemChannel
    }
}
