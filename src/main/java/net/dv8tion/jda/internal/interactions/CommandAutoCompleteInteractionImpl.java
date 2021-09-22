package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

public class CommandAutoCompleteInteractionImpl extends CommandInteractionImpl implements CommandAutoCompleteInteraction
{
    public CommandAutoCompleteInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
    }
}
