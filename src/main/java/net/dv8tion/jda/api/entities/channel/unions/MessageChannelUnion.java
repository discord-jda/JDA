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
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import javax.annotation.Nonnull;

/**
 * A union representing all channel types that implement {@link MessageChannel}.
 * <br>This class extends {@link MessageChannel} and primarily acts as a discovery tool for
 * developers to discover some common interfaces that a {@link MessageChannel} could be cast to.
 *
 * <br>This interface represents the follow concrete channel types:
 * <ul>
 *     <li>{@link TextChannel}</li>
 *     <li>{@link NewsChannel}</li>
 *     <li>{@link VoiceChannel}</li>
 *     <li>{@link StageChannel}</li>
 *     <li>{@link ThreadChannel}</li>
 *     <li>{@link PrivateChannel}</li>
 *     <li>{@link GroupChannel}</li>
 * </ul>
 */
public interface MessageChannelUnion extends MessageChannel
{
    /**
     * Casts this union to a {@link PrivateChannel}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * PrivateChannel channel = union.asPrivateChannel();
     * PrivateChannel channel2 = (PrivateChannel) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the channel is of type {@link ChannelType#PRIVATE} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>channel instanceof PrivateChannel</code>
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link PrivateChannel}.
     *
     * @return The channel as a {@link PrivateChannel}
     */
    @Nonnull
    PrivateChannel asPrivateChannel();

    /**
     * Casts this union to a {@link GroupChannel}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * GroupChannel channel = union.asGroupChannel();
     * GroupChannel channel2 = (GroupChannel) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the channel is of type {@link ChannelType#GROUP} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>channel instanceof GroupChannel</code>
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link GroupChannel}.
     *
     * @return The channel as a {@link GroupChannel}
     */
    @Nonnull
    GroupChannel asGroupChannel();

    /**
     * Casts this union to a {@link TextChannel}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * TextChannel channel = union.asTextChannel();
     * TextChannel channel2 = (TextChannel) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the channel is of type {@link ChannelType#TEXT} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>channel instanceof TextChannel</code>
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link TextChannel}.
     *
     * @return The channel as a {@link TextChannel}
     */
    @Nonnull
    TextChannel asTextChannel();

    /**
     * Casts this union to a {@link NewsChannel}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * NewsChannel channel = union.asNewsChannel();
     * NewsChannel channel2 = (NewsChannel) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the channel is of type {@link ChannelType#NEWS} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>channel instanceof NewsChannel</code>
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link NewsChannel}.
     *
     * @return The channel as a {@link NewsChannel}
     */
    @Nonnull
    NewsChannel asNewsChannel();

    /**
     * Casts this union to a {@link ThreadChannel}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * ThreadChannel channel = union.asThreadChannel();
     * ThreadChannel channel2 = (ThreadChannel) union;
     * </code></pre>
     *
     * You can use {@link #getType()}{@link ChannelType#isThread() .isThread()} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>channel instanceof ThreadChannel</code>
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link ThreadChannel}.
     *
     * @return The channel as a {@link ThreadChannel}
     */
    @Nonnull
    ThreadChannel asThreadChannel();

    /**
     * Casts this union to a {@link VoiceChannel}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * VoiceChannel channel = union.asVoiceChannel();
     * VoiceChannel channel2 = (VoiceChannel) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the channel is of type {@link ChannelType#VOICE} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>channel instanceof VoiceChannel</code>
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
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * StageChannel channel = union.asStageChannel();
     * StageChannel channel2 = (StageChannel) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the channel is of type {@link ChannelType#STAGE} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>channel instanceof StageChannel</code>
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link StageChannel}.
     *
     * @return The channel as a {@link StageChannel}
     */
    @Nonnull
    StageChannel asStageChannel();

    /**
     * Casts this union to a {@link net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * IThreadContainer channel = union.asThreadContainer();
     * IThreadContainer channel2 = (IThreadContainer) union;
     * </code></pre>
     *
     * You can use <code>channel instanceof IThreadContainer</code> to validate whether you can call this method.
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer}.
     *
     * @return The channel as a {@link net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer}
     */
    @Nonnull
    IThreadContainer asThreadContainer();

    /**
     * Casts this union to a {@link GuildMessageChannel}.
     * <br>This works for the following channel types represented by this union:
     * <ul>
     *     <li>{@link TextChannel}</li>
     *     <li>{@link NewsChannel}</li>
     *     <li>{@link ThreadChannel}</li>
     * </ul>
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * GuildMessageChannel channel = union.asGuildMessageChannel();
     * GuildMessageChannel channel2 = (GuildMessageChannel) union;
     * </code></pre>
     *
     * You can use {@link #getType()}{@link ChannelType#isGuild() .isGuild()} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>channel instanceof GuildMessageChannel</code>
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link GuildMessageChannel}.
     *
     * @return The channel as a {@link GuildMessageChannel}
     */
    @Nonnull
    GuildMessageChannel asGuildMessageChannel();

    /**
     * Casts this union to a {@link AudioChannel}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * AudioChannel channel = union.asAudioChannel();
     * AudioChannel channel2 = (AudioChannel) union;
     * </code></pre>
     *
     * You can use <code>channel instanceof AudioChannel</code> to validate whether you can call this method.
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link AudioChannel}.
     *
     * @return The channel as a {@link AudioChannel}
     */
    @Nonnull
    AudioChannel asAudioChannel();
}
