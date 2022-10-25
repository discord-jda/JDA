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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.pagination.ScheduledEventMembersPaginationAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScheduledEventMembersPaginationActionImpl extends PaginationActionImpl<Member, ScheduledEventMembersPaginationAction> implements ScheduledEventMembersPaginationAction
{
    protected final Guild guild;

    public ScheduledEventMembersPaginationActionImpl(ScheduledEvent event)
    {
        super(event.getGuild().getJDA(), Route.Guilds.GET_SCHEDULED_EVENT_USERS.compile(event.getGuild().getId(), event.getId()).withQueryParams("with_member", "true"), 1, 100, 100);
        this.guild = event.getGuild();
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    protected void handleSuccess(Response response, Request<List<Member>> request)
    {
        DataArray array = response.getArray();
        List<Member> members = new ArrayList<>(array.length());
        EntityBuilder builder = api.getEntityBuilder();
        for (int i = 0; i < array.length(); i++)
        {
            try
            {
                DataObject object = array.getObject(i);
                if (object.isNull("member"))
                    continue;
                DataObject userObject = object.getObject("user");
                DataObject memberObject = object.getObject("member");
                Member member = builder.createMember((GuildImpl) guild, memberObject.put("user", userObject));
                members.add(member);
            }
            catch (ParsingException | NullPointerException e)
            {
                LOG.warn("Encountered an exception in ScheduledEventPagination", e);
            }
        }

        if (order == PaginationOrder.BACKWARD)
            Collections.reverse(members);
        if (useCache)
            cached.addAll(members);

        if (!members.isEmpty())
        {
            last = members.get(members.size() - 1);
            lastKey = last.getIdLong();
        }
        request.onSuccess(members);
    }

    @Override
    protected long getKey(Member it)
    {
        return it.getIdLong();
    }
}
