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
package net.dv8tion.jda.api;

import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

/**
 * Represents the bit offsets used by Discord for Permissions.
 */
public enum Permission
{
    // General Server / Channel Permissions
    MANAGE_CHANNEL(                      4, true,  true,  "Manage Channels"),
    MANAGE_SERVER(                       5, true,  false, "Manage Server"),
    VIEW_AUDIT_LOGS(                     7, true,  false, "View Audit Log"),
    VIEW_CHANNEL(                       10, true,  true,  "View Channels"),
    VIEW_GUILD_INSIGHTS(                19, true,  false, "View Server Insights"),
    MANAGE_ROLES(                       28, true,  false, "Manage Roles"),
    MANAGE_PERMISSIONS(                 28, false, true,  "Manage Permissions"),
    MANAGE_WEBHOOKS(                    29, true,  true,  "Manage Webhooks"),
    MANAGE_GUILD_EXPRESSIONS(           30, true,  false, "Manage Expressions"),
    MANAGE_EVENTS(                      33, true,  true,  "Manage Events"),
    USE_EMBEDDED_ACTIVITIES(            39, true,  true,  "Use Activities"),
    VIEW_CREATOR_MONETIZATION_ANALYTICS(41, true,  false, "View Creator Analytics"),
    CREATE_GUILD_EXPRESSIONS(           43, true,  false, "Create Expressions"),
    CREATE_SCHEDULED_EVENTS(            44, true,  false, "Create Events"),

    // Membership Permissions
    CREATE_INSTANT_INVITE(0, true, true,  "Create Instant Invite"),
    KICK_MEMBERS(         1, true, false, "Kick Members"),
    BAN_MEMBERS(          2, true, false, "Ban Members"),
    NICKNAME_CHANGE(     26, true, false, "Change Nickname"),
    NICKNAME_MANAGE(     27, true, false, "Manage Nicknames"),
    MODERATE_MEMBERS(    40, true, false, "Timeout Members"),

    // Text Permissions
    MESSAGE_ADD_REACTION(         6, true, true, "Add Reactions"),
    MESSAGE_SEND(                11, true, true, "Send Messages"),
    MESSAGE_TTS(                 12, true, true, "Send TTS Messages"),
    MESSAGE_MANAGE(              13, true, true, "Manage Messages"),
    MESSAGE_EMBED_LINKS(         14, true, true, "Embed Links"),
    MESSAGE_ATTACH_FILES(        15, true, true, "Attach Files"),
    MESSAGE_HISTORY(             16, true, true, "Read History"),
    MESSAGE_MENTION_EVERYONE(    17, true, true, "Mention Everyone"),
    MESSAGE_EXT_EMOJI(           18, true, true, "Use External Emojis"),
    USE_APPLICATION_COMMANDS(    31, true, true, "Use Application Commands"),
    MESSAGE_EXT_STICKER(         37, true, true, "Use External Stickers"),
    MESSAGE_ATTACH_VOICE_MESSAGE(46, true, true, "Send Voice Messages"),
    MESSAGE_SEND_POLLS(          49, true, true, "Create Polls"),
    USE_EXTERNAL_APPLICATIONS(   50, true, true, "Use External Apps"),

    // Thread Permissions
    MANAGE_THREADS(          34, true, true, "Manage Threads"),
    CREATE_PUBLIC_THREADS(   35, true, true, "Create Public Threads"),
    CREATE_PRIVATE_THREADS(  36, true, true, "Create Private Threads"),
    MESSAGE_SEND_IN_THREADS( 38, true, true, "Send Messages in Threads"),

    // Voice Permissions
    PRIORITY_SPEAKER(          8, true, true, "Priority Speaker"),
    VOICE_STREAM(              9, true, true, "Video"),
    VOICE_CONNECT(            20, true, true, "Connect"),
    VOICE_SPEAK(              21, true, true, "Speak"),
    VOICE_MUTE_OTHERS(        22, true, true, "Mute Members"),
    VOICE_DEAF_OTHERS(        23, true, true, "Deafen Members"),
    VOICE_MOVE_OTHERS(        24, true, true, "Move Members"),
    VOICE_USE_VAD(            25, true, true, "Use Voice Activity"),
    @Deprecated
    @ForRemoval(deadline="5.1.0")
    @ReplaceWith("USE_EMBEDDED_ACTIVITIES")
    VOICE_START_ACTIVITIES(   39, true, true, "Use Activities"),
    VOICE_USE_SOUNDBOARD(     42, true, true, "Use Soundboard"),
    VOICE_USE_EXTERNAL_SOUNDS(45, true, true, "Use External Sounds"),
    VOICE_SET_STATUS(         48, true, true, "Set Voice Channel Status"),

