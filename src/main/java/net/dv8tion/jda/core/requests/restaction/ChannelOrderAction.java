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

package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.Route;
import org.apache.http.util.Args;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;

public class ChannelOrderAction extends OrderAction<Channel, ChannelOrderAction>
{
    private final boolean textChannels;

    public ChannelOrderAction(Guild guild, boolean textChannels)
    {
        super(guild, Route.Guilds.MODIFY_CHANNELS.compile(guild.getId()));
        this.textChannels = textChannels;

        Collection chans = textChannels ? guild.getTextChannels() : guild.getVoiceChannels();
        this.orderList.addAll(chans);
    }

    public ChannelOrderAction selectPosition(Channel selectedChannel)
    {
        Args.notNull(selectedChannel, "Channel");
        Args.check(selectedChannel.getGuild().equals(guild), "Provided selected channel is not from this Guild!");
        Args.check(selectedChannel instanceof TextChannel == textChannels,
                "Provided channel's type is not the same as the channel type being handled by this ChannelOrderAction.");

        return selectPosition(orderList.indexOf(selectedChannel));
    }

    public ChannelOrderAction swapPosition(Channel swapChannel)
    {
        Args.notNull(swapChannel, "Provided swapChannel");
        Args.check(swapChannel.getGuild().equals(guild), "Provided selected channel is not from this Guild!");
        Args.check(swapChannel instanceof TextChannel == textChannels,
                "Provided channel's type is not the same as the channel type being handled by this ChannelOrderAction.");

        return swapPosition(orderList.indexOf(swapChannel));
    }

    public ChannelType getChannelType()
    {
        return textChannels ? ChannelType.TEXT : ChannelType.VOICE;
    }

    public Channel getSelectedChannel()
    {
        if (selectedPosition == -1)
            throw new IllegalStateException("No position has been selected yet");

        return orderList.get(selectedPosition);
    }

    public TextChannel getSelectedTextChannel()
    {
        if (!textChannels)
            throw new IllegalStateException("This ChannelOrderAction is dealing with VoiceChannels, NOT TextChannels");

        return (TextChannel) getSelectedChannel();
    }

    public VoiceChannel getSelectedVoiceChannel()
    {
        if (textChannels)
            throw new IllegalStateException("This ChannelOrderAction is dealing with TextChannels, NOT VoiceChannels");

        return (VoiceChannel) getSelectedChannel();
    }

    @Override
    protected void finalizeData()
    {
        JSONArray array = new JSONArray();
        for (int i = 0; i < orderList.size(); i++)
        {
            Channel chan = orderList.get(i);
            array.put(new JSONObject()
                    .put("id", chan.getId())
                    .put("position", i));
        }

        this.data = array;
    }
}
