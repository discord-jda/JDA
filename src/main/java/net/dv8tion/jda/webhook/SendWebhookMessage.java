package net.dv8tion.jda.webhook;

import gnu.trove.set.TLongSet;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Set;

/**
 * Represents a message that has been send from a webhook
 */
public class SendWebhookMessage implements Message
{
    protected final long id;
    protected final String content;
    protected final String nonce;
    protected final MessageType type;
    protected final long channelId;
    protected final long webhookId;
    protected final boolean mentionsEveryone;
    protected final boolean pinned;
    protected final boolean tts;
    protected final User author;
    protected final List<Message.Attachment> attachments;
    protected final List<MessageEmbed> embeds;
    protected final List<User> mentionedUsers;
    protected final Set<Long> mentionedRoles;

    protected SendWebhookMessage(long id, long channelId, long webhookId, MessageType type,
                                 boolean mentionsEveryone, List<User> mentionedUsers, Set<Long> mentionedRoles,
                                 boolean tts, boolean pinned,
                                 String content, String nonce, User author,
                                 List<Message.Attachment> attachments, List<MessageEmbed> embeds)
    {

        this.id = id;
        this.channelId = channelId;
        this.webhookId = webhookId;
        this.mentionedUsers = mentionedUsers;
        this.mentionedRoles = Collections.unmodifiableSet(mentionedRoles);
        this.author = author;
        this.type = type;
        this.content = content;
        this.nonce = nonce;
        this.tts = tts;
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
    public List<TextChannel> getMentionedChannels()
    {
        unsupported();
        return null;
    }

    @Override
    public List<Role> getMentionedRoles()
    {
        unsupported();
        return null;
    }

    @Override
    public List<Member> getMentionedMembers(Guild guild)
    {
        unsupported();
        return null;
    }

    @Override
    public List<Member> getMentionedMembers()
    {
        unsupported();
        return null;
    }

    @Override
    public List<IMentionable> getMentions(MentionType... types)
    {
        unsupported();
        return null;
    }

    @Override
    public boolean isMentioned(IMentionable mentionable, MentionType... types)
    {
        unsupported();
        return false;
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
    public Member getMember()
    {
        unsupported();
        return null;
    }

    @Override
    public String getJumpUrl()
    {
        unsupported();
        return null;
    }

    @Override
    public String getContentDisplay()
    {
        unsupported();
        return null;
    }

    @Override
    public String getContentRaw()
    {
        return content;
    }

    @Override
    public String getContentStripped()
    {
        unsupported();
        return null;
    }

    @Override
    public List<String> getInvites()
    {
        unsupported();
        return null;
    }

    @Override
    public String getNonce()
    {
        return nonce;
    }

    @Override
    public boolean isFromType(ChannelType type)
    {
        return type == ChannelType.TEXT;
    }

    @Override
    public ChannelType getChannelType()
    {
        unsupported();
        return null;
    }

    @Override
    public boolean isWebhookMessage()
    {
        return true;
    }

    @Override
    public MessageChannel getChannel()
    {
        unsupported();
        return null;
    }

    @Override
    public PrivateChannel getPrivateChannel()
    {
        unsupported();
        return null;
    }

    @Override
    public Group getGroup()
    {
        unsupported();
        return null;
    }

    @Override
    public TextChannel getTextChannel()
    {
        unsupported();
        return null;
    }

    @Override
    public Category getCategory()
    {
        unsupported();
        return null;
    }

    @Override
    public Guild getGuild()
    {
        unsupported();
        return null;
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
    public List<Emote> getEmotes()
    {
        unsupported();
        return null;
    }

    @Override
    public List<MessageReaction> getReactions()
    {
        unsupported();
        return null;
    }

    @Override
    public boolean isTTS()
    {
        return tts;
    }

    @Override
    public MessageAction editMessage(CharSequence newContent)
    {
        unsupported();
        return null;
    }

    @Override
    public MessageAction editMessage(MessageEmbed newContent)
    {
        unsupported();
        return null;
    }

    @Override
    public MessageAction editMessageFormat(String format, Object... args)
    {
        unsupported();
        return null;
    }

    @Override
    public MessageAction editMessage(Message newContent)
    {
        unsupported();
        return null;
    }

    @Override
    public AuditableRestAction<Void> delete()
    {
        unsupported();
        return null;
    }

    @Override
    public JDA getJDA()
    {
        unsupported();
        return null;
    }

    @Override
    public boolean isPinned()
    {
        return pinned;
    }

    @Override
    public RestAction<Void> pin()
    {
        unsupported();
        return null;
    }

    @Override
    public RestAction<Void> unpin()
    {
        unsupported();
        return null;
    }

    @Override
    public RestAction<Void> addReaction(Emote emote)
    {
        unsupported();
        return null;
    }

    @Override
    public RestAction<Void> addReaction(String unicode)
    {
        unsupported();
        return null;
    }

    @Override
    public RestAction<Void> clearReactions()
    {
        unsupported();
        return null;
    }

    @Override
    public MessageType getType()
    {
        return MessageType.DEFAULT;
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision)
    {
        unsupported();
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
     * @return immutable set of mentioned Role ids
     */
    public Set<Long> getMentionedRoleIds()
    {
        return mentionedRoles;
    }

    protected void unsupported()
    {
        throw new UnsupportedOperationException("This operation is not supported for Messages that are from webhooks!\n" +
            String.format("Use JDA#getTextChannelById(%s)#getMessageById(%s) to perform this action on the message", channelId, id));
    }

    /**
     * Represents an attachment on a {@link net.dv8tion.jda.webhook.SendWebhookMessage WebhookMessage}
     */
    static class Attachment extends Message.Attachment
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
