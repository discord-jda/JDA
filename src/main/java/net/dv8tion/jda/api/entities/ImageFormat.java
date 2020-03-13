package net.dv8tion.jda.api.entities;

public enum ImageFormat
{
    JPG,
    JPEG,
    PNG,
    WEBP,
    GIF;

    @Override
    public String toString()
    {
        return name().toLowerCase();
    }

}
