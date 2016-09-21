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

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.MessageHistory;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.handle.EntityBuilder;
import net.dv8tion.jda.core.requests.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PrivateChannelImpl implements PrivateChannel
{
    private final String id;
    private final User user;

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
                    request.onFailure(
                            new ErrorResponseException(
                                    ErrorResponse.fromJSON(response.getObject()), response));
                }
            }
        };
    }

    @Override
    public RestAction<Message> sendFile(File file, Message message)
    {
        return null;
    }

    @Override
    public RestAction<Message> getMessageById(String messageId)
    {
        checkNull(messageId, "messageId");

        Route.CompiledRoute route = Route.Messages.GET_MESSAGE.compile(getId(), messageId);
        return new RestAction<Message>(getJDA(), route, null) {
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
                    request.onFailure(
                            new ErrorResponseException(
                                    ErrorResponse.fromJSON(response.getObject()), response));
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
                if (response.isOk())
                {
                    request.onSuccess(null);
                }
                else
                {
                    request.onFailure(
                            new ErrorResponseException(
                                    ErrorResponse.fromJSON(response.getObject()), response));
                }
            }
        };
    }

    @Override
    public MessageHistory getHistory()
    {
        return null;
    }

    @Override
    public RestAction sendTyping()
    {
        return null;
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
                {

                    request.onSuccess(null);
                }
                else
                {
                    request.onFailure(
                            new ErrorResponseException(
                                    ErrorResponse.fromJSON(response.getObject()), response));
                }
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
                {
                    request.onSuccess(null);
                }
                else
                {
                    request.onFailure(
                            new ErrorResponseException(
                                    ErrorResponse.fromJSON(response.getObject()), response));
                }
            }
        };
    }

    @Override
    public RestAction<List<Message>> getPinnedMessages()
    {
        Route.CompiledRoute route = Route.Messages.GET_PINNED_MESSAGES.compile(getId());
        return new RestAction<List<Message>>(getJDA(), route, null) {
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
                    request.onFailure(
                            new ErrorResponseException(
                                    ErrorResponse.fromJSON(response.getObject()), response));
                }
            }
        };
    }

    @Override
    public RestAction close()
    {
        return null;
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

    public PrivateChannelImpl setFake(boolean fake)
    {
        this.fake = fake;
        return this;
    }

    private void checkNull(Object obj, String name)
    {
        if (obj == null)
            throw new NullPointerException("Provided " + name + " was null!");
    }
}
