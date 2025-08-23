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

import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.*;

import javax.annotation.Nonnull;

/**
 * A union representing all channel types that implement {@link Channel}.
 * <br>This class extends {@link Channel} and primarily acts as a discovery tool for
 * developers to discover some common interfaces that a {@link Channel} could be cast to.
 *
 * <br>This interface represents the follow concrete channel types:
 * <ul>
 *     <li>{@link PrivateChannel}</li>
 *     <li>{@link GroupChannel}</li>
 *     <li>{@link TextChannel}</li>
 *     <li>{@link NewsChannel}</li>
 *     <li>{@link ThreadChannel}</li>
 *     <li>{@link VoiceChannel}</li>
 *     <li>{@link StageChannel}</li>
 *     <li>{@link ForumChannel}</li>
 *     <li>{@link MediaChannel}</li>
 *     <li>{@link Category}</li>
 * </ul>
 */
public interface ChannelUnion extends Channel
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
     * Casts this union to a {@link ForumChannel}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * ForumChannel channel = union.asForumChannel();
     * ForumChannel channel2 = (ForumChannel) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the channel is of type {@link ChannelType#FORUM} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>channel instanceof ForumChannel</code>
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link ForumChannel}.
     *
     * @return The channel as a {@link ForumChannel}
     */
    @Nonnull
    ForumChannel asForumChannel();

    /**
     * Casts this union to a {@link MediaChannel}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * MediaChannel channel = union.asMediaChannel();
     * MediaChannel channel2 = (MediaChannel) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the channel is of type {@link ChannelType#MEDIA} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>channel instanceof MediaChannel</code>
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link MediaChannel}.
     *
     * @return The channel as a {@link MediaChannel}
     */
    @Nonnull
    MediaChannel asMediaChannel();

    /**
     * Casts this union to a {@link Category}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * Category channel = union.asCategory();
     * Category channel2 = (Category) union;
     * </code></pre>
     *
     * You can use {@link #getType()} to see if the channel is of type {@link ChannelType#CATEGORY} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>channel instanceof Category</code>
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link Category}.
     *
     * @return The channel as a {@link Category}
     */
    @Nonnull
    Category asCategory();

    /**
     * Casts this union to a {@link MessageChannel}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * MessageChannel channel = union.asMessageChannel();
     * MessageChannel channel2 = (MessageChannel) union;
     * </code></pre>
     *
     * You can use {@link #getType()}{@link ChannelType#isMessage() .isMessage()} to validate whether you can call this
     * method in addition to normal instanceof checks: <code>channel instanceof MessageChannel</code>
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link MessageChannel}.
     *
     * @return The channel as a {@link MessageChannel}
     */
    @Nonnull
    MessageChannel asMessageChannel();

    /**
     * Casts this union to a {@link GuildChannel}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * GuildChannel channel = union.asGuildChannel();
     * GuildChannel channel2 = (GuildChannel) union;
     * </code></pre>
     *
     * You can use {@link #getType()}{@link ChannelType#isGuild() isGuild()} to validate whether you can call this
     * method in addition to normal instanceof checks: <code>channel instanceof GuildChannel</code>
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link GuildChannel}.
     *
     * @return The channel as a {@link GuildChannel}
     */
    @Nonnull
    GuildChannel asGuildChannel();

    /**
     * Casts this union to a {@link GuildMessageChannel}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * GuildMessageChannel channel = union.asGuildMessageChannel();
     * GuildMessageChannel channel2 = (GuildMessageChannel) union;
     * </code></pre>
     *
     * You can use {@link #getType()}{@link ChannelType#isGuild() .isGuild()}
     * and {@link #getType()}{@link ChannelType#isMessage() .isMessage()} to validate whether you can call this
     * method in addition to normal instanceof checks: <code>channel instanceof GuildMessageChannel</code>
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
     * You can use {@link #getType()}{@link ChannelType#isAudio() .isAudio()} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>channel instanceof AudioChannel</code>
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link AudioChannel}.
     *
     * @return The channel as a {@link AudioChannel}
     */
    @Nonnull
    AudioChannel asAudioChannel();

    /**
     * Casts this union to a {@link IThreadContainer}.
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
     *         If the channel represented by this union is not actually a {@link IThreadContainer}.
     *
     * @return The channel as a {@link IThreadContainer}
     */
    IThreadContainer asThreadContainer();

    /**
     * Casts this union to a {@link StandardGuildChannel}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * StandardGuildChannel channel = union.asStandardGuildChannel();
     * StandardGuildChannel channel2 = (StandardGuildChannel) union;
     * </code></pre>
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link StandardGuildChannel}.
     *
     * @return The channel as a {@link StandardGuildChannel}
     */
    @Nonnull
    StandardGuildChannel asStandardGuildChannel();

    /**
     * Casts this union to a {@link StandardGuildMessageChannel}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * StandardGuildMessageChannel channel = union.asStandardGuildMessageChannel();
     * StandardGuildMessageChannel channel2 = (StandardGuildMessageChannel) union;
     * </code></pre>
     *
     * @throws IllegalStateException
     *         If the channel represented by this union is not actually a {@link StandardGuildMessageChannel}.
     *
     * @return The channel as a {@link StandardGuildMessageChannel}
     */
    @Nonnull
    StandardGuildMessageChannel asStandardGuildMessageChannel();
}
