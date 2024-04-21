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
package net.dv8tion.jda.api.events.stage

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.StageInstance
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import javax.annotation.Nonnull

/**
 * Indicates that a [StageInstance][net.dv8tion.jda.api.entities.StageInstance] was created/deleted/changed.
 * <br></br>Every StageInstanceEvent is derived from this event and can be casted.
 *
 *
 * Can be used to detect any StageInstanceEvent.
 */
abstract class GenericStageInstanceEvent(
    @Nonnull api: JDA, responseNumber: Long,
    /**
     * The affected [StageInstance]
     *
     * @return The [StageInstance]
     */
    @get:Nonnull
    @param:Nonnull val instance: StageInstance
) : GenericGuildEvent(api, responseNumber, instance.guild) {

    @get:Nonnull
    val channel: StageChannel
        /**
         * The [StageChannel] this instance belongs to
         *
         * @return The StageChannel
         */
        get() = instance.channel
}
