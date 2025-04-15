package net.dv8tion.jda.api.entities;

public enum SKUFlag
{
    AVAILABLE(2),
    GUILD_SUBSCRIPTION(7),
    USER_SUBSCRIPTION(8);

    private final int offset;

    SKUFlag(int offset)
    {
        this.offset = offset;
    }

    public int getOffset()
    {
        return offset;
    }
}
