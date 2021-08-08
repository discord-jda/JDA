package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.restaction.CommandEditAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.restaction.CommandEditActionImpl;

import javax.annotation.Nonnull;

public class ContextMenuCommand extends Command
{
    public ContextMenuCommand(JDAImpl api, Guild guild, DataObject json)
    {
        super(api, guild, json);
    }

    @Nonnull
    @Override
    public CommandEditAction editCommand() {
        if (applicationId != api.getSelfUser().getApplicationIdLong())
            throw new IllegalStateException("Cannot edit a command from another bot!");
        return guild == null ?
                new CommandEditActionImpl(api, getId(), CommandType.USER_COMMAND) :
                new CommandEditActionImpl(guild, getId(), CommandType.USER_COMMAND);
    }

    @Override
    public String toString()
    {
        return "CMC:" + getName() + "(" + getId() + ")";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof Command))
            return false;
        return getIdLong() == ((ContextMenuCommand) obj).getIdLong();
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(getIdLong());
    }
}
