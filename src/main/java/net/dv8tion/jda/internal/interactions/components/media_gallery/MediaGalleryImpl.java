package net.dv8tion.jda.internal.interactions.components.media_gallery;

import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.interactions.components.media_gallery.MediaGallery;
import net.dv8tion.jda.api.interactions.components.media_gallery.MediaGalleryItem;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class MediaGalleryImpl
        extends AbstractComponentImpl
        implements MediaGallery, MessageTopLevelComponentUnion, ContainerChildComponentUnion
{
    private final MediaGalleryItem[] items;

    public MediaGalleryImpl(MediaGalleryItem... items)
    {
        this.items = items;
    }

    @Override
    public List<MediaGalleryItem> getItems()
    {
        return Arrays.asList(items);
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.MEDIA_GALLERY;
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
        return DataObject.empty()
                .put("type", getType().getKey())
                .put("items", DataArray.fromCollection(getItems()));
    }
}
