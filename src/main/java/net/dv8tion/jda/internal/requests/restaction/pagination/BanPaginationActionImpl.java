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
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.pagination.BanPaginationAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.requests.Route;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BanPaginationActionImpl
    extends PaginationActionImpl<Guild.Ban, BanPaginationAction>
    implements BanPaginationAction
{
    protected final Guild guild;

    public BanPaginationActionImpl(Guild guild)
    {
        super(guild.getJDA(), Route.Guilds.GET_BANS.compile(guild.getId()), 1, 1000, 1000);
        this.guild = guild;
    }

    @Override
    @Nonnull
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    protected void handleSuccess(Response response, Request<List<Guild.Ban>> request)
    {
        EntityBuilder builder = api.getEntityBuilder();
        DataArray bannedArr = response.getArray();
        List<Guild.Ban> bans = new ArrayList<>(bannedArr.length());

        for (int i = 0; i < bannedArr.length(); i++)
        {
            final DataObject object = bannedArr.getObject(i);
            try
            {
                DataObject user = object.getObject("user");
                Guild.Ban ban = new Guild.Ban(builder.createUser(user), object.getString("reason", null));

                bans.add(ban);
            }
            catch (Exception t)
            {
                LOG.error("Got an unexpected error while decoding ban index {} for guild {}:\nData: {}",
                          i, guild.getId(), object, t);
            }
        }

        if (order == PaginationOrder.BACKWARD)
            Collections.reverse(bans);
        if (useCache)
            cached.addAll(bans);

        if (!bans.isEmpty())
        {
            last = bans.get(bans.size() - 1);
            lastKey = last.getUser().getIdLong();
        }

        request.onSuccess(Collections.unmodifiableList(bans));
    }

    @Override
    protected long getKey(Guild.Ban it)
    {
        return it.getUser().getIdLong();
    }
}
