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
package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.api.utils.WidgetUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a guild's widget
 * 
 * @see WidgetUtil#getWidget(long)
 * @see WidgetUtil#getWidget(String)
 */
public interface Widget extends ISnowflake
{

    /**
     * Shows whether or not the widget for a guild is available. If this
     * method returns false, all other values will be null
     * 
     * @return True, if the widget is available, false otherwise
     */
    boolean isAvailable();

    /**
     * Gets the name of the guild
     *
     * @throws IllegalStateException
     *         If the widget is not {@link #isAvailable() available}
     *
     * @return the name of the guild
     */
    @Nonnull
    String getName();

    /**
     * Gets an invite code for the guild, or null if no invite channel is
     * enabled in the widget
     *
     * @throws IllegalStateException
     *         If the widget is not {@link #isAvailable() available}
     *
     * @return an invite code for the guild, if widget invites are enabled
     */
    @Nullable
    String getInviteCode();

    /**
     * Gets the list of voice channels in the guild
     *
     * @throws IllegalStateException
     *         If the widget is not {@link #isAvailable() available}
     *
     * @return the list of voice channels in the guild
     */
    @Nonnull
    List<VoiceChannel> getVoiceChannels();

    /**
     * Gets a voice channel with the given ID, or null if the voice channel is not found
     * 
     * @param  id
     *         the ID of the voice channel
     *
     * @throws IllegalStateException
     *         If the widget is not {@link #isAvailable() available}
     * @throws NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return possibly-null VoiceChannel with the given ID. 
     */
    @Nullable
    VoiceChannel getVoiceChannelById(@Nonnull String id);

    /**
     * Gets a voice channel with the given ID, or {@code null} if the voice channel is not found
     *
     * @param  id
     *         the ID of the voice channel
     *
     * @throws IllegalStateException
     *         If the widget is not {@link #isAvailable() available}
     *
     * @return possibly-null VoiceChannel with the given ID.
     */
    @Nullable
    VoiceChannel getVoiceChannelById(long id);

    /**
     * Gets a list of online members in the guild
     *
     * @throws IllegalStateException
     *         If the widget is not {@link #isAvailable() available}
     *
     * @return the list of members
     */
    @Nonnull
    List<Member> getMembers();

    /**
     * Gets a member with the given ID, or null if the member is not found
     * 
     * @param  id
     *         the ID of the member
     *
     * @throws NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * @throws IllegalStateException
     *         If the widget is not {@link #isAvailable() available}
     *
     * @return possibly-null Member with the given ID. 
     */
    @Nullable
    Member getMemberById(@Nonnull String id);

    /**
     * Gets a member with the given ID, or {@code null} if the member is not found
     *
     * @param  id
     *         the ID of the member
     *
     * @throws IllegalStateException
     *         If the widget is not {@link #isAvailable() available}
     *
     * @return possibly-null Member with the given ID.
     */
    @Nullable
    Member getMemberById(long id);

    /**
     * Represents a member of a guild
     * 
     * @see     Widget#getMembers()
     * @see     Widget#getMemberById(long)
     * @see     Widget#getMemberById(String)
     * @see     VoiceChannel#getMembers()
     */
    public interface Member extends IMentionable
    {

        /**
         * Returns whether or not the given member is a bot account
         * 
         * @return true if the member is a bot, false otherwise
         */
        boolean isBot();

        /**
         * Returns the username of the member
         * 
         * @return the username of the member
         */
        @Nonnull
        String getName();

        /**
         * Gets the discriminator of the member
         * 
         * @return the never-null discriminator of the member
         */
        @Nonnull
        String getDiscriminator();

        /**
         * Gets the avatar hash of the member, or null if they do not have
         * an avatar set.
         * 
         * @return possibly-null String containing the avatar hash of the
         *         member
         */
        @Nullable
        String getAvatarId();

        /**
         * Gets the avatar url of the member, or null if they do not have
         * an avatar set.
         * 
         * @return possibly-null String containing the avatar url of the
         *         member
         */
        @Nullable
        String getAvatarUrl();

        /**
         * Returns an {@link ImageProxy} for this user's avatar image.
         *
         * @return Possibly-null {@link ImageProxy} of this user's avatar image
         *
         * @see    #getAvatarUrl()
         */
        @Nullable
        ImageProxy getAvatar();

        /**
         * Gets the asset id of the member's default avatar
         * 
         * @return never-null String containing the asset id of the member's
         *         default avatar
         */
        @Nonnull
        String getDefaultAvatarId();

        /**
         * Gets the url of the member's default avatar
         * 
         * @return never-null String containing the url of the member's
         *         default avatar
         */
        @Nonnull
        String getDefaultAvatarUrl();

        /**
         * Returns an {@link ImageProxy} for this user's default avatar image.
         *
         * @return Never-null {@link ImageProxy} of this user's default avatar image
         *
         * @see    #getDefaultAvatarUrl()
         */
        @Nonnull
        ImageProxy getDefaultAvatar();

