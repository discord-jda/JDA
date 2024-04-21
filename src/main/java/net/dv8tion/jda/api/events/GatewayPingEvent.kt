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
package net.dv8tion.jda.api.events

import net.dv8tion.jda.api.JDA
import javax.annotation.Nonnull

/**
 * Indicates that the gateway ping has been updated by the heartbeat cycle.
 * <br></br>You can always get the last ping update with [net.dv8tion.jda.api.JDA.getGatewayPing].
 *
 *
 * Can be used to detect changes to the gateway ping.
 *
 *
 * Identifier: `gateway-ping`
 */
class GatewayPingEvent(
    @Nonnull api: JDA,
    /**
     * The previous ping for the current JDA session
     *
     * @return The previous ping in milliseconds, or -1 if no ping was available yet
     */
    override val oldValue: Long
) : Event(api), UpdateEvent<JDA, Long> {
    /**
     * The new ping for the current JDA session
     *
     * @return The new ping in milliseconds
     */
    @get:Nonnull
    override val newValue: Long
        @Nonnull get() = field

    init {
        newValue = api.gatewayPing
    }

    override val entity: E
        @Nonnull get() = getJDA()

    companion object {
        val propertyIdentifier = "gateway-ping"
            @Nonnull get() = Companion.field
    }
}
