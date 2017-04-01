/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.managers.GuildManager;
import net.dv8tion.jda.core.managers.GuildManagerUpdatable;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.Collection;
import java.util.List;

/**
 * Represents a Discord {@link net.dv8tion.jda.core.entities.Guild Guild}.
 * This should contain all information provided from Discord about a Guild.
 */
public interface Guild extends ISnowflake
{
    /**
     * The human readable name of the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <p>
     * This value can be modified using {@link net.dv8tion.jda.core.managers.GuildManager#setName(String)}
     * or {@link net.dv8tion.jda.core.managers.GuildManagerUpdatable#getNameField()}.
     *
     * @return Never-null String containing the Guild's name.
     */
    String getName();

    /**
     * The Discord hash-id of the {@link net.dv8tion.jda.core.entities.Guild Guild} icon image.
     * If no icon has been set, this returns {@code null}.
     * <p>
     * The Guild icon can be modified using {@link net.dv8tion.jda.core.managers.GuildManager#setIcon(Icon)}
     * or {@link net.dv8tion.jda.core.managers.GuildManagerUpdatable#getIconField()}.
     *
     * @return Possibly-null String containing the Guild's icon hash-id.
     */
    String getIconId();

    /**
     * The URL of the {@link net.dv8tion.jda.core.entities.Guild Guild} icon image.
     * If no icon has been set, this returns {@code null}.
     * <p>
     * The Guild icon can be modified using {@link net.dv8tion.jda.core.managers.GuildManager#setIcon(Icon)}
     * or {@link net.dv8tion.jda.core.managers.GuildManagerUpdatable#getIconField()}.
     *
     * @return Possibly-null String containing the Guild's icon URL.
     */
    String getIconUrl();

    /**
     * The Discord hash-id of the splash image for this Guild. A Splash image is an image displayed when viewing a
     * Discord Guild Invite on the web or in client just before accepting or declining the invite.
     * If no splash has been set, this returns {@code null}.
     * <br>Splash images are VIP/Partner Guild only.
     * <p>
     * The Guild splash can be modified using {@link net.dv8tion.jda.core.managers.GuildManager#setSplash(Icon)}
     * or {@link net.dv8tion.jda.core.managers.GuildManagerUpdatable#getSplashField()}.
     *
     * @return Possibly-null String containing the Guild's splash hash-id
     */
    String getSplashId();

    /**
     * The URL of the splash image for this Guild. A Splash image is an image displayed when viewing a
     * Discord Guild Invite on the web or in client just before accepting or declining the invite.
     * If no splash has been set, this returns {@code null}.
     * <br>Splash images are VIP/Partner Guild only.
     * <p>
     * The Guild splash can be modified using {@link net.dv8tion.jda.core.managers.GuildManager#setSplash(Icon)}
     * or {@link net.dv8tion.jda.core.managers.GuildManagerUpdatable#getSplashField()}.
     *
     * @return Possibly-null String containing the Guild's splash URL.
     */
    String getSplashUrl();

    /**
     * Provides the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} that has been set as the channel
     * which {@link net.dv8tion.jda.core.entities.Member Members} will be moved to after they have been inactive in a
     * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} for longer than {@link #getAfkTimeout()}.
     * <br>If no channel has been set as the AFK channel, this returns {@code null}.
     * <p>
     * This value can be modified using {@link net.dv8tion.jda.core.managers.GuildManager#setAfkChannel(VoiceChannel)}
     * or {@link net.dv8tion.jda.core.managers.GuildManagerUpdatable#getAfkChannelField()}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} that is the AFK Channel.
     */
    VoiceChannel getAfkChannel();

