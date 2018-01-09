/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.audit;

/**
 * ActionTypes for {@link net.dv8tion.jda.core.audit.AuditLogEntry AuditLogEntry} instances
 * <br>Found via {@link net.dv8tion.jda.core.audit.AuditLogEntry#getType() AuditLogEntry.getType()}
 */
public enum ActionType
{
    /**
     * An Administrator updated {@link net.dv8tion.jda.core.entities.Guild Guild} information.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#GUILD_AFK_CHANNEL GUILD_AFK_CHANNEL}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#GUILD_AFK_TIMEOUT GUILD_AFK_TIMEOUT}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#GUILD_EXPLICIT_CONTENT_FILTER GUILD_EXPLICIT_CONTENT_FILTER}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#GUILD_ICON GUILD_ICON}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#GUILD_MFA_LEVEL GUILD_MFA_LEVEL}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#GUILD_NAME GUILD_NAME}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#GUILD_NOTIFICATION_LEVEL GUILD_NOTIFICATION_LEVEL}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#GUILD_OWNER GUILD_OWNER}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#GUILD_REGION GUILD_REGION}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#GUILD_SPLASH GUILD_SPLASH}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#GUILD_SYSTEM_CHANNEL GUILD_SYSTEM_CHANNEL}</li>
     * </ul>
     */
    GUILD_UPDATE(1, TargetType.GUILD),


    /**
     * An Administrator created a {@link net.dv8tion.jda.core.entities.Channel Channel}
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_BITRATE CHANNEL_BITRATE} (VoiceChannel only)</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_USER_LIMIT CHANNEL_USER_LIMIT} (VoiceChannel only)</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_TOPIC CHANNEL_TOPIC} (TextChannel only)</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_NSFW CHANNEL_NSFW} (TextChannel only)</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_OVERRIDES CHANNEL_OVERRIDES}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_NAME CHANNEL_NAME}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_TYPE CHANNEL_TYPE}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ID ID}</li>
     * </ul>
     */
    CHANNEL_CREATE(10, TargetType.CHANNEL),

    /**
     * An Administrator updated {@link net.dv8tion.jda.core.entities.Channel Channel} information.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_BITRATE CHANNEL_BITRATE} (VoiceChannel only)</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_USER_LIMIT CHANNEL_USER_LIMIT} (VoiceChannel only)</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_TOPIC CHANNEL_TOPIC} (TextChannel only)</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_NSFW CHANNEL_NSFW} (TextChannel only)</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_NAME CHANNEL_NAME}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_TYPE CHANNEL_TYPE}</li>
     * </ul>
     */
    CHANNEL_UPDATE(11, TargetType.CHANNEL),

    /**
     * An Administrator deleted a {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_BITRATE CHANNEL_BITRATE} (VoiceChannel only)</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_USER_LIMIT CHANNEL_USER_LIMIT} (VoiceChannel only)</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_TOPIC CHANNEL_TOPIC} (TextChannel only)</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_NSFW CHANNEL_NSFW} (TextChannel only)</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_OVERRIDES CHANNEL_OVERRIDES}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_NAME CHANNEL_NAME}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#CHANNEL_TYPE CHANNEL_TYPE}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ID ID}</li>
     * </ul>
     */
    CHANNEL_DELETE(12, TargetType.CHANNEL),

    /**
     * An Administrator created a {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#OVERRIDE_ALLOW OVERRIDE_ALLOW}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#OVERRIDE_DENY OVERRIDE_DENY}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#OVERRIDE_TYPE OVERRIDE_TYPE}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ID ID}</li>
     * </ul>
     *
     * <h2>Possible Options</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogOption#ROLE ROLE}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogOption#USER USER}</li>
     * </ul>
     */
    CHANNEL_OVERRIDE_CREATE(13, TargetType.CHANNEL),

    /**
     * An Administrator updated {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} information.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#OVERRIDE_ALLOW OVERRIDE_ALLOW}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#OVERRIDE_DENY OVERRIDE_DENY}</li>
     * </ul>
     */
    CHANNEL_OVERRIDE_UPDATE(14, TargetType.CHANNEL),

