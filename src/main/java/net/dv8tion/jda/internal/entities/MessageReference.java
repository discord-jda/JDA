package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.Message;

public class MessageReference
{
    private final long messageId;
    private final long channelId;
    private final Message referencedMessage;

    public MessageReference(long messageId, long channelId, Message referencedMessage)
    {
        this.messageId = messageId;
        this.channelId = channelId;
        this.referencedMessage = referencedMessage;
    }

    public Message getReferencedMessage()
    {
        return referencedMessage;
    }

    public long getMessageIdLong()
    {
        return messageId;
    }

    public long getChannelIdLong()
    {
        return channelId;
    }


    public String getMessageId()
    {
        return Long.toUnsignedString(getMessageIdLong());
    }

    public String getChannelId()
    {
        return Long.toUnsignedString(getChannelIdLong());
    }
}
