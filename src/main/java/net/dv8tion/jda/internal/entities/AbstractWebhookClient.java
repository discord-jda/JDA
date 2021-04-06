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
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.io.InputStream;

@SuppressWarnings("unchecked")
public abstract class AbstractWebhookClient<T extends WebhookMessageAction> implements WebhookClient<T>
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

    public abstract T sendRequest();
    public abstract T editRequest(String messageId);

    @Nonnull
    @Override
    public T sendMessage(@Nonnull String content)
    {
        return (T) sendRequest().setContent(content);
    }

    @Nonnull
    @Override
    public T sendMessage(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds)
    {
        return (T) sendRequest().addEmbeds(embed, embeds);
    }

    @Nonnull
    @Override
    public T sendMessage(@Nonnull Message message)
    {
        return (T) sendRequest().applyMessage(message);
    }

    @Nonnull
    @Override
    public T sendFile(@Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return (T) sendRequest().addFile(name, data, options);
    }

    @Nonnull
    @Override
    public T editMessageById(@Nonnull String messageId, @Nonnull String content)
    {
        return (T) editRequest(messageId).setContent(content);
    }

    @Nonnull
    @Override
    public T editMessageById(@Nonnull String messageId, @Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds)
    {
        return (T) editRequest(messageId).addEmbeds(embed, embeds);
    }

    @Nonnull
    @Override
    public T editMessageById(@Nonnull String messageId, @Nonnull Message message)
    {
        return (T) editRequest(messageId).applyMessage(message);
    }

    @Nonnull
    @Override
    public T editMessageById(@Nonnull String messageId, @Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return (T) editRequest(messageId).addFile(name, data, options);
    }

    @Nonnull
    @Override
    public RestAction<Void> deleteMessageById(@Nonnull String messageId)
    {
        Checks.isSnowflake(messageId);
        Route.CompiledRoute route = Route.Webhooks.EXECUTE_WEBHOOK_DELETE.compile(Long.toUnsignedString(id), token, messageId);
        return new RestActionImpl<>(api, route);
    }
}
