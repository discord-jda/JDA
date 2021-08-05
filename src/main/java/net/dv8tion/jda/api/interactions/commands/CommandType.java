package net.dv8tion.jda.api.interactions.commands;

import javax.annotation.Nonnull;

/**
 * The available types of {@link Command Commands}
 */
public enum CommandType
{
    /** Placeholder for future option types */
    UNKNOWN(-1),
    /**
     * A slash command is the default command that appears once a user types "/"
     */
    SLASH(1),

    /**
     * A user command, also known as a "User Context Menu Command", appears when opening a context menu on a user
     */
    USER_CONTEXT(2),

    /**
     * A message command, also known as a "Message Context Menu Command", appears when opening a context menu on a message
     */
    MESSAGE_CONTEXT(3),
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
