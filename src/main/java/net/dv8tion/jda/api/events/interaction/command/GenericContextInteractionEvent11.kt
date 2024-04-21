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
package net.dv8tion.jda.api.events.interaction.command

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.context.ContextInteraction
import net.dv8tion.jda.api.interactions.commands.context.ContextInteraction.ContextTarget
import javax.annotation.Nonnull

/**
 * Indicates that a [ContextInteraction] was used.
 *
 *
 * **Requirements**<br></br>
 * To receive these events, you must unset the **Interactions Endpoint URL** in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the [Discord Developers Portal](https://discord.com/developers/applications).
 */
open class GenericContextInteractionEvent<T>(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull interaction: ContextInteraction<T>
) : GenericCommandInteractionEvent(api, responseNumber, interaction), ContextInteraction<T> {
    @Nonnull
    override fun getInteraction(): ContextInteraction<T>? {
        return super.getInteraction() as ContextInteraction<T>
    }

    @Nonnull
    override fun getTargetType(): ContextTarget {
        return interaction!!.getTargetType()
    }

    @Nonnull
    override fun getTarget(): T & Any {
        return interaction!!.getTarget()
    }
}
