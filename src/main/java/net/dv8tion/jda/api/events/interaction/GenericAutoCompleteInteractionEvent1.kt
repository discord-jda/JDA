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
package net.dv8tion.jda.api.events.interaction

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.interactions.callbacks.IAutoCompleteCallback
import net.dv8tion.jda.api.interactions.commands.*
import net.dv8tion.jda.api.requests.restaction.interactions.AutoCompleteCallbackAction
import javax.annotation.Nonnull

/**
 * Indicates that a user is typing in an auto-complete interactive field.
 *
 *
 * **Requirements**<br></br>
 * To receive these events, you must unset the **Interactions Endpoint URL** in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the [Discord Developers Portal](https://discord.com/developers/applications).
 *
 * @see IAutoCompleteCallback
 *
 * @see OptionData.setAutoComplete
 */
open class GenericAutoCompleteInteractionEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull interaction: Interaction
) : GenericInteractionCreateEvent(api, responseNumber, interaction), IAutoCompleteCallback {
    @Nonnull
    override fun getInteraction(): IAutoCompleteCallback? {
        return super.getInteraction() as IAutoCompleteCallback
    }

    @Nonnull
    override fun replyChoices(@Nonnull choices: Collection<Command.Choice>): AutoCompleteCallbackAction {
        return interaction!!.replyChoices(choices)
    }
}
