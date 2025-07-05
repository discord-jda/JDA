package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.Clan;
import net.dv8tion.jda.api.utils.data.DataObject;

public class ClanImpl implements Clan
{

    private final String identity_guild_id;
    private final boolean identity_enabled;
    private final String tagName;
    private final String badge;

    public ClanImpl(DataObject data)
    {
        this.identity_guild_id = data.getString("identity_guild_id");
        this.identity_enabled = data.getBoolean("identity_enabled");
        this.tagName = data.getString("tag");
        this.badge = data.getString("badge");
    }

    @Override
    public String getGuildId()
    {
        return identity_guild_id;
    }

    @Override
    public String getTagName()
    {
        return tagName;
    }

    @Override
    public String getBadge()
    {
        return badge;
    }

    @Override
    public boolean isEnabled()
    {
        return identity_enabled;
    }

    @Override
    public boolean equals(Clan clan)
    {
        if (!getGuildId().equals(clan.getGuildId()))
            return false;

        if (isEnabled() != clan.isEnabled())
            return false;

        if (!getTagName().equals(clan.getTagName()))
            return false;

        return getBadge().equals(clan.getBadge());
    }
}
