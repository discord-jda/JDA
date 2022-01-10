package net.dv8tion.jda.internal.requests.restaction.pagination;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.IThreadContainer;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadChannelPaginationAction;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.requests.Route;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ThreadChannelPaginationActionImpl extends PaginationActionImpl<ThreadChannel, ThreadChannelPaginationAction> implements ThreadChannelPaginationAction
{
    protected final IThreadContainer channel;
    protected final boolean useID;

    public ThreadChannelPaginationActionImpl(JDA api, Route.CompiledRoute route, IThreadContainer channel, boolean useID)
    {
        super(api, route, 2, 100, 100);
        this.channel = channel;
        this.useID = useID;
    }

    @Nonnull
    @Override
    public IThreadContainer getChannel()
    {
        return channel;
    }

    @Override
    protected Route.CompiledRoute finalizeRoute()
    {
        Route.CompiledRoute route = super.finalizeRoute();

        final String limit = String.valueOf(this.limit.get());
        final long last = this.lastKey;

        route = route.withQueryParams("limit", limit);

        if (last == 0)
            return route;

        if (useID)
            return route.withQueryParams("before", Long.toUnsignedString(last));
        // OffsetDateTime#toString() is defined to be ISO8601, needs no helper method.
        return route.withQueryParams("before", TimeUtil.getTimeCreated(last).toString());
    }

    @Override
    protected void handleSuccess(Response response, Request<List<ThreadChannel>> request)
    {
        DataObject obj = response.getObject();
        DataArray selfThreadMembers = obj.getArray("members");
        DataArray threads = obj.getArray("threads");

        List<ThreadChannel> list = new ArrayList<>(threads.length());
        EntityBuilder builder = api.getEntityBuilder();

        TLongObjectMap<DataObject> selfThreadMemberMap = new TLongObjectHashMap<>();
        for (int i = 0; i < selfThreadMembers.length(); i++)
        {
            DataObject selfThreadMember = selfThreadMembers.getObject(i);

            //Store the thread member based on the "id" which is the _thread's_ id, not the member's id (which would be our id)
            selfThreadMemberMap.put(selfThreadMember.getLong("id"), selfThreadMember);
        }

        for (int i = 0; i < threads.length(); i++)
        {
            try
            {
                DataObject threadObj = threads.getObject(i);
                DataObject selfThreadMemberObj = selfThreadMemberMap.get(threadObj.getLong("id", 0));

                if (selfThreadMemberObj != null)
                {
                    //Combine the thread and self thread-member into a single object to model what we get from
                    // thread payloads (like from Gateway, etc)
                    threadObj.put("member", selfThreadMemberObj);
                }

                ThreadChannel thread = builder.createThreadChannel(threadObj, getGuild().getIdLong());
                list.add(thread);

                if (this.useCache)
                    this.cached.add(thread);
                this.last = thread;
                this.lastKey = last.getIdLong();
            }
            catch (ParsingException | NullPointerException e)
            {
                LOG.warn("Encountered exception in ThreadChannelPagination", e);
            }
        }

        request.onSuccess(list);
    }

    @Override
    protected long getKey(ThreadChannel it)
    {
        return it.getIdLong();
    }
}
