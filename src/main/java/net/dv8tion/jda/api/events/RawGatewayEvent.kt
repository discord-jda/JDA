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
import net.dv8tion.jda.api.utils.data.DataObject
import javax.annotation.Nonnull

/**
 * Wrapper for the raw dispatch event received from discord.
 * <br></br>This provides the raw structure of a gateway event through a [net.dv8tion.jda.api.utils.data.DataObject]
 * instance containing:
 *
 *  * d: The payload of the package (DataObject)
 *  * t: The type of the package (String)
 *  * op: The opcode of the package, always 0 for dispatch (int)
 *  * s: The sequence number, equivalent to [.getResponseNumber] (long)
 *
 *
 *
 * Sent after derived events. This is disabled by default and can be enabled through either
 * the [JDABuilder][net.dv8tion.jda.api.JDABuilder.setRawEventsEnabled]
 * or [DefaultShardManagerBuilder][net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder.setRawEventsEnabled].
 *
 * @see net.dv8tion.jda.api.JDABuilder.setRawEventsEnabled
 * @see net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder.setRawEventsEnabled
 * @see [Gateway Documentation](https://discord.com/developers/docs/topics/gateway)
 */
class RawGatewayEvent(
    @Nonnull api: JDA, responseNumber: Long,
    /**
     * The raw gateway package including sequence and type.
     *
     *
     *  * d: The payload of the package (DataObject)
     *  * t: The type of the package (String)
     *  * op: The opcode of the package, always 0 for dispatch (int)
     *  * s: The sequence number, equivalent to [.getResponseNumber] (long)
     *
     *
     * @return The data object
     */
    /**
     * The raw gateway package including sequence and type.
     *
     *
     *  * d: The payload of the package (DataObject)
     *  * t: The type of the package (String)
     *  * op: The opcode of the package, always 0 for dispatch (int)
     *  * s: The sequence number, equivalent to [.getResponseNumber] (long)
     *
     *
     * @return The data object
     */
    @param:Nonnull val `package`: DataObject
) : Event(api, responseNumber) {
    val payload: DataObject
        /**
         * The payload of the package.
         *
         * @return The payload as a [net.dv8tion.jda.api.utils.data.DataObject] instance
         */
        @Nonnull get() = `package`.getObject("d")
    val type: String
        /**
         * The type of event.
         *
         * @return The type of event.
         */
        @Nonnull get() = `package`.getString("t")
}
