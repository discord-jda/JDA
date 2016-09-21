/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.dv8tion.jda.core;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.handle.EntityBuilder;
import org.json.JSONArray;

import java.util.LinkedList;
import java.util.List;

public class MessageHistory
{
    private final JDAImpl api;
    private final String channelId;
    private String lastId = null;
    private boolean atEnd = false;
    private final List<Message> queued = new LinkedList<>();

    public MessageHistory(MessageChannel channel)
    {
        this.api = (JDAImpl) channel.getJDA();
        if (channel instanceof TextChannel &&
                !((TextChannel) channel).getGuild().getMember(api.getSelfInfo()).hasPermission(Permission.MESSAGE_HISTORY))
            throw new PermissionException(Permission.MESSAGE_HISTORY);

        this.channelId = channel.getId();
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
     * Queues the next set of 100 Messages and returns them
     * If the end of the chat was already reached, this function returns null
     *
     * @return a list of the next 100 Messages (max), or null if at end of chat
     */
    public List<Message> retrieve()
    {
        return retrieve(100);
    }

    /**
     * Queues the next set of Messages and returns them
     * If the end of the chat was already reached, this function returns null
     *
     * @param amount the amount to Messages to queue
     * @return a list of the next [amount] Messages (max), or null if at end of chat
     */
    public List<Message> retrieve(int amount)
    {
        if (atEnd)
        {
            return null;
        }
        int toQueue;
        LinkedList<Message> out = new LinkedList<>();
        EntityBuilder builder = EntityBuilder.get(api);
        while(amount > 0)
        {
            toQueue = Math.min(amount, 100);
            try
            {
//                Requester2.Response response = api.getRequester2().get(Requester2.DISCORD_API_PREFIX + "channels/" + channelId
//                        + "/messages?limit=" + toQueue + (lastId != null ? "&before=" + lastId : ""));
//                if(!response.isOk())
//                    throw new RuntimeException("Error fetching message-history for channel with id " + channelId + "... Error: " + response.toString());
//
//                JSONArray array = response.getArray();

//                for (int i = 0; i < array.length(); i++)
//                {
//                    out.add(builder.createMessage(array.getJSONObject(i)));
//                }
//                if(array.length() < toQueue) {
//                    atEnd = true;
//                    break;
//                }
//                else
//                {
//                    lastId = out.getLast().getId();
//                }
            }
            catch (Exception ex)
            {
                JDAImpl.LOG.log(ex);
                break;
            }
            amount -= toQueue;
        }
        if(out.size() == 0)
        {
            return null;
        }
        queued.addAll(out);
        return out;
    }
}