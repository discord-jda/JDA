package net.dv8tion.jda.api.events.guild.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the safety alert channel of a {@link Guild Guild} changed.
 *
 * <p>Can be used to detect when a guild safety alert changes and retrieve the old one
 *
 * <p>Identifier: {@code safety_alerts_channel}
 */
public class GuildUpdateSafetyAlertsChannelEvent extends GenericGuildUpdateEvent<TextChannel>
{
    public static final String IDENTIFIER = "safety_alerts_channel";

    public GuildUpdateSafetyAlertsChannelEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nullable TextChannel oldSafetyAlertsChannel)
    {
        super(api, responseNumber, guild, oldSafetyAlertsChannel, guild.getSafetyAlertsChannel(), IDENTIFIER);
    }

    /**
     * The previous safety alert channel.
     *
     * @return The previous safety alert channel
     */
    @Nullable
    public TextChannel getOldSafetyAlertsChannel()
    {
        return getOldValue();
    }

    /**
     * The new safety alert channel.
     *
     * @return The new safety alert channel
     */
    @Nullable
    public TextChannel getNewSafetyAlertsChannel()
    {
        return getNewValue();
    }
}
