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

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.OnlineStatus;
import net.dv8tion.jda.entities.Game;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.handle.EntityBuilder;
import net.dv8tion.jda.requests.Requester;
import org.json.JSONException;
import org.json.JSONObject;

public class UserImpl implements User
{
    private final String id;
    private final JDAImpl api;
    private String username;
    private String discriminator;
    private String avatarId;
    private Game game = null;
    private OnlineStatus onlineStatus = OnlineStatus.OFFLINE;
    private PrivateChannel privateChannel = null;
    private boolean isBot = false;

    public UserImpl(String id, JDAImpl api)
    {
        this.id = id;
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
    public String getUsername()
    {
        return username;
    }

    @Override
    public String getDiscriminator()
    {
        return discriminator;
    }

    @Override
    public String getAsMention()
    {
        return "<@" + getId() + '>';
    }

    @Override
    public String getAvatarId()
    {
        return avatarId;
    }

    @Override
    public String getAvatarUrl()
    {
        return getAvatarId() == null ? null : "https://cdn.discordapp.com/avatars/" + getId() + "/" + getAvatarId() + ".jpg";
    }

    @Override
    public String getDefaultAvatarId()
    {
        return DefaultAvatar.values()[Integer.parseInt(getDiscriminator()) % DefaultAvatar.values().length].toString();
    }

    @Override
    public String getDefaultAvatarUrl()
    {
        return "https://discordapp.com/assets/" + getDefaultAvatarId() + ".png";
    }

    @Override
    public Game getCurrentGame()
    {
        return game;
    }

    @Override
    public OnlineStatus getOnlineStatus()
    {
        return onlineStatus;
    }

    @Override
    public PrivateChannel getPrivateChannel()
    {
        if (privateChannel == null)
        {
            try
            {
                Requester.Response response = api.getRequester().post(Requester.DISCORD_API_PREFIX + "users/@me/channels",
                        new JSONObject().put("recipient_id", getId()));
                if(response.isOk())
                    new EntityBuilder(api).createPrivateChannel(response.getObject());
                else
                    throw new RuntimeException("Could not get Private-channel for user: " + getUsername() + "... Error: " + response.toString());
            }
            catch (JSONException ex)
            {
                JDAImpl.LOG.log(ex);
            }
        }
        return privateChannel;
    }

    @Override
    public boolean isBot()
    {
        return isBot;
    }

    public UserImpl setUserName(String username)
    {
        this.username = username;
        return this;
    }

    public UserImpl setDiscriminator(String discriminator)
    {
        this.discriminator = discriminator;
        return this;
    }

    public UserImpl setAvatarId(String avatarId)
    {
        this.avatarId = avatarId;
        return this;
    }

    public UserImpl setCurrentGame(Game game)
    {
        this.game = game;
        return this;
    }

    public UserImpl setOnlineStatus(OnlineStatus onlineStatus)
    {
        this.onlineStatus = onlineStatus;
        return this;
    }

    public UserImpl setPrivateChannel(PrivateChannel channel)
    {
        this.privateChannel = channel;
        if (channel != null)
        {
            api.getPmChannelMap().put(channel.getId(), channel);
        }
        return this;
    }

    public UserImpl setIsBot(boolean isBot)
    {
        this.isBot = isBot;
        return this;
    }

    public boolean hasPrivateChannel()
    {
        return privateChannel != null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof User))
            return false;
        User oUser = (User) o;
        return this == oUser || this.getId().equals(oUser.getId());
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }

    @Override
    public String toString()
    {
        return "U:" + getUsername() + '(' + getId() + ')';
    }

    private enum DefaultAvatar
    {
        BLURPLE("6debd47ed13483642cf09e832ed0bc1b"),
        GREY("322c936a8c8be1b803cd94861bdfa868"),
        GREEN("dd4dbc0016779df1378e7812eabaa04d"),
        ORANGE("0e291f67c9274a1abdddeb3fd919cbaa"),
        RED("1cbd08c76f8af6dddce02c5138971129");

        private final String text;

        DefaultAvatar(String text)
        {
            this.text = text;
        }

        @Override
        public String toString()
        {
            return text;
        }
    }
}
