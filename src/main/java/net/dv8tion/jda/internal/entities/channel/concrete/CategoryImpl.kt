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
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.managers.channel.concrete.CategoryManager
import net.dv8tion.jda.api.requests.restaction.ChannelAction
import net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction
import net.dv8tion.jda.api.utils.MiscUtil.newLongMap
import net.dv8tion.jda.internal.entities.GuildImpl
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractGuildChannelImpl
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IPermissionContainerMixin
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IPositionableChannelMixin
import net.dv8tion.jda.internal.managers.channel.concrete.CategoryManagerImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.PermissionUtil
import javax.annotation.Nonnull

class CategoryImpl(id: Long, guild: GuildImpl?) : AbstractGuildChannelImpl<CategoryImpl?>(id, guild), Category,
    IPositionableChannelMixin<CategoryImpl?>, IPermissionContainerMixin<CategoryImpl?> {
    private val overrides = newLongMap<PermissionOverride>()
    override var positionRaw = 0
        private set

    @get:Nonnull
    override val type: ChannelType
        get() = ChannelType.CATEGORY

    @Nonnull
    override fun createTextChannel(@Nonnull name: String?): ChannelAction<TextChannel?>? {
        val action: ChannelAction<TextChannel?> = getGuild().createTextChannel(name, this)
        return trySync(action)
    }

    @Nonnull
    override fun createNewsChannel(@Nonnull name: String?): ChannelAction<NewsChannel?>? {
        val action: ChannelAction<NewsChannel?> = getGuild().createNewsChannel(name, this)
        return trySync(action)
    }

    @Nonnull
    override fun createVoiceChannel(@Nonnull name: String?): ChannelAction<VoiceChannel?>? {
        val action: ChannelAction<VoiceChannel?> = getGuild().createVoiceChannel(name, this)
        return trySync(action)
    }

    @Nonnull
    override fun createStageChannel(@Nonnull name: String?): ChannelAction<StageChannel?>? {
        val action: ChannelAction<StageChannel?> = getGuild().createStageChannel(name, this)
        return trySync(action)
    }

    @Nonnull
    override fun createForumChannel(@Nonnull name: String?): ChannelAction<ForumChannel?>? {
        val action: ChannelAction<ForumChannel?> = getGuild().createForumChannel(name, this)
        return trySync(action)
    }

    @Nonnull
    override fun createMediaChannel(@Nonnull name: String?): ChannelAction<MediaChannel?>? {
        val action: ChannelAction<MediaChannel?> = getGuild().createMediaChannel(name, this)
        return trySync(action)
    }

    @Nonnull
    override fun modifyTextChannelPositions(): CategoryOrderAction? {
        return getGuild().modifyTextChannelPositions(this)
    }

    @Nonnull
    override fun modifyVoiceChannelPositions(): CategoryOrderAction? {
        return getGuild().modifyVoiceChannelPositions(this)
    }

    @Nonnull
    override fun createCopy(@Nonnull guild: Guild?): ChannelAction<Category?>? {
        Checks.notNull(guild, "Guild")
        val action: ChannelAction<Category?> = guild.createCategory(name)
        if (guild == getGuild()) {
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
    override fun createCopy(): ChannelAction<Category?>? {
        return createCopy(getGuild())
    }

    @get:Nonnull
    override val manager: CategoryManager
        get() = CategoryManagerImpl(this)

    override fun getPermissionOverrideMap(): TLongObjectMap<PermissionOverride> {
        return overrides
    }

    override fun setPosition(position: Int): CategoryImpl {
        positionRaw = position
        return this
    }

    private fun <T : GuildChannel?> trySync(action: ChannelAction<T>): ChannelAction<T>? {
        val selfMember: Member = getGuild().getSelfMember()
        if (!selfMember.canSync(this)) {
            val botPerms = PermissionUtil.getEffectivePermission(this, selfMember)
            for (override in getPermissionOverrides()) {
                val perms = override.deniedRaw or override.allowedRaw
                if (perms and botPerms.inv() != 0L) return action
            }
        }
        return action.syncPermissionOverrides()
    }
}
