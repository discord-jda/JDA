package net.dv8tion.jda.api.interactions.commands;

public enum CommandType
{
    SLASH(1),
    USER(2),
    MESSAGE(3),
    UNKNOWN(-1);

    private final int id;

    CommandType(int id)
    {
        this.id = id;
    }

    /**
     * The Discord id key used to represent the channel type.
     *
     * @return The id key used by discord for this channel type.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Static accessor for retrieving a channel type based on its Discord id key.
     *
     * @param  id
     *         The id key of the requested channel type.
     *
     * @return The ChannelType that is referred to by the provided key. If the id key is unknown, {@link #UNKNOWN} is returned.
     */
    public static CommandType fromId(int id)
    {
        for (CommandType type : values())
        {
            if (type.id == id)
                return type;
        }
        return UNKNOWN;
    }
}
