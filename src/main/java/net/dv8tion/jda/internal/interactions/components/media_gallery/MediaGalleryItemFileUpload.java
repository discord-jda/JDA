package net.dv8tion.jda.internal.interactions.components.media_gallery;

import net.dv8tion.jda.api.interactions.components.ResolvedMedia;
import net.dv8tion.jda.api.interactions.components.media_gallery.MediaGalleryItem;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class MediaGalleryItemFileUpload implements MediaGalleryItem
{
    private final FileUpload file; // Contains name and description
    private final String description;
    private final boolean spoiler;

    public MediaGalleryItemFileUpload(FileUpload upload)
    {
        this(upload, null, false);
    }

    public MediaGalleryItemFileUpload(FileUpload file, String description, boolean spoiler)
    {
        this.file = file;
        this.description = description;
        this.spoiler = spoiler;
    }

    @Nonnull
    @Override
    public MediaGalleryItem withDescription(@Nonnull String description)
    {
        return new MediaGalleryItemFileUpload(file, description, spoiler);
    }

    @Nonnull
    @Override
    public MediaGalleryItem withSpoiler(boolean spoiler)
    {
        return new MediaGalleryItemFileUpload(file, description, spoiler);
    }

    @Nonnull
    @Override
    public String getUrl()
    {
        // FileUpload is mutable unfortunately
        return "attachment://" + file.getName();
    }

    @Nonnull
    public FileUpload getFile()
    {
        return file;
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
        // FileUpload is mutable unfortunately
        final String fileDescription = file.getDescription();
        if (fileDescription != null)
            return fileDescription;
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
                .put("media", DataObject.empty().put("url", getUrl()));
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof MediaGalleryItemFileUpload)) return false;
        MediaGalleryItemFileUpload that = (MediaGalleryItemFileUpload) o;
        return spoiler == that.spoiler && Objects.equals(file, that.file) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(file, description, spoiler);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("file", file)
                .addMetadata("spoiler", spoiler)
                .addMetadata("description", getDescription())
                .toString();
    }
}
