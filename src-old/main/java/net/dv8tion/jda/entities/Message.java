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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.entities;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.requests.Requester;

import java.io.File;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a Text message received from Discord.<br>
 * This repsents message received from both {@link net.dv8tion.jda.entities.TextChannel TextChannels}
 * and from {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannels}.
 */
public interface Message
{
    /**
     * The Id of this Message
     *
     * @return String Id
     */
    String getId();

    /**
     * A immutable list of all mentioned users. if noone was mentioned, this list is empty
     * In {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel's}, this always returns an empty List
     *
     * @return immutable list of mentioned users
     */
    List<User> getMentionedUsers();

    /**
     * Checks if given user was mentioned in this message in any way (@User, @everyone, @here).
     *
     * @param user
     *      The user to check on.
     * @return
     *      True if the given user was mentioned in this message.
     */
    boolean isMentioned(User user);

    /**
     * A immutable list of all mentioned {@link net.dv8tion.jda.entities.TextChannel TextChannels}. if none were mentioned, this list is empty
     * In {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel's}, this always returns an empty List
     *
     * @return immutable list of mentioned TextChannels
     */
    List<TextChannel> getMentionedChannels();

    /**
     * A immutable list of all mentioned {@link net.dv8tion.jda.entities.Role Roles}. if none were mentioned, this list is empty
     * In {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel's}, this always returns an empty List
     *
     * @return immutable list of mentioned Roles
     */
    List<Role> getMentionedRoles();

    /**
     * Is this Message mentioning everyone using @everyone or @here?
     * In {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel's}, this always returns false
     *
     * @return if mentioning everyone
     */
    boolean mentionsEveryone();

    /**
     * Gives a copy of the Object holding the time this message was originally sent
     *
     * @return time of Message
     */
    OffsetDateTime getTime();

    /**
     * Returns true, if this Message was edited as least once
     *
     * @return if edited
     */
    boolean isEdited();

    /**
     * Gives a copy of the Object holding the time this message was last recently updated
     * If this message was never updated ({@link #isEdited()} returns false), this will be NULL
     *
     * @return time of most recent update
     */
    OffsetDateTime getEditedTimestamp();

    /**
     * The author of this Message
     *
     * @return Message author
     */
    User getAuthor();

    /**
     * The textual content of this message.
     * Mentioned users will get returned as @Username
     * If you want the actual Content (mentions as &lt;@id&gt;), use {@link #getRawContent()} instead
     *
     * @return message-text
     */
    String getContent();

    /**
     * The raw textual content of this message.
     * Mentioned users will get returned as &lt;@id&gt;
     *
     * @return raw message-text
     */
    String getRawContent();

    /**
     * The content, with all its formatting characters stripped.
     * All remaining characters used in formatting (the ones that did not have a matching partner) are getting escaped.
     *
     * Mentioned users will get returned as @Username
     *
     * @return message-text with stripped formatting
     */
    String getStrippedContent();

    /**
     * Checks, whether this Message was sent in a {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel} (Private Message),
     * or in a {@link net.dv8tion.jda.entities.TextChannel TextChannel} (sent in Guild channel)
     *
     * @return
     *      true, if this Message is from a PrivateChannel, false if it is from a TextChannel
     */
    boolean isPrivate();

    /**
     * Returns the Id of the Channel this Message was sent in.
     * This can be the id of either a {@link net.dv8tion.jda.entities.TextChannel TextChannel} or a {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel}
     * To get the corresponding channel, you can use {@link net.dv8tion.jda.JDA#getTextChannelById(String)} or {@link net.dv8tion.jda.JDA#getPrivateChannelById(String)}
     * or if you just want to reply, you can also use {@link #getChannel()}.
     * (Hint: {@link #isPrivate()} could be helpful!)
     *
     * @return
     *      The Id of the Channel this was sent in
     */
    String getChannelId();

    /**
     * Returns the {@link net.dv8tion.jda.entities.MessageChannel MessageChannel} that this message was sent in
     *
     * @return
     *      The MessageChannel of this Message
     */
    MessageChannel getChannel();

    /**
     * An unmodifiable list of {@link net.dv8tion.jda.entities.Message.Attachment Attachment} that are attached to this message.<br>
     * Most likely this will only ever be 1 {@link net.dv8tion.jda.entities.Message.Attachment Attachment} at most.
     *
     * @return
     *      Unmodifiable list of {@link net.dv8tion.jda.entities.Message.Attachment Attachments}.
     */
    List<Attachment> getAttachments();

    /**
     * An unmodifiable list of {@link net.dv8tion.jda.entities.MessageEmbed MessageEmbeds} that are available to this message.
     *
     * @return
     *      Unmodifiable list of all given embeds
     */
    List<MessageEmbed> getEmbeds();

    /**
     * Is this Message supposed to be TTS (Text-to-speach)
     *
     * @return if message is tts
     */
    boolean isTTS();

    /**
     * Edits this Messages content to the provided String.
     * If The Message was not created by this account, this does not have any effect
     * If this method failed, null gets returned
     *
     * @param newContent the new content of the Message
     * @return a new Message-Object for the edited message
     */
    Message updateMessage(String newContent);

