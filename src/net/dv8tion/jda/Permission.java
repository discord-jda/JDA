package net.dv8tion.jda;

/**
 * Created by Michael Ritter on 13.12.2015.
 */
public enum Permission
{
    CREATE_INSTANT_INVITE(0),
    KICK_MEMBERS(1),
    BAN_MEMBERS(2),
    MANAGE_ROLES(3),
    MANAGE_PERMISSIONS(3),
    MANAGE_CHANNEL(4),
    MANAGE_SERVER(5),

    MESSAGE_READ(10),
    MESSAGE_WRITE(11),
    MESSAGE_TTS(12),
    MESSAGE_MANAGE(13),
    MESSAGE_EMBED_LINKS(14),
    MESSAGE_ATTACH_FILES(15),
    MESSAGE_HISTORY(16),
    MESSAGE_MENTION_EVERYONE(17),

    VOICE_CONNECT(20),
    VOICE_SPEAK(21),
    VOICE_MUTE_OTHERS(22),
    VOICE_DEAF_OTHERS(23),
    VOICE_MOVE_OTHERS(24),
    VOICE_USE_VAD(25);

    private int offset;

    Permission(int offset)
    {
        this.offset = offset;
    }

    public int getOffset()
    {
        return offset;
    }
}
