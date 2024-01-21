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
package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;

import javax.annotation.Nonnull;

/**
 * Represents the different types of {@link net.dv8tion.jda.api.entities.Message Messages} that can be received from Discord.
 * <br>A normal text based message is {@link #DEFAULT}.
 */
public enum MessageType
{
    /**
     * The normal text messages received when a user or bot sends a Message.
     */
    DEFAULT(0, false, true),

    /**
     * Specialized messages used for Groups as a System-Message showing that a new User has been added to the Group.
     * Also used in message threads to indicate a member has joined that thread.
     */
    RECIPIENT_ADD(1, true, false),

    /**
     * Specialized messages used for Groups as a System-Message showing that a new User has been removed from the Group.
     * Also used in message threads to indicate a member has left that thread.
     */
    RECIPIENT_REMOVE(2, true, false),

    /**
     * Specialized message used for Groups as a System-Message showing that a Call was started.
     */
    CALL(3, true, false),

    /**
     * Specialized message used for Groups as a System-Message showing that the name of the Group was changed.
     * Also used in message threads to indicate the name of that thread has changed.
     */
    CHANNEL_NAME_CHANGE(4, true, false),

    /**
     * Specialized message used for Groups as a System-Message showing that the icon of the Group was changed.
     */
    CHANNEL_ICON_CHANGE(5, true, false),

    /**
     * Specialized message used in MessageChannels as a System-Message to announce new pins
     */
    CHANNEL_PINNED_ADD(6, true, true),

    /**
     * Specialized message used to welcome new members in a Guild
     */
    GUILD_MEMBER_JOIN(7, true, true),

    /**
     * Specialized message used to announce a new booster
     */
    GUILD_MEMBER_BOOST(8, true, true),

    /**
     * Specialized message used to announce the server has reached tier 1
     */
    GUILD_BOOST_TIER_1(9, true, true),

    /**
     * Specialized message used to announce the server has reached tier 2
     */
    GUILD_BOOST_TIER_2(10, true, true),

    /**
     * Specialized message used to announce the server has reached tier 3
     */
    GUILD_BOOST_TIER_3(11, true, true),

    /**
     * Specialized message used to announce when a crosspost webhook is added to a channel
     */
    CHANNEL_FOLLOW_ADD(12, true, true),

    /**
     * System message related to discovery qualifications.
     */
    GUILD_DISCOVERY_DISQUALIFIED(14, true, false),

    /**
     * System message related to discovery qualifications.
     */
    GUILD_DISCOVERY_REQUALIFIED(15, true, false),

    /**
     * System message related to discovery qualifications.
     */
    GUILD_DISCOVERY_GRACE_PERIOD_INITIAL_WARNING(16, true, false),

    /**
     * System message related to discovery qualifications.
     */
    GUILD_DISCOVERY_GRACE_PERIOD_FINAL_WARNING(17, true, false),

    /**
     * This is sent to a TextChannel when a message thread is created if the message from which the thread was started is "old".
     * The definition of "old" is loose, but is currently a very liberal definition.
     */
    THREAD_CREATED(18, true, true),

    /**
     * Reply to another message. This usually comes with a {@link Message#getReferencedMessage() referenced message}.
     */
    INLINE_REPLY(19, false, true),

    /**
     * This message was created by an interaction. Usually in combination with Slash Commands.
     */
    SLASH_COMMAND(20, false, true),

    /**
     * A new message sent as the first message in threads that are started from an existing message in the parent channel.
     * It only contains a message reference field that points to the message from which the thread was started.
     */
    THREAD_STARTER_MESSAGE(21, false, false),

    /**
     * The "Invite your friends" messages that are sent to guild owners in new servers.
     */
    GUILD_INVITE_REMINDER(22, true, true),

    /**
     * This message was created by an interaction. Usually in combination with Context Menus.
     */
    CONTEXT_COMMAND(23, false, true),

    /**
     * This message was created by the automod system.
     *
     * <p>Messages from this type usually come with custom embeds containing relevant information, the author is the user that triggered the filter.
     */
    AUTO_MODERATION_ACTION(24, true, true),

    /**
     * Sent when someone purchases a role subscription.
     *
     * @see Role.RoleTags#isAvailableForPurchase()
     * @see Role.RoleTags#hasSubscriptionListing()
     */
    ROLE_SUBSCRIPTION_PURCHASE(25, true, true),

    /**
     * Sent by a bot when a command is restricted to premium users.
     * <br>Contains a button which allows to upgrade to premium.
     */
    INTERACTION_PREMIUM_UPSELL(26, true, true),

    /**
     * Messages created in {@link StageChannel StageChannels} to indicate that a stage instance has started.
     * <br>The message content will be the {@link StageInstance#getTopic() topic} and the author is the user who started the stage instance.
     */
    STAGE_START(27, true, true),

