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

package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Represents a type of file, you can use presets such as {@link #IMAGE},
 * or specify an extension using {@link #ofExtension(String)}.
 *
 * <p>Remember that this only checks the extension, Discord does not check for any file signature/MIME type,
 * and thus does not guarantee receiving a valid file.
 */
public final class FileType {
    private static final Pattern EXTENSION_PATTERN = Pattern.compile("[\\w\\-.]+");

    /** Matches any image supported by the Discord client. */
    public static final FileType IMAGE = new FileType("image");
    /** Matches any video supported by the Discord client. */
    public static final FileType VIDEO = new FileType("video");
    /** Matches any audio supported by the Discord client. */
    public static final FileType AUDIO = new FileType("audio");

    private final String value;

    @ApiStatus.Internal
    public FileType(String value) {
        this.value = value;
    }

    /**
     * Creates a {@link FileType} matching the provided extension.
     *
     * @param  extension
     *         The extension to match against.
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the extension is {@code null} or empty</li>
     *             <li>If the extension does not match {@code [\w\-.]+} (latin letters, digits, dashes or dots)</li>
     *         </ul>
     *
     * @return The new {@link FileType}
     */
    @Nonnull
    public static FileType ofExtension(@Nonnull String extension) {
        Checks.matches(extension, EXTENSION_PATTERN, "Extension");
        return new FileType("." + extension);
    }

    /**
     * Whether this file type accepts all image formats supported by Discord.
     *
     * @return {@code true} if this file type accepts all image formats supported by Discord, {@code false} if not
     */
    public boolean isImage() {
        return value.equals("image");
    }

    /**
     * Whether this file type accepts all video formats supported by Discord.
     *
     * @return {@code true} if this file type accepts all video formats supported by Discord, {@code false} if not
     */
    public boolean isVideo() {
        return value.equals("video");
    }

    /**
     * Whether this file type accepts all audio formats supported by Discord.
     *
     * @return {@code true} if this file type accepts all audio formats supported by Discord, {@code false} if not
     */
    public boolean isAudio() {
        return value.equals("audio");
    }

    /**
     * The raw value of this file type.
     *
     * @return The raw value
     */
    @Nonnull
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileType)) {
            return false;
        }
        FileType that = (FileType) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .setName("FileType")
                .addMetadata("value", value)
                .toString();
    }
}
