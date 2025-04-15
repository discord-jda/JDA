package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.SKUType;

public class SKUImpl extends SkuSnowflakeImpl implements net.dv8tion.jda.api.entities.SKU
{
    // As described in https://discord.com/developers/docs/resources/sku#sku-object-sku-types
    private final SKUType type;
    private final String name;
    private final String slug;
    private final int flags;

    /*
    More undocumented fields are:
    dependent_sku_id: Probably for up and downgrading of tiers.
    manifest_label: Unclear
    access_type: Unclear. Type integer
    features: Probably an array of strings containing the feature descriptions
    release_date: The date the SKU was published. Type unclear. UNIX timestamp?
    show_age_gate: Probably if this SKU has an age gap. Doesnt seem to be supported to far. Type: Boolean
     */

    public SKUImpl(long id, SKUType type, String name, String slug, int flags)
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
    public int getFlags()
    {
        return flags;
    }
}
