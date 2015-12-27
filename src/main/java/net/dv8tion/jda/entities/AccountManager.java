/**
 * 
 */
package net.dv8tion.jda.entities;

public interface AccountManager
{
    /**
     * Set the avatar of the connected account.
     * 
     * @param avatar
     *      a base64 encoded image, "null" to remove or null to discard changes
     */
    public AccountManager setAvatar(String avatar);

    /**
     * Set the email of the connected account.
     * 
     * @param email
     *      the new email or null to discard changes
     */
    public AccountManager setEmail(String email);

    /**
     * Set the password of the connected account.
     * 
     * @param password
     *      the new password or null to discard changes
     */
    public AccountManager setPassword(String password);

    /**
     * Set the username of the connected account.
     * 
     * @param username
     *      the new username or null to discard changes
     */
    public AccountManager setUsername(String username);
    

    /**
     * Updates the profile of the connected account, sends the changed data to the Discord server.
     */
    public void update();
}
