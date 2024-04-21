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
package net.dv8tion.jda.api.entities.channel.unions

import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import javax.annotation.Nonnull

/**
 * A union representing all channel types that implement [AudioChannel].
 * <br></br>This class extends [AudioChannel] and primarily acts as a discovery tool for
 * developers to discover some common interfaces that a [AudioChannel] could be cast to.
 *
 * <br></br>This interface represents the follow concrete channel types:
 *
 *  * [VoiceChannel]
 *  * [StageChannel]
 *
 */
interface AudioChannelUnion : AudioChannel {
    /**
     * Casts this union to a [VoiceChannel].
     * <br></br>This method exists for developer discoverability.
     *
     *
     * Note: This is effectively equivalent to using the cast operator:
     * <pre>`//These are the same!
     * VoiceChannel channel = union.asVoiceChannel();
     * VoiceChannel channel2 = (VoiceChannel) union;
    `</pre> *
     *
     * You can use [.getType] to see if the channel is of type [ChannelType.VOICE] to validate
     * whether you can call this method in addition to normal instanceof checks: `channel instanceof VoiceChannel`
     *
     * @throws IllegalStateException
     * If the channel represented by this union is not actually a [VoiceChannel].
     *
     * @return The channel as a [VoiceChannel]
     */
    @Nonnull
    fun asVoiceChannel(): VoiceChannel?

    /**
     * Casts this union to a [StageChannel].
     * <br></br>This method exists for developer discoverability.
     *
     *
     * Note: This is effectively equivalent to using the cast operator:
     * <pre>`//These are the same!
     * StageChannel channel = union.asStageChannel();
     * StageChannel channel2 = (StageChannel) union;
    `</pre> *
     *
     * You can use [.getType] to see if the channel is of type [ChannelType.STAGE] to validate
     * whether you can call this method in addition to normal instanceof checks: `channel instanceof StageChannel`
     *
     * @throws IllegalStateException
     * If the channel represented by this union is not actually a [StageChannel].
     *
     * @return The channel as a [StageChannel]
     */
    @Nonnull
    fun asStageChannel(): StageChannel?

    /**
     * Casts this union to a [GuildMessageChannel].
     * <br></br>This method exists for developer discoverability.
     *
     *
     * Note: This is effectively equivalent to using the cast operator:
     * <pre>`//These are the same!
     * GuildMessageChannel channel = union.asGuildMessageChannel();
     * GuildMessageChannel channel2 = (GuildMessageChannel) union;
    `</pre> *
     *
     * @throws IllegalStateException
     * If the channel represented by this union is not actually a [GuildMessageChannel].
     *
     * @return The channel as a [GuildMessageChannel]
     */
    @Nonnull
    fun asGuildMessageChannel(): GuildMessageChannel?
}
