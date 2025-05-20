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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * Represents the voice state of a {@link net.dv8tion.jda.api.entities.Member Member} in a
 * {@link net.dv8tion.jda.api.entities.Guild Guild}.
 * 
 * <p>Voice states are only cached while the member is connected to a channel.
 *
 * @see Member#getVoiceState()
 */
public interface GuildVoiceState extends ISnowflake
{
    /**
     * Returns the {@link net.dv8tion.jda.api.JDA JDA} instance of this VoiceState
     *
     * @return The corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * Returns whether the {@link net.dv8tion.jda.api.entities.Member Member} muted themselves.
     *
     * @return The User's self-mute status, or false if the member is not connected to an audio channel
     */
    boolean isSelfMuted();

    /**
     * Returns whether the {@link net.dv8tion.jda.api.entities.Member Member} deafened themselves.
     *
     * @return The User's self-deaf status, or false if the member is not connected to an audio channel
     */
    boolean isSelfDeafened();

    /**
     * Returns whether the {@link net.dv8tion.jda.api.entities.Member Member} is muted, either
     * by choice {@link #isSelfMuted()} or muted by an admin {@link #isGuildMuted()}
     *
     * @return the Member's mute status, or false if the member is not connected to an audio channel
     */
    boolean isMuted();

    /**
     * Returns whether the {@link net.dv8tion.jda.api.entities.Member Member} is deafened, either
     * by choice {@link #isSelfDeafened()} or deafened by an admin {@link #isGuildDeafened()}
     *
     * @return the Member's deaf status, or false if the member is not connected to an audio channel
     */
    boolean isDeafened();

    /**
     * Returns whether the {@link net.dv8tion.jda.api.entities.Member Member} got muted by an Admin
     *
     * @return the Member's guild-mute status, or false if the member is not connected to an audio channel
     */
    boolean isGuildMuted();

    /**
     * Returns whether the {@link net.dv8tion.jda.api.entities.Member Member} got deafened by an Admin
     *
     * @return the Member's guild-deaf status, or false if the member is not connected to an audio channel
     */
    boolean isGuildDeafened();

    /**
     * Returns true if this {@link net.dv8tion.jda.api.entities.Member Member} is unable to speak because the
     * channel is actively suppressing audio communication. This occurs in
     * {@link VoiceChannel VoiceChannels} where the Member either doesn't have
     * {@link net.dv8tion.jda.api.Permission#VOICE_SPEAK Permission#VOICE_SPEAK} or if the channel is the
     * designated AFK channel.
     * <br>This is also used by {@link StageChannel StageChannels} for listeners without speaker approval.
     *
     * @return True, if this {@link net.dv8tion.jda.api.entities.Member Member's} audio is being suppressed.
     *
     * @see    #getRequestToSpeakTimestamp()
     */
    boolean isSuppressed();

    /**
     * Returns true if this {@link net.dv8tion.jda.api.entities.Member Member} is currently streaming with Go Live.
     *
     * @return True, if this member is streaming
     */
    boolean isStream();

    /**
     * Returns true if this {@link net.dv8tion.jda.api.entities.Member Member} has their camera turned on.
     * <br>This does not include streams! See {@link #isStream()}
     *
     * @return True, if this member has their camera turned on.
     */
    boolean isSendingVideo();

    /**
     * Returns the current {@link AudioChannelUnion} that the {@link Member} is in.
     * If the {@link Member} is currently not connected to a {@link AudioChannel}, this returns null.
     *
     * @return The AudioChannelUnion that the Member is connected to, or null if the member is not connected to an audio channel.
     */
    @Nullable
    AudioChannelUnion getChannel();

    /**
     * Returns the {@link Guild} for the {@link Member} that this GuildVoiceState belongs to.
     *
     * @return the Member's Guild
     */
    @Nonnull
    Guild getGuild();

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.Member Member} corresponding to this GuildVoiceState instance
     *
     * @return The member related to this voice state, might not be cached and thus have outdated information.
     */
    @Nonnull
    Member getMember();

    /**
     * Used to determine if the {@link Member} is currently connected to an {@link AudioChannel}
     * in the {@link Guild} returned from {@link #getGuild()}.
     * <br>If this is {@code false}, {@link #getChannel()} will return {@code null}.
     *
     * @return True, if the {@link Member} is currently connected to an {@link AudioChannel} in this {@link Guild}
     */
    boolean inAudioChannel();

    /**
     * The Session-Id for this VoiceState
     *
     * @return The Session-Id, or null if the member is not connected to an audio channel.
     */
    @Nullable
    String getSessionId();

    /**
     * The time at which the user requested to speak.
     * <br>This is used for {@link StageChannel StageChannels} and can only be approved by members with {@link net.dv8tion.jda.api.Permission#VOICE_MUTE_OTHERS Permission.VOICE_MUTE_OTHERS} on the channel.
     *
     * @return The request to speak timestamp, or null if this user didn't request to speak
     */
    @Nullable
    OffsetDateTime getRequestToSpeakTimestamp();

    /**
     * Promote the member to speaker.
     * <p>This requires a non-null {@link #getRequestToSpeakTimestamp()}.
     * You can use {@link #inviteSpeaker()} to invite the member to become a speaker if they haven't requested to speak.
     *
     * <p>This does nothing if the member is not connected to a {@link StageChannel}.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#VOICE_MUTE_OTHERS Permission.VOICE_MUTE_OTHERS}
     *         in the associated {@link StageChannel}
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> approveSpeaker();

    /**
     * Reject this members {@link #getRequestToSpeakTimestamp() request to speak}
     * or moves a {@link StageInstance#getSpeakers() speaker} back to the {@link StageInstance#getAudience() audience}.
     * <p>This requires a non-null {@link #getRequestToSpeakTimestamp()}.
     * The member will have to request to speak again.
     *
     * <p>This does nothing if the member is not connected to a {@link StageChannel}.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#VOICE_MUTE_OTHERS Permission.VOICE_MUTE_OTHERS}
     *         in the associated {@link StageChannel}
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> declineSpeaker();

    /**
     * Invite this member to become a speaker.
     *
     * <p>This does nothing if the member is not connected to a {@link StageChannel}.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#VOICE_MUTE_OTHERS Permission.VOICE_MUTE_OTHERS}
     *         in the associated {@link StageChannel}
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> inviteSpeaker();
}
