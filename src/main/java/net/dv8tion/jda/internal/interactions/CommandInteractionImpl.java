package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import org.jetbrains.annotations.NotNull;

public abstract class CommandInteractionImpl extends InteractionImpl implements CommandInteraction
{
    private final long commandId;
    private final String name;

    public CommandInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);

        DataObject commandData = data.getObject("data");
        this.commandId = commandData.getUnsignedLong("id");
        this.name = commandData.getString("name");
    }

    @NotNull
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public long getCommandIdLong()
    {
        return commandId;
    }
}
