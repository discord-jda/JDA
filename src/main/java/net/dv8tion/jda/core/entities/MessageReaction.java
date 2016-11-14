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

package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.impl.EmoteImpl;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class MessageReaction
{

    private final MessageChannel channel;
    private final ReactionEmote emote;
    private final String messageId;
    private final boolean self;
    private final int count;

    public MessageReaction(MessageChannel channel, ReactionEmote emote, String messageId, boolean self, int count)
    {
        this.channel = channel;
        this.emote = emote;
        this.messageId = messageId;
        this.self = self;
        this.count = count;
    }

    public boolean isSelf()
    {
        return self;
    }

    public int getCount()
    {
        return count;
    }

    public MessageChannel getChannel()
    {
        return channel;
    }

    public ReactionEmote getEmote()
    {
        return emote;
    }

    public String getMessageId()
    {
        return messageId;
    }

    public JDA getJDA()
    {
        return channel.getJDA();
    }

    public RestAction<List<User>> getUsers()
    {
        return getUsers(100);
    }

    public RestAction<List<User>> getUsers(int amount)
    {
        if (amount < 1 || amount > 100)
            throw new IllegalArgumentException("Amount is out of range 1-100!");
        String code = emote.isEmote()
                ? emote.getName() + ":" + emote.getId()
                : encode(emote.getName());
        Route.CompiledRoute route = Route.Messages.GET_REACTION_USERS.compile(channel.getId(), messageId, code, String.valueOf(amount));
        return new RestAction<List<User>>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }
                List<User> users = new LinkedList<>();
                JSONArray array = response.getArray();
                for (int i = 0; i < array.length(); i++)
                {
                    JSONObject json = array.getJSONObject(i);
                    String userId = json.getString("id");
                    User user = api.getUserById(userId);
                    if (user == null)
                        user = api.getFakeUserMap().get(userId);
                    if (user == null)
                        user = EntityBuilder.get(api).createFakeUser(json, true);
                    users.add(user);
                }
                request.onSuccess(users);
            }
        };
    }

    public RestAction<Void> removeReaction()
    {
        return removeReaction(getJDA().getSelfUser());
    }

    public RestAction<Void> removeReaction(User user)
    {
        if (user == null)
            throw new IllegalArgumentException("Provided User was null!");
        if (!user.equals(getJDA().getSelfUser()))
        {
            if (channel.getType() == ChannelType.TEXT)
            {
                Channel channel = (Channel) this.channel;
                if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
                    throw new PermissionException(Permission.MESSAGE_MANAGE);
            }
            else
            {
                throw new PermissionException("Unable to remove Reaction of other user in non-text channel!");
            }
        }

        String code = emote.isEmote()
                    ? emote.getName() + ":" + emote.getId()
                    : encode(emote.getName());
        Route.CompiledRoute route = Route.Messages.REMOVE_REACTION.compile(channel.getId(), messageId, code, user.getId());
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
    public boolean equals(Object obj)
    {
        return obj instanceof MessageReaction
                && ((MessageReaction) obj).emote.equals(emote)
                && (((MessageReaction) obj).self == self)
                && ((MessageReaction) obj).messageId.equals(messageId);
    }

    @Override
    public String toString()
    {
        return "MR:(M:(" + messageId + ") / " + emote + ")";
    }

    private static String encode(String chars)
    {
        try
        {
            return URLEncoder.encode(chars, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e); //thanks JDK 1.4
        }
    }

    public static class ReactionEmote implements ISnowflake
    {

        private final JDA api;
        private final String name;
        private final String id;

        public ReactionEmote(String name, String id, JDA api)
        {
            this.name = name;
            this.id = id;
            this.api = api;
        }

        public ReactionEmote(Emote emote)
        {
            this(emote.getName(), emote.getId(), emote.getJDA());
        }

        public boolean isEmote()
        {
            return id != null;
        }

        @Override
        public String getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public Emote getEmote()
        {
            if (!isEmote())
                return null;
            Emote e = api.getEmoteById(id);
            return e != null ? e : new EmoteImpl(id, api).setName(name);
        }

        public JDA getJDA()
        {
            return api;
        }

        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof ReactionEmote
                    && Objects.equals(((ReactionEmote) obj).getId(), id)
                    && ((ReactionEmote) obj).getName().equals(name);
        }

        @Override
        public String toString()
        {
            return "RE:" + (isEmote() ? getEmote() : getName() + "(" + getId() + ")");
        }
    }

}
