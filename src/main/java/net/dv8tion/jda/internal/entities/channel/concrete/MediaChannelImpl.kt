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
package net.dv8tion.jda.internal.entities.channel.concrete

import gnu.trove.map.TLongObjectMap
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.IPermissionHolder.hasPermission
import net.dv8tion.jda.api.entities.channel.ChannelFlag
import net.dv8tion.jda.api.entities.channel.ChannelFlag.Companion.fromRaw
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer
import net.dv8tion.jda.api.entities.channel.concrete.MediaChannel
import net.dv8tion.jda.api.entities.channel.forums.ForumTag
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.Emoji.Companion.fromUnicode
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.api.managers.channel.concrete.MediaChannelManager
import net.dv8tion.jda.api.requests.restaction.ChannelAction
import net.dv8tion.jda.api.requests.restaction.StageInstanceAction.setTopic
import net.dv8tion.jda.api.utils.MiscUtil.newLongMap
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.entities.GuildImpl
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractGuildChannelImpl
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.*
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.StandardGuildChannelMixin
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl
import net.dv8tion.jda.internal.managers.channel.concrete.MediaChannelManagerImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.Helpers
import net.dv8tion.jda.internal.utils.cache.SortedSnowflakeCacheViewImpl
import java.util.*
import javax.annotation.Nonnull

class MediaChannelImpl(id: Long, guild: GuildImpl?) : AbstractGuildChannelImpl<MediaChannelImpl?>(id, guild),
    MediaChannel, GuildChannelUnion, StandardGuildChannelMixin<MediaChannelImpl?>,
    IAgeRestrictedChannelMixin<MediaChannelImpl?>, ISlowmodeChannelMixin<MediaChannelImpl?>,
    IWebhookContainerMixin<MediaChannelImpl?>, IPostContainerMixin<MediaChannelImpl?>,
    ITopicChannelMixin<MediaChannelImpl?> {
    private val overrides = newLongMap<PermissionOverride>()

    @get:Nonnull
    override val availableTagCache =
        SortedSnowflakeCacheViewImpl<ForumTag>(ForumTag::class.java, ForumTag::getName, Comparator.naturalOrder())
    private override var defaultReaction: Emoji? = null
    private override var topic: String? = null
    override var parentCategoryIdLong: Long = 0
        private set
    override var isNSFW = false
        private set
    override var positionRaw = 0
        private set
    private override var flags = 0
    override var slowmode = 0
        private set
    private override var defaultSortOrder = 0
    override var defaultThreadSlowmode = 0
        protected set

    @get:Nonnull
    override val manager: MediaChannelManager
        get() = MediaChannelManagerImpl(this)

    @get:Nonnull
    override val members: List<Member>
        get() = getGuild().getMembers()
            .stream()
            .filter { m -> m.hasPermission(this, Permission.VIEW_CHANNEL) }
            .collect(Helpers.toUnmodifiableList())

    @Nonnull
    override fun createCopy(@Nonnull guild: Guild?): ChannelAction<MediaChannel?>? {
        Checks.notNull(guild, "Guild")
        val action: ChannelAction<MediaChannel?> = guild.createMediaChannel(name)
            .setNSFW(isNSFW)
            .setTopic(topic)
            .setSlowmode(slowmode)
            .setAvailableTags(availableTags)
        if (defaultSortOrder != -1) action.setDefaultSortOrder(IPostContainer.SortOrder.fromKey(defaultSortOrder))
        if (defaultReaction is UnicodeEmoji) action.setDefaultReaction(defaultReaction)
        if (guild == getGuild()) {
            val parent = parentCategory
            action.setDefaultReaction(defaultReaction)
            if (parent != null) action.setParent(parent)
            for (o in overrides.valueCollection()) {
                if (o.isMemberOverride) action.addMemberPermissionOverride(
                    o.idLong,
                    o.allowedRaw,
                    o.deniedRaw
                ) else action.addRolePermissionOverride(o.idLong, o.allowedRaw, o.deniedRaw)
            }
        }
        return action
    }

    @Nonnull
    override fun getFlags(): EnumSet<ChannelFlag> {
        return fromRaw(flags)
    }

    override fun getPermissionOverrideMap(): TLongObjectMap<PermissionOverride> {
        return overrides
    }

    override fun getTopic(): String {
        return topic!!
    }

    fun getDefaultReaction(): EmojiUnion? {
        return defaultReaction as EmojiUnion?
    }

    @Nonnull
    fun getDefaultSortOrder(): IPostContainer.SortOrder {
        return IPostContainer.SortOrder.fromKey(defaultSortOrder)
    }

    override fun getRawFlags(): Int {
        return flags
    }

    override fun getRawSortOrder(): Int {
        return defaultSortOrder
    }

    // Setters
    override fun setParentCategory(parentCategoryId: Long): MediaChannelImpl {
        parentCategoryIdLong = parentCategoryId
        return this
    }

    override fun setPosition(position: Int): MediaChannelImpl {
        positionRaw = position
        return this
    }

    override fun setDefaultThreadSlowmode(defaultThreadSlowmode: Int): MediaChannelImpl {
        this.defaultThreadSlowmode = defaultThreadSlowmode
        return this
    }

    override fun setNSFW(nsfw: Boolean): MediaChannelImpl {
        isNSFW = nsfw
        return this
    }

    override fun setSlowmode(slowmode: Int): MediaChannelImpl {
        this.slowmode = slowmode
        return this
    }

    override fun setTopic(topic: String): MediaChannelImpl {
        this.topic = topic
        return this
    }

    override fun setFlags(flags: Int): MediaChannelImpl {
        this.flags = flags
        return this
    }

    override fun setDefaultReaction(emoji: DataObject): MediaChannelImpl {
        if (emoji != null && !emoji.isNull("emoji_id")) defaultReaction = CustomEmojiImpl(
            "",
            emoji.getUnsignedLong("emoji_id"),
            false
        ) else if (emoji != null && !emoji.isNull("emoji_name")) defaultReaction =
            fromUnicode(emoji.getString("emoji_name")) else defaultReaction = null
        return this
    }

    override fun setDefaultSortOrder(defaultSortOrder: Int): MediaChannelImpl {
        this.defaultSortOrder = defaultSortOrder
        return this
    }
}
