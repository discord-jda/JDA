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
package net.dv8tion.jda.api.entities.channel.concrete

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildVoiceState
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.StageInstance
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.managers.channel.ChannelManager
import net.dv8tion.jda.api.managers.channel.concrete.StageChannelManager
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.ChannelAction
import net.dv8tion.jda.api.requests.restaction.StageInstanceAction
import net.dv8tion.jda.internal.requests.restaction.StageInstanceActionImpl
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a Stage Channel.
 *
 *
 * This is a specialized AudioChannel that can be used to host events with speakers and listeners.
 */
interface StageChannel : StandardGuildChannel, GuildMessageChannel, AudioChannel, IWebhookContainer,
    IAgeRestrictedChannel, ISlowmodeChannel {
    /**
     * [StageInstance] attached to this stage channel.
     *
     *
     * This indicates whether a stage channel is currently "live".
     *
     * @return The [StageInstance] or `null` if this stage is not live
     */
    @JvmField
    val stageInstance: StageInstance?

    /**
     * Create a new [StageInstance] for this stage channel.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [STAGE_ALREADY_OPEN][net.dv8tion.jda.api.requests.ErrorResponse.STAGE_ALREADY_OPEN]
     * <br></br>If there already is an active [StageInstance] for this channel
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>If the channel was deleted
     *
     *
     * @param  topic
     * The topic of this stage instance, must be 1-120 characters long
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the self member is not a stage moderator. (See [.isModerator])
     * @throws IllegalArgumentException
     * If the topic is null, empty, or longer than 120 characters
     *
     * @return [StageInstanceAction]
     */
    @Nonnull
    @CheckReturnValue
    fun createStageInstance(@Nonnull topic: String?): StageInstanceAction? {
        val permissions = getGuild().getSelfMember().getPermissions(this)
        val required = EnumSet.of(Permission.MANAGE_CHANNEL, Permission.VOICE_MUTE_OTHERS, Permission.VOICE_MOVE_OTHERS)
        for (perm in required) {
            if (!permissions.contains(perm)) throw InsufficientPermissionException(
                this,
                perm,
                "You must be a stage moderator to create a stage instance! Missing Permission: $perm"
            )
        }
        return StageInstanceActionImpl(this).setTopic(topic!!)
    }

    /**
     * Whether this member is considered a moderator for this stage channel.
     * <br></br>Moderators can modify the [Stage Instance][.getStageInstance] and promote speakers.
     * To promote a speaker you can use [GuildVoiceState.inviteSpeaker] or [GuildVoiceState.approveSpeaker] if they have already raised their hand (indicated by [GuildVoiceState.getRequestToSpeakTimestamp]).
     * A stage moderator can move between speaker and audience without raising their hand. This can be done with [Guild.requestToSpeak] and [Guild.cancelRequestToSpeak] respectively.
     *
     *
     * A member is considered a stage moderator if they have these permissions in the stage channel:
     *
     *  * [Permission.MANAGE_CHANNEL]
     *  * [Permission.VOICE_MUTE_OTHERS]
     *  * [Permission.VOICE_MOVE_OTHERS]
     *
     *
     * @param  member
     * The member to check
     *
     * @throws IllegalArgumentException
     * If the provided member is null or not from this guild
     *
     * @return True, if the provided member is a stage moderator
     */
    fun isModerator(@Nonnull member: Member): Boolean {
        Checks.notNull(member, "Member")
        return member.hasPermission(
            this,
            Permission.MANAGE_CHANNEL,
            Permission.VOICE_MUTE_OTHERS,
            Permission.VOICE_MOVE_OTHERS
        )
    }

    @Nonnull
    override fun createCopy(@Nonnull guild: Guild?): ChannelAction<StageChannel?>?
    @Nonnull
    override fun createCopy(): ChannelAction<StageChannel?>? {
        return createCopy(getGuild())
    }

    @get:Nonnull
    abstract override val manager: ChannelManager<*, *>?

    /**
     * Sends a [request-to-speak][GuildVoiceState.getRequestToSpeakTimestamp] indicator to the stage instance moderators.
     *
     * If the self member has [Permission.VOICE_MUTE_OTHERS] this will immediately promote them to speaker.
     *
     * @throws IllegalStateException
     * If the self member is not currently connected to the channel
     *
     * @return [RestAction]
     *
     * @see .cancelRequestToSpeak
     */
    @Nonnull
    @CheckReturnValue
    fun requestToSpeak(): RestAction<Void?>?

    /**
     * Cancels the [Request-to-Speak][.requestToSpeak].
     * <br></br>This can also be used to move back to the audience if you are currently a speaker.
     *
     *
     * If there is no request to speak or the member is not currently connected to an active [StageInstance], this does nothing.
     *
     * @throws IllegalStateException
     * If the self member is not currently connected to the channel
     *
     * @return [RestAction]
     *
     * @see .requestToSpeak
     */
    @Nonnull
    @CheckReturnValue
    fun cancelRequestToSpeak(): RestAction<Void?>?

    companion object {
        /**
         * The maximum limit you can set with [StageChannelManager.setUserLimit]. ({@value})
         */
        const val MAX_USERLIMIT = 10000
    }
}