    /**
     * Messages created in {@link StageChannel StageChannels} to indicate that a stage instance has ended.
     * <br>The message content will be the {@link StageInstance#getTopic() topic} and the author is the user who ended the stage instance.
     */
    STAGE_END(28, true, true),

    /**
     * Messages created in {@link StageChannel StageChannels} to indicate that a new {@link StageInstance#getSpeakers() speaker} is up.
     * <br>The author is the user who became speaker.
     */
    STAGE_SPEAKER(29, true, true),

    /**
     * Messages created in {@link StageChannel StageChannels} to indicate that a stage instance topic has been changed.
     * <br>The message content will be the new {@link StageInstance#getTopic() topic} and the author is the user who updated the topic.
     */
    STAGE_TOPIC(31, true, true),

    /**
     * Sent to the {@link Guild#getSystemChannel() system channel} when a guild administrator subscribes to the premium plan of an application.
     */
    GUILD_APPLICATION_PREMIUM_SUBSCRIPTION(32, true, true),

//    /**
//     * Sent when an application is added as integration to a private channel or group channel.
//     */
//    PRIVATE_CHANNEL_INTEGRATION_ADDED(33, true, true),
//
//    /**
//     * Sent when an application integration is removed from a private channel or group channel.
//     */
//    PRIVATE_CHANNEL_INTEGRATION_REMOVED(34, true, true),

//    /**
//     * Unclear what this is for or if its used at all
//     */
//    PREMIUM_REFERRAL(35, true, true),

    /**
     * Sent when a moderator activates a temporary security measure, such as pausing invites or direct messages.
     * <br>The message content is an ISO 8601 timestamp, which indicates when the action expires and disables the security measures automatically.
     *
     * @see java.time.OffsetDateTime#parse(CharSequence)
     */
    GUILD_INCIDENT_ALERT_MODE_ENABLED(36, true, false),

    /**
     * Sent when a moderator deactivates a temporary security measure, such as pausing invites or direct messages.
     */
    GUILD_INCIDENT_ALERT_MODE_DISABLED(37, true, false),

    /**
     * Sent when a moderator reports a raid in a guild.
     * <br>The message author is the reporter.
     */
    GUILD_INCIDENT_REPORT_RAID(38, true, false),

    /**
     * Sent when a moderator reports a raid as a false alarm in a guild.
     */
    GUILD_INCIDENT_REPORT_FALSE_ALARM(39, true, false),

    /**
     * Unknown MessageType.
     */
    UNKNOWN(-1, false, true);

    private final int id;
    private final boolean system;
    private final boolean deletable;

    MessageType(int id, boolean system, boolean deletable)
    {
        this.id = id;
        this.system = system;
        this.deletable = deletable;
    }

    /**
     * The Discord id key used to reference the MessageType.
     *
     * @return the Discord id key.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Whether this message type is for system messages.
     * <br>These are messages that are sent by Discord and don't look like messages from users.
     * Messages like this have some special restrictions.
     *
     * @return True, if this type is for a system message
     */
    public boolean isSystem()
    {
        return system;
    }

    /**
     * Whether messages of this type can be deleted.
     * <br>These are messages which are required to stay such as thread starter messages.
     *
     * <p><b>Messages which cannot be deleted:</b><br>
     * <ul>
     *     <li>{@link #RECIPIENT_ADD}</li>
     *     <li>{@link #RECIPIENT_REMOVE}</li>
     *     <li>{@link #CALL}</li>
     *     <li>{@link #CHANNEL_NAME_CHANGE}</li>
     *     <li>{@link #CHANNEL_ICON_CHANGE}</li>
     *     <li>{@link #GUILD_DISCOVERY_DISQUALIFIED}</li>
     *     <li>{@link #GUILD_DISCOVERY_REQUALIFIED}</li>
     *     <li>{@link #GUILD_DISCOVERY_GRACE_PERIOD_INITIAL_WARNING}</li>
     *     <li>{@link #GUILD_DISCOVERY_GRACE_PERIOD_FINAL_WARNING}</li>
     *     <li>{@link #GUILD_DISCOVERY_GRACE_PERIOD_FINAL_WARNING}</li>
     *     <li>{@link #THREAD_STARTER_MESSAGE}</li>
     *     <li>{@link #GUILD_APPLICATION_PREMIUM_SUBSCRIPTION}</li>
     * </ul>
     *
     * @return True, if delete is supported
     */
    public boolean canDelete()
    {
        return deletable;
    }

    /**
     * Used to retrieve a MessageType based on the Discord id key.
     * <br>If the {@code id} provided is not a known id, {@link #UNKNOWN} is returned
     *
     * @param  id
     *         The Discord key id of the requested MessageType.
     *
     * @return A MessageType with the same Discord id key as the one provided, or {@link #UNKNOWN}.
     */
    @Nonnull
    public static MessageType fromId(int id)
    {
        for (MessageType type : values())
        {
            if (type.id == id)
                return type;
        }
        return UNKNOWN;
    }
}
