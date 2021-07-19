package net.dv8tion.jda.api.events.user.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 * Indicates that the accent color of a {@link net.dv8tion.jda.api.entities.User User} changed.
 *
 * <p>Can be used to retrieve the user who changed their accent color and their previous accent color.
 *
 * <p>Identifier: {@code accent_color}
 *
 * <h2>Requirements</h2>
 * <p>This event requires that the user be fetched by the {@link net.dv8tion.jda.api.JDA#retrieveUserById(long)} method
 * as the user's profile data is not sent directly. Thus, the old value will be null even if the user has an accent color
 * unless previously fetched.
 */
public class UserUpdateAccentColorEvent extends GenericUserUpdateEvent<Color>
{
    public static final String IDENTIFIER = "accent_color";

    public UserUpdateAccentColorEvent(@Nonnull JDA api, long responseNumber, @Nonnull User user, @Nullable Color oldValue)
    {
        super(api, responseNumber, user, oldValue, user.getAccentColor(), IDENTIFIER);
    }

    /**
     * The old banner color
     *
     * @return The old banner color, null if previously unknown or unset.
     */
    public Color getOldAccentColor()
    {
        return getOldValue();
    }

    /**
     * The new banner color
     *
     * @return The new banner color
     */
    public Color getNewAccentColor()
    {
        return getNewValue();
    }
}
