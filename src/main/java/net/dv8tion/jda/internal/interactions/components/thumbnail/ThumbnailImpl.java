package net.dv8tion.jda.internal.interactions.components.thumbnail;

import net.dv8tion.jda.api.interactions.components.section.SectionAccessoryComponentUnion;
import net.dv8tion.jda.api.interactions.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

public class ThumbnailImpl
        extends AbstractComponentImpl
        implements Thumbnail, SectionAccessoryComponentUnion
{
    private final int uniqueId;
    private final String url;

    public ThumbnailImpl(String url)
    {
        this(-1, url);
    }

    private ThumbnailImpl(int uniqueId, String url)
    {
        this.uniqueId = uniqueId;
        this.url = url;
    }

    @Nonnull
    @Override
    public Thumbnail withUniqueId(int uniqueId)
    {
        Checks.notNegative(uniqueId, "Unique ID");
        return new ThumbnailImpl(uniqueId, url);
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Override
    public boolean isSpoiler()
    {
        return false;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.THUMBNAIL;
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
                .put("media", DataObject.empty().put("url", url));
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        return json;
    }
}
