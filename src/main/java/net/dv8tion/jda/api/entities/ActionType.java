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

/**
 * Represents the type of action that was performed when a one rules set in the auto-moderation system was broken.
 */
public enum ActionType
{
    /**
     * Blocks the message if the message breaks the auto-moderation rules.
     */
    BLOCK_MESSAGE(1),
    /**
     * Sends an alert to a specific channel if a member breaks the auto-moderation rules.
     */
    SEND_ALERT_MESSAGE(2),
    /**
     * Times out the member who broke the auto-moderation rules.
     */
    TIMEOUT(3),
    /**
     * An unknown action type.
     */
    UNKNOWN(-1);

    private final int value;

    ActionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ActionType fromValue(int value) {
        for (ActionType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
