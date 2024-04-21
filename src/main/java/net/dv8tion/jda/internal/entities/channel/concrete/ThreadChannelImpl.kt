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

import gnu.trove.set.TLongSet
import gnu.trove.set.hash.TLongHashSet
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.ThreadMember
import net.dv8tion.jda.api.entities.channel.ChannelFlag
import net.dv8tion.jda.api.entities.channel.ChannelFlag.Companion.fromRaw
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.attribute.IGuildChannelContainer.getChannelById
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel.AutoArchiveDuration
import net.dv8tion.jda.api.entities.channel.forums.ForumTag
import net.dv8tion.jda.api.entities.channel.unions.IThreadContainerUnion
import net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager
import net.dv8tion.jda.api.requests.Request
import net.dv8tion.jda.api.requests.Response
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.api.requests.restaction.CacheRestAction
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadMemberPaginationAction
import net.dv8tion.jda.api.utils.TimeUtil.getTimeCreated
import net.dv8tion.jda.api.utils.cache.CacheView.SimpleCacheView
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.entities.GuildImpl
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractGuildChannelImpl
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.ISlowmodeChannelMixin
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.GuildMessageChannelMixin
import net.dv8tion.jda.internal.managers.channel.concrete.ThreadChannelManagerImpl
import net.dv8tion.jda.internal.requests.DeferredRestAction
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.requests.restaction.pagination.ThreadMemberPaginationActionImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.Helpers
import java.time.OffsetDateTime
import java.util.*
import java.util.stream.LongStream
import javax.annotation.Nonnull
import kotlin.math.max

