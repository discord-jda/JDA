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
import javax.annotation.Nullable;

/**
 * This is the action which will be executed by the auto-moderation system when a rule is triggered.
 */
public interface AutoModerationAction {
    /**
     * Gets the action type.
     *
     * @return {@link ActionType ActionTypes}
     */
    @Nonnull
    ActionType getActionType();

    /**
     * Additional metadata needed during the execution for this specific action type
     *
     * @return {@link ActionMetadata ActionMetadata}
     */
    @Nullable
    ActionMetadata getActionMetadata();
}