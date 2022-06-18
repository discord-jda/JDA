package net.dv8tion.jda.api.entities;

public enum EventType {
    /**
     * This event is triggered when a message is sent.
     */
    MESSAGE_SEND(1),
    /**
     * An unknown event type.
     */
    UNKNOWN(-1);


    private final int value;

    EventType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static EventType fromValue(int value) {
        for (EventType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
