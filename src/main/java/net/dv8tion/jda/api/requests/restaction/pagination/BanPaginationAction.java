package net.dv8tion.jda.api.requests.restaction.pagination;

import net.dv8tion.jda.api.entities.Guild;

public interface BanPaginationAction extends PaginationAction<Guild.Ban, BanPaginationAction>
{
    Guild getGuild();
}
