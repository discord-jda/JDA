package net.dv8tion.jda.internal.interactions;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

import javax.annotation.Nonnull;

public abstract class CommandInteractionImpl extends InteractionImpl implements CommandInteraction
{

    protected final long commandId;
    protected final String name;

    public CommandInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
        DataObject commandData = data.getObject("data");
        DataObject resolveJson = commandData.optObject("resolved").orElseGet(DataObject::empty);
        this.commandId = commandData.getUnsignedLong("id");
        this.name = commandData.getString("name");

        parseResolved(jda, resolveJson);
    }

    protected abstract void parseResolved(JDAImpl jda, DataObject resolveJson);

    @Nonnull
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

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public MessageChannel getChannel()
    {
        return (MessageChannel) super.getChannel();
    }
}
