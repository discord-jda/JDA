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

package net.dv8tion.jda.api.entities.automod;

import net.dv8tion.jda.api.entities.GuildChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;

/**
 * Represents additional data used when an {@link AutoModerationAction action} is executed by the auto moderation system.
 *
 * <br>
 * Different values depend on the {@link AutoModerationActionType ActionType} of the action.
 */
public interface ActionMetadata
{
    /**
     * Returns the channel where an alert message will be sent when the rule is executed.
     * <br>
     * The associated action type is {@link AutoModerationActionType#SEND_ALERT_MESSAGE}
     *
     * @return {@link GuildChannel}
     */
    @Nullable
    GuildChannel getChannel();

    /**
     * Used to set the channel where an alert message will be sent when the rule is executed.
     *
     * @param channel
     *        The channel where an alert message will be sent when the rule is executed.
     *
     * @return {@link ActionMetadata}
     */
    @Nullable
    ActionMetadata setChannel(@Nonnull GuildChannel channel);

    /**
     * Returns the duration of the timeout and the maximum is 2419200 seconds (4 weeks)
     * <br>
     * The associated action type is {@link AutoModerationActionType#TIMEOUT}
     *
     * @return {@link Duration}
     */
    @Nullable
    Duration getDuration();

    /**
     * Used to set the duration of the timeout.
     *
     * @param duration
     *        The duration of the timeout.
     *
     * @return {@link ActionMetadata}
     */
    @Nullable
    ActionMetadata setDuration(@Nonnull Duration duration);
}