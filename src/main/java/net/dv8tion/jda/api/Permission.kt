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
package net.dv8tion.jda.api

import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors
import javax.annotation.Nonnull

/**
 * Represents the bit offsets used by Discord for Permissions.
 */
enum class Permission(
    /**
     * The binary offset of the permission.
     * <br></br>For more information about Discord's offset system refer to
     * [Discord Permissions](https://discord.com/developers/docs/topics/permissions).
     *
     * @return The offset that represents this [Permission][net.dv8tion.jda.api.Permission].
     */
    val offset: Int,
    /**
     * Returns whether or not this Permission is present at the Guild level
     * (configurable via [Roles][net.dv8tion.jda.api.entities.Role])
     *
     * @return True if this permission is present at the Guild level.
     */
    val isGuild: Boolean,
    /**
     * Returns whether or not this Permission is present Channel level
     * (configurable via [PermissionsOverrides][net.dv8tion.jda.api.entities.PermissionOverride])
     *
     * @return True if this permission is present at the Channel level.
     */
    val isChannel: Boolean,
    /**
     * The readable name as used in the Discord client.
     *
     * @return The readable name of this [Permission][net.dv8tion.jda.api.Permission].
     */
    @get:Nonnull
    @param:Nonnull override val name: String
) {
    // General Server / Channel Permissions
    MANAGE_CHANNEL(4, true, true, "Manage Channels"),
    MANAGE_SERVER(5, true, false, "Manage Server"),
    VIEW_AUDIT_LOGS(7, true, false, "View Audit Log"),
    VIEW_CHANNEL(10, true, true, "View Channels"),
    VIEW_GUILD_INSIGHTS(19, true, false, "View Server Insights"),
    MANAGE_ROLES(28, true, false, "Manage Roles"),
    MANAGE_PERMISSIONS(28, false, true, "Manage Permissions"),
    MANAGE_WEBHOOKS(29, true, true, "Manage Webhooks"),
    MANAGE_GUILD_EXPRESSIONS(30, true, false, "Manage Expressions"),
    MANAGE_EVENTS(33, true, true, "Manage Events"),
    VIEW_CREATOR_MONETIZATION_ANALYTICS(41, true, false, "View Creator Analytics"),
    CREATE_GUILD_EXPRESSIONS(43, true, false, "Create Expressions"),
    CREATE_SCHEDULED_EVENTS(44, true, false, "Create Events"),

    // Membership Permissions
    CREATE_INSTANT_INVITE(0, true, true, "Create Instant Invite"),
    KICK_MEMBERS(1, true, false, "Kick Members"),
    BAN_MEMBERS(2, true, false, "Ban Members"),
    NICKNAME_CHANGE(26, true, false, "Change Nickname"),
    NICKNAME_MANAGE(27, true, false, "Manage Nicknames"),
    MODERATE_MEMBERS(40, true, false, "Timeout Members"),

    // Text Permissions
    MESSAGE_ADD_REACTION(6, true, true, "Add Reactions"),
    MESSAGE_SEND(11, true, true, "Send Messages"),
    MESSAGE_TTS(12, true, true, "Send TTS Messages"),
    MESSAGE_MANAGE(13, true, true, "Manage Messages"),
    MESSAGE_EMBED_LINKS(14, true, true, "Embed Links"),
    MESSAGE_ATTACH_FILES(15, true, true, "Attach Files"),
    MESSAGE_HISTORY(16, true, true, "Read History"),
    MESSAGE_MENTION_EVERYONE(17, true, true, "Mention Everyone"),
    MESSAGE_EXT_EMOJI(18, true, true, "Use External Emojis"),
    USE_APPLICATION_COMMANDS(31, true, true, "Use Application Commands"),
    MESSAGE_EXT_STICKER(37, true, true, "Use External Stickers"),
    MESSAGE_ATTACH_VOICE_MESSAGE(46, true, true, "Send Voice Messages"),

    // Thread Permissions
    MANAGE_THREADS(34, true, true, "Manage Threads"),
    CREATE_PUBLIC_THREADS(35, true, true, "Create Public Threads"),
    CREATE_PRIVATE_THREADS(36, true, true, "Create Private Threads"),
    MESSAGE_SEND_IN_THREADS(38, true, true, "Send Messages in Threads"),

    // Voice Permissions
    PRIORITY_SPEAKER(8, true, true, "Priority Speaker"),
    VOICE_STREAM(9, true, true, "Video"),
    VOICE_CONNECT(20, true, true, "Connect"),
    VOICE_SPEAK(21, true, true, "Speak"),
    VOICE_MUTE_OTHERS(22, true, true, "Mute Members"),
    VOICE_DEAF_OTHERS(23, true, true, "Deafen Members"),
    VOICE_MOVE_OTHERS(24, true, true, "Move Members"),
    VOICE_USE_VAD(25, true, true, "Use Voice Activity"),
    VOICE_START_ACTIVITIES(39, true, true, "Use Activities"),
    VOICE_USE_SOUNDBOARD(42, true, true, "Use Soundboard"),
    VOICE_USE_EXTERNAL_SOUNDS(45, true, true, "Use External Sounds"),
    VOICE_SET_STATUS(48, true, true, "Set Voice Channel Status"),

    // Stage Channel Permissions
    REQUEST_TO_SPEAK(32, true, true, "Request to Speak"),

    // Advanced Permissions
    ADMINISTRATOR(3, true, false, "Administrator"),
    UNKNOWN(-1, false, false, "Unknown");

    /**
     * The value of this permission when viewed as a raw value.
     * <br></br>This is equivalent to: `1 << [.getOffset]`
     *
     * @return The raw value of this specific permission.
     */
    val rawValue: Long

    init {
        rawValue = 1L shl offset
    }

    val isText: Boolean
        /**
         * Whether this permission is specifically for [TextChannels][net.dv8tion.jda.api.entities.channel.concrete.TextChannel]
         *
         * @return True, if and only if this permission can only be applied to text channels
         */
        get() {
            return (rawValue and ALL_TEXT_PERMISSIONS) == rawValue
        }
    val isVoice: Boolean
        /**
         * Whether this permission is specifically for [VoiceChannels][net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel]
         *
         * @return True, if and only if this permission can only be applied to voice channels
         */
        get() {
            return (rawValue and ALL_VOICE_PERMISSIONS) == rawValue
        }

    companion object {
        /**
         * Empty array of Permission enum, useful for optimized use in [java.util.Collection.toArray].
         */
        // This is an optimization suggested by Effective Java 3rd Edition - Item 54
        @JvmField
        val EMPTY_PERMISSIONS: Array<Permission?> = arrayOfNulls(0)

        /**
         * Represents a raw set of all permissions
         */
        @JvmField
        val ALL_PERMISSIONS: Long = getRaw(*entries.toTypedArray())

        /**
         * All permissions that apply to a channel
         */
        @JvmField
        val ALL_CHANNEL_PERMISSIONS: Long = getRaw(Arrays.stream(entries.toTypedArray())
            .filter(Predicate({ obj: Permission -> obj.isChannel })).collect(Collectors.toSet())
        )

        /**
         * All Guild specific permissions which are only available to roles
         */
        val ALL_GUILD_PERMISSIONS: Long = getRaw(Arrays.stream(entries.toTypedArray())
            .filter(Predicate({ obj: Permission -> obj.isGuild })).collect(Collectors.toSet())
        )

        /**
         * All text channel specific permissions which are only available in text channel permission overrides
         */
        val ALL_TEXT_PERMISSIONS: Long = getRaw(
            MESSAGE_ADD_REACTION,
            MESSAGE_SEND,
            MESSAGE_TTS,
            MESSAGE_MANAGE,
            MESSAGE_EMBED_LINKS,
            MESSAGE_ATTACH_FILES,
            MESSAGE_EXT_EMOJI,
            MESSAGE_EXT_STICKER,
            MESSAGE_HISTORY,
            MESSAGE_MENTION_EVERYONE,
            USE_APPLICATION_COMMANDS,
            MANAGE_THREADS,
            CREATE_PUBLIC_THREADS,
            CREATE_PRIVATE_THREADS,
            MESSAGE_SEND_IN_THREADS,
            MESSAGE_ATTACH_VOICE_MESSAGE
        )

        /**
         * All voice channel specific permissions which are only available in voice channel permission overrides
         */
        val ALL_VOICE_PERMISSIONS: Long = getRaw(
            VOICE_STREAM,
            VOICE_CONNECT,
            VOICE_SPEAK,
            VOICE_MUTE_OTHERS,
            VOICE_DEAF_OTHERS,
            VOICE_MOVE_OTHERS,
            VOICE_USE_VAD,
            PRIORITY_SPEAKER,
            REQUEST_TO_SPEAK,
            VOICE_START_ACTIVITIES,
            VOICE_USE_SOUNDBOARD,
            VOICE_USE_EXTERNAL_SOUNDS
        )

        /**
         * Gets the first [Permission][net.dv8tion.jda.api.Permission] relating to the provided offset.
         * <br></br>If there is no [Permssions][net.dv8tion.jda.api.Permission] that matches the provided
         * offset, [Permission.UNKNOWN][net.dv8tion.jda.api.Permission.UNKNOWN] is returned.
         *
         * @param  offset
         * The offset to match a [Permission][net.dv8tion.jda.api.Permission] to.
         *
         * @return [Permission][net.dv8tion.jda.api.Permission] relating to the provided offset.
         */
        @Nonnull
        fun getFromOffset(offset: Int): Permission {
            for (perm: Permission in entries) {
                if (perm.offset == offset) return perm
            }
            return UNKNOWN
        }

        /**
         * A set of all [Permissions][net.dv8tion.jda.api.Permission] that are specified by this raw long representation of
         * permissions. The is best used with the getRaw methods in [Role][net.dv8tion.jda.api.entities.Role] or
         * [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride].
         *
         *
         * Example: [Role.getPermissionsRaw()][net.dv8tion.jda.api.entities.Role.getPermissionsRaw]
         *
         * @param  permissions
         * The raw `long` representation of permissions.
         *
         * @return Possibly-empty EnumSet of [Permissions][net.dv8tion.jda.api.Permission].
         */
        @JvmStatic
        @Nonnull
        fun getPermissions(permissions: Long): EnumSet<Permission> {
            if (permissions == 0L) return EnumSet.noneOf(Permission::class.java)
            val perms: EnumSet<Permission> = EnumSet.noneOf(Permission::class.java)
            for (perm: Permission in entries) {
                if (perm != UNKNOWN && (permissions and perm.rawValue) == perm.rawValue) perms.add(perm)
            }
            return perms
        }

        /**
         * This is effectively the opposite of [.getPermissions], this takes 1 or more [Permissions][net.dv8tion.jda.api.Permission]
         * and returns the raw offset `long` representation of the permissions.
         *
         * @param  permissions
         * The array of permissions of which to form into the raw long representation.
         *
         * @return Unsigned long representing the provided permissions.
         */
        @JvmStatic
        fun getRaw(@Nonnull vararg permissions: Permission?): Long {
            var raw: Long = 0
            for (perm: Permission? in permissions) {
                if (perm != null && perm != UNKNOWN) raw = raw or perm.rawValue
            }
            return raw
        }

        /**
         * This is effectively the opposite of [.getPermissions], this takes a Collection of [Permissions][net.dv8tion.jda.api.Permission]
         * and returns the raw offset `long` representation of the permissions.
         * <br></br>Example: `getRaw(EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND))`
         *
         * @param  permissions
         * The Collection of permissions of which to form into the raw long representation.
         *
         * @return Unsigned long representing the provided permissions.
         *
         * @see java.util.EnumSet EnumSet
         */
        fun getRaw(@Nonnull permissions: Collection<Permission>): Long {
            Checks.notNull(permissions, "Permission Collection")
            return getRaw(*permissions.toArray<Permission>(EMPTY_PERMISSIONS))
        }
    }
}
