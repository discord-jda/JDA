package net.dv8tion.jda.webhook;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.AbstractMessage;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Represents a message that has been send from a webhook
 */
public class SentWebhookMessage extends AbstractMessage
{
    protected final long id;
    protected final MessageType type;
    protected final long channelId;
    protected final long webhookId;
    protected final boolean mentionsEveryone;
    protected final boolean pinned;
    protected final User author;
    protected final List<Message.Attachment> attachments;
    protected final List<MessageEmbed> embeds;
    protected final List<User> mentionedUsers;
    protected final Set<Long> mentionedRoles;

    public SentWebhookMessage(long id, long channelId, long webhookId, MessageType type,
                                 boolean mentionsEveryone, List<User> mentionedUsers, Set<Long> mentionedRoles,
                                 boolean tts, boolean pinned,
                                 String content, String nonce, User author,
                                 List<Message.Attachment> attachments, List<MessageEmbed> embeds)
    {
        super(content, nonce, tts);

        this.id = id;
        this.channelId = channelId;
        this.webhookId = webhookId;
        this.mentionedUsers = mentionedUsers;
        this.mentionedRoles = Collections.unmodifiableSet(mentionedRoles);
        this.author = author;
        this.type = type;
        this.pinned = pinned;
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
        return pinned;
    }

    @Override
    public MessageType getType()
    {
        return type;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    /**
     * The Snowflake id of the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that this message was send to.
     *
     * @return Long containing the Id.
     */
    public long getChannelId()
    {
        return channelId;
    }

    /**
     * The Snowflake id of the webhook that send this message.
     *
     * @return Long containing the Id.
     */
    public long getWebhookId()
    {
        return webhookId;
    }

    /**
     * A immutable set of all mentioned {@link net.dv8tion.jda.core.entities.Role Roles} ids.
     * <br>If none were mentioned, this list is empty.
     *
     * <p><b>This may include Roles from other {@link net.dv8tion.jda.core.entities.Guild Guilds}</b>
     *
     * @throws java.lang.UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core.entities.MessageType#DEFAULT MessageType.DEFAULT}
     *
     *
     * @return immutable set of mentioned Role ids
     */
    public Set<Long> getMentionedRoleIds()
    {
        return mentionedRoles;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof SentWebhookMessage))
            return false;
        SentWebhookMessage oMsg = (SentWebhookMessage) o;
        return this == oMsg || this.id == oMsg.id;
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
     * Represents an attachment on a {@link SentWebhookMessage WebhookMessage}
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
