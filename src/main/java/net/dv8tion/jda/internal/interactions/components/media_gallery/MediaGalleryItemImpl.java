package net.dv8tion.jda.internal.interactions.components.media_gallery;

import net.dv8tion.jda.api.interactions.components.ResolvedMedia;
import net.dv8tion.jda.api.interactions.components.media_gallery.MediaGalleryItem;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.components.ResolvedMediaImpl;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class MediaGalleryItemImpl implements MediaGalleryItem
{
    private final String url, description;
    private final ResolvedMedia media;
    private final boolean spoiler;

    public MediaGalleryItemImpl(DataObject obj)
    {
        this(
                obj.getObject("media").getString("url"),
                obj.getString("description", null),
                new ResolvedMediaImpl(obj.getObject("media")),
                obj.getBoolean("spoiler", false)
        );
    }

    public MediaGalleryItemImpl(String url)
    {
        this(url, null, null, false);
    }

    public MediaGalleryItemImpl(String url, String description, ResolvedMedia media, boolean spoiler)
    {
        this.url = url;
        this.media = media;
        this.description = description;
        this.spoiler = spoiler;
    }

    @Nonnull
    @Override
    public MediaGalleryItem withDescription(@Nonnull String description)
    {
        return new MediaGalleryItemImpl(url, description, media, spoiler);
    }

    @Nonnull
    @Override
    public MediaGalleryItem withSpoiler(boolean spoiler)
    {
        return new MediaGalleryItemImpl(url, description, media, spoiler);
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
        return media;
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

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof MediaGalleryItemImpl)) return false;
        MediaGalleryItemImpl that = (MediaGalleryItemImpl) o;
        return spoiler == that.spoiler && Objects.equals(url, that.url) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(url, description, spoiler);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("url", url)
                .addMetadata("media", media)
                .addMetadata("spoiler", spoiler)
                .addMetadata("description", description)
                .toString();
    }
}
