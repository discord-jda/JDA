/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.client.entities.Call;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;

public class PrivateChannelImpl implements PrivateChannel
{
    private final String id;
    private final User user;

    private String lastMessageId;
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
    public String getLatestMessageId()
    {
        String messageId = lastMessageId;
        if (messageId == null)
            throw new IllegalStateException("No last message id found.");
        return messageId;
    }

    @Override
    public boolean hasLatestMessage()
    {
        return lastMessageId != null;
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

    public PrivateChannelImpl setLastMessageId(String id)
    {
        this.lastMessageId = id;
        return this;
    }

    private void checkNull(Object obj, String name)
    {
        if (obj == null)
            throw new NullPointerException("Provided " + name + " was null!");
    }
}
