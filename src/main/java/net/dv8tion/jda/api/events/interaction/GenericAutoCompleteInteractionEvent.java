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

package net.dv8tion.jda.api.events.interaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.callbacks.IAutoCompleteCallback;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.interactions.AutoCompleteCallbackAction;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Indicates that a user is typing in an auto-complete interactive field.
 *
 * <h2>Requirements</h2>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://discord.com/developers/applications" target="_blank">Discord Developers Portal</a>.
 *
 * @see IAutoCompleteCallback
 * @see OptionData#setAutoComplete(boolean)
 */
public class GenericAutoCompleteInteractionEvent extends GenericInteractionCreateEvent implements IAutoCompleteCallback
{
    public GenericAutoCompleteInteractionEvent(@Nonnull JDA api, long responseNumber, @Nonnull Interaction interaction)
    {
        super(api, responseNumber, interaction);
    }

    @Nonnull
    @Override
    public IAutoCompleteCallback getInteraction()
    {
        return (IAutoCompleteCallback) super.getInteraction();
    }

    @Nonnull
    @Override
    public AutoCompleteCallbackAction replyChoices(@Nonnull Collection<Command.Choice> choices)
    {
        return getInteraction().replyChoices(choices);
    }
}
