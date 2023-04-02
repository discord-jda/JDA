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

package net.dv8tion.jda.api.entities.channel.unions;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import javax.annotation.Nonnull;

/**
 * A union representing all channel types that implement {@link AudioChannel}.
 * <br>This class extends {@link AudioChannel} and primarily acts as a discovery tool for
 * developers to discover some common interfaces that a {@link AudioChannel} could be cast to.
 *
 * <br>This interface represents the follow concrete channel types:
 * <ul>
 *     <li>{@link VoiceChannel}</li>
 *     <li>{@link StageChannel}</li>
 * </ul>
 */
public interface AudioChannelUnion extends AudioChannel
{
    /**
     * Casts this union to a {@link VoiceChannel}.
     * <br>This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre>{@code
     * //These are the same!
     * VoiceChannel channel = union.asVoiceChannel();
     * VoiceChannel channel2 = (VoiceChannel) union;
     * }</pre>
     *
     * You can use {@link #getType()} to see if the channel is of type {@link ChannelType#VOICE} to validate
     * whether you can call this method in addition to normal instanceof checks: {@code channel instanceof VoiceChannel}
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link VoiceChannel}.
     *
     * @return The channel as a {@link VoiceChannel}
     */
    @Nonnull
    VoiceChannel asVoiceChannel();

    /**
     * Casts this union to a {@link StageChannel}.
     * <br>This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre>{@code
     * //These are the same!
     * StageChannel channel = union.asStageChannel();
     * StageChannel channel2 = (StageChannel) union;
     * }</pre>
     *
     * You can use {@link #getType()} to see if the channel is of type {@link ChannelType#STAGE} to validate
     * whether you can call this method in addition to normal instanceof checks: {@code channel instanceof StageChannel}
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link StageChannel}.
     *
     * @return The channel as a {@link StageChannel}
     */
    @Nonnull
    StageChannel asStageChannel();

    /**
     * Casts this union to a {@link GuildMessageChannel}.
     * <br>This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre>{@code
     * //These are the same!
     * GuildMessageChannel channel = union.asGuildMessageChannel();
     * GuildMessageChannel channel2 = (GuildMessageChannel) union;
     * }</pre>
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link GuildMessageChannel}.
     *
     * @return The channel as a {@link GuildMessageChannel}
     */
    @Nonnull
    GuildMessageChannel asGuildMessageChannel();
}
