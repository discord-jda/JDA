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
package net.dv8tion.jda.api.requests.restaction

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.Region
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.forums.BaseForumTag
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.internal.utils.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Extension of [RestAction][net.dv8tion.jda.api.requests.RestAction] specifically
 * designed to create a [GuildChannel].
 * This extension allows setting properties before executing the action.
 *
 * @since  3.0
 *
 * @see net.dv8tion.jda.api.entities.Guild
 *
 * @see net.dv8tion.jda.api.entities.Guild.createTextChannel
 * @see net.dv8tion.jda.api.entities.Guild.createNewsChannel
 * @see net.dv8tion.jda.api.entities.Guild.createVoiceChannel
 * @see net.dv8tion.jda.api.entities.Guild.createStageChannel
 * @see net.dv8tion.jda.api.entities.Guild.createCategory
 * @see net.dv8tion.jda.api.entities.channel.attribute.ICopyableChannel.createCopy
 * @see net.dv8tion.jda.api.entities.channel.attribute.ICopyableChannel.createCopy
 * @param <T>
 * The type of channel to create
</T> */
interface ChannelAction<T : GuildChannel?> : FluentAuditableRestAction<T, ChannelAction<T>?> {
    @get:Nonnull
    val guild: Guild?

    @get:Nonnull
    val type: ChannelType?

    /**
     * Sets the name for the new GuildChannel
     *
     * @param  name
     * The not-null name for the new GuildChannel (1-{@value Channel#MAX_NAME_LENGTH} characters long)
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided name is null or not between 1-{@value Channel#MAX_NAME_LENGTH} characters long
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setName(@Nonnull name: String?): ChannelAction<T>?

    /**
     * Sets the [Category] for the new GuildChannel.
     *
     *
     * You can use [.syncPermissionOverrides] to sync the channel with the category.
     *
     * @param  category
     * The parent for the new GuildChannel
     *
     * @throws UnsupportedOperationException
     * If this ChannelAction is for a Category
     * @throws IllegalArgumentException
     * If the provided category is `null`
     * or not from this Guild
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see .syncPermissionOverrides
     */
    @Nonnull
    @CheckReturnValue
    fun setParent(category: Category?): ChannelAction<T>?

    /**
     * Sets the position where the new Channel should be inserted into.
     * This refers to the raw position value, not the computed (relative) position.
     *
     *
     * By default (or by providing this method with `null`),
     * the position will automatically be computed based on the other Channels (inserted last in its respective group).
     *
     *
     * Note: This does not shift the position values of existing Channels if the values collide.
     * <br></br>As a reminder: The ordering of Channels is determined first by its Category's position, then by its raw
     * position value and finally by its id (younger Channels are below older ones)
     *
     * @param  position
     * The raw position value that should be used for the new Channel
     *
     * @throws IllegalArgumentException
     * If the provided position value is `<0`
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setPosition(position: Int?): ChannelAction<T>?

    /**
     * Sets the topic for the channel
     *
     * @param  topic
     * The topic for the new GuildChannel
     *
     * @throws UnsupportedOperationException
     * If this ChannelAction is not for a TextChannel
     * @throws IllegalArgumentException
     * If the provided topic is greater than {@value StandardGuildMessageChannel#MAX_TOPIC_LENGTH} in length.
     * For [IPostContainers][IPostContainer],
     * this limit is {@value IPostContainer#MAX_POST_CONTAINER_TOPIC_LENGTH} instead.
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setTopic(topic: String?): ChannelAction<T>?

    /**
     * Sets the NSFW flag for the channel
     *
     * @param  nsfw
     * The NSFW flag for the new GuildChannel
     *
     * @throws UnsupportedOperationException
     * If this ChannelAction is not for a TextChannel
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setNSFW(nsfw: Boolean): ChannelAction<T>?

    /**
     * Sets the slowmode value, which limits the amount of time that individual users must wait
     * between sending messages in the new channel. This is measured in seconds.
     *
     *
     * Note: Bots are unaffected by this.
     * <br></br>Having [MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] or
     * [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission also
     * grants immunity to slowmode.
     *
     * @param  slowmode
     * The number of seconds required to wait between sending messages in the channel.
     *
     * @throws UnsupportedOperationException
     * If this ChannelAction is not for a [ISlowmodeChannel]
     * @throws IllegalArgumentException
     * If the `slowmode` is greater than [ISlowmodeChannel.MAX_SLOWMODE], or less than 0
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setSlowmode(slowmode: Int): ChannelAction<T>?

    /**
     * Sets the slowmode value, which limits the amount of time that individual users must wait
     * between sending messages in the new channel. This is measured in seconds.
     * <br></br>This is applied to newly created threads by default.
     *
     *
     * Note: Bots are unaffected by this.
     * <br></br>Having [MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] or
     * [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission also
     * grants immunity to slowmode.
     *
     * @param  slowmode
     * The number of seconds required to wait between sending messages in the channel.
     *
     * @throws UnsupportedOperationException
     * If this ChannelAction is not for a [IThreadContainer]
     * @throws IllegalArgumentException
     * If the `slowmode` is greater than [ISlowmodeChannel.MAX_SLOWMODE], or less than 0
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setDefaultThreadSlowmode(slowmode: Int): ChannelAction<T>?

    /**
     * Sets the **<u>default reaction emoji</u>** of the channel.
     * <br></br>This does not support custom emoji from other guilds.
     *
     * @param  emoji
     * The new default reaction emoji, or null to unset.
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see IPostContainer.getDefaultReaction
     */
    @Nonnull
    @CheckReturnValue
    fun setDefaultReaction(emoji: Emoji?): ChannelAction<T>?

