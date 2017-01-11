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
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.requests.RestAction;

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

/**
 * Represents a Text message received from Discord.<br>
 * This represents messages received from both {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}
 * and from {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannels}.
 */
public interface Message extends ISnowflake
{

    /**
     * An immutable list of all mentioned Users. If no one was mentioned, this list is empty.
     * In {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannels}, this always returns an empty List.
     *
     * @return An immutable list of mentioned Users.
     */
    List<User> getMentionedUsers();

    /**
     * Checks if the given User was mentioned in this message in any way (@User, @everyone, @here).
     *
     * @param user
     *      The User to check on.
     * @return
     *      True if the given User was mentioned in this message.
     */
    boolean isMentioned(User user);

    /**
     * An immutable list of all mentioned {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}. If none were mentioned, this list is empty.
     * In {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannels}, this always returns an empty List.
     *
     * @return An immutable list of mentioned TextChannels.
     */
    List<TextChannel> getMentionedChannels();

    /**
     * An immutable list of all mentioned {@link net.dv8tion.jda.core.entities.Role Roles}. If none were mentioned, this list is empty.
     * In {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannels}, this always returns an empty List.
     *
     * @return An immutable list of mentioned Roles.
     */
    List<Role> getMentionedRoles();

    /**
     * Checks if this Message mentioning everyone using @everyone or @here.
     * In {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannels}, this always returns false.
     *
     * @return True if the message mentions everyone.
     */
    boolean mentionsEveryone();

    /**
     * Returns true if this Message was edited as least once.
     *
     * @return True if the Message was edited.
     */
    boolean isEdited();

    /**
     * Returns a copy of the Object holding the time this Message was last edited.
     * If this Message was never edited ({@link #isEdited()} returns false), this will be null.
     *
     * @return The time that the Message was last edited.
     */
    OffsetDateTime getEditedTime();

    /**
     * The author of this Message.
     *
     * @return The Message author as a User object.
     */
    User getAuthor();

    /**
     * The text content of this Message.
     * Mentioned users will get returned as @Username
     * If you want the raw content (mentions as &lt;@id&gt;), use {@link #getRawContent()} instead.
     *
     * @return The text content of the Message.
     */
    String getContent();

    /**
     * The raw text content of this Message.
     * Mentioned users will get returned as &lt;@id&gt;
     *
     * @return The raw text content of the Message.
     */
    String getRawContent();

    /**
     * The content, with all its formatting characters stripped.
     * All remaining characters used in formatting (the ones that did not have a matching partner) get escaped.
     *
     * Mentioned users will get returned as @Username
     *
     * @return The text content of the Message that has been stripped of formatting.
     */
    String getStrippedContent();

    /**
    * Checks whether the given Message is from the given ChannelType.
    *
    * @param type The ChannelType you are comparing the Message's ChannelType against.
    * @return True if the Message is from the given ChannelType.
    */
    boolean isFromType(ChannelType type);

    /**
    * Returns the ChannelType of the Channel this message was sent in.
    *
    * @return The ChannelType of the Channel this Message was sent in.
    */
    ChannelType getChannelType();

