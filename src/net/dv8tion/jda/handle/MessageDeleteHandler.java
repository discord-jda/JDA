/**
 * Created by Michael Ritter on 17.12.2015.
 */
package net.dv8tion.jda.handle;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.message.MessageDeleteEvent;
import org.json.JSONObject;

public class MessageDeleteHandler implements ISocketHandler
{
    private final JDA api;

    public MessageDeleteHandler(JDA api)
    {
        this.api = api;
    }

    @Override
    public void handle(JSONObject content)
    {
        TextChannel channel = api.getChannelMap().get(content.getString("channel_id"));
        if (channel != null)
        {
            api.getEventManager().handle(new MessageDeleteEvent(api, content.getString("id"), channel));
        }
        else
        {
            //TODO: handle private channel
        }
    }
}
