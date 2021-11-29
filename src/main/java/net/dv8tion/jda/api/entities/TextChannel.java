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
package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

import javax.annotation.Nonnull;

/**
 * Represents a Discord Text GuildChannel.
 * <br>Adds additional functionality and information for text channels in Discord.
 *
 * <p>This is a {@link GuildChannel GuildChannel} capable of sending messages.
 *
 * @see GuildChannel
 * @see MessageChannel
 * @see VoiceChannel
 * @see Category
 *
 * @see   Guild#getTextChannelCache()
 * @see   Guild#getTextChannels()
 * @see   Guild#getTextChannelsByName(String, boolean)
 * @see   Guild#getTextChannelById(long)
 *
 * @see   JDA#getTextChannelCache()
 * @see   JDA#getTextChannels()
 * @see   JDA#getTextChannelsByName(String, boolean)
 * @see   JDA#getTextChannelById(long)
 */
public interface TextChannel extends BaseGuildMessageChannel
{
    /**
     * The maximum duration of slowmode in seconds
     */
    int MAX_SLOWMODE = 21600;

    /**
     * The slowmode set for this TextChannel.
     * <br>If slowmode is set this returns an {@code int} between 1 and {@link net.dv8tion.jda.api.entities.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}.
     * <br>If not set this returns {@code 0}.
     *
     * <p>Note bots are unaffected by this.
     * <br>Having {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE MESSAGE_MANAGE} or
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} permission also
     * grants immunity to slowmode.
     *
     * @return The slowmode for this TextChannel, between 1 and {@link net.dv8tion.jda.api.entities.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}, or {@code 0} if no slowmode is set.
     */
    int getSlowmode();

    @Nonnull
    @Override
    ChannelAction<TextChannel> createCopy(@Nonnull Guild guild);

    @Nonnull
    @Override
    default ChannelAction<TextChannel> createCopy()
    {
        return createCopy(getGuild());
    }

    @Nonnull
    @Override
    TextChannelManager getManager();
}
