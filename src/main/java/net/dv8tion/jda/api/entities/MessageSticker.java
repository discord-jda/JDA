package net.dv8tion.jda.api.entities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MessageSticker implements ISnowflake
{
    private final long id;
    private final long packId;
    private final String name;
    private final String description;
    private final String asset;
    private final String previewAsset;
    private final StickerFormat formatType;

    public MessageSticker(final long id, final long packId, final String name, final String description, final String asset, final String previewAsset, final StickerFormat formatType)
    {
        this.id = id;
        this.packId = packId;
        this.name = name;
        this.description = description;
        this.asset = asset;
        this.previewAsset = previewAsset;
        this.formatType = formatType;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    /**
     * The ID of the pack the sticker is from.
     *
     * @return the ID of the pack the sticker is from
     */
    public long getPackId()
    {
        return packId;
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
     * The description of the sticker.
     *
     * @return the description of the sticker
     */
    @Nonnull
    public String getDescription()
    {
        return description;
    }

    /**
     * The asset hash of the sticker.
     *
     * @return the asset hash of the sticker
     */
    @Nonnull
    public String getAsset()
    {
        return asset;
    }

    /**
     * The preview asset hash of the sticker.
     *
     * @return the preview asset hash of the sticker or {@code null} if the sticker has no preview asset
     */
    @Nullable
    public String getPreviewAsset()
    {
        return previewAsset;
    }

    /**
     * The url of the sticker image.
     *
     * @return the url of the sticker
     */
    @Nonnull
    public String getStickerUrl()
    {
        return String.format("https://cdn.discordapp.com/stickers/%s/%s.png", id, asset);
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

    public enum StickerFormat
    {
        /**
         * The PNG format.
         */
        PNG(1),
        /**
         * The APNG format.
         */
        APNG(2),
        /**
         * The LOTTIE format.
         */
        LOTTIE(3),
        /**
         * Represents any unknown or unsupported {@link net.dv8tion.jda.api.entities.MessageSticker MessageSticker} format types.
         */
        UNKNOWN(-1);

        private final int id;

        StickerFormat(final int id)
        {
            this.id = id;
        }

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
