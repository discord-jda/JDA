package net.dv8tion.jda.api.entities;

public interface SKU extends SkuSnowflake
{
    SKUType getType();

    String getName();

    String getSlug();

    int getFlags();
}
