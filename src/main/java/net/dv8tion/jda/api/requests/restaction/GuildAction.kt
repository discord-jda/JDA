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

import Guild.ExplicitContentLevel
import Guild.NotificationLevel
import Guild.VerificationLevel
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.internal.requests.restaction.PermOverrideData
import net.dv8tion.jda.internal.utils.*
import java.awt.Color
import java.util.concurrent.*
import java.util.function.BooleanSupplier
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * [RestAction][net.dv8tion.jda.api.requests.RestAction] extension
 * specifically designed to allow for the creation of [Guilds][net.dv8tion.jda.api.entities.Guild].
 * <br></br>This is available to all account types but may undergo certain restrictions by Discord.
 *
 * @since  3.4.0
 *
 * @see net.dv8tion.jda.api.JDA.createGuild
 */
interface GuildAction : RestAction<Void?> {
    @Nonnull
    override fun setCheck(checks: BooleanSupplier?): GuildAction?
    @Nonnull
    override fun timeout(timeout: Long, @Nonnull unit: TimeUnit): GuildAction?
    @Nonnull
    override fun deadline(timestamp: Long): GuildAction?

    /**
     * Sets the [Icon][net.dv8tion.jda.api.entities.Icon]
     * for the resulting [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     * @param  icon
     * The [Icon][net.dv8tion.jda.api.entities.Icon] to use
     *
     * @return The current GuildAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setIcon(icon: Icon?): GuildAction?

    /**
     * Sets the name for the resulting [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     * @param  name
     * The name to use
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided name is `null`, blank or not between 2-100 characters long
     *
     * @return The current GuildAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setName(@Nonnull name: String?): GuildAction?

    /**
     * Sets the [VerificationLevel][net.dv8tion.jda.api.entities.Guild.VerificationLevel]
     * for the resulting [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     * @param  level
     * The [VerificationLevel][net.dv8tion.jda.api.entities.Guild.VerificationLevel] to use
     *
     * @return The current GuildAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setVerificationLevel(level: VerificationLevel?): GuildAction?

    /**
     * Sets the [NotificationLevel][net.dv8tion.jda.api.entities.Guild.NotificationLevel]
     * for the resulting [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     * @param  level
     * The [NotificationLevel][net.dv8tion.jda.api.entities.Guild.NotificationLevel] to use
     *
     * @return The current GuildAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setNotificationLevel(level: NotificationLevel?): GuildAction?

    /**
     * Sets the [ExplicitContentLevel][net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel]
     * for the resulting [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     * @param  level
     * The [ExplicitContentLevel][net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel] to use
     *
     * @return The current GuildAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setExplicitContentLevel(level: ExplicitContentLevel?): GuildAction?

    /**
     * Adds a [GuildChannel] to the resulting
     * Guild. This cannot be of type [CATEGORY][ChannelType.CATEGORY]!
     *
     * @param  channel
     * The [ChannelData]
     * to use for the construction of the GuildChannel
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided channel is `null`!
     *
     * @return The current GuildAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun addChannel(@Nonnull channel: ChannelData?): GuildAction?

    /**
     * Gets the [ChannelData]
     * of the specified index. The index is 0 based on insertion order of [.addChannel]!
     *
     * @param  index
     * The 0 based index of the channel
     *
     * @throws java.lang.IndexOutOfBoundsException
     * If the provided index is not in bounds
     *
     * @return The current GuildAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun getChannel(index: Int): ChannelData?

    /**
     * Removes the [ChannelData]
     * at the specified index and returns the removed object.
     *
     * @param  index
     * The index of the channel
     *
     * @throws java.lang.IndexOutOfBoundsException
     * If the index is out of bounds
     *
     * @return The removed object
     */
    @Nonnull
    @CheckReturnValue
    fun removeChannel(index: Int): ChannelData?

