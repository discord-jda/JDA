package net.dv8tion.jda.api.entities;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public enum SKUFlag
{
    /**
     * SKU is available for purchase
     */
    AVAILABLE(2),
    /**
     * Recurring SKU that can be purchased by a user and applied to a single server. Grants access to every user in that server.
     */
    GUILD_SUBSCRIPTION(7),
    /**
     * Recurring SKU purchased by a user for themselves. Grants access to the purchasing user in every server.
     */
    USER_SUBSCRIPTION(8);

    private final int offset;
    private final int raw;

    SKUFlag(int offset)
    {
        this.offset = offset;
        this.raw = 1 << offset;
    }

    public int getOffset()
    {
        return offset;
    }

    @Nonnull
    public static EnumSet<SKUFlag> getFlags(int flags)
    {
        if (flags == 0) return EnumSet.noneOf(SKUFlag.class);

        EnumSet<SKUFlag> flagSet = EnumSet.noneOf(SKUFlag.class);
        for (SKUFlag flag : SKUFlag.values())
        {
            if ((flags & flag.raw) == flag.raw) flagSet.add(flag);
        }
        return flagSet;
    }
}
