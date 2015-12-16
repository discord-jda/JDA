/**
 * Created by Michael Ritter on 16.12.2015.
 */
package net.dv8tion.jda.handle;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.impl.GuildImpl;
import net.dv8tion.jda.entities.impl.PrivateChannelImpl;
import net.dv8tion.jda.entities.impl.UserImpl;
import org.json.JSONObject;

import java.util.LinkedList;

public class GuildMemberAddHandler implements ISocketHandler
{
    private final JDA api;

    public GuildMemberAddHandler(JDA api)
    {
        this.api = api;
    }

    @Override
    public void handle(JSONObject content)
    {
        GuildImpl guild = (GuildImpl) api.getGuildMap().get(content.getString("guild_id"));
        User user = new EntityBuilder(api).createUser(content.getJSONObject("user"));
        if (api.getOffline_pms().containsKey(user.getId()))
        {
            PrivateChannel pc = new PrivateChannelImpl(api.getOffline_pms().get(user.getId()), user);
            ((UserImpl) user).setPrivateChannel(pc);
            api.getOffline_pms().remove(user.getId());
        }
        guild.getUserRoles().put(user, new LinkedList<>());
    }
}
