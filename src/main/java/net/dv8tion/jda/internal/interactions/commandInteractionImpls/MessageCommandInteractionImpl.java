package net.dv8tion.jda.internal.interactions.commandInteractionImpls;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.commandInteractions.MessageCommandInteraction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

import java.util.Arrays;
import java.util.Optional;

public class MessageCommandInteractionImpl extends CommandInteractionImpl implements MessageCommandInteraction
{
    protected final long targetID;
    protected Message targetMessage;

    public MessageCommandInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
        DataObject commandData = data.getObject("data");

        this.targetID = commandData.getLong("target_id");

        Optional<Object> messageOptional = Arrays.stream(resolved.values()).findFirst();
        this.targetMessage = (Message) messageOptional.orElse(null);
    }


    @Override
    public long getTargetIdLong()
    {
        return targetID;
    }

    @Override
    public Message getTargetMessage()
    {
        return targetMessage;
    }
}