    /**
     * Sets the **<u>default layout</u>** of the new [ForumChannel].
     *
     * @param  layout
     * The new default layout.
     *
     * @throws IllegalArgumentException
     * If null or [UNKNOWN][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel.Layout.UNKNOWN] is provided
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see ForumChannel.getDefaultLayout
     */
    @Nonnull
    @CheckReturnValue
    fun setDefaultLayout(@Nonnull layout: ForumChannel.Layout?): ChannelAction<T>?

    /**
     * Sets the **<u>default sort order</u>** of the channel.
     *
     * @param  sortOrder
     * The new default sort order.
     *
     * @throws IllegalArgumentException
     * If null or [UNKNOWN][net.dv8tion.jda.api.entities.channel.attribute.IPostContainer.SortOrder.UNKNOWN] is provided
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see IPostContainer.getDefaultSortOrder
     */
    @Nonnull
    @CheckReturnValue
    fun setDefaultSortOrder(@Nonnull sortOrder: IPostContainer.SortOrder?): ChannelAction<T>?

    /**
     * Sets the **<u>available tags</u>** of the channel.
     * <br></br>Tags will be ordered based on the provided list order.
     *
     *
     * You can use [ForumTagData] to create new tags.
     *
     * @param  tags
     * The new available tags in the desired order.
     *
     * @throws IllegalArgumentException
     * If the provided list is null or contains null elements
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see IPostContainer.getAvailableTags
     */
    @Nonnull
    @CheckReturnValue
    fun setAvailableTags(@Nonnull tags: List<BaseForumTag?>?): ChannelAction<T>?

    /**
     * Adds a new Role or Member [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     * for the new GuildChannel.
     *
     *
     * If setting permission overwrites, only permissions your bot has in the guild can be allowed/denied.
     *
     *
     * Example:
     * <pre>`Role role = guild.getPublicRole();
     * EnumSet<Permission> allow = EnumSet.of(Permission.VIEW_CHANNEL);
     * EnumSet<Permission> deny = EnumSet.of(Permission.MESSAGE_SEND);
     * channelAction.addPermissionOverride(role, allow, deny);
    `</pre> *
     *
     * @param  target
     * The not-null [Role][net.dv8tion.jda.api.entities.Role] or [Member][net.dv8tion.jda.api.entities.Member] for the override
     * @param  allow
     * The granted [Permissions][net.dv8tion.jda.api.Permission] for the override or null
     * @param  deny
     * The denied [Permissions][net.dv8tion.jda.api.Permission] for the override or null
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If any permission is set in allow/deny that the currently logged in account is missing,
     * unless [Permission.MANAGE_PERMISSIONS] or [Permission.MANAGE_ROLES] is granted to it within the context of the parent category.
     * @throws java.lang.IllegalArgumentException
     * If the specified target is null or not within the same guild.
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see java.util.EnumSet
     */
    @Nonnull
    @CheckReturnValue
    fun addPermissionOverride(
        @Nonnull target: IPermissionHolder,
        allow: Collection<Permission?>?,
        deny: Collection<Permission?>?
    ): ChannelAction<T>? {
        val allowRaw = if (allow != null) Permission.getRaw(allow) else 0
        val denyRaw = if (deny != null) Permission.getRaw(deny) else 0
        return addPermissionOverride(target, allowRaw, denyRaw)
    }

