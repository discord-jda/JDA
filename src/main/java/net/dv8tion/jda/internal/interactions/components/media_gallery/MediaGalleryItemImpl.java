package net.dv8tion.jda.internal.interactions.components.media_gallery;

import net.dv8tion.jda.api.interactions.components.ResolvedMedia;
import net.dv8tion.jda.api.interactions.components.media_gallery.MediaGalleryItem;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MediaGalleryItemImpl implements MediaGalleryItem
{
    private final String url, description;
    private final boolean spoiler;

    public MediaGalleryItemImpl(String url)
    {
        this(url, null, false);
    }

    public MediaGalleryItemImpl(String url, String description, boolean spoiler)
    {
        this.url = url;
        this.description = description;
        this.spoiler = spoiler;
    }

    @Nonnull
    @Override
    public MediaGalleryItem withDescription(@Nonnull String description)
    {
        return new MediaGalleryItemImpl(url, description, spoiler);
    }

    @Nonnull
    @Override
    public MediaGalleryItem withSpoiler(boolean spoiler)
    {
        return new MediaGalleryItemImpl(url, description, spoiler);
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

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
                .put("media", DataObject.empty().put("url", url));
    }
}
