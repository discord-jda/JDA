package net.dv8tion.jda.api.interactions.components.text;

import org.jetbrains.annotations.NotNull;

public enum TextInputStyle
{
    UNKNOWN(-1),
    SHORT(1),
    PARAGRAPH(2);

    private final int key;

    TextInputStyle(int type)
    {
        this.key = type;
    }

    public int getKey()
    {
        return key;
    }

    /**
     * Returns the style associated with the provided key
     *
     * @param  key
     *         The key to convert
     *
     * @return The text input style or {@link #UNKNOWN}
     */
    @NotNull
    public static TextInputStyle fromKey(int key)
    {
        for (TextInputStyle style : values())
        {
            if (style.key == key)
                return style;
        }
        return UNKNOWN;
    }
}
