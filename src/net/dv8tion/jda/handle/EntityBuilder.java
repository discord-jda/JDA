package net.dv8tion.jda.handle;

import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.SelfInfo;
import org.json.JSONObject;

/**
 * Created by Michael Ritter on 13.12.2015.
 */
public class EntityBuilder
{
    protected static Guild createGuild(JSONObject guild)
    {
        return null;
    }

    protected static PrivateChannel createPrivateChannel(JSONObject privatechat)
    {
        return null;
    }

    protected static SelfInfo createSelfInfo(JSONObject self)
    {
        return null;
    }
}