        /**
         * The URL for the user's avatar image
         * <br>If they do not have an avatar set, this will return the URL of their
         * default avatar
         * 
         * @return Never-null String containing the member's effective avatar url.
         */
        @Nonnull
        String getEffectiveAvatarUrl();

        /**
         * Returns an {@link ImageProxy} for this user's effective avatar image.
         *
         * @return Never-null {@link ImageProxy} of this user's effective avatar image
         *
         * @see    #getEffectiveAvatarUrl()
         */
        @Nonnull
        ImageProxy getEffectiveAvatar();

        /**
         * Gets the nickname of the member. If they do not have a nickname on
         * the guild, this will return null;
         * 
         * @return possibly-null String containing the nickname of the member
         */
        @Nullable
        String getNickname();

        /**
         * Gets the visible name of the member. If they have a nickname set,
         * this will be their nickname. Otherwise, it will be their username.
         * 
         * @return never-null String containing the member's effective (visible) name
         */
        @Nonnull
        String getEffectiveName();

        /**
         * Gets the online status of the member. The widget does not show
         * offline members, so this status should never be offline
         * 
         * @return the {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} of the member
         */
        @Nonnull
        OnlineStatus getOnlineStatus();

        /**
         * The game that the member is currently playing.
         * <br>This game cannot be a stream.
         * If the user is not currently playing a game, this will return null.
         *
         * @return Possibly-null {@link net.dv8tion.jda.api.entities.Activity Activity} containing the game
         *         that the member is currently playing.
         */
        @Nullable
        Activity getActivity();

        /**
         * The current voice state of the member.
         * <br>If the user is not in voice, this will return a VoiceState with a null channel.
         * 
         * @return never-null VoiceState of the member
         */
        @Nonnull
        VoiceState getVoiceState();

        /**
         * Gets the widget that to which this member belongs
         * 
         * @return the Widget that holds this member
         */
        @Nonnull
        Widget getWidget();

    }

    /**
     * Represents a voice channel
     * 
     * @see     Widget#getVoiceChannels()
     * @see     Widget#getVoiceChannelById(long)
     * @see     Widget#getVoiceChannelById(String)
     */
    public interface VoiceChannel extends ISnowflake
    {

        /**
         * Gets the integer position of the channel
         * 
         * @return integer position of the channel
         */
        int getPosition();

        /**
         * Gets the name of the channel	 * 
         * @return name of the channel
         */
        @Nonnull
        String getName();

        /**
         * Gets a list of all members in the channel
         * 
         * @return never-null, possibly-empty list of members in the channel
         */
        @Nonnull
        List<Member> getMembers();

        /**
         * Gets the Widget to which this voice channel belongs
         * 
         * @return the Widget object that holds this voice channel
         */
        @Nonnull
        Widget getWidget();
    }

    /**
     * Represents a {@link net.dv8tion.jda.api.entities.Widget.Member Member's} voice state
     * 
     * @see     Member#getVoiceState()
     */
    public interface VoiceState
    {

        /**
         * Gets the channel the member is in
         * 
         * @return never-null VoiceChannel
         */
        @Nullable
        VoiceChannel getChannel();

        /**
         * Used to determine if the member is currently in a voice channel.
         * <br>If this is false, getChannel() will return null
         * 
         * @return True, if the member is in a voice channel
         */
        boolean inVoiceChannel();

        /**
         * Whether the member is muted by an admin
         * 
         * @return True, if the member is muted
         */
        boolean isGuildMuted();

        /**
         * Whether the member is deafened by an admin
         * 
         * @return True, if the member is deafened
         */
        boolean isGuildDeafened();

        /**
         * Whether the member is suppressed
         * 
         * @return True, if the member is suppressed
         */
        boolean isSuppressed();

        /**
         * Whether the member is self-muted
         * 
         * @return True, if the member is self-muted
         */
        boolean isSelfMuted();

        /**
         * Whether the member is self-deafened
         * 
         * @return True, if the member is self-deafened
         */
        boolean isSelfDeafened();

        /**
         * Whether the member is muted, either by an admin or self-muted
         * 
         * @return True, if the member is self-muted or guild-muted
         */
        boolean isMuted();

        /**
         * Whether the member is deafened, either by an admin or self-deafened
         * 
         * @return True, if the member is self-deafened or guild-deafened
         */
        boolean isDeafened();

        /**
         * Gets the {@link net.dv8tion.jda.api.entities.Widget.Member Member} to which this
         * VoiceState belongs
         * 
         * @return the member
         */
        @Nonnull
        Member getMember();

        /**
         * Gets the {@link net.dv8tion.jda.api.entities.Widget Widget} to which the
         * {@link net.dv8tion.jda.api.entities.Widget.Member Member} of this VoiceState belongs
         * 
         * @return the widget
         */
        @Nonnull
        Widget getWidget();
    }
}
