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
package net.dv8tion.jda.api.events.interaction.component

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Mentions
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectInteraction
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu
import javax.annotation.Nonnull

/**
 * Indicates that a custom [EntitySelectMenu] on one of the bots messages was used by a user.
 *
 *
 * This fires when a user selects the options on one of the custom select menus attached to a bot or webhook message.
 * Use [.getMentions] or [.getValues] to handle the selected entities.
 *
 *
 * **Requirements**<br></br>
 * To receive these events, you must unset the **Interactions Endpoint URL** in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the [Discord Developers Portal](https://discord.com/developers/applications).
 *
 * @see StringSelectInteractionEvent
 */
class EntitySelectInteractionEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @param:Nonnull private val interaction: EntitySelectInteraction
) : GenericSelectMenuInteractionEvent<IMentionable?, EntitySelectMenu?>(api, responseNumber, interaction),
    EntitySelectInteraction {
    @Nonnull
    override fun getInteraction(): EntitySelectInteraction? {
        return this.interaction
    }

    @Nonnull
    override fun getMentions(): Mentions {
        return interaction.mentions
    }
}
