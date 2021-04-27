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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.ActionRow;
import net.dv8tion.jda.api.interactions.Component;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public interface WebhookMessageAction extends RestAction<Message>, AllowedMentions<WebhookMessageAction>
{
    @Nonnull
    @CheckReturnValue
    WebhookMessageAction setUsername(@Nullable String name);

    @Nonnull
    @CheckReturnValue
    WebhookMessageAction setAvatarUrl(@Nullable String iconUrl);

    @Nonnull
    @CheckReturnValue
    WebhookMessageAction setContent(@Nullable String content);

    @Nonnull
    @CheckReturnValue
    WebhookMessageAction setTTS(boolean tts);

    @Nonnull
    @CheckReturnValue
    WebhookMessageAction addEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds); // Doesn't work on ephemeral messages!

    @Nonnull
    @CheckReturnValue
    default WebhookMessageAction addEmbeds(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... other)
    {
        ArrayList<MessageEmbed> embeds = new ArrayList<>();
        embeds.add(embed);
        Collections.addAll(embeds, other);
        return addEmbeds(embeds);
    }

    @Nonnull
    @CheckReturnValue
    WebhookMessageAction addFile(@Nonnull String name, @Nonnull InputStream data, @Nonnull AttachmentOption... options);

    @Nonnull
    @CheckReturnValue
    default WebhookMessageAction addFile(@Nonnull String name, @Nonnull byte[] data, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(name, "Name");
        Checks.notNull(data, "Data");
        return addFile(name, new ByteArrayInputStream(data));
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageAction addFile(@Nonnull String name, @Nonnull File data, @Nonnull AttachmentOption... options)
    {
        Checks.notEmpty(name, "Name");
        Checks.notNull(data, "File");
        try
        {
            return addFile(name, new FileInputStream(data));
        }
        catch (FileNotFoundException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageAction addFile(@Nonnull File file, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(file, "File");
        return addFile(file.getName(), file);
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageAction addActionRow(@Nonnull Component... components)
    {
        return addActionRows(ActionRow.of(components));
    }

    @Nonnull
    @CheckReturnValue
    default WebhookMessageAction addActionRows(@Nonnull Collection<? extends ActionRow> rows)
    {
        Checks.noneNull(rows, "ActionRows");
        return addActionRows(rows.toArray(new ActionRow[0]));
    }

    @Nonnull
    @CheckReturnValue
    WebhookMessageAction addActionRows(@Nonnull ActionRow... rows);

    @Nonnull
    @CheckReturnValue
    WebhookMessageAction applyMessage(@Nonnull Message message);
}
