package net.dv8tion.jda.api.interactions.commands.build;

import net.dv8tion.jda.api.interactions.commands.CommandType;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;

/**
 * Builder for a Message-Command.
 */
public class MessageCommandData extends CommandData implements CommandDataBase<MessageCommandData>
{
    private boolean defaultPermissions = true; // whether the command uses default_permissions (blacklist/whitelist)

    public MessageCommandData(String name)
    {
        super(name);
    }

    @Nonnull
    @Override
    public CommandType getCommandType()
    {
        return CommandType.MESSAGE;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return super.toData().put("default_permission", defaultPermissions);
    }

    @Nonnull
    @Override
    public MessageCommandData setDefaultEnabled(boolean enabled)
    {
        this.defaultPermissions = enabled;
        return this;
    }

    @Nonnull
    @Override
    public MessageCommandData setName(@Nonnull String name)
    {
        super.name = name;
        return this;
    }
}
