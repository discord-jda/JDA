/**
 * 
 */
package net.dv8tion.jda.entities;

public interface AccountManager
{
    /**
     * Set the avatar of the connected account.
     * This change will only be applied, when {@link #update()} is called
     * 
     * @param avatar
     *      a base64 encoded image, "null" to remove or null to discard changes
     * @return
     * 	  this
     */
    public AccountManager setAvatar(String avatar);

    /**
     * Set the email of the connected account.
     * This change will only be applied, when {@link #update()} is called
     * 
     * @param email
     *      the new email or null to discard changes
     * @return
     * 	  this
     */
    public AccountManager setEmail(String email);

    /**
     * Set the password of the connected account.
     * This change will only be applied, when {@link #update()} is called
     * 
     * @param password
     *      the new password or null to discard changes
     * @return
     * 	  this
     */
    public AccountManager setPassword(String password);

    /**
     * Set the username of the connected account.
     * This change will only be applied, when {@link #update()} is called
     * 
     * @param username
     *      the new username or null to discard changes
     * @return
     * 	  this
     */
    public AccountManager setUsername(String username);

    /**
     * Updates the profile of the connected account, sends the changed data to the Discord server.
     */
    public void update();

    /**
     * Set currently played game of the connected account.
     * This change will be applied <b>immediately</b>
     * 
     * @param game
     *      the name of the game that should be displayed
     * @return
     * 	  this
     */
    public AccountManager setGame(String game);

    /**
     * Set status of the connected account.
     * This change will be applied <b>immediately</b>
     * 
     * @param idle
     *      weather the account should be displayed as idle o not
     * @return
     * 	  this
     */
    public AccountManager setIdle(boolean idle);
}