    /**
     * Removes the provided [ChannelData]
     * from this GuildAction if present.
     *
     * @param  data
     * The ChannelData to remove
     *
     * @return The current GuildAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun removeChannel(@Nonnull data: ChannelData?): GuildAction?

    /**
     * Creates a new [ChannelData]
     * instance and adds it to this GuildAction.
     *
     * @param  type
     * The [ChannelType] of the resulting GuildChannel
     * <br></br>This may be of type [TEXT][ChannelType.TEXT] or [VOICE][ChannelType.VOICE]!
     * @param  name
     * The name of the channel.
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If provided with an invalid ChannelType
     *  * If the provided name is `null` or blank
     *  * If the provided name is not between 2-100 characters long
     *
     *
     * @return The new ChannelData instance
     */
    @Nonnull
    @CheckReturnValue
    fun newChannel(@Nonnull type: ChannelType?, @Nonnull name: String?): ChannelData?

    @get:CheckReturnValue
    @get:Nonnull
    val publicRole: RoleData?

    /**
     * Retrieves the [RoleData] for the
     * provided index.
     * <br></br>The public role is at the index 0 and all others are ordered by insertion order!
     *
     * @param  index
     * The index of the role
     *
     * @throws java.lang.IndexOutOfBoundsException
     * If the provided index is out of bounds
     *
     * @return RoleData of the provided index
     */
    @Nonnull
    @CheckReturnValue
    fun getRole(index: Int): RoleData?

    /**
     * Creates and add a new [RoleData] object
     * representing a Role for the resulting Guild.
     *
     *
     * This can be used in [ChannelData.addPermissionOverride(...)][GuildAction.ChannelData.addPermissionOverride].
     * <br></br>You may change any properties of this [RoleData] instance!
     *
     * @return RoleData for the new Role
     */
    @Nonnull
    @CheckReturnValue
    fun newRole(): RoleData?

    /**
     * Mutable object containing information on a [Role][net.dv8tion.jda.api.entities.Role]
     * of the resulting [Guild][net.dv8tion.jda.api.entities.Guild] that is constructed by a GuildAction instance
     *
     *
     * This may be used in [GuildAction.ChannelData.addPermissionOverride]  ChannelData.addPermissionOverride(...)}!
     */
    class RoleData(val id: Long) : SerializableData {
        protected val isPublicRole: Boolean
        protected var permissions: Long? = null
        protected var name: String? = null
        protected var color: Int? = null
        protected var position: Int? = null
        protected var mentionable: Boolean? = null
        protected var hoisted: Boolean? = null

        init {
            isPublicRole = id == 0L
        }

        /**
         * Sets the raw permission value for this Role
         *
         * @param  rawPermissions
         * Raw permission value
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        fun setPermissionsRaw(rawPermissions: Long?): RoleData {
            permissions = rawPermissions
            return this
        }

        /**
         * Adds the provided permissions to the Role
         *
         * @param  permissions
         * The permissions to add
         *
         * @throws java.lang.IllegalArgumentException
         * If any of the provided permissions is `null`
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        fun addPermissions(@Nonnull vararg permissions: Permission?): RoleData {
            Checks.notNull(permissions, "Permissions")
            for (perm in permissions) Checks.notNull(perm, "Permissions")
            if (this.permissions == null) this.permissions = 0L
            this.permissions = this.permissions!! or Permission.getRaw(*permissions)
            return this
        }

        /**
         * Adds the provided permissions to the Role
         *
         * @param  permissions
         * The permissions to add
         *
         * @throws java.lang.IllegalArgumentException
         * If any of the provided permissions is `null`
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        fun addPermissions(@Nonnull permissions: Collection<Permission?>?): RoleData {
            Checks.noneNull(permissions, "Permissions")
            if (this.permissions == null) this.permissions = 0L
            this.permissions = this.permissions!! or Permission.getRaw(permissions!!)
            return this
        }

        /**
         * Sets the name for this Role
         *
         * @param  name
         * The name
         *
         * @throws java.lang.IllegalStateException
         * If this is the public role
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        fun setName(name: String?): RoleData {
            checkPublic("name")
            this.name = name
            return this
        }

        /**
         * Sets the color for this Role
         *
         * @param  color
         * The color for this Role
         *
         * @throws java.lang.IllegalStateException
         * If this is the public role
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        fun setColor(color: Color?): RoleData {
            checkPublic("color")
            this.color = color?.rgb
            return this
        }

        /**
         * Sets the color for this Role
         *
         * @param  color
         * The color for this Role, or `null` to unset
         *
         * @throws java.lang.IllegalStateException
         * If this is the public role
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        fun setColor(color: Int?): RoleData {
            checkPublic("color")
            this.color = color
            return this
        }

        /**
         * Sets the position for this Role
         *
         * @param  position
         * The position
         *
         * @throws java.lang.IllegalStateException
         * If this is the public role
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        fun setPosition(position: Int?): RoleData {
            checkPublic("position")
            this.position = position
            return this
        }

        /**
         * Sets whether the Role is mentionable
         *
         * @param  mentionable
         * Whether the role is mentionable
         *
         * @throws java.lang.IllegalStateException
         * If this is the public role
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        fun setMentionable(mentionable: Boolean?): RoleData {
            checkPublic("mentionable")
            this.mentionable = mentionable
            return this
        }

        /**
         * Sets whether the Role is hoisted
         *
         * @param  hoisted
         * Whether the role is hoisted
         *
         * @throws java.lang.IllegalStateException
         * If this is the public role
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        fun setHoisted(hoisted: Boolean?): RoleData {
            checkPublic("hoisted")
            this.hoisted = hoisted
            return this
        }

        @Nonnull
        override fun toData(): DataObject {
            val o = DataObject.empty().put("id", java.lang.Long.toUnsignedString(id))
            if (permissions != null) o.put("permissions", permissions)
            if (position != null) o.put("position", position)
            if (name != null) o.put("name", name)
            if (color != null) o.put("color", color!! and 0xFFFFFF)
            if (mentionable != null) o.put("mentionable", mentionable)
            if (hoisted != null) o.put("hoist", hoisted)
            return o
        }

        protected fun checkPublic(comment: String) {
            check(!isPublicRole) { "Cannot modify $comment for the public role!" }
        }
    }

    /**
     * GuildChannel information used for the creation of [Channels][GuildChannel] within
     * the construction of a [Guild][net.dv8tion.jda.api.entities.Guild] via GuildAction.
     *
     *
     * Use with [GuildAction.addChannel(ChannelData)][.addChannel].
     */
    class ChannelData(type: ChannelType, name: String) : SerializableData {
        protected val type: ChannelType
        protected val name: String
        protected val overrides: MutableSet<PermOverrideData> = HashSet()
        protected var position: Int? = null

