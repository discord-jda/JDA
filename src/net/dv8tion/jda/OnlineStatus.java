package net.dv8tion.jda;

public enum OnlineStatus
{
    ONLINE("online"),
    AWAY("away"),
    OFFLINE("offline"),
    UNKNOWN("");

    private String key;

    OnlineStatus(String key)
    {
        this.key = key;
    }

    public static OnlineStatus fromKey(String key)
    {
        for (OnlineStatus onlineStatus : values())
        {
            if (onlineStatus.key.equalsIgnoreCase(key))
            {
                return onlineStatus;
            }
        }
        return UNKNOWN;
    }
}
