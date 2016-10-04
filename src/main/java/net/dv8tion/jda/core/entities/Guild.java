/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Region;

import java.util.Collection;
import java.util.List;

/**
 * Represents a Discord {@link net.dv8tion.jda.core.entities.Guild Guild}. This should contain all information provided from Discord about a Guild.
 */
public interface Guild extends ISnowflake
{
    /**
     * The human readable name of the {@link net.dv8tion.jda.core.entities.Guild Guild}. If no name has been set, this returns null.
     *
     * @return
     *      Never-null String containing the Guild's name.
     */
    String getName();

    /**
     * The Discord Id of the {@link net.dv8tion.jda.core.entities.Guild Guild} icon image. If no icon has been set, this returns null.
     *
     * @return
     *      Possibly-null String containing the Guild's icon id.
     */
    String getIconId();

    /**
     * The URL of the {@link net.dv8tion.jda.core.entities.Guild Guild} icon image. If no icon has been set, this returns null.
     *
     * @return
     *      Possibly-null String containing the Guild's icon URL.
     */
    String getIconUrl();

    String getSplashId();

    String getSplashUrl();

    VoiceChannel getAfkChannel();

    /**
     * The {@link net.dv8tion.jda.core.entities.Member Member} object of the owner of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @return
     *      Never-null Member object containing the Guild owner.
     */
    Member getOwner();

    /**
     * The amount of time (in seconds) that must pass with no activity to be considered AFK by this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * Default is 300 seconds (5 minutes)
     *
     * @return
     *      Positive int representing the timeout value.
     */
    int getAfkTimeout();

    /**
     * The {@link net.dv8tion.jda.core.Region Region} that this {@link net.dv8tion.jda.core.entities.Guild Guild} exists in.<br>
     * If the {@link net.dv8tion.jda.core.Region Region} is not recognized, returns {@link net.dv8tion.jda.core.Region#UNKNOWN UNKNOWN}.
     *
     * @return
     *      The the Region location that the Guild is hosted in. Can return Region.UNKNOWN.
     */
    Region getRegion();

    /**
     * Used to determine the provided {@link net.dv8tion.jda.core.entities.User User} is a member of this Guild.
     *
     * @param user
     *          The user to determine whether or not they are a member of this guild.
     * @return
     *      True - if this user is present in this guild.
     */
    boolean isMember(User user);

    Member getMember(User user);

    Member getMemberById(String userId);

    List<Member> getMembersByName(String name, boolean ignoreCase);
    List<Member> getMembersByNickname(String nickname, boolean ignoreCase);
    List<Member> getMembersByEffectiveName(String name, boolean ignoreCase);
    List<Member> getMembersWithRoles(Role... roles);
    List<Member> getMembersWithRoles(Collection<Role> roles);


    List<Member> getMembers();

    /**
     * The {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} available on the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * The channels returned will be sorted according to their position.
     *
     * @return
     *      An Immutable List of {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}.
     */
    List<TextChannel> getTextChannels();
//
//    /**
//     * Creates a new {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} in this Guild.
//     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.Permission#MANAGE_CHANNEL MANAGE_CHANNEL Permission}
//     *
//     * @param name
//     *      the name of the TextChannel to create
//     * @return
//     *      the ChannelManager for the created TextChannel
//     * @throws net.dv8tion.jda.exceptions.GuildUnavailableException
//     *      if the guild is temporarily unavailable
//     */
//    ChannelManager createTextChannel(String name);
//
    /**
     * The {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels} available on the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * The channels returned will be sorted according to their position.
     *
     * @return
     *      An Immutable List of {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}.
     */
    List<VoiceChannel> getVoiceChannels();
//
//    /**
//     * Creates a new {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} in this Guild.
//     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.Permission#MANAGE_CHANNEL MANAGE_CHANNEL Permission}
//     *
//     * @param name
//     *      the name of the VoiceChannel to create
//     * @return
//     *      the ChannelManager for the created VoiceChannel
//     * @throws net.dv8tion.jda.exceptions.GuildUnavailableException
//     *      if the guild is temporarily unavailable
//     */
//    ChannelManager createVoiceChannel(String name);

