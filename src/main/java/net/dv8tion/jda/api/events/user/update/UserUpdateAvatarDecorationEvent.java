package net.dv8tion.jda.api.events.user.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that the {@link net.dv8tion.jda.api.entities.User.AvatarDecoration AvatarDecoration} of a {@link net.dv8tion.jda.api.entities.User User} changed.
 *
 * <p>Can be used to retrieve the User who changed their avatar and their previous Avatar Decoration object.
 *
 * <p>Identifier: {@code avatar_decoration}
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Additionally, this event requires the {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
public class UserUpdateAvatarDecorationEvent extends GenericUserUpdateEvent<User.AvatarDecoration>
{
    public static final String IDENTIFIER = "avatar_decoration";

    public UserUpdateAvatarDecorationEvent(@NotNull JDA api, long responseNumber, @NotNull User user, @Nullable User.AvatarDecoration oldAvatarDecoration)
    {
        super(api, responseNumber, user, oldAvatarDecoration, user.getAvatarDecoration(), IDENTIFIER);
    }

    /**
     * The old avatar decoration
     * @return The old avatar decoration
     */
    @Nullable
    public User.AvatarDecoration getOldAvatarDecoration()
    {
        return getOldValue();
    }

    /**
     * The new avatar decoration
     * @return The new avatar decoration
     */
    @Nullable
    public User.AvatarDecoration getNewAvatarDecoration()
    {
        return getNewValue();
    }

}
