/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api.components.thumbnail;

import net.dv8tion.jda.api.components.ResolvedMedia;
import net.dv8tion.jda.api.components.section.SectionAccessoryComponent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.internal.components.thumbnail.ThumbnailFileUpload;
import net.dv8tion.jda.internal.components.thumbnail.ThumbnailImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Component displaying a thumbnail, you can mark it as a spoiler and set a description.
 */
public interface Thumbnail extends SectionAccessoryComponent
{
    /**
     * The maximum amount of characters a thumbnail's description can have.
     */
    int MAX_DESCRIPTION_LENGTH = 256;

    /**
     * Constructs a new {@link Thumbnail} from the given URL.
     *
     * @param  url
     *         The URL of the thumbnail to display
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return The new {@link Thumbnail}
     */
    @Nonnull
    static Thumbnail fromUrl(@Nonnull String url)
    {
        Checks.notNull(url, "URL");
        return new ThumbnailImpl(url);
    }

    /**
     * Constructs a new {@link Thumbnail} from the {@link FileUpload}.
     *
     * <p>This will automatically add the file when building the message,
     * as such you do not need to add it manually (with {@link MessageCreateBuilder#addFiles(FileUpload...)} for example).
     *
     * @param  file
     *         The {@link FileUpload} to display
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return The new {@link Thumbnail}
     */
    @Nonnull
    static Thumbnail fromFile(@Nonnull FileUpload file)
    {
        Checks.notNull(file, "FileUpload");
        return new ThumbnailFileUpload(file);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    Thumbnail withUniqueId(int uniqueId);

    /**
     * Creates a new {@link Thumbnail} with the provided description.
     * <br>The description is known as an "alternative text",
     * and must not exceed {@value #MAX_DESCRIPTION_LENGTH} characters.
     *
     * @param  description
     *         The new description
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided, or the description is longer than {@value #MAX_DESCRIPTION_LENGTH} characters.
     *
     * @return The new {@link Thumbnail}
     */
    @Nonnull
    @CheckReturnValue
    Thumbnail withDescription(@Nullable String description);

    /**
     * Creates a new {@link Thumbnail} with the provided spoiler status.
     * <br>Spoilers are hidden until the user clicks on it.
     *
     * @param  spoiler
     *         The new spoiler status
     *
     * @return The new {@link Thumbnail}
     */
    @Nonnull
    @CheckReturnValue
    Thumbnail withSpoiler(boolean spoiler);

    /**
     * The URL of this thumbnail, this is always where the file originally came from.
     * <br>This can be either {@code attachment://filename.extension} or an actual URL.
     *
     * <p>If you want to download the file, you should use {@link #getResolvedMedia()} then {@link ResolvedMedia#getProxy()},
     * to avoid connecting your bot to unknown servers.
     *
     * @return The URL of this thumbnail
     */
    @Nonnull
    String getUrl();

    /**
     * The media resolved from this thumbnail, this is only available if you receive this component from Discord.
     *
     * @return Possibly-null {@link ResolvedMedia}
     */
    @Nullable
    ResolvedMedia getResolvedMedia();

    /**
     * The description of this thumbnail, or {@code null} if none has been set.
     *
     * @return Possibly-null description
     */
    @Nullable
    String getDescription();

    /**
     * Whether this thumbnail is hidden until the user clicks on it.
     *
     * @return {@code true} if this is hidden by default, {@code false} otherwise
     */
    boolean isSpoiler();

}