    /**
     * Adds a new Role or Member [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     * for the new GuildChannel.
     *
     *
     * If setting permission overwrites, only permissions your bot has in the guild can be allowed/denied.
     *
     *
     * Example:
     * <pre>`Role role = guild.getPublicRole();
     * long allow = Permission.VIEW_CHANNEL.getRawValue();
     * long deny = Permission.MESSAGE_SEND.getRawValue() | Permission.MESSAGE_ADD_REACTION.getRawValue();
     * channelAction.addPermissionOverride(role, allow, deny);
    `</pre> *
     *
     * @param  target
     * The not-null [Role][net.dv8tion.jda.api.entities.Role] or [Member][net.dv8tion.jda.api.entities.Member] for the override
     * @param  allow
     * The granted [Permissions][net.dv8tion.jda.api.Permission] for the override.
     * Use [net.dv8tion.jda.api.Permission.getRawValue] to retrieve these Permissions.
     * @param  deny
     * The denied [Permissions][net.dv8tion.jda.api.Permission] for the override.
     * Use [net.dv8tion.jda.api.Permission.getRawValue] to retrieve these Permissions.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If any permission is set in allow/deny that the currently logged in account is missing,
     * unless [Permission.MANAGE_PERMISSIONS] or [Permission.MANAGE_ROLES] is granted to it within the context of the parent category.
     * @throws java.lang.IllegalArgumentException
     *
     *  * If the specified target is null
     * or not within the same guild.
     *  * If one of the provided Permission values is invalid
     *
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see net.dv8tion.jda.api.Permission.getRawValue
     * @see net.dv8tion.jda.api.Permission.getRaw
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun addPermissionOverride(@Nonnull target: IPermissionHolder, allow: Long, deny: Long): ChannelAction<T>? {
        Checks.notNull(target, "Override Role/Member")
        if (target is Role) return addRolePermissionOverride(
            target.idLong,
            allow,
            deny
        ) else if (target is Member) return addMemberPermissionOverride(target.idLong, allow, deny)
        throw IllegalArgumentException("Cannot add override for " + target.javaClass.getSimpleName())
    }

    /**
     * Adds a new Member [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     * for the new GuildChannel.
     *
     *
     * If setting permission overwrites, only permissions your bot has in the guild can be allowed/denied.
     *
     *
     * Example:
     * <pre>`long userId = user.getIdLong();
     * EnumSet<Permission> allow = EnumSet.of(Permission.VIEW_CHANNEL);
     * EnumSet<Permission> deny = EnumSet.of(Permission.MESSAGE_SEND);
     * channelAction.addMemberPermissionOverride(userId, allow, deny);
    `</pre> *
     *
     * @param  memberId
     * The id for the member
     * @param  allow
     * The granted [Permissions][net.dv8tion.jda.api.Permission] for the override or null
     * @param  deny
     * The denied [Permissions][net.dv8tion.jda.api.Permission] for the override or null
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If any permission is set in allow/deny that the currently logged in account is missing,
     * unless [Permission.MANAGE_PERMISSIONS] or [Permission.MANAGE_ROLES] is granted to it within the context of the parent category.
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see java.util.EnumSet
     */
    @Nonnull
    @CheckReturnValue
    fun addMemberPermissionOverride(
        memberId: Long,
        allow: Collection<Permission?>?,
        deny: Collection<Permission?>?
    ): ChannelAction<T>? {
        val allowRaw = if (allow != null) Permission.getRaw(allow) else 0
        val denyRaw = if (deny != null) Permission.getRaw(deny) else 0
        return addMemberPermissionOverride(memberId, allowRaw, denyRaw)
    }

