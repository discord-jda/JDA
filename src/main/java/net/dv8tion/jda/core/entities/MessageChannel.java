/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.impl.MessageImpl;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.apache.http.util.Args;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

//import net.dv8tion.jda.core.exceptions.VerificationLevelException;

/**
 * Represents a Discord channel that can have messages and files sent to it.
 */
public interface MessageChannel extends ISnowflake //todo: doc error responses on rest actions
{
    /**
     * This method is a shortcut method to return the following information in the following situation:
     * If the MessageChannel is instance of..
     * <ul>
     *     <li><b>TextChannel</b> - Returns {@link TextChannel#getName()}</li>
     *     <li><b>PrivateChannel</b> Returns {@link PrivateChannel#getUser()} {@link net.dv8tion.jda.core.entities.User#getName() .getName()}</li>
     *     <li><b>Group</b> - Returns {@link net.dv8tion.jda.client.entities.Group#getName()}</li>
     * </ul>
     *
     * @return Possibly-null "name" of the MessageChannel. Different implementations determine what the name is.
     */
    String getName();

    /**
     * The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType}
     * of this MessageChannel.
     *
     * @return The ChannelType for this channel
     */
    ChannelType getType();

    /**
     * Returns the {@link net.dv8tion.jda.core.JDA JDA} instance of this MessageChannel
     *
     * @return the corresponding JDA instance
     */
    JDA getJDA();

    /**
     * Sends a plain text {@link net.dv8tion.jda.core.entities.Message Message} to this channel.
     * <br>This will fail if this channel is an instance of {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and
     * the account of the api does not have permissions to send a message to this channel.
     * <br>To determine if you are able to send a message in a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     * use {@link net.dv8tion.jda.core.entities.TextChannel#canTalk() TextChannel#canTalk}.
     *
     * @param  text
     *         the text to send
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does
     *         not have {@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}
     *         or {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}.
     * @throws java.lang.IllegalArgumentException
     *         if the provided text is null, empty or longer than 2000 characters
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Message Message}
     *         <br>The newly created message
     */
    RestAction<Message> sendMessage(String text);

    /**
     * Sends a specified {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} as a {@link net.dv8tion.jda.core.entities.Message Message}
     * to this channel.
     * <br>This will fail if this channel is an instance of {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and
     * the account of the api does not have permissions to send a message to this channel.
     * <br>To determine if you are able to send a message in a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     * use {@link net.dv8tion.jda.core.entities.TextChannel#canTalk() TextChannel#canTalk}.
     *
     * @param  embed
     *         the {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} to send
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         <br>If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does
     *         not have {@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}
     *         or {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}.
     * @throws java.lang.IllegalArgumentException
     *         if the provided embed is null
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Message Message}
     *         <br>The newly created message
     *
     * @see    net.dv8tion.jda.core.MessageBuilder
     * @see    net.dv8tion.jda.core.EmbedBuilder
     */
    RestAction<Message> sendMessage(MessageEmbed embed);

