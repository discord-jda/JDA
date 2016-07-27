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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.entities.impl;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.MultipartBody;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.MessageHistory;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.exceptions.BlockedException;
import net.dv8tion.jda.exceptions.PermissionException;
import net.dv8tion.jda.exceptions.RateLimitedException;
import net.dv8tion.jda.handle.EntityBuilder;
import net.dv8tion.jda.requests.Requester;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class PrivateChannelImpl implements PrivateChannel
{
    public static final String RATE_LIMIT_IDENTIFIER = "GLOBAL_PRIV_CHANNEL_RATELIMIT";
    private final String id;
    private final User user;
    private final JDAImpl api;

    public PrivateChannelImpl(String id, User user, JDAImpl api)
    {
        this.id = id;
        this.user = user;
        this.api = api;
    }

    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public User getUser()
    {
        return user;
    }

    @Override
    public Message sendMessage(String text)
    {
        return sendMessage(new MessageBuilder().appendString(text).build());
    }

    @Override
    public Message sendMessage(Message msg)
    {
        if (api.getMessageLimit(RATE_LIMIT_IDENTIFIER) != null)
        {
            throw new RateLimitedException(api.getMessageLimit(RATE_LIMIT_IDENTIFIER) - System.currentTimeMillis());
        }
        try
        {
            Requester.Response response = api.getRequester().post(Requester.DISCORD_API_PREFIX + "channels/" + getId() + "/messages",
                    new JSONObject().put("content", msg.getRawContent()));
            if (response.isRateLimit())
            {
                long retry_after = response.getObject().getLong("retry_after");
                api.setMessageTimeout(RATE_LIMIT_IDENTIFIER, retry_after);
                throw new RateLimitedException(retry_after);
            }
            if (!response.isOk())
            {
                throw new BlockedException();
            }
            return new EntityBuilder(api).createMessage(response.getObject());
        }
        catch (JSONException ex)
        {
            JDAImpl.LOG.log(ex);
            //sending failed
            return null;
        }
    }

    @Override
    public void sendMessageAsync(String text, Consumer<Message> callback)
    {
        sendMessageAsync(new MessageBuilder().appendString(text).build(), callback);
    }

    @Override
    public void sendMessageAsync(Message msg, Consumer<Message> callback)
    {
        ((MessageImpl) msg).setChannelId(id);
        TextChannelImpl.AsyncMessageSender.getInstance(getJDA(), RATE_LIMIT_IDENTIFIER).enqueue(msg, false, callback);
    }

    @Override
    public Message sendFile(File file, Message message)
    {
        if(file == null || !file.exists() || !file.canRead())
            throw new IllegalArgumentException("Provided file is either null, doesn't exist or is not readable!");
        if (file.length() > 8<<20)   //8MB
            throw new IllegalArgumentException("File is to big! Max file-size is 8MB");

        JDAImpl api = (JDAImpl) getJDA();
        try
        {
            MultipartBody body = Unirest.post(Requester.DISCORD_API_PREFIX + "channels/" + getId() + "/messages")
                    .header("authorization", getJDA().getAuthToken())
                    .header("user-agent", Requester.USER_AGENT)
                    .field("file", file);
            if (message != null)
                body.field("content", message.getRawContent()).field("tts", message.isTTS());

            String dbg = String.format("Requesting %s -> %s\n\tPayload: file: %s, message: %s, tts: %s\n\tResponse: ",
                    body.getHttpRequest().getHttpMethod().name(), body.getHttpRequest().getUrl(),
                    file.getAbsolutePath(), message == null ? "null" : message.getRawContent(), message == null ? "N/A" : message.isTTS());
            String requestBody = body.asString().getBody();
            Requester.LOG.trace(dbg + body);

            try
            {
                JSONObject messageJson = new JSONObject(requestBody);
                return new EntityBuilder(api).createMessage(messageJson);
            }
            catch (JSONException e)
            {
                Requester.LOG.fatal("Following json caused an exception: " + requestBody);
                Requester.LOG.log(e);
            }
        }
        catch (UnirestException e)
        {
            Requester.LOG.log(e);
        }
        return null;
    }

    @Override
    public void sendFileAsync(File file, Message message, Consumer<Message> callback)
    {
        Thread thread = new Thread(() ->
        {
            Message messageReturn = sendFile(file, message);
            if (callback != null)
                callback.accept(messageReturn);
        });
        thread.setName("PrivateChannelImpl SendFileAsync Channel: " + id);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public Message getMessageById(String messageId)
    {
        Requester.Response response = ((JDAImpl) getJDA()).getRequester().get(Requester.DISCORD_API_PREFIX + "channels/" + id + "/messages/" + messageId);

        if (response.isOk())
            return new EntityBuilder((JDAImpl) getJDA()).createMessage(response.getObject());

        //Doesn't exist.
        return null;
    }

    @Override
    public boolean deleteMessageById(String messageId)
    {
        Requester.Response response = ((JDAImpl) getJDA()).getRequester().delete(Requester.DISCORD_API_PREFIX + "channels/" + id + "/messages/" + messageId);

        if (response.isOk())
            return true;
        else if (response.code == 403)
            throw new PermissionException("Cannot delete another User's messages in a PrivateChannel.");

        //Doesn't exist. Either never existed, bad id, was deleted already, or not in this channel.
        return false;
    }

    @Override
    public MessageHistory getHistory()
    {
        return new MessageHistory(this);
    }

    @Override
    public void sendTyping()
    {
        api.getRequester().post(Requester.DISCORD_API_PREFIX + "channels/" + getId() + "/typing", new JSONObject());
    }

    @Override
    public boolean pinMessageById(String messageId)
    {
        Requester.Response response = ((JDAImpl) getJDA()).getRequester().put(
                Requester.DISCORD_API_PREFIX + "/channels/" + id + "/pins/" + messageId, new JSONObject());
        if (response.isRateLimit())
            throw new RateLimitedException(response.getObject().getInt("retry_after"));
        return response.isOk();
    }

    @Override
    public boolean unpinMessageById(String messageId)
    {
        Requester.Response response = ((JDAImpl) getJDA()).getRequester().delete(
                Requester.DISCORD_API_PREFIX + "/channels/" + id + "/pins/" + messageId);
        if (response.isRateLimit())
            throw new RateLimitedException(response.getObject().getInt("retry_after"));
        return response.isOk();
    }

    @Override
    public List<Message> getPinnedMessages()
    {
        List<Message> pinnedMessages = new LinkedList<>();
        Requester.Response response = ((JDAImpl) getJDA()).getRequester().get(
                Requester.DISCORD_API_PREFIX + "/channels/" + id + "/pins");
        if (response.isOk())
        {
            JSONArray pins = response.getArray();
            for (int i = 0; i < pins.length(); i++)
            {
                pinnedMessages.add(new EntityBuilder((JDAImpl) getJDA()).createMessage(pins.getJSONObject(i)));
            }
            return Collections.unmodifiableList(pinnedMessages);
        }
        else if (response.isRateLimit())
            throw new RateLimitedException(response.getObject().getInt("retry_after"));
        else
            throw new RuntimeException("An unknown error occured attempting to get pinned messages. Ask devs for help.\n" + response);
    }

    @Override
    public void close()
    {
        api.getRequester().delete(Requester.DISCORD_API_PREFIX + "channels/" + getId());
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof PrivateChannel))
            return false;
        PrivateChannel oPChannel = (PrivateChannel) o;
        return this == oPChannel || this.getId().equals(oPChannel.getId());
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }

    @Override
    public String toString()
    {
        return "PC:" + getUser().getUsername() + '(' + getId() + ')';
    }
}
