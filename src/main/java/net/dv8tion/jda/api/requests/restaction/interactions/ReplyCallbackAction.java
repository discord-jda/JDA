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
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
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

/**
 * A {@link InteractionCallbackAction} which can be used to send a message reply for an interaction.
 * <br>You can use {@link #setEphemeral(boolean)} to hide this message from other users.
 */
public interface ReplyCallbackAction extends InteractionCallbackAction<InteractionHook>, AllowedMentions<ReplyCallbackAction>
{
    @Nonnull
    @Override
    ReplyCallbackAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    ReplyCallbackAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    ReplyCallbackAction deadline(long timestamp);

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
    default ReplyCallbackAction addEmbeds(@Nonnull MessageEmbed... embeds)
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
    ReplyCallbackAction addEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds);

    /**
     * Add a single {@link ActionRow} to the message.
     *
     * @param  components
     *         The components for this action row
     *
     * @throws IllegalArgumentException
     *         If null is provided, an invalid number of components is provided,
     *         or any custom {@link ActionComponent#getId() id} is duplicated
     *
     * @return The same reply action, for chaining convenience
     *
     * @see    ActionRow#of(ItemComponent...)
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction addActionRow(@Nonnull ItemComponent... components)
    {
        return addActionRows(ActionRow.of(components));
    }

    /**
     * Add a single {@link ActionRow} to the message.
     *
     * @param  components
     *         The components for this action row
     *
     * @throws IllegalArgumentException
     *         If null is provided, an invalid number of components is provided,
     *         or any custom {@link ActionComponent#getId() id} is duplicated
     *
     * @return The same reply action, for chaining convenience
     *
     * @see    ActionRow#of(Collection)
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction addActionRow(@Nonnull Collection<? extends ItemComponent> components)
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
     *         If null is provided, more than 5 action rows are provided,
     *         or any custom {@link ActionComponent#getId() id} is duplicated
     *
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction addActionRows(@Nonnull Collection<? extends ActionRow> rows)
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
     *         If null is provided, more than 5 action rows are provided,
     *         or any custom {@link ActionComponent#getId() id} is duplicated
     *
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ReplyCallbackAction addActionRows(@Nonnull ActionRow... rows);

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
    ReplyCallbackAction setContent(@Nullable final String content);

    /**
     * Enable/Disable Text-To-Speech for the resulting message.
     *
     * @param  isTTS
     *         True, if this should cause a Text-To-Speech effect when sent to the channel
     *
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    ReplyCallbackAction setTTS(final boolean isTTS);

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
    ReplyCallbackAction setEphemeral(boolean ephemeral);


    /**
     * Adds the provided {@link File}.
     * <br><u>The stream will be closed upon execution!</u>
     * <br>The provided file will be appended to the message.
     *
     * @param  file
     *         The {@link File} data to upload in response to the interaction.
     * @param  options
     *         Possible options to apply to this attachment, such as marking it as spoiler image
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided file is {@code null}.
     *
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction addFile(@Nonnull File file, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(file, "File");
        return addFile(file, file.getName(), options);
    }

    /**
     * Adds the provided {@link File}.
     * <br><u>The stream will be closed upon execution!</u>
     * <br>The provided file will be appended to the message.
     *
     * <p>The {@code name} parameter is used to inform Discord about what the file should be called. This is 2 fold:
     * <ol>
     *     <li>The file name provided is the name that is found in {@link net.dv8tion.jda.api.entities.Message.Attachment#getFileName()}
     *          after upload and it is the name that will show up in the client when the upload is displayed.
     *     <br>Note: The fileName does not show up on the Desktop client for images. It does on mobile however.</li>
     *     <li>The extension of the provided fileName also determines how Discord will treat the file. Discord currently only
     *         has special handling for image file types, but the fileName's extension must indicate that it is an image file.
     *         This means it has to end in something like .png, .jpg, .jpeg, .gif, etc. As a note, you can also not provide
     *         a full name for the file and instead ONLY provide the extension like "png" or "gif" and Discord will generate
     *         a name for the upload and append the fileName as the extension.</li>
     * </ol>
     *
     * @param  file
     *         The {@link File} data to upload in response to the interaction.
     * @param  name
     *         The file name that should be sent to discord
     * @param  options
     *         Possible options to apply to this attachment, such as marking it as spoiler image
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided file or filename is {@code null}.
     *
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction addFile(@Nonnull File file, @Nonnull String name, @Nonnull AttachmentOption... options)
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

    /**
     * Adds the provided {@code byte[]} as file data.
     * <br><u>The stream will be closed upon execution!</u>
     * <br>The provided file will be appended to the message.
     *
     * @param  data
     *         The {@code byte[]} data to upload in response to the interaction.
     * @param  name
     *         The file name that should be sent to discord
     *         <br>Refer to the documentation for {@link #addFile(java.io.File, String, AttachmentOption...)} for information about this parameter.
     * @param  options
     *         Possible options to apply to this attachment, such as marking it as spoiler image
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided data or filename is {@code null}.
     *
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction addFile(@Nonnull byte[] data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(data, "Data");
        return addFile(new ByteArrayInputStream(data), name, options);
    }

    /**
     * Adds the provided {@link java.io.InputStream InputStream} as file data.
     * <br><u>The stream will be closed upon execution!</u>
     * <br>The provided file will be appended to the message.
     *
     * @param  data
     *         The InputStream data to upload in response to the interaction.
     * @param  name
     *         The file name that should be sent to discord
     *         <br>Refer to the documentation for {@link #addFile(java.io.File, String, AttachmentOption...)} for information about this parameter.
     * @param  options
     *         Possible options to apply to this attachment, such as marking it as spoiler image
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided data or filename is {@code null}.
     *
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ReplyCallbackAction addFile(@Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options);
}
