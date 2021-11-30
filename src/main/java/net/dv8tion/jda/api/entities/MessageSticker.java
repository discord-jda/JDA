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
package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * An object representing a sticker in a Discord message.
 *
 * @see Message#getStickers()
 */
public class MessageSticker implements ISnowflake
{
    private final long id;
    private final String name;
    private final String description;
    private final long packId;
    private final StickerFormat formatType;
    private final Set<String> tags;

    /** Template for {@link #getIconUrl()} */
    public static final String ICON_URL = "https://cdn.discordapp.com/stickers/%s.%s";

    public MessageSticker(final long id, final String name, final String description, final long packId, final StickerFormat formatType, final Set<String> tags)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.packId = packId;
        this.formatType = formatType;
        this.tags = tags;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    /**
     * The name of the sticker.
     *
     * @return the name of the sticker
     */
    @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     * The description of the sticker or empty String if the sticker doesn't have one.
     *
     * @return Possibly-empty String containing the description of the sticker
     */
    @Nonnull
    public String getDescription()
    {
        return description;
    }

    /**
     * The ID of the pack the sticker is from.
     *
     * <p>If this sticker is from a guild, this will be the guild id instead.
     *
     * @return the ID of the pack the sticker is from
     */
    @Nonnull
    public String getPackId()
    {
        return Long.toUnsignedString(getPackIdLong());
    }

    /**
     * The ID of the pack the sticker is from.
     *
     * <p>If this sticker is from a guild, this will be the guild id instead.
     *
     * @return the ID of the pack the sticker is from
     */
    public long getPackIdLong()
    {
        return packId;
    }

    /**
     * The url of the sticker image.
     *
     * @throws java.lang.IllegalStateException
     *         If the {@link StickerFormat StickerFormat} of this sticker is {@link StickerFormat#UNKNOWN UNKNOWN}
     *
     * @return The image url of the sticker
     */
    @Nonnull
    public String getIconUrl()
    {
        return Helpers.format(ICON_URL, getId(), formatType.getExtension());
    }

    /**
     * The {@link StickerFormat Format} of the sticker.
     *
     * @return the format of the sticker
     */
    @Nonnull
    public StickerFormat getFormatType()
    {
        return formatType;
    }

    /**
     * Set of tags of the sticker. Tags can be used instead of the name of the sticker as aliases.
     *
     * @return Possibly-empty unmodifiable Set of tags of the sticker
     */
    @Nonnull
    public Set<String> getTags()
    {
        return tags;
    }

    public enum StickerFormat
    {
        /**
         * The PNG format.
         */
        PNG(1, "png"),
        /**
         * The APNG format.
         */
        APNG(2, "apng"),
        /**
         * The LOTTIE format.
         * <br>Lottie isn't a standard renderable image. It is a JSON with data that can be rendered using the lottie library.
         *
         * @see <a href="https://airbnb.io/lottie/">Lottie website</a>
         */
        LOTTIE(3, "json"),
        /**
         * Represents any unknown or unsupported {@link net.dv8tion.jda.api.entities.MessageSticker MessageSticker} format types.
         */
        UNKNOWN(-1, null);

        private final int id;
        private final String extension;

        StickerFormat(final int id, final String extension)
        {
            this.id = id;
            this.extension = extension;
        }

        /**
         * The file extension used for the sticker asset.
         *
         * @throws java.lang.IllegalStateException
         *         If the {@link StickerFormat StickerFormat} is {@link StickerFormat#UNKNOWN UNKNOWN}
         *
         * @return The file extension for this format
         */
        @Nonnull
        public String getExtension()
        {
            if (this == UNKNOWN)
                throw new IllegalStateException("Can only get extension of a known format");
            return extension;
        }

        /**
         * Resolves the specified format identifier to the StickerFormat enum constant.
         *
         * @param  id
         *         The id for this format
         *
         * @return The representative StickerFormat or UNKNOWN if it can't be resolved
         */
        @Nonnull
        public static MessageSticker.StickerFormat fromId(int id)
        {
            for (MessageSticker.StickerFormat stickerFormat : values())
            {
                if (stickerFormat.id == id)
                    return stickerFormat;
            }
            return UNKNOWN;
        }
    }
}
