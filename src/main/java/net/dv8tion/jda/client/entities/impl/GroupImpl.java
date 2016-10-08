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

import net.dv8tion.jda.client.entities.Call;
import net.dv8tion.jda.client.entities.Friend;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageHistory;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.RestAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
        return null;
    }

    @Override
    public RestAction<Message> sendMessage(Message msg)
    {
        return null;
    }

    @Override
    public RestAction<Message> sendFile(File file, Message message)
    {
        return null;
    }

    @Override
    public RestAction<Message> getMessageById(String messageId)
    {
        return null;
    }

    @Override
    public RestAction<Void> deleteMessageById(String messageId)
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
    public RestAction<Void> pinMessageById(String messageId)
    {
        return null;
    }

    @Override
    public RestAction<Void> unpinMessageById(String messageId)
    {
        return null;
    }

    @Override
    public RestAction<List<Message>> getPinnedMessages()
    {
        return null;
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
}
