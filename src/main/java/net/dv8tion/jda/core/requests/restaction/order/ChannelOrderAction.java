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

package net.dv8tion.jda.core.requests.restaction.order;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.Route;
import org.apache.http.util.Args;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;

/**
 * Implementation of {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction OrderAction}
 * to modify the order of {@link net.dv8tion.jda.core.entities.Channel Channels} for a {@link net.dv8tion.jda.core.entities.Guild Guild}.
 * <br>To apply the changes you must finish the {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 *
 * @param <T>
 *        The type of {@link net.dv8tion.jda.core.entities.Channel Channel} defining
 *        which channels to order
 *
 * @since 3.0
 */
public class ChannelOrderAction<T extends Channel> extends OrderAction<T, ChannelOrderAction>
{
    protected final Guild guild;
    protected final ChannelType type;

    /**
     * Creates a new ChannelOrderAction instance
     *
     * @param guild
     *        The target {@link net.dv8tion.jda.core.entities.Guild Guild}
     *        of which to order the channels defined by the specified type
     * @param type
     *        The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} corresponding
     *        to the generic type of {@link net.dv8tion.jda.core.entities.Channel Channel} which
     *        defines the type of channel that will be ordered
     */
    public ChannelOrderAction(Guild guild, ChannelType type)
    {
        super(guild.getJDA(), Route.Guilds.MODIFY_CHANNELS.compile(guild.getId()));
        this.guild = guild;
        this.type = type;

        Collection chans = type == ChannelType.TEXT ? guild.getTextChannels() : guild.getVoiceChannels();
        this.orderList.addAll(chans);
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} which holds
     * the channels from {@link #getCurrentOrder()}
     *
     * @return The corresponding {@link net.dv8tion.jda.core.entities.Guild Guild}
     */
    public Guild getGuild()
    {
        return guild;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} of
     * all channels that are ordered by this ChannelOrderAction
     *
     * @return The corresponding {@link net.dv8tion.jda.core.entities.ChannelType ChannelType}
     */
    public ChannelType getChannelType()
    {
        return type;
    }

    @Override
    protected void finalizeData()
    {
        final Member self = guild.getSelfMember();
        if (!self.hasPermission(Permission.MANAGE_CHANNEL))
            throw new PermissionException(Permission.MANAGE_CHANNEL);
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

    @Override
    protected void validateInput(T entity)
    {
        Args.check(entity.getGuild().equals(guild), "Provided channel is not from this Guild!");
        Args.check(orderList.contains(entity), "Provided channel is not in the list of orderable channels!");
    }
}
