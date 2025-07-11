package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.Clan;
import net.dv8tion.jda.api.utils.data.DataObject;

public class ClanImpl implements Clan
{

    public static final String CLAN_KEY = "clan";

    private String identity_guild_id;
    private boolean identity_enabled;
    private String tagName;
    private String badge;

    public ClanImpl(DataObject data)
    {
        if (data == null) return;

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
    public boolean equals(Clan otherClan)
    {
        if (!getGuildId().equals(otherClan.getGuildId()))
            return false;

        if (isEnabled() != otherClan.isEnabled())
            return false;

        if (!getTagName().equals(otherClan.getTagName()))
            return false;

        return getBadge().equals(otherClan.getBadge());
    }
}
