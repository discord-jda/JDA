/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.webhook;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.AbstractMessage;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a message that has been sent from a webhook
 */
public class SentWebhookMessage extends AbstractMessage
{
    protected final long id;
    protected final long channelId;
    protected final long webhookId;
    protected final boolean mentionsEveryone;
    protected final User author;
    protected final List<Message.Attachment> attachments;
    protected final List<MessageEmbed> embeds;
    protected final List<User> mentionedUsers;
    protected final List<Long> mentionedRolesLong;
    protected List<String> mentionedRolesString;

    public SentWebhookMessage(long id, long channelId, long webhookId, boolean mentionsEveryone,
                              List<User> mentionedUsers, List<Long> mentionedRolesLong, boolean tts, String content,
                              String nonce, User author, List<Message.Attachment> attachments, List<MessageEmbed> embeds)
    {
        super(content, nonce, tts);

        this.id = id;
        this.channelId = channelId;
        this.webhookId = webhookId;
        this.mentionedUsers = mentionedUsers;
        this.mentionedRolesLong = Collections.unmodifiableList(mentionedRolesLong);
        this.author = author;
        this.mentionsEveryone = mentionsEveryone;
        this.attachments = Collections.unmodifiableList(attachments);
        this.embeds = Collections.unmodifiableList(embeds);
    }

    @Override
    public List<User> getMentionedUsers()
    {
        return mentionedUsers;
    }

    @Override
    public boolean mentionsEveryone()
    {
        return mentionsEveryone;
    }

    @Override
    public boolean isEdited()
    {
        return false;
    }

    @Override
    public OffsetDateTime getEditedTime()
    {
        return null;
    }

    @Override
    public User getAuthor()
    {
        return author;
    }

    @Override
    public boolean isFromType(ChannelType type)
    {
        return type == ChannelType.TEXT;
    }

    @Override
    public boolean isWebhookMessage()
    {
        return true;
    }

    @Override
    public List<Message.Attachment> getAttachments()
    {
        return attachments;
    }

    @Override
    public List<MessageEmbed> getEmbeds()
    {
        return embeds;
    }

    @Override
    public boolean isPinned()
    {
        return false;
    }

    @Override
    public MessageType getType()
    {
        return MessageType.DEFAULT;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    /**
     * The Snowflake id of the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that this message was send to.
     *
     * @return the Snowflake id of the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that this message was send to.
     */
    public long getChannelIdLong()
    {
        return channelId;
    }

    /**
     * The Snowflake id of the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that this message was send to.
     *
     * @return Never-null String containing the Id of the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that this message was send to.
     */
    public String getChannelId()
    {
        return Long.toUnsignedString(getChannelIdLong());
    }

    /**
     * The Snowflake id of the webhook that send this message.
     *
     * @return the snowflake id of the webhook that send this message.
     */
    public long getWebhookIdLong()
    {
        return webhookId;
    }

    /**
     * The Snowflake id of the webhook that send this message.
     *
     * @return Never-null String containing the Id of this webhook.
     */
    public String getWebhookId()
    {
        return Long.toUnsignedString(getWebhookIdLong());
    }

    /**
     * An immutable list of all mentioned {@link net.dv8tion.jda.core.entities.Role Roles} ids.
     * <br>If none were mentioned, this list is empty.
     *
     * <p><b>This may include Roles from other {@link net.dv8tion.jda.core.entities.Guild Guilds}</b>
     *
     * @throws java.lang.UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core.entities.MessageType#DEFAULT MessageType.DEFAULT}
     *
     *
     * @return immutable list of mentioned Role ids
     */
    public List<Long> getMentionedRoleIdsLong()
    {
        return mentionedRolesLong;
    }

    /**
     * An immutable list of all mentioned {@link net.dv8tion.jda.core.entities.Role Roles} ids.
     * <br>If none were mentioned, this list is empty.
     *
     * <p><b>This may include Roles from other {@link net.dv8tion.jda.core.entities.Guild Guilds}</b>
     *
     * @throws java.lang.UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core.entities.MessageType#DEFAULT MessageType.DEFAULT}
     *
     *
     * @return immutable list of mentioned Role ids
     */
    public List<String> getMentionedRoleIds()
    {
        if (mentionedRolesString == null)
        {
            mentionedRolesString = Collections.unmodifiableList(mentionedRolesLong.stream()
                .map(Long::toUnsignedString).collect(Collectors.toList()));
        }

        return mentionedRolesString;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof SentWebhookMessage))
            return false;
        SentWebhookMessage oMsg = (SentWebhookMessage) o;
        return this.id == oMsg.id;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public String toString()
    {
        return String.format("WM:%#s:%.20s(%s)", author, this, getId());
    }

    protected void unsupported()
    {
        throw new UnsupportedOperationException("This operation is not supported for Messages that are from webhooks!\n" +
            String.format("Use JDA#getTextChannelById(%s)#getMessageById(%s) to perform this action on the message", channelId, id));
    }

    /**
     * Represents an attachment on a {@link net.dv8tion.jda.webhook.SentWebhookMessage WebhookMessage}
     *
     * <p>JDA methods such as {@link net.dv8tion.jda.core.entities.Message.Attachment#getJDA() getJDA()} are not available
     * and will throw an {@link java.lang.UnsupportedOperationException UnsupportedOperationException}
     */
    public static class Attachment extends Message.Attachment
    {

        public Attachment(long id, String url, String proxyUrl, String filename, int size, int height, int width)
        {
            super(id, url, proxyUrl, filename, size, height, width, null);
        }

        @Override
        public JDA getJDA()
        {
            throw new UnsupportedOperationException("This operation is not supported for Messages that are from webhooks!");
        }
    }
}
