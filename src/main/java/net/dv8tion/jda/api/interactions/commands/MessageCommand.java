package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

/**
 * Represents a Discord message-command.
 * <br>This can be used to edit or delete the command.
 *
 * @see Guild#retrieveCommandById(String)
 * @see Guild#retrieveCommands()
 */
public class MessageCommand extends Command
{

    public MessageCommand(JDAImpl api, Guild guild, DataObject json)
    {
        super(api, guild, json);
    }

    @Override
    public CommandType getType()
    {
        return CommandType.MESSAGE;
    }
}
