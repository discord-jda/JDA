package net.dv8tion.jda.internal.interactions.components.thumbnail;

import net.dv8tion.jda.api.interactions.components.ResolvedMedia;
import net.dv8tion.jda.api.interactions.components.section.SectionAccessoryComponentUnion;
import net.dv8tion.jda.api.interactions.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ThumbnailImpl
        extends AbstractComponentImpl
        implements Thumbnail, SectionAccessoryComponentUnion
{
    private final int uniqueId;
    private final String url;
    private final String description;
    private final boolean spoiler;

    public ThumbnailImpl(String url)
    {
        this(-1, url, null, false);
    }

    private ThumbnailImpl(int uniqueId, String url, String description, boolean spoiler)
    {
        this.uniqueId = uniqueId;
        this.url = url;
        this.description = description;
        this.spoiler = spoiler;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.THUMBNAIL;
    }

    @Nonnull
    @Override
    public Thumbnail withUniqueId(int uniqueId)
    {
        Checks.notNegative(uniqueId, "Unique ID");
        return new ThumbnailImpl(uniqueId, url, description, spoiler);
    }

    @Nonnull
    @Override
    public Thumbnail withUrl(@Nonnull String url)
    {
        Checks.notNull(url, "URL");
        return new ThumbnailImpl(uniqueId, url, description, spoiler);
    }

    @Nonnull
    @Override
    public Thumbnail withDescription(String description)
    {
        return new ThumbnailImpl(uniqueId, url, description, spoiler);
    }

    @Nonnull
    @Override
    public Thumbnail withSpoiler(boolean spoiler)
    {
        return new ThumbnailImpl(uniqueId, url, description, spoiler);
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Nonnull
    @Override
    public String getUrl()
    {
        return url;
    }

    @Nullable
    @Override
    public ResolvedMedia getResolvedMedia()
    {
        return null;
    }

    @Nullable
    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public boolean isSpoiler()
    {
        return spoiler;
    }

    @Override
    public boolean isMessageCompatible()
    {
        return true;
    }

    @Override
    public boolean isModalCompatible()
    {
        return false;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        final DataObject json = DataObject.empty()
                .put("type", getType().getKey())
                .put("media", DataObject.empty().put("url", url))
                .put("spoiler", spoiler);
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        if (description != null)
            json.put("description", description);
        return json;
    }
}
