package net.dv8tion.jda.api.entities;

public enum KeywordPresetType {
    /**
     * Words that may be considered forms of swearing or cursing.
     */
    PROFANITY(1),
    /**
     * Words that refer to sexually explicit behavior or activity.
     */
    SEXUAL_CONTENT(2),
    /**
     * Personal insults or words that may be considered hate speech.
     */
    SLURS(3),
    /**
     * An unknown preset type.
     */
    UNKNOWN(-1);

    private final int value;

    KeywordPresetType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static KeywordPresetType fromValue(int value) {
        for (KeywordPresetType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
