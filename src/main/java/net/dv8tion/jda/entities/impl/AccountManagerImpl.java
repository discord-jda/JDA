package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.entities.AccountManager;
import org.json.JSONObject;

public class AccountManagerImpl implements AccountManager
{

    private String avatar = null;
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

    public void update()
    {
        try
        {
            JSONObject object = new JSONObject();
            object.put("avatar", avatar == null ? api.getSelfInfo().getAvatarId() : (avatar.equals("null") ? JSONObject.NULL : avatar));
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
            // self.setID(result.getString("id")); ID should never change unless something really really bad happens
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
    public AccountManager setAvatar(String avatar)
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
}
