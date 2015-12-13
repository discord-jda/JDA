package net.dv8tion.jda.handle;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.SelfInfo;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.impl.UserImpl;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Michael Ritter on 13.12.2015.
 */
public class EntityBuilder
{
    private final JDA api;

    public EntityBuilder(JDA api)
    {
        this.api = api;
    }

    protected Guild createGuild(JSONObject guild)
    {
        //TODO: Acutally generate guild
        JSONArray members = guild.getJSONArray("members");
        for (int i = 0; i < members.length(); i++)
        {
            JSONObject member = members.getJSONObject(i);
            createUser(member.getJSONObject("user"));
        }
        JSONArray presences = guild.getJSONArray("presences");
        for (int i = 0; i < presences.length(); i++)
        {
            JSONObject presence = presences.getJSONObject(i);
            UserImpl user = ((UserImpl) api.getUserMap().get(presence.getJSONObject("user").getString("id")));
            user.setCurrentGameId(-1);
        }
        return null;
    }

    protected PrivateChannel createPrivateChannel(JSONObject privatechat)
    {
        return null;
    }

    protected User createUser(JSONObject user)
    {
        String id = user.getString("id");
        UserImpl userObj = ((UserImpl) api.getUserMap().get(id));
        if (userObj == null)
        {
            userObj = new UserImpl();
            api.getUserMap().put(id, userObj);
        }
        userObj.setId(id);
        userObj.setUserName(user.getString("username"));
        userObj.setDiscriminator(user.getString("discriminator"));
        userObj.setAvatarId(user.getString("avatar"));
        return userObj;
    }

    protected SelfInfo createSelfInfo(JSONObject self)
    {
        SelfInfo selfInfo = api.getSelfInfo();
        if (selfInfo == null)
        {
            //selfInfo = new selfInfo();
            api.setSelfInfo(selfInfo);
        }
//        selfInfo.setVerified(self.getBoolean("verified"));
//        selfInfo.setUsername(self.getString("username"));
//        selfInfo.setId(self.getString("id"));
//        selfInfo.setEmail(self.getString("email"));
//        selfInfo.setDiscriminator(self.getString("discriminator"));
//        selfInfo.setAvatarId(self.getString("avatar"));
        return selfInfo;
    }
}