    // Stage Channel Permissions
    REQUEST_TO_SPEAK(      32, true, true, "Request to Speak"),

    // Advanced Permissions
    ADMINISTRATOR(3, true, false, "Administrator"),


    UNKNOWN(-1, false, false, "Unknown");

    /**
     * Empty array of Permission enum, useful for optimized use in {@link java.util.Collection#toArray(Object[])}.
     */
    // This is an optimization suggested by Effective Java 3rd Edition - Item 54
    public static final Permission[] EMPTY_PERMISSIONS = new Permission[0];

    /**
     * Represents a raw set of all permissions
     *
     * @deprecated To be removed in 5.1.0
     */
    @Deprecated
    @ForRemoval(deadline = "5.1.0")
    public static final long ALL_PERMISSIONS = Permission.getRaw(Permission.values());

    /**
     * All permissions that apply to a channel
     *
     * @deprecated To be removed in 5.1.0 (use {@link Permission#isChannel()} instead)
     */
    @Deprecated
    @ForRemoval(deadline = "5.1.0")
    public static final long ALL_CHANNEL_PERMISSIONS = Permission.getRaw(Arrays.stream(values())
            .filter(Permission::isChannel).collect(Collectors.toSet()));

    /**
     * All Guild specific permissions which are only available to roles
     *
     * @deprecated To be removed in 5.1.0 (use {@link Permission#isGuild()} instead)
     */
    @Deprecated
    @ForRemoval(deadline = "5.1.0")
    public static final long ALL_GUILD_PERMISSIONS = Permission.getRaw(Arrays.stream(values())
            .filter(Permission::isGuild).collect(Collectors.toSet()));

    /**
     * All text channel specific permissions which are only available in text channel permission overrides
     *
     * @deprecated To be removed in 5.1.0
     */
    @Deprecated
    @ForRemoval(deadline = "5.1.0")
    public static final long ALL_TEXT_PERMISSIONS
            = Permission.getRaw(MESSAGE_ADD_REACTION, MESSAGE_SEND, MESSAGE_TTS, MESSAGE_MANAGE,
                                MESSAGE_EMBED_LINKS, MESSAGE_ATTACH_FILES, MESSAGE_EXT_EMOJI, MESSAGE_EXT_STICKER,
                                MESSAGE_HISTORY, MESSAGE_MENTION_EVERYONE,
                                USE_APPLICATION_COMMANDS, USE_EXTERNAL_APPLICATIONS, USE_EMBEDDED_ACTIVITIES, USE_EXTERNAL_APPLICATIONS,
                                MANAGE_THREADS, CREATE_PUBLIC_THREADS, CREATE_PRIVATE_THREADS,
                                MESSAGE_SEND_IN_THREADS, MESSAGE_ATTACH_VOICE_MESSAGE, MESSAGE_SEND_POLLS);

    /**
     * All voice channel specific permissions which are only available in voice channel permission overrides
     *
     * @deprecated To be removed in 5.1.0
     */
    @Deprecated
    @ForRemoval(deadline = "5.1.0")
    public static final long ALL_VOICE_PERMISSIONS
            = Permission.getRaw(VOICE_STREAM, VOICE_CONNECT, VOICE_SPEAK, VOICE_MUTE_OTHERS,
                                VOICE_DEAF_OTHERS, VOICE_MOVE_OTHERS, VOICE_USE_VAD,
                                PRIORITY_SPEAKER, REQUEST_TO_SPEAK, USE_EMBEDDED_ACTIVITIES,
                                VOICE_USE_SOUNDBOARD, VOICE_USE_EXTERNAL_SOUNDS, VOICE_SET_STATUS);

    private final int offset;
    private final long raw;
    private final boolean isGuild, isChannel;
    private final String name;

    Permission(int offset, boolean isGuild, boolean isChannel, @Nonnull String name)
    {
        this.offset = offset;
        this.raw = 1L << offset;
        this.isGuild = isGuild;
        this.isChannel = isChannel;
        this.name = name;
    }

    /**
     * The readable name as used in the Discord client.
     *
     * @return The readable name of this {@link net.dv8tion.jda.api.Permission Permission}.
     */
    @Nonnull
    public String getName()
    {
        return this.name;
    }

    /**
     * The binary offset of the permission.
     * <br>For more information about Discord's offset system refer to
     * <a href="https://discord.com/developers/docs/topics/permissions">Discord Permissions</a>.
     *
     * @return The offset that represents this {@link net.dv8tion.jda.api.Permission Permission}.
     */
    public int getOffset()
    {
        return offset;
    }

