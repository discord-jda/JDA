package net.dv8tion.jda.api.entities.templates;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.ISnowflake;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * POJO for the roles information provided by a template.
 *
 * @see TemplateGuild#getChannels()
 */
public class TemplateChannel implements ISnowflake
{
    private final long id;
    private final ChannelType channelType;
    private final String name;
    private final String topic;
    private final int rawPosition;
    private final long parentId;

    // text only properties
    private final boolean nsfw;
    private final int slowmode;

    // voice only properties
    private final int bitrate;
    private final int userLimit;

    public TemplateChannel(final long id, final ChannelType channelType, final String name, final String topic, final int rawPosition, final long parentId,
                           final boolean nsfw, final int slowmode, final int bitrate, final int userLimit)
    {
        this.id = id;
        this.channelType = channelType;
        this.name = name;
        this.topic = topic;
        this.rawPosition = rawPosition;
        this.parentId = parentId;

        this.nsfw = nsfw;
        this.slowmode = slowmode;

        this.bitrate = bitrate;
        this.userLimit = userLimit;
    }

    /**
     * The ids of channels are their position as stored by Discord so this will not look like a typical snowflake.
     *
     * @return The id of the channel as stored by Discord
     */
    @Override
    public long getIdLong()
    {
        return this.id;
    }

    /**
     * As the ids of channels are their position, the date of creation cannot be calculated.
     *
     * @return {@code null}
     */
    @Override
    public OffsetDateTime getTimeCreated()
    {
        return null;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.ChannelType ChannelType} for this TemplateChannel
     *
     * @return The channel type
     */
    @Nonnull
    public ChannelType getType()
    {
        return this.channelType;
    }

    /**
     * The human readable name of the  GuildChannel.
     * <br>If no name has been set, this returns null.
     *
     * @return The name of this GuildChannel
     */
    @Nonnull
    public String getName()
    {
        return this.name;
    }

    /**
     * The topic set for this TemplateChannel.
     * <br>If no topic has been set or the {@link net.dv8tion.jda.api.entities.ChannelType ChannelType}
     * <b>is not {@link net.dv8tion.jda.api.entities.ChannelType#TEXT TEXT}</b>, this returns {@code null}.
     *
     * @return Possibly-null String containing the topic of this TemplateChannel.
     */
    @Nullable
    public String getTopic()
    {
        return this.topic;
    }

    /**
     * The actual position of the {@link net.dv8tion.jda.api.entities.templates.TemplateChannel TemplateChannel} as stored and given by Discord.
     * Channel positions are actually based on a pairing of the creation time (as stored in the snowflake id)
     * and the position. If 2 or more channels share the same position then they are sorted based on their creation date.
     * The more recent a channel was created, the lower it is in the hierarchy.
     *
     * @return The true, Discord stored, position of the {@link net.dv8tion.jda.api.entities.templates.TemplateChannel TemplateChannel}.
     */
    public int getPositionRaw()
    {
        return this.rawPosition;
    }

    /**
     * Parent Category id of this TemplateChannel. Channels don't need to have a parent Category.
     * <br>Note that a Category channel will always return {@code -1} for this method
     * as nested categories are not supported.
     *
     * @return The id of the parent Category or {@code -1} if the channel doesn't have a parent Category
     */
    public long getParentId()
    {
        return this.parentId;
    }

    /**
     * Whether or not this channel is considered as "NSFW" (Not-Safe-For-Work).
     * <br>If the {@link net.dv8tion.jda.api.entities.ChannelType ChannelType}
     * <b>is not {@link net.dv8tion.jda.api.entities.ChannelType#TEXT TEXT}</b>, this returns {@code false}.
     *
     * @return Whether this TextChannel is considered NSFW or {@code false} if the channel is not a text channel
     */
    public boolean isNSFW()
    {
        return this.nsfw;
    }

    /**
     * The slowmode set for this TemplateChannel.
     * <br>If slowmode is set this returns an {@code int} between 1 and {@link net.dv8tion.jda.api.entities.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}.
     * <br>If not set this returns {@code 0} or if the {@link net.dv8tion.jda.api.entities.ChannelType ChannelType}
     * <b>is not {@link net.dv8tion.jda.api.entities.ChannelType#TEXT TEXT}</b>, this returns {@code -1}.
     *
     * <p>Note bots are unaffected by this.
     * <br>Having {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE MESSAGE_MANAGE} or
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} permission also
     * grants immunity to slowmode.
     *
     * @return The slowmode for this TextChannel, between 1 and {@link net.dv8tion.jda.api.entities.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}, {@code 0} if no slowmode is set or {@code -1} if the channel is not a text channel
     */
    public int getSlowmode()
    {
        return this.channelType == ChannelType.TEXT ? this.slowmode : -1;
    }

    /**
     * The audio bitrate of the voice audio that is transmitted in this channel. While higher bitrates can be sent to
     * this channel, it will be scaled down by the client.
     * <br>If the {@link net.dv8tion.jda.api.entities.ChannelType ChannelType}
     * <b>is not {@link net.dv8tion.jda.api.entities.ChannelType#VOICE VOICE}</b>, this returns {@code -1}.
     *
     * <br>Default and recommended value is 64000
     *
     * @return The audio bitrate of this voice channel
     */
    public int getBitrate()
    {
        return this.channelType == ChannelType.VOICE ? this.bitrate : -1;
    }
    /**
     * The maximum amount of {@link net.dv8tion.jda.api.entities.Member Members} that can be in this
     * voice channel at once.
     * <br>If the {@link net.dv8tion.jda.api.entities.ChannelType ChannelType}
     * <b>is not {@link net.dv8tion.jda.api.entities.ChannelType#VOICE VOICE}</b>, this returns {@code -1}.
     *
     * <br>0 - No limit
     *
     * @return The maximum amount of members allowed in this channel at once.
     */
    public int getUserLimit()
    {
        return this.channelType == ChannelType.VOICE ? this.userLimit : -1;
    }
}
