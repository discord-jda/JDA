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
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.WebhookClient;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageDeleteAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.api.utils.messages.MessagePollData;
import net.dv8tion.jda.internal.requests.restaction.WebhookMessageCreateActionImpl;
import net.dv8tion.jda.internal.requests.restaction.WebhookMessageDeleteActionImpl;
import net.dv8tion.jda.internal.requests.restaction.WebhookMessageEditActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Collection;

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

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public String getToken()
    {
        return token;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    public abstract WebhookMessageCreateActionImpl<T> sendRequest();
    public abstract WebhookMessageEditActionImpl<T> editRequest(String messageId);

    @Nonnull
    @Override
    public WebhookMessageCreateAction<T> sendMessage(@Nonnull String content)
    {
        return sendRequest().setContent(content);
    }

    @Nonnull
    @Override
    public WebhookMessageCreateAction<T> sendMessageEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        return sendRequest().addEmbeds(embeds);
    }

    @Nonnull
    @Override
    public WebhookMessageCreateAction<T> sendMessageComponents(@Nonnull Collection<? extends MessageTopLevelComponent> components)
    {
        return sendRequest().setComponents(components);
    }

    @Nonnull
    @Override
    public WebhookMessageCreateAction<T> sendMessage(@Nonnull MessageCreateData message)
    {
        return sendRequest().applyData(message);
    }

    @Nonnull
    @Override
    public WebhookMessageCreateAction<T> sendMessagePoll(@Nonnull MessagePollData poll)
    {
        Checks.notNull(poll, "Message Poll");
        return sendRequest().setPoll(poll);
    }

    @Nonnull
    @Override
    public WebhookMessageCreateAction<T> sendFiles(@Nonnull Collection<? extends FileUpload> files)
    {
        return sendRequest().addFiles(files);
    }

    @Nonnull
    @Override
    public WebhookMessageEditActionImpl<T> editMessageById(@Nonnull String messageId, @Nonnull String content)
    {
        return (WebhookMessageEditActionImpl<T>) editRequest(messageId).setContent(content);
    }

    @Nonnull
    @Override
    public WebhookMessageEditAction<T> editMessageComponentsById(@Nonnull String messageId, @Nonnull Collection<? extends MessageTopLevelComponent> components)
    {
        Checks.noneNull(components, "Components");
        return editRequest(messageId).setComponents(components);
    }

    @Nonnull
    @Override
    public WebhookMessageEditActionImpl<T> editMessageEmbedsById(@Nonnull String messageId, @Nonnull Collection<? extends MessageEmbed> embeds)
    {
        return (WebhookMessageEditActionImpl<T>) editRequest(messageId).setEmbeds(embeds);
    }

    @Nonnull
    @Override
    public WebhookMessageEditActionImpl<T> editMessageById(@Nonnull String messageId, @Nonnull MessageEditData message)
    {
        return (WebhookMessageEditActionImpl<T>) editRequest(messageId).applyData(message);
    }

    @Nonnull
    @Override
    public WebhookMessageEditActionImpl<T> editMessageAttachmentsById(@Nonnull String messageId, @Nonnull Collection<? extends AttachedFile> attachments)
    {
        return (WebhookMessageEditActionImpl<T>) editRequest(messageId).setAttachments(attachments);
    }

    @Nonnull
    @Override
    public WebhookMessageDeleteAction deleteMessageById(@Nonnull String messageId)
    {
        if (!"@original".equals(messageId))
            Checks.isSnowflake(messageId);
        Route.CompiledRoute route = Route.Webhooks.EXECUTE_WEBHOOK_DELETE.compile(Long.toUnsignedString(id), token, messageId);
        return new WebhookMessageDeleteActionImpl(api, route);
    }
}
