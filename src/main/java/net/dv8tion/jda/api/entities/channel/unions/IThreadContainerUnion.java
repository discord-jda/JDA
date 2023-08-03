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
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.MediaChannel;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;

import javax.annotation.Nonnull;

/**
 * A union representing all channel types that implement {@link net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer}.
 * <br>This class extends {@link net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer} and primarily acts as a discovery tool for
 * developers to discover some common interfaces that a {@link net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer} could be cast to.
 *
 * <br>This interface represents the follow concrete channel types:
 * <ul>
 *     <li>{@link TextChannel}</li>
 *     <li>{@link NewsChannel}</li>
 *     <li>{@link ForumChannel}</li>
 *     <li>{@link MediaChannel}</li>
 * </ul>
 */
public interface IThreadContainerUnion extends IThreadContainer
{
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
     * Casts this union to a {@link GuildMessageChannel}.
     * <br>This works for the following channel types represented by this union:
     * <ul>
     *     <li>{@link TextChannel}</li>
     *     <li>{@link NewsChannel}</li>
     * </ul>
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * <pre><code>
     * //These are the same!
     * GuildMessageChannel channel = union.asGuildMessageChannel();
     * GuildMessageChannel channel2 = (GuildMessageChannel) union;
     * </code></pre>
     *
     * You can use {@link #getType()}{@link ChannelType#isMessage() .isMessage()} to validate
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
     * Casts this union to a {@link StandardGuildChannel}.
     * <br>This works for the following channel types represented by this union:
     * <ul>
     *     <li>{@link TextChannel}</li>
     *     <li>{@link NewsChannel}</li>
     * </ul>
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
     * <br>This works for the following channel types represented by this union:
     * <ul>
     *     <li>{@link TextChannel}</li>
     *     <li>{@link NewsChannel}</li>
     * </ul>
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