    /**
     * Edits this Messages content to the provided String.
     * If The Message was not created by this account, this does not have any effect.
     * After the message has been edited, the corresponding new {@link net.dv8tion.jda.entities.Message Message} object is passed to the callback-function
     * This method will wait, and update later, if a Rate-Limit occurs.
     *
     * @param newContent
     *      the new content of the Message
     * @param callback
     *      the Callback-function that is called upon successful edit with the Message-object of the edited message or null, if editing failed.
     *      You can pass null as callback, if you do not need the updated Message-object.
     */
    void updateMessageAsync(String newContent, Consumer<Message> callback);

    /**
     * Deletes this Message from the server.
     * Calling this function on a Message created by another User while not having the
     * {@link net.dv8tion.jda.Permission#MESSAGE_MANAGE MESSAGE_MANAGE Permission} will have no effect
     */
    void deleteMessage();

    /**
     * Returns the {@link net.dv8tion.jda.JDA JDA} instance of this Message
     * @return
     *      the corresponding JDA instance
     */
    JDA getJDA();

    /**
     * Whether or not this Message has been pinned in its parent channel.
     *
     * @return
     *      True - if this message has been pinned.
     */
    boolean isPinned();

    /**
     * This is a shortcut method to {@link MessageChannel#pinMessageById(String)}.<br>
     * If this method returns true, then the action was successful and this Message's
     * {@link #isPinned()} will now return true.
     *
     * @return
     *      True - if the action completed successfully and this message became pinned.
     */
    boolean pin();

    /**
     * This is a shortcut method to {@link MessageChannel#unpinMessageById(String)}.<br>
     * If this method returns true, then the action was successful and this Message's
     * {@link #isPinned()} will now return false.
     *
     * @return
     *      True - if the action completed successfully and this message was unpinned.
     */
    boolean unpin();

    /**
     * This specifies the type of Message sent. Messages can represent more than just simple text sent by Users.<br>
     * Messages can also be sent as special actions like Calls, GroupIcon changes and more.
     *
     * @return
     *      The type of message this is.
     */
    MessageType getType();

    /**
     * Represents a {@link net.dv8tion.jda.entities.Message Message} file attachment.
     */
    class Attachment
    {
        private final String id;
        private final String url;
        private final String proxyUrl;
        private final String fileName;
        private final int size;
        private final int height;
        private final int width;
        private final JDA jda;

        public Attachment(String id, String url, String proxyUrl, String fileName, int size, int height, int width, JDA jda)
        {
            this.id = id;
            this.url = url;
            this.proxyUrl = proxyUrl;
            this.fileName = fileName;
            this.size = size;
            this.height = height;
            this.width = width;
            this.jda = jda;
        }

        /**
         * The id of the attachment. This is not the id of the message that the attachment was attached to.
         *
         * @return
         *      Non-null String containing the Attachment ID.
         */
        public String getId()
        {
            return id;
        }

        /**
         * The url of the Attachment, most likely on the Discord servers.
         *
         * @return
         *      Non-null String containing the Attachment URL.
         */
        public String getUrl()
        {
            return url;
        }

        /**
         * The url of the Attachment, proxied by Discord.
         *
         * @return
         *      Non-null String containing the proxied Attachment url.
         */
        public String getProxyUrl()
        {
            return proxyUrl;
        }

        /**
         * The file name of the Attachment when it was first uploaded.
         *
         * @return
         *      Non-null String containing the Attachment file name.
         */
        public String getFileName()
        {
            return fileName;
        }

        /**
         * Downloads this attachment to given File
         *
         * @param file
         *      The file, where the attachment will get downloaded to
         * @return
         *      boolean true, if successful, otherwise false
         */
        public boolean download(File file)
        {
            InputStream in = null;
            try
            {
                URL url = new URL(getUrl());
                URLConnection con;
                if (jda.getGlobalProxy() == null)
                {
                    con = url.openConnection();
                }
                else
                {
                    con = url.openConnection(new Proxy(Proxy.Type.HTTP,
                            new InetSocketAddress(jda.getGlobalProxy().getAddress(), jda.getGlobalProxy().getPort())));
                }
                con.addRequestProperty("user-agent", Requester.USER_AGENT);
                in = con.getInputStream();
                Files.copy(in, Paths.get(file.getAbsolutePath()));
                return true;
            }
            catch (Exception e)
            {
                JDAImpl.LOG.log(e);
            }
            finally
            {
                if (in != null)
                {
                    try {in.close();} catch(Exception ignored) {}
                }
            }
            return false;
        }

        /**
         * The size of the attachment in bytes.<br>
         * Example: if {@link #getSize() getSize()} returns 1024, then the attachment is 1024 bytes, or 1KB, in size.
         *
         * @return
         *      Positive int containing the size of the Attachment.
         */
        public int getSize()
        {
            return size;
        }

        /**
         * The height of the Attachment if this Attachment is an image.<br>
         * If this Attachment is not an image, this returns 0.
         *
         * @return
         *      Never-negative int containing image Attachment height.
         */
        public int getHeight()
        {
            return height;
        }

        /**
         * The width of the Attachment if this Attachment is an image.<br>
         * If this Attachment is not an image, this returns 0.
         *
         * @return
         *      Never-negative int containing image Attachment width.
         */
        public int getWidth()
        {
            return width;
        }

        /**
         * Based on the values of getHeight and getWidth being larger than zero.
         *
         * @return
         *      True if width and height are greater than zero.
         */
        public boolean isImage()
        {
            return height > 0 && width > 0;
        }
    }
}