    /**
     * The {@link net.dv8tion.jda.core.entities.Role Roles} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * The roles returned will be sorted according to their position.
     *
     * @return
     *      An Immutable List of {@link net.dv8tion.jda.core.entities.Role Roles}.
     */
    List<Role> getRoles();

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.Role Role} which has the same id as the one provided.<br>
     * If there is no {@link net.dv8tion.jda.core.entities.Role Role} that matches the requested id, <code>null</code> is returned.
     *
     * @param id
     *      The id of the {@link net.dv8tion.jda.core.entities.Role Role}.
     * @return
     *      Possibly-null Role with matching id.
     */
    Role getRoleById(String id);
//
//    /**
//     * Creates a new {@link net.dv8tion.jda.core.entities.Role Role} in this Guild.
//     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.Permission#MANAGE_ROLES MANAGE_ROLES Permission}
//     *
//     * @return
//     *      the RoleManager for the created Role
//     * @throws net.dv8tion.jda.exceptions.GuildUnavailableException
//     *      if the guild is temporarily unavailable
//     */
//    RoleManager createRole();
//
//    /**
//     * Creates a new {@link net.dv8tion.jda.core.entities.Role Role} in this {@link net.dv8tion.jda.core.entities.Guild Guild} with the same settings as the given {@link net.dv8tion.jda.core.entities.Role Role}.
//     * It will be placed at the bottom (just over the @everyone role) to avoid permission hierarchy conflicts.
//     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.Permission#MANAGE_ROLES MANAGE_ROLES Permission}
//     * and all {@link net.dv8tion.jda.Permission Permissions} the given {@link net.dv8tion.jda.core.entities.Role Role} has.
//     *
//     * @param role
//     *      The {@link net.dv8tion.jda.core.entities.Role Role} that should be copied
//     * @return
//     *      the RoleManager for the created Role
//     * @throws net.dv8tion.jda.exceptions.GuildUnavailableException
//     *      if the guild is temporarily unavailable
//     * @throws net.dv8tion.jda.exceptions.PermissionException
//     *      if the bot doesn't has {@link net.dv8tion.jda.Permission#MANAGE_ROLES MANAGE_ROLES Permission} and every Permission the given Role has
//     */
//    RoleManager createCopyOfRole(Role role);

//
//    /**
//     * Provides the {@link net.dv8tion.jda.core.entities.Role Role} that determines the color for the provided {@link net.dv8tion.jda.core.entities.User User}
//     *
//     * If the {@link net.dv8tion.jda.core.entities.User User} has the default color, this returns the same as getPublicRole();
//     */
//    Role getColorDeterminantRoleForUser(User user);

    /**
     * The @everyone {@link net.dv8tion.jda.core.entities.Role Role} of this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return The @everyone {@link net.dv8tion.jda.core.entities.Role Role}
     */
    Role getPublicRole();

    /**
     * The default {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} for a {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * This channel cannot be deleted and the {@link #getPublicRole() Public Role} always has the ability to
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} this channel.
     *
     * @return
     *      The {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} representing the public channel for this guild.
     */
    TextChannel getPublicChannel();

//    /**
//     * Returns the {@link net.dv8tion.jda.managers.GuildManager GuildManager} for this Guild.
//     * In the GuildManager, you can modify most of its properties, and leave or delete it.
//     *
//     * @return
//     *      The GuildManager of this Guild
//     * @throws net.dv8tion.jda.exceptions.GuildUnavailableException
//     *      if the guild is temporarily unavailable
//     */
//    GuildManager getManager();
//
//    /**
//     * Returns the {@link net.dv8tion.jda.managers.AudioManager AudioManager} that represents the
//     * audio connection for this Guild.
//     *
//     * @return
//     *      The AudioManager for this Guild.
//     */
//    AudioManager getAudioManager();

    /**
     * Returns the {@link net.dv8tion.jda.core.JDA JDA} instance of this Guild
     * @return
     *      the corresponding JDA instance
     */
    JDA getJDA();

    /**
     * A list containing the {@link net.dv8tion.jda.core.entities.VoiceState VoiceState} of every {@link net.dv8tion.jda.core.entities.Member Member}
     * in this {@link net.dv8tion.jda.core.entities.Guild Guild}.<br>
     * This will never return an empty list because if it were empty, that would imply that there are no
     * {@link net.dv8tion.jda.core.entities.Member Members} in this {@link net.dv8tion.jda.core.entities.Guild Guild}, which is
     * impossible.
     *
     * @return
     *      Never-empty list containing all the {@link VoiceState VoiceStates} on this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     */
    List<VoiceState> getVoiceStates();

    /**
     * Returns the verification-Level of this Guild. For a short description of the different values, see {@link net.dv8tion.jda.core.entities.Guild.VerificationLevel}.
     * @return
     *      The Verification-Level of this Guild.
     */
    VerificationLevel getVerificationLevel();

    /**
     * Returns the default message Notification-Level of this Guild. For a short description of the different values,
     * see {@link net.dv8tion.jda.core.entities.Guild.NotificationLevel NotificationLevel}.
     * @return
     *      The default message Notification-Level of this Guild.
     */
    NotificationLevel getDefaultNotificationLevel();

    /**
     * Returns the level of multifactor authentication required to execute administrator restricted functions in this guild.
     * For a short description of the different values,
     * see {@link net.dv8tion.jda.core.entities.Guild.MFALevel MFALevel}.
     * @return
     *      The MFA-Level required by this Guild.
     */
    MFALevel getRequiredMFALevel();

    /**
     * Checks if the current Verification-level of this guild allows JDA to send messages to it.
     *
     * @return
     *      True if Verification-level allows sending of messages, false if not.
     * @see net.dv8tion.jda.core.entities.Guild.VerificationLevel
     *      VerificationLevel Enum with a list of possible verification-levels and their requirements
     */
    boolean checkVerification();

    /**
     * Returns whether or not this Guild is available. A Guild can be unavailable, if the Discord server has problems.
     * If a Guild is unavailable, no actions on it can be performed (Messages, Manager,...)
     *
     * @return
     *      If the Guild is available
     */
    boolean isAvailable();

