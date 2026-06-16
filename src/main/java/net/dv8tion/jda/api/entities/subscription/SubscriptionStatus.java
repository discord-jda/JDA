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
 *
 */

package net.dv8tion.jda.api.entities.subscription;

import javax.annotation.Nonnull;

/**
 * Representation of a Discord Subscription Status
 *
 * @see <a href="https://discord.com/developers/docs/resources/subscription#subscription-statuses" target="_blank">Discord Docs about Subscription Statuses</a>
 */
public enum SubscriptionStatus {
    ACTIVE(0),
    ENDING(2),
    INACTIVE(1);

    private final int value;

    SubscriptionStatus(int value) {
        this.value = value;
    }

    /**
     * Gets the Subscription status related to the provided key.
     *
     * @param  key
     *         The Discord key referencing a Subscription status.
     *
     * @return The Subscription status that has the key provided
     */
    @Nonnull
    public static SubscriptionStatus fromValue(int key) {
        return SubscriptionStatus.values()[key];
    }

    public int getValue() {
        return value;
    }
}
