package net.dv8tion.jda;

import net.dv8tion.jda.api.entities.ISnowflake;

public class ASnowflake implements ISnowflake
{
    @Override
    public long getIdLong()
    {
        return 42;
    }
}
