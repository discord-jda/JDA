package net.dv8tion.jda;

import net.dv8tion.jda.api.entities.ISnowflake;

class ASnowflake implements ISnowflake
{
    @Override
    public long getIdLong()
    {
        return 42;
    }
}