        // Text only
        protected var topic: String? = null
        protected var nsfw: Boolean? = null

        // Voice only
        protected var bitrate: Int? = null
        protected var userlimit: Int? = null

        /**
         * Constructs a data object containing information on
         * a [GuildChannel] to be used in the construction
         * of a [Guild][net.dv8tion.jda.api.entities.Guild]!
         *
         * @param  type
         * The [ChannelType] of the resulting GuildChannel
         * <br></br>This may be of type [TEXT][ChannelType.TEXT] or [VOICE][ChannelType.VOICE]!
         * @param  name
         * The name of the channel.
         *
         * @throws java.lang.IllegalArgumentException
         *
         *  * If provided with an invalid ChannelType
         *  * If the provided name is `null` or blank
         *  * If the provided name is not between 2-100 characters long
         *
         */
        init {
            Checks.notBlank(name, "Name")
            Checks.check(
                type == ChannelType.TEXT || type == ChannelType.VOICE || type == ChannelType.STAGE,
                "Can only create channels of type TEXT, STAGE, or VOICE in GuildAction!"
            )
            Checks.check(
                name.length >= 2 && name.length <= 100,
                "Channel name has to be between 2-100 characters long!"
            )
            this.type = type
            this.name = name
        }

        /**
         * Sets the topic for this channel.
         * <br></br>These are only relevant to channels of type [TEXT][ChannelType.TEXT].
         *
         * @param  topic
         * The topic for the channel
         *
         * @throws java.lang.IllegalArgumentException
         * If the provided topic is bigger than 1024 characters
         *
         * @return This ChannelData instance for chaining convenience
         */
        @Nonnull
        fun setTopic(topic: String?): ChannelData {
            require(!(topic != null && topic.length > 1024)) { "Channel Topic must not be greater than 1024 in length!" }
            this.topic = topic
            return this
        }

        /**
         * Sets the whether this channel should be marked NSFW.
         * <br></br>These are only relevant to channels of type [TEXT][ChannelType.TEXT].
         *
         * @param  nsfw
         * Whether this channel should be marked NSFW
         *
         * @return This ChannelData instance for chaining convenience
         */
        @Nonnull
        fun setNSFW(nsfw: Boolean?): ChannelData {
            this.nsfw = nsfw
            return this
        }

