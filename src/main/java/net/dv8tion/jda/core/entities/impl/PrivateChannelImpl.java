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
import net.dv8tion.jda.core.MessageHistory;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;

import java.io.File;

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
    public RestAction sendMessage(String text)
    {
        return null;
    }

    @Override
    public RestAction sendMessage(Message msg)
    {
        return null;
    }

    @Override
    public RestAction sendFile(File file, Message message)
    {
        return null;
    }

    @Override
    public RestAction getMessageById(String messageId)
    {
        return null;
    }

    @Override
    public RestAction deleteMessageById(String messageId)
    {
        return null;
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
    public RestAction pinMessageById(String messageId)
    {
        return null;
    }

    @Override
    public RestAction unpinMessageById(String messageId)
    {
        return null;
    }

    @Override
    public RestAction getPinnedMessages()
    {
        return null;
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
}
