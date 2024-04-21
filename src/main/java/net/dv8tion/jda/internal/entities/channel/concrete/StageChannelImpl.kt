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
import net.dv8tion.jda.api.entities.IPermissionHolder.getPermissions
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.StageInstance
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.managers.channel.concrete.StageChannelManager
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.api.requests.restaction.ChannelAction
import net.dv8tion.jda.api.requests.restaction.StageInstanceAction
import net.dv8tion.jda.api.utils.MiscUtil.newLongMap
import net.dv8tion.jda.api.utils.data.DataObject.Companion.empty
import net.dv8tion.jda.internal.entities.GuildImpl
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractStandardGuildChannelImpl
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IAgeRestrictedChannelMixin
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.ISlowmodeChannelMixin
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IWebhookContainerMixin
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.AudioChannelMixin
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.GuildMessageChannelMixin
import net.dv8tion.jda.internal.managers.channel.concrete.StageChannelManagerImpl
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.requests.restaction.StageInstanceActionImpl
import net.dv8tion.jda.internal.utils.Checks
import java.time.OffsetDateTime
import java.util.*
import javax.annotation.Nonnull

class StageChannelImpl(id: Long, guild: GuildImpl?) : AbstractStandardGuildChannelImpl<StageChannelImpl?>(id, guild),
    StageChannel, AudioChannelMixin<StageChannelImpl?>, GuildMessageChannelMixin<StageChannelImpl?>,
    IWebhookContainerMixin<StageChannelImpl?>, IAgeRestrictedChannelMixin<StageChannelImpl?>,
    ISlowmodeChannelMixin<StageChannelImpl?> {
    private val connectedMembers = newLongMap<Member>()
    override var stageInstance: StageInstance? = null
        private set
    override var regionRaw: String? = null
        private set
    override var bitrate = 0
        private set
    override var userLimit = 0
        private set
    override var slowmode = 0
        private set
    override var isNSFW = false
        private set
    override var latestMessageIdLong: Long = 0
        private set

    @get:Nonnull
    override val type: ChannelType
        get() = ChannelType.STAGE

    @get:Nonnull
    override val members: List<Member>
        get() = Collections.unmodifiableList(ArrayList(connectedMembers.valueCollection()))

    @Nonnull
    override fun createStageInstance(@Nonnull topic: String?): StageInstanceAction? {
        val permissions: EnumSet<Permission> = getGuild().getSelfMember().getPermissions(this)
        val required = EnumSet.of(Permission.MANAGE_CHANNEL, Permission.VOICE_MUTE_OTHERS, Permission.VOICE_MOVE_OTHERS)
        for (perm in required) {
            if (!permissions.contains(perm)) throw InsufficientPermissionException(
                this,
                perm,
                "You must be a stage moderator to create a stage instance! Missing Permission: $perm"
            )
        }
        return StageInstanceActionImpl(this).setTopic(topic)
    }

    @Nonnull
    override fun createCopy(@Nonnull guild: Guild?): ChannelAction<StageChannel?>? {
        Checks.notNull(guild, "Guild")
        val action: ChannelAction<StageChannel?> = guild.createStageChannel(name).setBitrate(bitrate)
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

    override fun canTalk(@Nonnull member: Member?): Boolean {
        Checks.notNull(member, "Member")
        return member!!.hasPermission(this, Permission.MESSAGE_SEND)
    }

    @get:Nonnull
    override val manager: StageChannelManager
        get() = StageChannelManagerImpl(this)

    @Nonnull
    override fun requestToSpeak(): RestAction<Void?>? {
        val guild: Guild = getGuild()
        val route = Route.Guilds.UPDATE_VOICE_STATE.compile(guild.id!!, "@me")
        val body = empty().put("channel_id", id)
        // Stage moderators can bypass the request queue by just unsuppressing
        if (guild.selfMember.hasPermission(
                this,
                Permission.VOICE_MUTE_OTHERS
            )
        ) body.putNull("request_to_speak_timestamp").put("suppress", false) else body.put(
            "request_to_speak_timestamp",
            OffsetDateTime.now().toString()
        )
        check(this == guild.selfMember.voiceState!!.channel) { "Cannot request to speak without being connected to the stage channel!" }
        return RestActionImpl(jda, route, body)
    }

    @Nonnull
    override fun cancelRequestToSpeak(): RestAction<Void?>? {
        val guild: Guild = getGuild()
        val route = Route.Guilds.UPDATE_VOICE_STATE.compile(guild.id!!, "@me")
        val body = empty()
            .putNull("request_to_speak_timestamp")
            .put("suppress", true)
            .put("channel_id", id)
        check(this == guild.selfMember.voiceState!!.channel) { "Cannot cancel request to speak without being connected to the stage channel!" }
        return RestActionImpl(jda, route, body)
    }

    override fun getConnectedMembersMap(): TLongObjectMap<Member> {
        return connectedMembers
    }

    override fun setBitrate(bitrate: Int): StageChannelImpl {
        this.bitrate = bitrate
        return this
    }

    override fun setUserLimit(userlimit: Int): StageChannelImpl {
        userLimit = userlimit
        return this
    }

    override fun setRegion(region: String): StageChannelImpl {
        regionRaw = region
        return this
    }

    fun setStageInstance(instance: StageInstance?): StageChannelImpl {
        stageInstance = instance
        return this
    }

    override fun setNSFW(ageRestricted: Boolean): StageChannelImpl {
        isNSFW = ageRestricted
        return this
    }

    override fun setSlowmode(slowmode: Int): StageChannelImpl {
        this.slowmode = slowmode
        return this
    }

    override fun setLatestMessageIdLong(latestMessageId: Long): StageChannelImpl {
        latestMessageIdLong = latestMessageId
        return this
    }
}
