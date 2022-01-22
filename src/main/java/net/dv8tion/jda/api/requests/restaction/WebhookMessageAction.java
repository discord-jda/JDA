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
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
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

/**
 * Extension of a default {@link net.dv8tion.jda.api.requests.RestAction RestAction}
 * that allows setting message information before sending!
 *
 * <p>This is available as return type of all sendMessage/sendFile methods in {@link net.dv8tion.jda.api.entities.WebhookClient WebhookClient}.
 *
 * <p><u>When this RestAction has been executed all provided files will be closed.</u>
 * <br>Note that the garbage collector also frees opened file streams when it finalizes the stream object.
 *
 * @since  4.3.0
 *
 * @see    net.dv8tion.jda.api.entities.WebhookClient#sendMessage(String)
 */
// TODO: WebhookMessage type (no channel/guild attached)
public interface WebhookMessageAction<T> extends RestAction<T>, AllowedMentions<WebhookMessageAction<T>>
{
//    /**
//     * Set the apparent username for the message author.
//     * <br>This changes the username that is shown for the message author.
//     *
//     * <p>This cannot be used with {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHooks}!
//     *
//     * @param  name
//     *         The username to use, or null to use the default
//     *
//     * @return The same message action, for chaining convenience
//     */
//    @Nonnull
//    @CheckReturnValue
//    WebhookMessageAction<T> setUsername(@Nullable String name);
//
//    /**
//     * Set the apparent avatar for the message author.
//     * <br>This changes the avatar that is shown for the message author.
//     *
//     * <p>This cannot be used with {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHooks}!
//     *
//     * @param  iconUrl
//     *         The URL to the avatar, or null to use default
//     *
//     * @return The same message action, for chaining convenience
//     */
//    @Nonnull
//    @CheckReturnValue
//    WebhookMessageAction<T> setAvatarUrl(@Nullable String iconUrl);

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
     * <p>This only works on {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHooks}!
     *
     * @param  ephemeral
     *         True, if this message should be invisible for other users
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageAction<T> setEphemeral(boolean ephemeral);

