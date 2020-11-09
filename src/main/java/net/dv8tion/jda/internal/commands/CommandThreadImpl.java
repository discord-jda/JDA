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
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.InteractionWebhookAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.WebhookMessageActionImpl;

public class CommandThreadImpl implements CommandThread
{
    private final SlashCommandEvent event;

    public CommandThreadImpl(SlashCommandEvent event)
    {
        this.event = event;
    }

    @Override
    public SlashCommandEvent getEvent()
    {
        return event;
    }

    @Override
    public Message getOriginalMessage()
    {
        return null; // TODO: Handle this
    }

    @Override
    public InteractionWebhookAction sendMessage(String content)
    {
        Route.CompiledRoute route = Route.Webhooks.EXECUTE_WEBHOOK.compile(getJDA().getSelfUser().getApplicationId(), event.getInteractionToken());
        route = route.withQueryParams("wait", "true");
        return new WebhookMessageActionImpl(getJDA(), route).setContent(content);
    }

    @Override
    public InteractionWebhookAction editMessage(String content)
    {
        Route.CompiledRoute route = Route.Webhooks.EXECUTE_WEBHOOK_EDIT.compile(getJDA().getSelfUser().getApplicationId(), event.getInteractionToken(), "@original");
        route = route.withQueryParams("wait", "true");
        return new WebhookMessageActionImpl(getJDA(), route).setContent(content);
    }

    @Override
    public RestAction<Void> deleteMessage()
    {
        Route.CompiledRoute route = Route.Webhooks.EXECUTE_WEBHOOK_DELETE.compile(getJDA().getSelfUser().getApplicationId(), event.getInteractionToken(), "@original");
        return new RestActionImpl<>(getJDA(), route);
    }
}
