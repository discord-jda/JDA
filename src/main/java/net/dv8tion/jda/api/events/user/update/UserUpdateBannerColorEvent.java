package net.dv8tion.jda.api.events.user.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

public class UserUpdateBannerColorEvent extends GenericUserUpdateEvent<Color>
{
    public static final String IDENTIFIER = "banner_color";

    public UserUpdateBannerColorEvent(@Nonnull JDA api, long responseNumber, @Nonnull User user, @Nullable Color oldValue)
    {
        super(api, responseNumber, user, oldValue, user.getBannerColor(), IDENTIFIER);
    }

    public Color getOldBannerColor()
    {
        return getOldValue();
    }

    public Color getNewBannerColor()
    {
        return getNewValue();
    }
}
