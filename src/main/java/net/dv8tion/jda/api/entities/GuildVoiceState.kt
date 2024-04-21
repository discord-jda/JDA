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

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import net.dv8tion.jda.api.requests.RestAction
import java.time.OffsetDateTime
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents the voice state of a [Member][net.dv8tion.jda.api.entities.Member] in a
 * [Guild][net.dv8tion.jda.api.entities.Guild].
 *
 * @see Member.getVoiceState
 */
interface GuildVoiceState : ISnowflake {
    @get:Nonnull
    val jDA: JDA?

    /**
     * Returns whether the [Member][net.dv8tion.jda.api.entities.Member] muted themselves.
     *
     * @return The User's self-mute status
     */
    @JvmField
    val isSelfMuted: Boolean

    /**
     * Returns whether the [Member][net.dv8tion.jda.api.entities.Member] deafened themselves.
     *
     * @return The User's self-deaf status
     */
    @JvmField
    val isSelfDeafened: Boolean

    /**
     * Returns whether the [Member][net.dv8tion.jda.api.entities.Member] is muted, either
     * by choice [.isSelfMuted] or muted by an admin [.isGuildMuted]
     *
     * @return the Member's mute status
     */
    @JvmField
    val isMuted: Boolean

    /**
     * Returns whether the [Member][net.dv8tion.jda.api.entities.Member] is deafened, either
     * by choice [.isSelfDeafened] or deafened by an admin [.isGuildDeafened]
     *
     * @return the Member's deaf status
     */
    @JvmField
    val isDeafened: Boolean

    /**
     * Returns whether the [Member][net.dv8tion.jda.api.entities.Member] got muted by an Admin
     *
     * @return the Member's guild-mute status
     */
    @JvmField
    val isGuildMuted: Boolean

    /**
     * Returns whether the [Member][net.dv8tion.jda.api.entities.Member] got deafened by an Admin
     *
     * @return the Member's guild-deaf status
     */
    @JvmField
    val isGuildDeafened: Boolean

    /**
     * Returns true if this [Member][net.dv8tion.jda.api.entities.Member] is unable to speak because the
     * channel is actively suppressing audio communication. This occurs in
     * [VoiceChannels][VoiceChannel] where the Member either doesn't have
     * [Permission#VOICE_SPEAK][net.dv8tion.jda.api.Permission.VOICE_SPEAK] or if the channel is the
     * designated AFK channel.
     * <br></br>This is also used by [StageChannels][StageChannel] for listeners without speaker approval.
     *
     * @return True, if this [Member&#39;s][net.dv8tion.jda.api.entities.Member] audio is being suppressed.
     *
     * @see .getRequestToSpeakTimestamp
     */
    @JvmField
    val isSuppressed: Boolean

    /**
     * Returns true if this [Member][net.dv8tion.jda.api.entities.Member] is currently streaming with Go Live.
     *
     * @return True, if this member is streaming
     */
    val isStream: Boolean

    /**
     * Returns true if this [Member][net.dv8tion.jda.api.entities.Member] has their camera turned on.
     * <br></br>This does not include streams! See [.isStream]
     *
     * @return True, if this member has their camera turned on.
     */
    val isSendingVideo: Boolean

    /**
     * Returns the current [AudioChannelUnion] that the [Member] is in.
     * If the [Member] is currently not connected to a [AudioChannel], this returns null.
     *
     * @return The AudioChannelUnion that the Member is connected to, or null.
     */
    @JvmField
    val channel: AudioChannelUnion?

    @JvmField
    @get:Nonnull
    val guild: Guild?

    @JvmField
    @get:Nonnull
    val member: Member?

    /**
     * Used to determine if the [Member] is currently connected to an [AudioChannel]
     * in the [Guild] returned from [.getGuild].
     * <br></br>If this is `false`, [.getChannel] will return `null`.
     *
     * @return True, if the [Member] is currently connected to an [AudioChannel] in this [Guild]
     */
    fun inAudioChannel(): Boolean

    /**
     * The Session-Id for this VoiceState
     *
     * @return The Session-Id
     */
    @JvmField
    val sessionId: String?

    /**
     * The time at which the user requested to speak.
     * <br></br>This is used for [StageChannels][StageChannel] and can only be approved by members with [Permission.VOICE_MUTE_OTHERS][net.dv8tion.jda.api.Permission.VOICE_MUTE_OTHERS] on the channel.
     *
     * @return The request to speak timestamp, or null if this user didn't request to speak
     */
    @JvmField
    val requestToSpeakTimestamp: OffsetDateTime?

    /**
     * Promote the member to speaker.
     *
     * This requires a non-null [.getRequestToSpeakTimestamp].
     * You can use [.inviteSpeaker] to invite the member to become a speaker if they haven't requested to speak.
     *
     *
     * This does nothing if the member is not connected to a [StageChannel].
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.VOICE_MUTE_OTHERS][net.dv8tion.jda.api.Permission.VOICE_MUTE_OTHERS]
     * in the associated [StageChannel]
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun approveSpeaker(): RestAction<Void?>?

    /**
     * Reject this members [request to speak][.getRequestToSpeakTimestamp]
     * or moves a [speaker][StageInstance.getSpeakers] back to the [audience][StageInstance.getAudience].
     *
     * This requires a non-null [.getRequestToSpeakTimestamp].
     * The member will have to request to speak again.
     *
     *
     * This does nothing if the member is not connected to a [StageChannel].
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.VOICE_MUTE_OTHERS][net.dv8tion.jda.api.Permission.VOICE_MUTE_OTHERS]
     * in the associated [StageChannel]
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun declineSpeaker(): RestAction<Void?>?

    /**
     * Invite this member to become a speaker.
     *
     *
     * This does nothing if the member is not connected to a [StageChannel].
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.VOICE_MUTE_OTHERS][net.dv8tion.jda.api.Permission.VOICE_MUTE_OTHERS]
     * in the associated [StageChannel]
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun inviteSpeaker(): RestAction<Void?>?
}
