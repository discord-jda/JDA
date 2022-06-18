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

import net.dv8tion.jda.api.entities.*;

import javax.annotation.Nonnull;

/**
 * A specialized union representing all channel types that can be used for the "default" channel for
 * {@link Guild#getDefaultChannel()} or {@link Member#getDefaultChannel()}.
 * <br>This is the channel that the Discord client will default to opening when a Guild is opened for the first time
 * when accepting an invite that is not directed at a specific {@link IInviteContainer channel}.
 *
 * <br>This class extends {@link StandardGuildChannel} and primarily acts as a discovery tool for
 * developers to understand which channels might be returned as default channels.
 *
 * <br><br><b>Not all {@link StandardGuildChannel} channels can be used as a default channel!</b>
 *
 * <br><br>This interface represents the follow concrete channel types:
 * <ul>
 *     <li>{@link TextChannel}</li>
 *     <li>{@link NewsChannel}</li>
 * </ul>
 */
public interface DefaultGuildChannelUnion extends StandardGuildChannel
{
    /**
     * Casts this union to a {@link TextChannel}.
     * This method exists for developer discoverability.
     *
     * Note: This is effectively equivalent to using the cast operator:
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
     * Note: This is effectively equivalent to using the cast operator:
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

    //TODO: add asForumChannel() (I think ForumChannels can be the default?)

    /**
     * Casts this union to a {@link IThreadContainer}.
     * <br>This only works for the following channel types represented by this union:
     * <ul>
     *     <li>{@link TextChannel}</li>
     *     <li>{@link NewsChannel}</li>
     * </ul>
     *
     * Note: This is effectively equivalent to using the cast operator:
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
     * Casts this union to a {@link StandardGuildMessageChannel}.
     * <br>This only works for the following channel types represented by this union:
     * <ul>
     *     <li>{@link TextChannel}</li>
     *     <li>{@link NewsChannel}</li>
     * </ul>
     *
     * Note: This is effectively equivalent to using the cast operator:
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
