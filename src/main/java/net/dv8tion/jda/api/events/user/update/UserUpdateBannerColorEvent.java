package net.dv8tion.jda.api.events.user.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 * Indicates that the banner color of a {@link net.dv8tion.jda.api.entities.User User} changed.
 *
 * <p>Can be used to retrieve the user who changed their banner color and their previous banner color.
 *
 * <p>Identifier: {@code banner_color}
 *
 * <h2>Requirements</h2>
 * <p>This event requires that the user be fetched by the {@link net.dv8tion.jda.api.JDA#retrieveUserById(long)} method
 * as the user's profile data is not sent directly. Thus, the old value will be null even if the user has a banner color
 * unless previously fetched.
 */
public class UserUpdateBannerColorEvent extends GenericUserUpdateEvent<Color>
{
    public static final String IDENTIFIER = "banner_color";

    public UserUpdateBannerColorEvent(@Nonnull JDA api, long responseNumber, @Nonnull User user, @Nullable Color oldValue)
    {
        super(api, responseNumber, user, oldValue, user.getBannerColor(), IDENTIFIER);
    }

    /**
     * The old banner color
     *
     * @return The old banner color, null if previously unknown or unset.
     */
    public Color getOldBannerColor()
    {
        return getOldValue();
    }

    /**
     * The new banner color
     *
     * @return The new banner color
     */
    public Color getNewBannerColor()
    {
        return getNewValue();
    }
}
