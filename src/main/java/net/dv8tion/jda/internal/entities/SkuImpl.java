package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Sku;

import javax.annotation.Nonnull;

public class SkuImpl implements Sku
{

    private final JDA api;
    private final long id;
    private final SkuType type;
    private final long applicationId;
    private final String name;
    private final String slug;
    private final int flags;

    public SkuImpl(JDA api, long id, SkuType type, long applicationId, String name, String slug, int flags)
    {
        this.api = api;
        this.id = id;
        this.type = type;
        this.applicationId = applicationId;
        this.name = name;
        this.slug = slug;
        this.flags = flags;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Nonnull
    @Override
    public Sku.SkuType getType()
    {
        return type;
    }

    @Override
    public long getApplicationIdLong()
    {
        return applicationId;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Nonnull
    @Override
    public String getSlug()
    {
        return slug;
    }

    @Override
    public int getFlagsRaw()
    {
        return flags;
    }
}
