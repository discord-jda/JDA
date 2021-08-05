package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

public class UserCommand extends Command
{
    public UserCommand(JDAImpl api, Guild guild, DataObject json)
    {
        super(api, guild, json);
    }
}
