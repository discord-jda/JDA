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
 * Enum of possible/expected keys that can be provided
 * to {@link AuditLogEntry#getChangeByKey(AuditLogKey) AuditLogEntry.getChangeByKey(AuditLogEntry.AuditLogKey}.
 *
 * <p>Each constant in this enum has elaborate documentation on expected values for the
 * returned {@link AuditLogChange AuditLogChange}.
 * <br>There is no guarantee that the resulting type is accurate or that the value selected is not {@code null}!
 */
public enum AuditLogKey
{
    /**
     * This is sometimes visible for {@link ActionType ActionTypes}
     * which create a new entity.
     * <br>Use with designated {@code getXById} method.
     *
     * <p>Expected type: <b>String</b>
     */
    ID("id"),

    // GUILD
    /**
     * Change for the {@link net.dv8tion.jda.core.entities.Guild#getName() Guild.getName()} value
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_NAME("name"),

    /**
     * Change of User ID for the owner of a {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_OWNER("owner_id"),

    /**
     * Change of region represented by a key.
     * <br>Use with {@link net.dv8tion.jda.core.Region#fromKey(String) Region.fromKey(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_REGION("region"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild.Timeout AFKTimeout} of a Guild.
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild.Timeout#fromKey(int) Timeout.fromKey(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    GUILD_AFK_TIMEOUT("afk_timeout"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild#getAfkChannel() Guild.getAfkChannel()} value represented by a VoiceChannel ID.
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#getVoiceChannelById(String) Guild.getVoiceChannelById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_AFK_CHANNEL("afk_channel_id"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild#getSystemChannel() Guild.getSystemChannel()} value represented by a TextChannel ID.
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#getTextChannelById(String) Guild.getTextChannelById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_SYSTEM_CHANNEL("system_channel_id"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild#getExplicitContentLevel() Guild.getExplicitContentLevel()} of a Guild.
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild.ExplicitContentLevel#fromKey(int) Guild.ExplicitContentLevel.fromKey(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    GUILD_EXPLICIT_CONTENT_FILTER("explicit_content_filter"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild#getIconId() Icon ID} of a Guild.
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_ICON("icon"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild#getSplashId() Splash ID} of a Guild.
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_SPLASH("splash"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild#getVerificationLevel() Guild.getVerificationLevel()} value.
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild.VerificationLevel#fromKey(int) Guild.VerificationLevel.fromKey(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    GUILD_VERIFICATION_LEVEL("verification_level"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild#getDefaultNotificationLevel() Guild.getDefaultNotificationLevel()} value.
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild.NotificationLevel#fromKey(int) Guild.NotificationLevel.fromKey(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    GUILD_NOTIFICATION_LEVEL("default_message_notifications"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild#getRequiredMFALevel() Guild.getRequiredMFALevel()} value
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild.MFALevel#fromKey(int) Guild.MFALevel.fromKey(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    GUILD_MFA_LEVEL("mfa_level"),


    // CHANNEL
    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Channel#getName() Channel.getName()} value.
     *
     * <p>Expected type: <b>String</b>
     */
    CHANNEL_NAME("name"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Channel#getParent() Channel.getParent()} value.
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#getCategoryById(String) Guild.getCategoryById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    CHANNEL_PARENT("parent_id"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.TextChannel#getTopic() TextChannel.getTopic()} value.
     * <br>Only for {@link net.dv8tion.jda.core.entities.ChannelType#TEXT ChannelType.TEXT}
     *
     * <p>Expected type: <b>String</b>
     */
    CHANNEL_TOPIC("topic"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.VoiceChannel#getBitrate() VoiceChannel.getBitrate()} value.
     * <br>Only for {@link net.dv8tion.jda.core.entities.ChannelType#VOICE ChannelType.VOICE}
     *
     * <p>Expected type: <b>Integer</b>
     */
    CHANNEL_BITRATE("bitrate"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.VoiceChannel#getUserLimit() VoiceChannel.getUserLimit()} value.
     * <br>Only for {@link net.dv8tion.jda.core.entities.ChannelType#VOICE ChannelType.VOICE}
     *
     * <p>Expected type: <b>Integer</b>
     */
    CHANNEL_USER_LIMIT("user_limit"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.TextChannel#isNSFW() TextChannel.isNSFW()} value.
     * <br>Only for {@link net.dv8tion.jda.core.entities.ChannelType#TEXT ChannelType.TEXT}
     *
     * <p>Expected type: <b>Boolean</b>
     */
    CHANNEL_NSFW("nsfw"),

    /**
     * The integer type of this channel.
     * <br>Use with {@link net.dv8tion.jda.core.entities.ChannelType#fromId(int) ChannelType.fromId(int)}.
     *
     * <p>Expected type: <b>int</b>
     */
    CHANNEL_TYPE("type"),

    /**
     * The overrides for this channel.
     *
     * <p>Expected type: <b>List{@literal <Map<String, Object>>}</b>
     */
    CHANNEL_OVERRIDES("permission_overwrites"),


    // MEMBER
    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Member#getNickname() Member.getNickname()} value
     *
     * <p>Expected type: <b>String</b>
     */
    MEMBER_NICK("nick"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Member#getVoiceState() GuildVoiceState} of a Member.
     * <br>Indicating that the {@link net.dv8tion.jda.core.entities.GuildVoiceState#isGuildMuted() Guild.isGuildMuted()} value updated.
     *
     * <p>Expected type: <b>Boolean</b>
     */
    MEMBER_MUTE("mute"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Member#getVoiceState() GuildVoiceState} of a Member.
     * <br>Indicating that the {@link net.dv8tion.jda.core.entities.GuildVoiceState#isGuildDeafened() Guild.isGuildDeafened()} value updated.
     *
     * <p>Expected type: <b>Boolean</b>
     */
    MEMBER_DEAF("deaf"),

    /**
     * Roles added to {@link net.dv8tion.jda.core.entities.Member#getRoles() Member.getRoles()} with this action
     * <br>Containing a list of {@link net.dv8tion.jda.core.entities.Role Role} IDs
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#getRoleById(String) Guild.getRoleById(String)}
     *
     * <p>Expected type: <b>List{@literal <String>}</b>
     */
    MEMBER_ROLES_ADD("$add"),

    /**
     * Roles removed from {@link net.dv8tion.jda.core.entities.Member#getRoles() Member.getRoles()} with this action
     * <br>Containing a list of {@link net.dv8tion.jda.core.entities.Role Role} IDs
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#getRoleById(String) Guild.getRoleById(String)}
     *
     * <p>Expected type: <b>List{@literal <String>}</b>
     */
    MEMBER_ROLES_REMOVE("$remove"),


    // PERMISSION OVERRIDE
    /**
     * Modified raw denied permission bits
     * <br>Similar to the value returned by {@link net.dv8tion.jda.core.entities.PermissionOverride#getDeniedRaw() PermissionOverride.getDeniedRaw()}
     * <br>Use with {@link net.dv8tion.jda.core.Permission#getPermissions(long) Permission.getPermissions(long)}
     *
     * <p>Expected type: <b>long</b>
     */
    OVERRIDE_DENY("deny"),

    /**
     * Modified raw allowed permission bits
     * <br>Similar to the value returned by {@link net.dv8tion.jda.core.entities.PermissionOverride#getAllowedRaw() PermissionOverride.getAllowedRaw()}
     * <br>Use with {@link net.dv8tion.jda.core.Permission#getPermissions(long) Permission.getPermissions(long)}
     *
     * <p>Expected type: <b>long</b>
     */
    OVERRIDE_ALLOW("allow"),

    /**
     * The string type of this override.
     * <br>{@code "role"} or {@code "member"}.
     *
     * <p>Expected type: <b>String</b>
     */
    OVERRIDE_TYPE("type"),


    // ROLE
    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Role#getName() Role.getName()} value.
     *
     * <p>Expected type: <b>String</b>
     */
    ROLE_NAME("name"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Role#getPermissionsRaw() Role.getPermissionsRaw()} value.
     * <br>Use with {@link net.dv8tion.jda.core.Permission#getPermissions(long) Permission.getPermissions(long)}
     *
     * <p>Expected type: <b>Long</b>
     */
    ROLE_PERMISSIONS("permissions"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Role#getColor() Role.getColor()} value.
     * <br>Use with {@link java.awt.Color#Color(int) Color(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    ROLE_COLOR("color"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Role#isHoisted() Role.isHoisted()} value.
     *
     * <p>Expected type: <b>Boolean</b>
     */
    ROLE_HOISTED("hoist"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Role#isMentionable() Role.isMentionable()} value.
     *
     * <p>Expected type: <b>Boolean</b>
     */
    ROLE_MENTIONABLE("mentionable"),


    // EMOTE
    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Emote#getName() Emote.getName()} value.
     *
     * <p>Expected type: <b>String</b>
     */
    EMOTE_NAME("name"),

    /**
     * Roles added to {@link net.dv8tion.jda.core.entities.Emote#getRoles() Emote.getRoles()} with this action
     * <br>Containing a list of {@link net.dv8tion.jda.core.entities.Role Role} IDs
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#getRoleById(String) Guild.getRoleById(String)}
     *
     * <p>Expected type: <b>List{@literal <String>}</b>
     */
    EMOTE_ROLES_ADD("$add"),

    /**
     * Roles remove from {@link net.dv8tion.jda.core.entities.Emote#getRoles() Emote.getRoles()} with this action
     * <br>Containing a list of {@link net.dv8tion.jda.core.entities.Role Role} IDs
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#getRoleById(String) Guild.getRoleById(String)}
     *
     * <p>Expected type: <b>List{@literal <String>}</b>
     */
    EMOTE_ROLES_REMOVE("$remove"),


    // WEBHOOK
    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Webhook#getName() Webhook.getName()} value.
     *
     * <p>Expected type: <b>String</b>
     */
    WEBHOOK_NAME("name"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Webhook#getDefaultUser() Webhook.getDefaultUser()}'s avatar hash of a Webhook.
     * <br>This is used to build the {@link net.dv8tion.jda.core.entities.User#getAvatarUrl() User.getAvatarUrl()}!
     *
     * <p>Expected type: <b>String</b>
     */
    WEBHOOK_ICON("avatar_hash"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Webhook#getChannel() Webhook.getChannel()} for
     * the target {@link net.dv8tion.jda.core.entities.Webhook Webhook}
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#getTextChannelById(String) Guild.getTextChannelById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    WEBHOOK_CHANNEL("channel_id"),


    // INVITE
    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Invite#getCode() Invite.getCode()} for
     * the target {@link net.dv8tion.jda.core.entities.Invite Invite}
     * <br>Use with {@link net.dv8tion.jda.core.entities.Invite#resolve(net.dv8tion.jda.core.JDA, String)} Invite.resolve(JDA, String)}
     *
     * <p>Expected type: <b>String</b>
     */
    INVITE_CODE("code"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Invite#getMaxAge() Invite.getMaxAge()} for
     * the target {@link net.dv8tion.jda.core.entities.Invite Invite}
     *
     * <p>Expected type: <b>int</b>
     */
    INVITE_MAX_AGE("max_age"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Invite#isTemporary() Invite.isTemporary()} for
     * the target {@link net.dv8tion.jda.core.entities.Invite Invite}
     *
     * <p>Expected type: <b>boolean</b>
     */
    INVITE_TEMPORARY("temporary"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Invite#getInviter() Invite.getInviter()} ID for
     * the target {@link net.dv8tion.jda.core.entities.Invite Invite}
     * <br>Use with {@link net.dv8tion.jda.core.JDA#getUserById(String) JDA.getUserById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    INVITE_INVITER("inviter"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Invite#getChannel() Invite.getChannel()} ID for
     * the target {@link net.dv8tion.jda.core.entities.Invite Invite}
     * <br>Use with {@link net.dv8tion.jda.core.JDA#getTextChannelById(String) JDA.getTextChannelById(String)}
     * or {@link net.dv8tion.jda.core.JDA#getVoiceChannelById(String) JDA.getVoiceChannelById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    INVITE_CHANNEL("channel_id"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Invite#getUses() Invite.getUses()} for
     * the target {@link net.dv8tion.jda.core.entities.Invite Invite}
     *
     * <p>Expected type: <b>int</b>
     */
    INVITE_USES("uses"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Invite#getMaxUses() Invite.getMaxUses()} for
     * the target {@link net.dv8tion.jda.core.entities.Invite Invite}
     *
     * <p>Expected type: <b>int</b>
     */
    INVITE_MAX_USES("max_uses");


    private final String key;

    AuditLogKey(String key)
    {
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return name() + '(' + key + ')';
    }
}
