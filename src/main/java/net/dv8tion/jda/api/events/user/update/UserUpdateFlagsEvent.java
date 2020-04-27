package net.dv8tion.jda.api.events.user.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Indicates that the {@link net.dv8tion.jda.api.entities.User.UserFlag UserFlags} of a {@link net.dv8tion.jda.api.entities.User User} changed.
 * 
 * <p>Can be used to retrieve the User who got their flags changed and their previous flags.
 * 
 * <p>Identifier: {@code public_flags}
 *
 * <h2>Requirements</h2>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Additionally, this event also requires the {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
public class UserUpdateFlagsEvent extends GenericUserUpdateEvent<Integer>
{
    public static final String IDENTIFIER = "public_flags";
    
    public UserUpdateFlagsEvent(@Nonnull JDA api, long responseNumber, @Nonnull User user, int oldFlags)
    {
        super(api, responseNumber, user, oldFlags, User.UserFlag.getRaw(user.getFlags()), IDENTIFIER);
    }

    /**
     * @return The old {@code Integer} representation of the {@link net.dv8tion.jda.api.entities.User.UserFlag UserFlags}.
     */
    @Nullable
    public Integer getOldFlags()
    {
        return getOldValue();
    }

    /**
     * @return Possibly-null EnumSet of previous {@link net.dv8tion.jda.api.entities.User.UserFlag UserFlags}.
     */
    @Nullable
    public EnumSet<User.UserFlag> getOldFlagSet(){
        return previous == null ? null : User.UserFlag.getFlags(previous);
    }

    /**
     * @return The new {@code Integer} representation of the {@link net.dv8tion.jda.api.entities.User.UserFlag UserFlags}.
     */
    @Nullable
    public Integer getNewFlags(){
        return getNewValue();
    }

    /**
     * @return Possibly-null EnumSet of the new {@link net.dv8tion.jda.api.entities.User.UserFlag UserFlags}.
     */
    @Nullable
    public EnumSet<User.UserFlag> getNewFlagSet(){
        return next == null ? null : User.UserFlag.getFlags(next);
    }
}
