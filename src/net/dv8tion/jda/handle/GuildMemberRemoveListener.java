/**
 * Created by Michael Ritter on 16.12.2015.
 */
package net.dv8tion.jda.handle;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.impl.GuildImpl;
import net.dv8tion.jda.entities.impl.UserImpl;
import org.json.JSONObject;

public class GuildMemberRemoveListener implements ISocketHandler
{
    private final JDA api;

    public GuildMemberRemoveListener(JDA api)
    {
        this.api = api;
    }

    @Override
    public void handle(JSONObject content)
    {
        GuildImpl guild = (GuildImpl) api.getGuildMap().get(content.getString("guild_id"));
        UserImpl user = ((UserImpl) api.getUserMap().get(content.getJSONObject("user").getString("id")));
        guild.getUserRoles().remove(user);
        if (user.hasPrivateChannel())
        {
            boolean exists = api.getGuildMap().values().stream().anyMatch(g -> (((GuildImpl) g).getUserRoles().containsKey(user)));
            if (!exists)
            {
                api.getOffline_pms().put(user.getId(), user.getPrivateChannel().getId());
                api.getUserMap().remove(user.getId());
            }
        }
    }
}