    /**
     * Sends a specified {@link net.dv8tion.jda.core.entities.Message Message} to this channel.
     * <br>This will fail if this channel is an instance of {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and
     * the account of the api does not have permissions to send a message to this channel.
     * <br>To determine if you are able to send a message in a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     * use {@link net.dv8tion.jda.core.entities.TextChannel#canTalk() TextChannel#canTalk}.
     *
     * @param  msg
     *         the {@link net.dv8tion.jda.core.entities.Message Message} to send
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         <br>If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does
     *         not have {@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}
     *         or {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}.
     * @throws java.lang.IllegalArgumentException
     *         if the provided message is null
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Message Message}
     *         <br>The newly created message
     *
     * @see    net.dv8tion.jda.core.MessageBuilder
     */
    RestAction<Message> sendMessage(Message msg);

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * Sends the provided {@link net.dv8tion.jda.core.entities.Message Message} with the uploaded file.
     * <br>If you do not wish to send a Message with the uploaded file, you can provide {@code null} for
     * the {@code message} parameter.
     *
     * @param  file
     *         The file to upload to the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * @param  message
     *         The message to be sent along with the uploaded file. This value can be {@code null}.
     *
     * @throws IOException
     *         If an I/O error occurs while reading the File.
     * @throws java.lang.IllegalArgumentException
     *         If the provided file is larger than {@code 8MB} or {@code null}.
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Message Message}
     *         <br>The {@link net.dv8tion.jda.core.entities.Message Message} created from this upload.
     */
    RestAction<Message> sendFile(File file, Message message) throws IOException;

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * Sends the provided {@link net.dv8tion.jda.core.entities.Message Message} with the uploaded file.
     * <br>If you do not wish to send a Message with the uploaded file, you can provide {@code null} for
     * the {@code message} parameter.
     *
     * @param  file
     *         The file to upload to the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * @param  fileName
     *         The name that should be sent to discord
     * @param  message
     *         The message to be sent along with the uploaded file. This value can be {@code null}.
     *
     * @throws IOException
     *         If an I/O error occurs while reading the File.
     * @throws java.lang.IllegalArgumentException
     *         If the provided file is larger than {@code 8MB} or file/filename is {@code null} or {@code empty}.
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Message Message}
     *         <br>The {@link net.dv8tion.jda.core.entities.Message Message} created from this upload.
     */
    RestAction<Message> sendFile(File file, String fileName, Message message) throws IOException;

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * Sends the provided {@link net.dv8tion.jda.core.entities.Message Message} with the uploaded file.
     * <br>If you do not wish to send a Message with the uploaded file, you can provide {@code null} for
     * the {@code message} parameter.
     * <br>This allows you to send an {@link java.io.InputStream InputStream} as substitute to a file.
     *
     * @param  data
     *         The InputStream data to upload to the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * @param  fileName
     *         The name that should be sent to discord
     * @param  message
     *         The message to be sent along with the uploaded file. This value can be {@code null}.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided filename is {@code null} or {@code empty}.
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Message Message}
     *         <br>The {@link net.dv8tion.jda.core.entities.Message Message} created from this upload.
     */
    RestAction<Message> sendFile(InputStream data, String fileName, Message message);

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * Sends the provided {@link net.dv8tion.jda.core.entities.Message Message} with the uploaded file.
     * <br>If you do not wish to send a Message with the uploaded file, you can provide {@code null} for
     * the {@code message} parameter.
     * <br>This allows you to send an {@code byte[]} as substitute to a file.
     *
     * @param  data
     *         The {@code byte[]} data to upload to the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * @param  fileName
     *         The name that should be sent to discord
     * @param  message
     *         The message to be sent along with the uploaded file. This value can be {@code null}.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided filename is {@code null} or {@code empty} or the provided data is larger than 8MB.
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Message Message}
     *         <br>The {@link net.dv8tion.jda.core.entities.Message Message} created from this upload.
     */
    RestAction<Message> sendFile(byte[] data, String fileName, Message message);

    /**
     * Attempts to get a {@link net.dv8tion.jda.core.entities.Message Message} from the Discord servers that has
     * the same id as the id provided.
     *
     * @param  messageId
     *         The id of the sought after Message
     *
     * @throws IllegalArgumentException
     *         if the provided messageId is null
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Message
     *         <br>The Message defined by the provided id.
     */
    RestAction<Message> getMessageById(String messageId);

    /**
     * Attempts to delete a {@link net.dv8tion.jda.core.entities.Message Message} from the Discord servers that has
     * the same id as the id provided.
     *
     * @param  messageId
     *         The id of the Message that should be deleted
     *
     * @throws IllegalArgumentException
     *         if the provided messageId is null
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Void
     */
    RestAction<Void> deleteMessageById(String messageId);

    /**
     * Creates a new {@link MessageHistory MessageHistory} object for each call of this method.
     * <br>This is <b>NOT</b> an internal message cache, but rather it queries the Discord servers for previously sent messages.
     *
     * @return The {@link MessageHistory MessageHistory} for this channel.
     */
    MessageHistory getHistory();

    default RestAction<MessageHistory> getHistoryAround(Message message, int limit)
    {
        Args.notNull(message, "Provided target message");
        Args.check(message.getChannel().equals(this), "The provided Message is not from the MessageChannel!");

        return getHistoryAround(message.getId(), limit);
    }

    default RestAction<MessageHistory> getHistoryAround(String messageId, int limit)
    {
        Args.notEmpty(messageId, "Provided messageId");
        Args.check(limit > 100 || limit < 1, "Provided limit was out of bounds. Minimum: 1, Max: 100. Provided: %d", limit);

        Route.CompiledRoute route = Route.Messages.GET_MESSAGE_HISTORY_AROUND.compile(this.getId(), Integer.toString(limit), messageId);
        return new RestAction<MessageHistory>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                MessageHistory mHistory = new MessageHistory(MessageChannel.this);

                EntityBuilder builder = EntityBuilder.get(api);
                LinkedList<Message> msgs  = new LinkedList<>();
                JSONArray historyJson = response.getArray();

                for (int i = 0; i < historyJson.length(); i++)
                    msgs.add(builder.createMessage(historyJson.getJSONObject(i)));

                msgs.forEach(msg -> mHistory.history.put(msg.getId(), msg));
                request.onSuccess(mHistory);
            }
        };
    }

    /**
     * Sends the typing status to discord. This is what is used to make the message "X is typing..." appear.
     * <br>The typing status only lasts for 10 seconds or until a message is sent.
     * <br>So if you wish to show continuous typing you will need to call this method once every 10 seconds.
     *
     * <p>The official discord client sends this every 5 seconds even though the typing status lasts 10.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Void
     */
    RestAction<Void> sendTyping();

    /**
     * Attempts to react to a message represented by the specified {@code messageId}
     * in this MessageChannel.
     *
     * <p>The unicode provided has to be a UTF-8 representation of the emoji
     * that is supposed to be represented by the Reaction.
     * <br>To retrieve the characters needed you can use an api or
     * the official discord client by escaping the emoji (\:emoji-name:)
     * and copying the resulting emoji from the sent message.
     *
     * <p>This method encodes the provided unicode for you.
     * <b>Do not encode the emoji before providing the unicode.</b>
     *
     * @param  messageId
     *         The not-null messageId to attach the reaction to
     * @param  unicode
     *         The UTF-8 characters to react with
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the currently logged
     *         in account does not have {@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permisison.MESSAGE_ADD_REACTION}
     *         in this channel.
     * @throws IllegalArgumentException
     *         if any of the provided parameters is null
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction} - Type: Void
     */
    default RestAction<Void> addReactionById(String messageId, String unicode)
    {
        Args.notNull(messageId, "MessageId");
        Args.containsNoBlanks(unicode, "Provided Unicode");
        if (this instanceof TextChannel)
        {
            TextChannel tChan = (TextChannel) this;
            Guild guild = tChan.getGuild();
            Member selfMember = guild.getSelfMember();

            if (!selfMember.hasPermission(tChan, Permission.MESSAGE_ADD_REACTION))
                throw new PermissionException(Permission.MESSAGE_ADD_REACTION);
        }

        String encoded;
        try
        {
            encoded = URLEncoder.encode(unicode, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e); //thanks JDK 1.4
        }
        Route.CompiledRoute route = Route.Messages.ADD_REACTION.compile(getId(), messageId, encoded);
        return new RestAction<Void>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Attempts to react to a message represented by the specified {@code messageId}
     * in this MessageChannel.
     *
     * <p><b>An Emote is not the same as an emoji!</b>
     * <br>Emotes are custom guild-specific images unlike global unicode emojis!
     *
     * @param  messageId
     *         The not-null messageId to attach the reaction to
     * @param  emote
     *         The not-null {@link net.dv8tion.jda.core.entities.Emote} to react with
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the currently logged
     *         in account does not have {@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permisison.MESSAGE_ADD_REACTION}
     *         in this channel.
     * @throws IllegalArgumentException
     *         if any of the provided parameters is null
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction} - Type: Void
     */
    default RestAction<Void> addReactionById(String messageId, Emote emote)
    {
        Args.notNull(messageId, "MessageId");
        Args.notNull(emote, "Emote");
        if (this instanceof TextChannel)
        {
            TextChannel tChan = (TextChannel) this;
            Guild guild = tChan.getGuild();
            Member selfMember = guild.getSelfMember();

            if (!selfMember.hasPermission(tChan, Permission.MESSAGE_ADD_REACTION))
                throw new PermissionException(Permission.MESSAGE_ADD_REACTION);
        }

        Route.CompiledRoute route = Route.Messages.ADD_REACTION.compile(getId(), messageId, String.format("%s:%s", emote.getName(), emote.getId()));
        return new RestAction<Void>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Used to pin a message.
     * <br>If the provided messageId is invalid or not in this channel, this does nothing.
     *
     * @param  messageId
     *         The message to pin.
     *
     * @throws IllegalArgumentException
     *         if the provided messageId is null
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Void
     */
    RestAction<Void> pinMessageById(String messageId);

    /**
     * Used to unpin a message.
     * <br>If the provided messageId is invalid or not in this channel, this does nothing.
     *
     * @param  messageId
     *         The message to unpin.
     *
     * @throws IllegalArgumentException
     *         if the provided messageId is null
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Void
     */
    RestAction<Void> unpinMessageById(String messageId);

    /**
     * Retrieves a List of {@link net.dv8tion.jda.core.entities.Message Messages} that have been pinned in this channel.
     * <br>If no messages have been pinned, this retrieves an empty List.
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: List{@literal <}{@link net.dv8tion.jda.core.entities.Message}{@literal >}
     *         <br>An immutable list of pinned messages
     */
    RestAction<List<Message>> getPinnedMessages();

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * @param  id
     *         The id referencing the Message that should be edited
     * @param  newContent
     *         The new content for the edited message
     *
     * @throws IllegalArgumentException
     *         if the provided content is {@code null}, {@code empty} or larger than 2000 characters
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         if this is a TextChannel and the currently logged in account does not have:
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}
     *             <br>And the message is by another user</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Message}
     *         <br>The modified Message
     */
    default RestAction<Message> editMessageById(String id, String newContent)
    {
        return editMessageById(id, new MessageBuilder().appendString(newContent).build());
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * @param  id
     *         The id referencing the Message that should be edited
     * @param  newContent
     *         The new content for the edited message
     *
     * @throws IllegalArgumentException
     *         if the provided message or id is {@code null}
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         if this is a TextChannel and the currently logged in account does not have:
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}
     *             <br>And the message is by another user</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Message}
     *         <br>The modified Message
     */
    default RestAction<Message> editMessageById(String id, Message newContent)
    {
        Args.notNull(id, "id");
        Args.notNull(newContent, "message");

        JSONObject json = ((MessageImpl) newContent).toJSONObject();
        Route.CompiledRoute route = Route.Messages.EDIT_MESSAGE.compile(getId(), id);
        return new RestAction<Message>(getJDA(), route, json)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                {
                    try
                    {
                        Message m = EntityBuilder.get(api).createMessage(response.getObject());
                        request.onSuccess(m);
                    }
                    catch (IllegalArgumentException e)
                    {
                        request.onFailure(e);
                    }
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }
}
