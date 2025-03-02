package net.dv8tion.jda.internal.interactions.components.media_gallery;

import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.interactions.components.media_gallery.MediaGallery;
import net.dv8tion.jda.api.interactions.components.media_gallery.MediaGalleryItem;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MediaGalleryImpl
        extends AbstractComponentImpl
        implements MediaGallery, MessageTopLevelComponentUnion, ContainerChildComponentUnion
{
    private final int uniqueId;
    private final List<MediaGalleryItem> items;

    public MediaGalleryImpl(DataObject data)
    {
        this(
                data.getInt("id"),
                data.getArray("items")
                        .stream(DataArray::getObject)
                        .map(MediaGalleryItemImpl::new)
                        .collect(Collectors.toList())
        );
    }

    public MediaGalleryImpl(Collection<? extends MediaGalleryItem> items)
    {
        this(-1, items);
    }

    private MediaGalleryImpl(int uniqueId, Collection<? extends MediaGalleryItem> items)
    {
        this.uniqueId = uniqueId;
        this.items = Helpers.copyAsUnmodifiableList(items);
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.MEDIA_GALLERY;
    }

    @Nonnull
    @Override
    public MediaGallery withUniqueId(int uniqueId)
    {
        Checks.notNegative(uniqueId, "Unique ID");
        return new MediaGalleryImpl(uniqueId, items);
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Nonnull
    @Override
    public List<MediaGalleryItem> getItems()
    {
        return items;
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
                .put("items", DataArray.fromCollection(getItems()));
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        return json;
    }
}
