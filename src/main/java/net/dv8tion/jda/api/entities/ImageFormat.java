package net.dv8tion.jda.api.entities;

/**
 * Represents an extension that the discord cdn accepts.
 * <br>The default used is {@link #PNG} unless specified by the method's documentation.
 */
public enum ImageFormat
{
    /**
     * Indicates that the image should have the .jpg extension
     */
    JPG,
    /**
     * Indicates that the image should have the .jpeg extension
     */
    JPEG,
    /**
     * Indicates that the image should have the .png extension
     */
    PNG,
    /**
     * Indicates that the image should have the .webp extension
     */
    WEBP,
    /**
     * Indicates that the image should have the .gif extension
     */
    GIF;

    @Override
    public String toString()
    {
        return name().toLowerCase();
    }
}
