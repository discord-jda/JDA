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

package net.dv8tion.jda.internal.requests.restaction.pagination;

import net.dv8tion.jda.api.entities.ThreadMember;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadMemberPaginationAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.channel.concrete.ThreadChannelImpl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ThreadMemberPaginationActionImpl
    extends PaginationActionImpl<ThreadMember, ThreadMemberPaginationAction>
    implements ThreadMemberPaginationAction
{
    private final ThreadChannelImpl channel;

    public ThreadMemberPaginationActionImpl(ThreadChannel channel)
    {
        super(channel.getJDA(), Route.Channels.LIST_THREAD_MEMBERS.compile(channel.getId()).withQueryParams("with_member", "true"), 1, 100, 100);
        this.channel = (ThreadChannelImpl) channel;
        this.order = PaginationOrder.FORWARD;
    }

    @Nonnull
    @Override
    public ThreadChannel getThreadChannel()
    {
        return channel;
    }

    @Nonnull
    @Override
    public EnumSet<PaginationOrder> getSupportedOrders()
    {
        return EnumSet.of(getOrder());
    }

    @Override
    protected long getKey(ThreadMember it)
    {
        return it.getIdLong();
    }

    @Override
    protected void handleSuccess(Response response, Request<List<ThreadMember>> request)
    {
        DataArray array = response.getArray();
        List<ThreadMember> members = new ArrayList<>(array.length());
        EntityBuilder builder = api.getEntityBuilder();
        for (int i = 0; i < array.length(); i++)
        {
            try
            {
                DataObject object = array.getObject(i);
                if (object.isNull("member"))
                    continue;
                ThreadMember threadMember = builder.createThreadMember(channel.getGuild(), channel, object);
                members.add(threadMember);
            }
            catch (ParsingException | NullPointerException e)
            {
                LOG.warn("Encountered an exception in ThreadMemberPaginationAction", e);
            }
        }

//        if (order == PaginationOrder.BACKWARD)
//            Collections.reverse(members);
        if (useCache)
            cached.addAll(members);

        if (!members.isEmpty())
        {
            last = members.get(members.size() - 1);
            lastKey = last.getIdLong();
        }
        request.onSuccess(members);
    }
}
