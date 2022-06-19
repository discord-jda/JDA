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
 * Represents an action that will be executed when an auto moderation rule is triggered.
 */
public interface AutoModerationAction {
    /**
     * Returns the type of this action.
     *
     * @return {@link ActionType ActionType}
     */
    @Nonnull
    ActionType getActionType();

    /**
     * Returns additional metadata used during the execution of this specific action type.
     *
     * @return {@link ActionMetadata ActionMetadata}
     */
    @Nullable
    ActionMetadata getActionMetadata();
}
