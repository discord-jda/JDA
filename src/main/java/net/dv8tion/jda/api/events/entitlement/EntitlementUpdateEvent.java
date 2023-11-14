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

package net.dv8tion.jda.api.events.entitlement;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Entitlement;

import javax.annotation.Nonnull;

/**
 * Indicates a user's subscription has renewed for the next billing period.
 * The {@link Entitlement#getEndsAt() endsAt} field will have an updated value with the new expiration date.
 *
 * <p><b>Notice</b><br>
 * If a user's subscription is cancelled, you will not receive an {@link EntitlementDeleteEvent EntitlementDeleteEvent}.
 * <br>Instead, you will simply not receive an {@link EntitlementUpdateEvent EntitlementUpdateEvent} with a new {@link Entitlement#getEndsAt() endsAt} date at the end of the billing period.
 *
 * @see #getEntitlement()
 */
public class EntitlementUpdateEvent extends GenericEntitlementEvent
{
    public EntitlementUpdateEvent(@Nonnull JDA api, long responseNumber, @Nonnull Entitlement entitlement)
    {
        super(api, responseNumber, entitlement);
    }
}
