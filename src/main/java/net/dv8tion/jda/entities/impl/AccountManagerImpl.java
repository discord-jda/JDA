package net.dv8tion.jda.entities.impl;

import org.json.JSONObject;

import net.dv8tion.jda.OnlineStatus;
import net.dv8tion.jda.entities.AccountManager;
import net.dv8tion.jda.utils.AvatarUtil;

public class AccountManagerImpl implements AccountManager
{

    private AvatarUtil.Avatar avatar = null;
    private String email = null;
    private String new_password = null;
    private String username = null;

    private String password;

    private final JDAImpl api;

    public AccountManagerImpl(JDAImpl api, String password)
    {
        this.api = api;
        this.password = password;
    }

    @Override
    public AccountManager setAvatar(AvatarUtil.Avatar avatar)
    {
        this.avatar = avatar;
        return this;
    }

    @Override
    public AccountManager setEmail(String email)
    {
        this.email = email;
        return this;
    }

    @Override
    public AccountManager setPassword(String password)
    {
        this.new_password = password;
        return this;
    }

    @Override
    public AccountManager setUsername(String username)
    {
        this.username = username;
        return this;
    }

    @Override
    public AccountManager setGame(String game)
    {
        ((SelfInfoImpl) api.getSelfInfo()).setCurrentGame(game);
        updateStatusAndGame(game, api.getSelfInfo().getOnlineStatus() == OnlineStatus.AWAY ? System.currentTimeMillis() : JSONObject.NULL);
        return this;
    }

    public void update()
    {
        try
        {
            JSONObject object = new JSONObject();
            object.put("avatar", avatar == null ? api.getSelfInfo().getAvatarId() : (avatar == AvatarUtil.DELETE_AVATAR ? JSONObject.NULL : avatar.getEncoded()));
            object.put("email", email == null ? api.getSelfInfo().getEmail() : email);
            if (new_password != null)
            {
                object.put("new_password", new_password);
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
            if (new_password != null)
            {
                this.password = new_password;
            }

            this.avatar = null;
            this.email = null;
            this.new_password = null;
            this.username = null;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public AccountManager setIdle(boolean idle)
    {
        ((SelfInfoImpl) api.getSelfInfo()).setOnlineStatus(idle ? OnlineStatus.AWAY : OnlineStatus.ONLINE);
        updateStatusAndGame(api.getSelfInfo().getCurrentGame(), idle ? System.currentTimeMillis() : JSONObject.NULL);
        return this;
    }

    private void updateStatusAndGame(String game, Object idle)
    {
        JSONObject object = new JSONObject().put("op", 3).put("d", new JSONObject().put("game", new JSONObject().put("name", game)).put("idle_since", idle));
        api.getClient().send(object.toString());
    }
}
