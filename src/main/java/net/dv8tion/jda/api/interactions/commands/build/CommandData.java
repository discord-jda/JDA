package net.dv8tion.jda.api.interactions.commands.build;

import net.dv8tion.jda.api.interactions.commands.CommandType;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

public abstract class CommandData implements SerializableData
{
    protected String name;

    public CommandData(String name) {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 32, "Name");
        this.name = name;
    }

    /**
     * The command type
     *
     * @return The command type
     */
    @Nonnull
    public abstract CommandType getCommandType();

    /**
     * The configured name
     *
     * @return The name
     */
    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
                .put("name", getName())
                .put("type", getCommandType().getKey());
    }
}
