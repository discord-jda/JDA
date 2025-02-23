package net.dv8tion.jda.internal.interactions.components.media_gallery;

import net.dv8tion.jda.api.interactions.components.media_gallery.MediaGalleryItem;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;

public class MediaGalleryItemImpl implements MediaGalleryItem
{
    private final String url;

    public MediaGalleryItemImpl(String url)
    {
        this.url = url;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
                .put("media", DataObject.empty().put("url", url));
    }
}
