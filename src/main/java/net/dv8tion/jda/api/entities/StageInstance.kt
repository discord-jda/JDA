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

import net.dv8tion.jda.annotations.DeprecatedSince
import net.dv8tion.jda.annotations.ForRemoval
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.managers.StageInstanceManager
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.internal.utils.Helpers
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * A Stage Instance holds information about a live stage.
 *
 *
 * This instance indicates an active stage channel with speakers, usually to host events such as presentations or meetings.
 */
interface StageInstance : ISnowflake {
    @JvmField
    @get:Nonnull
    val guild: Guild?

    @JvmField
    @get:Nonnull
    val channel: StageChannel

    @JvmField
    @get:Nonnull
    val topic: String?

    @JvmField
    @get:Nonnull
    val privacyLevel: PrivacyLevel?

    @get:Nonnull
    val speakers: List<Member?>?
        /**
         * All current speakers of this stage instance.
         *
         *
         * A member is considered a **speaker** when they are currently connected to the stage channel
         * and their voice state is not [suppressed][GuildVoiceState.isSuppressed].
         * When a member is not a speaker, they are part of the [audience][.getAudience].
         *
         *
         * Only [stage moderators][StageChannel.isModerator] can promote or invite speakers.
         * A stage moderator can move between speaker and audience at any time.
         *
         * @return Immutable [List] of [Members][Member] which can speak in this stage instance
         */
        get() = channel.members
            .stream()
            .filter { member ->
                !member.getVoiceState().isSuppressed()
            } // voice states should not be null since getMembers() checks only for connected members in the channel
            .collect(Helpers.toUnmodifiableList())

    @get:Nonnull
    val audience: List<Member?>?
        /**
         * All current audience members of this stage instance.
         *
         *
         * A member is considered part of the **audience** when they are currently connected to the stage channel
         * and their voice state is [suppressed][GuildVoiceState.isSuppressed].
         * When a member is not part of the audience, they are considered a [speaker][.getSpeakers].
         *
         *
         * Only [stage moderators][StageChannel.isModerator] can promote or invite speakers.
         * A stage moderator can move between speaker and audience at any time.
         *
         * @return Immutable [List] of [Members][Member] which cannot speak in this stage instance
         */
        get() = channel.members
            .stream()
            .filter { member ->
                member.getVoiceState().isSuppressed()
            } // voice states should not be null since getMembers() checks only for connected members in the channel
            .collect(Helpers.toUnmodifiableList())

    /**
     * Deletes this stage instance
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_STAGE_INSTANCE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_STAGE_INSTANCE]
     * <br></br>If this stage instance is already deleted
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>If the channel was deleted
     *
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the self member is not a [stage moderator][StageChannel.isModerator]
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun delete(): RestAction<Void?>?

    @JvmField
    @get:CheckReturnValue
    @get:Nonnull
    val manager: StageInstanceManager?

    /**
     * The privacy level for a stage instance.
     *
     *
     * This indicates from where people can join the stage instance.
     */
    enum class PrivacyLevel(
        /**
         * The raw API key for this privacy level
         *
         * @return The raw API value or `-1` if this is [.UNKNOWN]
         */
        @JvmField val key: Int
    ) {
        /** Placeholder for future privacy levels, indicates that this version of JDA does not support this privacy level yet  */
        UNKNOWN(-1),

        /**
         * This stage instance can be accessed by lurkers, meaning users that are not active members of the guild
         *
         */
        @ForRemoval
        @DeprecatedSince("5.0.0")
        @Deprecated("Public stage instances are no longer supported by discord")
        PUBLIC(1),

        /** This stage instance can only be accessed by guild members  */
        GUILD_ONLY(2);

        companion object {
            /**
             * Converts the raw API key into the respective enum value
             *
             * @param  key
             * The API key
             *
             * @return The enum value or [.UNKNOWN]
             */
            @JvmStatic
            @Nonnull
            fun fromKey(key: Int): PrivacyLevel {
                for (level in entries) {
                    if (level.key == key) return level
                }
                return UNKNOWN
            }
        }
    }
}