    /**
     * An Administrator deleted a {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#OVERRIDE_ALLOW OVERRIDE_ALLOW}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#OVERRIDE_DENY OVERRIDE_DENY}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#OVERRIDE_TYPE OVERRIDE_TYPE}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ID ID}</li>
     * </ul>
     *
     * <h2>Possible Options</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogOption#ROLE ROLE}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogOption#USER USER}</li>
     * </ul>
     */
    CHANNEL_OVERRIDE_DELETE(15, TargetType.CHANNEL),


    /**
     * An Administrator has kicked a member.
     */
    KICK( 20, TargetType.MEMBER),

    /**
     * An Administrator has pruned members for inactivity.
     *
     * <h2>Possible Options</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogOption#DELETE_MEMBER_DAYS DELETE_MEMBER_DAYS}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogOption#MEMBERS_REMOVED MEMBERS_REMOVED}</li>
     * </ul>
     */
    PRUNE(21, TargetType.MEMBER),

    /**
     * An Administrator has banned a user.
     */
    BAN(  22, TargetType.MEMBER),

    /**
     * An Administrator has unbanned a user.
     */
    UNBAN(23, TargetType.MEMBER),


    /**
     * A {@link net.dv8tion.jda.core.entities.Member Member} was either updated by an administrator or
     * the member updated itself.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#MEMBER_NICK MEMBER_NICK}</li>
     * </ul>
     */
    MEMBER_UPDATE(     24, TargetType.MEMBER),

    /**
     * An Administrator updated the roles of a member.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#MEMBER_ROLES_ADD MEMBER_ROLES_ADD}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#MEMBER_ROLES_REMOVE MEMBER_ROLES_REMOVE}</li>
     * </ul>
     */
    MEMBER_ROLE_UPDATE(25, TargetType.MEMBER),


    /**
     * An Administrator has created a {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ROLE_COLOR ROLE_COLOR}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ROLE_HOISTED ROLE_HOISTED}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ROLE_MENTIONABLE ROLE_MENTIONABLE}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ROLE_NAME ROLE_NAME}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ROLE_PERMISSIONS ROLE_PERMISSIONS}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ID ID}</li>
     * </ul>
     */
    ROLE_CREATE(30, TargetType.ROLE),

    /**
     * An Administrator has updated a {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ROLE_COLOR ROLE_COLOR}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ROLE_HOISTED ROLE_HOISTED}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ROLE_MENTIONABLE ROLE_MENTIONABLE}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ROLE_NAME ROLE_NAME}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ROLE_PERMISSIONS ROLE_PERMISSIONS}</li>
     * </ul>
     */
    ROLE_UPDATE(31, TargetType.ROLE),

    /**
     * An Administrator has deleted a {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ROLE_COLOR ROLE_COLOR}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ROLE_HOISTED ROLE_HOISTED}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ROLE_MENTIONABLE ROLE_MENTIONABLE}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ROLE_NAME ROLE_NAME}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ROLE_PERMISSIONS ROLE_PERMISSIONS}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ID ID}</li>
     * </ul>
     */
    ROLE_DELETE(32, TargetType.ROLE),


    /**
     * Someone has created an {@link net.dv8tion.jda.core.entities.Invite Invite}.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#INVITE_CHANNEL INVITE_CHANNEL}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#INVITE_CODE INVITE_CODE}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#INVITE_INVITER INVITE_INVITER}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#INVITE_MAX_AGE INVITE_MAX_AGE}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#INVITE_MAX_USES INVITE_MAX_USES}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#INVITE_USES INVITE_USES}</li>
     * </ul>
     */
    INVITE_CREATE(40, TargetType.INVITE),

    /**
     * An {@link net.dv8tion.jda.core.entities.Invite Invite} has been updated.
     */
    INVITE_UPDATE(41, TargetType.INVITE),

