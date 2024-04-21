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
package net.dv8tion.jda.api.entities.channel.concrete

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel
import net.dv8tion.jda.api.managers.channel.ChannelManager
import net.dv8tion.jda.api.requests.restaction.ChannelAction
import javax.annotation.Nonnull

/**
 * Represents a Discord Text GuildChannel.
 * <br></br>Adds additional functionality and information for text channels in Discord,
 * on top of the common functionality present in other guild message channels.
 *
 *
 * This is a [GuildChannel][net.dv8tion.jda.api.entities.channel.middleman.GuildChannel] capable of sending messages.
 *
 * @see net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
 *
 * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
 *
 * @see net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel
 *
 *
 * @see Guild.getTextChannelCache
 * @see Guild.getTextChannels
 * @see Guild.getTextChannelsByName
 * @see Guild.getTextChannelById
 * @see JDA.getTextChannelCache
 * @see JDA.getTextChannels
 * @see JDA.getTextChannelsByName
 * @see JDA.getTextChannelById
 */
interface TextChannel : StandardGuildMessageChannel, ISlowmodeChannel {
    @Nonnull
    override fun createCopy(@Nonnull guild: Guild?): ChannelAction<TextChannel?>?
    @Nonnull
    override fun createCopy(): ChannelAction<TextChannel?>? {
        return createCopy(getGuild())
    }

    @get:Nonnull
    abstract override val manager: ChannelManager<*, *>?
}
