package net.dv8tion.jda.managers;

import net.dv8tion.jda.OnlineStatus;
import net.dv8tion.jda.entities.SelfInfo;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.entities.impl.SelfInfoImpl;
import net.dv8tion.jda.utils.AvatarUtil;
import org.json.JSONObject;

public class AccountManager
{

    private AvatarUtil.Avatar avatar = null;
    private String email = null;
    private String newPassword = null;
    private String username = null;

    private String password;

    private final JDAImpl api;

    public AccountManager(JDAImpl api, String password)
    {
        this.api = api;
        this.password = password;
    }

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
    public AccountManager setAvatar(AvatarUtil.Avatar avatar)
    {
        this.avatar = avatar;
        return this;
    }

    /**
     * Set the email of the connected account.
     * This change will only be applied, when {@link #update()} is called
     *
     * @param email
     *      the new email or null to discard changes
     * @return
     * 	  this
     */
    public AccountManager setEmail(String email)
    {
        this.email = email;
        return this;
    }

    /**
     * Set the password of the connected account.
     * This change will only be applied, when {@link #update()} is called
     *
     * @param password
     *      the new password or null to discard changes
     * @return
     * 	  this
     */
    public AccountManager setPassword(String password)
    {
        this.newPassword = password;
        return this;
    }

    /**
     * Set the username of the connected account.
     * This change will only be applied, when {@link #update()} is called
     *
     * @param username
     *      the new username or null to discard changes
     * @return
     * 	  this
     */
    public AccountManager setUsername(String username)
    {
        this.username = username;
        return this;
    }

    /**
     * Set currently played game of the connected account.
     * This change will be applied <b>immediately</b>
     *
     * @param game
     *      the name of the game that should be displayed
     * @return
     * 	  this
     */
    public AccountManager setGame(String game)
    {
        ((SelfInfoImpl) api.getSelfInfo()).setCurrentGame(game);
        updateStatusAndGame();
        return this;
    }

    /**
     * Set status of the connected account.
     * This change will be applied <b>immediately</b>
     *
     * @param idle
     *      weather the account should be displayed as idle o not
     * @return
     * 	  this
     */
    public AccountManager setIdle(boolean idle)
    {
        ((SelfInfoImpl) api.getSelfInfo()).setOnlineStatus(idle ? OnlineStatus.AWAY : OnlineStatus.ONLINE);
        updateStatusAndGame();
        return this;
    }

    /**
     * Updates the profile of the connected account, sends the changed data to the Discord server.
     *
     * @return
     *      this
     */
    public AccountManager update()
    {
        try
        {
            JSONObject object = new JSONObject();
            object.put("avatar", avatar == null ? api.getSelfInfo().getAvatarId() : (avatar == AvatarUtil.DELETE_AVATAR ? JSONObject.NULL : avatar.getEncoded()));
            object.put("email", email == null ? api.getSelfInfo().getEmail() : email);
            if (newPassword != null)
            {
                object.put("new_password", newPassword);
            }
            object.put("password", password);
            object.put("username", username == null ? api.getSelfInfo().getUsername() : username);

            JSONObject result = api.getRequester().patch("https://discordapp.com/api/users/@me", object);

            if (result == null)
            {
                throw new Exception("Something went wrong while changing the account settings.");
            }

            SelfInfoImpl self = (SelfInfoImpl) api.getSelfInfo();

            self.setAvatarId(result.isNull("avatar") ? null : result.getString("avatar"));
            self.setDiscriminator(result.getString("discriminator"));
            self.setEmail(result.getString("email"));
            // self.setID(result.getString("id")); ID should never change unless
            // something really really bad happens
            api.setAuthToken(result.getString("token"));
            self.setUserName(result.getString("username"));
            self.setVerified(result.getBoolean("verified"));
            if (newPassword != null)
            {
                this.password = newPassword;
            }

            this.avatar = null;
            this.email = null;
            this.newPassword = null;
            this.username = null;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return this;
    }

    private void updateStatusAndGame()
    {
        SelfInfo selfInfo = api.getSelfInfo();
        JSONObject content = new JSONObject()
                .put("game", selfInfo.getCurrentGame() == null ? JSONObject.NULL : new JSONObject().put("name", selfInfo.getCurrentGame()))
                .put("idle_since", selfInfo.getOnlineStatus() == OnlineStatus.AWAY ? System.currentTimeMillis() : JSONObject.NULL);
        api.getClient().send(new JSONObject().put("op", 3).put("d", content).toString());
    }
}