    /**
     * An Administrator has deleted an {@link net.dv8tion.jda.core.entities.Invite Invite}.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#INVITE_CHANNEL INVITE_CHANNEL}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#INVITE_CODE INVITE_CODE}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#INVITE_INVITER INVITE_INVITER}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#INVITE_MAX_AGE INVITE_MAX_AGE}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#INVITE_MAX_USES INVITE_MAX_USES}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#INVITE_USES INVITE_USES}</li>
     * </ul>
     */
    INVITE_DELETE(42, TargetType.INVITE),


    /**
     * An Administrator has created a {@link net.dv8tion.jda.core.entities.Webhook Webhook}.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#WEBHOOK_CHANNEL WEBHOOK_CHANNEL}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#WEBHOOK_ICON WEBHOOK_ICON}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#WEBHOOK_NAME WEBHOOK_NAME}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ID ID}</li>
     * </ul>
     */
    WEBHOOK_CREATE(50, TargetType.WEBHOOK),

    /**
     * An Administrator has updated a {@link net.dv8tion.jda.core.entities.Webhook Webhook}.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#WEBHOOK_CHANNEL WEBHOOK_CHANNEL}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#WEBHOOK_ICON WEBHOOK_ICON}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#WEBHOOK_NAME WEBHOOK_NAME}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ID ID}</li>
     * </ul>
     */
    WEBHOOK_UPDATE(51, TargetType.WEBHOOK),

    /**
     * An Administrator has deleted a {@link net.dv8tion.jda.core.entities.Webhook Webhook}.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#WEBHOOK_CHANNEL WEBHOOK_CHANNEL}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#WEBHOOK_ICON WEBHOOK_ICON}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#WEBHOOK_NAME WEBHOOK_NAME}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#ID ID}</li>
     * </ul>
     */
    WEBHOOK_REMOVE(52, TargetType.WEBHOOK),


    /**
     * An Administrator created an {@link net.dv8tion.jda.core.entities.Emote Emote}.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#EMOTE_NAME EMOTE_NAME}</li>
     * </ul>
     */
    EMOTE_CREATE(60, TargetType.EMOTE),

    /**
     * An Administrator updated an {@link net.dv8tion.jda.core.entities.Emote Emote}.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#EMOTE_NAME EMOTE_NAME}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#EMOTE_ROLES_ADD EMOTE_ROLES_ADD}</li>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#EMOTE_ROLES_REMOVE EMOTE_ROLES_REMOVE}</li>
     * </ul>
     */
    EMOTE_UPDATE(61, TargetType.EMOTE),

    /**
     * An Administrator deleted an {@link net.dv8tion.jda.core.entities.Emote Emote}.
     *
     * <h2>Possible Keys</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogKey#EMOTE_NAME EMOTE_NAME}</li>
     * </ul>
     */
    EMOTE_DELETE(62, TargetType.EMOTE),


    /**
     * A message was created.
     */
    MESSAGE_CREATE(70, TargetType.UNKNOWN),

    /**
     * A message was updated.
     */
    MESSAGE_UPDATE(71, TargetType.UNKNOWN),

    /**
     * An Administrator has deleted one or more {@link net.dv8tion.jda.core.entities.Message Messages}.
     *
     * <h2>Possible Options</h2>
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.audit.AuditLogOption#COUNT COUNT}</li>
     * </ul>
     */
    MESSAGE_DELETE(72, TargetType.MEMBER),


    UNKNOWN(-1, TargetType.UNKNOWN);

    private final int key;
    private final TargetType target;

    ActionType(int key, TargetType target)
    {
        this.key = key;
        this.target = target;
    }

    /**
     * The raw key used to identify types within the api.
     *
     * @return Raw key for this ActionType
     */
    public int getKey()
    {
        return key;
    }

    /**
     * The expected {@link net.dv8tion.jda.core.audit.TargetType TargetType}
     * for this ActionType
     *
     * @return {@link net.dv8tion.jda.core.audit.TargetType TargetType}
     */
    public TargetType getTargetType()
    {
        return target;
    }

    public static ActionType from(int key)
    {
        for (ActionType type : values())
        {
            if (type.key == key)
                return type;
        }
        return UNKNOWN;
    }
}
