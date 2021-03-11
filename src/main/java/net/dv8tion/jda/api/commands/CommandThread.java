/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.InteractionWebhookAction;

import javax.annotation.Nonnull;

// this is used for followup responses on commands
public interface CommandThread
{
    SlashCommandEvent getEvent();

    default JDA getJDA()
    {
        return getEvent().getJDA();
    }

    default long getInteractionIdLong()
    {
        return getEvent().getInteractionIdLong();
    }

    @Nonnull
    default String getInteractionId()
    {
        return Long.toUnsignedString(getInteractionIdLong());
    }

    @Nonnull
    default String getInteractionToken()
    {
        return getEvent().getInteractionToken();
    }

    InteractionWebhookAction sendMessage(String content); // TODO: Other send overloads
    // TODO: Edit followup messages
    InteractionWebhookAction editOriginal(String content); // doesn't work with ephemeral messages
    // TODO: Delete followup messages
    RestAction<Void> deleteOriginal(); // doesn't work with ephemeral messages
}
