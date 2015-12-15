/**
 * Created by Michael Ritter on 15.12.2015.
 */
package net.dv8tion.jda.handle;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.MessageReceivedEvent;
import org.json.JSONObject;

public class MessageRecievedHandler implements ISocketHandler
{
    private final JDA api;

    public MessageRecievedHandler(JDA api)
    {
        this.api = api;
    }

    @Override
    public void handle(JSONObject content)
    {
        String channel_id = content.getString("channel_id");
        TextChannel channel = api.getChannelMap().get(channel_id);
        if (channel != null)
        {
            api.getEventManager().handle(new MessageReceivedEvent(new EntityBuilder(api).createMessage(content)));
        }
        else
        {
            //TODO PRIVATE MESSAGE
        }
    }
}
