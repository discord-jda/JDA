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
package net.dv8tion.jda.api.entities.templates

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.channel.ChannelType
import java.time.OffsetDateTime
import java.util.*
import javax.annotation.Nonnull

/**
 * POJO for the channels information provided by a template.
 *
 * @see TemplateGuild.getChannels
 */
class TemplateChannel(
    /**
     * The ids of channels are their position as stored by Discord so this will not look like a typical snowflake.
     *
     * @return The id of the channel as stored by Discord
     */
    override val idLong: Long,
    /**
     * The [ChannelType] for this TemplateChannel
     *
     * @return The channel type
     */
    @get:Nonnull val type: ChannelType,
    /**
     * The human readable name of the  GuildChannel.
     * <br></br>If no name has been set, this returns null.
     *
     * @return The name of this GuildChannel
     */
    @get:Nonnull val name: String, private val topic: String,
    /**
     * The actual position of the [TemplateChannel][net.dv8tion.jda.api.entities.templates.TemplateChannel] as stored and given by Discord.
     * Channel positions are actually based on a pairing of the creation time (as stored in the snowflake id)
     * and the position. If 2 or more channels share the same position then they are sorted based on their creation date.
     * The more recent a channel was created, the lower it is in the hierarchy.
     *
     * @return The true, Discord stored, position of the [TemplateChannel][net.dv8tion.jda.api.entities.templates.TemplateChannel].
     */
    val positionRaw: Int,
    /**
     * Parent Category id of this TemplateChannel. Channels don't need to have a parent Category.
     * <br></br>Note that a Category channel will always return `-1` for this method
     * as nested categories are not supported.
     *
     * @return The id of the parent Category or `-1` if the channel doesn't have a parent Category
     */
    val parentId: Long,
    /**
     * Whether or not this channel is considered an Announcement-/News-Channel.
     * <br></br>These channels can be used to crosspost messages to other guilds by using a follower type webhook.
     *
     * @return True, if this is considered a news channel
     */
    val isNews: Boolean, permissionOverrides: List<PermissionOverride>?,
    /**
     * Whether or not this channel is considered as "NSFW" (Not-Safe-For-Work).
     * <br></br>If the [ChannelType]
     * **is not [TEXT][ChannelType.TEXT]**, this returns `false`.
     *
     * @return Whether this TextChannel is considered NSFW or `false` if the channel is not a text channel
     */
    // text only properties
    val isNSFW: Boolean,
    /**
     * The slowmode set for this TemplateChannel.
     * <br></br>If slowmode is set this returns an `int` between 1 and [TextChannel.MAX_SLOWMODE][net.dv8tion.jda.api.entities.channel.concrete.TextChannel.MAX_SLOWMODE].
     * <br></br>If not set this returns `0`.
     *
     *
     * Note bots are unaffected by this.
     * <br></br>Having [MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] or
     * [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL] permission also
     * grants immunity to slowmode.
     *
     * @return The slowmode for this TextChannel, between 1 and [TextChannel.MAX_SLOWMODE][net.dv8tion.jda.api.entities.channel.concrete.TextChannel.MAX_SLOWMODE], `0` if no slowmode is set.
     */
    val slowmode: Int,
    /**
     * The audio bitrate of the voice audio that is transmitted in this channel. While higher bitrates can be sent to
     * this channel, it will be scaled down by the client.
     *
     * <br></br>Default and recommended value is 64000
     *
     * @return The audio bitrate of this voice channel
     */
    // voice only properties
    val bitrate: Int,
    /**
     * The maximum amount of [Members][net.dv8tion.jda.api.entities.Member] that can be in this
     * voice channel at once.
     *
     * <br></br>0 - No limit
     *
     * @return The maximum amount of members allowed in this channel at once.
     */
    val userLimit: Int
) : ISnowflake {

    /**
     * Gets all of the [PermissionOverrides][net.dv8tion.jda.api.entities.templates.TemplateChannel.PermissionOverride] that are part
     * of this [TemplateChannel][net.dv8tion.jda.api.entities.templates.TemplateChannel].
     * <br></br>**This will only contain [Role][net.dv8tion.jda.api.entities.templates.TemplateRole] overrides.**
     *
     * @return Immutable list of all [PermissionOverrides][net.dv8tion.jda.api.entities.templates.TemplateChannel.PermissionOverride]
     * for this [TemplateChannel][net.dv8tion.jda.api.entities.templates.TemplateChannel].
     */
    @get:Nonnull
    val permissionOverrides: List<PermissionOverride>

    init {
        this.permissionOverrides = Collections.unmodifiableList(permissionOverrides)
    }

    override val timeCreated: OffsetDateTime?
        /**
         * As the ids of channels are their position, the date of creation cannot be calculated.
         *
         * @throws java.lang.UnsupportedOperationException
         * The date of creation cannot be calculated.
         */
        get() {
            throw UnsupportedOperationException("The date of creation cannot be calculated")
        }

    /**
     * The topic set for this TemplateChannel.
     * <br></br>If no topic has been set or the [ChannelType]
     * **is not [TEXT][ChannelType.TEXT]**, this returns `null`.
     *
     * @return Possibly-null String containing the topic of this TemplateChannel.
     */
    fun getTopic(): String? {
        return topic
    }

    /**
     * Represents the specific [Role][net.dv8tion.jda.api.entities.templates.TemplateRole]
     * permission overrides that can be set for channels.
     *
     * @see TemplateChannel.getPermissionOverrides
     */
    class PermissionOverride(
        private override val id: Long,
        /**
         * This is the raw binary representation (as a base 10 long) of the permissions **allowed** by this override.
         * <br></br>The long relates to the offsets used by each [Permission][net.dv8tion.jda.api.Permission].
         *
         * @return Never-negative long containing the binary representation of the allowed permissions of this override.
         */
        val allowedRaw: Long,
        /**
         * This is the raw binary representation (as a base 10 long) of the permissions **denied** by this override.
         * <br></br>The long relates to the offsets used by each [Permission][net.dv8tion.jda.api.Permission].
         *
         * @return Never-negative long containing the binary representation of the denied permissions of this override.
         */
        val deniedRaw: Long
    ) : ISnowflake {

        val inheritRaw: Long
            /**
             * This is the raw binary representation (as a base 10 long) of the permissions **not affected** by this override.
             * <br></br>The long relates to the offsets used by each [Permission][net.dv8tion.jda.api.Permission].
             *
             * @return Never-negative long containing the binary representation of the unaffected permissions of this override.
             */
            get() = (allowedRaw or deniedRaw).inv()

        @get:Nonnull
        val allowed: EnumSet<Permission>
            /**
             * EnumSet of all [Permissions][net.dv8tion.jda.api.Permission] that are specifically allowed by this override.
             * <br></br><u>Changes to the returned set do not affect this entity directly.</u>
             *
             * @return Possibly-empty set of allowed [Permissions][net.dv8tion.jda.api.Permission].
             */
            get() = Permission.getPermissions(allowedRaw)

        @get:Nonnull
        val inherit: EnumSet<Permission>
            /**
             * EnumSet of all [Permission][net.dv8tion.jda.api.Permission] that are unaffected by this override.
             * <br></br><u>Changes to the returned set do not affect this entity directly.</u>
             *
             * @return Possibly-empty set of unaffected [Permissions][net.dv8tion.jda.api.Permission].
             */
            get() = Permission.getPermissions(inheritRaw)

        @get:Nonnull
        val denied: EnumSet<Permission>
            /**
             * EnumSet of all [Permissions][net.dv8tion.jda.api.Permission] that are denied by this override.
             * <br></br><u>Changes to the returned set do not affect this entity directly.</u>
             *
             * @return Possibly-empty set of denied [Permissions][net.dv8tion.jda.api.Permission].
             */
            get() = Permission.getPermissions(deniedRaw)

        /**
         * The ids of roles are their position as stored by Discord so this will not look like a typical snowflake.
         *
         * @return The id for the role this override is for
         */
        override fun getIdLong(): Long {
            return id
        }

        /**
         * As the ids of roles are their position, the date of creation cannot be calculated.
         *
         * @throws java.lang.UnsupportedOperationException
         * The date of creation cannot be calculated.
         */
        override fun getTimeCreated(): OffsetDateTime? {
            throw UnsupportedOperationException("The date of creation cannot be calculated")
        }
    }
}
