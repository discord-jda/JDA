package net.dv8tion.jda.api.events.interaction;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.MessageCommandInteraction;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.interactions.MessageCommandInteractionImpl;
import org.jetbrains.annotations.NotNull;

public class MessageCommandEvent extends GenericCommandEvent implements MessageCommandInteraction
{
    private final MessageCommandInteraction commandInteraction;
    public MessageCommandEvent(JDAImpl api, long responseNumber, MessageCommandInteractionImpl interaction)
    {
        super(api, responseNumber, interaction);
        this.commandInteraction = interaction;
    }

    @Override
    public long getCommandIdLong()
    {
        return commandInteraction.getCommandIdLong();
    }

    @Override
    public long getInteractedIdLong()
    {
        return commandInteraction.getInteractedIdLong();
    }

    @NotNull
    @Override
    public Message getInteractedMessage()
    {
        return commandInteraction.getInteractedMessage();
    }
}
