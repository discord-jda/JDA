package net.dv8tion.jda.internal.interactions.components.thumbnail;

import net.dv8tion.jda.api.interactions.components.section.SectionAccessoryComponentUnion;
import net.dv8tion.jda.api.interactions.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;

import javax.annotation.Nonnull;

public class ThumbnailImpl
        extends AbstractComponentImpl
        implements Thumbnail, SectionAccessoryComponentUnion
{
    private final String url;

    public ThumbnailImpl(String url)
    {
        this.url = url;
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
        return DataObject.empty()
                .put("type", getType().getKey())
                .put("media", DataObject.empty().put("url", url));
    }
}
