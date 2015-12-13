package net.dv8tion.jda.handle;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.impl.GuildImpl;
import net.dv8tion.jda.entities.impl.SelfInfoImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Michael Ritter on 13.12.2015.
 */
public class ReadyHandler implements ISocketHandler
{
    private final JDA api;
    private final EntityBuilder builder;

    public ReadyHandler(JDA api)
    {
        this.api = api;
        this.builder = new EntityBuilder(api);
    }

    @Override
    public void handle(JSONObject content)
    {
        //TODO: User-Setings; read_state; guild voice states; channel perm overrides; voice channels
        builder.createSelfInfo(content.getJSONObject("user"));
        JSONArray muted = content.getJSONObject("user_settings").getJSONArray("muted_channels");
        List<String> mutedChannelIds = new ArrayList<>();
        List<TextChannel> mutedChannels = new ArrayList<>();
        for (int i = 0; i < muted.length(); i++)
        {
            mutedChannelIds.add(muted.getString(i));
        }
        JSONArray guilds = content.getJSONArray("guilds");
        for (int i = 0; i < guilds.length(); i++)
        {
            Guild guild = builder.createGuild(guilds.getJSONObject(i));
            Iterator<String> iterator = mutedChannelIds.iterator();
            while (iterator.hasNext())
            {
                String id = iterator.next();
                TextChannel chan = ((GuildImpl) guild).getTextChannelsMap().get(id);
                if (chan != null)
                {
                    mutedChannels.add(chan);
                    iterator.remove();
                }
            }
        }
        ((SelfInfoImpl) api.getSelfInfo()).setMutedChannels(mutedChannels);
        JSONArray priv_chats = content.getJSONArray("private_channels");
        for (int i = 0; i < priv_chats.length(); i++)
        {
            builder.createPrivateChannel(priv_chats.getJSONObject(i));
        }
    }
}
