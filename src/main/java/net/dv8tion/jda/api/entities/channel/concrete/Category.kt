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
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.attribute.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.managers.channel.ChannelManager
import net.dv8tion.jda.api.requests.restaction.ChannelAction
import net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction
import net.dv8tion.jda.internal.utils.Helpers
import java.util.stream.Stream
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a channel category in the official Discord API.
 * <br></br>Categories are used to keep order in a Guild by dividing the channels into groups.
 *
 * @see Guild.getCategoryCache
 * @see Guild.getCategories
 * @see Guild.getCategoriesByName
 * @see Guild.getCategoryById
 * @see JDA.getCategoryCache
 * @see JDA.getCategories
 * @see JDA.getCategoriesByName
 * @see JDA.getCategoryById
 */
interface Category : GuildChannel, ICopyableChannel, IPositionableChannel, IPermissionContainer, IMemberContainer {
    @get:Nonnull
    val channels: List<GuildChannel?>
        /**
         * All [Channels][GuildChannel] listed for this Category.
         * <br></br>Includes all types of channels, except for threads.
         *
         * @return Immutable list of all child channels
         */
        get() = getGuild()
            .getChannelCache()
            .ofType(ICategorizableChannel::class.java)
            .applyStream<List<GuildChannel?>> { stream: Stream<ICategorizableChannel> ->
                stream
                    .filter { it: ICategorizableChannel -> this == it.parentCategory }
                    .sorted()
                    .collect(Helpers.toUnmodifiableList())
            }

    @get:Nonnull
    val textChannels: List<TextChannel?>?
        /**
         * All [TextChannels][TextChannel]
         * listed for this Category
         *
         * @return Immutable list of all child TextChannels
         */
        get() = getGuild().getTextChannelCache().applyStream<List<TextChannel?>> { stream: Stream<TextChannel> ->
            stream.filter { channel: TextChannel -> equals(channel.parentCategory) }
                .sorted()
                .collect(Helpers.toUnmodifiableList())
        }

    @get:Nonnull
    val newsChannels: List<NewsChannel?>?
        /**
         * All [NewsChannels][NewsChannel]
         * listed for this Category
         *
         * @return Immutable list of all child NewsChannels
         */
        get() = getGuild().getNewsChannelCache().applyStream<List<NewsChannel?>> { stream: Stream<NewsChannel> ->
            stream.filter { channel: NewsChannel -> equals(channel.parentCategory) }
                .sorted()
                .collect(Helpers.toUnmodifiableList())
        }

    @get:Nonnull
    val forumChannels: List<ForumChannel?>?
        /**
         * All [ForumChannels][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel] listed for this Category
         *
         * @return Immutable list of all child ForumChannels
         */
        get() = getGuild().getForumChannelCache().applyStream<List<ForumChannel?>> { stream: Stream<ForumChannel> ->
            stream.filter { channel: ForumChannel -> equals(channel.parentCategory) }
                .sorted()
                .collect(Helpers.toUnmodifiableList())
        }

    @get:Nonnull
    val mediaChannels: List<MediaChannel?>?
        /**
         * All [MediaChannels][net.dv8tion.jda.api.entities.channel.concrete.MediaChannel] listed for this Category
         *
         * @return Immutable list of all child ForumChannels
         */
        get() = getGuild().getMediaChannelCache().applyStream<List<MediaChannel?>> { stream: Stream<MediaChannel> ->
            stream.filter { channel: MediaChannel -> equals(channel.parentCategory) }
                .sorted()
                .collect(Helpers.toUnmodifiableList())
        }

    @get:Nonnull
    val voiceChannels: List<VoiceChannel?>?
        /**
         * All [VoiceChannels][VoiceChannel]
         * listed for this Category
         *
         * @return Immutable list of all child VoiceChannels
         */
        get() = getGuild().getVoiceChannelCache().applyStream<List<VoiceChannel?>> { stream: Stream<VoiceChannel> ->
            stream.filter { channel: VoiceChannel -> equals(channel.parentCategory) }
                .sorted()
                .collect(Helpers.toUnmodifiableList())
        }

    @get:Nonnull
    val stageChannels: List<StageChannel?>?
        /**
         * All [StageChannel]
         * listed for this Category
         *
         * @return Immutable list of all child StageChannel
         */
        get() = getGuild().getStageChannelCache().applyStream<List<StageChannel?>> { stream: Stream<StageChannel> ->
            stream.filter { channel: StageChannel -> equals(channel.parentCategory) }
                .sorted()
                .collect(Helpers.toUnmodifiableList())
        }

