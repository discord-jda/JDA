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
package net.dv8tion.jda.api.events.stage.update

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.StageInstance
import net.dv8tion.jda.api.events.UpdateEvent
import net.dv8tion.jda.api.events.stage.GenericStageInstanceEvent
import javax.annotation.Nonnull

/**
 * Indicates that a [StageInstance][net.dv8tion.jda.api.entities.StageInstance] was updated.
 * <br></br>Every StageInstanceUpdateEvent is derived from this event and can be casted.
 *
 *
 * Can be used to detect any StageInstanceUpdateEvent.
 */
abstract class GenericStageInstanceUpdateEvent<T>(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull stageInstance: StageInstance,
    protected val previous: T,
    protected val next: T,
    @get:Nonnull override val propertyIdentifier: String
) : GenericStageInstanceEvent(api, responseNumber, stageInstance), UpdateEvent<StageInstance?, T?> {

    @get:Nonnull
    override val entity: E
        get() = getInstance()
    override val oldValue: T?
        get() = previous
    override val newValue: T?
        get() = next
}
