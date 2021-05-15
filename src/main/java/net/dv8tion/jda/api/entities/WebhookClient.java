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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.interactions.ComponentLayout;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.*;
import java.util.Collection;

public interface WebhookClient
{
    @Nonnull
    @CheckReturnValue
    WebhookMessageAction sendMessage(@Nonnull String content);

    @Nonnull
    @CheckReturnValue
    WebhookMessageAction sendMessage(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds); // TODO: Collection<MessageEmbed> overload

    @Nonnull
    @CheckReturnValue
    WebhookMessageAction sendMessage(@Nonnull Message message);

    @Nonnull
    @CheckReturnValue
    default WebhookMessageAction sendMessageFormat(@Nonnull String format, @Nonnull Object... args)
    {
        Checks.notNull(format, "Format String");
        return sendMessage(String.format(format, args));
    }


    @Nonnull
    @CheckReturnValue
    WebhookMessageAction sendFile(@Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options);

    @Nonnull
    @CheckReturnValue
    default WebhookMessageAction sendFile(@Nonnull File file, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(file, "File");
        return sendFile(file, file.getName(), options);
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageAction sendFile(@Nonnull File file, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(file, "File");
        Checks.check(file.exists() && file.canRead(),
                "Provided file doesn't exist or cannot be read!");
        Checks.notNull(name, "Name");

        try
        {
            return sendFile(new FileInputStream(file), name, options);
        }
        catch (FileNotFoundException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageAction sendFile(@Nonnull byte[] data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(data, "Data");
        Checks.notNull(name, "Name");

        return sendFile(new ByteArrayInputStream(data), name, options);
    }

    // TODO: These should be specific update actions similar to ComponentInteraction#editMessage

    @Nonnull
    @CheckReturnValue
    WebhookMessageUpdateAction editMessageById(@Nonnull String messageId, @Nonnull String content);

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editMessageById(long messageId, @Nonnull String content)
    {
        return editMessageById(Long.toUnsignedString(messageId), content);
    }

    @Nonnull
    @CheckReturnValue
    WebhookMessageUpdateAction editMessageById(@Nonnull String messageId, @Nonnull Collection<? extends ComponentLayout> components); // We use ComponentLayout for forward compatibility here

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editMessageById(long messageId, @Nonnull Collection<? extends ComponentLayout> components)
    {
        return editMessageById(Long.toUnsignedString(messageId), components);
    }

    @Nonnull
    @CheckReturnValue
    WebhookMessageUpdateAction editMessageById(@Nonnull String messageId, @Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds);

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editMessageById(long messageId, @Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds)
    {
        return editMessageById(Long.toUnsignedString(messageId), embed, embeds);
    }

    @Nonnull
    @CheckReturnValue
    WebhookMessageUpdateAction editMessageById(@Nonnull String messageId, @Nonnull Message message);

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editMessageById(long messageId, Message message)
    {
        return editMessageById(Long.toUnsignedString(messageId), message);
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editMessageFormatById(@Nonnull String messageId, @Nonnull String format, @Nonnull Object... args)
    {
        Checks.notNull(format, "Format String");
        return editMessageById(messageId, String.format(format, args));
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editMessageFormatById(long messageId, @Nonnull String format, @Nonnull Object... args)
    {
        return editMessageFormatById(Long.toUnsignedString(messageId), format, args);
    }


    // NOTE: These only add files to the message, I believe you can't actually edit or delete any

    @Nonnull
    @CheckReturnValue
    WebhookMessageUpdateAction editMessageById(@Nonnull String messageId, @Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options);

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editMessageById(@Nonnull String messageId, @Nonnull File file, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(file, "File");
        return editMessageById(messageId, file, file.getName(), options);
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editMessageById(@Nonnull String messageId, @Nonnull File file, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(file, "File");
        Checks.check(file.exists() && file.canRead(),
                "Provided file doesn't exist or cannot be read!");
        Checks.notNull(name, "Name");

        try
        {
            return editMessageById(messageId, new FileInputStream(file), name, options);
        }
        catch (FileNotFoundException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editMessageById(@Nonnull String messageId, @Nonnull byte[] data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(data, "Data");
        Checks.notNull(name, "Name");

        return editMessageById(messageId, new ByteArrayInputStream(data), name, options);
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editMessageById(long messageId, @Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return editMessageById(Long.toUnsignedString(messageId), data, name, options);
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editMessageById(long messageId, @Nonnull File file, @Nonnull AttachmentOption... options)
    {
        return editMessageById(Long.toUnsignedString(messageId), file, options);
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editMessageById(long messageId, @Nonnull File file, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return editMessageById(Long.toUnsignedString(messageId), file, name, options);
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editMessageById(long messageId, @Nonnull byte[] data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return editMessageById(Long.toUnsignedString(messageId), data, name, options);
    }


    @Nonnull
    @CheckReturnValue
    RestAction<Void> deleteMessageById(@Nonnull String messageId);

    @Nonnull
    @CheckReturnValue
    default RestAction<Void> deleteMessageById(long messageId)
    {
        return deleteMessageById(Long.toUnsignedString(messageId));
    }

//    @Nonnull
//    static WebhookClient<WebhookMessageAction> createClient(@Nonnull JDA api, @Nonnull String url)
//    {
//        Checks.notNull(url, "URL");
//        Matcher matcher = Webhook.WEBHOOK_URL.matcher(url);
//        if (!matcher.matches())
//            throw new IllegalArgumentException("Provided invalid webhook URL");
//        String id = matcher.group(1);
//        String token = matcher.group(2);
//        return createClient(api, id, token);
//    }
//
//    @Nonnull
//    static WebhookClient<WebhookMessageAction> createClient(@Nonnull JDA api, @Nonnull String webhookId, @Nonnull String webhookToken)
//    {
//        Checks.notNull(api, "JDA");
//        Checks.notBlank(webhookToken, "Token");
//        return new AbstractWebhookClient<WebhookMessageAction>(MiscUtil.parseSnowflake(webhookId), webhookToken, api)
//        {
//            @Override
//            public WebhookMessageAction sendRequest()
//            {
//                Route.CompiledRoute route = Route.Webhooks.EXECUTE_WEBHOOK.compile(webhookId, webhookToken);
//                route = route.withQueryParams("wait", "true");
//                WebhookMessageActionImpl action = new WebhookMessageActionImpl(api, route);
//                action.run();
//                return action;
//            }
//
//            @Override
//            public WebhookMessageAction editRequest(String messageId)
//            {
//                Checks.isSnowflake(messageId);
//                Route.CompiledRoute route = Route.Webhooks.EXECUTE_WEBHOOK_EDIT.compile(webhookId, webhookToken, messageId);
//                route = route.withQueryParams("wait", "true");
//                WebhookMessageActionImpl action = new WebhookMessageActionImpl(api, route);
//                action.run();
//                return action;
//            }
//        };
//    }
}
