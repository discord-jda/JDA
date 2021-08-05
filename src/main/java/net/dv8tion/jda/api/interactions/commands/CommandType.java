package net.dv8tion.jda.api.interactions.commands;

import javax.annotation.Nonnull;

public enum CommandType
{
    /** Placeholder for future option types */
    UNKNOWN(-1),
    SLASH_COMMAND(1),
    USER_COMMAND(2),
    MESSAGE_COMMAND(3),
    ;

    private final int raw;

    CommandType(int raw)
    {
        this.raw = raw;
    }

    /**
     * The raw value for this type or -1 for {@link #UNKNOWN}
     *
     * @return The raw value
     */
    public int getKey()
    {
        return raw;
    }

    /**
     * Converts the provided raw type to the enum constant.
     *
     * @param  key
     *         The raw type
     *
     * @return The CommandType constant or {@link #UNKNOWN}
     */
    @Nonnull
    public static CommandType fromKey(int key)
    {
        for (CommandType type : values())
        {
            if (type.raw == key)
                return type;
        }
        return UNKNOWN;
    }
}
