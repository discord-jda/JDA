package net.dv8tion.jda.api.events.channel.voice.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link VoiceChannel VoiceChannel}'s region changed.
 *
 * <p>Can be used to get affected VoiceChannel, affected Guild and previous region.
 *
 * <p>Identifier: {@code rtc_region}
 */
public class VoiceChannelUpdateRegionEvent extends GenericVoiceChannelUpdateEvent<String>
{
    public static final String IDENTIFIER = "rtc_region";

    public VoiceChannelUpdateRegionEvent(@NotNull JDA api, long responseNumber, @NotNull VoiceChannel channel, @Nullable String oldRegion)
    {
        super(api, responseNumber, channel, oldRegion, channel.getRegionRaw(), IDENTIFIER);
    }

    /**
     * The old {@link Region}
     *
     * @return The old region
     */
    @Nonnull
    public Region getOldRegion()
    {
        return getOldValue() == null ? Region.AUTOMATIC : Region.fromKey(getOldValue());
    }

    /**
     * The new {@link Region}
     *
     * @return The new region
     */
    @Nonnull
    public Region getNewRegion()
    {
        return getNewValue() == null ? Region.AUTOMATIC : Region.fromKey(getNewValue());
    }

    /**
     * The old raw region String
     *
     * @return The old raw region String
     */
    @Nullable
    public String getOldRegionRaw()
    {
        return getOldValue();
    }

    /**
     * The new raw region String
     *
     * @return The new raw region String
     */
    @Nullable
    public String getNewRegionRaw()
    {
        return getNewValue();
    }
}
