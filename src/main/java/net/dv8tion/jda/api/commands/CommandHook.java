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

package net.dv8tion.jda.api.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.InteractionWebhookAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.*;

// this is used for followup responses on commands
public interface CommandHook
{
    @Nonnull
    SlashCommandEvent getEvent();

    // Whether we should treat messages as ephemeral by default
    @Nonnull
    CommandHook setEphemeral(boolean ephemeral);

    @Nonnull
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

    @Nonnull
    @CheckReturnValue
    InteractionWebhookAction sendMessage(@Nonnull String content);

    @Nonnull
    @CheckReturnValue
    InteractionWebhookAction sendMessage(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds);

    @Nonnull
    @CheckReturnValue
    InteractionWebhookAction sendMessage(@Nonnull Message message);

    @Nonnull
    @CheckReturnValue
    InteractionWebhookAction sendFile(@Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options);

    @Nonnull
    @CheckReturnValue
    default InteractionWebhookAction sendMessageFormat(@Nonnull String format, @Nonnull Object... args)
    {
        Checks.notNull(format, "Format String");
        return sendMessage(String.format(format, args));
    }

    @Nonnull
    @CheckReturnValue
    default InteractionWebhookAction sendFile(@Nonnull File file, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(file, "File");
        return sendFile(file, file.getName(), options);
    }

    @Nonnull
    @CheckReturnValue
    default InteractionWebhookAction sendFile(@Nonnull File file, @Nonnull String name, @Nonnull AttachmentOption... options)
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
    default InteractionWebhookAction sendFile(@Nonnull byte[] data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(data, "Data");
        Checks.notNull(name, "Name");

        return sendFile(new ByteArrayInputStream(data), name, options);
    }


    @Nonnull
    @CheckReturnValue
    InteractionWebhookAction editMessageById(@Nonnull String messageId, @Nonnull String content);

    @Nonnull
    @CheckReturnValue
    default InteractionWebhookAction editMessageById(long messageId, @Nonnull String content)
    {
        return editMessageById(Long.toUnsignedString(messageId), content);
    }

    @Nonnull
    @CheckReturnValue
    InteractionWebhookAction editMessageById(@Nonnull String messageId, @Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds);

    @Nonnull
    @CheckReturnValue
    default InteractionWebhookAction editMessageById(long messageId, @Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds)
    {
        return editMessageById(Long.toUnsignedString(messageId), embed, embeds);
    }

    @Nonnull
    @CheckReturnValue
    InteractionWebhookAction editMessageById(@Nonnull String messageId, @Nonnull Message message);

    @Nonnull
    @CheckReturnValue
    default InteractionWebhookAction editMessageById(long messageId, Message message)
    {
        return editMessageById(Long.toUnsignedString(messageId), message);
    }

    @Nonnull
    @CheckReturnValue
    default InteractionWebhookAction editMessageFormatById(@Nonnull String messageId, @Nonnull String format, @Nonnull Object... args)
    {
        Checks.notNull(format, "Format String");
        return editMessageById(messageId, String.format(format, args));
    }

    @Nonnull
    @CheckReturnValue
    default InteractionWebhookAction editMessageFormatById(long messageId, @Nonnull String format, @Nonnull Object... args)
    {
        return editMessageFormatById(Long.toUnsignedString(messageId), format, args);
    }

    @Nonnull
    @CheckReturnValue
    default InteractionWebhookAction editOriginal(@Nonnull String content)
    {
        return editMessageById("@original", content);
    }

    @Nonnull
    @CheckReturnValue
    default InteractionWebhookAction editOriginal(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds)
    {
        return editMessageById("@original", embed, embeds);
    }

    @Nonnull
    @CheckReturnValue
    default InteractionWebhookAction editOriginal(@Nonnull Message message)
    {
        return editMessageById("@original", message);
    }

    @Nonnull
    @CheckReturnValue
    default InteractionWebhookAction editOriginalFormat(@Nonnull String format, @Nonnull Object... args)
    {
        Checks.notNull(format, "Format String");
        return editOriginal(String.format(format, args));
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

    @Nonnull
    @CheckReturnValue
    default RestAction<Void> deleteOriginal()
    {
        return deleteMessageById("@original");
    }
}
