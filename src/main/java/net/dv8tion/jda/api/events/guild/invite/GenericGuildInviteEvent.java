/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api.events.guild.invite;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;

import javax.annotation.Nonnull;

/**
 * Indicates that an {@link Invite} was created or deleted in a {@link Guild}.
 * <br>Every GuildInviteEvent is derived from this event and can be casted.
 *
 * <p>Can be used to detect any GuildInviteEvent.
 *
 * <h2>Requirements</h2>
 *
 * <p>These events require the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_INVITES GUILD_INVITES} intent to be enabled.
 * <br>These events will only fire for invite events that occur in channels where you can {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL}.
 */
public class GenericGuildInviteEvent extends GenericGuildEvent
{
    private final String code;
    private final GuildChannel channel;

    public GenericGuildInviteEvent(@Nonnull JDA api, long responseNumber, @Nonnull String code, @Nonnull GuildChannel channel)
    {
        super(api, responseNumber, channel.getGuild());
        this.code = code;
        this.channel = channel;
    }

    /**
     * The invite code.
     * <br>This can be converted to a url with {@code discord.gg/<code>}.
     *
     * @return The invite code
     */
    @Nonnull
    public String getCode()
    {
        return code;
    }

    /**
     * The invite url.
     * <br>This uses the {@code https://discord.gg/<code>} format.
     *
     * @return The invite url
     */
    @Nonnull
    public String getUrl()
    {
        return "https://discord.gg/" + code;
    }

    /**
     * The {@link GuildChannel} this invite points to.
     *
     * @return {@link GuildChannel}
     */
    @Nonnull
    public GuildChannel getChannel()
    {
        return channel;
    }

    /**
     * The {@link ChannelType} for of the {@link #getChannel() channel} this invite points to.
     *
     * @return {@link ChannelType}
     */
    @Nonnull
    public ChannelType getChannelType()
    {
        return channel.getType();
    }

    /**
     * The {@link TextChannel} this invite points to.
     *
     * @throws IllegalStateException
     *         If this did not happen in a channel of type {@link ChannelType#TEXT ChannelType.TEXT}
     *
     * @return {@link TextChannel}
     *
     * @see    #getChannel()
     * @see    #getChannelType()
     */
    @Nonnull
    public TextChannel getTextChannel()
    {
        if (getChannelType() != ChannelType.TEXT)
            throw new IllegalStateException("The channel is not of type TEXT");
        return (TextChannel) getChannel();
    }

    /**
     * The {@link NewsChannel} this invite points to.
     *
     * @throws IllegalStateException
     *         If this did not happen in a channel of type {@link ChannelType#NEWS ChannelType.NEWS}
     *
     * @return {@link NewsChannel}
     *
     * @see    #getChannel()
     * @see    #getChannelType()
     */
    @Nonnull
    public NewsChannel getNewsChannel()
    {
        if (getChannelType() != ChannelType.NEWS)
            throw new IllegalStateException("The channel is not of type NEWS");
        return (NewsChannel) getChannel();
    }

    /**
     * The {@link VoiceChannel} this invite points to.
     *
     * @throws IllegalStateException
     *         If this did not happen in a voice channel or stage channel
     *
     * @return {@link VoiceChannel}
     *
     * @see    #getChannel()
     * @see    #getChannelType()
     */
    @Nonnull
    public VoiceChannel getVoiceChannel()
    {
        if (!(channel instanceof VoiceChannel))
            throw new IllegalStateException("The channel is not of type VOICE or STAGE");
        return (VoiceChannel) getChannel();
    }

    /**
     * The {@link StageChannel} this invite points to.
     *
     * @throws IllegalStateException
     *         If this did not happen in a channel of type {@link ChannelType#STAGE ChannelType.STAGE}
     *
     * @return {@link StageChannel}
     *
     * @see    #getChannel()
     * @see    #getChannelType()
     */
    @Nonnull
    public StageChannel getStageChannel()
    {
        if (getChannelType() != ChannelType.STAGE)
            throw new IllegalStateException("The channel is not of type STAGE");
        return (StageChannel) getChannel();
    }

    /**
     * The {@link Category} this invite points to.
     *
     * @throws IllegalStateException
     *         If this did not happen in a channel of type {@link ChannelType#CATEGORY ChannelType.CATEGORY}
     *
     * @return {@link Category}
     *
     * @see    #getChannel()
     * @see    #getChannelType()
     */
    @Nonnull
    public Category getCategory()
    {
        if (getChannelType() != ChannelType.CATEGORY)
            throw new IllegalStateException("The channel is not of type CATEGORY");
        return (Category) getChannel();
    }
}
