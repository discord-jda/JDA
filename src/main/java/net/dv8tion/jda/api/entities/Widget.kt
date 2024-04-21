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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.utils.ImageProxy
import javax.annotation.Nonnull

/**
 * Represents a guild's widget
 *
 * @see WidgetUtil.getWidget
 * @see WidgetUtil.getWidget
 */
interface Widget : ISnowflake {
    /**
     * Shows whether or not the widget for a guild is available. If this
     * method returns false, all other values will be null
     *
     * @return True, if the widget is available, false otherwise
     */
    val isAvailable: Boolean

    @JvmField
    @get:Nonnull
    val name: String?

    /**
     * Gets an invite code for the guild, or null if no invite channel is
     * enabled in the widget
     *
     * @throws IllegalStateException
     * If the widget is not [available][.isAvailable]
     *
     * @return an invite code for the guild, if widget invites are enabled
     */
    val inviteCode: String?

    @get:Nonnull
    val voiceChannels: List<VoiceChannel?>?

    /**
     * Gets a voice channel with the given ID, or null if the voice channel is not found
     *
     * @param  id
     * the ID of the voice channel
     *
     * @throws IllegalStateException
     * If the widget is not [available][.isAvailable]
     * @throws NumberFormatException
     * If the provided `id` cannot be parsed by [Long.parseLong]
     *
     * @return possibly-null VoiceChannel with the given ID.
     */
    fun getVoiceChannelById(id: String?): VoiceChannel?

    /**
     * Gets a voice channel with the given ID, or `null` if the voice channel is not found
     *
     * @param  id
     * the ID of the voice channel
     *
     * @throws IllegalStateException
     * If the widget is not [available][.isAvailable]
     *
     * @return possibly-null VoiceChannel with the given ID.
     */
    fun getVoiceChannelById(id: Long): VoiceChannel?

    @get:Nonnull
    val members: List<Member?>?

    /**
     * Gets a member with the given ID, or null if the member is not found
     *
     * @param  id
     * the ID of the member
     *
     * @throws NumberFormatException
     * If the provided `id` cannot be parsed by [Long.parseLong]
     * @throws IllegalStateException
     * If the widget is not [available][.isAvailable]
     *
     * @return possibly-null Member with the given ID.
     */
    fun getMemberById(id: String?): Member?

    /**
     * Gets a member with the given ID, or `null` if the member is not found
     *
     * @param  id
     * the ID of the member
     *
     * @throws IllegalStateException
     * If the widget is not [available][.isAvailable]
     *
     * @return possibly-null Member with the given ID.
     */
    fun getMemberById(id: Long): Member?

    /**
     * Represents a member of a guild
     *
     * @see Widget.getMembers
     * @see Widget.getMemberById
     * @see Widget.getMemberById
     * @see VoiceChannel.getMembers
     */
    interface Member : IMentionable {
        /**
         * Returns whether or not the given member is a bot account
         *
         * @return true if the member is a bot, false otherwise
         */
        val isBot: Boolean

        @get:Nonnull
        val name: String?

        @get:Nonnull
        val discriminator: String?

        /**
         * Gets the avatar hash of the member, or null if they do not have
         * an avatar set.
         *
         * @return possibly-null String containing the avatar hash of the
         * member
         */
        val avatarId: String?

        /**
         * Gets the avatar url of the member, or null if they do not have
         * an avatar set.
         *
         * @return possibly-null String containing the avatar url of the
         * member
         */
        val avatarUrl: String?

        /**
         * Returns an [ImageProxy] for this user's avatar image.
         *
         * @return Possibly-null [ImageProxy] of this user's avatar image
         *
         * @see .getAvatarUrl
         */
        val avatar: ImageProxy?

        @get:Nonnull
        val defaultAvatarId: String?

        @get:Nonnull
        val defaultAvatarUrl: String?

        @get:Nonnull
        val defaultAvatar: ImageProxy?

        @get:Nonnull
        val effectiveAvatarUrl: String?

        @get:Nonnull
        val effectiveAvatar: ImageProxy?

        /**
         * Gets the nickname of the member. If they do not have a nickname on
         * the guild, this will return null;
         *
         * @return possibly-null String containing the nickname of the member
         */
        val nickname: String?

        @JvmField
        @get:Nonnull
        val effectiveName: String?

        @get:Nonnull
        val onlineStatus: OnlineStatus?

        /**
         * The game that the member is currently playing.
         * <br></br>This game cannot be a stream.
         * If the user is not currently playing a game, this will return null.
         *
         * @return Possibly-null [Activity][net.dv8tion.jda.api.entities.Activity] containing the game
         * that the member is currently playing.
         */
        val activity: Activity?

        @get:Nonnull
        val voiceState: VoiceState?

        @JvmField
        @get:Nonnull
        val widget: Widget?
    }

    /**
     * Represents a voice channel
     *
     * @see Widget.getVoiceChannels
     * @see Widget.getVoiceChannelById
     * @see Widget.getVoiceChannelById
     */
    interface VoiceChannel : ISnowflake {
        /**
         * Gets the integer position of the channel
         *
         * @return integer position of the channel
         */
        val position: Int

        @get:Nonnull
        val name: String?

        @get:Nonnull
        val members: List<Member?>?

        @get:Nonnull
        val widget: Widget?
    }

    /**
     * Represents a [Member&#39;s][net.dv8tion.jda.api.entities.Widget.Member] voice state
     *
     * @see Member.getVoiceState
     */
    interface VoiceState {
        /**
         * Gets the channel the member is in
         *
         * @return never-null VoiceChannel
         */
        val channel: VoiceChannel?

        /**
         * Used to determine if the member is currently in a voice channel.
         * <br></br>If this is false, getChannel() will return null
         *
         * @return True, if the member is in a voice channel
         */
        fun inVoiceChannel(): Boolean

        /**
         * Whether the member is muted by an admin
         *
         * @return True, if the member is muted
         */
        val isGuildMuted: Boolean

        /**
         * Whether the member is deafened by an admin
         *
         * @return True, if the member is deafened
         */
        val isGuildDeafened: Boolean

        /**
         * Whether the member is suppressed
         *
         * @return True, if the member is suppressed
         */
        val isSuppressed: Boolean

        /**
         * Whether the member is self-muted
         *
         * @return True, if the member is self-muted
         */
        val isSelfMuted: Boolean

        /**
         * Whether the member is self-deafened
         *
         * @return True, if the member is self-deafened
         */
        val isSelfDeafened: Boolean

        /**
         * Whether the member is muted, either by an admin or self-muted
         *
         * @return True, if the member is self-muted or guild-muted
         */
        val isMuted: Boolean

        /**
         * Whether the member is deafened, either by an admin or self-deafened
         *
         * @return True, if the member is self-deafened or guild-deafened
         */
        val isDeafened: Boolean

        @JvmField
        @get:Nonnull
        val member: Member?

        @JvmField
        @get:Nonnull
        val widget: Widget?
    }
}
