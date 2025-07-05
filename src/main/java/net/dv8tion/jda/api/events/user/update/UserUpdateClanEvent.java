package net.dv8tion.jda.api.events.user.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Clan;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;

public class UserUpdateClanEvent extends GenericUserUpdateEvent<Clan>
{
    public static final String IDENTIFIER = "clan";

    public UserUpdateClanEvent(@Nonnull JDA api, long responseNumber, @Nonnull User user, @Nonnull Clan clan)
    {
        super(api, responseNumber, user, user.getPrimaryClan(), clan, IDENTIFIER);
    }

    /**
     * The old clan
     *
     * @return The old clan
     */
    @Nonnull
    public Clan getOldClan()
    {
        assert getOldValue() != null;

        return getOldValue();
    }

    /**
     * The new clan
     *
     * @return The new clan
     */
    @Nonnull
    public Clan getNewClan()
    {
        assert getNewValue() != null;

        return getNewValue();
    }
}

