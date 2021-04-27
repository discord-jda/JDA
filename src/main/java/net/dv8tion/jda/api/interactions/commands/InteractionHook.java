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

package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.WebhookClient;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.InteractionWebhookAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;

// this is used for followup responses on commands
public interface InteractionHook extends WebhookClient<InteractionWebhookAction>
{
    @Nonnull
    Interaction getInteraction();

    // Whether we should treat messages as ephemeral by default
    @Nonnull
    InteractionHook setEphemeral(boolean ephemeral);

    @Nonnull
    JDA getJDA();

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
    default InteractionWebhookAction editOriginal(@Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return editMessageById("@original", data, name, options);
    }

    @Nonnull
    @CheckReturnValue
    default InteractionWebhookAction editOriginal(@Nonnull File file, @Nonnull AttachmentOption... options)
    {
        return editMessageById("@original", file, options);
    }

    @Nonnull
    @CheckReturnValue
    default InteractionWebhookAction editOriginal(@Nonnull File file, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return editMessageById("@original", file, name, options);
    }

    @Nonnull
    @CheckReturnValue
    default InteractionWebhookAction editOriginal(@Nonnull byte[] data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return editMessageById("@original", data, name, options);
    }


    @Nonnull
    @CheckReturnValue
    default RestAction<Void> deleteOriginal()
    {
        return deleteMessageById("@original");
    }
}
