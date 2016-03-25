/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.exceptions.BlockedException;
import net.dv8tion.jda.exceptions.RateLimitedException;
import net.dv8tion.jda.handle.EntityBuilder;
import net.dv8tion.jda.requests.Requester;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.function.Consumer;

public class PrivateChannelImpl implements PrivateChannel
{
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
        if (api.getMessageLimit() != null)
        {
            throw new RateLimitedException(api.getMessageLimit() - System.currentTimeMillis());
        }
        try
        {
            JSONObject response = api.getRequester().post(Requester.DISCORD_API_PREFIX + "channels/" + getId() + "/messages",
                    new JSONObject().put("content", msg.getRawContent()));
            if (response.has("retry_after"))
            {
                long retry_after = response.getLong("retry_after");
                api.setMessageTimeout(retry_after);
                throw new RateLimitedException(retry_after);
            }
            if (!response.has("id"))
            {
                throw new BlockedException();
            }
            return new EntityBuilder(api).createMessage(response);
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
        ((MessageImpl) msg).setChannelId(getId());
        TextChannelImpl.AsyncMessageSender.getInstance(getJDA()).enqueue(msg, callback);
    }

    @Override
    @Deprecated
    public Message sendFile(File file)
    {
        return sendFile(file, null);
    }

    @Override
    @Deprecated
    public void sendFileAsync(File file, Consumer<Message> callback)
    {
        sendFileAsync(file, null, callback);
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
                callback.accept(message);
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void sendTyping()
    {
        api.getRequester().post(Requester.DISCORD_API_PREFIX + "channels/" + getId() + "/typing", new JSONObject());
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
