package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

public class MemberCommand extends Command
{
    public MemberCommand(JDAImpl api, Guild guild, DataObject json)
    {
        super(api, guild, json);
    }

    @Override
    public String toString()
    {
        return "MC:" + getName() + "(" + getId() + ")";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof Command))
            return false;
        return getIdLong() == ((MemberCommand) obj).getIdLong();
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(getIdLong());
    }
}
