package net.dv8tion.jda.internal.requests.restaction.pagination;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.pagination.BanPaginationAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.requests.Route;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
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
    protected Route.CompiledRoute finalizeRoute()
    {
        Route.CompiledRoute route = super.finalizeRoute();

        String after = null;
        String limit = String.valueOf(getLimit());
        long last = this.lastKey;
        if (last != 0)
            after = Long.toUnsignedString(last);

        route = route.withQueryParams("limit", limit);

        if (after != null)
            route = route.withQueryParams("after", after);

        return route;
    }

    @Override
    protected void handleSuccess(Response response, Request<List<Guild.Ban>> request)
    {
        EntityBuilder builder = api.getEntityBuilder();
        List<Guild.Ban> bans = new LinkedList<>();
        DataArray bannedArr = response.getArray();

        for (int i = 0; i < bannedArr.length(); i++)
        {
            final DataObject object = bannedArr.getObject(i);
            DataObject user = object.getObject("user");

            bans.add(new Guild.Ban(builder.createUser(user), object.getString("reason", null)));
        }

        request.onSuccess(Collections.unmodifiableList(bans));
    }

    @Override
    protected long getKey(Guild.Ban it)
    {
        return it.getUser().getIdLong();
    }
}
