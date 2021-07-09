package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class MessageReference
{
    private final long messageId;
    private final long channelId;
    private final Message referencedMessage;
    private final JDA api;

    public MessageReference(long messageId, long channelId, @Nullable Message referencedMessage, JDA api)
    {
        this.messageId = messageId;
        this.channelId = channelId;
        this.referencedMessage = referencedMessage;
        this.api = api;
    }

    /**
     * Retrieves the referenced message for this message. If one is already present, it will be returned.
     *
     * @throws java.lang.IllegalStateException
     *         If this message does not have a reference
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.Message}
     */
    @Nonnull
    public RestAction<Message> retrieveReferencedMessage()
    {
        Route.CompiledRoute route = Route.Messages.GET_MESSAGE.compile(getChannelId(), getMessageId());
        JDAImpl jda = (JDAImpl) getJDA();

        return new RestActionImpl<>(jda, route,
                (response, request) -> jda.getEntityBuilder().createMessage(response.getObject(), getChannel(), false));
    }

    /**
     * Referenced message.
     *
     * <p>This will have different meaning depending on the {@link Message#getType() type} of message.
     * Usually, this is a {@link MessageType#INLINE_REPLY INLINE_REPLY} reference.
     * This can be null even if the type is {@link MessageType#INLINE_REPLY INLINE_REPLY}, when the message it references doesn't exist or discord wasn't able to resolve it in time.
     *
     * @return The referenced message, or null
     */
    @Nullable
    public Message getReferencedMessage()
    {
        return referencedMessage;
    }

    /**
     * The channel from which this message originates.
     * <br>Messages from other guilds can be referenced, in which case JDA may not have the channel cached.
     *
     * @return The origin channel for this message reference, or null
     */
    @Nullable
    public TextChannel getChannel()
    {
        return api.getTextChannelById(getChannelIdLong());
    }

    /**
     * Returns the message id for this reference.
     *
     * @return The message id
     */
    public long getMessageIdLong()
    {
        return messageId;
    }

    /**
     * Returns the channel id for this reference.
     *
     * @return The channel id
     */
    public long getChannelIdLong()
    {
        return channelId;
    }

    /**
     * Returns the message id for this reference.
     *
     * @return The message id
     */
    public String getMessageId()
    {
        return Long.toUnsignedString(getMessageIdLong());
    }

    /**
     * Returns the channel id for this reference.
     *
     * @return The channel id
     */
    public String getChannelId()
    {
        return Long.toUnsignedString(getChannelIdLong());
    }

    /**
     * Returns the JDA instance related to this message reference.
     *
     * @return The corresponding JDA instance
     */
    public JDA getJDA()
    {
        return api;
    }
}