    /**
     * Creates a new [TextChannel] with this Category as parent.
     * For this to be successful, the logged in account has to have the
     * [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission in this Category.
     *
     *
     * This will copy all [PermissionOverrides][net.dv8tion.jda.api.entities.PermissionOverride] of this Category!
     * Unless the bot is unable to sync it with this category due to permission escalation.
     * See [IPermissionHolder.canSync] for details.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The channel could not be created due to a permission discrepancy
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The [VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] permission was removed
     *
     *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
     * <br></br>The maximum number of channels were exceeded
     *
     *
     * @param  name
     * The name of the TextChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
     * @throws IllegalArgumentException
     * If the provided name is `null`, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters
     *
     * @return A specific [ChannelAction]
     * <br></br>This action allows to set fields for the new TextChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    fun createTextChannel(@Nonnull name: String?): ChannelAction<TextChannel?>?

    /**
     * Creates a new [NewsChannel][net.dv8tion.jda.api.entities.channel.concrete.NewsChannel] with this Category as parent.
     * For this to be successful, the logged in account has to have the
     * [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission in this Category.
     *
     *
     * This will copy all [PermissionOverrides][net.dv8tion.jda.api.entities.PermissionOverride] of this Category!
     * Unless the bot is unable to sync it with this category due to permission escalation.
     * See [IPermissionHolder.canSync] for details.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The channel could not be created due to a permission discrepancy
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The [VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] permission was removed
     *
     *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
     * <br></br>The maximum number of channels were exceeded
     *
     *
     * @param  name
     * The name of the NewsChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
     * @throws IllegalArgumentException
     * If the provided name is `null`, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters
     *
     * @return A specific [ChannelAction]
     * <br></br>This action allows to set fields for the new NewsChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    fun createNewsChannel(@Nonnull name: String?): ChannelAction<NewsChannel?>?

    /**
     * Creates a new [VoiceChannel] with this Category as parent.
     * For this to be successful, the logged in account has to have the
     * [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission in this Category.
     *
     *
     * This will copy all [PermissionOverrides][net.dv8tion.jda.api.entities.PermissionOverride] of this Category!
     * Unless the bot is unable to sync it with this category due to permission escalation.
     * See [IPermissionHolder.canSync] for details.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The channel could not be created due to a permission discrepancy
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The [VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] permission was removed
     *
     *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
     * <br></br>The maximum number of channels were exceeded
     *
     *
     * @param  name
     * The name of the VoiceChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
     * @throws IllegalArgumentException
     * If the provided name is `null`, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters
     *
     * @return A specific [ChannelAction]
     * <br></br>This action allows to set fields for the new VoiceChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    fun createVoiceChannel(@Nonnull name: String?): ChannelAction<VoiceChannel?>?

    /**
     * Creates a new [StageChannel] with this Category as parent.
     * For this to be successful, the logged in account has to have the
     * [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission in this Category.
     *
     *
     * This will copy all [PermissionOverrides][net.dv8tion.jda.api.entities.PermissionOverride] of this Category!
     * Unless the bot is unable to sync it with this category due to permission escalation.
     * See [IPermissionHolder.canSync] for details.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The channel could not be created due to a permission discrepancy
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The [VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] permission was removed
     *
     *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
     * <br></br>The maximum number of channels were exceeded
     *
     *
     * @param  name
     * The name of the StageChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
     * @throws IllegalArgumentException
     * If the provided name is `null`, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters
     *
     * @return A specific [ChannelAction]
     * <br></br>This action allows to set fields for the new StageChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    fun createStageChannel(@Nonnull name: String?): ChannelAction<StageChannel?>?

    /**
     * Creates a new [ForumChannel] with this Category as parent.
     * For this to be successful, the logged in account has to have the
     * [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission in this Category.
     *
     *
     * This will copy all [PermissionOverrides][net.dv8tion.jda.api.entities.PermissionOverride] of this Category!
     * Unless the bot is unable to sync it with this category due to permission escalation.
     * See [IPermissionHolder.canSync] for details.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The channel could not be created due to a permission discrepancy
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The [VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] permission was removed
     *
     *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
     * <br></br>The maximum number of channels were exceeded
     *
     *
     * @param  name
     * The name of the ForumChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
     * @throws IllegalArgumentException
     * If the provided name is `null`, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters
     *
     * @return A specific [ChannelAction]
     * <br></br>This action allows to set fields for the new ForumChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    fun createForumChannel(@Nonnull name: String?): ChannelAction<ForumChannel?>?

    /**
     * Creates a new [MediaChannel] with this Category as parent.
     * For this to be successful, the logged in account has to have the
     * [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] Permission in this Category.
     *
     *
     * This will copy all [PermissionOverrides][net.dv8tion.jda.api.entities.PermissionOverride] of this Category!
     * Unless the bot is unable to sync it with this category due to permission escalation.
     * See [IPermissionHolder.canSync] for details.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The channel could not be created due to a permission discrepancy
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The [VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] permission was removed
     *
     *  * [MAX_CHANNELS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_CHANNELS]
     * <br></br>The maximum number of channels were exceeded
     *
     *
     * @param  name
     * The name of the MediaChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the logged in account does not have the [net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission
     * @throws IllegalArgumentException
     * If the provided name is `null`, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters
     *
     * @return A specific [ChannelAction]
     * <br></br>This action allows to set fields for the new MediaChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    fun createMediaChannel(@Nonnull name: String?): ChannelAction<MediaChannel?>?

    /**
     * Modifies the positional order of this Category's nested [TextChannels][.getTextChannels] and [NewsChannels][.getNewsChannels].
     * <br></br>This uses an extension of [ChannelOrderAction]
     * specialized for ordering the nested [TextChannels][TextChannel]
     * and [NewsChannels][NewsChannel] of this [Category].
     * <br></br>Like [ChannelOrderAction], the returned [CategoryOrderAction]
     * can be used to move TextChannels/NewsChannels [up][OrderAction.moveUp],
     * [down][OrderAction.moveDown], or
     * [to][OrderAction.moveTo] a specific position.
     * <br></br>This uses **ascending** order with a 0 based index.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNNKOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>One of the channels has been deleted before the completion of the task.
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The currently logged in account was removed from the Guild.
     *
     *
     * @return A [CategoryOrderAction] for
     * ordering the Category's [TextChannels][TextChannel]
     * and [NewsChannels][NewsChannel].
     */
    @Nonnull
    @CheckReturnValue
    fun modifyTextChannelPositions(): CategoryOrderAction?

