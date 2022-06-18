package net.dv8tion.jda.api.entities;

public enum ActionType
{
    /**
     * Blocks the message if the message breaks the auto-moderation rules.
     */
    BLOCK_MESSAGE(1),
    /**
     * Sends an alert to a specific channel if a member breaks the auto-moderation rules.
     */
    SEND_ALERT_MESSAGE(2),
    /**
     * Times out the member who broke the auto-moderation rules.
     */
    TIMEOUT(3),
    /**
     * An unknown action type.
     */
    UNKNOWN(-1);

    private final int value;

    ActionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ActionType fromValue(int value) {
        for (ActionType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
