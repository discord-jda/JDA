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

package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.WebhookClient;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;

/**
 * Webhook API for an interaction. Valid for up to 15 minutes after the interaction.
 * <br>This can be used to send followup messages or edit the original message of an interaction.
 *
 * <p>The interaction has to be acknowledged before any of these actions can be performed.
 * You need to call one of {@link Interaction#deferReply() deferReply()}, {@link Interaction#reply(String) reply(...)}, {@link ComponentInteraction#deferEdit() deferEdit()}, or {@link ComponentInteraction#editMessage(String) editMessage(...)} first.
 *
 * <p>When {@link Interaction#deferReply()} is used, the first message will act identically to {@link #editOriginal(String) editOriginal(...)}.
 * This means that you cannot make your deferred reply ephemeral through this interaction hook. You need to specify whether your reply is ephemeral or not directly in {@link Interaction#deferReply(boolean) deferReply(boolean)}.
 *
 * @see #editOriginal(String)
 * @see #deleteOriginal()
 * @see #sendMessage(String)
 */
// this is used for followup responses on commands
public interface InteractionHook extends WebhookClient
{
    /**
     * The interaction attached to this hook.
     *
     * @return The {@link Interaction}
     */
    @Nonnull
    Interaction getInteraction();

    /**
     * Whether messages sent from this interaction hook should be ephemeral by default.
     * <br>This does not affect message updates, including deferred replies sent with {@link #sendMessage(String) sendMessage(...)} methods.
     *
     * @param  ephemeral
     *         True if messages should be ephemeral
     *
     * @return The same interaction hook instance
     */
    @Nonnull
    InteractionHook setEphemeral(boolean ephemeral);

    /**
     * The JDA instance for this interaction
     *
     * @return The JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * Retrieves the original reply to this interaction.
     * <br>This doesn't work for ephemeral messages and will always cause an unknown message error response.
     *
     * @return {@link RestAction} - Type: {@link Message}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Message> retrieveOriginal();

    // TODO: Docs
    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editOriginal(@Nonnull String content)
    {
        return editMessageById("@original", content);
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editOriginal(@Nonnull Collection<? extends ComponentLayout> components)
    {
        return editMessageById("@original", components);
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editOriginal(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds)
    {
        return editMessageById("@original", embed, embeds);
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editOriginal(@Nonnull Message message)
    {
        return editMessageById("@original", message);
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editOriginalFormat(@Nonnull String format, @Nonnull Object... args)
    {
        Checks.notNull(format, "Format String");
        return editOriginal(String.format(format, args));
    }


    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editOriginal(@Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return editMessageById("@original", data, name, options);
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editOriginal(@Nonnull File file, @Nonnull AttachmentOption... options)
    {
        return editMessageById("@original", file, options);
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editOriginal(@Nonnull File file, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return editMessageById("@original", file, name, options);
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageUpdateAction editOriginal(@Nonnull byte[] data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return editMessageById("@original", data, name, options);
    }

    /**
     * Delete the original reply.
     * <br>This doesn't work for ephemeral messages.
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> deleteOriginal()
    {
        return deleteMessageById("@original");
    }
}