    /**
     * Adds a new Role [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     * for the new GuildChannel.
     *
     *
     * If setting permission overwrites, only permissions your bot has in the guild can be allowed/denied.
     *
     *
     * Example:
     * <pre>`long roleId = role.getIdLong();
     * EnumSet<Permission> allow = EnumSet.of(Permission.VIEW_CHANNEL);
     * EnumSet<Permission> deny = EnumSet.of(Permission.MESSAGE_SEND);
     * channelAction.addRolePermissionOverride(roleId, allow, deny);
    `</pre> *
     *
     * @param  roleId
     * The id for the role
     * @param  allow
     * The granted [Permissions][net.dv8tion.jda.api.Permission] for the override or null
     * @param  deny
     * The denied [Permissions][net.dv8tion.jda.api.Permission] for the override or null
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If any permission is set in allow/deny that the currently logged in account is missing,
     * unless [Permission.MANAGE_PERMISSIONS] or [Permission.MANAGE_ROLES] is granted to it within the context of the parent category.
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see java.util.EnumSet
     */
    @Nonnull
    @CheckReturnValue
    fun addRolePermissionOverride(
        roleId: Long,
        allow: Collection<Permission?>?,
        deny: Collection<Permission?>?
    ): ChannelAction<T>? {
        val allowRaw = if (allow != null) Permission.getRaw(allow) else 0
        val denyRaw = if (deny != null) Permission.getRaw(deny) else 0
        return addRolePermissionOverride(roleId, allowRaw, denyRaw)
    }