        /**
         * Sets the bitrate for this channel.
         * <br></br>These are only relevant to channels of type [VOICE][ChannelType.VOICE].
         *
         * @param  bitrate
         * The bitrate for the channel (8000-96000)
         *
         * @throws java.lang.IllegalArgumentException
         * If the provided bitrate is not between 8000-96000
         *
         * @return This ChannelData instance for chaining convenience
         */
        @Nonnull
        fun setBitrate(bitrate: Int?): ChannelData {
            if (bitrate != null) {
                Checks.check(bitrate >= 8000, "Bitrate must be greater than 8000.")
                Checks.check(bitrate <= 96000, "Bitrate must be less than 96000.")
            }
            this.bitrate = bitrate
            return this
        }

        /**
         * Sets the userlimit for this channel.
         * <br></br>These are only relevant to channels of type [VOICE][ChannelType.VOICE].
         *
         * @param  userlimit
         * The userlimit for the channel (0-99)
         *
         * @throws java.lang.IllegalArgumentException
         * If the provided userlimit is not between 0-99
         *
         * @return This ChannelData instance for chaining convenience
         */
        @Nonnull
        fun setUserlimit(userlimit: Int?): ChannelData {
            require(!(userlimit != null && (userlimit < 0 || userlimit > 99))) { "Userlimit must be between 0-99!" }
            this.userlimit = userlimit
            return this
        }

        /**
         * Sets the position for this channel.
         *
         * @param  position
         * The position for the channel
         *
         * @return This ChannelData instance for chaining convenience
         */
        @Nonnull
        fun setPosition(position: Int?): ChannelData {
            this.position = position
            return this
        }

        /**
         * Adds a [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride] to this channel
         * with the provided [RoleData]!
         * <br></br>Use [GuildAction.newRole()][.newRole] to retrieve an instance of RoleData.
         *
         * @param  role
         * The target role
         * @param  allow
         * The permissions to grant in the override
         * @param  deny
         * The permissions to deny in the override
         *
         * @throws java.lang.IllegalArgumentException
         * If the provided role is `null`
         *
         * @return This ChannelData instance for chaining convenience
         */
        @Nonnull
        fun addPermissionOverride(@Nonnull role: RoleData, allow: Long, deny: Long): ChannelData {
            Checks.notNull(role, "Role")
            overrides.add(PermOverrideData(PermOverrideData.ROLE_TYPE, role.id, allow, deny))
            return this
        }

        /**
         * Adds a [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride] to this channel
         * with the provided [RoleData][GuildAction.RoleData]!
         * <br></br>Use [GuildAction.newRole()][.newRole] to retrieve an instance of RoleData.
         *
         * @param  role
         * The target role
         * @param  allow
         * The permissions to grant in the override
         * @param  deny
         * The permissions to deny in the override
         *
         * @throws java.lang.IllegalArgumentException
         *
         *  * If the provided role is `null`
         *  * If any permission is `null`
         *
         *
         * @return This ChannelData instance for chaining convenience
         */
        @Nonnull
        fun addPermissionOverride(
            @Nonnull role: RoleData,
            allow: Collection<Permission?>?,
            deny: Collection<Permission?>?
        ): ChannelData {
            var allowRaw: Long = 0
            var denyRaw: Long = 0
            if (allow != null) {
                Checks.noneNull(allow, "Granted Permissions")
                allowRaw = Permission.getRaw(allow)
            }
            if (deny != null) {
                Checks.noneNull(deny, "Denied Permissions")
                denyRaw = Permission.getRaw(deny)
            }
            return addPermissionOverride(role, allowRaw, denyRaw)
        }

        @Nonnull
        override fun toData(): DataObject {
            val o = DataObject.empty()
            o.put("name", name)
            o.put("type", type.id)
            if (topic != null) o.put("topic", topic)
            if (nsfw != null) o.put("nsfw", nsfw)
            if (bitrate != null) o.put("bitrate", bitrate)
            if (userlimit != null) o.put("user_limit", userlimit)
            if (position != null) o.put("position", position)
            if (!overrides.isEmpty()) o.put("permission_overwrites", overrides)
            return o
        }
    }
}
