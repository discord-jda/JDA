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
import net.dv8tion.jda.api.Region.Companion.fromKey
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.attribute.IVoiceStatusChannel
import net.dv8tion.jda.api.entities.channel.concrete.*
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.api.requests.restaction.ChannelAction
import net.dv8tion.jda.api.utils.MiscUtil.newLongMap
import net.dv8tion.jda.api.utils.data.DataObject.Companion.empty
import net.dv8tion.jda.internal.entities.GuildImpl
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractStandardGuildChannelImpl
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IAgeRestrictedChannelMixin
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.ISlowmodeChannelMixin
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IWebhookContainerMixin
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.AudioChannelMixin
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.GuildMessageChannelMixin
import net.dv8tion.jda.internal.managers.channel.concrete.VoiceChannelManagerImpl
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import javax.annotation.Nonnull

class VoiceChannelImpl(id: Long, guild: GuildImpl?) : AbstractStandardGuildChannelImpl<VoiceChannelImpl?>(id, guild),
    VoiceChannel, GuildMessageChannelMixin<VoiceChannelImpl?>, AudioChannelMixin<VoiceChannelImpl?>,
    IWebhookContainerMixin<VoiceChannelImpl?>, IAgeRestrictedChannelMixin<VoiceChannelImpl?>,
    ISlowmodeChannelMixin<VoiceChannelImpl?> {
    private val connectedMembers = newLongMap<Member>()
    override var regionRaw: String? = null
        private set

    @get:Nonnull
    override var status = ""
        private set
    override var latestMessageIdLong: Long = 0
        private set
    override var bitrate = 0
        private set
    override var userLimit = 0
        private set
    override var slowmode = 0
        private set
    override var isNSFW = false
        private set

    @get:Nonnull
    override val type: ChannelType
        get() = ChannelType.VOICE

    override fun canTalk(@Nonnull member: Member?): Boolean {
        Checks.notNull(member, "Member")
        return member!!.hasPermission(this, Permission.MESSAGE_SEND)
    }

    @get:Nonnull
    override val members: List<Member>
        get() = Collections.unmodifiableList(ArrayList(connectedMembers.valueCollection()))

    @Nonnull
    override fun createCopy(@Nonnull guild: Guild?): ChannelAction<VoiceChannel?>? {
        Checks.notNull(guild, "Guild")
        val action: ChannelAction<VoiceChannel?> = guild.createVoiceChannel(name)
            .setBitrate(bitrate)
            .setUserlimit(userLimit)
        if (regionRaw != null) {
            action.setRegion(fromKey(regionRaw))
        }
        if (guild == getGuild()) {
            val parent = parentCategory
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

    @get:Nonnull
    override val manager: VoiceChannelManager
        get() = VoiceChannelManagerImpl(this)

    @Nonnull
    override fun modifyStatus(@Nonnull status: String?): AuditableRestAction<Void?>? {
        Checks.notLonger(status, IVoiceStatusChannel.MAX_STATUS_LENGTH, "Voice Status")
        checkCanAccessChannel()
        if (this == getGuild().getSelfMember().voiceState.channel) checkPermission(Permission.VOICE_SET_STATUS) else checkCanManage()
        val route = Route.Channels.SET_STATUS.compile(id)
        val body = empty().put("status", status)
        return AuditableRestActionImpl(api, route, body)
    }

    override fun getConnectedMembersMap(): TLongObjectMap<Member> {
        return connectedMembers
    }

    override fun setBitrate(bitrate: Int): VoiceChannelImpl {
        this.bitrate = bitrate
        return this
    }

    override fun setRegion(region: String): VoiceChannelImpl {
        regionRaw = region
        return this
    }

    override fun setUserLimit(userLimit: Int): VoiceChannelImpl {
        this.userLimit = userLimit
        return this
    }

    override fun setNSFW(nsfw: Boolean): VoiceChannelImpl {
        isNSFW = nsfw
        return this
    }

    override fun setSlowmode(slowmode: Int): VoiceChannelImpl {
        this.slowmode = slowmode
        return this
    }

    override fun setLatestMessageIdLong(latestMessageId: Long): VoiceChannelImpl {
        latestMessageIdLong = latestMessageId
        return this
    }

    fun setStatus(status: String): VoiceChannelImpl {
        this.status = status
        return this
    }
}
