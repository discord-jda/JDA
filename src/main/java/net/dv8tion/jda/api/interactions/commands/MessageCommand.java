package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.restaction.CommandEditAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.restaction.CommandEditActionImpl;

import javax.annotation.Nonnull;

public class MessageCommand extends Command
{
    public MessageCommand(JDAImpl api, Guild guild, DataObject json)
    {
        super(api, guild, json);
    }

    @Nonnull
    @Override
    public CommandEditAction editCommand() {
        if (applicationId != api.getSelfUser().getApplicationIdLong())
            throw new IllegalStateException("Cannot edit a command from another bot!");
        return guild == null ?
                new CommandEditActionImpl(api, getId(), CommandType.MESSAGE_CONTEXT) :
                new CommandEditActionImpl(guild, getId(), CommandType.MESSAGE_CONTEXT);
    }

    @Override
    public String toString()
    {
        return "MC:" + getName() + "(" + getId() + ")";
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(getIdLong());
    }
}
