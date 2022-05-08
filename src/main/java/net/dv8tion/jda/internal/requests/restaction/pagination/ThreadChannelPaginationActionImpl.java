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
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;

public class ThreadChannelPaginationActionImpl extends PaginationActionImpl<ThreadChannel, ThreadChannelPaginationAction> implements ThreadChannelPaginationAction
{
    protected final IThreadContainer channel;

    // Whether IDs or ISO8601 timestamps shall be provided for all pagination requests.
    // Some thread pagination endpoints require this odd and singular behavior throughout the discord api.
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

    @Nonnull
    @Override
    public EnumSet<PaginationOrder> getSupportedOrders()
    {
        return EnumSet.of(PaginationOrder.BACKWARD);
    }

    //Thread pagination supplies ISO8601 timestamps for some cases, see constructor
    @Nonnull
    @Override
    protected String getPaginationLastEvaluatedKey(long lastId, ThreadChannel last)
    {
        if (useID)
            return Long.toUnsignedString(lastId);

        if (order == PaginationOrder.FORWARD && lastId == 0)
        {
            // first second of 2015 aka discords epoch, hard coding something older makes no sense to me
            return "2015-01-01T00:00:00.000";
        }

        // this should be redundant, due to calling this with PaginationAction#getLast() as last param,
        // but let's have this here.
        if (last == null)
            throw new NoSuchElementException("No entities have been retrieved yet.");

        // OffsetDateTime#toString() is defined to be ISO8601, needs no helper method.
        return last.getTimeArchiveInfoLastModified().toString();
    }

    @Override
    protected void handleSuccess(Response response, Request<List<ThreadChannel>> request)
    {
        DataObject obj = response.getObject();
        DataArray selfThreadMembers = obj.getArray("members");
        DataArray threads = obj.getArray("threads");

        List<ThreadChannel> list = new ArrayList<>(threads.length());
        EntityBuilder builder = api.getEntityBuilder();

        TLongObjectMap<DataObject> selfThreadMemberMap = Helpers.convertToMap((o) -> o.getUnsignedLong("id"), selfThreadMembers);

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