    /**
     * The {@link net.dv8tion.jda.core.entities.Member Member} object of the owner of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <p>
     * Ownership can be transferred using {@link GuildController#transferOwnership(Member)}.
     *
     * @return Never-null Member object containing the Guild owner.
     */
    Member getOwner();

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild.Timeout Timeout} set for this Guild representing the amount of time
     * that must pass for a Member to have had no activity in a {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     * to be considered AFK. If {@link #getAfkChannel()} is not {@code null} (thus an AFK channel has been set) then Member
     * will be automatically moved to the AFK channel after they have been inactive for longer than the returned Timeout.
     * <br>Default is {@link Timeout#SECONDS_300 300 seconds (5 minutes)}.
     * <p>
     * This value can be modified using {@link net.dv8tion.jda.core.managers.GuildManager#setAfkTimeout(net.dv8tion.jda.core.entities.Guild.Timeout)}
     * or {@link net.dv8tion.jda.core.managers.GuildManagerUpdatable#getAfkTimeoutField()}.
     *
     * @return The {@link net.dv8tion.jda.core.entities.Guild.Timeout Timeout} set for this Guild.
     */
    Timeout getAfkTimeout();

    /**
     * The {@link net.dv8tion.jda.core.Region Region} that this {@link net.dv8tion.jda.core.entities.Guild Guild} is
     * using for audio connections.
     * <br>If the {@link net.dv8tion.jda.core.Region Region} is not recognized, returns {@link net.dv8tion.jda.core.Region#UNKNOWN UNKNOWN}.
     * <p>
     * This value can be modified using {@link net.dv8tion.jda.core.managers.GuildManager#setRegion(net.dv8tion.jda.core.Region)}
     * or {@link net.dv8tion.jda.core.managers.GuildManagerUpdatable#getRegionField()}.
     *
     * @return The the audio Region this Guild is using for audio connections. Can return Region.UNKNOWN.
     */
    Region getRegion();

    /**
     * Used to determine if the provided {@link net.dv8tion.jda.core.entities.User User} is a member of this Guild.
     *
     * @param  user
     *         The user to determine whether or not they are a member of this guild.
     *
     * @return True - if this user is present in this guild.
     */
    boolean isMember(User user);

    /**
     * Gets the {@link net.dv8tion.jda.core.entities.Member Member} object of the currently logged in account in this guild.
     * <br>This is basically {@link net.dv8tion.jda.core.JDA#getSelfUser()} being provided to {@link #getMember(User)}.
     *
     * @return The Member object of the currently logged in account.
     */
    Member getSelfMember();

    /**
     * Gets the Guild specific {@link net.dv8tion.jda.core.entities.Member Member} object for the provided
     * {@link net.dv8tion.jda.core.entities.User User}.
     * <br>If the user is not in this guild, {@code null} is returned.
     *
     * @param  user
     *         The {@link net.dv8tion.jda.core.entities.User User} which to retrieve a related Member object for.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Member Member} for the related {@link net.dv8tion.jda.core.entities.User User}.
     */
    Member getMember(User user);

    /**
     * Gets a {@link net.dv8tion.jda.core.entities.Member Member} object via the id of the user. The id relates to
     * {@link net.dv8tion.jda.core.entities.User#getId()}, and this method is similar to {@link JDA#getUserById(String)}
     * <br>This is more efficient that using {@link JDA#getUserById(String)} and {@link #getMember(User)}.
     * <br>If no Member in this Guild has the {@code userId} provided, this returns {@code null}.
     *
     * @param  userId
     *         The Discord id of the User for which a Member object is requested.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Member Member} with the related {@code userId}.
     */
    Member getMemberById(String userId);

    /**
     * A list of all {@link net.dv8tion.jda.core.entities.Member Members} in this Guild.
     * <br>The Members are not provided in any particular order.
     *
     * @return Immutable list of all members in this Guild.
     */
    List<Member> getMembers();

    /**
     * Gets a list of all {@link net.dv8tion.jda.core.entities.Member Members} who have the same name as the one provided.
     * <br>This compares against {@link net.dv8tion.jda.core.entities.Member#getUser()}{@link net.dv8tion.jda.core.entities.User#getName() .getName()}
     * <br>If there are no {@link net.dv8tion.jda.core.entities.Member Members} with the provided name, then this returns an empty list.
     *
     * @param  name
     *         The name used to filter the returned Members.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all Members with the same name as the name provided.
     */
    List<Member> getMembersByName(String name, boolean ignoreCase);

    /**
     * Gets a list of all {@link net.dv8tion.jda.core.entities.Member Members} who have the same nickname as the one provided.
     * <br>This compares against {@link Member#getNickname()}. If a Member does not have a nickname, the comparison results as false.
     * <br>If there are no {@link net.dv8tion.jda.core.entities.Member Members} with the provided name, then this returns an empty list.
     *
     * @param  nickname
     *         The nickname used to filter the returned Members.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all Members with the same nickname as the nickname provided.
     */
    List<Member> getMembersByNickname(String nickname, boolean ignoreCase);

    /**
     * Gets a list of all {@link net.dv8tion.jda.core.entities.Member Members} who have the same effective name as the one provided.
     * <br>This compares against {@link net.dv8tion.jda.core.entities.Member#getEffectiveName()}}.
     * <br>If there are no {@link net.dv8tion.jda.core.entities.Member Members} with the provided name, then this returns an empty list.
     *
     *
     * @param  name
     *         The name used to filter the returned Members.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all Members with the same effective name as the name provided.
     */
    List<Member> getMembersByEffectiveName(String name, boolean ignoreCase);

    /**
     * Gets a list of {@link net.dv8tion.jda.core.entities.Member Members} that have all
     * {@link net.dv8tion.jda.core.entities.Role Roles} provided.
     * <br>If there are no {@link net.dv8tion.jda.core.entities.Member Members} with all provided roles, then this returns an empty list.
     *
     * @param  roles
     *         The {@link net.dv8tion.jda.core.entities.Role Roles} that a {@link net.dv8tion.jda.core.entities.Member Member}
     *         must have to be included in the returned list.
     *
     * @throws java.lang.IllegalArgumentException
     *         If a provided {@link net.dv8tion.jda.core.entities.Role Role} is from a different guild or null.
     *
     * @return Possibly-empty immutable list of Members with all provided Roles.
     */
    List<Member> getMembersWithRoles(Role... roles);

    /**
     * Gets a list of {@link net.dv8tion.jda.core.entities.Member Members} that have all provided
     * {@link net.dv8tion.jda.core.entities.Role Roles}.
     * <br>If there are no {@link net.dv8tion.jda.core.entities.Member Members} with all provided roles, then this returns an empty list.
     *
     * @param  roles
     *         The {@link net.dv8tion.jda.core.entities.Role Roles} that a {@link net.dv8tion.jda.core.entities.Member Member}
     *         must have to be included in the returned list.
     *
     * @throws java.lang.IllegalArgumentException
     *         If a provided {@link net.dv8tion.jda.core.entities.Role Role} is from a different guild or null.
     *
     * @return Possibly-empty immutable list of Members with all provided Roles.
     */
    List<Member> getMembersWithRoles(Collection<Role> roles);

    /**
     * Gets a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} from this guild that has the same id as the
     * one provided. This method is similar to {@link net.dv8tion.jda.core.JDA#getTextChannelById(String)}, but it only
     * checks this specific Guild for a TextChannel.
     * <br>If there is no {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} with matching id.
     */
    TextChannel getTextChannelById(String id);

    /**
     * Gets all {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} in this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>The channels returned will be sorted according to their position.
     *
     * @return An immutable List of all {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} in this Guild.
     */
    List<TextChannel> getTextChannels();

    /**
     * Gets a list of all {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} in this Guild that have the same
     * name as the one provided.
     * <br>If there are no {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} with the provided name, then this returns an empty list.
     *
     * @param  name
     *         The name used to filter the returned {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all TextChannels names that match the provided name.
     */
    List<TextChannel> getTextChannelsByName(String name, boolean ignoreCase);

    /**
     * Gets a {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} from this guild that has the same id as the
     * one provided. This method is similar to {@link net.dv8tion.jda.core.JDA#getVoiceChannelById(String)}, but it only
     * checks this specific Guild for a VoiceChannel.
     * <br>If there is no {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with matching id.
     */
    VoiceChannel getVoiceChannelById(String id);

    /**
     * Gets all {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels} in this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>The channels returned will be sorted according to their position.
     *
     * @return An immutable List of {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}.
     */
    List<VoiceChannel> getVoiceChannels();

    /**
     * Gets a list of all {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels} in this Guild that have the same
     * name as the one provided.
     * <br>If there are no {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels} with the provided name, then this returns an empty list.
     *
     * @param  name
     *         The name used to filter the returned {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all VoiceChannel names that match the provided name.
     */
    List<VoiceChannel> getVoiceChannelsByName(String name, boolean ignoreCase);

    /**
     * Gets a {@link net.dv8tion.jda.core.entities.Role Role} from this guild that has the same id as the
     * one provided.
     * <br>If there is no {@link net.dv8tion.jda.core.entities.Role Role} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Role Role} with matching id.
     */
    Role getRoleById(String id);

    /**
     * Gets all {@link net.dv8tion.jda.core.entities.Role Roles} in this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>The roles returned will be sorted according to their position.
     *
     * @return An immutable List of {@link net.dv8tion.jda.core.entities.Role Roles}.
     */
    List<Role> getRoles();

    /**
     * Gets a list of all {@link net.dv8tion.jda.core.entities.Role Roles} in this Guild that have the same
     * name as the one provided.
     * <br>If there are no {@link net.dv8tion.jda.core.entities.Role Roles} with the provided name, then this returns an empty list.
     *
     * @param  name
     *         The name used to filter the returned {@link net.dv8tion.jda.core.entities.Role Roles}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all Role names that match the provided name.
     */
    List<Role> getRolesByName(String name, boolean ignoreCase);

    /**
     * Gets an {@link net.dv8tion.jda.core.entities.Emote Emote} from this guild that has the same id as the
     * one provided.
     * <br>If there is no {@link net.dv8tion.jda.core.entities.Emote Emote} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id
     *         the emote id
     *
     * @return An Emote matching the specified Id.
     */
    Emote getEmoteById(String id);

    /**
     * Gets all custom {@link net.dv8tion.jda.core.entities.Emote Emotes} belonging to this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>Emotes are not ordered in any specific way in the returned list.
     *
     * @return An immutable List of {@link net.dv8tion.jda.core.entities.Emote Emotes}.
     */
    List<Emote> getEmotes();

    /**
     * Gets a list of all {@link net.dv8tion.jda.core.entities.Emote Emotes} in this Guild that have the same
     * name as the one provided.
     * <br>If there are no {@link net.dv8tion.jda.core.entities.Emote Emotes} with the provided name, then this returns an empty list.
     *
     * @param  name
     *         The name used to filter the returned {@link net.dv8tion.jda.core.entities.Emote Emotes}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all Role names that match the provided name.
     */
    List<Emote> getEmotesByName(String name, boolean ignoreCase);

    /**
     * The @everyone {@link net.dv8tion.jda.core.entities.Role Role} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>This role is special because its {@link net.dv8tion.jda.core.entities.Role#getPosition()} is calculated as
     * {@code -1}. All other role positions are 0 or greater. This implies that the public role is <b>always</b> below
     * any custom roles created in this Guild. Additionally, all members of this guild are implied to have this role so
     * it is not included in the list returned by {@link net.dv8tion.jda.core.entities.Member#getRoles()}.
     *
     * @return The @everyone {@link net.dv8tion.jda.core.entities.Role Role}
     */
    Role getPublicRole();

    /**
     * The default {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} for a {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>This is the channel that the Discord client will default to opening when a Guild is opened for the first time
     * either on first load or when accepting an invite.
     * <br>This channel cannot be deleted and the {@link #getPublicRole() Public Role} always has the ability to
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} this channel.
     *
     * @return The {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} representing the public channel for this guild.
     */
    TextChannel getPublicChannel();

    /**
     * Returns the {@link net.dv8tion.jda.core.managers.GuildManager GuildManager} for this Guild, used to modify
     * all properties and settings of the Guild.
     * <br>This manager type is the auto-updating type. This means that single changes are made per update. If you
     * would like batch change functionality (change more than 1 thing in a single REST call) use the Updatable manager
     * system. For Guild: {@link #getManagerUpdatable()}.
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         if the guild is temporarily unavailable ({@link #isAvailable()})
     *
     * @return The Manager of this Guild
     */
    GuildManager getManager();

    /**
     * Returns the {@link net.dv8tion.jda.core.managers.GuildManagerUpdatable Updatable GuildManager} for this Guild, used to modify
     * all properties and settings of the Guild.
     * <br>This manager type is the Updatable type. This means that multiple changes can be made before a REST request is
     * sent to Discord. This manager type is great for clients which wish to display all modifiable fields and update
     * the entity using an "apply" button or something similar.
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         if the guild is temporarily unavailable ({@link #isAvailable()})
     *
     * @return The Updatable Manager of this Guild
     */
    GuildManagerUpdatable getManagerUpdatable();

    /**
     * Returns the {@link net.dv8tion.jda.core.managers.GuildController GuildController} for this Guild. The controller
     * is used to perform all admin style functions in the Guild. A few include: kicking, banning, changing member roles,
     * changing role and channel positions, and more. Checkout the {@link net.dv8tion.jda.core.managers.GuildController GuildController}
     * class for more info.
     *
     * @return The controller for this Guild.
     */
    GuildController getController();

    /**
     * Retrieves the recent mentions for the currently logged in
     * client account in this Guild.
     *
     * <p>The returned {@link net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction MentionPaginationAction}
     * allows to filter by whether the messages mention everyone or a role.
     *
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *         If the currently logged in account is not from {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If this Guild is not currently {@link #isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction MentionPaginationAction}
     *
     * @see    net.dv8tion.jda.core.JDA#asClient()
     * @see    net.dv8tion.jda.client.JDAClient#getRecentMentions(Guild)
     */
    MentionPaginationAction getRecentMentions();

    /**
     * Used to leave a Guild. If the currently logged in account is the owner of this guild ({@link net.dv8tion.jda.core.entities.Guild#getOwner()})
     * then ownership of the Guild needs to be transferred to a different {@link net.dv8tion.jda.core.entities.Member Member}
     * before leaving using {@link GuildController#transferOwnership(Member)}.
     *
     * @throws java.lang.IllegalStateException
     *         Thrown if the currently logged in account is the Owner of this Guild.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link java.lang.Void}
     */
    RestAction<Void> leave();

    /**
     * Used to completely delete a Guild. This can only be done if the currently logged in account is the owner of the Guild.
     * <br>If the account has MFA enabled, use {@link #delete(String)} instead to provide the MFA code.
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         Thrown if the currently logged in account is not the owner of this Guild.
     * @throws java.lang.IllegalStateException
     *         If the currently logged in account has MFA enabled. ({@link net.dv8tion.jda.core.entities.SelfUser#isMfaEnabled()}).
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction} - Type: {@link java.lang.Void}
     */
    RestAction<Void> delete();

    /**
     * Used to completely delete a guild. This can only be done if the currently logged in account is the owner of the Guild.
     * <br>This method is specifically used for when MFA is enabled on the logged in account {@link SelfUser#isMfaEnabled()}.
     * If MFA is not enabled, use {@link #delete()}.
     *
     * @param  mfaCode
     *         The Multifactor Authentication code generated by an app like
     *         <a href="https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2" target="_blank">Google Authenticator</a>.
     *         <br><b>This is not the MFA token given to you by Discord.</b> The code is typically 6 characters long.
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         Thrown if the currently logged in account is not the owner of this Guild.
     * @throws java.lang.IllegalArgumentException
     *         If the provided {@code mfaCode} is {@code null} or empty when {@link SelfUser#isMfaEnabled()} is true.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction} - Type: {@link java.lang.Void}
     */
    RestAction<Void> delete(String mfaCode);

    /**
     * Returns the {@link net.dv8tion.jda.core.managers.AudioManager AudioManager} that represents the
     * audio connection for this Guild.
     *
     * @return The AudioManager for this Guild.
     */
    AudioManager getAudioManager();

    /**
     * Returns the {@link net.dv8tion.jda.core.JDA JDA} instance of this Guild
     *
     * @return the corresponding JDA instance
     */
    JDA getJDA();

    /**
     * Retrieves all {@link net.dv8tion.jda.core.entities.Invite Invites} for this guild.
     * <br>Requires {@link net.dv8tion.jda.core.Permission#MANAGE_SERVER MANAGE_SERVER} in this guild.
     * Will throw a {@link net.dv8tion.jda.core.exceptions.PermissionException PermissionException} otherwise.
     *
     * <p>To get all invites for a {@link net.dv8tion.jda.core.entities.Channel Channel}
     * use {@link net.dv8tion.jda.core.entities.Channel#getInvites() Channel.getInvites()}
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         if the account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_SERVER MANAGE_SERVER} in this Guild.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: List{@literal <}{@link net.dv8tion.jda.core.entities.Invite Invite}{@literal >}
     *         <br>The list of expanded Invite objects
     *
     * @see     net.dv8tion.jda.core.entities.Channel#getInvites()
     */
    RestAction<List<Invite>> getInvites();

    /**
     * Retrieves all {@link net.dv8tion.jda.core.entities.Webhook Webhooks} for this Guild.
     * <br>Requires {@link net.dv8tion.jda.core.Permission#MANAGE_WEBHOOKS MANAGE_WEBHOOKS} in this Guild.
     *
     * <p>To get all webhooks for a specific {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}, use
     * {@link TextChannel#getWebhooks()}
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         if the account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_WEBHOOKS MANAGE_WEBHOOKS} in this Guild.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: List{@literal <}{@link net.dv8tion.jda.core.entities.Webhook Webhook}{@literal >}
     *         <br>A list of all Webhooks in this Guild.
     *
     * @see     TextChannel#getWebhooks()
     */
    RestAction<List<Webhook>> getWebhooks();

    /**
     * A list containing the {@link net.dv8tion.jda.core.entities.GuildVoiceState GuildVoiceState} of every {@link net.dv8tion.jda.core.entities.Member Member}
     * in this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>This will never return an empty list because if it were empty, that would imply that there are no
     * {@link net.dv8tion.jda.core.entities.Member Members} in this {@link net.dv8tion.jda.core.entities.Guild Guild}, which is
     * impossible.
     *
     * @return Never-empty list containing all the {@link GuildVoiceState GuildVoiceStates} on this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     */
    List<GuildVoiceState> getVoiceStates();

    /**
     * Returns the verification-Level of this Guild. Verification level is one of the factors that determines if a Member
     * can send messages in a Guild.
     * <br>For a short description of the different values, see {@link net.dv8tion.jda.core.entities.Guild.VerificationLevel}.
     * <p>
     * This value can be modified using {@link GuildManager#setVerificationLevel(net.dv8tion.jda.core.entities.Guild.VerificationLevel)}
     * or {@link net.dv8tion.jda.core.managers.GuildManagerUpdatable#getVerificationLevelField()}
     *
     * @return The Verification-Level of this Guild.
     */
    VerificationLevel getVerificationLevel();

    /**
     * Returns the default message Notification-Level of this Guild. Notification level determines when Members get notification
     * for messages. The value returned is the default level set for any new Members that join the Guild.
     * <br>For a short description of the different values, see {@link net.dv8tion.jda.core.entities.Guild.NotificationLevel NotificationLevel}.
     * <p>
     * This value can be modified using {@link GuildManager#setDefaultNotificationLevel(net.dv8tion.jda.core.entities.Guild.NotificationLevel)}
     * or {@link net.dv8tion.jda.core.managers.GuildManagerUpdatable#getDefaultNotificationLevelField()}
     *
     * @return The default message Notification-Level of this Guild.
     */
    NotificationLevel getDefaultNotificationLevel();

    /**
     * Returns the level of multifactor authentication required to execute administrator restricted functions in this guild.
     * <br>For a short description of the different values, see {@link net.dv8tion.jda.core.entities.Guild.MFALevel MFALevel}.
     * <p>
     * This value can be modified using {@link GuildManager#setRequiredMFALevel(net.dv8tion.jda.core.entities.Guild.MFALevel)}
     * or {@link net.dv8tion.jda.core.managers.GuildManagerUpdatable#getRequiredMFALevelField()}
     *
     * @return The MFA-Level required by this Guild.
     */
    MFALevel getRequiredMFALevel();

    /**
     * Checks if the current Verification-level of this guild allows JDA to send messages to it.
     *
     * @return True if Verification-level allows sending of messages, false if not.
     *
     * @see    net.dv8tion.jda.core.entities.Guild.VerificationLevel
     *         VerificationLevel Enum with a list of possible verification-levels and their requirements
     */
    boolean checkVerification();

    /**
     * Returns whether or not this Guild is available. A Guild can be unavailable, if the Discord server has problems.
     * <br>If a Guild is unavailable, no actions on it can be performed (Messages, Manager,...)
     *
     * @return If the Guild is available
     */
    boolean isAvailable();

    /**
     * Represents the idle time allowed until a user is moved to the
     * AFK {@link net.dv8tion.jda.core.entities.VoiceChannel} if one is set
     * ({@link net.dv8tion.jda.core.entities.Guild#getAfkChannel() Guild.getAfkChannel()}).
     */
    enum Timeout
    {
        SECONDS_60(60),
        SECONDS_300(300),
        SECONDS_900(900),
        SECONDS_1800(1800),
        SECONDS_3600(3600);

        private final int seconds;

        Timeout(int seconds)
        {
            this.seconds = seconds;
        }

        /**
         * The amount of seconds represented by this {@link Timeout}.
         *
         * @return An positive non-negative int representing the timeout amount in seconds.
         */
        public int getSeconds()
        {
            return seconds;
        }

        /**
         * Retrieves the {@link net.dv8tion.jda.core.entities.Guild.Timeout Timeout} based on the amount of seconds requested.
         * <br>If the {@code seconds} amount provided is not valid for Discord, an IllegalArgumentException will be thrown.
         *
         * @param  seconds
         *         The amount of seconds before idle timeout.
         *
         * @throws java.lang.IllegalArgumentException
         *         If the provided {@code seconds} is an invalid timeout amount.
         *
         * @return The {@link net.dv8tion.jda.core.entities.Guild.Timeout Timeout} related to the amount of seconds provided.
         */
        public static Timeout fromKey(int seconds)
        {
            for (Timeout t : values())
            {
                if (t.getSeconds() == seconds)
                    return t;
            }
            throw new IllegalArgumentException("Provided key was not recognized. Seconds: " + seconds);
        }
    }

    /**
     * Represents the Verification-Level of the Guild.
     * The Verification-Level determines what requirement you have to meet to be able to speak in this Guild.
     * <p>
     * <br><b>None</b>   {@literal ->} everyone can talk.
     * <br><b>Low</b>    {@literal ->} verified email required.
     * <br><b>Medium</b> {@literal ->} you have to be member of discord for at least 5min.
     * <br><b>High</b>   {@literal ->} you have to be member of this guild for at least 10min.
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

        /**
         * The Discord id key for this Verification Level.
         *
         * @return Integer id key for this VerificationLevel.
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Used to retrieve a {@link net.dv8tion.jda.core.entities.Guild.VerificationLevel VerificationLevel} based
         * on the Discord id key.
         *
         * @param  key
         *         The Discord id key representing the requested VerificationLevel.
         *
         * @return The VerificationLevel related to the provided key, or {@link #UNKNOWN VerificationLevel.UNKNOWN} if the key is not recognized.
         */
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
     * The Verification-Level determines what messages you receive pings for.
     * <p>
     * <br><b>All_Messages</b>   {@literal ->} Every message sent in this guild will result in a message ping.
     * <br><b>Mentions_Only</b>  {@literal ->} Only messages that specifically mention will result in a ping.
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

        /**
         * The Discord id key used to represent this NotificationLevel.
         *
         * @return Integer id for this NotificationLevel.
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Used to retrieve a {@link net.dv8tion.jda.core.entities.Guild.NotificationLevel NotificationLevel} based
         * on the Discord id key.
         *
         * @param  key
         *         The Discord id key representing the requested NotificationLevel.
         *
         * @return The NotificationLevel related to the provided key, or {@link #UNKNOWN NotificationLevel.UNKNOWN} if the key is not recognized.
         */
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
     * <br>The MFA Level restricts administrator functions to account with MFA Level equal to or higher than that set by the guild.
     * <p>
     * <br><b>None</b>             {@literal ->} There is no MFA level restriction on administrator functions in this guild.
     * <br><b>Two_Factor_Auth</b>  {@literal ->} Users must have 2FA enabled on their account to perform administrator functions.
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

        /**
         * The Discord id key used to represent this MFALevel.
         *
         * @return Integer id for this MFALevel.
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Used to retrieve a {@link net.dv8tion.jda.core.entities.Guild.MFALevel MFALevel} based
         * on the Discord id key.
         *
         * @param  key
         *         The Discord id key representing the requested MFALevel.
         *
         * @return The MFALevel related to the provided key, or {@link #UNKNOWN MFALevel.UNKNOWN} if the key is not recognized.
         */
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
