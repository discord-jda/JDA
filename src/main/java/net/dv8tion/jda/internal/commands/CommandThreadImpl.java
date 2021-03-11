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

package net.dv8tion.jda.internal.commands;

import net.dv8tion.jda.api.commands.CommandThread;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.InteractionWebhookAction;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.TriggerRestAction;
import net.dv8tion.jda.internal.requests.restaction.WebhookMessageActionImpl;

import java.util.LinkedList;
import java.util.List;

public class CommandThreadImpl implements CommandThread
{
    private final SlashCommandEvent event;
    private final List<TriggerRestAction<?>> readyCallbacks = new LinkedList<>();
    private boolean isReady;

    public CommandThreadImpl(SlashCommandEvent event)
    {
        this.event = event;
    }

    public synchronized void ready()
    {
        this.isReady = true;
        readyCallbacks.forEach(TriggerRestAction::run);
    }

    private synchronized <T extends TriggerRestAction<R>, R> T onReady(T runnable)
    {
        if (isReady)
            runnable.run();
        else
            readyCallbacks.add(runnable);
        return runnable;
    }

    @Override
    public SlashCommandEvent getEvent()
    {
        return event;
    }

    @Override
    public InteractionWebhookAction sendMessage(String content)
    {
        Route.CompiledRoute route = Route.Interactions.CREATE_FOLLOWUP.compile(getJDA().getSelfUser().getApplicationId(), event.getInteractionToken());
        route = route.withQueryParams("wait", "true");
        return onReady(new WebhookMessageActionImpl(getJDA(), route)).setContent(content);
    }

    @Override
    public InteractionWebhookAction editOriginal(String content)
    {
        Route.CompiledRoute route = Route.Interactions.EDIT_FOLLOWUP.compile(getJDA().getSelfUser().getApplicationId(), event.getInteractionToken(), "@original");
        route = route.withQueryParams("wait", "true");
        return onReady(new WebhookMessageActionImpl(getJDA(), route)).setContent(content);
    }

    @Override
    public RestAction<Void> deleteOriginal()
    {
        Route.CompiledRoute route = Route.Interactions.DELETE_FOLLOWUP.compile(getJDA().getSelfUser().getApplicationId(), event.getInteractionToken(), "@original");
        return onReady(new TriggerRestAction<>(getJDA(), route));
    }
}
