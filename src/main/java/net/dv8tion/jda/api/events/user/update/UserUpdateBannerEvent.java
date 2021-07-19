package net.dv8tion.jda.api.events.user.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the banner of a {@link net.dv8tion.jda.api.entities.User User} changed.
 *
 * <p>Can be used to retrieve the user who changed their banner and their previous banner.
 *
 * <p>Identifier: {@code banner}
 *
 * <h2>Requirements</h2>
 * <p>This event requires that the user be fetched by the {@link net.dv8tion.jda.api.JDA#retrieveUserById(long)} method
 * as the user's profile data is not sent directly. Thus, the old value will be null even if the user has a banner unless
 * previously fetched.
 */
public class UserUpdateBannerEvent extends GenericUserUpdateEvent<String>
{
    public static final String IDENTIFIER = "user_banner";

    public UserUpdateBannerEvent(@Nonnull JDA api, long responseNumber, @Nonnull User user, @Nullable String oldBanner)
    {
        super(api, responseNumber, user, oldBanner, user.getBannerId(), IDENTIFIER);
    }

    /**
     * The previous banner id
     *
     * @return The previous banner id, null if previously unknown or unset.
     */
    @Nullable
    public String getOldBannerId()
    {
        return getOldValue();
    }

    /**
     * The previous banner url
     *
     * @return The previous banner url, null if previously unknown or unset.
     */
    @Nullable
    public String getOldBannerUrl()
    {
        return previous == null ? null : String.format(User.BANNER_URL, getUser().getId(), previous, previous.startsWith("a_") ? "gif" : "png");
    }

    /**
     * The new banner id
     *
     * @return The new banner id
     */
    @Nullable
    public String getNewBannerId()
    {
        return getNewValue();
    }

    /**
     * The new banner url
     *
     * @return The new banner url
     */
    @Nullable
    public String getNewBannerUrl()
    {
        return next == null ? null : String.format(User.BANNER_URL, getUser().getId(), next, next.startsWith("a_") ? "gif" : "png");
    }
}
