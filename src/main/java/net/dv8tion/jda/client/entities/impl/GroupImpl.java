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

package net.dv8tion.jda.client.entities.impl;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.body.MultipartBody;
import net.dv8tion.jda.client.entities.Call;
import net.dv8tion.jda.client.entities.Friend;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.MessageHistory;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.*;
import net.dv8tion.jda.core.utils.IOUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class GroupImpl implements Group
{
    private final String id;
    private final JDAImpl api;

    private HashMap<String, User> userMap = new HashMap<>();

    private Call currentCall;
    private User owner;
    private String name;
    private String iconId;

    public GroupImpl(String id, JDAImpl api)
    {
        this.id = id;
        this.api = api;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public ChannelType getType()
    {
        return ChannelType.GROUP;
    }

    @Override
    public String getIconId()
    {
        return iconId;
    }

    @Override
    public String getIconUrl()
    {
        return iconId == null ? null : "https://cdn.discordapp.com/channel-icons/" + id + "/" + iconId + ".jpg";
    }

    @Override
    public User getOwner()
    {
        return owner;
    }

    @Override
    public List<User> getUsers()
    {
        return Collections.unmodifiableList(
                new ArrayList<>(
                        userMap.values()));
    }

    @Override
    public List<User> getNonFriendUsers()
    {
        List<User> nonFriends = new ArrayList<>();
        userMap.forEach((userId, user) ->
        {
            Friend friend = api.asClient().getFriendById(userId);
            if (friend == null)
                nonFriends.add(user);
        });
        return Collections.unmodifiableList(nonFriends);
    }

    @Override
    public List<Friend> getFriends()
    {
        List<Friend> friends = new ArrayList<>();
        for (String userId : userMap.keySet())
        {
            Friend friend = api.asClient().getFriendById(userId);
            if (friend != null)
                friends.add(friend);
        }
        return Collections.unmodifiableList(friends);
    }

    @Override
    public RestAction<Call> startCall()
    {
        return null;
    }

    @Override
    public Call getCurrentCall()
    {
        return currentCall;
    }

    @Override
    public RestAction leaveGroup()
    {
        return null;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public RestAction<Message> sendMessage(String text)
    {
        return sendMessage(new MessageBuilder().appendString(text).build());
    }

    @Override
    public RestAction<Message> sendMessage(Message msg)
    {
        Route.CompiledRoute route = Route.Messages.SEND_MESSAGE.compile(getId());
        JSONObject json = new JSONObject().put("content", msg.getRawContent()).put("tts", msg.isTTS());
        return new RestAction<Message>(getJDA(), route, json)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                {
                    Message m = EntityBuilder.get(getJDA()).createMessage(response.getObject());
                    request.onSuccess(m);
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public RestAction<Message> sendFile(File file, Message message) throws IOException
    {
        checkNull(file, "file");

        if(file == null || !file.exists() || !file.canRead())
            throw new IllegalArgumentException("Provided file is either null, doesn't exist or is not readable!");
        if (file.length() > 8<<20)   //8MB
            throw new IllegalArgumentException("File is to big! Max file-size is 8MB");

        return sendFile(IOUtil.readFully(file), file.getName(), message);
    }

    @Override
    public RestAction<Message> sendFile(InputStream data, String fileName, Message message)
    {
        checkNull(data, "data InputStream");
        checkNull(fileName, "fileName");

        Route.CompiledRoute route = Route.Messages.SEND_MESSAGE.compile(id);
        MultipartBody body = Unirest.post(Requester.DISCORD_API_PREFIX + route.getCompiledRoute())
                .field("", ""); //We use this to change from an HttpRequest to a MultipartBody

        body.field("file", data, fileName);

        if (message != null)
        {
            body.field("content", message.getRawContent());
            body.field("tts", message.isTTS());
        }

        return new RestAction<Message>(getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(EntityBuilder.get(api).createMessage(response.getObject()));
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<Message> sendFile(byte[] data, String fileName, Message message)
    {
        checkNull(fileName, "fileName");

        if (data.length > 8<<20)   //8MB
            throw new IllegalArgumentException("Provided data is too large! Max file-size is 8MB");

        Route.CompiledRoute route = Route.Messages.SEND_MESSAGE.compile(id);
        MultipartBody body = Unirest.post(Requester.DISCORD_API_PREFIX + route.getCompiledRoute())
                .field("", ""); //We use this to change from an HttpRequest to a MultipartBody

        body.field("file", data, fileName);

        if (message != null)
        {
            body.field("content", message.getRawContent());
            body.field("tts", message.isTTS());
        }

        return new RestAction<Message>(getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(EntityBuilder.get(api).createMessage(response.getObject()));
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<Message> getMessageById(String messageId)
    {
        checkNull(messageId, "messageId");

        Route.CompiledRoute route = Route.Messages.GET_MESSAGE.compile(getId(), messageId);
        return new RestAction<Message>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                {
                    Message m = EntityBuilder.get(getJDA()).createMessage(response.getObject());
                    request.onSuccess(m);
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public RestAction<Void> deleteMessageById(String messageId)
    {
        checkNull(messageId, "messageId");

        Route.CompiledRoute route = Route.Messages.DELETE_MESSAGE.compile(getId(), messageId);
        return new RestAction<Void>(getJDA(), route, null) {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                //TODO: check if the fail is due to a permission error
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public MessageHistory getHistory()
    {
        return new MessageHistory(this);
    }

    @Override
    public RestAction<MessageHistory> getHistoryAround(Message markerMessage, int limit)
    {
        return MessageHistory.getHistoryAround(this, markerMessage, limit);
    }

    @Override
    public RestAction<MessageHistory> getHistoryAround(String markedMessageId, int limit)
    {
        return MessageHistory.getHistoryAround(this, markedMessageId, limit);
    }

    @Override
    public RestAction<Void> sendTyping()
    {
        Route.CompiledRoute route = Route.Channels.SEND_TYPING.compile(id);
        return new RestAction<Void>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<Void> pinMessageById(String messageId)
    {
        checkNull(messageId, "messageId");

        Route.CompiledRoute route = Route.Messages.ADD_PINNED_MESSAGE.compile(getId(), messageId);
        return new RestAction<Void>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<Void> unpinMessageById(String messageId)
    {
        checkNull(messageId, "messageId");

        Route.CompiledRoute route = Route.Messages.REMOVE_PINNED_MESSAGE.compile(getId(), messageId);
        return new RestAction<Void>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<List<Message>> getPinnedMessages()
    {
        Route.CompiledRoute route = Route.Messages.GET_PINNED_MESSAGES.compile(getId());
        return new RestAction<List<Message>>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                {
                    LinkedList<Message> pinnedMessages = new LinkedList<>();
                    EntityBuilder builder = EntityBuilder.get(getJDA());
                    JSONArray pins = response.getArray();

                    for (int i = 0; i < pins.length(); i++)
                    {
                        pinnedMessages.add(builder.createMessage(pins.getJSONObject(i)));
                    }

                    request.onSuccess(Collections.unmodifiableList(pinnedMessages));
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public String toString()
    {
        return String.format("G:%s(%s)", getName(), getId());
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Group))
            return false;

        Group oGroup = (Group) o;
        return id.equals(oGroup.getId());
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    public HashMap<String, User> getUserMap()
    {
        return userMap;
    }

    public GroupImpl setCurrentCall(Call call)
    {
        this.currentCall = call;
        return this;
    }

    public GroupImpl setOwner(User owner)
    {
        this.owner = owner;
        return this;
    }

    public GroupImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public GroupImpl setIconId(String iconId)
    {
        this.iconId = iconId;
        return this;
    }

    private void checkNull(Object obj, String name)
    {
        if (obj == null)
            throw new NullPointerException("Provided " + name + " was null!");
    }
}
