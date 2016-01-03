package net.dv8tion.jda.events.message;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.Event;

public class MessageAcknowledgedEvent extends Event
{
    private final boolean isPrivate;
    private final String messageId;
    private final String channelId;

    public MessageAcknowledgedEvent(JDA api, int responseNumber, String messageId, String channelId, boolean isPrivate)
    {
        super(api, responseNumber);
        this.messageId = messageId;
        this.channelId = channelId;
        this.isPrivate = isPrivate;
    }

    public String getMessageId()
    {
        return messageId;
    }

    public TextChannel getTextChannel()
    {
        return getJDA().getTextChannelById(channelId);
    }

    public PrivateChannel getPrivateChannel()
    {
        return getJDA().getPrivateChannelById(channelId);
    }

    public Guild getGuild()
    {
        return isPrivate ? null : getTextChannel().getGuild();
    }
}
