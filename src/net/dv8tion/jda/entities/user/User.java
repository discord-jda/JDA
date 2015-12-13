package net.dv8tion.jda.entities.user;

public interface User
{
    /**
     * The Discord ID of the User. This is typically 18 characters long.
     * @return
     */
    String getId();

    /**
     * The username of the user. Length is between 2 and 32 (inclusive).
     * @return
     */
    String getUsername();

    /**
     * The descriminator of the User. Used to differentiate between users with the same usernames.
     * This will be important when the friends list is released for human readable searching.
     * Ex: DV8FromTheWorld#9148
     * @return
     */
    String getDiscriminator();

    /**
     * The Discord Id for this user's avatar image.
     * If the user has not set an image, this will return null.
     * @return
     */
    String getAvatarId();

    /**
     * The URL for the for the user's avatar image.
     * If the user has not set an image, this will return null.
     * @return
     */
    String getAvatarUrl();

    /**
     * The Discord Id for the game that the user is currently playing.
     * If the user is not currently playing a game, this will return null.
     * @return
     */
    String getCurrentGameId();

    /**
     * The name of the game that the user is currently playing.
     * If the user is not currently playing a game, this will return null.
     * @return
     */
    String getCurrentGameName();
}
