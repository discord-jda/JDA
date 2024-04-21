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
 * Indicates that our [Status][net.dv8tion.jda.api.JDA.Status] changed. (Example: SHUTTING_DOWN -&gt; SHUTDOWN)
 *
 * <br></br>Can be used to detect internal status changes. Possibly to log or forward on user's end.
 *
 *
 * Identifier: `status`
 */
class StatusChangeEvent(
    @Nonnull api: JDA,
    /**
     * The status that we changed to
     *
     * @return The new status
     */
    /**
     * The status that we changed to
     *
     * @return The new status
     */
    @param:Nonnull override val newValue: JDA.Status,
    /**
     * The previous status
     *
     * @return The previous status
     */
    /**
     * The previous status
     *
     * @return The previous status
     */
    @param:Nonnull override val oldValue: JDA.Status
) : Event(api), UpdateEvent<JDA, JDA.Status> {
    @Nonnull public get()
    {
        return field
    }
    @Nonnull public get()
    {
        return field
    }

    override val entity: E
        @Nonnull get() = getJDA()

    companion object {
        val propertyIdentifier = "status"
            @Nonnull get() = Companion.field
    }
}