    /**
     * Adds a new Member [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride] for the new GuildChannel.
     *
     *
     * If setting permission overwrites, only permissions your bot has in the guild can be allowed/denied.
     *
     *
     * Example:
     * <pre>`long userId = user.getIdLong();
     * long allow = Permission.VIEW_CHANNEL.getRawValue();
     * long deny = Permission.MESSAGE_SEND.getRawValue() | Permission.MESSAGE_ADD_REACTION.getRawValue();
     * channelAction.addMemberPermissionOverride(userId, allow, deny);
    `</pre> *
     *
     * @param  memberId
     * The id for the member
     * @param  allow
     * The granted [Permissions][net.dv8tion.jda.api.Permission] for the override.
     * Use [net.dv8tion.jda.api.Permission.getRawValue] to retrieve these Permissions.
     * @param  deny
     * The denied [Permissions][net.dv8tion.jda.api.Permission] for the override.
     * Use [net.dv8tion.jda.api.Permission.getRawValue] to retrieve these Permissions.
     *
     * @throws java.lang.IllegalArgumentException
     * If one of the provided Permission values is invalid
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If any permission is set in allow/deny that the currently logged in account is missing,
     * unless [Permission.MANAGE_PERMISSIONS] or [Permission.MANAGE_ROLES] is granted to it within the context of the parent category.
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see net.dv8tion.jda.api.Permission.getRawValue
     * @see net.dv8tion.jda.api.Permission.getRaw
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun addMemberPermissionOverride(memberId: Long, allow: Long, deny: Long): ChannelAction<T>?

    /**
     * Adds a new Role [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride] for the new GuildChannel.
     *
     *
     * If setting permission overwrites, only permissions your bot has in the guild can be allowed/denied.
     *
     *
     * Example:
     * <pre>`long roleId = role.getIdLong();
     * long allow = Permission.VIEW_CHANNEL.getRawValue();
     * long deny = Permission.MESSAGE_SEND.getRawValue() | Permission.MESSAGE_ADD_REACTION.getRawValue();
     * channelAction.addMemberPermissionOverride(roleId, allow, deny);
    `</pre> *
     *
     * @param  roleId
     * The id for the role
     * @param  allow
     * The granted [Permissions][net.dv8tion.jda.api.Permission] for the override.
     * Use [net.dv8tion.jda.api.Permission.getRawValue] to retrieve these Permissions.
     * @param  deny
     * The denied [Permissions][net.dv8tion.jda.api.Permission] for the override.
     * Use [net.dv8tion.jda.api.Permission.getRawValue] to retrieve these Permissions.
     *
     * @throws java.lang.IllegalArgumentException
     * If one of the provided Permission values is invalid
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If any permission is set in allow/deny that the currently logged in account is missing,
     * unless [Permission.MANAGE_PERMISSIONS] or [Permission.MANAGE_ROLES] is granted to it within the context of the parent category.
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see net.dv8tion.jda.api.Permission.getRawValue
     * @see net.dv8tion.jda.api.Permission.getRaw
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun addRolePermissionOverride(roleId: Long, allow: Long, deny: Long): ChannelAction<T>?

    /**
     * Removes any existing override with the provided id.
     * <br></br>If no override with the provided id exists, this method does nothing.
     *
     * @param  id
     * The member or role id of the override
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun removePermissionOverride(id: Long): ChannelAction<T>?

    /**
     * Removes any existing override with the provided id.
     * <br></br>If no override with the provided id exists, this method does nothing.
     *
     * @param  id
     * The member or role id of the override
     *
     * @throws IllegalArgumentException
     * If the provided string is not a valid snowflake or null
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun removePermissionOverride(@Nonnull id: String?): ChannelAction<T>? {
        return removePermissionOverride(MiscUtil.parseSnowflake(id))
    }

    /**
     * Removes any existing override with the provided role/member.
     * <br></br>If no override for the provided role/member exists, this method does nothing.
     *
     * @param  holder
     * The member or role of the override
     *
     * @throws IllegalArgumentException
     * If the provided permission holder is null
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun removePermissionOverride(@Nonnull holder: IPermissionHolder): ChannelAction<T>? {
        Checks.notNull(holder, "PermissionHolder")
        return removePermissionOverride(holder.idLong)
    }

    /**
     * Removes all currently configured permission overrides
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun clearPermissionOverrides(): ChannelAction<T>?

    /**
     * Syncs the permission overrides of the channel with the category.
     *
     *
     * If setting permission overwrites, only permissions your bot has in the guild can be allowed/denied.
     * In order to properly sync permissions the currently logged in account must have all allowed/denied permissions or [Permission.MANAGE_ROLES] in the parent category.
     *
     * @throws IllegalArgumentException
     * If no parent has been configured. You have to use [.setParent] before calling this method.
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun syncPermissionOverrides(): ChannelAction<T>?

    /**
     * Sets the bitrate for the new VoiceChannel
     *
     * @param  bitrate
     * The bitrate for the new VoiceChannel in `bps` (limits 8000 &lt;= bitrate &lt;= [Guild.getMaxBitrate])
     * or `null` to use the default 64kbps.
     *
     * @throws UnsupportedOperationException
     * If this ChannelAction is not for a VoiceChannel
     * @throws IllegalArgumentException
     * If the provided bitrate is less than 8000 or greater than [net.dv8tion.jda.api.entities.Guild.getMaxBitrate]
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setBitrate(bitrate: Int?): ChannelAction<T>?

    /**
     * Sets the userlimit for the new [AudioChannel].
     * <br></br>The limit maximum varies by type.
     *
     *  * [ChannelType.VOICE] - {@value VoiceChannel#MAX_USERLIMIT}
     *  * [ChannelType.STAGE] - {@value StageChannel#MAX_USERLIMIT}
     *
     *
     * @param  userlimit
     * The userlimit for the new AudioChannel or `null`/`0` to use no limit
     *
     * @throws UnsupportedOperationException
     * If this ChannelAction is not for a AudioChannel
     * @throws IllegalArgumentException
     * If the provided userlimit is negative or above the permitted limit
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setUserlimit(userlimit: Int?): ChannelAction<T>?

    /**
     * Sets the voice region for the new AudioChannel
     *
     * @param  region
     * The region for the new AudioChannel, or `null` to set to [Region.AUTOMATIC]
     *
     * @throws UnsupportedOperationException
     * If this ChannelAction is not for an AudioChannel
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setRegion(region: Region?): ChannelAction<T>?
}
