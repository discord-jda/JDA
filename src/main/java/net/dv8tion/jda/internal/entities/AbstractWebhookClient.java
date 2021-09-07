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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.WebhookClient;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ComponentLayout;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.WebhookMessageActionImpl;
import net.dv8tion.jda.internal.requests.restaction.WebhookMessageUpdateActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractWebhookClient<T> implements WebhookClient<T>
{
    protected final long id;
    protected final JDA api;
    protected String token;

    protected AbstractWebhookClient(long webhookId, String webhookToken, JDA api)
    {
        this.id = webhookId;
        this.token = webhookToken;
        this.api = api;
    }

    public abstract WebhookMessageActionImpl<T> sendRequest();
    public abstract WebhookMessageUpdateActionImpl<T> editRequest(String messageId);

    @NotNull
    @Override
    public WebhookMessageActionImpl<T> sendMessage(@NotNull String content)
    {
        return sendRequest().setContent(content);
    }

    @NotNull
    @Override
    public WebhookMessageActionImpl<T> sendMessageEmbeds(@NotNull Collection<? extends MessageEmbed> embeds)
    {
        return sendRequest().addEmbeds(embeds);
    }

    @NotNull
    @Override
    public WebhookMessageActionImpl<T> sendMessage(@NotNull Message message)
    {
        return sendRequest().applyMessage(message);
    }

    @NotNull
    @Override
    public WebhookMessageActionImpl<T> sendFile(@NotNull InputStream data, @NotNull String name, @NotNull AttachmentOption... options)
    {
        return sendRequest().addFile(data, name, options);
    }

    @NotNull
    @Override
    public WebhookMessageUpdateActionImpl<T> editMessageById(@NotNull String messageId, @NotNull String content)
    {
        return (WebhookMessageUpdateActionImpl<T>) editRequest(messageId).setContent(content);
    }

    @NotNull
    @Override
    public WebhookMessageUpdateAction<T> editMessageComponentsById(@NotNull String messageId, @NotNull Collection<? extends ComponentLayout> components)
    {
        Checks.noneNull(components, "Components");
        if (components.stream().anyMatch(x -> !(x instanceof ActionRow)))
            throw new UnsupportedOperationException("The provided component layout is not supported");
        List<ActionRow> actionRows = components.stream().map(ActionRow.class::cast).collect(Collectors.toList());
        return editRequest(messageId).setActionRows(actionRows);
    }

    @NotNull
    @Override
    public WebhookMessageUpdateActionImpl<T> editMessageEmbedsById(@NotNull String messageId, @NotNull Collection<? extends MessageEmbed> embeds)
    {
        return (WebhookMessageUpdateActionImpl<T>) editRequest(messageId).setEmbeds(embeds);
    }

    @NotNull
    @Override
    public WebhookMessageUpdateActionImpl<T> editMessageById(@NotNull String messageId, @NotNull Message message)
    {
        return (WebhookMessageUpdateActionImpl<T>) editRequest(messageId).applyMessage(message);
    }

    @NotNull
    @Override
    public WebhookMessageUpdateActionImpl<T> editMessageById(@NotNull String messageId, @NotNull InputStream data, @NotNull String name, @NotNull AttachmentOption... options)
    {
        return (WebhookMessageUpdateActionImpl<T>) editRequest(messageId).addFile(data, name, options);
    }

    @NotNull
    @Override
    public RestAction<Void> deleteMessageById(@NotNull String messageId)
    {
        Checks.isSnowflake(messageId);
        Route.CompiledRoute route = Route.Webhooks.EXECUTE_WEBHOOK_DELETE.compile(Long.toUnsignedString(id), token, messageId);
        return new RestActionImpl<>(api, route);
    }
}
