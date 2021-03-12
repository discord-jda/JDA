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

import net.dv8tion.jda.api.commands.CommandThread;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public interface CommandReplyAction extends RestAction<CommandThread>, AllowedMentions<CommandReplyAction>
{
    @Nonnull
    @CheckReturnValue
    default CommandReplyAction addEmbeds(@Nonnull MessageEmbed... embeds)
    {
        Checks.noneNull(embeds, "MessageEmbed");
        return addEmbeds(Arrays.asList(embeds));
    }

    @Nonnull
    @CheckReturnValue
    CommandReplyAction addEmbeds(@Nonnull Collection<MessageEmbed> embeds);

    // doesn't support embeds or attachments
    @Nonnull
    @CheckReturnValue
    CommandReplyAction setEphemeral(boolean ephemeral);

    @Nonnull
    @CheckReturnValue
    default CommandReplyAction addFile(@Nonnull File file, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(file, "File");
        return addFile(file, file.getName(), options);
    }

    @Nonnull
    @CheckReturnValue
    default CommandReplyAction addFile(@Nonnull File file, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        try
        {
            Checks.notNull(file, "File");
            Checks.check(file.exists() && file.canRead(), "Provided file either does not exist or cannot be read from!");
            return addFile(new FileInputStream(file), name, options);
        }
        catch (FileNotFoundException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    @Nonnull
    @CheckReturnValue
    default CommandReplyAction addFile(@Nonnull byte[] data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(data, "Data");
        return addFile(new ByteArrayInputStream(data), name, options);
    }

    @Nonnull
    @CheckReturnValue
    CommandReplyAction addFile(@Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options);

    @Nonnull
    @Override
    CommandReplyAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    CommandReplyAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    CommandReplyAction deadline(long timestamp);

    @Nonnull
    CommandReplyAction setTTS(final boolean isTTS);

    @Nonnull
    CommandReplyAction setContent(@Nullable final String content);

    enum Flag
    {
        EPHEMERAL(6);

        private final int raw;

        Flag(int offset)
        {
            this.raw = 1 << offset;
        }

        public int getRaw()
        {
            return raw;
        }
    }

    enum ResponseType
    { // TODO: Write better docs
//        /**  */ Unused (this is only for HTTP webhooks)
//        PONG(1),
//        /** ACK a command without sending a message, eating the user's input */
//        ACKNOWLEDGE(2),
//        /** Respond with a message, eating the user's input */
//        CHANNEL_MESSAGE(3),
        /** Respond with a message, showing the user's input */
        CHANNEL_MESSAGE_WITH_SOURCE(4),
        /** ACK a command without sending a message, showing the user's input */
        DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE(5),
        ;
        private final int raw;

        ResponseType(int raw)
        {
            this.raw = raw;
        }

        public int getRaw()
        {
            return raw;
        }
    }
}
