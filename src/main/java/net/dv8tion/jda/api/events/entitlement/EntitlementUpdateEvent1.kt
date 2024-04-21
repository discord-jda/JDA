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
package net.dv8tion.jda.api.events.entitlement

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Entitlement
import javax.annotation.Nonnull

/**
 * Indicates an [Entitlement] has renewed for the next billing period.
 * The [timeEnding][Entitlement.getTimeEnding] will have an updated value with the new expiration date.
 *
 *
 * **Notice**<br></br>
 * The [timeEnding][Entitlement.getTimeEnding] is updated for active subscriptions at the end of every billing period to
 * indicate renewal. When an [Entitlement] has not been renewed, Discord will indicate this by not emitting
 * an [EntitlementUpdateEvent] with the new [timeEnding][Entitlement.getTimeEnding] date
 *
 * @see .getEntitlement
 */
class EntitlementUpdateEvent(@Nonnull api: JDA, responseNumber: Long, @Nonnull entitlement: Entitlement?) :
    GenericEntitlementEvent(api, responseNumber, entitlement)
