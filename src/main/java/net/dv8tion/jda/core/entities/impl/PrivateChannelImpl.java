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

package net.dv8tion.jda.core.entities.impl;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.body.MultipartBody;
import net.dv8tion.jda.client.entities.Call;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.MessageHistory;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.*;
import net.dv8tion.jda.core.utils.IOUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PrivateChannelImpl implements PrivateChannel
{
    private final String id;
    private final User user;

    private Call currentCall = null;
    private boolean fake = false;

    public PrivateChannelImpl(String id, User user)
    {
        this.id = id;
        this.user = user;
    }

    @Override
    public User getUser()
    {
        return user;
    }

    @Override
    public String getName()
    {
        return user.getName();
    }

    @Override
    public ChannelType getType()
    {
        return ChannelType.PRIVATE;
    }

    @Override
    public JDA getJDA()
    {
        return user.getJDA();
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

        return sendFile(file, file.getName(), message);
    }

    @Override
    public RestAction<Message> sendFile(File file, String fileName, Message message) throws IOException
    {
        checkNull(file, "file");

        if(file == null || !file.exists() || !file.canRead())
            throw new IllegalArgumentException("Provided file is either null, doesn't exist or is not readable!");
        if (file.length() > 8<<20)   //8MB
            throw new IllegalArgumentException("File is to big! Max file-size is 8MB");

        return sendFile(IOUtil.readFully(file), fileName, message);
    }

    @Override
    public RestAction<Message> sendFile(InputStream data, String fileName, Message message)
    {
        checkNull(data, "data InputStream");
        checkNull(fileName, "fileName");

        Route.CompiledRoute route = Route.Messages.SEND_MESSAGE.compile(id);
        MultipartBody body = Unirest.post(Requester.DISCORD_API_PREFIX + route.getCompiledRoute())
                .fields(null); //We use this to change from an HttpRequest to a MultipartBody

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
                .fields(null); //We use this to change from an HttpRequest to a MultipartBody

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
    public RestAction sendTyping()
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
    public RestAction<Void> close()
    {
        Route.CompiledRoute route = Route.Channels.DELETE_CHANNEL.compile(id);
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
    public String getId()
    {
        return id;
    }

    @Override
    public boolean isFake()
    {
        return fake;
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

    public PrivateChannelImpl setFake(boolean fake)
    {
        this.fake = fake;
        return this;
    }

    public PrivateChannelImpl setCurrentCall(Call currentCall)
    {
        this.currentCall = currentCall;
        return this;
    }

    private void checkNull(Object obj, String name)
    {
        if (obj == null)
            throw new NullPointerException("Provided " + name + " was null!");
    }
}