    /**
    * Returns a boolean based on whether this Message is a Webhook.
    *
    * @return True is this Message is a Webhook.
    */
    boolean isWebhookMessage();

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} that this message was sent in.
     *
     * @return The MessageChannel of this Message.
     */
    MessageChannel getChannel();

    /**
    * Returns the {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} that this message was sent in.
    *
    * @return The PrivateChannel of this Message.
    */
    PrivateChannel getPrivateChannel();

    /**
    * Returns the {@link net.dv8tion.jda.core.entities.Group Group} that this message was sent in.
    *
    * @return The Group of this Message.
    */
    Group getGroup();

    /**
    * Returns the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that this message was sent in.
    *
    * @return The TextChannel of this Message.
    */
    TextChannel getTextChannel();

    /**
    * Returns the {@link net.dv8tion.jda.core.entities.Guild Guild} that this message was sent in.
    *
    * @return The Guild of this Message.
    */
    Guild getGuild();

    /**
     * An unmodifiable list of {@link net.dv8tion.jda.core.entities.Message.Attachment Attachments} that are attached to this message.<br>
     * Most likely, this will only ever be 1 {@link net.dv8tion.jda.core.entities.Message.Attachment Attachment} at most.
     *
     * @return Unmodifiable list of {@link net.dv8tion.jda.core.entities.Message.Attachment Attachments}.
     */
    List<Attachment> getAttachments();

    /**
     * An unmodifiable list of {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds} that are available to this message.
     *
     * @return An unmodifiable list of all given embeds.
     */
    List<MessageEmbed> getEmbeds();

    /**
     * All emotes used in this Message.
     * <p>
     * <b>This may or may not contain fake Emotes.
     * To check whether an Emote is fake you can test if {@link net.dv8tion.jda.core.entities.Emote#getGuild() Emote#getGuild} returns null.</b>
     *
     * @return An immutable list of the Emotes used in this message (example match &lt;:jda:230988580904763393&gt;)
     */
    List<Emote> getEmotes();

    List<MessageReaction> getReactions();

    /**
     * Checks if this Message is a TTS (Text-to-speech) Message.
     *
     * @return True is this Message is a TTS Message.
     */
    boolean isTTS();

    /**
     * Edits this Message's content to the provided String.
     * If the Message was not created by this account, this will result in a PermissionException.
     * If this method failed, null gets returned.
     *
     * @param newContent The new content of the Message.
     * @return A new Message-Object for the edited message.
     */
    RestAction<Message> editMessage(String newContent);

    /**
     * Edits this Message's content to be the content of the provided message.
     * If the Message was not created by this account, this will result in a PermissionException.
     * If this method fails, null gets returned.
     *
     * @param newContent The new content of the Message.
     * @return A new Message-Object for the edited message.
     */
    RestAction<Message> editMessage(Message newContent);

    /**
     * Deletes this Message from the server.
     * Calling this function on a Message created by another User while not having the
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE MESSAGE_MANAGE Permission} will have no effect.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     */
    RestAction<Void> deleteMessage();

    /**
     * Returns the {@link net.dv8tion.jda.core.JDA JDA} instance of this Message.
     *
     * @return The corresponding JDA instance.
     */
    JDA getJDA();

    /**
     * Checks whether this Message has been pinned in its parent Channel.
     *
     * @return True if this Message has been pinned.
     */
    boolean isPinned();

    /**
     * This is a shortcut method to {@link MessageChannel#pinMessageById(String)}.<br>
     * If this method returns true, then the action was successful and this Message's
     * {@link #isPinned()} will now return true.
     *
     * @return True if the action completed successfully and this Message became pinned.
     */
    RestAction<Void> pin();

    /**
     * This is a shortcut method to {@link MessageChannel#unpinMessageById(String)}.<br>
     * If this method returns true, then the action was successful and this Message's
     * {@link #isPinned()} will now return false.
     *
     * @return True if the action completed successfully and this Message was unpinned.
     */
    RestAction<Void> unpin();

    RestAction<Void> addReaction(Emote emote);
    RestAction<Void> addReaction(String unicode);
    RestAction<Void> clearReactions();

    /**
     * This specifies the type of Message sent. Messages can represent more than just simple text sent by Users.<br>
     * Messages can also be sent as special actions like Calls, GroupIcon changes and more.
     *
     * @return
     *      The type of Message this is.
     */
    MessageType getType();

    /**
     * Represents a {@link net.dv8tion.jda.core.entities.Message Message} file attachment.
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
         * @return Non-null String containing the Attachment ID.
         */
        public String getId()
        {
            return id;
        }

        /**
         * The url of the Attachment, most likely on the Discord servers.
         *
         * @return Non-null String containing the Attachment URL.
         */
        public String getUrl()
        {
            return url;
        }

        /**
         * The url of the Attachment, proxied by Discord.
         *
         * @return Non-null String containing the proxied Attachment url.
         */
        public String getProxyUrl()
        {
            return proxyUrl;
        }

        /**
         * The file name of the Attachment when it was first uploaded.
         *
         * @return Non-null String containing the Attachment file name.
         */
        public String getFileName()
        {
            return fileName;
        }

        /**
         * Downloads this attachment to given File.
         *
         * @param file The file, where the attachment will get downloaded to.
         * @return boolean true, if successful, otherwise false
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
         * @return Positive int containing the size of the Attachment.
         */
        public int getSize()
        {
            return size;
        }

        /**
         * The height of the Attachment if this Attachment is an image.<br>
         * If this Attachment is not an image, this returns 0.
         *
         * @return Never-negative int containing image Attachment height.
         */
        public int getHeight()
        {
            return height;
        }

        /**
         * The width of the Attachment if this Attachment is an image.<br>
         * If this Attachment is not an image, this returns 0.
         *
         * @return Never-negative int containing image Attachment width.
         */
        public int getWidth()
        {
            return width;
        }

        /**
         * Returns a boolean based on the values of getHeight and getWidth being larger than zero.
         *
         * @return True if width and height are greater than zero.
         */
        public boolean isImage()
        {
            return height > 0 && width > 0;
        }
    }
}
