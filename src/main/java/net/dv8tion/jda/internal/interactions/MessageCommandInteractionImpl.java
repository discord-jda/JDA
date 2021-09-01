package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.MessageCommandInteraction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import org.jetbrains.annotations.NotNull;

public class MessageCommandInteractionImpl extends CommandInteractionImpl implements MessageCommandInteraction
{
    private final long messageId;
    private final Message message;

    public MessageCommandInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);

        DataObject commandData = data.getObject("data");
        DataObject resolved = commandData.getObject("resolved");

        this.messageId = commandData.getUnsignedLong("target_id");

        this.message = jda.getEntityBuilder().createMessage(resolved.getObject("messages").getObject(Long.toUnsignedString(messageId)), true);
    }

    @Override
    public long getInteractedIdLong()
    {
        return messageId;
    }

    @NotNull
    @Override
    public Message getInteractedMessage()
    {
        return message;
    }
}
