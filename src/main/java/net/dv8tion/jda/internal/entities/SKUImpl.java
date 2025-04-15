package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.SKU;
import net.dv8tion.jda.api.entities.SKUFlag;
import net.dv8tion.jda.api.entities.SKUType;

import java.util.Set;

public class SKUImpl extends SkuSnowflakeImpl implements SKU
{
    // As described in https://discord.com/developers/docs/resources/sku#sku-object-sku-types
    private final SKUType type;
    private final String name;
    private final String slug;
    private final Set<SKUFlag> flags;

    public SKUImpl(long id, SKUType type, String name, String slug, Set<SKUFlag> flags)
    {
        super(id);
        this.type = type;
        this.name = name;
        this.slug = slug;
        this.flags = flags;
    }

    @Override
    public SKUType getType()
    {
        return type;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getSlug()
    {
        return slug;
    }

    @Override
    public Set<SKUFlag> getFlags()
    {
        return flags;
    }
}
