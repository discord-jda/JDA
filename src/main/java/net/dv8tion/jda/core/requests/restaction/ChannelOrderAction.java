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

import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.apache.http.util.Args;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ChannelOrderAction extends RestAction<Void>
{
    private final Guild guild;
    private final boolean textChannels;
    private final List<Channel> channels;
    private int selectedPosition = -1;

    public ChannelOrderAction(Guild guild, boolean textChannels)
    {
        super(guild.getJDA(), Route.Guilds.MODIFY_CHANNELS.compile(guild.getId()), null);
        this.guild = guild;
        this.textChannels = textChannels;

        Collection chans = textChannels ? guild.getTextChannels() : guild.getVoiceChannels();
        this.channels = new ArrayList<>(chans.size());
        this.channels.addAll(chans);
    }

    public List<Channel> currentOrder()
    {
        return Collections.unmodifiableList(channels);
    }

    public ChannelOrderAction selectPosition(int selectedPosition)
    {
        Args.notNegative(selectedPosition, "Provided selectedPosition");
        Args.check(selectedPosition < channels.size(), "Provided selectedPosition is too big and is out of bounds. selectedPosition: "
                + selectedPosition);

        this.selectedPosition = selectedPosition;

        return this;
    }

    public ChannelOrderAction selectPosition(Channel selectedChannel)
    {
        Args.notNull(selectedChannel, "Channel");
        Args.check(selectedChannel.getGuild().equals(guild), "Provided selected channel is not from this Guild!");
        Args.check(selectedChannel instanceof TextChannel == textChannels,
                "Provided channel's type is not the same as the channel type being handled by this ChannelOrderAction.");

        return selectPosition(channels.indexOf(selectedChannel));
    }

    public ChannelOrderAction moveUp(int amount)
    {
        Args.notNegative(amount, "Provided amount");
        if (selectedPosition == -1)
            throw new IllegalStateException("Cannot move until a channel has been selected. Use #selectChannel first.");
        Args.check(selectedPosition - amount >= 0,
                "Amount provided to move up is too large and would be out of bounds." +
                "Selected position: " + selectedPosition + " Amount: " + amount + " Largest Position: " + channels.size());

        return moveTo(selectedPosition - amount);
    }

    public ChannelOrderAction moveDown(int amount)
    {
        Args.notNegative(amount, "Provided amount");
        if (selectedPosition == -1)
            throw new IllegalStateException("Cannot move until a channel has been selected. Use #selectChannel first.");

        Args.check(selectedPosition + amount < channels.size(),
                "Amount provided to move down is too large and would be out of bounds." +
                "Selected position: " + selectedPosition + " Amount: " + amount + " Largest Position: " + channels.size());

        return moveTo(selectedPosition + amount);
    }

    public ChannelOrderAction moveTo(int position)
    {
        Args.notNegative(position, "Provided position");
        Args.check(position < channels.size(), "Provided position is too big and is out of bounds.");

        Channel selectedChannel = channels.remove(selectedPosition);
        channels.add(position, selectedChannel);

        return this;
    }

    public ChannelOrderAction swapPosition(Channel swapChannel)
    {
        Args.notNull(swapChannel, "Provided swapChannel");
        Args.check(swapChannel.getGuild().equals(guild), "Provided selected channel is not from this Guild!");
        Args.check(swapChannel instanceof TextChannel == textChannels,
                "Provided channel's type is not the same as the channel type being handled by this ChannelOrderAction.");

        return swapPosition(channels.indexOf(swapChannel));
    }


    public ChannelOrderAction swapPosition(int swapPosition)
    {
        Args.notNegative(swapPosition, "Provided swapPosition");
        Args.check(swapPosition < channels.size(), "Provided swapPosition is too big and is out of bounds. swapPosition: "
                + swapPosition);

        Channel selectedChannel = channels.get(selectedPosition);
        Channel swapChannel = channels.get(swapPosition);
        channels.set(swapPosition, selectedChannel);
        channels.set(selectedPosition, swapChannel);

        return this;
    }

    public ChannelType getChannelType()
    {
        return textChannels ? ChannelType.TEXT : ChannelType.VOICE;
    }

    public Guild getGuild()
    {
        return guild;
    }

    public int getSelectedPosition()
    {
        return selectedPosition;
    }

    public Channel getSelectedChannel()
    {
        if (selectedPosition == -1)
            throw new IllegalStateException("No position has been selected yet");

        return channels.get(selectedPosition);
    }

    @Override
    protected void finalizeData()
    {
        JSONArray array = new JSONArray();
        for (int i = 0; i < channels.size(); i++)
        {
            Channel chan = channels.get(i);
            array.put(new JSONObject()
                    .put("id", chan.getId())
                    .put("position", i));
        }

        this.data = array;
    }

    @Override
    protected void handleResponse(Response response, Request request)
    {
        if (response.isOk())
            request.onSuccess(null);
        else
            request.onFailure(response);
    }
}
