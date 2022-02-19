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

import net.dv8tion.jda.annotations.DeprecatedSince;
import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.api.managers.StageInstanceManager;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Stage Instance holds information about a live stage.
 *
 * <p>This instance indicates an active stage channel with speakers, usually to host events such as presentations or meetings.
 */
public interface StageInstance extends ISnowflake
{
    /**
     * The {@link Guild} this stage instance is in
     *
     * @return The {@link Guild}
     */
    @Nonnull
    Guild getGuild();

    /**
     * The {@link StageChannel} for this stage instance
     *
     * @return The {@link StageChannel}
     */
    @Nonnull
    StageChannel getChannel();

    /**
     * The topic of this stage instance
     *
     * @return The topic
     */
    @Nonnull
    String getTopic();

    /**
     * The {@link PrivacyLevel} of this stage instance
     *
     * @return The {@link PrivacyLevel}
     */
    @Nonnull
    PrivacyLevel getPrivacyLevel();

    /**
     * Whether this stage instance can be found in stage discovery.
     *
     * @return True if this is a public stage that can be found in stage discovery
     *
     * @deprecated Stage discovery has been removed from the platform
     */
    @Deprecated
    @ForRemoval
    @DeprecatedSince("5.0.0")
    boolean isDiscoverable();

    /**
     * All current speakers of this stage instance.
     *
     * <p>A member is considered a <b>speaker</b> when they are currently connected to the stage channel
     * and their voice state is not {@link GuildVoiceState#isSuppressed() suppressed}.
     * When a member is not a speaker, they are part of the {@link #getAudience() audience}.
     *
     * <p>Only {@link StageChannel#isModerator(Member) stage moderators} can promote or invite speakers.
     * A stage moderator can move between speaker and audience at any time.
     *
     * @return {@link List} of {@link Member Members} which can speak in this stage instance
     */
    @Nonnull
    default List<Member> getSpeakers()
    {
        return Collections.unmodifiableList(getChannel().getMembers()
                .stream()
                .filter(member -> !member.getVoiceState().isSuppressed()) // voice states should not be null since getMembers() checks only for connected members in the channel
                .collect(Collectors.toList()));
    }

    /**
     * All current audience members of this stage instance.
     *
     * <p>A member is considered part of the <b>audience</b> when they are currently connected to the stage channel
     * and their voice state is {@link GuildVoiceState#isSuppressed() suppressed}.
     * When a member is not part of the audience, they are considered a {@link #getSpeakers() speaker}.
     *
     * <p>Only {@link StageChannel#isModerator(Member) stage moderators} can promote or invite speakers.
     * A stage moderator can move between speaker and audience at any time.
     *
     * @return {@link List} of {@link Member Members} which cannot speak in this stage instance
     */
    @Nonnull
    default List<Member> getAudience()
    {
        return Collections.unmodifiableList(getChannel().getMembers()
                .stream()
                .filter(member -> member.getVoiceState().isSuppressed()) // voice states should not be null since getMembers() checks only for connected members in the channel
                .collect(Collectors.toList()));
    }

    /**
     * Deletes this stage instance
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_STAGE_INSTANCE UNKNOWN_STAGE_INSTANCE}
     *     <br>If this stage instance is already deleted</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If the channel was deleted</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the self member is not a {@link StageChannel#isModerator(Member) stage moderator}
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> delete();

    /**
     * The {@link StageInstanceManager} used to update this stage instance.
     * <p>This can be used to update multiple fields such as topic and privacy level in one request
     *
     * <p>If this stage instance is already deleted, this will fail with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_STAGE_INSTANCE ErrorResponse.UNKNOWN_STAGE_INSTANCE}.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the self member is not a {@link StageChannel#isModerator(Member) stage moderator}
     *
     * @return The {@link StageInstanceManager}
     */
    @Nonnull
    @CheckReturnValue
    StageInstanceManager getManager();

    /**
     * The privacy level for a stage instance.
     *
     * <p>This indicates from where people can join the stage instance.
     */
    enum PrivacyLevel
    {
        /** Placeholder for future privacy levels, indicates that this version of JDA does not support this privacy level yet */
        UNKNOWN(-1),
        /**
         * This stage instance can be accessed by lurkers, meaning users that are not active members of the guild
         *
         * @deprecated Public stage instances are no longer supported by discord
         */
        @Deprecated
        @ForRemoval
        @DeprecatedSince("5.0.0")
        PUBLIC(1),
        /** This stage instance can only be accessed by guild members */
        GUILD_ONLY(2);

        private final int key;

        PrivacyLevel(int key)
        {
            this.key = key;
        }

        /**
         * The raw API key for this privacy level
         *
         * @return The raw API value or {@code -1} if this is {@link #UNKNOWN}
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Converts the raw API key into the respective enum value
         *
         * @param  key
         *         The API key
         *
         * @return The enum value or {@link #UNKNOWN}
         */
        @Nonnull
        public static PrivacyLevel fromKey(int key)
        {
            for (PrivacyLevel level : values())
            {
                if (level.key == key)
                    return level;
            }
            return UNKNOWN;
        }
    }
}
