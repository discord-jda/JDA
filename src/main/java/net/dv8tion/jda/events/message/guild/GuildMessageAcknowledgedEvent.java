package net.dv8tion.jda.events.message.guild;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.TextChannel;

public class GuildMessageAcknowledgedEvent extends GenericGuildMessageEvent
{
    private final String messageId;

    public GuildMessageAcknowledgedEvent(JDA api, int responseNumber, String messageId, TextChannel channel)
    {
        super(api, responseNumber, null, channel);
        this.messageId = messageId;
    }

    public String getMessageId()
    {
        return messageId;
    }
}
