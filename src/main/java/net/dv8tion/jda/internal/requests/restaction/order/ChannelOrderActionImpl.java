/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.internal.requests.restaction.order;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.restaction.order.ChannelOrderAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.stream.Collectors;

public class ChannelOrderActionImpl
    extends OrderActionImpl<GuildChannel, ChannelOrderAction>
    implements ChannelOrderAction
{
    protected final Guild guild;
    protected final int bucket;

    /**
     * Creates a new ChannelOrderAction instance
     *
     * @param  guild
     *         The target {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         of which to order the channels defined by the specified type
     * @param  bucket
     *         The sorting bucket
     */
    public ChannelOrderActionImpl(Guild guild, int bucket)
    {
        this(guild, bucket, getChannelsOfType(guild, bucket));
    }

    /**
     * Creates a new ChannelOrderAction instance using the provided
     * {@link net.dv8tion.jda.api.entities.Guild Guild}, as well as the provided
     * list of {@link GuildChannel Channels}.
     *
     * @param  guild
     *         The target {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         of which to order the channels defined by the specified type
     * @param  bucket
     *         The sorting bucket
     * @param  channels
     *         The {@link GuildChannel Channels} to order, all of which
     *         are on the same Guild specified, and all of which are of the same generic type of GuildChannel
     *         corresponding to the the ChannelType specified.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the channels are {@code null}, an empty collection,
     *         or any of them do not have the same ChannelType as the one
     *         provided.
     */
    public ChannelOrderActionImpl(Guild guild, int bucket, Collection<? extends GuildChannel> channels)
    {
        super(guild.getJDA(), Route.Guilds.MODIFY_CHANNELS.compile(guild.getId()));

        Checks.notNull(channels, "Channels to order");
        Checks.notEmpty(channels, "Channels to order");
        Checks.check(channels.stream().allMatch(c -> guild.equals(c.getGuild())),
            "One or more channels are not from the correct guild");
        Checks.check(channels.stream().allMatch(c -> c.getType().getSortBucket() == bucket),
            "One or more channels did not match the expected bucket " + bucket);

        this.guild = guild;
        this.bucket = bucket;
        this.orderList.addAll(channels);
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    public int getSortBucket()
    {
        return bucket;
    }

    @Override
    protected RequestBody finalizeData()
    {
        final Member self = guild.getSelfMember();
        if (!self.hasPermission(Permission.MANAGE_CHANNEL))
            throw new InsufficientPermissionException(guild, Permission.MANAGE_CHANNEL);
        DataArray array = DataArray.empty();
        for (int i = 0; i < orderList.size(); i++)
        {
            GuildChannel chan = orderList.get(i);
            array.add(DataObject.empty()
                    .put("id", chan.getId())
                    .put("position", i));
        }

        return getRequestBody(array);
    }

    @Override
    protected void validateInput(GuildChannel entity)
    {
        Checks.check(entity.getGuild().equals(guild), "Provided channel is not from this Guild!");
        Checks.check(orderList.contains(entity), "Provided channel is not in the list of orderable channels!");
    }

    protected static Collection<GuildChannel> getChannelsOfType(Guild guild, int bucket)
    {
        return guild.getChannels().stream()
            .filter(it -> it.getType().getSortBucket() == bucket)
            .sorted()
            .collect(Collectors.toList());
    }
}
