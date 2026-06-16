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

package net.dv8tion.jda.api.events.subscription;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.entities.subscription.Subscription;

import javax.annotation.Nonnull;

/**
 * Indicates that an {@link Subscription Subscription} was either created, updated, or deleted
 *
 * @see SubscriptionCreateEvent
 * @see SubscriptionUpdateEvent
 * @see SubscriptionDeleteEvent
 */
public abstract class GenericSubscriptionEvent extends Event {
    protected final Subscription subscription;

    protected GenericSubscriptionEvent(@Nonnull JDA api, long responseNumber, @Nonnull Subscription subscription) {
        super(api, responseNumber);
        this.subscription = subscription;
    }

    /**
     * The {@link Subscription}
     *
     * @return The {@link Subscription}
     */
    @Nonnull
    public Subscription getSubscription() {
        return subscription;
    }
}
