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
 * This is the keyword which was flagged by the auto-moderation system and caused the action to be executed.
 * <br>
 * This is only valid for the trigger type {@link net.dv8tion.jda.api.entities.TriggerType#KEYWORD_PRESET}.
 */
public enum KeywordPresetType {
    /**
     * Words that may be considered forms of swearing or cursing.
     */
    PROFANITY(1),
    /**
     * Words that refer to sexually explicit behavior or activity.
     */
    SEXUAL_CONTENT(2),
    /**
     * Personal insults or words that may be considered hate speech.
     */
    SLURS(3),
    /**
     * An unknown preset type.
     */
    UNKNOWN(-1);

    private final int value;

    KeywordPresetType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static KeywordPresetType fromValue(int value) {
        for (KeywordPresetType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
