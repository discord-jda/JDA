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

package net.dv8tion.jda.api.components.filedisplay;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.ResolvedMedia;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import net.dv8tion.jda.internal.components.filedisplay.FileDisplayFileUpload;
import net.dv8tion.jda.internal.components.filedisplay.FileDisplayImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;

/**
 * Component displaying a file, you can mark it as a spoiler.
 *
 * <p>This will appear as a generic download-able file,
 * meaning that audio files and text files cannot be played/previewed.
 * <br>You can instead use {@linkplain net.dv8tion.jda.api.components.mediagallery.MediaGallery media galleries} to display images.
 *
 * <p><b>Requirements:</b> {@linkplain MessageRequest#useComponentsV2() Components V2} needs to be enabled!
 */
public interface FileDisplay extends Component, MessageTopLevelComponent, ContainerChildComponent
{
    /**
     * Constructs a new {@link FileDisplay} from the {@link FileUpload}.
     *
     * <p>This method can also be used to upload external resources,
     * such as by using {@link FileUpload#fromData(InputStream, String)},
     * in which case it will re-upload the entire file.
     *
     * <p>This will automatically add the file when building the message,
     * as such you do not need to add it manually (with {@link MessageCreateBuilder#addFiles(FileUpload...)} for example).
     *
     * <p><u>Example</u>
     * <pre><code>
     * MessageChannel channel; // = reference of a MessageChannel
     * // It's recommended to use a more robust HTTP library instead,
     * // such as Java 11+'s HttpClient, or OkHttp (included with JDA), among many other options.
     * InputStream file = new URL("https://http.cat/500").openStream();
     * // You can also replace this with a local file
     * FileDisplay fileDisplay = FileDisplay.fromFile(FileUpload.fromData(file, "cat.png"))
     *     .setDescription("This is a cute car :3");
     * channel.sendComponents(fileDisplay)
     *     .useComponentsV2()
     *     .queue();
     * </code></pre>
     *
     * @param  file
     *         The {@link FileUpload} to display
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return The new {@link FileDisplay}
     */
    @Nonnull
    static FileDisplay fromFile(@Nonnull FileUpload file)
    {
        Checks.notNull(file, "FileUpload");
        return new FileDisplayFileUpload(file);
    }

    /**
     * Constructs a new {@link FileDisplay} with the provided file name.
     *
     * <p>You will need to add the file before building the message,
     * such as with {@link MessageCreateBuilder#addFiles(FileUpload...)}, for example.
     *
     * <p><u>Example</u>
     * <pre><code>
     * MessageChannel channel; // = reference of a MessageChannel
     * // It's recommended to use a more robust HTTP library instead,
     * // such as Java 11+'s HttpClient, or OkHttp (included with JDA), among many other options.
     * InputStream file = new URL("https://http.cat/500").openStream();
     * FileDisplay fileDisplay = FileDisplay.fromFileName("cat.png") // Match the file name in FileUpload
     *     .setDescription("This is a cute car :3");
     * channel.sendComponents(fileDisplay)
     *     // You can also replace this with a local file
     *     .addFiles(FileUpload.fromData(file, "cat.png"))
     *     .useComponentsV2()
     *     .queue();
     * </code></pre>
     *
     * @param  fileName
     *         The name of the file to display
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return The new {@link FileDisplay}
     */
    @Nonnull
    static FileDisplay fromFileName(@Nonnull String fileName)
    {
        Checks.notNull(fileName, "File name");
        return new FileDisplayImpl("attachment://" + fileName);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    FileDisplay withUniqueId(int uniqueId);

    /**
     * Creates a new {@link FileDisplay} with the provided spoiler status.
     * <br>Spoilers are hidden until the user clicks on it.
     *
     * @param  spoiler
     *         The new spoiler status
     *
     * @return The new {@link FileDisplay}
     */
    @Nonnull
    @CheckReturnValue
    FileDisplay withSpoiler(boolean spoiler);

    /**
     * The URL of this file, this is always where the file originally came from.
     * <br>This can be either {@code attachment://filename.extension} or an actual URL.
     *
     * <p>If you want to download the file, you should use {@link #getResolvedMedia()} then {@link ResolvedMedia#getProxy()},
     * to avoid connecting your bot to unknown servers.
     *
     * @return The URL of this file
     */
    @Nonnull
    String getUrl();

    /**
     * The media resolved from this file, this is only available if you receive this component from Discord.
     *
     * @return Possibly-null {@link ResolvedMedia}
     */
    @Nullable
    ResolvedMedia getResolvedMedia();

    /**
     * Whether this file is hidden until the user clicks on it.
     *
     * @return {@code true} if this is hidden by default, {@code false} otherwise
     */
    boolean isSpoiler();
}
