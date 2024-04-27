package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Sku;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;

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

    @Nonnull
    @Override
    public RestAction<Void> createPurchaseDiscount(long userId, int percentOff, int ttl)
    {
        if (percentOff < 1 || percentOff > 100)
            throw new IllegalArgumentException("Percent off must be between 1 and 100");

        if (ttl < 60 || ttl > 3600)
            throw new IllegalArgumentException("TTL must be between 60 and 3600 seconds");

        Route.CompiledRoute route = Route.Store.CREATE_PURCHASE_DISCOUNT.compile(getId(), String.valueOf(userId));

        DataObject body = DataObject.empty();
        body.put("percent_off", percentOff);
        body.put("ttl", ttl);

        return new RestActionImpl<>(this.api, route, body);
    }

    @Nonnull
    @Override
    public RestAction<Void> deletePurchaseDiscount(long userId)
    {
        Route.CompiledRoute route = Route.Store.DELETE_PURCHASE_DISCOUNT.compile(getId(), String.valueOf(userId));
        return new RestActionImpl<>(this.api, route);
    }
}
