package net.dv8tion.jda.api.entities;


public enum TriggerType
{
    /**
     * This checks if the content sent by a member contains a specific word.
     * <br>
     * The max per guild is 3.
     */
    KEYWORD(1, 3),
    /**
     * As the name suggests it checks for any links which are found to be harmful.
     *
     * <br>
     * The max per guild is 1.
     */
    HARMFUL_LINK(2, 1),
    /**
     * This checks for generic spam.
     *
     * <br>
     * The max per guild is 1.
     */
    SPAM(3, 1),
    /**
     * This check if the content contains words from internal pre-defined word sets
     *
     * <br>
     * The max per guild is 1.
     */
    KEYWORD_PRESET(4, 1),
    /**
     * An unknown trigger type.
     */
    UNKNOWN(-1, 0);

    private final int value;
    private final int maxPerGuild;

    TriggerType(int value, int maxPerGuild) {
        this.value = value;
        this.maxPerGuild = maxPerGuild;
    }

    public int getValue() {
        return value;
    }

    public int getMaxPerGuild() {
        return maxPerGuild;
    }

    public static TriggerType fromValue(int value) {
        for (TriggerType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
