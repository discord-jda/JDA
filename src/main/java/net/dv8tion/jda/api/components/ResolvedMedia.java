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

package net.dv8tion.jda.api.components;

import net.dv8tion.jda.api.entities.Placeholder;
import net.dv8tion.jda.api.utils.AttachmentProxy;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A media resolved by Discord including some metadata,
 * typically comes from {@linkplain net.dv8tion.jda.api.entities.Message.MessageFlag#IS_COMPONENTS_V2 V2 Components}.
 */
public interface ResolvedMedia {
    /**
     * The ID of the attachment represented by this resolved media,
     * may return {@code null} if this media was created from an external link.
     *
     * @return The ID of the attachment, or {@code null}
     */
    @Nullable
    String getAttachmentId();

    /**
     * The ID of the attachment represented by this resolved media,
     * may return {@code null} if this media was created from an external link.
     *
     * @return The ID of the attachment, or {@code null}
     */
    @Nullable
    default Long getAttachmentIdLong() {
        String attachmentId = getAttachmentId();
        return attachmentId != null ? Long.parseUnsignedLong(attachmentId) : null;
    }

    /**
     * The URL of this media, for locally-uploaded files, this will always be a URL from Discord's CDN,
     * in other cases it <i>may</i> be an external URL.
     *
     * <p>If you want to download the file, you should use {@link #getProxy()}.
     *
     * @return The URL of this media
     */
    @Nonnull
    String getUrl();

    /**
     * The URL of this media, proxied by Discord's CDN.
     *
     * <p>This URL may be invalid if the media failed to load.
     *
     * <p>If you want to download the file, you should use {@link #getProxy()}.
     *
     * @return The proxy URL of this media
     */
    @Nonnull
    String getProxyUrl();

    /**
     * An {@link AttachmentProxy} for this media.
     * <br>This allows you to easily download the media.
     *
     * <p>This proxy may not be usable if the media failed to load.
     *
     * @return The {@link AttachmentProxy} of this media
     */
    @Nonnull
    AttachmentProxy getProxy();

    /**
     * The width of this media, if available, or {@code 0}.
     *
     * <p>This may be {@code 0} if the media failed to load.
     *
     * @return Width of this media, or {@code 0}
     */
    int getWidth();

    /**
     * The height of this media, if available, or {@code 0}.
     *
     * <p>This may be {@code 0} if the media failed to load.
     *
     * @return Height of this media, or {@code 0}
     */
    int getHeight();

    /**
     * The <a href="https://en.wikipedia.org/wiki/Media_type" target="_blank">media type</a>,
     * if available, or {@code null}.
     *
     * <p>This may be absent if the media failed to load.
     *
     * @return The media type, or {@code null}
     */
    @Nullable
    String getContentType();

    /**
     * The placeholder, if this is an image or video, or {@code null}.
     *
     * @return The placeholder or {@code null}
     *
     * @see    Placeholder
     */
    @Nullable
    Placeholder getPlaceholder();

    /**
     * Returns the raw media flags of this media.
     *
     * @return The raw media flags
     *
     * @see    #getFlags()
     */
    long getFlagsRaw();

    /**
     * Returns an unmodifiable set of all {@link ResolvedMediaFlag ResolvedMediaFlags} present for this media.
     *
     * @return Unmodifiable set of present {@link ResolvedMediaFlag ResolvedMediaFlags}
     *
     * @see    ResolvedMediaFlag
     */
    @Nonnull
    @Unmodifiable
    Set<ResolvedMediaFlag> getFlags();

    /**
     * Known media flags.
     */
    enum ResolvedMediaFlag {
        /**
         * This image is animated.
         */
        IS_ANIMATED(0);

        private final int value;

        ResolvedMediaFlag(int offset) {
            this.value = 1 << offset;
        }

        /**
         * Returns the value of the flag as represented in the bitfield. It is always a power of 2. (single bit)
         *
         * @return Non-zero bit value of the field
         */
        public int getValue() {
            return value;
        }

        /**
         * Given a bitfield, this function extracts all enum values according to their bit values and returns
         * a set containing all matching media flags.
         *
         * @param  bitfield
         *         Non-negative integer representing a bitfield of media flags
         *
         * @return Set of media flags found in the bitfield
         */
        @Nonnull
        public static EnumSet<ResolvedMediaFlag> fromBitField(int bitfield) {
            return Arrays.stream(ResolvedMediaFlag.values())
                    .filter(e -> (e.value & bitfield) > 0)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(ResolvedMediaFlag.class)));
        }

        /**
         * Converts a collection of media flags back to the integer representing the bitfield.
         * This is the reverse operation of {@link #fromBitField(int)}.
         *
         * @param  flags
         *         A non-null collection of media flags
         *
         * @throws IllegalArgumentException
         *         If the provided collection is {@code null}
         *
         * @return Integer value of the bitfield representing the given media flags
         */
        public static int toBitField(@Nonnull Collection<ResolvedMediaFlag> flags) {
            Checks.notNull(flags, "Flags");
            int rawFlags = 0;
            for (ResolvedMediaFlag flag : flags) {
                rawFlags |= flag.value;
            }
            return rawFlags;
        }
    }
}
