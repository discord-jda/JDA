/**
 * 
 */
package net.dv8tion.jda.entities;

import net.dv8tion.jda.utils.AvatarUtil;

public interface AccountManager
{
    /**
     * Set the avatar of the connected account.
     * This change will only be applied, when {@link #update()} is called
     * Avatars can get generated through the methods of {@link net.dv8tion.jda.utils.AvatarUtil AvatarUtil}
     * 
     * @param avatar
     *      a Avatar object, null to keep current Avatar or {@link net.dv8tion.jda.utils.AvatarUtil#DELETE_AVATAR AvatarUtil#DELETE_AVATAR} to remove the avatar
     * @return
     * 	  this
     */
    AccountManager setAvatar(AvatarUtil.Avatar avatar);

    /**
     * Set the email of the connected account.
     * This change will only be applied, when {@link #update()} is called
     * 
     * @param email
     *      the new email or null to discard changes
     * @return
     * 	  this
     */
    AccountManager setEmail(String email);

    /**
     * Set the password of the connected account.
     * This change will only be applied, when {@link #update()} is called
     * 
     * @param password
     *      the new password or null to discard changes
     * @return
     * 	  this
     */
    AccountManager setPassword(String password);

    /**
     * Set the username of the connected account.
     * This change will only be applied, when {@link #update()} is called
     * 
     * @param username
     *      the new username or null to discard changes
     * @return
     * 	  this
     */
    AccountManager setUsername(String username);

    /**
     * Updates the profile of the connected account, sends the changed data to the Discord server.
     */
    void update();

    /**
     * Set currently played game of the connected account.
     * This change will be applied <b>immediately</b>
     * 
     * @param game
     *      the name of the game that should be displayed
     * @return
     * 	  this
     */
    AccountManager setGame(String game);

    /**
     * Set status of the connected account.
     * This change will be applied <b>immediately</b>
     * 
     * @param idle
     *      weather the account should be displayed as idle o not
     * @return
     * 	  this
     */
    AccountManager setIdle(boolean idle);
}
