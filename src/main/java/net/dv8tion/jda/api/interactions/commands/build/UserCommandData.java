package net.dv8tion.jda.api.interactions.commands.build;

import net.dv8tion.jda.api.interactions.commands.CommandType;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;

/**
 * Builder for a User-Command.
 */
public class UserCommandData extends CommandData implements CommandDataBase<UserCommandData>
{
    private boolean defaultPermissions = true; // whether the command uses default_permissions (blacklist/whitelist)

    public UserCommandData(String name)
    {
        super(name);
    }

    @Nonnull
    @Override
    public CommandType getCommandType()
    {
        return CommandType.USER;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject obj = super.toData().put("default_permission", defaultPermissions);
        System.out.println(obj);
        return obj;
    }

    @Nonnull
    @Override
    public UserCommandData setDefaultEnabled(boolean enabled)
    {
        this.defaultPermissions = enabled;
        return this;
    }

    @Nonnull
    @Override
    public UserCommandData setName(@Nonnull String name)
    {
        super.name = name;
        return this;
    }
}
