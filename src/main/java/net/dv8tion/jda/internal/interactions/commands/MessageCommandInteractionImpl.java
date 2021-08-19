package net.dv8tion.jda.internal.interactions.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.interactions.MessageCommandInteraction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

import javax.annotation.Nonnull;
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
        
        //Assigning null would break @Nonnull in #getTargetMessage, this should be a discord bug if there's no target message
        this.targetMessage = (Message) messageOptional.orElseThrow();
    }


    @Override
    public long getTargetIdLong()
    {
        return targetID;
    }

    @Override
    @Nonnull
    public Message getTargetMessage()
    {
        return targetMessage;
    }
}