    /**
     * The value of this permission when viewed as a raw value.
     * <br>This is equivalent to: <code>1 {@literal <<} {@link #getOffset()}</code>
     *
     * @return The raw value of this specific permission.
     */
    public long getRawValue()
    {
        return raw;
    }

    /**
     * Returns whether or not this Permission is present at the Guild level
     * (configurable via {@link net.dv8tion.jda.api.entities.Role Roles})
     *
     * @return True if this permission is present at the Guild level.
     */
    public boolean isGuild()
    {
        return isGuild;
    }

    /**
     * Returns whether or not this Permission is present Channel level
     * (configurable via {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionsOverrides})
     *
     * @return True if this permission is present at the Channel level.
     */
    public boolean isChannel()
    {
        return isChannel;
    }

    /**
     * Whether this permission is specifically for {@link net.dv8tion.jda.api.entities.channel.concrete.TextChannel TextChannels}
     *
     * @return True, if and only if this permission can only be applied to text channels
     *
     * @deprecated To be removed in 5.1.0
     */
    @Deprecated
    @ForRemoval(deadline = "5.1.0")
    public boolean isText()
    {
        return (raw & ALL_TEXT_PERMISSIONS) == raw;
    }

    /**
     * Whether this permission is specifically for {@link net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel VoiceChannels}
     *
     * @return True, if and only if this permission can only be applied to voice channels
     *
     * @deprecated To be removed in 5.1.0
     */
    @Deprecated
    @ForRemoval(deadline = "5.1.0")
    public boolean isVoice()
    {
        return (raw & ALL_VOICE_PERMISSIONS) == raw;
    }

    /**
     * Gets the first {@link net.dv8tion.jda.api.Permission Permission} relating to the provided offset.
     * <br>If there is no {@link net.dv8tion.jda.api.Permission Permssions} that matches the provided
     * offset, {@link net.dv8tion.jda.api.Permission#UNKNOWN Permission.UNKNOWN} is returned.
     *
     * @param  offset
     *         The offset to match a {@link net.dv8tion.jda.api.Permission Permission} to.
     *
     * @return {@link net.dv8tion.jda.api.Permission Permission} relating to the provided offset.
     */
    @Nonnull
    public static Permission getFromOffset(int offset)
    {
        for (Permission perm : values())
        {
            if (perm.offset == offset)
                return perm;
        }
        return UNKNOWN;
    }

    /**
     * A set of all {@link net.dv8tion.jda.api.Permission Permissions} that are specified by this raw long representation of
     * permissions. The is best used with the getRaw methods in {@link net.dv8tion.jda.api.entities.Role Role} or
     * {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}.
     *
     * <p>Example: {@link net.dv8tion.jda.api.entities.Role#getPermissionsRaw() Role.getPermissionsRaw()}
     *
     * @param  permissions
     *         The raw {@code long} representation of permissions.
     *
     * @return Possibly-empty EnumSet of {@link net.dv8tion.jda.api.Permission Permissions}.
     *
     */
    @Nonnull
    public static EnumSet<Permission> getPermissions(long permissions)
    {
        if (permissions == 0)
            return EnumSet.noneOf(Permission.class);
        EnumSet<Permission> perms = EnumSet.noneOf(Permission.class);
        for (Permission perm : Permission.values())
        {
            if (perm != UNKNOWN && (permissions & perm.raw) == perm.raw)
                perms.add(perm);
        }
        return perms;
    }

    /**
     * This is effectively the opposite of {@link #getPermissions(long)}, this takes 1 or more {@link net.dv8tion.jda.api.Permission Permissions}
     * and returns the raw offset {@code long} representation of the permissions.
     *
     * @param  permissions
     *         The array of permissions of which to form into the raw long representation.
     *
     * @return Unsigned long representing the provided permissions.
     */
    public static long getRaw(@Nonnull Permission... permissions)
    {
        long raw = 0;
        for (Permission perm : permissions)
        {
            if (perm != null && perm != UNKNOWN)
                raw |= perm.raw;
        }

        return raw;
    }

    /**
     * This is effectively the opposite of {@link #getPermissions(long)}, this takes a Collection of {@link net.dv8tion.jda.api.Permission Permissions}
     * and returns the raw offset {@code long} representation of the permissions.
     * <br>Example: {@code getRaw(EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND))}
     *
     * @param  permissions
     *         The Collection of permissions of which to form into the raw long representation.
     *
     * @return Unsigned long representing the provided permissions.
     *
     * @see    java.util.EnumSet EnumSet
     */
    public static long getRaw(@Nonnull Collection<Permission> permissions)
    {
        Checks.notNull(permissions, "Permission Collection");

        return getRaw(permissions.toArray(EMPTY_PERMISSIONS));
    }
}
