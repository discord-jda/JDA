package net.dv8tion.jda.internal.interactions.components.file;

import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.ResolvedMedia;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.interactions.components.file.File;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FileImpl extends AbstractComponentImpl implements File, MessageTopLevelComponentUnion, ContainerChildComponentUnion
{
    private final int uniqueId;
    private final String url;
    private final boolean spoiler;

    public FileImpl(String url)
    {
        this(-1, url, false);
    }

    private FileImpl(int uniqueId, String url, boolean spoiler)
    {
        this.uniqueId = uniqueId;
        this.url = url;
        this.spoiler = spoiler;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.FILE;
    }

    @Nonnull
    @Override
    public File withUniqueId(int uniqueId)
    {
        return new FileImpl(uniqueId, url, spoiler);
    }

    @Nonnull
    @Override
    public File withUrl(@Nonnull String url)
    {
        return new FileImpl(uniqueId, url, spoiler);
    }

    @Nonnull
    @Override
    public File withSpoiler(boolean spoiler)
    {
        return new FileImpl(uniqueId, url, spoiler);
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
                .put("file", DataObject.empty().put("url", url))
                .put("spoiler", spoiler);
        if (uniqueId >= -1)
            json.put("id", uniqueId);
        return json;
    }
}
