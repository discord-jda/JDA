package net.dv8tion.jda.events.message.priv;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.PrivateChannel;

public class PrivateMessageAcknowledgedEvent extends GenericPrivateMessageEvent
{
    private final String messageId;

    public PrivateMessageAcknowledgedEvent(JDA api, int responseNumber, String messageId, PrivateChannel channel)
    {
        super(api, responseNumber, null, channel);
        this.messageId = messageId;
    }

    public String getMessageId()
    {
        return messageId;
    }
}
