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
package net.dv8tion.jda.api.entities.channel.concrete;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IVoiceStatusChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

import javax.annotation.Nonnull;

/**
 * Represents a Discord Voice GuildChannel.
 * <br>Adds additional information specific to voice channels in Discord.
 *
 * @see GuildChannel
 * @see TextChannel
 * @see Category
 *
 * @see   Guild#getVoiceChannelCache()
 * @see   Guild#getVoiceChannels()
 * @see   Guild#getVoiceChannelsByName(String, boolean)
 * @see   Guild#getVoiceChannelById(long)
 *
 * @see   JDA#getVoiceChannelCache()
 * @see   JDA#getVoiceChannels()
 * @see   JDA#getVoiceChannelsByName(String, boolean)
 * @see   JDA#getVoiceChannelById(long)
 */
public interface VoiceChannel extends StandardGuildChannel, GuildMessageChannel, AudioChannel, IWebhookContainer, IAgeRestrictedChannel, ISlowmodeChannel, IVoiceStatusChannel
{
    /**
     * The maximum limit you can set with {@link VoiceChannelManager#setUserLimit(int)}. ({@value})
     */
    int MAX_USERLIMIT = 99;

    @Nonnull
    @Override
    ChannelAction<VoiceChannel> createCopy(@Nonnull Guild guild);

    @Nonnull
    @Override
    default ChannelAction<VoiceChannel> createCopy()
    {
        return createCopy(getGuild());
    }

    @Nonnull
    @Override
    VoiceChannelManager getManager();
}
