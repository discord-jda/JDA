package net.dv8tion.jda;


import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.handle.EntityBuilder;
import net.dv8tion.jda.requests.RequestBuilder;
import net.dv8tion.jda.requests.RequestType;
import org.json.JSONArray;

import java.util.LinkedList;
import java.util.List;


public class MessageHistory
{
    private final JDA api;
    private final TextChannel channel;
    private String lastId = null;
    private boolean atEnd = false;
    private List<Message> queued = new LinkedList<>();

    public MessageHistory(JDA api, TextChannel channel)
    {
        this.api = api;
        this.channel = channel;
    }

    /**
     * Gets all available Messages. Can be called multiple times and always returns the full set
     *
     * @return all available Messages
     */
    public List<Message> retrieveAll()
    {
        while (!atEnd && retrieve() != null)
        {
            //Nothing needed here
        }
        return queued;
    }

    /**
     * Returns all already by the retrieve methods pulled messages of this history
     *
     * @return the list of already pulled messages
     */
    public List<Message> getRecent()
    {
        return queued;
    }

    /**
     * Queues the next set of 50 Messages and returns them
     * If the end of the chat was already reached, this function returns null
     *
     * @return a list of the next 50 Messages (max), or null if at end of chat
     */
    public List<Message> retrieve()
    {
        return retrieve(50);
    }

    /**
     * Queues the next set of Messages and returns them
     * If the end of the chat was already reached, this function returns null
     *
     * @param amount the amount to Messages to queue (limited to 100)
     * @return a list of the next [amount] Messages (max), or null if at end of chat
     */
    public List<Message> retrieve(int amount)
    {
        if (atEnd)
        {
            return null;
        }
        amount = Math.min(amount, 100);
        RequestBuilder rb = new RequestBuilder(api);
        rb.setType(RequestType.GET);
        rb.setUrl("https://discordapp.com/api/channels/" + channel.getId() + "/messages?limit=" + amount + (lastId != null ? "&before=" + lastId : ""));
        LinkedList<Message> out = new LinkedList<>();
        try
        {
            JSONArray array = new JSONArray(rb.makeRequest());
            EntityBuilder builder = new EntityBuilder(api);
            for (int i = 0; i < array.length(); i++)
            {
                out.add(builder.createMessage(array.getJSONObject(i)));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        if (out.size() < amount)
        {
            atEnd = true;
        }
        if (out.size() > 0)
        {
            lastId = out.getLast().getId();
        }
        else
        {
            return null;
        }
        queued.addAll(out);
        return out;
    }
}