    /**
     * Set the content for this message.
     *
     * @param  content
     *         The new message content
     *
     * @throws IllegalArgumentException
     *         If the provided content is longer than {@link net.dv8tion.jda.api.entities.Message#MAX_CONTENT_LENGTH MAX_CONTENT_LENGTH} characters
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageAction<T> setContent(@Nullable String content);

    /**
     * Enable/Disable Text-To-Speech for the resulting message.
     *
     * @param  tts
     *         True, if this should cause a Text-To-Speech effect when sent to the channel
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageAction<T> setTTS(boolean tts);

    /**
     * Add {@link MessageEmbed MessageEmbeds} to this message
     *
     * @param  embeds
     *         The message embeds to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or one of the embeds is too big
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageAction<T> addEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds); // Doesn't work on ephemeral messages!

    /**
     * Add {@link MessageEmbed MessageEmbeds} to this message
     *
     * @param  embed
     *         The first message embed to add
     * @param  other
     *         Additional message embeds to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or one of the embeds is too big
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageAction<T> addEmbeds(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... other)
    {
        ArrayList<MessageEmbed> embeds = new ArrayList<>();
        embeds.add(embed);
        Collections.addAll(embeds, other);
        return addEmbeds(embeds);
    }

    /**
     * Adds the provided {@link java.io.InputStream InputStream} as file data.
     * <br><u>The stream will be closed upon execution!</u>
     *
     * @param  data
     *         The InputStream that will be interpreted as file data
     * @param  name
     *         The file name that should be used to interpret the type of the given data
     *         using the file-name extension. This name is similar to what will be visible
     *         through {@link net.dv8tion.jda.api.entities.Message.Attachment#getFileName() Message.Attachment.getFileName()}
     * @param  options
     *         Possible options to apply to this attachment, such as marking it as spoiler image
     *
     * @throws java.lang.IllegalStateException
     *         If the file limit of {@value Message#MAX_FILE_AMOUNT} has been reached prior to calling this method
     * @throws java.lang.IllegalArgumentException
     *         If the provided data is {@code null} or the provided name is blank or {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is targeting a TextChannel and the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageAction<T> addFile(@Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options);

    /**
     * Adds the provided byte[] as file data.
     *
     * @param  data
     *         The byte[] that will be interpreted as file data
     * @param  name
     *         The file name that should be used to interpret the type of the given data
     *         using the file-name extension. This name is similar to what will be visible
     *         through {@link net.dv8tion.jda.api.entities.Message.Attachment#getFileName() Message.Attachment.getFileName()}
     * @param  options
     *         Possible options to apply to this attachment, such as marking it as spoiler image
     *
     * @throws java.lang.IllegalStateException
     *         If the file limit of {@value Message#MAX_FILE_AMOUNT} has been reached prior to calling this method
     * @throws java.lang.IllegalArgumentException
     *         If the provided data is {@code null} or the provided name is blank or {@code null}
     *         or if the provided data exceeds the maximum file size of the currently logged in account
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is targeting a TextChannel and the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}
     *
     * @return The same message action, for chaining convenience
     *
     * @see    net.dv8tion.jda.api.entities.SelfUser#getAllowedFileSize() SelfUser.getAllowedFileSize()
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageAction<T> addFile(@Nonnull byte[] data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(name, "Name");
        Checks.notNull(data, "Data");
        return addFile(new ByteArrayInputStream(data), name, options);
    }

    /**
     * Adds the provided {@link java.io.File File} as file data.
     *
     * <br><u>This method opens a {@link java.io.FileInputStream FileInputStream} which will be closed by executing this action!</u>
     *
     * @param  file
     *         The File that will be interpreted as file data
     * @param  name
     *         The file name that should be used to interpret the type of the given data
     *         using the file-name extension. This name is similar to what will be visible
     *         through {@link net.dv8tion.jda.api.entities.Message.Attachment#getFileName() Message.Attachment.getFileName()}
     * @param  options
     *         Possible options to apply to this attachment, such as marking it as spoiler image
     *
     * @throws java.lang.IllegalStateException
     *         If the file limit of {@value Message#MAX_FILE_AMOUNT} has been reached prior to calling this method
     * @throws java.lang.IllegalArgumentException
     *         If the provided file is {@code null} or the provided name is blank or {@code null}
     *         or if the provided file is bigger than the maximum file size of the currently logged in account,
     *         or if the provided file does not exist/ is not readable
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is targeting a TextChannel and the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}
     *
     * @return The same message action, for chaining convenience
     *
     * @see    net.dv8tion.jda.api.entities.SelfUser#getAllowedFileSize() SelfUser.getAllowedFileSize()
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageAction<T> addFile(@Nonnull File file, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        Checks.notEmpty(name, "Name");
        Checks.notNull(file, "File");
        try
        {
            return addFile(new FileInputStream(file), name, options);
        }
        catch (FileNotFoundException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Adds the provided {@link java.io.File File} as file data.
     * <br>Shortcut for {@link #addFile(java.io.File, String, net.dv8tion.jda.api.utils.AttachmentOption...) addFile(file, file.getName())} with the same side-effects.
     *
     * @param  file
     *         The File that will be interpreted as file data
     * @param  options
     *         Possible options to apply to this attachment, such as marking it as spoiler image
     *
     * @throws java.lang.IllegalStateException
     *         If the file limit of {@value Message#MAX_FILE_AMOUNT} has been reached prior to calling this method
     * @throws java.lang.IllegalArgumentException
     *         If the provided file is {@code null} or if the provided File is bigger than the maximum file size of the currently logged in account
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is targeting a TextChannel and the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}
     *
     * @return The same message action, for chaining convenience
     *
     * @see    net.dv8tion.jda.api.entities.SelfUser#getAllowedFileSize() SelfUser.getAllowedFileSize()
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageAction<T> addFile(@Nonnull File file, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(file, "File");
        return addFile(file, file.getName(), options);
    }

    /**
     * Add one {@link ActionRow ActionRow} for the message.
     *
     * @param  components
     *         The components for the action row, such as {@link Button Button}
     *
     * @throws IllegalArgumentException
     *         If null is provided, an invalid number of components is provided,
     *         or any custom {@link ActionComponent#getId() id} is duplicated
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageAction<T> addActionRow(@Nonnull ItemComponent... components)
    {
        return addActionRows(ActionRow.of(components));
    }

    /**
     * Add one {@link ActionRow ActionRow} for the message.
     *
     * @param  components
     *         The components for the action row, such as {@link Button Button}
     *
     * @throws IllegalArgumentException
     *         If null is provided, an invalid number of components is provided,
     *         or any custom {@link ActionComponent#getId() id} is duplicated
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageAction<T> addActionRow(@Nonnull Collection<? extends ItemComponent> components)
    {
        return addActionRows(ActionRow.of(components));
    }

    /**
     * Add {@link ActionRow ActionRows} for the message.
     *
     * @param  rows
     *         The action rows to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, more than 5 action rows are provided,
     *         or any custom {@link ActionComponent#getId() id} is duplicated
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageAction<T> addActionRows(@Nonnull Collection<? extends ActionRow> rows)
    {
        Checks.noneNull(rows, "ActionRows");
        return addActionRows(rows.toArray(new ActionRow[0]));
    }

    /**
     * Add {@link ActionRow ActionRows} for the message.
     *
     * @param  rows
     *         The action rows to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, more than 5 action rows are provided,
     *         or any custom {@link ActionComponent#getId() id} is duplicated
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageAction<T> addActionRows(@Nonnull ActionRow... rows);

    /**
     * Applies the sendable information of the provided {@link net.dv8tion.jda.api.entities.Message Message}
     * to this message action settings.
     * <br>This will override all existing settings <b>if</b> new settings are available.
     *
     * <p><b>This does not copy files!</b>
     *
     * @param  message
     *         The Message to apply settings from
     *
     * @throws java.lang.IllegalArgumentException
     *         If the message contains a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
     *         that exceeds the sendable character limit,
     *         see {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() MessageEmbed.isSendable()}
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageAction<T> applyMessage(@Nonnull Message message);
}
