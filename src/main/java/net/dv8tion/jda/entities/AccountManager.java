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
     */
    AccountManager setAvatar(AvatarUtil.Avatar avatar);

    /**
     * Set the email of the connected account.
     * This change will only be applied, when {@link #update()} is called
     * 
     * @param email
     *      the new email or null to discard changes
     */
    AccountManager setEmail(String email);

    /**
     * Set the password of the connected account.
     * This change will only be applied, when {@link #update()} is called
     * 
     * @param password
     *      the new password or null to discard changes
     */
    AccountManager setPassword(String password);

    /**
     * Set the username of the connected account.
     * This change will only be applied, when {@link #update()} is called
     * 
     * @param username
     *      the new username or null to discard changes
     */
    AccountManager setUsername(String username);
    

    /**
     * Updates the profile of the connected account, sends the changed data to the Discord server.
     */
    void update();
}