    /**
     * Modifies the positional order of this Category's nested [VoiceChannels][.getVoiceChannels] and [StageChannels][.getStageChannels].
     * <br></br>This uses an extension of [ChannelOrderAction]
     * specialized for ordering the nested [VoiceChannels][VoiceChannel]
     * and [StageChannels][StageChannel] of this [Category].
     * <br></br>Like [ChannelOrderAction], the returned [CategoryOrderAction]
     * can be used to move VoiceChannels/StageChannels [up][OrderAction.moveUp],
     * [down][OrderAction.moveDown], or
     * [to][OrderAction.moveTo] a specific position.
     * <br></br>This uses **ascending** order with a 0 based index.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNNKOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>One of the channels has been deleted before the completion of the task.
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The currently logged in account was removed from the Guild.
     *
     *
     * @return A [CategoryOrderAction] for
     * ordering the Category's [VoiceChannels][VoiceChannel]
     * and [StageChannels][StageChannel].
     */
    @Nonnull
    @CheckReturnValue
    fun modifyVoiceChannelPositions(): CategoryOrderAction?

    @get:Nonnull
    override val members: List<Member?>
        get() = channels.stream()
            .filter { obj: GuildChannel? -> IMemberContainer::class.java.isInstance(obj) }
            .map { obj: GuildChannel? -> IMemberContainer::class.java.cast(obj) }
            .map { obj: IMemberContainer -> obj.getMembers() }
            .flatMap { obj: List<Member?>? -> obj!!.stream() }
            .distinct()
            .collect(Helpers.toUnmodifiableList())

    @Nonnull
    override fun createCopy(@Nonnull guild: Guild?): ChannelAction<Category?>?
    @Nonnull
    override fun createCopy(): ChannelAction<Category?>?

    @get:Nonnull
    abstract override val manager: ChannelManager<*, *>?
}
