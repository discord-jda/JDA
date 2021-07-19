package net.dv8tion.jda.api.events.user.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UserUpdateBannerEvent extends GenericUserUpdateEvent<String>
{
    public static final String IDENTIFIER = "user_banner";

    public UserUpdateBannerEvent(@Nonnull JDA api, long responseNumber, @Nonnull User user, @Nullable String oldBanner)
    {
        super(api, responseNumber, user, oldBanner, user.getBannerId(), IDENTIFIER);
    }

    @Nullable
    public String getOldBannerId()
    {
        return getOldValue();
    }

    @Nullable
    public String getOldBannerUrl()
    {
        return previous == null ? null : String.format(User.BANNER_URL, getUser().getId(), previous, previous.startsWith("a_") ? "gif" : "png");
    }

    @Nullable
    public String getNewBannerId()
    {
        return getNewValue();
    }

    @Nullable
    public String getNewBannerUrl()
    {
        return next == null ? null : String.format(User.BANNER_URL, getUser().getId(), next, next.startsWith("a_") ? "gif" : "png");
    }
}
