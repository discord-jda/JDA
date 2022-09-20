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

package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.api.entities.StageInstance;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manager providing functionality to update one or more fields for a {@link net.dv8tion.jda.api.entities.StageInstance StageInstance}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setTopic("LMAO JOIN FOR FREE NITRO")
 *        .setPrivacyLevel(PrivacyLevel.PUBLIC)
 *        .queue();
 * manager.reset(ChannelManager.TOPIC | ChannelManager.PRIVACY_LEVEL)
 *        .setTopic("Talent Show | WINNER GETS FREE NITRO")
 *        .setPrivacyLevel(PrivacyLevel.GUILD_ONLY)
 *        .queue();
 * }</pre>
 *
 * @see net.dv8tion.jda.api.entities.StageInstance#getManager()
 */
public interface StageInstanceManager extends Manager<StageInstanceManager>
{
    /** Used to reset the topic field */
    long TOPIC         = 1;
    /** Used to reset the privacy level field */
    long PRIVACY_LEVEL = 1 << 1;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(ChannelManager.TOPIC | ChannelManager.PRIVACY_LEVEL);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #TOPIC}</li>
     *     <li>{@link #PRIVACY_LEVEL}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return StageInstanceManager for chaining convenience
     */
    @Nonnull
    @Override
    StageInstanceManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br>Example: {@code manager.reset(ChannelManager.TOPIC, ChannelManager.PRIVACY_LEVEL);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #TOPIC}</li>
     *     <li>{@link #PRIVACY_LEVEL}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return StageInstanceManager for chaining convenience
     */
    @Nonnull
    @Override
    StageInstanceManager reset(long... fields);

    /**
     * The associated {@link StageInstance}
     *
     * @return The {@link StageInstance}
     */
    @Nonnull
    StageInstance getStageInstance();

    /**
     * Sets the topic for this stage instance.
     * <br>This shows up in stage discovery and in the stage view.
     *
     * @param  topic
     *         The topic or null to reset, must be 1-120 characters long
     *
     * @throws IllegalArgumentException
     *         If the topic is longer than 120 characters
     *
     * @return StageInstanceManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    StageInstanceManager setTopic(@Nullable String topic);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel PrivacyLevel} for this stage instance.
     * <br>This indicates whether guild lurkers are allowed to join the stage instance or only guild members.
     *
     * @param  level
     *         The privacy level
     *
     * @throws IllegalArgumentException
     *         If the privacy level is null, {@link net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel#UNKNOWN UNKNOWN},
     *         or {@link net.dv8tion.jda.api.entities.StageInstance.PrivacyLevel#PUBLIC PUBLIC}.
     *
     * @return StageInstanceManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("deprecation")
    StageInstanceManager setPrivacyLevel(@Nonnull StageInstance.PrivacyLevel level);
}
