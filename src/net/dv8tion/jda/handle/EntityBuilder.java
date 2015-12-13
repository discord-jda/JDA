package net.dv8tion.jda.handle;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.OnlineStatus;
import net.dv8tion.jda.Region;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.entities.impl.GuildImpl;
import net.dv8tion.jda.entities.impl.SelfInfoImpl;
import net.dv8tion.jda.entities.impl.UserImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

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
        String id = guild.getString("id");
        GuildImpl guildObj = ((GuildImpl) api.getGuildMap().get(id));
        if (guildObj == null)
        {
            guildObj = new GuildImpl();
            api.getGuildMap().put(id, guildObj);
        }
        guildObj.setId(id);
        guildObj.setIconId(guild.isNull("icon") ? null : guild.getString("icon"));
        guildObj.setRegion(Region.getRegion(guild.getString("region")));
        guildObj.setName(guild.getString("name"));
        guildObj.setOwnerId(guild.getString("owner_id"));
        guildObj.setAfkTimeout(guild.getInt("afk_timeout"));
        guildObj.setAfkChannelId(guild.isNull("afk_channel_id") ? null : guild.getString("afk_channel_id"));

        //TODO: parse channels

        JSONArray roles = guild.getJSONArray("roles");
        for (int i = 0; i < roles.length(); i++)
        {
            guildObj.getRolesModifiable().add(createRole(roles.getJSONObject(i)));
        }

        final GuildImpl finGuild = guildObj;
        JSONArray members = guild.getJSONArray("members");
        for (int i = 0; i < members.length(); i++)
        {
            JSONObject member = members.getJSONObject(i);
            User user = createUser(member.getJSONObject("user"));
            guildObj.getRolesMap().put(user, new ArrayList<>());
            JSONArray roleArr = member.getJSONArray("roles");
            for (int j = 0; j < roleArr.length(); j++)
            {
                String roleId = roleArr.getString(j);
                guildObj.getRolesModifiable().stream().filter(role -> role.getId().equals(roleId)).forEach(role -> finGuild.getRolesMap().get(user).add(role));
            }
        }
        JSONArray presences = guild.getJSONArray("presences");
        for (int i = 0; i < presences.length(); i++)
        {
            JSONObject presence = presences.getJSONObject(i);
            UserImpl user = ((UserImpl) api.getUserMap().get(presence.getJSONObject("user").getString("id")));
            user.setCurrentGameId(presence.isNull("game_id") ? -1 : presence.getInt("game_id"));
            user.setOnlineStatus(OnlineStatus.fromKey(presence.getString("status")));
        }
        return guildObj;
    }

    protected PrivateChannel createPrivateChannel(JSONObject privatechat)
    {
        return null;
    }

    protected Role createRole(JSONObject role)
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
        userObj.setAvatarId(user.isNull("avatar") ? null : user.getString("avatar"));
        return userObj;
    }

    protected SelfInfo createSelfInfo(JSONObject self)
    {
        SelfInfoImpl selfInfo = ((SelfInfoImpl) api.getSelfInfo());
        if (selfInfo == null)
        {
            selfInfo = new SelfInfoImpl();
            api.setSelfInfo(selfInfo);
        }
        selfInfo.setVerified(self.getBoolean("verified"));
        selfInfo.setUserName(self.getString("username"));
        selfInfo.setId(self.getString("id"));
        selfInfo.setEmail(self.getString("email"));
        selfInfo.setDiscriminator(self.getString("discriminator"));
        selfInfo.setAvatarId(self.getString("avatar"));
        return selfInfo;
    }
}
