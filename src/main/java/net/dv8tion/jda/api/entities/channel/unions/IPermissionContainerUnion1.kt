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
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer
import net.dv8tion.jda.api.entities.channel.concrete.*
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel
import javax.annotation.Nonnull

/**
 * A union representing all channel types that implement [net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer].
 * <br></br>This class extends [net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer] and primarily acts as a discovery tool for
 * developers to discover some common interfaces that a [net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer] could be cast to.
 *
 * <br></br>This interface represents the follow concrete channel types:
 *
 *  * [TextChannel]
 *  * [NewsChannel]
 *  * [VoiceChannel]
 *  * [StageChannel]
 *  * [ForumChannel]
 *  * [Category]
 *
 */
interface IPermissionContainerUnion : IPermissionContainer {
    /**
     * Casts this union to a [TextChannel].
     * This method exists for developer discoverability.
     *
     *
     * Note: This is effectively equivalent to using the cast operator:
     * <pre>`
     * //These are the same!
     * TextChannel channel = union.asTextChannel();
     * TextChannel channel2 = (TextChannel) union;
    `</pre> *
     *
     * You can use [.getType] to see if the channel is of type [ChannelType.TEXT] to validate
     * whether you can call this method in addition to normal instanceof checks: `channel instanceof TextChannel`
     *
     * @throws IllegalStateException
     * If the channel represented by this union is not actually a [TextChannel].
     *
     * @return The channel as a [TextChannel]
     */
    @Nonnull
    fun asTextChannel(): TextChannel?

    /**
     * Casts this union to a [NewsChannel].
     * This method exists for developer discoverability.
     *
     *
     * Note: This is effectively equivalent to using the cast operator:
     * <pre>`
     * //These are the same!
     * NewsChannel channel = union.asNewsChannel();
     * NewsChannel channel2 = (NewsChannel) union;
    `</pre> *
     *
     * You can use [.getType] to see if the channel is of type [ChannelType.NEWS] to validate
     * whether you can call this method in addition to normal instanceof checks: `channel instanceof NewsChannel`
     *
     * @throws IllegalStateException
     * If the channel represented by this union is not actually a [NewsChannel].
     *
     * @return The channel as a [NewsChannel]
     */
    @Nonnull
    fun asNewsChannel(): NewsChannel?

    /**
     * Casts this union to a [VoiceChannel].
     * This method exists for developer discoverability.
     *
     *
     * Note: This is effectively equivalent to using the cast operator:
     * <pre>`
     * //These are the same!
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
     * This method exists for developer discoverability.
     *
     *
     * Note: This is effectively equivalent to using the cast operator:
     * <pre>`
     * //These are the same!
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
     * Casts this union to a [Category].
     * This method exists for developer discoverability.
     *
     *
     * Note: This is effectively equivalent to using the cast operator:
     * <pre>`
     * //These are the same!
     * Category channel = union.asCategory();
     * Category channel2 = (Category) union;
    `</pre> *
     *
     * You can use [.getType] to see if the channel is of type [ChannelType.CATEGORY] to validate
     * whether you can call this method in addition to normal instanceof checks: `channel instanceof Category`
     *
     * @throws IllegalStateException
     * If the channel represented by this union is not actually a [Category].
     *
     * @return The channel as a [Category]
     */
    @Nonnull
    fun asCategory(): Category?

    /**
     * Casts this union to a [ForumChannel].
     * This method exists for developer discoverability.
     *
     *
     * Note: This is effectively equivalent to using the cast operator:
     * <pre>`
     * //These are the same!
     * ForumChannel channel = union.asForumChannel();
     * ForumChannel channel2 = (ForumChannel) union;
    `</pre> *
     *
     * You can use [.getType] to see if the channel is of type [ChannelType.FORUM] to validate
     * whether you can call this method in addition to normal instanceof checks: `channel instanceof ForumChannel`
     *
     * @throws IllegalStateException
     * If the channel represented by this union is not actually a [ForumChannel].
     *
     * @return The channel as a [ForumChannel]
     */
    @Nonnull
    fun asForumChannel(): ForumChannel?

