package net.dv8tion.jda.api.events.guild.member.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.Member Member} updated their {@link net.dv8tion.jda.api.entities.Guild Guild} avatar.
 *
 * <p>Can be used to retrieve members who change their per guild avatar, triggering guild, the old avatar id and the new avatar id.
 *
 * <p>Identifier: {@code member_avatar}
 *
 * <h2>Requirements</h2>
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
public class GuildMemberUpdateAvatarEvent extends GenericGuildMemberUpdateEvent<String>
{
    public static final String IDENTIFIER = "member_avatar";

    public GuildMemberUpdateAvatarEvent(@NotNull JDA api, long responseNumber, @NotNull Member member, @Nullable String oldAvatarId)
    {
        super(api, responseNumber, member, oldAvatarId, member.getAvatarId(), IDENTIFIER);
    }

    /**
     * The old avatar id
     *
     * @return The old avatar id
     */
    public String getOldAvatarId()
    {
        return getOldValue();
    }

    /**
     * The new avatar id
     *
     * @return The new avatar id
     */
    public String getNewAvatarId()
    {
        return getNewValue();
    }
}
