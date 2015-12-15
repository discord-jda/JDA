/**
 * Created by Michael Ritter on 15.12.2015.
 */
package net.dv8tion.jda.handle;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.MessageCreateEvent;
import org.json.JSONObject;

public class MessageCreateHandler implements ISocketHandler
{
    private final JDA api;

    public MessageCreateHandler(JDA api)
    {
        this.api = api;
    }

    @Override
    public void handle(JSONObject content)
    {
        String id = content.getString("id");
        TextChannel channel = api.getChannelMap().get(id);
        if (channel != null)
        {
            api.getEventManager().handle(new MessageCreateEvent(new EntityBuilder(api).createMessage(content)));
        }
        else
        {
            //TODO PRIVATE MESSAGE
        }
    }
}
