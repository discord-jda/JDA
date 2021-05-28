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

package net.dv8tion.jda.api.requests.restaction.interactions;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * A {@link CallbackAction} which can be used to send a message reply for an interaction.
 * <br>You can use {@link #setEphemeral(boolean)} to hide this message from other users.
 */
public interface ReplyAction extends CallbackAction, AllowedMentions<ReplyAction>
{
    @Nonnull
    @Override
    ReplyAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    ReplyAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    ReplyAction deadline(long timestamp);

    /**
     * Add {@link MessageEmbed MessageEmbeds} for the message
     *
     * @param  embeds
     *         The message embeds to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or one of the embeds is too big
     *
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default ReplyAction addEmbeds(@Nonnull MessageEmbed... embeds)
    {
        Checks.noneNull(embeds, "MessageEmbed");
        return addEmbeds(Arrays.asList(embeds));
    }

    /**
     * Add {@link MessageEmbed MessageEmbeds} for the message
     *
     * @param  embeds
     *         The message embeds to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or one of the embeds is too big
     *
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ReplyAction addEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds);

    /**
     * Add a single {@link ActionRow} to the message.
     *
     * @param  components
     *         The components for this action row
     *
     * @throws IllegalArgumentException
     *         If null is provided or more than 5 components are provided
     *
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default ReplyAction addActionRow(@Nonnull Component... components)
    {
        return addActionRows(ActionRow.of(components));
    }

    /**
     * Add {@link ActionRow ActionRows} to the message.
     *
     * @param  rows
     *         The action rows to add
     *
     * @throws IllegalArgumentException
     *         If null is provided or more than 5 action rows are provided
     *
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default ReplyAction addActionRows(@Nonnull Collection<? extends ActionRow> rows)
    {
        Checks.noneNull(rows, "ActionRows");
        return addActionRows(rows.toArray(new ActionRow[0]));
    }

    /**
     * Add {@link ActionRow ActionRows} to the message.
     *
     * @param  rows
     *         The action rows to add
     *
     * @throws IllegalArgumentException
     *         If null is provided or more than 5 action rows are provided
     *
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ReplyAction addActionRows(@Nonnull ActionRow... rows);

    /**
     * Set the content for this message.
     *
     * @param  content
     *         The new message content or null to unset
     *
     * @throws IllegalArgumentException
     *         If the provided content is longer than {@link net.dv8tion.jda.api.entities.Message#MAX_CONTENT_LENGTH MAX_CONTENT_LENGTH} characters
     *
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    ReplyAction setContent(@Nullable final String content);

    /**
     * Enable/Disable Text-To-Speech for the resulting message.
     *
     * @param  isTTS
     *         True, if this should cause a Text-To-Speech effect when sent to the channel
     *
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    ReplyAction setTTS(final boolean isTTS);

    /**
     * Set whether this message should be visible to other users.
     * <br>When a message is ephemeral, it will only be visible to the user that used the interaction.
     *
     * <p>Ephemeral messages have some limitations and will be removed once the user restarts their client.
     * <br>Limitations:
     * <ul>
     *     <li>Cannot be deleted by the bot</li>
     *     <li>Cannot contain any files/attachments</li>
     *     <li>Cannot be reacted to</li>
     *     <li>Cannot be retrieved</li>
     * </ul>
     *
     * @param  ephemeral
     *         True, if this message should be invisible for other users
     *
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ReplyAction setEphemeral(boolean ephemeral);

    // Currently not supported, sad face
//    @Nonnull
//    @CheckReturnValue
//    default CommandReplyAction addFile(@Nonnull File file, @Nonnull AttachmentOption... options)
//    {
//        Checks.notNull(file, "File");
//        return addFile(file, file.getName(), options);
//    }
//
//    @Nonnull
//    @CheckReturnValue
//    default CommandReplyAction addFile(@Nonnull File file, @Nonnull String name, @Nonnull AttachmentOption... options)
//    {
//        try
//        {
//            Checks.notNull(file, "File");
//            Checks.check(file.exists() && file.canRead(), "Provided file either does not exist or cannot be read from!");
//            return addFile(new FileInputStream(file), name, options);
//        }
//        catch (FileNotFoundException e)
//        {
//            throw new IllegalArgumentException(e);
//        }
//    }
//
//    @Nonnull
//    @CheckReturnValue
//    default CommandReplyAction addFile(@Nonnull byte[] data, @Nonnull String name, @Nonnull AttachmentOption... options)
//    {
//        Checks.notNull(data, "Data");
//        return addFile(new ByteArrayInputStream(data), name, options);
//    }
//
//    @Nonnull
//    @CheckReturnValue
//    CommandReplyAction addFile(@Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options);

//    enum Flag
//    {
//        EPHEMERAL(6);
//
//        private final int raw;
//
//        Flag(int offset)
//        {
//            this.raw = 1 << offset;
//        }
//
//        public int getRaw()
//        {
//            return raw;
//        }
//    }
}
