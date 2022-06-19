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

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * This is additional data which is used when an action is executed by the auto-moderation system.
 *
 * <br>
 * Different values depend on the {@link ActionType ActionType} of the action.
 */
public interface ActionMetadata {
    /**
     * Returns the channel where an alert message will be sent when the rule is executed.
     *
     * @return {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     */
    TextChannel getChannel();

    /**
     * Returns the duration of the timeout.
     *
     * @return {@link java.time.Duration Duration}
     */
    @Nonnull
    Duration getDuration();
}