class ThreadChannelImpl(id: Long, guild: GuildImpl?, @get:Nonnull override val type: ChannelType) :
    AbstractGuildChannelImpl<ThreadChannelImpl?>(id, guild), ThreadChannel,
    GuildMessageChannelMixin<ThreadChannelImpl?>, ISlowmodeChannelMixin<ThreadChannelImpl?> {
    val threadMemberView = SimpleCacheView<ThreadMember?>(ThreadMember::class.java, null)
    var appliedTagsSet: TLongSet = TLongHashSet(ForumChannel.MAX_POST_TAGS)
        private set

    @get:Nonnull
    override var autoArchiveDuration: AutoArchiveDuration? = null
        private set
    private override var parentChannel: IThreadContainerUnion? = null
    override var isLocked = false
        private set
    override var isArchived = false
        private set
    private var invitable = false
    var archiveTimestamp: Long = 0
        private set
    private var creationTimestamp: Long = 0
    override var ownerIdLong: Long = 0
        private set
    override var latestMessageIdLong: Long = 0
        private set
    override var messageCount = 0
        private set
    override var totalMessageCount = 0
        private set
    override var memberCount = 0
        private set
    override var slowmode = 0
        private set
    var rawFlags = 0
        private set

    @Nonnull
    override fun getFlags(): EnumSet<ChannelFlag> {
        return fromRaw(rawFlags)
    }

    override fun canTalk(@Nonnull member: Member?): Boolean {
        Checks.notNull(member, "Member")
        return if (type == ChannelType.GUILD_PRIVATE_THREAD && threadMemberView[member!!.idLong] == null) member.hasPermission(
            getParentChannel(),
            Permission.MANAGE_THREADS,
            Permission.MESSAGE_SEND_IN_THREADS
        ) else member!!.hasPermission(
            getParentChannel(),
            Permission.VIEW_CHANNEL,
            Permission.MESSAGE_SEND_IN_THREADS
        )
    }

    @get:Nonnull
    override val members: List<Member>
        get() = emptyList()

    @Nonnull
    fun getParentChannel(): IThreadContainerUnion? {
        val realChannel: IThreadContainer =
            getGuild().getChannelById<IThreadContainer>(IThreadContainer::class.java, parentChannel!!.idLong)
        if (realChannel != null) parentChannel = realChannel as IThreadContainerUnion
        return parentChannel
    }

    @Nonnull
    fun getAppliedTags(): List<ForumTag> {
        val parent = getParentChannel()
        return if (parent!!.type !== ChannelType.FORUM) emptyList<ForumTag>() else parent!!.asForumChannel()!!.availableTagCache
            .stream()
            .filter { tag -> appliedTagsSet.contains(tag.getIdLong()) }
            .collect(Helpers.toUnmodifiableList())
    }

    @Nonnull
    override fun retrieveParentMessage(): RestAction<Message?>? {
        return parentMessageChannel.retrieveMessageById(this.getIdLong())
    }

    @Nonnull
    override fun retrieveStartMessage(): RestAction<Message?>? {
        return retrieveMessageById(id)
    }

    @get:Nonnull
    override val permissionContainer: IPermissionContainer?
        get() = getParentChannel()

    @Nonnull
    fun getThreadMembers(): List<ThreadMember?> {
        return threadMemberView.asList()
    }

    override fun getThreadMemberById(id: Long): ThreadMember? {
        return threadMemberView[id]
    }

    @Nonnull
    override fun retrieveThreadMemberById(id: Long): CacheRestAction<ThreadMember?>? {
        val jda = jda as JDAImpl
        return DeferredRestAction<ThreadMember?, RestActionImpl<ThreadMember?>>(jda, ThreadMember::class.java,
            { getThreadMemberById(id) }
        ) {
            val route = Route.Channels.GET_THREAD_MEMBER.compile(id, java.lang.Long.toUnsignedString(id))
                .withQueryParams("with_member", "true")
            RestActionImpl<ThreadMember?>(
                jda,
                route
            ) { resp: Response, req: Request<ThreadMember?>? ->
                jda.entityBuilder.createThreadMember(
                    getGuild(),
                    this,
                    resp.`object`
                )
            }
        }
    }

    @Nonnull
    override fun retrieveThreadMembers(): ThreadMemberPaginationAction? {
        return ThreadMemberPaginationActionImpl(this)
    }

    fun isInvitable(): Boolean {
        if (type != ChannelType.GUILD_PRIVATE_THREAD) throw UnsupportedOperationException("Only private threads support the concept of invitable.")
        return invitable
    }

    @get:Nonnull
    override val timeArchiveInfoLastModified: OffsetDateTime
        get() = Helpers.toOffset(archiveTimestamp)

    @Nonnull
    override fun getTimeCreated(): OffsetDateTime {
        return if (creationTimestamp == 0L) getTimeCreated(getIdLong()) else Helpers.toOffset(creationTimestamp)
    }

    @Nonnull
    override fun join(): RestAction<Void?>? {
        checkUnarchived()
        val route = Route.Channels.JOIN_THREAD.compile(id)
        return RestActionImpl(api, route)
    }

    @Nonnull
    override fun leave(): RestAction<Void?>? {
        checkUnarchived()
        val route = Route.Channels.LEAVE_THREAD.compile(id)
        return RestActionImpl(api, route)
    }

    @Nonnull
    override fun addThreadMemberById(id: Long): RestAction<Void?>? {
        checkUnarchived()
        checkInvitable()
        checkPermission(Permission.MESSAGE_SEND_IN_THREADS)
        val route = Route.Channels.ADD_THREAD_MEMBER.compile(id, java.lang.Long.toUnsignedString(id))
        return RestActionImpl(api, route)
    }

    @Nonnull
    override fun removeThreadMemberById(id: Long): RestAction<Void?>? {
        checkUnarchived()
        val privateThreadOwner = type == ChannelType.GUILD_PRIVATE_THREAD && ownerIdLong == api.getSelfUser().idLong
        if (!privateThreadOwner) checkPermission(Permission.MANAGE_THREADS)
        val route = Route.Channels.REMOVE_THREAD_MEMBER.compile(id, java.lang.Long.toUnsignedString(id))
        return RestActionImpl(api, route)
    }

    @get:Nonnull
    override val manager: ThreadChannelManager
        get() = ThreadChannelManagerImpl(this)

    override fun checkCanManage() {
        if (isOwner) return
        checkPermission(Permission.MANAGE_THREADS)
    }

    override fun setLatestMessageIdLong(latestMessageId: Long): ThreadChannelImpl {
        latestMessageIdLong = latestMessageId
        return this
    }

    fun setAutoArchiveDuration(autoArchiveDuration: AutoArchiveDuration?): ThreadChannelImpl {
        this.autoArchiveDuration = autoArchiveDuration
        return this
    }

    fun setParentChannel(channel: IThreadContainer?): ThreadChannelImpl {
        parentChannel = channel as IThreadContainerUnion?
        return this
    }

    fun setLocked(locked: Boolean): ThreadChannelImpl {
        isLocked = locked
        return this
    }

    fun setArchived(archived: Boolean): ThreadChannelImpl {
        isArchived = archived
        return this
    }

    fun setInvitable(invitable: Boolean): ThreadChannelImpl {
        this.invitable = invitable
        return this
    }

    fun setArchiveTimestamp(archiveTimestamp: Long): ThreadChannelImpl {
        this.archiveTimestamp = archiveTimestamp
        return this
    }

    fun setCreationTimestamp(creationTimestamp: Long): ThreadChannelImpl {
        this.creationTimestamp = creationTimestamp
        return this
    }

    fun setOwnerId(ownerId: Long): ThreadChannelImpl {
        ownerIdLong = ownerId
        return this
    }

    fun setMessageCount(messageCount: Int): ThreadChannelImpl {
        this.messageCount = messageCount
        return this
    }

    fun setTotalMessageCount(messageCount: Int): ThreadChannelImpl {
        totalMessageCount =
            max(messageCount.toDouble(), this.messageCount.toDouble()).toInt() // If this is 0 we use the older count
        return this
    }

    fun setMemberCount(memberCount: Int): ThreadChannelImpl {
        this.memberCount = memberCount
        return this
    }

    override fun setSlowmode(slowmode: Int): ThreadChannelImpl {
        this.slowmode = slowmode
        return this
    }

    fun setAppliedTags(tags: LongStream): ThreadChannelImpl {
        val set: TLongSet = TLongHashSet(ForumChannel.MAX_POST_TAGS)
        tags.forEach { entry: Long -> set.add(entry) }
        appliedTagsSet = set
        return this
    }

    fun setFlags(flags: Int): ThreadChannelImpl {
        rawFlags = flags
        return this
    }

    private fun checkUnarchived() {
        check(!isArchived) { "Cannot modify a ThreadChannel while it is archived!" }
    }

    private fun checkInvitable() {
        if (ownerIdLong == api.getSelfUser().idLong) return
        if (!isPublic && !isInvitable()) checkPermission(Permission.MANAGE_THREADS)
    }
}