    /**
     * Casts this union to a [GuildMessageChannel].
     * This method exists for developer discoverability.
     *
     *
     * Note: This is effectively equivalent to using the cast operator:
     * <pre>`
     * //These are the same!
     * GuildMessageChannel channel = union.asGuildMessageChannel();
     * GuildMessageChannel channel2 = (GuildMessageChannel) union;
    `</pre> *
     *
     * You can use [.getType][.isGuild()][ChannelType.isGuild]
     * and [.getType][.isMessage()][ChannelType.isMessage] to validate whether you can call this
     * method in addition to normal instanceof checks: `channel instanceof GuildMessageChannel`
     *
     * @throws IllegalStateException
     * If the channel represented by this union is not actually a [GuildMessageChannel].
     *
     * @return The channel as a [GuildMessageChannel]
     */
    @Nonnull
    fun asGuildMessageChannel(): GuildMessageChannel?

    /**
     * Casts this union to a [AudioChannel].
     * This method exists for developer discoverability.
     *
     *
     * Note: This is effectively equivalent to using the cast operator:
     * <pre>`
     * //These are the same!
     * AudioChannel channel = union.asAudioChannel();
     * AudioChannel channel2 = (AudioChannel) union;
    `</pre> *
     *
     * You can use [.getType][.isAudio()][ChannelType.isAudio] to validate
     * whether you can call this method in addition to normal instanceof checks: `channel instanceof AudioChannel`
     *
     * @throws IllegalStateException
     * If the channel represented by this union is not actually a [AudioChannel].
     *
     * @return The channel as a [AudioChannel]
     */
    @Nonnull
    fun asAudioChannel(): AudioChannel?

    /**
     * Casts this union to a [net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer].
     * This method exists for developer discoverability.
     *
     *
     * Note: This is effectively equivalent to using the cast operator:
     * <pre>`
     * //These are the same!
     * IThreadContainer channel = union.asThreadContainer();
     * IThreadContainer channel2 = (IThreadContainer) union;
    `</pre> *
     *
     * You can use `channel instanceof IThreadContainer` to validate whether you can call this method.
     *
     * @throws IllegalStateException
     * If the channel represented by this union is not actually a [net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer].
     *
     * @return The channel as a [net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer]
     */
    fun asThreadContainer(): IThreadContainer?

    /**
     * Casts this union to a [StandardGuildChannel].
     * This method exists for developer discoverability.
     *
     *
     * Note: This is effectively equivalent to using the cast operator:
     * <pre>`
     * //These are the same!
     * StandardGuildChannel channel = union.asStandardGuildChannel();
     * StandardGuildChannel channel2 = (StandardGuildChannel) union;
    `</pre> *
     *
     * @throws IllegalStateException
     * If the channel represented by this union is not actually a [StandardGuildChannel].
     *
     * @return The channel as a [StandardGuildChannel]
     */
    @Nonnull
    fun asStandardGuildChannel(): StandardGuildChannel?

    /**
     * Casts this union to a [StandardGuildMessageChannel].
     * This method exists for developer discoverability.
     *
     *
     * Note: This is effectively equivalent to using the cast operator:
     * <pre>`
     * //These are the same!
     * StandardGuildMessageChannel channel = union.asStandardGuildMessageChannel();
     * StandardGuildMessageChannel channel2 = (StandardGuildMessageChannel) union;
    `</pre> *
     *
     * @throws IllegalStateException
     * If the channel represented by this union is not actually a [StandardGuildMessageChannel].
     *
     * @return The channel as a [StandardGuildMessageChannel]
     */
    @Nonnull
    fun asStandardGuildMessageChannel(): StandardGuildMessageChannel?
}