//    /**
//     * Provides a list of all {@link net.dv8tion.jda.utils.InviteUtil.AdvancedInvite Invites} for this Guild.
//     *
//     * @return
//     *      An Immutable List of {@link net.dv8tion.jda.utils.InviteUtil.AdvancedInvite Invites} for this guild.
//     */
//    List<AdvancedInvite> getInvites();

    /**
     * Represents the Verification-Level of the Guild.
     * The Verification-Level determines what requirement you have to meet to be able to speak in this Guild.<br>
     * None   -&gt; everyone can talk.<br>
     * Low    -&gt; verified email required.<br>
     * Medium -&gt; you have to be member of discord for at least 5min.<br>
     * High   -&gt; you have to be member of this guild for at least 10min.
     */
    enum VerificationLevel
    {
        NONE(0),
        LOW(1),
        MEDIUM(2),
        HIGH(3),
        UNKNOWN(-1);

        private final int key;

        VerificationLevel(int key)
        {
            this.key = key;
        }

        public int getKey()
        {
            return key;
        }

        public static VerificationLevel fromKey(int key)
        {
            for (VerificationLevel level : VerificationLevel.values())
            {
                if(level.getKey() == key)
                    return level;
            }
            return UNKNOWN;
        }
    }

    /**
     * Represents the Notification-level of the Guild.
     * The Verification-Level determines what messages you receive pings for.<br>
     * All_Messages   -&gt; Every message sent in this guild will result in a message ping.<br>
     * Mentions_Only  -&gt; Only messages that specifically mention will result in a ping.<br>
     */
    enum NotificationLevel
    {
        ALL_MESSAGES(0),
        MENTIONS_ONLY(1),
        UNKNOWN(-1);

        private final int key;

        NotificationLevel(int key)
        {
            this.key = key;
        }

        public int getKey()
        {
            return key;
        }

        public static NotificationLevel fromKey(int key)
        {
            for (NotificationLevel level : values())
            {
                if (level.getKey() == key)
                    return level;
            }
            return UNKNOWN;
        }
    }

    /**
     * Represents the Multifactor Authentication level required by the Guild.
     * The MFA Level restricts administrator functions to account with MFA Level equal to or higher than that set by the guild.<br>
     * None             -&gt; There is no MFA level restriction on administrator functions in this guild.<br>
     * Two_Factor_Auth  -&gt; Users must have 2FA enabled on their account to perform administrator functions.<br>
     */
    enum MFALevel
    {
        NONE(0),
        TWO_FACTOR_AUTH(1),
        UNKNOWN(-1);

        private final int key;

        MFALevel(int key)
        {
            this.key = key;
        }

        public int getKey()
        {
            return key;
        }

        public static MFALevel fromKey(int key)
        {
            for (MFALevel level : values())
            {
                if (level.getKey() == key)
                    return level;
            }
            return UNKNOWN;
        }
    }
}
