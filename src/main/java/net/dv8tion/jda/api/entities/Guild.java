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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.channel.IGuildChannelContainer;
import net.dv8tion.jda.api.entities.templates.Template;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.*;
import net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction;
import net.dv8tion.jda.api.requests.restaction.order.ChannelOrderAction;
import net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.api.requests.restaction.pagination.PaginationAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.cache.MemberCacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView;
import net.dv8tion.jda.api.utils.concurrent.Task;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.requests.DeferredRestAction;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.concurrent.task.GatewayTask;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents a Discord {@link net.dv8tion.jda.api.entities.Guild Guild}.
 * This should contain all information provided from Discord about a Guild.
 *
 * @see JDA#getGuildCache()
 * @see JDA#getGuildById(long)
 * @see JDA#getGuildsByName(String, boolean)
 * @see JDA#getGuilds()
 */
public interface Guild extends IGuildChannelContainer, ISnowflake
{
    /** Template for {@link #getIconUrl()}. */
    String ICON_URL = "https://cdn.discordapp.com/icons/%s/%s.%s";
    /** Template for {@link #getSplashUrl()}. */
    String SPLASH_URL = "https://cdn.discordapp.com/splashes/%s/%s.png";
    /** Template for {@link #getBannerUrl()}. */
    String BANNER_URL = "https://cdn.discordapp.com/banners/%s/%s.%s";

    /**
     * Retrieves the list of guild commands.
     * <br>This list does not include global commands! Use {@link JDA#retrieveCommands()} for global commands.
     *
     * @return {@link RestAction} - Type: {@link List} of {@link Command}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<Command>> retrieveCommands();

    /**
     * Retrieves the existing {@link Command} instance by id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  id
     *         The command id
     *
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake
     *
     * @return {@link RestAction} - Type: {@link Command}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Command> retrieveCommandById(@Nonnull String id);

    /**
     * Retrieves the existing {@link Command} instance by id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  id
     *         The command id
     *
     * @return {@link RestAction} - Type: {@link Command}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Command> retrieveCommandById(long id)
    {
        return retrieveCommandById(Long.toUnsignedString(id));
    }

    /**
     * Creates or updates a command.
     * <br>If a command with the same name exists, it will be replaced.
     * This operation is idempotent.
     * Commands will persist between restarts of your bot, you only have to create a command once.
     *
     * <p>To specify a complete list of all commands you can use {@link #updateCommands()} instead.
     *
     * <p>You need the OAuth2 scope {@code "applications.commands"} in order to add commands to a guild.
     *
     * @param  command
     *         The {@link CommandData} for the command
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link RestAction} - Type: {@link Command}
     *         <br>The RestAction used to create or update the command
     *
     * @see    Commands#slash(String, String) Commands.slash(...)
     * @see    Commands#message(String) Commands.message(...)
     * @see    Commands#user(String) Commands.user(...)
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Command> upsertCommand(@Nonnull CommandData command);

    /**
     * Creates or updates a slash command.
     * <br>If a command with the same name exists, it will be replaced.
     * This operation is idempotent.
     * Commands will persist between restarts of your bot, you only have to create a command once.
     *
     * <p>To specify a complete list of all commands you can use {@link #updateCommands()} instead.
     *
     * <p>You need the OAuth2 scope {@code "applications.commands"} in order to add commands to a guild.
     *
     * @param  name
     *         The lowercase alphanumeric (with dash) name, 1-32 characters
     * @param  description
     *         The description for the command, 1-100 characters
     *
     * @throws IllegalArgumentException
     *         If null is provided or the name/description do not meet the requirements
     *
     * @return {@link CommandCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    default CommandCreateAction upsertCommand(@Nonnull String name, @Nonnull String description)
    {
        return (CommandCreateAction) upsertCommand(new CommandDataImpl(name, description));
    }

    /**
     * Configures the complete list of guild commands.
     * <br>This will replace the existing command list for this guild. You should only use this at most once on startup!
     *
     * <p>This operation is idempotent.
     * Commands will persist between restarts of your bot, you only have to create a command once.
     *
     * <p>You need the OAuth2 scope {@code "applications.commands"} in order to add commands to a guild.
     *
     * <h4>Examples</h4>
     * <pre>{@code
     * // Set list to 2 commands
     * guild.updateCommands()
     *   .addCommands(Commands.slash("ping", "Gives the current ping"))
     *   .addCommands(Commands.slash("ban", "Ban the target user")
     *     .addOption(OptionType.USER, "user", "The user to ban", true))
     *   .queue();
     * // Delete all commands
     * guild.updateCommands().queue();
     * }</pre>
     *
     * @return {@link CommandListUpdateAction}
     *
     * @see    JDA#updateCommands()
     */
    @Nonnull
    @CheckReturnValue
    CommandListUpdateAction updateCommands();

    /**
     * Edit an existing command by id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  id
     *         The id of the command to edit
     *
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake
     *
     * @return {@link CommandEditAction} used to edit the command
     */
    @Nonnull
    @CheckReturnValue
    CommandEditAction editCommandById(@Nonnull String id);

    /**
     * Edit an existing command by id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  id
     *         The id of the command to edit
     *
     * @return {@link CommandEditAction} used to edit the command
     */
    @Nonnull
    @CheckReturnValue
    default CommandEditAction editCommandById(long id)
    {
        return editCommandById(Long.toUnsignedString(id));
    }

    /**
     * Delete the command for this id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  commandId
     *         The id of the command that should be deleted
     *
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> deleteCommandById(@Nonnull String commandId);

    /**
     * Delete the command for this id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  commandId
     *         The id of the command that should be deleted
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> deleteCommandById(long commandId)
    {
        return deleteCommandById(Long.toUnsignedString(commandId));
    }

    /**
     * Retrieves the {@link CommandPrivilege CommandPrivileges} for the command with the specified ID.
     *
     * <p>These privileges are used to restrict who can use commands through Role/User whitelists/blacklists.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  commandId
     *         The id of the command, this can be global or guild command
     *
     * @throws IllegalArgumentException
     *         If the id is not a valid snowflake
     *
     * @return {@link RestAction} - Type: {@link List} of {@link CommandPrivilege}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<CommandPrivilege>> retrieveCommandPrivilegesById(@Nonnull String commandId);

    /**
     * Retrieves the {@link CommandPrivilege CommandPrivileges} for the command with the specified ID.
     *
     * <p>These privileges are used to restrict who can use commands through Role/User whitelists/blacklists.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  commandId
     *         The id of the command, this can be global or guild command
     *
     * @throws IllegalArgumentException
     *         If the id is not a valid snowflake
     *
     * @return {@link RestAction} - Type: {@link List} of {@link CommandPrivilege}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<List<CommandPrivilege>> retrieveCommandPrivilegesById(long commandId)
    {
        return retrieveCommandPrivilegesById(Long.toUnsignedString(commandId));
    }

    /**
     * Retrieves the {@link CommandPrivilege CommandPrivileges} for the commands in this guild.
     * <br>The RestAction provides a {@link Map} from the command id to the list of privileges.
     *
     * <p>These privileges are used to restrict who can use commands through Role/User whitelists/blacklists.
     *
     * @return {@link RestAction} - Type: {@link Map} from {@link String} Command ID to {@link List} of {@link CommandPrivilege}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Map<String, List<CommandPrivilege>>> retrieveCommandPrivileges();

    /**
     * Updates the list of {@link CommandPrivilege CommandPrivileges} for the specified command.
     * <br>Note that commands are enabled by default for all members of a guild, which means you can only <em>blacklist</em> roles and members using this method.
     * To change this behavior, use {@link CommandData#setDefaultEnabled(boolean)} on your command.
     *
     * <p>These privileges are used to restrict who can use commands through Role/User whitelists/blacklists.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  id
     *         The id of the command, this can be global or guild command
     * @param  privileges
     *         Complete list of up to 10 {@link CommandPrivilege CommandPrivileges} for this command
     *
     * @throws IllegalArgumentException
     *         If null is provided, the id is not a valid snowflake, or more than 10 privileges are provided
     *
     * @return {@link RestAction} - Type: {@link List} or {@link CommandPrivilege}
     *         The updated list of privileges for this command.
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<CommandPrivilege>> updateCommandPrivilegesById(@Nonnull String id, @Nonnull Collection<? extends CommandPrivilege> privileges);

    /**
     * Updates the list of {@link CommandPrivilege CommandPrivileges} for the specified command.
     * <br>Note that commands are enabled by default for all members of a guild, which means you can only <em>blacklist</em> roles and members using this method.
     * To change this behavior, use {@link CommandData#setDefaultEnabled(boolean)} on your command.
     *
     * <p>These privileges are used to restrict who can use commands through Role/User whitelists/blacklists.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  id
     *         The id of the command, this can be global or guild command
     * @param  privileges
     *         Complete list of up to 10 {@link CommandPrivilege CommandPrivileges} for this command
     *
     * @throws IllegalArgumentException
     *         If null is provided, the id is not a valid snowflake, or more than 10 privileges are provided
     *
     * @return {@link RestAction} - Type: {@link List} or {@link CommandPrivilege}
     *         The updated list of privileges for this command.
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<List<CommandPrivilege>> updateCommandPrivilegesById(@Nonnull String id, @Nonnull CommandPrivilege... privileges)
    {
        Checks.noneNull(privileges, "CommandPrivileges");
        return updateCommandPrivilegesById(id, Arrays.asList(privileges));
    }

    /**
     * Updates the list of {@link CommandPrivilege CommandPrivileges} for the specified command.
     * <br>Note that commands are enabled by default for all members of a guild, which means you can only <em>blacklist</em> roles and members using this method.
     * To change this behavior, use {@link CommandData#setDefaultEnabled(boolean)} on your command.
     *
     * <p>These privileges are used to restrict who can use commands through Role/User whitelists/blacklists.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  id
     *         The id of the command, this can be global or guild command
     * @param  privileges
     *         Complete list of up to 10 {@link CommandPrivilege CommandPrivileges} for this command
     *
     * @throws IllegalArgumentException
     *         If null is provided or more than 10 privileges are provided
     *
     * @return {@link RestAction} - Type: {@link List} or {@link CommandPrivilege}
     *         The updated list of privileges for this command.
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<List<CommandPrivilege>> updateCommandPrivilegesById(long id, @Nonnull Collection<? extends CommandPrivilege> privileges)
    {
        return updateCommandPrivilegesById(Long.toUnsignedString(id), privileges);
    }

    /**
     * Updates the list of {@link CommandPrivilege CommandPrivileges} for the specified command.
     * <br>Note that commands are enabled by default for all members of a guild, which means you can only <em>blacklist</em> roles and members using this method.
     * To change this behavior, use {@link CommandData#setDefaultEnabled(boolean)} on your command.
     *
     * <p>These privileges are used to restrict who can use commands through Role/User whitelists/blacklists.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  id
     *         The id of the command, this can be global or guild command
     * @param  privileges
     *         Complete list of up to 10 {@link CommandPrivilege CommandPrivileges} for this command
     *
     * @throws IllegalArgumentException
     *         If null is provided or more than 10 privileges are provided
     *
     * @return {@link RestAction} - Type: {@link List} or {@link CommandPrivilege}
     *         The updated list of privileges for this command.
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<List<CommandPrivilege>> updateCommandPrivilegesById(long id, @Nonnull CommandPrivilege... privileges)
    {
        Checks.noneNull(privileges, "CommandPrivileges");
        return updateCommandPrivilegesById(id, Arrays.asList(privileges));
    }

    /**
     * Updates the list of {@link CommandPrivilege CommandPrivileges} for the specified commands.
     * <br>The argument for this function is a {@link Map} similar to the one returned by {@link #retrieveCommandPrivileges()}.
     * <br>Note that commands are enabled by default for all members of a guild, which means you can only <em>blacklist</em> roles and members using this method.
     * To change this behavior, use {@link CommandData#setDefaultEnabled(boolean)} on your command.
     *
     * <p>These privileges are used to restrict who can use commands through Role/User whitelists/blacklists.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  privileges
     *         Complete map of {@link CommandPrivilege CommandPrivileges} for each command
     *
     * @throws IllegalArgumentException
     *         If null is provided, any of the map keys is not a valid snowflake, or more than 10 privileges are provided for any command
     *
     * @return {@link RestAction} - Type: {@link Map} from {@link String} Command ID to {@link List} of {@link CommandPrivilege}
     *         The updated map of command privileges for this guild.
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Map<String, List<CommandPrivilege>>> updateCommandPrivileges(@Nonnull Map<String, ? extends Collection<CommandPrivilege>> privileges);

    /**
     * Retrieves the available regions for this Guild
     * <br>Shortcut for {@link #retrieveRegions(boolean) retrieveRegions(true)}
     * <br>This will include deprecated voice regions by default.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type {@link java.util.EnumSet EnumSet}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<EnumSet<Region>> retrieveRegions()
    {
        return retrieveRegions(true);
    }

    /**
     * Retrieves the available regions for this Guild
     *
     * @param  includeDeprecated
     *         Whether to include deprecated regions
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type {@link java.util.EnumSet EnumSet}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<EnumSet<Region>> retrieveRegions(boolean includeDeprecated);

    /**
     * Adds the user represented by the provided id to this guild.
     * <br>This requires an <b>OAuth2 Access Token</b> with the scope {@code guilds.join}.
     *
     * @param  accessToken
     *         The access token
     * @param  userId
     *         The user id
     *
     * @throws IllegalArgumentException
     *         If the user id or access token is blank, empty, or null,
     *         or if the provided user is already in this guild
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#CREATE_INSTANT_INVITE Permission.CREATE_INSTANT_INVITE}
     *
     * @return {@link MemberAction MemberAction}
     *
     * @see    <a href="https://discord.com/developers/docs/topics/oauth2" target="_blank">Discord OAuth2 Documentation</a>
     *
     * @since  3.7.0
     */
    @Nonnull
    @CheckReturnValue
    MemberAction addMember(@Nonnull String accessToken, @Nonnull String userId);

    /**
     * Adds the provided user to this guild.
     * <br>This requires an <b>OAuth2 Access Token</b> with the scope {@code guilds.join}.
     *
     * @param  accessToken
     *         The access token
     * @param  user
     *         The user
     *
     * @throws IllegalArgumentException
     *         If the user or access token is blank, empty, or null,
     *         or if the provided user is already in this guild
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#CREATE_INSTANT_INVITE Permission.CREATE_INSTANT_INVITE}
     *
     * @return {@link MemberAction MemberAction}
     *
     * @see    <a href="https://discord.com/developers/docs/topics/oauth2" target="_blank">Discord OAuth2 Documentation</a>
     *
     * @since  3.7.0
     */
    @Nonnull
    @CheckReturnValue
    default MemberAction addMember(@Nonnull String accessToken, @Nonnull User user)
    {
        Checks.notNull(user, "User");
        return addMember(accessToken, user.getId());
    }

    /**
     * Adds the user represented by the provided id to this guild.
     * <br>This requires an <b>OAuth2 Access Token</b> with the scope {@code guilds.join}.
     *
     * @param  accessToken
     *         The access token
     * @param  userId
     *         The user id
     *
     * @throws IllegalArgumentException
     *         If the user id or access token is blank, empty, or null,
     *         or if the provided user is already in this guild
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#CREATE_INSTANT_INVITE Permission.CREATE_INSTANT_INVITE}
     *
     * @return {@link MemberAction MemberAction}
     *
     * @see    <a href="https://discord.com/developers/docs/topics/oauth2" target="_blank">Discord OAuth2 Documentation</a>
     *
     * @since  3.7.0
     */
    @Nonnull
    @CheckReturnValue
    default MemberAction addMember(@Nonnull String accessToken, long userId)
    {
        return addMember(accessToken, Long.toUnsignedString(userId));
    }

    /**
     * Whether this guild has loaded members.
     * <br>This will always be false if the {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent is disabled.
     *
     * @return True, if members are loaded.
     */
    boolean isLoaded();

    /**
     * Re-apply the {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy} of this session to all {@link Member Members} of this Guild.
     *
     * <h4>Example</h4>
     * <pre>{@code
     * // Check if the members of this guild have at least 50% bots (bot collection/farm)
     * public void checkBots(Guild guild) {
     *     // Keep in mind: This requires the GUILD_MEMBERS intent which is disabled in createDefault and createLight by default
     *     guild.retrieveMembers() // Load members CompletableFuture<Void> (async and eager)
     *          .thenApply((v) -> guild.getMemberCache()) // Turn into CompletableFuture<MemberCacheView>
     *          .thenAccept((members) -> {
     *              int total = members.size();
     *              // Casting to double to get a double as result of division, don't need to worry about precision with small counts like this
     *              double bots = (double) members.applyStream(stream ->
     *                  stream.map(Member::getUser)
     *                        .filter(User::isBot)
     *                        .count()); // Count bots
     *              if (bots / total > 0.5) // Check how many members are bots
     *                  System.out.println("More than 50% of members in this guild are bots");
     *          })
     *          .thenRun(guild::pruneMemberCache); // Then prune the cache
     * }
     * }</pre>
     *
     * @see #unloadMember(long)
     * @see JDA#unloadUser(long)
     */
    void pruneMemberCache();

    /**
     * Attempts to remove the user with the provided id from the member cache.
     * <br>If you attempt to remove the {@link JDA#getSelfUser() SelfUser} this will simply return {@code false}.
     *
     * <p>This should be used by an implementation of {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     * as an upstream request to remove a member. For example a Least-Recently-Used (LRU) cache might use this to drop
     * old members if the cache capacity is reached. Or a timeout cache could use this to remove expired members.
     *
     * @param  userId
     *         The target user id
     *
     * @return True, if the cache was changed
     *
     * @see    #pruneMemberCache()
     * @see    JDA#unloadUser(long)
     */
    boolean unloadMember(long userId);

    /**
     * The expected member count for this guild.
     * <br>If this guild is not lazy loaded this should be identical to the size returned by {@link #getMemberCache()}.
     *
     * <p>When {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} is disabled, this will not be updated.
     *
     * @return The expected member count for this guild
     */
    int getMemberCount();

    /**
     * The human readable name of the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <p>
     * This value can be modified using {@link GuildManager#setName(String)}.
     *
     * @return Never-null String containing the Guild's name.
     */
    @Nonnull
    String getName();

    /**
     * The Discord hash-id of the {@link net.dv8tion.jda.api.entities.Guild Guild} icon image.
     * If no icon has been set, this returns {@code null}.
     * <p>
     * The Guild icon can be modified using {@link GuildManager#setIcon(Icon)}.
     *
     * @return Possibly-null String containing the Guild's icon hash-id.
     */
    @Nullable
    String getIconId();

    /**
     * The URL of the {@link net.dv8tion.jda.api.entities.Guild Guild} icon image.
     * If no icon has been set, this returns {@code null}.
     * <p>
     * The Guild icon can be modified using {@link GuildManager#setIcon(Icon)}.
     *
     * @return Possibly-null String containing the Guild's icon URL.
     */
    @Nullable
    default String getIconUrl()
    {
        String iconId = getIconId();
        return iconId == null ? null : String.format(ICON_URL, getId(), iconId, iconId.startsWith("a_") ? "gif" : "png");
    }

    /**
     * The Features of the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <p>
     * <a target="_blank" href="https://discord.com/developers/docs/resources/guild#guild-object-guild-features"><b>List of Features</b></a>
     *
     *
     * @return Never-null, unmodifiable Set containing all of the Guild's features.
     */
    @Nonnull
    Set<String> getFeatures();

    /**
     * The Discord hash-id of the splash image for this Guild. A Splash image is an image displayed when viewing a
     * Discord Guild Invite on the web or in client just before accepting or declining the invite.
     * If no splash has been set, this returns {@code null}.
     * <br>Splash images are VIP/Partner Guild only.
     * <p>
     * The Guild splash can be modified using {@link GuildManager#setSplash(Icon)}.
     *
     * @return Possibly-null String containing the Guild's splash hash-id
     */
    @Nullable
    String getSplashId();

    /**
     * The URL of the splash image for this Guild. A Splash image is an image displayed when viewing a
     * Discord Guild Invite on the web or in client just before accepting or declining the invite.
     * If no splash has been set, this returns {@code null}.
     * <br>Splash images are VIP/Partner Guild only.
     * <p>
     * The Guild splash can be modified using {@link GuildManager#setSplash(Icon)}.
     *
     * @return Possibly-null String containing the Guild's splash URL.
     */
    @Nullable
    default String getSplashUrl()
    {
        String splashId = getSplashId();
        return splashId == null ? null : String.format(SPLASH_URL, getId(), splashId);
    }

    /**
     * The vanity url code for this Guild. The vanity url is the custom invite code of partnered / official / boosted Guilds.
     * <br>The returned String will be the code that can be provided to {@code discord.gg/{code}} to get the invite link.
     *
     * @return The vanity code or null
     *
     * @since  4.0.0
     *
     * @see    #getVanityUrl()
     */
    @Nullable
    String getVanityCode();

    /**
     * The vanity url for this Guild. The vanity url is the custom invite code of partnered / official / boosted Guilds.
     * <br>The returned String will be the vanity invite link to this guild.
     *
     * @return The vanity url or null
     *
     * @since  4.0.0
     */
    @Nullable
    default String getVanityUrl()
    {
        return getVanityCode() == null ? null : "https://discord.gg/" + getVanityCode();
    }

    /**
     * Retrieves the Vanity Invite meta data for this guild.
     * <br>This allows you to inspect how many times the vanity invite has been used.
     * You can use {@link #getVanityUrl()} if you only care about the invite.
     *
     * <p>This action requires the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVITE_CODE_INVALID INVITE_CODE_INVALID}
     *     <br>If this guild does not have a vanity invite</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The vanity invite cannot be fetched due to a permission discrepancy</li>
     * </ul>
     *
     * @throws InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_SERVER Permission.MANAGE_SERVER}
     *
     * @return {@link RestAction} - Type: {@link VanityInvite}
     *
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    RestAction<VanityInvite> retrieveVanityInvite();

    /**
     * The description for this guild.
     * <br>This is displayed in the server browser below the guild name for verified guilds.
     *
     * <p>The description can be modified using {@link GuildManager#setDescription(String)}.
     *
     * @return The description
     *
     * @since  4.0.0
     */
    @Nullable
    String getDescription();

    /**
     * The preferred locale for this guild.
     * <br>If the guild doesn't have the COMMUNITY feature, this returns the default.
     *
     * <br>Default: {@link Locale#US}
     *
     * @return The preferred {@link Locale} for this guild
     *
     * @since  4.2.1
     */
    @Nonnull
    Locale getLocale();

    /**
     * The guild banner id.
     * <br>This is shown in guilds below the guild name.
     *
     * <p>The banner can be modified using {@link GuildManager#setBanner(Icon)}.
     *
     * @return The guild banner id or null
     *
     * @since  4.0.0
     *
     * @see    #getBannerUrl()
     */
    @Nullable
    String getBannerId();

    /**
     * The guild banner url.
     * <br>This is shown in guilds below the guild name.
     *
     * <p>The banner can be modified using {@link GuildManager#setBanner(Icon)}.
     *
     * @return The guild banner url or null
     *
     * @since  4.0.0
     */
    @Nullable
    default String getBannerUrl()
    {
        String bannerId = getBannerId();
        return bannerId == null ? null : String.format(BANNER_URL, getId(), bannerId, bannerId.startsWith("a_") ? "gif" : "png");
    }

    /**
     * The boost tier for this guild.
     * <br>Each tier unlocks new perks for a guild that can be seen in the {@link #getFeatures() features}.
     *
     * @return The boost tier.
     *
     * @since  4.0.0
     */
    @Nonnull
    BoostTier getBoostTier();

    /**
     * The amount of boosts this server currently has.
     *
     * @return The boost count
     *
     * @since  4.0.0
     */
    int getBoostCount();

    /**
     * Sorted list of {@link net.dv8tion.jda.api.entities.Member Members} that boost this guild.
     * <br>The list is sorted by {@link net.dv8tion.jda.api.entities.Member#getTimeBoosted()} ascending.
     * This means the first element will be the member who has been boosting for the longest time.
     *
     * @return Possibly-immutable list of members who boost this guild
     */
    @Nonnull
    List<Member> getBoosters();

    /**
     * The maximum bitrate that can be applied to a voice channel in this guild.
     * <br>This depends on the features of this guild that can be unlocked for partners or through boosting.
     *
     * @return The maximum bitrate
     *
     * @since  4.0.0
     */
    default int getMaxBitrate()
    {
        int maxBitrate = getFeatures().contains("VIP_REGIONS") ? 384000 : 96000;
        return Math.max(maxBitrate, getBoostTier().getMaxBitrate());
    }

    /**
     * Returns the maximum size for files that can be uploaded to this Guild.
     * This returns 8 MiB for Guilds without a Boost Tier or Guilds with Boost Tier 1, 50 MiB for Guilds with Boost Tier 2 and 100 MiB for Guilds with Boost Tier 3.
     *
     * @return The maximum size for files that can be uploaded to this Guild
     *
     * @since 4.2.0
     */
    default long getMaxFileSize()
    {
        return getBoostTier().getMaxFileSize();
    }

    /**
     * The maximum amount of emotes a guild can have based on the guilds boost tier.
     *
     * @return The maximum amount of emotes
     *
     * @since 4.0.0
     */
    default int getMaxEmotes()
    {
        int maxEmotes = getFeatures().contains("MORE_EMOJI") ? 200 : 50;
        return Math.max(maxEmotes, getBoostTier().getMaxEmotes());
    }

    /**
     * The maximum amount of members that can join this guild.
     *
     * @return The maximum amount of members
     *
     * @since  4.0.0
     *
     * @see    #retrieveMetaData()
     */
    int getMaxMembers();

    /**
     * The maximum amount of connected members this guild can have at a time.
     * <br>This includes members that are invisible but still connected to discord.
     * If too many members are online the guild will become unavailable for others.
     *
     * @return The maximum amount of connected members this guild can have
     *
     * @since  4.0.0
     *
     * @see    #retrieveMetaData()
     */
    int getMaxPresences();

    /**
     * Loads {@link MetaData} for this guild instance.
     *
     * @return {@link RestAction} - Type: {@link MetaData}
     *
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    RestAction<MetaData> retrieveMetaData();

    /**
     * Provides the {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} that has been set as the channel
     * which {@link net.dv8tion.jda.api.entities.Member Members} will be moved to after they have been inactive in a
     * {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} for longer than {@link #getAfkTimeout()}.
     * <br>If no channel has been set as the AFK channel, this returns {@code null}.
     * <p>
     * This value can be modified using {@link GuildManager#setAfkChannel(VoiceChannel)}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} that is the AFK Channel.
     */
    @Nullable
    VoiceChannel getAfkChannel();

    /**
     * Provides the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} that has been set as the channel
     * which newly joined {@link net.dv8tion.jda.api.entities.Member Members} will be announced in.
     * <br>If no channel has been set as the system channel, this returns {@code null}.
     * <p>
     * This value can be modified using {@link GuildManager#setSystemChannel(TextChannel)}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} that is the system Channel.
     */
    @Nullable
    TextChannel getSystemChannel();

    /**
     * Provides the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} that lists the rules of the guild.
     * <br>If this guild doesn't have the COMMUNITY {@link #getFeatures() feature}, this returns {@code null}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} that is the rules channel
     *
     * @see    #getFeatures()
     */
    @Nullable
    TextChannel getRulesChannel();

    /**
     * Provides the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} that receives community updates.
     * <br>If this guild doesn't have the COMMUNITY {@link #getFeatures() feature}, this returns {@code null}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} that is the community updates channel
     *
     * @see    #getFeatures()
     */
    @Nullable
    TextChannel getCommunityUpdatesChannel();

    /**
     * The {@link net.dv8tion.jda.api.entities.Member Member} object for the owner of this Guild.
     * <br>This is null when the owner is no longer in this guild or not yet loaded (lazy loading).
     * Sometimes owners of guilds delete their account or get banned by Discord.
     *
     * <p>If lazy-loading is used it is recommended to use {@link #retrieveOwner()} instead.
     *
     * <p>Ownership can be transferred using {@link net.dv8tion.jda.api.entities.Guild#transferOwnership(Member)}.
     *
     * <p>This only works when the member was added to cache. Lazy loading might load this later.
     * <br>See {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     *
     * @return Possibly-null Member object for the Guild owner.
     *
     * @see    #getOwnerIdLong()
     * @see    #retrieveOwner()
     */
    @Nullable
    Member getOwner();

    /**
     * The ID for the current owner of this guild.
     * <br>This is useful for debugging purposes or as a shortcut.
     *
     * @return The ID for the current owner
     *
     * @see    #getOwner()
     */
    long getOwnerIdLong();

    /**
     * The ID for the current owner of this guild.
     * <br>This is useful for debugging purposes or as a shortcut.
     *
     * @return The ID for the current owner
     *
     * @see    #getOwner()
     */
    @Nonnull
    default String getOwnerId()
    {
        return Long.toUnsignedString(getOwnerIdLong());
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild.Timeout Timeout} set for this Guild representing the amount of time
     * that must pass for a Member to have had no activity in a {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}
     * to be considered AFK. If {@link #getAfkChannel()} is not {@code null} (thus an AFK channel has been set) then Member
     * will be automatically moved to the AFK channel after they have been inactive for longer than the returned Timeout.
     * <br>Default is {@link Timeout#SECONDS_300 300 seconds (5 minutes)}.
     * <p>
     * This value can be modified using {@link GuildManager#setAfkTimeout(net.dv8tion.jda.api.entities.Guild.Timeout)}.
     *
     * @return The {@link net.dv8tion.jda.api.entities.Guild.Timeout Timeout} set for this Guild.
     */
    @Nonnull
    Timeout getAfkTimeout();

    /**
     * Used to determine if the provided {@link net.dv8tion.jda.api.entities.User User} is a member of this Guild.
     *
     * <p>This will only check cached members!
     *
     * @param  user
     *         The user to determine whether or not they are a member of this guild.
     *
     * @return True - if this user is present in this guild.
     */
    boolean isMember(@Nonnull User user);

    /**
     * Gets the {@link net.dv8tion.jda.api.entities.Member Member} object of the currently logged in account in this guild.
     * <br>This is basically {@link net.dv8tion.jda.api.JDA#getSelfUser()} being provided to {@link #getMember(User)}.
     *
     * @return The Member object of the currently logged in account.
     */
    @Nonnull
    Member getSelfMember();

    /**
     * Returns the NSFW Level that this guild is classified with.
     * <br>For a short description of the different values, see {@link net.dv8tion.jda.api.entities.Guild.NSFWLevel NSFWLevel}.
     * <p>
     * This value can only be modified by Discord after reviewing the Guild.
     *
     * @return The NSFWLevel of this guild.
     */
    @Nonnull
    NSFWLevel getNSFWLevel();

    /**
     * Gets the Guild specific {@link net.dv8tion.jda.api.entities.Member Member} object for the provided
     * {@link net.dv8tion.jda.api.entities.User User}.
     * <br>If the user is not in this guild, {@code null} is returned.
     *
     * <p>This will only check cached members!
     *
     * @param  user
     *         The {@link net.dv8tion.jda.api.entities.User User} which to retrieve a related Member object for.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided user is null
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Member Member} for the related {@link net.dv8tion.jda.api.entities.User User}.
     *
     * @see    #retrieveMember(User)
     */
    @Nullable
    Member getMember(@Nonnull User user);

    /**
     * Gets a {@link net.dv8tion.jda.api.entities.Member Member} object via the id of the user. The id relates to
     * {@link net.dv8tion.jda.api.entities.User#getId()}, and this method is similar to {@link JDA#getUserById(String)}
     * <br>This is more efficient that using {@link JDA#getUserById(String)} and {@link #getMember(User)}.
     * <br>If no Member in this Guild has the {@code userId} provided, this returns {@code null}.
     *
     * <p>This will only check cached members!
     *
     * @param  userId
     *         The Discord id of the User for which a Member object is requested.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Member Member} with the related {@code userId}.
     *
     * @see    #retrieveMemberById(String)
     */
    @Nullable
    default Member getMemberById(@Nonnull String userId)
    {
        return getMemberCache().getElementById(userId);
    }

    /**
     * Gets a {@link net.dv8tion.jda.api.entities.Member Member} object via the id of the user. The id relates to
     * {@link net.dv8tion.jda.api.entities.User#getIdLong()}, and this method is similar to {@link JDA#getUserById(long)}
     * <br>This is more efficient that using {@link JDA#getUserById(long)} and {@link #getMember(User)}.
     * <br>If no Member in this Guild has the {@code userId} provided, this returns {@code null}.
     *
     * <p>This will only check cached members!
     * <br>See {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     *
     * @param  userId
     *         The Discord id of the User for which a Member object is requested.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Member Member} with the related {@code userId}.
     *
     * @see    #retrieveMemberById(long)
     */
    @Nullable
    default Member getMemberById(long userId)
    {
        return getMemberCache().getElementById(userId);
    }

    /**
     * Searches for a {@link net.dv8tion.jda.api.entities.Member} that has the matching Discord Tag.
     * <br>Format has to be in the form {@code Username#Discriminator} where the
     * username must be between 2 and 32 characters (inclusive) matching the exact casing and the discriminator
     * must be exactly 4 digits.
     * <br>This does not check the {@link net.dv8tion.jda.api.entities.Member#getNickname() nickname} of the member
     * but the username.
     *
     * <p>This will only check cached members!
     * <br>See {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     *
     * <p>This only checks users that are in this guild. If a user exists
     * with the tag that is not available in the {@link #getMemberCache() Member-Cache} it will not be detected.
     * <br>Currently Discord does not offer a way to retrieve a user by their discord tag.
     *
     * @param  tag
     *         The Discord Tag in the format {@code Username#Discriminator}
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided tag is null or not in the described format
     *
     * @return The {@link net.dv8tion.jda.api.entities.Member} for the discord tag or null if no member has the provided tag
     *
     * @see    net.dv8tion.jda.api.JDA#getUserByTag(String)
     */
    @Nullable
    default Member getMemberByTag(@Nonnull String tag)
    {
        User user = getJDA().getUserByTag(tag);
        return user == null ? null : getMember(user);
    }

    /**
     * Searches for a {@link net.dv8tion.jda.api.entities.Member} that has the matching Discord Tag.
     * <br>Format has to be in the form {@code Username#Discriminator} where the
     * username must be between 2 and 32 characters (inclusive) matching the exact casing and the discriminator
     * must be exactly 4 digits.
     * <br>This does not check the {@link net.dv8tion.jda.api.entities.Member#getNickname() nickname} of the member
     * but the username.
     *
     * <p>This will only check cached members!
     * <br>See {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     *
     * <p>This only checks users that are in this guild. If a user exists
     * with the tag that is not available in the {@link #getMemberCache() Member-Cache} it will not be detected.
     * <br>Currently Discord does not offer a way to retrieve a user by their discord tag.
     *
     * @param  username
     *         The name of the user
     * @param  discriminator
     *         The discriminator of the user
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided arguments are null or not in the described format
     *
     * @return The {@link net.dv8tion.jda.api.entities.Member} for the discord tag or null if no member has the provided tag
     *
     * @see    #getMemberByTag(String)
     */
    @Nullable
    default Member getMemberByTag(@Nonnull String username, @Nonnull String discriminator)
    {
        User user = getJDA().getUserByTag(username, discriminator);
        return user == null ? null : getMember(user);
    }

    /**
     * A list of all {@link net.dv8tion.jda.api.entities.Member Members} in this Guild.
     * <br>The Members are not provided in any particular order.
     *
     * <p>This will only check cached members!
     * <br>See {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getMemberCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return Immutable list of all <b>cached</b> members in this Guild.
     *
     * @see    #loadMembers()
     */
    @Nonnull
    default List<Member> getMembers()
    {
        return getMemberCache().asList();
    }

    /**
     * Gets a list of all {@link net.dv8tion.jda.api.entities.Member Members} who have the same name as the one provided.
     * <br>This compares against {@link net.dv8tion.jda.api.entities.Member#getUser()}{@link net.dv8tion.jda.api.entities.User#getName() .getName()}
     * <br>If there are no {@link net.dv8tion.jda.api.entities.Member Members} with the provided name, then this returns an empty list.
     *
     * <p>This will only check cached members!
     * <br>See {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     *
     * @param  name
     *         The name used to filter the returned Members.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @throws IllegalArgumentException
     *         If the provided name is null
     *
     * @return Possibly-empty immutable list of all Members with the same name as the name provided.
     *
     * @see    #retrieveMembersByPrefix(String, int)
     */
    @Nonnull
    default List<Member> getMembersByName(@Nonnull String name, boolean ignoreCase)
    {
        return getMemberCache().getElementsByUsername(name, ignoreCase);
    }

    /**
     * Gets a list of all {@link net.dv8tion.jda.api.entities.Member Members} who have the same nickname as the one provided.
     * <br>This compares against {@link Member#getNickname()}. If a Member does not have a nickname, the comparison results as false.
     * <br>If there are no {@link net.dv8tion.jda.api.entities.Member Members} with the provided name, then this returns an empty list.
     *
     * <p>This will only check cached members!
     * <br>See {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     *
     * @param  nickname
     *         The nickname used to filter the returned Members.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all Members with the same nickname as the nickname provided.
     *
     * @see    #retrieveMembersByPrefix(String, int)
     */
    @Nonnull
    default List<Member> getMembersByNickname(@Nullable String nickname, boolean ignoreCase)
    {
        return getMemberCache().getElementsByNickname(nickname, ignoreCase);
    }

    /**
     * Gets a list of all {@link net.dv8tion.jda.api.entities.Member Members} who have the same effective name as the one provided.
     * <br>This compares against {@link net.dv8tion.jda.api.entities.Member#getEffectiveName()}.
     * <br>If there are no {@link net.dv8tion.jda.api.entities.Member Members} with the provided name, then this returns an empty list.
     *
     * <p>This will only check cached members!
     * <br>See {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     *
     * @param  name
     *         The name used to filter the returned Members.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @throws IllegalArgumentException
     *         If the provided name is null
     *
     * @return Possibly-empty immutable list of all Members with the same effective name as the name provided.
     *
     * @see    #retrieveMembersByPrefix(String, int)
     */
    @Nonnull
    default List<Member> getMembersByEffectiveName(@Nonnull String name, boolean ignoreCase)
    {
        return getMemberCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a list of {@link net.dv8tion.jda.api.entities.Member Members} that have all {@link net.dv8tion.jda.api.entities.Role Roles} provided.
     * <br>If there are no {@link net.dv8tion.jda.api.entities.Member Members} with all provided roles, then this returns an empty list.
     *
     * <p>This will only check cached members!
     * <br>See {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     *
     * @param  roles
     *         The {@link net.dv8tion.jda.api.entities.Role Roles} that a {@link net.dv8tion.jda.api.entities.Member Member}
     *         must have to be included in the returned list.
     *
     * @throws java.lang.IllegalArgumentException
     *         If a provided {@link net.dv8tion.jda.api.entities.Role Role} is from a different guild or null.
     *
     * @return Possibly-empty immutable list of Members with all provided Roles.
     *
     * @see    #findMembersWithRoles(Role...)
     */
    @Nonnull
    default List<Member> getMembersWithRoles(@Nonnull Role... roles)
    {
        return getMemberCache().getElementsWithRoles(roles);
    }

    /**
     * Gets a list of {@link net.dv8tion.jda.api.entities.Member Members} that have all provided {@link net.dv8tion.jda.api.entities.Role Roles}.
     * <br>If there are no {@link net.dv8tion.jda.api.entities.Member Members} with all provided roles, then this returns an empty list.
     *
     * <p>This will only check cached members!
     * <br>See {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     *
     * @param  roles
     *         The {@link net.dv8tion.jda.api.entities.Role Roles} that a {@link net.dv8tion.jda.api.entities.Member Member}
     *         must have to be included in the returned list.
     *
     * @throws java.lang.IllegalArgumentException
     *         If a provided {@link net.dv8tion.jda.api.entities.Role Role} is from a different guild or null.
     *
     * @return Possibly-empty immutable list of Members with all provided Roles.
     *
     * @see    #findMembersWithRoles(Collection)
     */
    @Nonnull
    default List<Member> getMembersWithRoles(@Nonnull Collection<Role> roles)
    {
        return getMemberCache().getElementsWithRoles(roles);
    }

    /**
     * {@link net.dv8tion.jda.api.utils.cache.MemberCacheView MemberCacheView} for all cached
     * {@link net.dv8tion.jda.api.entities.Member Members} of this Guild.
     *
     * <p>This will only provide cached members!
     * <br>See {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.MemberCacheView MemberCacheView}
     *
     * @see    #loadMembers()
     */
    @Nonnull
    MemberCacheView getMemberCache();


    /**
     * Sorted {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link GuildScheduledEvent GuildScheduledEvents} of this Guild.
     * <br>Scheduled events are sorted by their start time, and events that start at the same time
     * are sorted by their snowflake ID.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @Nonnull
    SortedSnowflakeCacheView<GuildScheduledEvent> getScheduledEventCache();
    /**s
     * Gets a list of all {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvents} in this Guild that have the same
     * name as the one provided.
     * <br>If there are no {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvents} with the provided name,
     * then this returns an empty list.
     *
     * @param  name
     *         The name used to filter the returned {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent} objects.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all GuildScheduledEvent names that match the provided name.
     */
    @Nonnull
    default List<GuildScheduledEvent> getScheduledEventsbyName(@Nonnull String name, boolean ignoreCase)
    {
        return getScheduledEventCache().getElementsByName(name, ignoreCase);
    }
    /**
     * Gets a {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent} from this guild that has the same id as the
     * one provided. This method is similar to {@link net.dv8tion.jda.api.JDA#getGuildScheduledEventById(String)}, but it only
     * checks this specific Guild for a scheduled event.
     * <br>If there is no {@link net.dv8tion.jda.api.entities.StageChannel StageChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.StageChannel StageChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.StageChannel StageChannel} with matching id.
     */
    @Nullable
    default GuildScheduledEvent getScheduledEventById(@Nonnull String id)
    {
        return getScheduledEventCache().getElementById(id);
    }
    /**
     * Gets a {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent} from this guild that has the same id as the
     * one provided. This method is similar to {@link net.dv8tion.jda.api.JDA#getGuildScheduledEventById(long)}, but it only
     * checks this specific Guild for a scheduled event.
     * <br>If there is no {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent}.
     *
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent} with matching id.
     */
    @Nullable
    default GuildScheduledEvent getScheduledEventById(long id)
    {
        return getScheduledEventCache().getElementById(id);
    }
    /**
     * Gets all {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvents} in this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <br>The scheduled events returned will be sorted by their start times, with events starting earlier appearing first in the list. If two events are
     * set to start at the same time, the event with the lesser snowflake ID will appear first.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getScheduledEventCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return An immutable List of {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvents}.
     */
    @Nonnull
    default List<GuildScheduledEvent> getScheduledEvents()
    {
        return getScheduledEventCache().asList();
    }

    @Nonnull
    @Override
    SortedSnowflakeCacheView<StageChannel> getStageChannelCache();

    @Nonnull
    @Override
    SortedSnowflakeCacheView<ThreadChannel> getThreadChannelCache();

    @Nonnull
    @Override
    SortedSnowflakeCacheView<Category> getCategoryCache();

    @Nonnull
    @Override
    SortedSnowflakeCacheView<TextChannel> getTextChannelCache();

    @Nonnull
    @Override
    SortedSnowflakeCacheView<NewsChannel> getNewsChannelCache();

    @Nonnull
    @Override
    SortedSnowflakeCacheView<VoiceChannel> getVoiceChannelCache();

    /**
     * Populated list of {@link GuildChannel channels} for this guild.
     * This includes all types of channels, such as category/voice/text.
     * <br>This includes hidden channels by default.
     *
     * <p>The returned list is ordered in the same fashion as it would be by the official discord client.
     * <ol>
     *     <li>TextChannel and NewsChannel without parent</li>
     *     <li>VoiceChannel without parent</li>
     *     <li>StageChannel without parent</li>
     *     <li>Categories
     *         <ol>
     *             <li>TextChannel and NewsChannel with category as parent</li>
     *             <li>VoiceChannel with category as parent</li>
     *             <li>StageChannel with category as parent</li>
     *         </ol>
     *     </li>
     * </ol>
     *
     * @return Immutable list of channels for this guild
     *
     * @see    #getChannels(boolean)
     */
    @Nonnull
    default List<GuildChannel> getChannels()
    {
        return getChannels(true);
    }

    /**
     * Populated list of {@link GuildChannel channels} for this guild.
     * This includes all types of channels, such as category/voice/text.
     *
     * <p>The returned list is ordered in the same fashion as it would be by the official discord client.
     * <ol>
     *     <li>TextChannel and NewsChannel without parent</li>
     *     <li>VoiceChannel without parent</li>
     *     <li>StageChannel without parent</li>
     *     <li>Categories
     *         <ol>
     *             <li>TextChannel and NewsChannel with category as parent</li>
     *             <li>VoiceChannel with category as parent</li>
     *             <li>StageChannel with category as parent</li>
     *         </ol>
     *     </li>
     * </ol>
     *
     *
     * @param  includeHidden
     *         Whether to include channels with denied {@link Permission#VIEW_CHANNEL View Channel Permission}
     *
     * @return Immutable list of channels for this guild
     *
     * @see    #getChannels()
     */
    @Nonnull
    List<GuildChannel> getChannels(boolean includeHidden);

    /**
     * Gets a {@link net.dv8tion.jda.api.entities.Role Role} from this guild that has the same id as the
     * one provided.
     * <br>If there is no {@link net.dv8tion.jda.api.entities.Role Role} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.Role Role}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Role Role} with matching id.
     */
    @Nullable
    default Role getRoleById(@Nonnull String id)
    {
        return getRoleCache().getElementById(id);
    }

    /**
     * Gets a {@link net.dv8tion.jda.api.entities.Role Role} from this guild that has the same id as the
     * one provided.
     * <br>If there is no {@link net.dv8tion.jda.api.entities.Role Role} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.api.entities.Role Role}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Role Role} with matching id.
     */
    @Nullable
    default Role getRoleById(long id)
    {
        return getRoleCache().getElementById(id);
    }

    /**
     * Gets all {@link net.dv8tion.jda.api.entities.Role Roles} in this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <br>The roles returned will be sorted according to their position. The highest role being at index 0
     * and the lowest at the last index.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getRoleCache()} and use its more efficient
     * versions of handling these values.
     *
     * @return An immutable List of {@link net.dv8tion.jda.api.entities.Role Roles}.
     */
    @Nonnull
    default List<Role> getRoles()
    {
        return getRoleCache().asList();
    }

    /**
     * Gets a list of all {@link net.dv8tion.jda.api.entities.Role Roles} in this Guild that have the same
     * name as the one provided.
     * <br>If there are no {@link net.dv8tion.jda.api.entities.Role Roles} with the provided name, then this returns an empty list.
     *
     * @param  name
     *         The name used to filter the returned {@link net.dv8tion.jda.api.entities.Role Roles}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all Role names that match the provided name.
     */
    @Nonnull
    default List<Role> getRolesByName(@Nonnull String name, boolean ignoreCase)
    {
        return getRoleCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Looks up a role which is the integration role for a bot.
     * <br>These roles are created when the bot requested a list of permission in the authorization URL.
     *
     * <p>To check whether a role is a bot role you can use {@code role.getTags().isBot()} and you can use
     * {@link Role.RoleTags#getBotIdLong()} to check which bot it applies to.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#ROLE_TAGS CacheFlag.ROLE_TAGS} to be enabled.
     * See {@link net.dv8tion.jda.api.JDABuilder#enableCache(CacheFlag, CacheFlag...) JDABuilder.enableCache(...)}.
     *
     * @param  userId
     *         The user id of the bot
     *
     * @return The bot role, or null if no role matches
     */
    @Nullable
    default Role getRoleByBot(long userId)
    {
        return getRoleCache().applyStream(stream ->
            stream.filter(role -> role.getTags().getBotIdLong() == userId)
                  .findFirst()
                  .orElse(null)
        );
    }

    /**
     * Looks up a role which is the integration role for a bot.
     * <br>These roles are created when the bot requested a list of permission in the authorization URL.
     *
     * <p>To check whether a role is a bot role you can use {@code role.getTags().isBot()} and you can use
     * {@link Role.RoleTags#getBotIdLong()} to check which bot it applies to.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#ROLE_TAGS CacheFlag.ROLE_TAGS} to be enabled.
     * See {@link net.dv8tion.jda.api.JDABuilder#enableCache(CacheFlag, CacheFlag...) JDABuilder.enableCache(...)}.
     *
     * @param  userId
     *         The user id of the bot
     *
     * @throws IllegalArgumentException
     *         If the userId is null or not a valid snowflake
     *
     * @return The bot role, or null if no role matches
     */
    @Nullable
    default Role getRoleByBot(@Nonnull String userId)
    {
        return getRoleByBot(MiscUtil.parseSnowflake(userId));
    }

    /**
     * Looks up a role which is the integration role for a bot.
     * <br>These roles are created when the bot requested a list of permission in the authorization URL.
     *
     * <p>To check whether a role is a bot role you can use {@code role.getTags().isBot()} and you can use
     * {@link Role.RoleTags#getBotIdLong()} to check which bot it applies to.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#ROLE_TAGS CacheFlag.ROLE_TAGS} to be enabled.
     * See {@link net.dv8tion.jda.api.JDABuilder#enableCache(CacheFlag, CacheFlag...) JDABuilder.enableCache(...)}.
     *
     * @param  user
     *         The bot user
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The bot role, or null if no role matches
     */
    @Nullable
    default Role getRoleByBot(@Nonnull User user)
    {
        Checks.notNull(user, "User");
        return getRoleByBot(user.getIdLong());
    }

    /**
     * Looks up the role which is the integration role for the currently connected bot (self-user).
     * <br>These roles are created when the bot requested a list of permission in the authorization URL.
     *
     * <p>To check whether a role is a bot role you can use {@code role.getTags().isBot()} and you can use
     * {@link Role.RoleTags#getBotIdLong()} to check which bot it applies to.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#ROLE_TAGS CacheFlag.ROLE_TAGS} to be enabled.
     * See {@link net.dv8tion.jda.api.JDABuilder#enableCache(CacheFlag, CacheFlag...) JDABuilder.enableCache(...)}.
     *
     * @return The bot role, or null if no role matches
     */
    @Nullable
    default Role getBotRole()
    {
        return getRoleByBot(getJDA().getSelfUser());
    }

    /**
     * Looks up the role which is the booster role of this guild.
     * <br>These roles are created when the first user boosts this guild.
     *
     * <p>To check whether a role is a booster role you can use {@code role.getTags().isBoost()}.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#ROLE_TAGS CacheFlag.ROLE_TAGS} to be enabled.
     * See {@link net.dv8tion.jda.api.JDABuilder#enableCache(CacheFlag, CacheFlag...) JDABuilder.enableCache(...)}.
     *
     * @return The boost role, or null if no role matches
     */
    @Nullable
    default Role getBoostRole()
    {
        return getRoleCache().applyStream(stream ->
            stream.filter(role -> role.getTags().isBoost())
                  .findFirst()
                  .orElse(null)
        );
    }

    /**
     * Sorted {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.api.entities.Role Roles} of this Guild.
     * <br>Roles are sorted according to their position.
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @Nonnull
    SortedSnowflakeCacheView<Role> getRoleCache();

    /**
     * Gets an {@link net.dv8tion.jda.api.entities.Emote Emote} from this guild that has the same id as the
     * one provided.
     * <br>If there is no {@link net.dv8tion.jda.api.entities.Emote Emote} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * <p><b>Unicode emojis are not included as {@link net.dv8tion.jda.api.entities.Emote Emote}!</b>
     *
     * <p>This requires the {@link net.dv8tion.jda.api.utils.cache.CacheFlag#EMOTE CacheFlag.EMOTE} to be enabled!
     *
     * @param  id
     *         the emote id
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return An Emote matching the specified Id.
     *
     * @see    #retrieveEmoteById(String)
     */
    @Nullable
    default Emote getEmoteById(@Nonnull String id)
    {
        return getEmoteCache().getElementById(id);
    }

    /**
     * Gets an {@link net.dv8tion.jda.api.entities.Emote Emote} from this guild that has the same id as the
     * one provided.
     * <br>If there is no {@link net.dv8tion.jda.api.entities.Emote Emote} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * <p><b>Unicode emojis are not included as {@link net.dv8tion.jda.api.entities.Emote Emote}!</b>
     *
     * <p>This requires the {@link net.dv8tion.jda.api.utils.cache.CacheFlag#EMOTE CacheFlag.EMOTE} to be enabled!
     *
     * @param  id
     *         the emote id
     *
     * @return An Emote matching the specified Id.
     *
     * @see    #retrieveEmoteById(long)
     */
    @Nullable
    default Emote getEmoteById(long id)
    {
        return getEmoteCache().getElementById(id);
    }

    /**
     * Gets all custom {@link net.dv8tion.jda.api.entities.Emote Emotes} belonging to this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <br>Emotes are not ordered in any specific way in the returned list.
     *
     * <p><b>Unicode emojis are not included as {@link net.dv8tion.jda.api.entities.Emote Emote}!</b>
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getEmoteCache()} and use its more efficient
     * versions of handling these values.
     *
     * <p>This requires the {@link net.dv8tion.jda.api.utils.cache.CacheFlag#EMOTE CacheFlag.EMOTE} to be enabled!
     *
     * @return An immutable List of {@link net.dv8tion.jda.api.entities.Emote Emotes}.
     *
     * @see    #retrieveEmotes()
     */
    @Nonnull
    default List<Emote> getEmotes()
    {
        return getEmoteCache().asList();
    }

    /**
     * Gets a list of all {@link net.dv8tion.jda.api.entities.Emote Emotes} in this Guild that have the same
     * name as the one provided.
     * <br>If there are no {@link net.dv8tion.jda.api.entities.Emote Emotes} with the provided name, then this returns an empty list.
     *
     * <p><b>Unicode emojis are not included as {@link net.dv8tion.jda.api.entities.Emote Emote}!</b>
     *
     * <p>This requires the {@link net.dv8tion.jda.api.utils.cache.CacheFlag#EMOTE CacheFlag.EMOTE} to be enabled!
     *
     * @param  name
     *         The name used to filter the returned {@link net.dv8tion.jda.api.entities.Emote Emotes}. Without colons.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @return Possibly-empty immutable list of all Emotes that match the provided name.
     */
    @Nonnull
    default List<Emote> getEmotesByName(@Nonnull String name, boolean ignoreCase)
    {
        return getEmoteCache().getElementsByName(name, ignoreCase);
    }

    /**
     * {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link net.dv8tion.jda.api.entities.Emote Emotes} of this Guild.
     * <br>This will be empty if {@link net.dv8tion.jda.api.utils.cache.CacheFlag#EMOTE} is disabled.
     *
     * <p>This requires the {@link net.dv8tion.jda.api.utils.cache.CacheFlag#EMOTE CacheFlag.EMOTE} to be enabled!
     *
     * @return {@link net.dv8tion.jda.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     *
     * @see    #retrieveEmotes()
     */
    @Nonnull
    SnowflakeCacheView<Emote> getEmoteCache();

    /**
     * Retrieves an immutable list of emotes together with their respective creators.
     *
     * <p>Note that {@link ListedEmote#getUser()} is only available if the currently
     * logged in account has {@link net.dv8tion.jda.api.Permission#MANAGE_EMOTES_AND_STICKERS Permission.MANAGE_EMOTES_AND_STICKERS}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: List of {@link net.dv8tion.jda.api.entities.ListedEmote ListedEmote}
     *
     * @since  3.8.0
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<ListedEmote>> retrieveEmotes();

    /**
     * Retrieves a listed emote together with its respective creator.
     * <br><b>This does not include unicode emoji.</b>
     *
     * <p>Note that {@link ListedEmote#getUser()} is only available if the currently
     * logged in account has {@link net.dv8tion.jda.api.Permission#MANAGE_EMOTES_AND_STICKERS Permission.MANAGE_EMOTES_AND_STICKERS}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>If the provided id does not correspond to an emote in this guild</li>
     * </ul>
     *
     * @param  id
     *         The emote id
     *
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.ListedEmote ListedEmote}
     *
     * @since  3.8.0
     */
    @Nonnull
    @CheckReturnValue
    RestAction<ListedEmote> retrieveEmoteById(@Nonnull String id);

    /**
     * Retrieves a listed emote together with its respective creator.
     *
     * <p>Note that {@link ListedEmote#getUser()} is only available if the currently
     * logged in account has {@link net.dv8tion.jda.api.Permission#MANAGE_EMOTES_AND_STICKERS Permission.MANAGE_EMOTES_AND_STICKERS}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>If the provided id does not correspond to an emote in this guild</li>
     * </ul>
     *
     * @param  id
     *         The emote id
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.ListedEmote ListedEmote}
     *
     * @since  3.8.0
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<ListedEmote> retrieveEmoteById(long id)
    {
        return retrieveEmoteById(Long.toUnsignedString(id));
    }

    /**
     * Retrieves a listed emote together with its respective creator.
     *
     * <p>Note that {@link ListedEmote#getUser()} is only available if the currently
     * logged in account has {@link net.dv8tion.jda.api.Permission#MANAGE_EMOTES_AND_STICKERS Permission.MANAGE_EMOTES_AND_STICKERS}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>If the provided emote does not correspond to an emote in this guild anymore</li>
     * </ul>
     *
     * @param  emote
     *         The emote
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.ListedEmote ListedEmote}
     *
     * @since  3.8.0
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<ListedEmote> retrieveEmote(@Nonnull Emote emote)
    {
        Checks.notNull(emote, "Emote");
        if (emote.getGuild() != null)
            Checks.check(emote.getGuild().equals(this), "Emote must be from the same Guild!");

        JDA jda = getJDA();
        return new DeferredRestAction<>(jda, ListedEmote.class,
        () -> {
            if (emote instanceof ListedEmote)
            {
                ListedEmote listedEmote = (ListedEmote) emote;
                if (listedEmote.hasUser() || !getSelfMember().hasPermission(Permission.MANAGE_EMOTES_AND_STICKERS))
                    return listedEmote;
            }
            return null;
        }, () -> retrieveEmoteById(emote.getId()));
    }

    /**
     * Retrieves an immutable list of the currently banned {@link net.dv8tion.jda.api.entities.User Users}.
     * <br>If you wish to ban or unban a user, use either {@link #ban(User, int) ban(User, int)} or
     * {@link #unban(User) unban(User)}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The ban list cannot be fetched due to a permission discrepancy</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#BAN_MEMBERS} permission.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@literal List<}{@link net.dv8tion.jda.api.entities.Guild.Ban Ban}{@literal >}
     *         <br>Retrieves an immutable list of all users currently banned from this Guild
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<Ban>> retrieveBanList();

    /**
     * Retrieves a {@link net.dv8tion.jda.api.entities.Guild.Ban Ban} of the provided ID
     * <br>If you wish to ban or unban a user, use either {@link #ban(String, int) ban(id, int)} or
     * {@link #unban(String) unban(id)}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The ban list cannot be fetched due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_BAN UNKNOWN_BAN}
     *     <br>Either the ban was removed before finishing the task or it did not exist in the first place</li>
     * </ul>
     *
     * @param  userId
     *         the id of the banned user
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#BAN_MEMBERS} permission.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.Guild.Ban Ban}
     *         <br>An unmodifiable ban object for the user banned from this guild
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Ban> retrieveBanById(long userId)
    {
        return retrieveBanById(Long.toUnsignedString(userId));
    }

    /**
     * Retrieves a {@link net.dv8tion.jda.api.entities.Guild.Ban Ban} of the provided ID
     * <br>If you wish to ban or unban a user, use either {@link #ban(String, int) ban(id, int)} or
     * {@link #unban(String) unban(id)}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The ban list cannot be fetched due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_BAN UNKNOWN_BAN}
     *     <br>Either the ban was removed before finishing the task or it did not exist in the first place</li>
     * </ul>
     *
     * @param  userId
     *         the id of the banned user
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#BAN_MEMBERS} permission.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.Guild.Ban Ban}
     *         <br>An unmodifiable ban object for the user banned from this guild
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Ban> retrieveBanById(@Nonnull String userId);

    /**
     * Retrieves a {@link net.dv8tion.jda.api.entities.Guild.Ban Ban} of the provided {@link net.dv8tion.jda.api.entities.User User}
     * <br>If you wish to ban or unban a user, use either {@link #ban(User, int) ban(User, int)} or
     * {@link #unban(User) unban(User)}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The ban list cannot be fetched due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_BAN UNKNOWN_BAN}
     *     <br>Either the ban was removed before finishing the task or it did not exist in the first place</li>
     * </ul>
     *
     * @param  bannedUser
     *         the banned user
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#BAN_MEMBERS} permission.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.Guild.Ban Ban}
     *         <br>An unmodifiable ban object for the user banned from this guild
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Ban> retrieveBan(@Nonnull User bannedUser)
    {
        Checks.notNull(bannedUser, "bannedUser");
        return retrieveBanById(bannedUser.getId());
    }

    /**
     * The method calculates the amount of Members that would be pruned if {@link #prune(int, Role...)} was executed.
     * Prunability is determined by a Member being offline for at least <i>days</i> days.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The prune count cannot be fetched due to a permission discrepancy</li>
     * </ul>
     *
     * @param  days
     *         Minimum number of days since a member has been offline to get affected.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the account doesn't have {@link net.dv8tion.jda.api.Permission#KICK_MEMBERS KICK_MEMBER} Permission.
     * @throws IllegalArgumentException
     *         If the provided days are less than {@code 1} or more than {@code 30}
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: Integer
     *         <br>The amount of Members that would be affected.
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Integer> retrievePrunableMemberCount(int days);

    /**
     * The @everyone {@link net.dv8tion.jda.api.entities.Role Role} of this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <br>This role is special because its {@link net.dv8tion.jda.api.entities.Role#getPosition() position} is calculated as
     * {@code -1}. All other role positions are 0 or greater. This implies that the public role is <b>always</b> below
     * any custom roles created in this Guild. Additionally, all members of this guild are implied to have this role so
     * it is not included in the list returned by {@link net.dv8tion.jda.api.entities.Member#getRoles() Member.getRoles()}.
     * <br>The ID of this Role is the Guild's ID thus it is equivalent to using {@link #getRoleById(long) getRoleById(getIdLong())}.
     *
     * @return The @everyone {@link net.dv8tion.jda.api.entities.Role Role}
     */
    @Nonnull
    Role getPublicRole();

    /**
     * The default {@link net.dv8tion.jda.api.entities.BaseGuildMessageChannel BaseGuildMessageChannel} for a {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <br>This is the channel that the Discord client will default to opening when a Guild is opened for the first time when accepting an invite
     * that is not directed at a specific {@link net.dv8tion.jda.api.entities.BaseGuildMessageChannel BaseGuildMessageChannel}.
     *
     * <p>Note: This channel is the first channel in the guild (ordered by position) that the {@link #getPublicRole()}
     * has the {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} in.
     *
     * @return The {@link net.dv8tion.jda.api.entities.BaseGuildMessageChannel BaseGuildMessageChannel} representing the default channel for this guild
     */
    @Nullable
    BaseGuildMessageChannel getDefaultChannel();

    /**
     * Returns the {@link GuildManager GuildManager} for this Guild, used to modify
     * all properties and settings of the Guild.
     * <br>You modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction.queue()}.
     *
     * <p>This is a lazy idempotent getter. The manager is retained after the first call.
     * This getter is not thread-safe and would require guards by the user.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER Permission.MANAGE_SERVER}
     *
     * @return The Manager of this Guild
     */
    @Nonnull
    GuildManager getManager();

    /**
     * Returns whether this Guild has its boost progress bar shown.
     *
     * @return True, if this Guild has its boost progress bar shown
     */
    boolean isBoostProgressBarEnabled();

    /**
     * A {@link PaginationAction PaginationAction} implementation
     * that allows to {@link Iterable iterate} over all {@link net.dv8tion.jda.api.audit.AuditLogEntry AuditLogEntries} of
     * this Guild.
     * <br>This iterates from the most recent action to the first logged one. (Limit 90 days into history by discord api)
     *
     * <h4>Examples</h4>
     * <pre>{@code
     * public void logBan(GuildBanEvent event) {
     *     Guild guild = event.getGuild();
     *     List<TextChannel> modLog = guild.getTextChannelsByName("mod-log", true);
     *     guild.retrieveAuditLogs()
     *          .type(ActionType.BAN) // filter by type
     *          .limit(1)
     *          .queue(list -> {
     *             if (list.isEmpty()) return;
     *             AuditLogEntry entry = list.get(0);
     *             String message = String.format("%#s banned %#s with reason %s",
     *                                            entry.getUser(), event.getUser(), entry.getReason());
     *             modLog.forEach(channel ->
     *               channel.sendMessage(message).queue()
     *             );
     *          });
     * }
     * }</pre>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account
     *         does not have the permission {@link net.dv8tion.jda.api.Permission#VIEW_AUDIT_LOGS VIEW_AUDIT_LOGS}
     *
     * @return {@link AuditLogPaginationAction AuditLogPaginationAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditLogPaginationAction retrieveAuditLogs();

    /**
     * Used to leave a Guild. If the currently logged in account is the owner of this guild ({@link net.dv8tion.jda.api.entities.Guild#getOwner()})
     * then ownership of the Guild needs to be transferred to a different {@link net.dv8tion.jda.api.entities.Member Member}
     * before leaving using {@link #transferOwnership(Member)}.
     *
     * @throws java.lang.IllegalStateException
     *         Thrown if the currently logged in account is the Owner of this Guild.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link java.lang.Void}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> leave();

    /**
     * Used to completely delete a Guild. This can only be done if the currently logged in account is the owner of the Guild.
     * <br>If the account has MFA enabled, use {@link #delete(String)} instead to provide the MFA code.
     *
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     *         Thrown if the currently logged in account is not the owner of this Guild.
     * @throws java.lang.IllegalStateException
     *         If the currently logged in account has MFA enabled. ({@link net.dv8tion.jda.api.entities.SelfUser#isMfaEnabled()}).
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction} - Type: {@link java.lang.Void}
     */
    @Nonnull
    @CheckReturnValue
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
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     *         Thrown if the currently logged in account is not the owner of this Guild.
     * @throws java.lang.IllegalArgumentException
     *         If the provided {@code mfaCode} is {@code null} or empty when {@link SelfUser#isMfaEnabled()} is true.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction} - Type: {@link java.lang.Void}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> delete(@Nullable String mfaCode);

    /**
     * The {@link net.dv8tion.jda.api.managers.AudioManager AudioManager} that represents the
     * audio connection for this Guild.
     * <br>If no AudioManager exists for this Guild, this will create a new one.
     * <br>This operation is synchronized on all audio managers for this JDA instance,
     * this means that calling getAudioManager() on any other guild while a thread is accessing this method may be locked.
     *
     * @throws IllegalStateException
     *         If {@link GatewayIntent#GUILD_VOICE_STATES} is disabled
     *
     * @return The AudioManager for this Guild.
     *
     * @see    net.dv8tion.jda.api.JDA#getAudioManagerCache() JDA.getAudioManagerCache()
     */
    @Nonnull
    AudioManager getAudioManager();

    /**
     * Once the currently logged in account is connected to a {@link StageChannel},
     * this will trigger a {@link GuildVoiceState#getRequestToSpeakTimestamp() Request-to-Speak} (aka raise your hand).
     *
     * <p>This will set an internal flag to automatically request to speak once the bot joins a stage channel.
     * <br>You can use {@link #cancelRequestToSpeak()} to move back to the audience or cancel your pending request.
     *
     * <p>If the self member has {@link Permission#VOICE_MUTE_OTHERS} this will immediately promote them to speaker.
     *
     * <p>Example:
     * <pre>{@code
     * stageChannel.createStageInstance("Talent Show").queue()
     * guild.requestToSpeak(); // Set request to speak flag
     * guild.getAudioManager().openAudioConnection(stageChannel); // join the channel
     * }</pre>
     *
     * @return {@link Task} representing the request to speak.
     *         Calling {@link Task#get()} can result in deadlocks and should be avoided at all times.
     *
     * @see    #cancelRequestToSpeak()
     */
    @Nonnull
    Task<Void> requestToSpeak();

    /**
     * Cancels the {@link #requestToSpeak() Request-to-Speak}.
     * <br>This can also be used to move back to the audience if you are currently a speaker.
     *
     * <p>If there is no request to speak or the member is not currently connected to a {@link StageChannel}, this does nothing.
     *
     * @return {@link Task} representing the request to speak cancellation.
     *         Calling {@link Task#get()} can result in deadlocks and should be avoided at all times.
     *
     * @see    #requestToSpeak()
     */
    @Nonnull
    Task<Void> cancelRequestToSpeak();

    /**
     * Returns the {@link net.dv8tion.jda.api.JDA JDA} instance of this Guild
     *
     * @return the corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * Retrieves all {@link net.dv8tion.jda.api.entities.Invite Invites} for this guild.
     * <br>Requires {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in this guild.
     * Will throw an {@link net.dv8tion.jda.api.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * <p>To get all invites for a {@link GuildChannel GuildChannel}
     * use {@link IInviteContainer#retrieveInvites() GuildChannel.retrieveInvites()}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if the account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in this Guild.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: List{@literal <}{@link net.dv8tion.jda.api.entities.Invite Invite}{@literal >}
     *         <br>The list of expanded Invite objects
     *
     * @see     IInviteContainer#retrieveInvites()
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<Invite>> retrieveInvites();

    /**
     * Retrieves all {@link net.dv8tion.jda.api.entities.templates.Template Templates} for this guild.
     * <br>Requires {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in this guild.
     * Will throw an {@link net.dv8tion.jda.api.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if the account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in this Guild.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: List{@literal <}{@link net.dv8tion.jda.api.entities.templates.Template Template}{@literal >}
     *         <br>The list of Template objects
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<Template>> retrieveTemplates();

    /**
     * Used to create a new {@link net.dv8tion.jda.api.entities.templates.Template Template} for this Guild.
     * <br>Requires {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in this Guild.
     * Will throw an {@link net.dv8tion.jda.api.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#ALREADY_HAS_TEMPLATE Guild already has a template}
     *     <br>The guild already has a template.</li>
     * </ul>
     *
     * @param  name
     *         The name of the template
     * @param  description
     *         The description of the template
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if the account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in this Guild
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 1-100 characters long, or
     *         if the provided description is not between 0-120 characters long
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.templates.Template Template}
     *         <br>The created Template object
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Template> createTemplate(@Nonnull String name, @Nullable String description);

    /**
     * Retrieves all {@link net.dv8tion.jda.api.entities.Webhook Webhooks} for this Guild.
     * <br>Requires {@link net.dv8tion.jda.api.Permission#MANAGE_WEBHOOKS MANAGE_WEBHOOKS} in this Guild.
     *
     * <p>To get all webhooks for a specific {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}, use
     * {@link TextChannel#retrieveWebhooks()}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if the account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_WEBHOOKS MANAGE_WEBHOOKS} in this Guild.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: List{@literal <}{@link net.dv8tion.jda.api.entities.Webhook Webhook}{@literal >}
     *         <br>A list of all Webhooks in this Guild.
     *
     * @see     TextChannel#retrieveWebhooks()
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<Webhook>> retrieveWebhooks();

    /**
     * A list containing the {@link net.dv8tion.jda.api.entities.GuildVoiceState GuildVoiceState} of every {@link net.dv8tion.jda.api.entities.Member Member}
     * in this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <br>This will never return an empty list because if it were empty, that would imply that there are no
     * {@link net.dv8tion.jda.api.entities.Member Members} in this {@link net.dv8tion.jda.api.entities.Guild Guild}, which is
     * impossible.
     *
     * @return Never-empty immutable list containing all the {@link GuildVoiceState GuildVoiceStates} on this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     */
    @Nonnull
    List<GuildVoiceState> getVoiceStates();

    /**
     * Returns the verification-Level of this Guild. Verification level is one of the factors that determines if a Member
     * can send messages in a Guild.
     * <br>For a short description of the different values, see {@link net.dv8tion.jda.api.entities.Guild.VerificationLevel}.
     * <p>
     * This value can be modified using {@link GuildManager#setVerificationLevel(net.dv8tion.jda.api.entities.Guild.VerificationLevel)}.
     *
     * @return The Verification-Level of this Guild.
     */
    @Nonnull
    VerificationLevel getVerificationLevel();

    /**
     * Returns the default message Notification-Level of this Guild. Notification level determines when Members get notification
     * for messages. The value returned is the default level set for any new Members that join the Guild.
     * <br>For a short description of the different values, see {@link net.dv8tion.jda.api.entities.Guild.NotificationLevel NotificationLevel}.
     * <p>
     * This value can be modified using {@link GuildManager#setDefaultNotificationLevel(net.dv8tion.jda.api.entities.Guild.NotificationLevel)}.
     *
     * @return The default message Notification-Level of this Guild.
     */
    @Nonnull
    NotificationLevel getDefaultNotificationLevel();

    /**
     * Returns the level of multifactor authentication required to execute administrator restricted functions in this guild.
     * <br>For a short description of the different values, see {@link net.dv8tion.jda.api.entities.Guild.MFALevel MFALevel}.
     * <p>
     * This value can be modified using {@link GuildManager#setRequiredMFALevel(net.dv8tion.jda.api.entities.Guild.MFALevel)}.
     *
     * @return The MFA-Level required by this Guild.
     */
    @Nonnull
    MFALevel getRequiredMFALevel();

    /**
     * The level of content filtering enabled in this Guild.
     * <br>This decides which messages sent by which Members will be scanned for explicit content.
     *
     * @return {@link net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel ExplicitContentLevel} for this Guild
     */
    @Nonnull
    ExplicitContentLevel getExplicitContentLevel();

    /**
     * Retrieves and collects members of this guild into a list.
     * <br>This will use the configured {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     * to decide which members to retain in cache.
     *
     * <p>You can use {@link #findMembers(Predicate)} to filter specific members.
     *
     * <p><b>This requires the privileged GatewayIntent.GUILD_MEMBERS to be enabled!</b>
     *
     * <p><b>You MUST NOT use blocking operations such as {@link Task#get()}!</b>
     * The response handling happens on the event thread by default.
     *
     * @throws IllegalStateException
     *         If the {@link GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} is not enabled
     *
     * @return {@link Task} - Type: {@link List} of {@link Member}
     */
    @Nonnull
    @CheckReturnValue
    default Task<List<Member>> loadMembers()
    {
        return findMembers((m) -> true);
    }

    /**
     * Retrieves and collects members of this guild into a list.
     * <br>This will use the configured {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     * to decide which members to retain in cache.
     *
     * <p><b>This requires the privileged GatewayIntent.GUILD_MEMBERS to be enabled!</b>
     *
     * <p><b>You MUST NOT use blocking operations such as {@link Task#get()}!</b>
     * The response handling happens on the event thread by default.
     *
     * @param  filter
     *         Filter to decide which members to include
     *
     * @throws IllegalArgumentException
     *         If the provided filter is null
     * @throws IllegalStateException
     *         If the {@link GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} is not enabled
     *
     * @return {@link Task} - Type: {@link List} of {@link Member}
     */
    @Nonnull
    @CheckReturnValue
    default Task<List<Member>> findMembers(@Nonnull Predicate<? super Member> filter)
    {
        Checks.notNull(filter, "Filter");
        List<Member> list = new ArrayList<>();
        CompletableFuture<List<Member>> future = new CompletableFuture<>();
        Task<Void> reference = loadMembers((member) -> {
            if (filter.test(member))
                list.add(member);
        });
        GatewayTask<List<Member>> task = new GatewayTask<>(future, reference::cancel);
        reference.onSuccess(it -> future.complete(list))
                 .onError(future::completeExceptionally);
        return task;
    }

    /**
     * Retrieves and collects members of this guild into a list.
     * <br>This will use the configured {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     * to decide which members to retain in cache.
     *
     * <p><b>This requires the privileged GatewayIntent.GUILD_MEMBERS to be enabled!</b>
     *
     * <p><b>You MUST NOT use blocking operations such as {@link Task#get()}!</b>
     * The response handling happens on the event thread by default.
     *
     * @param  roles
     *         Collection of all roles the members must have
     *
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws IllegalStateException
     *         If the {@link GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} is not enabled
     *
     * @return {@link Task} - Type: {@link List} of {@link Member}
     *
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    default Task<List<Member>> findMembersWithRoles(@Nonnull Collection<Role> roles)
    {
        Checks.noneNull(roles, "Roles");
        for (Role role : roles)
            Checks.check(this.equals(role.getGuild()), "All roles must be from the same guild!");

        if (isLoaded() || roles.isEmpty() || roles.contains(getPublicRole())) // Member#getRoles never contains the public role
        {
            CompletableFuture<List<Member>> future = CompletableFuture.completedFuture(getMembersWithRoles(roles));
            return new GatewayTask<>(future, () -> {});
        }

        return findMembers(member -> member.getRoles().containsAll(roles));
    }

    /**
     * Retrieves and collects members of this guild into a list.
     * <br>This will use the configured {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     * to decide which members to retain in cache.
     *
     * <p><b>This requires the privileged GatewayIntent.GUILD_MEMBERS to be enabled!</b>
     *
     * <p><b>You MUST NOT use blocking operations such as {@link Task#get()}!</b>
     * The response handling happens on the event thread by default.
     *
     * @param  roles
     *         All roles the members must have
     *
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws IllegalStateException
     *         If the {@link GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} is not enabled
     *
     * @return {@link Task} - Type: {@link List} of {@link Member}
     *
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    default Task<List<Member>> findMembersWithRoles(@Nonnull Role... roles)
    {
        Checks.noneNull(roles, "Roles");
        return findMembersWithRoles(Arrays.asList(roles));
    }

    /**
     * Retrieves all members of this guild.
     * <br>This will use the configured {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
     * to decide which members to retain in cache.
     *
     * <p><b>This requires the privileged GatewayIntent.GUILD_MEMBERS to be enabled!</b>
     *
     * <p><b>You MUST NOT use blocking operations such as {@link Task#get()}!</b>
     * The response handling happens on the event thread by default.
     *
     * @param  callback
     *         Consumer callback for each member
     *
     * @throws IllegalArgumentException
     *         If the callback is null
     * @throws IllegalStateException
     *         If the {@link GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} is not enabled
     *
     * @return {@link Task} cancellable handle for this request
     */
    @Nonnull
    Task<Void> loadMembers(@Nonnull Consumer<Member> callback);

    /**
     * Load the member for the specified user.
     * <br>If the member is already loaded it will be retrieved from {@link #getMemberById(long)}
     * and immediately provided if the member information is consistent. The cache consistency directly
     * relies on the enabled {@link GatewayIntent GatewayIntents} as {@link GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS}
     * is required to keep the cache updated with the latest information. You can pass {@code update = false} to always
     * return immediately if the member is cached regardless of cache consistency.
     *
     * <p>When the intent {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS}
     * is disabled this will always make a request even if the member is cached. You can use {@link #retrieveMember(User, boolean)} to disable this behavior.
     *
     * <p>Possible {@link net.dv8tion.jda.api.exceptions.ErrorResponseException ErrorResponseExceptions} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER}
     *     <br>The specified user is not a member of this guild</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER}
     *     <br>The specified user does not exist</li>
     * </ul>
     *
     * @param  user
     *         The user to load the member from
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link RestAction} - Type: {@link Member}
     *
     * @see    #pruneMemberCache()
     * @see    #unloadMember(long)
     */
    @Nonnull
    default RestAction<Member> retrieveMember(@Nonnull User user)
    {
        Checks.notNull(user, "User");
        return retrieveMemberById(user.getId());
    }

    /**
     * Load the member for the specified user.
     * <br>If the member is already loaded it will be retrieved from {@link #getMemberById(long)}
     * and immediately provided if the member information is consistent. The cache consistency directly
     * relies on the enabled {@link GatewayIntent GatewayIntents} as {@link GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS}
     * is required to keep the cache updated with the latest information. You can pass {@code update = false} to always
     * return immediately if the member is cached regardless of cache consistency.
     *
     * <p>When the intent {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS}
     * is disabled this will always make a request even if the member is cached. You can use {@link #retrieveMemberById(String, boolean)} to disable this behavior.
     *
     * <p>Possible {@link net.dv8tion.jda.api.exceptions.ErrorResponseException ErrorResponseExceptions} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER}
     *     <br>The specified user is not a member of this guild</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER}
     *     <br>The specified user does not exist</li>
     * </ul>
     *
     * @param  id
     *         The user id to load the member from
     *
     * @throws IllegalArgumentException
     *         If the provided id is empty or null
     * @throws NumberFormatException
     *         If the provided id is not a snowflake
     *
     * @return {@link RestAction} - Type: {@link Member}
     *
     * @see    #pruneMemberCache()
     * @see    #unloadMember(long)
     */
    @Nonnull
    default RestAction<Member> retrieveMemberById(@Nonnull String id)
    {
        return retrieveMemberById(MiscUtil.parseSnowflake(id));
    }

    /**
     * Load the member for the specified user.
     * <br>If the member is already loaded it will be retrieved from {@link #getMemberById(long)}
     * and immediately provided if the member information is consistent. The cache consistency directly
     * relies on the enabled {@link GatewayIntent GatewayIntents} as {@link GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS}
     * is required to keep the cache updated with the latest information. You can pass {@code update = false} to always
     * return immediately if the member is cached regardless of cache consistency.
     *
     * <p>When {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS}
     * is disabled this will always make a request even if the member is cached. You can use {@link #retrieveMemberById(long, boolean)} to disable this behavior.
     *
     * <p>Possible {@link net.dv8tion.jda.api.exceptions.ErrorResponseException ErrorResponseExceptions} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER}
     *     <br>The specified user is not a member of this guild</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER}
     *     <br>The specified user does not exist</li>
     * </ul>
     *
     * @param  id
     *         The user id to load the member from
     *
     * @return {@link RestAction} - Type: {@link Member}
     *
     * @see    #pruneMemberCache()
     * @see    #unloadMember(long)
     */
    @Nonnull
    default RestAction<Member> retrieveMemberById(long id)
    {
        return retrieveMemberById(id, true);
    }

    /**
     * Shortcut for {@code guild.retrieveMemberById(guild.getOwnerIdLong())}.
     * <br>This will retrieve the current owner of the guild.
     * It is possible that the owner of a guild is no longer a registered discord user in which case this will fail.
     *
     * <p>When {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS}
     * is disabled this will always make a request even if the member is cached. You can use {@link #retrieveOwner(boolean)} to disable this behavior.
     *
     * <p>Possible {@link net.dv8tion.jda.api.exceptions.ErrorResponseException ErrorResponseExceptions} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER}
     *     <br>The specified user is not a member of this guild</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER}
     *     <br>The specified user does not exist</li>
     * </ul>
     *
     * @return {@link RestAction} - Type: {@link Member}
     *
     * @see    #pruneMemberCache()
     * @see    #unloadMember(long)
     *
     * @see    #getOwner()
     * @see    #getOwnerIdLong()
     * @see    #retrieveMemberById(long)
     */
    @Nonnull
    default RestAction<Member> retrieveOwner()
    {
        return retrieveMemberById(getOwnerIdLong());
    }

    /**
     * Load the member for the specified user.
     * <br>If the member is already loaded it will be retrieved from {@link #getMemberById(long)}
     * and immediately provided if the member information is consistent. The cache consistency directly
     * relies on the enabled {@link GatewayIntent GatewayIntents} as {@link GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS}
     * is required to keep the cache updated with the latest information. You can pass {@code update = false} to always
     * return immediately if the member is cached regardless of cache consistency.
     *
     * <p>Possible {@link net.dv8tion.jda.api.exceptions.ErrorResponseException ErrorResponseExceptions} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER}
     *     <br>The specified user is not a member of this guild</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER}
     *     <br>The specified user does not exist</li>
     * </ul>
     *
     * @param  user
     *         The user to load the member from
     * @param  update
     *         Whether JDA should perform a request even if the member is already cached to update properties such as the name
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link RestAction} - Type: {@link Member}
     *
     * @see    #pruneMemberCache()
     * @see    #unloadMember(long)
     */
    @Nonnull
    default RestAction<Member> retrieveMember(@Nonnull User user, boolean update)
    {
        Checks.notNull(user, "User");
        return retrieveMemberById(user.getId(), update);
    }

    /**
     * Load the member for the specified user.
     * <br>If the member is already loaded it will be retrieved from {@link #getMemberById(long)}
     * and immediately provided if the member information is consistent. The cache consistency directly
     * relies on the enabled {@link GatewayIntent GatewayIntents} as {@link GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS}
     * is required to keep the cache updated with the latest information. You can pass {@code update = false} to always
     * return immediately if the member is cached regardless of cache consistency.
     *
     * <p>Possible {@link net.dv8tion.jda.api.exceptions.ErrorResponseException ErrorResponseExceptions} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER}
     *     <br>The specified user is not a member of this guild</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER}
     *     <br>The specified user does not exist</li>
     * </ul>
     *
     * @param  id
     *         The user id to load the member from
     * @param  update
     *         Whether JDA should perform a request even if the member is already cached to update properties such as the name
     *
     * @throws IllegalArgumentException
     *         If the provided id is empty or null
     * @throws NumberFormatException
     *         If the provided id is not a snowflake
     *
     * @return {@link RestAction} - Type: {@link Member}
     *
     * @see    #pruneMemberCache()
     * @see    #unloadMember(long)
     */
    @Nonnull
    default RestAction<Member> retrieveMemberById(@Nonnull String id, boolean update)
    {
        return retrieveMemberById(MiscUtil.parseSnowflake(id), update);
    }

    /**
     * Load the member for the specified user.
     * <br>If the member is already loaded it will be retrieved from {@link #getMemberById(long)}
     * and immediately provided if the member information is consistent. The cache consistency directly
     * relies on the enabled {@link GatewayIntent GatewayIntents} as {@link GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS}
     * is required to keep the cache updated with the latest information. You can pass {@code update = false} to always
     * return immediately if the member is cached regardless of cache consistency.
     *
     * <p>Possible {@link net.dv8tion.jda.api.exceptions.ErrorResponseException ErrorResponseExceptions} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER}
     *     <br>The specified user is not a member of this guild</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER}
     *     <br>The specified user does not exist</li>
     * </ul>
     *
     * @param  id
     *         The user id to load the member from
     * @param  update
     *         Whether JDA should perform a request even if the member is already cached to update properties such as the name
     *
     * @return {@link RestAction} - Type: {@link Member}
     *
     * @see    #pruneMemberCache()
     * @see    #unloadMember(long)
     */
    @Nonnull
    RestAction<Member> retrieveMemberById(long id, boolean update);

    /**
     * Shortcut for {@code guild.retrieveMemberById(guild.getOwnerIdLong())}.
     * <br>This will retrieve the current owner of the guild.
     * It is possible that the owner of a guild is no longer a registered discord user in which case this will fail.
     *
     * <p>Possible {@link net.dv8tion.jda.api.exceptions.ErrorResponseException ErrorResponseExceptions} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER}
     *     <br>The specified user is not a member of this guild</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER}
     *     <br>The specified user does not exist</li>
     * </ul>
     *
     * @param  update
     *         Whether JDA should perform a request even if the member is already cached to update properties such as the name
     *
     * @return {@link RestAction} - Type: {@link Member}
     *
     * @see    #pruneMemberCache()
     * @see    #unloadMember(long)
     *
     * @see    #getOwner()
     * @see    #getOwnerIdLong()
     * @see    #retrieveMemberById(long)
     */
    @Nonnull
    default RestAction<Member> retrieveOwner(boolean update)
    {
        return retrieveMemberById(getOwnerIdLong(), update);
    }

    /**
     * Retrieves a list of members.
     * <br>If the user does not resolve to a member of this guild, then it will not appear in the resulting list.
     * It is possible that none of the users resolve to a member, in which case an empty list will be the result.
     *
     * <p>If the {@link GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent is enabled,
     * this will load the {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} and {@link Activity Activities}
     * of the members. You can use {@link #retrieveMembers(boolean, Collection)} to disable presences.
     *
     * <p>The requests automatically timeout after {@code 10} seconds.
     * When the timeout occurs a {@link java.util.concurrent.TimeoutException TimeoutException} will be used to complete exceptionally.
     *
     * <p><b>You MUST NOT use blocking operations such as {@link Task#get()}!</b>
     * The response handling happens on the event thread by default.
     *
     * @param  users
     *         The users of the members (max 100)
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the input contains null</li>
     *             <li>If the input is more than 100 IDs</li>
     *         </ul>
     *
     * @return {@link Task} handle for the request
     */
    @Nonnull
    @CheckReturnValue
    default Task<List<Member>> retrieveMembers(@Nonnull Collection<User> users)
    {
        Checks.noneNull(users, "Users");
        if (users.isEmpty())
            return new GatewayTask<>(CompletableFuture.completedFuture(Collections.emptyList()), () -> {});

        long[] ids = users.stream().mapToLong(User::getIdLong).toArray();
        return retrieveMembersByIds(ids);
    }

    /**
     * Retrieves a list of members by their user id.
     * <br>If the id does not resolve to a member of this guild, then it will not appear in the resulting list.
     * It is possible that none of the IDs resolve to a member, in which case an empty list will be the result.
     *
     * <p>If the {@link GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent is enabled,
     * this will load the {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} and {@link Activity Activities}
     * of the members. You can use {@link #retrieveMembersByIds(boolean, Collection)} to disable presences.
     *
     * <p>The requests automatically timeout after {@code 10} seconds.
     * When the timeout occurs a {@link java.util.concurrent.TimeoutException TimeoutException} will be used to complete exceptionally.
     *
     * <p><b>You MUST NOT use blocking operations such as {@link Task#get()}!</b>
     * The response handling happens on the event thread by default.
     *
     * @param  ids
     *         The ids of the members (max 100)
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the input contains null</li>
     *             <li>If the input is more than 100 IDs</li>
     *         </ul>
     *
     * @return {@link Task} handle for the request
     */
    @Nonnull
    @CheckReturnValue
    default Task<List<Member>> retrieveMembersByIds(@Nonnull Collection<Long> ids)
    {
        Checks.noneNull(ids, "IDs");
        if (ids.isEmpty())
            return new GatewayTask<>(CompletableFuture.completedFuture(Collections.emptyList()), () -> {});

        long[] arr = ids.stream().mapToLong(Long::longValue).toArray();
        return retrieveMembersByIds(arr);
    }

    /**
     * Retrieves a list of members by their user id.
     * <br>If the id does not resolve to a member of this guild, then it will not appear in the resulting list.
     * It is possible that none of the IDs resolve to a member, in which case an empty list will be the result.
     *
     * <p>If the {@link GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent is enabled,
     * this will load the {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} and {@link Activity Activities}
     * of the members. You can use {@link #retrieveMembersByIds(boolean, String...)} to disable presences.
     *
     * <p>The requests automatically timeout after {@code 10} seconds.
     * When the timeout occurs a {@link java.util.concurrent.TimeoutException TimeoutException} will be used to complete exceptionally.
     *
     * <p><b>You MUST NOT use blocking operations such as {@link Task#get()}!</b>
     * The response handling happens on the event thread by default.
     *
     * @param  ids
     *         The ids of the members (max 100)
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the input contains null</li>
     *             <li>If the input is more than 100 IDs</li>
     *         </ul>
     *
     * @return {@link Task} handle for the request
     */
    @Nonnull
    @CheckReturnValue
    default Task<List<Member>> retrieveMembersByIds(@Nonnull String... ids)
    {
        Checks.notNull(ids, "Array");
        if (ids.length == 0)
            return new GatewayTask<>(CompletableFuture.completedFuture(Collections.emptyList()), () -> {});

        long[] arr = new long[ids.length];
        for (int i = 0; i < ids.length; i++)
            arr[i] = MiscUtil.parseSnowflake(ids[i]);
        return retrieveMembersByIds(arr);
    }

    /**
     * Retrieves a list of members by their user id.
     * <br>If the id does not resolve to a member of this guild, then it will not appear in the resulting list.
     * It is possible that none of the IDs resolve to a member, in which case an empty list will be the result.
     *
     * <p>If the {@link GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent is enabled,
     * this will load the {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} and {@link Activity Activities}
     * of the members. You can use {@link #retrieveMembersByIds(boolean, long...)} to disable presences.
     *
     * <p>The requests automatically timeout after {@code 10} seconds.
     * When the timeout occurs a {@link java.util.concurrent.TimeoutException TimeoutException} will be used to complete exceptionally.
     *
     * <p><b>You MUST NOT use blocking operations such as {@link Task#get()}!</b>
     * The response handling happens on the event thread by default.
     *
     * @param  ids
     *         The ids of the members (max 100)
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the input contains null</li>
     *             <li>If the input is more than 100 IDs</li>
     *         </ul>
     *
     * @return {@link Task} handle for the request
     */
    @Nonnull
    @CheckReturnValue
    default Task<List<Member>> retrieveMembersByIds(@Nonnull long... ids)
    {
        boolean presence = getJDA().getGatewayIntents().contains(GatewayIntent.GUILD_PRESENCES);
        return retrieveMembersByIds(presence, ids);
    }

    /**
     * Retrieves a list of members.
     * <br>If the user does not resolve to a member of this guild, then it will not appear in the resulting list.
     * It is possible that none of the users resolve to a member, in which case an empty list will be the result.
     *
     * <p>You can only load presences with the {@link GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent enabled.
     *
     * <p>The requests automatically timeout after {@code 10} seconds.
     * When the timeout occurs a {@link java.util.concurrent.TimeoutException TimeoutException} will be used to complete exceptionally.
     *
     * <p><b>You MUST NOT use blocking operations such as {@link Task#get()}!</b>
     * The response handling happens on the event thread by default.
     *
     * @param  includePresence
     *         Whether to load presences of the members (online status/activity)
     * @param  users
     *         The users of the members (max 100)
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If includePresence is {@code true} and the GUILD_PRESENCES intent is disabled</li>
     *             <li>If the input contains null</li>
     *             <li>If the input is more than 100 users</li>
     *         </ul>
     *
     * @return {@link Task} handle for the request
     */
    @Nonnull
    @CheckReturnValue
    default Task<List<Member>> retrieveMembers(boolean includePresence, @Nonnull Collection<User> users)
    {
        Checks.noneNull(users, "Users");
        if (users.isEmpty())
            return new GatewayTask<>(CompletableFuture.completedFuture(Collections.emptyList()), () -> {});

        long[] ids = users.stream().mapToLong(User::getIdLong).toArray();
        return retrieveMembersByIds(includePresence, ids);
    }

    /**
     * Retrieves a list of members by their user id.
     * <br>If the id does not resolve to a member of this guild, then it will not appear in the resulting list.
     * It is possible that none of the IDs resolve to a member, in which case an empty list will be the result.
     *
     * <p>You can only load presences with the {@link GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent enabled.
     *
     * <p>The requests automatically timeout after {@code 10} seconds.
     * When the timeout occurs a {@link java.util.concurrent.TimeoutException TimeoutException} will be used to complete exceptionally.
     *
     * <p><b>You MUST NOT use blocking operations such as {@link Task#get()}!</b>
     * The response handling happens on the event thread by default.
     *
     * @param  includePresence
     *         Whether to load presences of the members (online status/activity)
     * @param  ids
     *         The ids of the members (max 100)
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If includePresence is {@code true} and the GUILD_PRESENCES intent is disabled</li>
     *             <li>If the input contains null</li>
     *             <li>If the input is more than 100 IDs</li>
     *         </ul>
     *
     * @return {@link Task} handle for the request
     */
    @Nonnull
    @CheckReturnValue
    default Task<List<Member>> retrieveMembersByIds(boolean includePresence, @Nonnull Collection<Long> ids)
    {
        Checks.noneNull(ids, "IDs");
        if (ids.isEmpty())
            return new GatewayTask<>(CompletableFuture.completedFuture(Collections.emptyList()), () -> {});

        long[] arr = ids.stream().mapToLong(Long::longValue).toArray();
        return retrieveMembersByIds(includePresence, arr);
    }

    /**
     * Retrieves a list of members by their user id.
     * <br>If the id does not resolve to a member of this guild, then it will not appear in the resulting list.
     * It is possible that none of the IDs resolve to a member, in which case an empty list will be the result.
     *
     * <p>You can only load presences with the {@link GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent enabled.
     *
     * <p>The requests automatically timeout after {@code 10} seconds.
     * When the timeout occurs a {@link java.util.concurrent.TimeoutException TimeoutException} will be used to complete exceptionally.
     *
     * <p><b>You MUST NOT use blocking operations such as {@link Task#get()}!</b>
     * The response handling happens on the event thread by default.
     *
     * @param  includePresence
     *         Whether to load presences of the members (online status/activity)
     * @param  ids
     *         The ids of the members (max 100)
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If includePresence is {@code true} and the GUILD_PRESENCES intent is disabled</li>
     *             <li>If the input contains null</li>
     *             <li>If the input is more than 100 IDs</li>
     *         </ul>
     *
     * @return {@link Task} handle for the request
     */
    @Nonnull
    @CheckReturnValue
    default Task<List<Member>> retrieveMembersByIds(boolean includePresence, @Nonnull String... ids)
    {
        Checks.notNull(ids, "Array");
        if (ids.length == 0)
            return new GatewayTask<>(CompletableFuture.completedFuture(Collections.emptyList()), () -> {});

        long[] arr = new long[ids.length];
        for (int i = 0; i < ids.length; i++)
            arr[i] = MiscUtil.parseSnowflake(ids[i]);
        return retrieveMembersByIds(includePresence, arr);
    }

    /**
     * Retrieves a list of members by their user id.
     * <br>If the id does not resolve to a member of this guild, then it will not appear in the resulting list.
     * It is possible that none of the IDs resolve to a member, in which case an empty list will be the result.
     *
     * <p>You can only load presences with the {@link GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent enabled.
     *
     * <p>The requests automatically timeout after {@code 10} seconds.
     * When the timeout occurs a {@link java.util.concurrent.TimeoutException TimeoutException} will be used to complete exceptionally.
     *
     * <p><b>You MUST NOT use blocking operations such as {@link Task#get()}!</b>
     * The response handling happens on the event thread by default.
     *
     * @param  includePresence
     *         Whether to load presences of the members (online status/activity)
     * @param  ids
     *         The ids of the members (max 100)
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If includePresence is {@code true} and the GUILD_PRESENCES intent is disabled</li>
     *             <li>If the input contains null</li>
     *             <li>If the input is more than 100 IDs</li>
     *         </ul>
     *
     * @return {@link Task} handle for the request
     */
    @Nonnull
    @CheckReturnValue
    Task<List<Member>> retrieveMembersByIds(boolean includePresence, @Nonnull long... ids);

    /**
     * Queries a list of members using a radix tree based on the provided name prefix.
     * <br>This will check both the username and the nickname of the members.
     * Additional filtering may be required. If no members with the specified prefix exist, the list will be empty.
     *
     * <p>The requests automatically timeout after {@code 10} seconds.
     * When the timeout occurs a {@link java.util.concurrent.TimeoutException TimeoutException} will be used to complete exceptionally.
     *
     * <p><b>You MUST NOT use blocking operations such as {@link Task#get()}!</b>
     * The response handling happens on the event thread by default.
     *
     * @param  prefix
     *         The case-insensitive name prefix
     * @param  limit
     *         The max amount of members to retrieve (1-100)
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided prefix is null or empty.</li>
     *             <li>If the provided limit is not in the range of [1, 100]</li>
     *         </ul>
     *
     * @return {@link Task} handle for the request
     *
     * @see    #getMembersByName(String, boolean)
     * @see    #getMembersByNickname(String, boolean)
     * @see    #getMembersByEffectiveName(String, boolean)
     */
    @Nonnull
    @CheckReturnValue
    Task<List<Member>> retrieveMembersByPrefix(@Nonnull String prefix, int limit);

    @Nonnull
    @CheckReturnValue
    RestAction<List<ThreadChannel>> retrieveActiveThreads();


    /**
     * Retrieves a {@link GuildScheduledEvent} by its ID.
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_GUILD_SCHEDULED_EVENT ErrorResponse.UNKOWN_GUILD_SCHEDULED_EVENT}
     *     <br>A scheduled event with the specified ID does not exist in the guild, or the currently logged in user does not
     *     have access to it.</li>
     * </ul>
     *
     * @param  id
     *         The ID of the {@link GuildScheduledEvent}
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent}
     *
     * @see #getScheduledEventById(long)
     */
    @Nonnull
    @CheckReturnValue
    RestAction<GuildScheduledEvent> retrieveScheduledEventById(long id);

    /**
     * Retrieves a {@link GuildScheduledEvent} by its ID.
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_GUILD_SCHEDULED_EVENT ErrorResponse.UNKOWN_GUILD_SCHEDULED_EVENT}
     *     <br>A scheduled event with the specified ID does not exist in this guild, or the currently logged in user does not
     *     have access to it.</li>
     * </ul>
     *
     * @param  id
     *         The ID of the {@link GuildScheduledEvent}
     *
     * @throws IllegalArgumentException
     *         If the specified ID is {@code null} or empty
     *
     * @throws NumberFormatException
     *         If the specified ID cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent}
     *
     * @see #getScheduledEventById(long)
     */
    @Nonnull
    @CheckReturnValue
    RestAction<GuildScheduledEvent> retrieveScheduledEventById(String id);

    /**
     * Retrieves a {@link GuildScheduledEvent} by its ID.
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_GUILD_SCHEDULED_EVENT ErrorResponse.UNKOWN_GUILD_SCHEDULED_EVENT}
     *     <br>A scheduled event with the specified ID does not exist in this guild, or the currently logged in user does not
     *     have access to it.</li>
     * </ul>
     *
     * @param  id
     *         The ID of the {@link GuildScheduledEvent}
     *
     * @throws IllegalArgumentException
     *         If the specified ID is {@code null} or empty
     *
     * @throws NumberFormatException
     *         If the specified ID cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent}
     *
     * @see #getScheduledEventById(long)
     */

    /* From GuildController */

    /**
     * Used to move a {@link net.dv8tion.jda.api.entities.Member Member} from one {@link net.dv8tion.jda.api.entities.AudioChannel AudioChannel}
     * to another {@link net.dv8tion.jda.api.entities.AudioChannel AudioChannel}.
     * <br>As a note, you cannot move a Member that isn't already in a AudioChannel. Also they must be in a AudioChannel
     * in the same Guild as the one that you are moving them to.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be moved due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission was removed</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The specified channel was deleted before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} that you are moving.
     * @param  audioChannel
     *         The destination {@link net.dv8tion.jda.api.entities.AudioChannel AudioChannel} to which the member is being
     *         moved to. Or null to perform a voice kick.
     *
     * @throws IllegalStateException
     *         If the Member isn't currently in a AudioChannel in this Guild, or {@link net.dv8tion.jda.api.utils.cache.CacheFlag#VOICE_STATE} is disabled.
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided member is {@code null}</li>
     *             <li>If the provided Member isn't part of this {@link net.dv8tion.jda.api.entities.Guild Guild}</li>
     *             <li>If the provided AudioChannel isn't part of this {@link net.dv8tion.jda.api.entities.Guild Guild}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         <ul>
     *             <li>If this account doesn't have {@link net.dv8tion.jda.api.Permission#VOICE_MOVE_OTHERS}
     *                 in the AudioChannel that the Member is currently in.</li>
     *             <li>If this account <b>AND</b> the Member being moved don't have
     *                 {@link net.dv8tion.jda.api.Permission#VOICE_CONNECT} for the destination AudioChannel.</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> moveVoiceMember(@Nonnull Member member, @Nullable AudioChannel audioChannel);

    /**
     * Used to kick a {@link net.dv8tion.jda.api.entities.Member Member} from a {@link net.dv8tion.jda.api.entities.AudioChannel AudioChannel}.
     * <br>As a note, you cannot kick a Member that isn't already in a AudioChannel. Also they must be in a AudioChannel
     * in the same Guild.
     *
     * <p>Equivalent to {@code moveVoiceMember(member, null)}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be moved due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The specified channel was deleted before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} that you are moving.
     *
     * @throws IllegalStateException
     *         If the Member isn't currently in a AudioChannel in this Guild, or {@link net.dv8tion.jda.api.utils.cache.CacheFlag#VOICE_STATE} is disabled.
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the provided arguments is {@code null}</li>
     *             <li>If the provided Member isn't part of this {@link net.dv8tion.jda.api.entities.Guild Guild}</li>
     *             <li>If the provided AudioChannel isn't part of this {@link net.dv8tion.jda.api.entities.Guild Guild}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this account doesn't have {@link net.dv8tion.jda.api.Permission#VOICE_MOVE_OTHERS}
     *         in the AudioChannel that the Member is currently in.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> kickVoiceMember(@Nonnull Member member)
    {
        return moveVoiceMember(member, null);
    }

    /**
     * Changes the Member's nickname in this guild.
     * The nickname is visible to all members of this guild.
     *
     * <p>To change the nickname for the currently logged in account
     * only the Permission {@link net.dv8tion.jda.api.Permission#NICKNAME_CHANGE NICKNAME_CHANGE} is required.
     * <br>To change the nickname of <b>any</b> {@link net.dv8tion.jda.api.entities.Member Member} for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     * the Permission {@link net.dv8tion.jda.api.Permission#NICKNAME_MANAGE NICKNAME_MANAGE} is required.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The nickname of the target Member is not modifiable due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} for which the nickname should be changed.
     * @param  nickname
     *         The new nickname of the {@link net.dv8tion.jda.api.entities.Member Member}, provide {@code null} or an
     *         empty String to reset the nickname
     *
     * @throws IllegalArgumentException
     *         If the specified {@link net.dv8tion.jda.api.entities.Member Member}
     *         is not from the same {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *         Or if the provided member is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         <ul>
     *             <li>If attempting to set nickname for self and the logged in account has neither {@link net.dv8tion.jda.api.Permission#NICKNAME_CHANGE}
     *                 or {@link net.dv8tion.jda.api.Permission#NICKNAME_MANAGE}</li>
     *             <li>If attempting to set nickname for another member and the logged in account does not have {@link net.dv8tion.jda.api.Permission#NICKNAME_MANAGE}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If attempting to set nickname for another member and the logged in account cannot manipulate the other user due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> modifyNickname(@Nonnull Member member, @Nullable String nickname);

    /**
     * This method will prune (kick) all members who were offline for at least <i>days</i> days.
     * <br>The RestAction returned from this method will return the amount of Members that were pruned.
     * <br>You can use {@link Guild#retrievePrunableMemberCount(int)} to determine how many Members would be pruned if you were to
     * call this method.
     *
     * <p>This might timeout when pruning many members.
     * You can use {@code prune(days, false)} to ignore the prune count and avoid a timeout.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The prune cannot finished due to a permission discrepancy</li>
     * </ul>
     *
     * @param  days
     *         Minimum number of days since a member has been offline to get affected.
     * @param  roles
     *         Optional roles to include in prune filter
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the account doesn't have {@link net.dv8tion.jda.api.Permission#KICK_MEMBERS KICK_MEMBER} Permission.
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided days are not in the range from 1 to 30 (inclusive)</li>
     *             <li>If null is provided</li>
     *             <li>If any of the provided roles is not from this guild</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction} - Type: Integer
     *         <br>The amount of Members that were pruned from the Guild.
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Integer> prune(int days, @Nonnull Role... roles)
    {
        return prune(days, true, roles);
    }

    /**
     * This method will prune (kick) all members who were offline for at least <i>days</i> days.
     * <br>The RestAction returned from this method will return the amount of Members that were pruned.
     * <br>You can use {@link Guild#retrievePrunableMemberCount(int)} to determine how many Members would be pruned if you were to
     * call this method.
     *
     * <p>This might timeout when pruning many members with {@code wait=true}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The prune cannot finished due to a permission discrepancy</li>
     * </ul>
     *
     * @param  days
     *         Minimum number of days since a member has been offline to get affected.
     * @param  wait
     *         Whether to calculate the number of pruned members and wait for the response (timeout for too many pruned)
     * @param  roles
     *         Optional roles to include in prune filter
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the account doesn't have {@link net.dv8tion.jda.api.Permission#KICK_MEMBERS KICK_MEMBER} Permission.
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided days are not in the range from 1 to 30 (inclusive)</li>
     *             <li>If null is provided</li>
     *             <li>If any of the provided roles is not from this guild</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction} - Type: Integer
     *         <br>Provides the amount of Members that were pruned from the Guild, if wait is true.
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Integer> prune(int days, boolean wait, @Nonnull Role... roles);

    /**
     * Kicks the {@link net.dv8tion.jda.api.entities.Member Member} from the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.api.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.api.entities.User User}
     * until Discord sends the {@link net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent GuildMemberRemoveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be kicked due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} to kick
     *         from the from the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * @param  reason
     *         The reason for this action or {@code null} if there is no specified reason
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided member is not a Member of this Guild or is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#KICK_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot kick the other member due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     * @throws java.lang.IllegalArgumentException
     *         If the provided reason is longer than 512 characters
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *         Kicks the provided Member from the current Guild
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> kick(@Nonnull Member member, @Nullable String reason);

    /**
     * Kicks the {@link net.dv8tion.jda.api.entities.Member Member} specified by the userId from the from the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.api.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.api.entities.User User}
     * until Discord sends the {@link net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent GuildMemberRemoveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be kicked due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  userId
     *         The id of the {@link net.dv8tion.jda.api.entities.User User} to kick
     *         from the from the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * @param  reason
     *         The reason for this action or {@code null} if there is no specified reason
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#KICK_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot kick the other member due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     * @throws java.lang.IllegalArgumentException
     *         If the user for the provided id cannot be kicked from this Guild or the provided {@code userId} is blank/null.
     * @throws java.lang.IllegalArgumentException
     *         If the provided reason is longer than 512 characters
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> kick(@Nonnull String userId, @Nullable String reason);

    /**
     * Kicks a {@link net.dv8tion.jda.api.entities.Member Member} from the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.api.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.api.entities.User User}
     * until Discord sends the {@link net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent GuildMemberRemoveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be kicked due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} to kick from the from the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided member is not a Member of this Guild or is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#KICK_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot kick the other member due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *         Kicks the provided Member from the current Guild
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> kick(@Nonnull Member member)
    {
        return kick(member, null);
    }

    /**
     * Kicks the {@link net.dv8tion.jda.api.entities.Member Member} specified by the userId from the from the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.api.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.api.entities.User User}
     * until Discord sends the {@link net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent GuildMemberRemoveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be kicked due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  userId
     *         The id of the {@link net.dv8tion.jda.api.entities.User User} to kick from the from the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#KICK_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot kick the other member due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     * @throws java.lang.IllegalArgumentException
     *         If the userId provided does not correspond to a Member in this Guild or the provided {@code userId} is blank/null.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> kick(@Nonnull String userId)
    {
        return kick(userId, null);
    }

    /**
     * Bans the {@link net.dv8tion.jda.api.entities.User User} and deletes messages sent by the user
     * based on the amount of delDays.
     * <br>If you wish to ban a user without deleting any messages, provide delDays with a value of 0.
     *
     * <p>You can unban a user with {@link net.dv8tion.jda.api.entities.Guild#unban(User) Guild.unban(User)}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.api.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.api.entities.User User's}
     * {@link net.dv8tion.jda.api.entities.Member Member} object (if the User was in the Guild)
     * until Discord sends the {@link net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent GuildMemberRemoveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be banned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  user
     *         The {@link net.dv8tion.jda.api.entities.User User} to ban.
     * @param  delDays
     *         The history of messages, in days, that will be deleted.
     * @param  reason
     *         The reason for this action or {@code null} if there is no specified reason
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot ban the other user due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided amount of days (delDays) is less than 0.</li>
     *             <li>If the provided amount of days (delDays) is bigger than 7.</li>
     *             <li>If the provided reason is longer than 512 characters.</li>
     *             <li>If the provided user is null</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> ban(@Nonnull User user, int delDays, @Nullable String reason);

    /**
     * Bans the user specified by the userId and deletes messages sent by the user
     * based on the amount of delDays.
     * <br>If you wish to ban a user without deleting any messages, provide delDays with a value of 0.
     *
     * <p>You can unban a user with {@link net.dv8tion.jda.api.entities.Guild#unban(User) Guild.unban(User)}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.api.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.api.entities.User User's}
     * {@link net.dv8tion.jda.api.entities.Member Member} object (if the User was in the Guild)
     * until Discord sends the {@link net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent GuildMemberRemoveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be banned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER UNKNOWN_USER}
     *     <br>The specified User does not exit</li>
     * </ul>
     *
     * @param  userId
     *         The id of the {@link net.dv8tion.jda.api.entities.User User} to ban.
     * @param  delDays
     *         The history of messages, in days, that will be deleted.
     * @param  reason
     *         The reason for this action or {@code null} if there is no specified reason
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot ban the other user due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided amount of days (delDays) is less than 0.</li>
     *             <li>If the provided amount of days (delDays) is bigger than 7.</li>
     *             <li>If the provided reason is longer than 512 characters.</li>
     *             <li>If the provided userId is null</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> ban(@Nonnull String userId, int delDays, @Nullable String reason);

    /**
     * Bans the {@link net.dv8tion.jda.api.entities.Member Member} and deletes messages sent by the user
     * based on the amount of delDays.
     * <br>If you wish to ban a member without deleting any messages, provide delDays with a value of 0.
     *
     * <p>You can unban a user with {@link net.dv8tion.jda.api.entities.Guild#unban(User) Guild.unban(User)}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.api.entities.Guild#getMembers()} will still contain the
     * {@link net.dv8tion.jda.api.entities.Member Member} until Discord sends the
     * {@link net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent GuildMemberRemoveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be banned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} to ban.
     * @param  delDays
     *         The history of messages, in days, that will be deleted.
     * @param  reason
     *         The reason for this action or {@code null} if there is no specified reason
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot ban the other user due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided amount of days (delDays) is less than 0.</li>
     *             <li>If the provided amount of days (delDays) is bigger than 7.</li>
     *             <li>If the provided reason is longer than 512 characters.</li>
     *             <li>If the provided member is {@code null}</li>
     *         </ul>
     *
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> ban(@Nonnull Member member, int delDays, @Nullable String reason)
    {
        Checks.notNull(member, "Member");
        //Don't check if the provided member is from this guild. It doesn't matter if they are or aren't.

        return ban(member.getUser(), delDays, reason);
    }

    /**
     * Bans the {@link net.dv8tion.jda.api.entities.Member Member} and deletes messages sent by the user
     * based on the amount of delDays.
     * <br>If you wish to ban a member without deleting any messages, provide delDays with a value of 0.
     *
     * <p>You can unban a user with {@link net.dv8tion.jda.api.entities.Guild#unban(User) Guild.unban(User)}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.api.entities.Guild#getMembers()} will still contain the
     * {@link net.dv8tion.jda.api.entities.Member Member} until Discord sends the
     * {@link net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent GuildMemberRemoveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be banned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} to ban.
     * @param  delDays
     *         The history of messages, in days, that will be deleted.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot ban the other user due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided amount of days (delDays) is less than 0.</li>
     *             <li>If the provided amount of days (delDays) is bigger than 7.</li>
     *             <li>If the provided member is {@code null}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> ban(@Nonnull Member member, int delDays)
    {
        return ban(member, delDays, null);
    }

    /**
     * Bans the {@link net.dv8tion.jda.api.entities.Member Member} and deletes messages sent by the user
     * based on the amount of delDays.
     * <br>If you wish to ban a member without deleting any messages, provide delDays with a value of 0.
     *
     * <p>You can unban a user with {@link net.dv8tion.jda.api.entities.Guild#unban(User) Guild.unban(User)}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.api.entities.Guild#getMembers()} will still contain the
     * {@link net.dv8tion.jda.api.entities.Member Member} until Discord sends the
     * {@link net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent GuildMemberRemoveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be banned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  user
     *         The {@link net.dv8tion.jda.api.entities.User User} to ban.
     * @param  delDays
     *         The history of messages, in days, that will be deleted.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot ban the other user due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided amount of days (delDays) is less than 0.</li>
     *             <li>If the provided amount of days (delDays) is bigger than 7.</li>
     *             <li>If the provided user is {@code null}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> ban(@Nonnull User user, int delDays)
    {
        return ban(user, delDays, null);
    }

    /**
     * Bans the user specified by the userId and deletes messages sent by the user
     * based on the amount of delDays.
     * <br>If you wish to ban a user without deleting any messages, provide delDays with a value of 0.
     *
     * <p>You can unban a user with {@link net.dv8tion.jda.api.entities.Guild#unban(User) Guild.unban(User)}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.api.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.api.entities.User User's}
     * {@link net.dv8tion.jda.api.entities.Member Member} object (if the User was in the Guild)
     * until Discord sends the {@link net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent GuildMemberRemoveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be banned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  userId
     *         The id of the {@link net.dv8tion.jda.api.entities.User User} to ban.
     * @param  delDays
     *         The history of messages, in days, that will be deleted.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot ban the other user due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided amount of days (delDays) is less than 0.</li>
     *             <li>If the provided amount of days (delDays) is bigger than 7.</li>
     *             <li>If the provided userId is {@code null}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> ban(@Nonnull String userId, int delDays)
    {
        return ban(userId, delDays, null);
    }

    /**
     * Unbans the specified {@link net.dv8tion.jda.api.entities.User User} from this Guild.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be unbanned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER UNKNOWN_USER}
     *     <br>The specified User is invalid</li>
     * </ul>
     *
     * @param  user
     *         The id of the {@link net.dv8tion.jda.api.entities.User User} to unban.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#BAN_MEMBERS} permission.
     * @throws IllegalArgumentException
     *         If the provided user is null
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> unban(@Nonnull User user)
    {
        Checks.notNull(user, "User");

        return unban(user.getId());
    }

    /**
     * Unbans the a user specified by the userId from this Guild.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be unbanned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER UNKNOWN_USER}
     *     <br>The specified User does not exist</li>
     * </ul>
     *
     * @param  userId
     *         The id of the {@link net.dv8tion.jda.api.entities.User User} to unban.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#BAN_MEMBERS} permission.
     * @throws IllegalArgumentException
     *         If the provided id is null or blank
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> unban(@Nonnull String userId);

    /**
     * Puts the specified Member in time out in this {@link net.dv8tion.jda.api.entities.Guild Guild} for a specific amount of time.
     * <br>While a Member is in time out, they cannot send messages, reply, react, or speak in voice channels.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be put into time out due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The member to put in time out
     * @param  amount
     *         The amount of the provided {@link TimeUnit unit} to put the specified Member in time out for
     * @param  unit
     *         The {@link TimeUnit Unit} type of {@code amount}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MODERATE_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot put a timeout on the other Member due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     * @throws IllegalArgumentException
     *         If any of the following checks are true
     *         <ul>
     *             <li>The provided {@code member} is null</li>
     *             <li>The provided {@code amount} is lower than or equal to {@code 0}</li>
     *             <li>The provided {@code unit} is null</li>
     *             <li>The provided {@code amount} with the {@code unit} results in a date that is more than {@value Member#MAX_TIME_OUT_LENGTH} days in the future</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> timeoutFor(@Nonnull Member member, long amount, @Nonnull TimeUnit unit)
    {
        Checks.check(amount >= 1, "The amount must be more than 0");
        Checks.notNull(unit, "TimeUnit");
        return timeoutUntil(member, Helpers.toOffset(System.currentTimeMillis() + unit.toMillis(amount)));
    }

    /**
     * Puts the specified Member in time out in this {@link net.dv8tion.jda.api.entities.Guild Guild} for a specific amount of time.
     * <br>While a Member is in time out, all permissions except {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} and
     * {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY} are removed from them.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be put into time out due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The member to put in time out
     * @param  duration
     *         The duration to put the specified Member in time out for
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MODERATE_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot put a timeout on the other Member due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     * @throws IllegalArgumentException
     *         If any of the following checks are true
     *         <ul>
     *             <li>The provided {@code member} is null</li>
     *             <li>The provided {@code duration} is null</li>
     *             <li>The provided {@code duration} results in a date that is more than {@value Member#MAX_TIME_OUT_LENGTH} days in the future</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> timeoutFor(@Nonnull Member member, @Nonnull Duration duration)
    {
        Checks.notNull(duration, "Duration");
        return timeoutUntil(member, Helpers.toOffset(System.currentTimeMillis() + duration.toMillis()));
    }

    /**
     * Puts the specified Member in time out in this {@link net.dv8tion.jda.api.entities.Guild Guild} until the specified date.
     * <br>While a Member is in time out, all permissions except {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} and
     * {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY} are removed from them.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be put into time out due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The member to put in time out
     * @param  temporal
     *         The time the specified Member will be released from time out or null to remove the time out
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MODERATE_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot put a timeout on the other Member due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     * @throws IllegalArgumentException
     *         If any of the following are true
     *         <ul>
     *             <li>The provided {@code member} is null</li>
     *             <li>The provided {@code temporal} is in the past</li>
     *             <li>The provided {@code temporal} is more than {@value Member#MAX_TIME_OUT_LENGTH} days in the future</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> timeoutUntil(@Nonnull Member member, @Nonnull TemporalAccessor temporal)
    {
        Checks.notNull(member, "Member");
        if (!getSelfMember().canInteract(member))
            throw new HierarchyException("Can't modify a member with higher or equal highest role than yourself!");
        return timeoutUntilById(member.getId(), temporal);
    }

    /**
     * Puts a Member specified by the id in time out in this {@link net.dv8tion.jda.api.entities.Guild Guild} for a specific amount of time.
     * <br>While a Member is in time out, all permissions except {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} and
     * {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY} are removed from them.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be put into time out due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  userId
     *         The user id of the Member to put in time out
     * @param  amount
     *         The amount of the provided {@link TimeUnit unit} to put the specified Member in time out for
     * @param  unit
     *         The {@link TimeUnit Unit} type of {@code amount}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MODERATE_MEMBERS} permission.
     * @throws IllegalArgumentException
     *         If any of the following checks are true
     *         <ul>
     *             <li>The provided {@code userId} is not a valid snowflake</li>
     *             <li>The provided {@code amount} is lower than or equal to {@code 0}</li>
     *             <li>The provided {@code unit} is null</li>
     *             <li>The provided {@code amount} with the {@code unit} results in a date that is more than {@value Member#MAX_TIME_OUT_LENGTH} days in the future</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> timeoutForById(long userId, long amount, @Nonnull TimeUnit unit)
    {
        return timeoutForById(Long.toUnsignedString(userId), amount, unit);
    }

    /**
     * Puts a Member specified by the id in time out in this {@link net.dv8tion.jda.api.entities.Guild Guild} for a specific amount of time.
     * <br>While a Member is in time out, all permissions except {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} and
     * {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY} are removed from them.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be put into time out due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  userId
     *         The user id of the Member to put in time out
     * @param  amount
     *         The amount of the provided {@link TimeUnit unit} to put the specified Member in time out for
     * @param  unit
     *         The {@link TimeUnit Unit} type of {@code amount}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MODERATE_MEMBERS} permission.
     * @throws IllegalArgumentException
     *         If any of the following checks are true
     *         <ul>
     *             <li>The provided {@code userId} is not a valid snowflake</li>
     *             <li>The provided {@code amount} is lower than or equal to {@code 0}</li>
     *             <li>The provided {@code unit} is null</li>
     *             <li>The provided {@code amount} with the {@code unit} results in a date that is more than {@value Member#MAX_TIME_OUT_LENGTH} days in the future</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> timeoutForById(@Nonnull String userId, long amount, @Nonnull TimeUnit unit)
    {
        Checks.check(amount >= 1, "The amount must be more than 0");
        Checks.notNull(unit, "TimeUnit");
        return timeoutUntilById(userId, Helpers.toOffset(System.currentTimeMillis() + unit.toMillis(amount)));
    }

    /**
     * Puts the specified Member in time out in this {@link net.dv8tion.jda.api.entities.Guild Guild} for a specific amount of time.
     * <br>While a Member is in time out, all permissions except {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} and
     * {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY} are removed from them.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be put into time out due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  userId
     *         The user id of the Member to put in time out
     * @param  duration
     *         The duration to put the specified Member in time out for
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MODERATE_MEMBERS} permission.
     * @throws IllegalArgumentException
     *         If any of the following checks are true
     *         <ul>
     *             <li>The provided {@code userId} is not a valid snowflake</li>
     *             <li>The provided {@code duration} is null</li>
     *             <li>The provided {@code duration} is not positive</li>
     *             <li>The provided {@code duration} results in a date that is more than {@value Member#MAX_TIME_OUT_LENGTH} days in the future</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> timeoutForById(long userId, @Nonnull Duration duration)
    {
        return timeoutForById(Long.toUnsignedString(userId), duration);
    }

    /**
     * Puts the specified Member in time out in this {@link net.dv8tion.jda.api.entities.Guild Guild} for a specific amount of time.
     * <br>While a Member is in time out, all permissions except {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} and
     * {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY} are removed from them.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be put into time out due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  userId
     *         The user id of the Member to put in time out
     * @param  duration
     *         The duration to put the specified Member in time out for
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MODERATE_MEMBERS} permission.
     * @throws IllegalArgumentException
     *         If any of the following checks are true
     *         <ul>
     *             <li>The provided {@code userId} is not a valid snowflake</li>
     *             <li>The provided {@code duration} is null</li>
     *             <li>The provided {@code duration} is not positive</li>
     *             <li>The provided {@code duration} results in a date that is more than {@value Member#MAX_TIME_OUT_LENGTH} days in the future</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> timeoutForById(@Nonnull String userId, @Nonnull Duration duration)
    {
        Checks.notNull(duration, "Duration");
        Checks.check(!(duration.isNegative() || duration.isZero()), "Duration may not be negative or zero");
        return timeoutUntilById(userId, Helpers.toOffset(System.currentTimeMillis() + duration.toMillis()));
    }

    /**
     * Puts a Member specified by the id in time out in this {@link net.dv8tion.jda.api.entities.Guild Guild} until the specified date.
     * <br>While a Member is in time out, all permissions except {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} and
     * {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY} are removed from them.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be put into time out due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  userId
     *         The user id of the Member to put in time out
     * @param  temporal
     *         The time the specified Member will be released from time out or null to remove the time out
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MODERATE_MEMBERS} permission.
     * @throws IllegalArgumentException
     *         If any of the following checks are true
     *         <ul>
     *             <li>The provided {@code userId} is not a valid snowflake</li>
     *             <li>The provided {@code temporal} is in the past</li>
     *             <li>The provided {@code temporal} is more than {@value Member#MAX_TIME_OUT_LENGTH} days in the future</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> timeoutUntilById(long userId, @Nonnull TemporalAccessor temporal)
    {
        return timeoutUntilById(Long.toUnsignedString(userId), temporal);
    }

    /**
     * Puts a Member specified by the id in time out in this {@link net.dv8tion.jda.api.entities.Guild Guild} until the specified date.
     * <br>While a Member is in time out, all permissions except {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} and
     * {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY} are removed from them.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be timed out due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  userId
     *         The user id of the Member to put in time out
     * @param  temporal
     *         The time the specified Member will be released from time out or null to remove the time out
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MODERATE_MEMBERS} permission.
     * @throws IllegalArgumentException
     *         If any of the following checks are true
     *         <ul>
     *             <li>The provided {@code userId} is not a valid snowflake</li>
     *             <li>The provided {@code temporal} is null</li>
     *             <li>The provided {@code temporal} is in the past</li>
     *             <li>The provided {@code temporal} is more than {@value Member#MAX_TIME_OUT_LENGTH} days in the future</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> timeoutUntilById(@Nonnull String userId, @Nonnull TemporalAccessor temporal);

    /**
     * Removes a time out from the specified Member in this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The time out cannot be removed due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The Member to remove a time out from
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MODERATE_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot remove the timeout from the other Member due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    default AuditableRestAction<Void> removeTimeout(@Nonnull Member member)
    {
        Checks.notNull(member, "Member");
        if (!getSelfMember().canInteract(member))
            throw new HierarchyException("Can't modify a member with higher or equal highest role than yourself!");
        return removeTimeoutById(member.getId());
    }

    /**
     * Removes a time out from a Member specified by the id in this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The time out cannot be removed due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  userId
     *         The user id of the Member to put in time out
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MODERATE_MEMBERS} permission.
     * @throws IllegalArgumentException
     *         If the specified user id is not a valid snowflake
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    default AuditableRestAction<Void> removeTimeoutById(long userId)
    {
        return removeTimeoutById(Long.toUnsignedString(userId));
    }

    /**
     * Removes a time out from a Member specified by the id in this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The time out cannot be removed due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  userId
     *         The user id of the Member to put in time out
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MODERATE_MEMBERS} permission.
     * @throws IllegalArgumentException
     *         If the specified user id is not a valid snowflake
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> removeTimeoutById(@Nonnull String userId);

    /**
     * Sets the Guild Deafened state state of the {@link net.dv8tion.jda.api.entities.Member Member} based on the provided
     * boolean.
     *
     * <p><b>Note:</b> The Member's {@link net.dv8tion.jda.api.entities.GuildVoiceState#isGuildDeafened() GuildVoiceState.isGuildDeafened()} value won't change
     * until JDA receives the {@link net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent GuildVoiceGuildDeafenEvent} event related to this change.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be deafened due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#USER_NOT_CONNECTED USER_NOT_CONNECTED}
     *     <br>The specified Member is not connected to a voice channel</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} who's {@link GuildVoiceState VoiceState} is being changed.
     * @param  deafen
     *         Whether this {@link net.dv8tion.jda.api.entities.Member Member} should be deafened or undeafened.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#VOICE_DEAF_OTHERS} permission.
     * @throws IllegalArgumentException
     *         If the provided member is not from this Guild or null.
     * @throws java.lang.IllegalStateException
     *         If the provided member is not currently connected to a voice channel.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> deafen(@Nonnull Member member, boolean deafen);

    /**
     * Sets the Guild Muted state state of the {@link net.dv8tion.jda.api.entities.Member Member} based on the provided
     * boolean.
     *
     * <p><b>Note:</b> The Member's {@link net.dv8tion.jda.api.entities.GuildVoiceState#isGuildMuted() GuildVoiceState.isGuildMuted()} value won't change
     * until JDA receives the {@link net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent GuildVoiceGuildMuteEvent} event related to this change.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be muted due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#USER_NOT_CONNECTED USER_NOT_CONNECTED}
     *     <br>The specified Member is not connected to a voice channel</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} who's {@link GuildVoiceState VoiceState} is being changed.
     * @param  mute
     *         Whether this {@link net.dv8tion.jda.api.entities.Member Member} should be muted or unmuted.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#VOICE_DEAF_OTHERS} permission.
     * @throws java.lang.IllegalArgumentException
     *         If the provided member is not from this Guild or null.
     * @throws java.lang.IllegalStateException
     *         If the provided member is not currently connected to a voice channel.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> mute(@Nonnull Member member, boolean mute);

    /**
     * Atomically assigns the provided {@link net.dv8tion.jda.api.entities.Role Role} to the specified {@link net.dv8tion.jda.api.entities.Member Member}.
     * <br><b>This can be used together with other role modification methods as it does not require an updated cache!</b>
     *
     * <p>If multiple roles should be added/removed (efficiently) in one request
     * you may use {@link #modifyMemberRoles(Member, Collection, Collection) modifyMemberRoles(Member, Collection, Collection)} or similar methods.
     *
     * <p>If the specified role is already present in the member's set of roles this does nothing.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_ROLE UNKNOWN_ROLE}
     *     <br>If the specified Role does not exist</li>
     * </ul>
     *
     * @param  member
     *         The target member who will receive the new role
     * @param  role
     *         The role which should be assigned atomically
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the specified member/role are not from the current Guild</li>
     *             <li>Either member or role are {@code null}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> addRoleToMember(@Nonnull Member member, @Nonnull Role role);

    /**
     * Atomically assigns the provided {@link net.dv8tion.jda.api.entities.Role Role} to the specified member by their user id.
     * <br><b>This can be used together with other role modification methods as it does not require an updated cache!</b>
     *
     * <p>If multiple roles should be added/removed (efficiently) in one request
     * you may use {@link #modifyMemberRoles(Member, Collection, Collection) modifyMemberRoles(Member, Collection, Collection)} or similar methods.
     *
     * <p>If the specified role is already present in the member's set of roles this does nothing.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_ROLE UNKNOWN_ROLE}
     *     <br>If the specified Role does not exist</li>
     * </ul>
     *
     * @param  userId
     *         The id of the target member who will receive the new role
     * @param  role
     *         The role which should be assigned atomically
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the specified role is not from the current Guild</li>
     *             <li>If the role is {@code null}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> addRoleToMember(long userId, @Nonnull Role role)
    {
        Checks.notNull(role, "Role");
        Checks.check(role.getGuild().equals(this), "Role must be from the same guild! Trying to use role from %s in %s", role.getGuild().toString(), toString());

        Member member = getMemberById(userId);
        if (member != null)
            return addRoleToMember(member, role);
        if (!getSelfMember().hasPermission(Permission.MANAGE_ROLES))
            throw new InsufficientPermissionException(this, Permission.MANAGE_ROLES);
        if (!getSelfMember().canInteract(role))
            throw new HierarchyException("Can't modify a role with higher or equal highest role than yourself! Role: " + role.toString());
        Route.CompiledRoute route = Route.Guilds.ADD_MEMBER_ROLE.compile(getId(), Long.toUnsignedString(userId), role.getId());
        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    /**
     * Atomically assigns the provided {@link net.dv8tion.jda.api.entities.Role Role} to the specified member by their user id.
     * <br><b>This can be used together with other role modification methods as it does not require an updated cache!</b>
     *
     * <p>If multiple roles should be added/removed (efficiently) in one request
     * you may use {@link #modifyMemberRoles(Member, Collection, Collection) modifyMemberRoles(Member, Collection, Collection)} or similar methods.
     *
     * <p>If the specified role is already present in the member's set of roles this does nothing.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_ROLE UNKNOWN_ROLE}
     *     <br>If the specified Role does not exist</li>
     * </ul>
     *
     * @param  userId
     *         The id of the target member who will receive the new role
     * @param  role
     *         The role which should be assigned atomically
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the specified role is not from the current Guild</li>
     *             <li>If the role is {@code null}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> addRoleToMember(@Nonnull String userId, @Nonnull Role role)
    {
        return addRoleToMember(MiscUtil.parseSnowflake(userId), role);
    }

    /**
     * Atomically removes the provided {@link net.dv8tion.jda.api.entities.Role Role} from the specified {@link net.dv8tion.jda.api.entities.Member Member}.
     * <br><b>This can be used together with other role modification methods as it does not require an updated cache!</b>
     *
     * <p>If multiple roles should be added/removed (efficiently) in one request
     * you may use {@link #modifyMemberRoles(Member, Collection, Collection) modifyMemberRoles(Member, Collection, Collection)} or similar methods.
     *
     * <p>If the specified role is not present in the member's set of roles this does nothing.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_ROLE UNKNOWN_ROLE}
     *     <br>If the specified Role does not exist</li>
     * </ul>
     *
     * @param  member
     *         The target member who will lose the specified role
     * @param  role
     *         The role which should be removed atomically
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the specified member/role are not from the current Guild</li>
     *             <li>Either member or role are {@code null}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> removeRoleFromMember(@Nonnull Member member, @Nonnull Role role);

    /**
     * Atomically removes the provided {@link net.dv8tion.jda.api.entities.Role Role} from the specified member by their user id.
     * <br><b>This can be used together with other role modification methods as it does not require an updated cache!</b>
     *
     * <p>If multiple roles should be added/removed (efficiently) in one request
     * you may use {@link #modifyMemberRoles(Member, Collection, Collection) modifyMemberRoles(Member, Collection, Collection)} or similar methods.
     *
     * <p>If the specified role is not present in the member's set of roles this does nothing.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_ROLE UNKNOWN_ROLE}
     *     <br>If the specified Role does not exist</li>
     * </ul>
     *
     * @param  userId
     *         The id of the target member who will lose the specified role
     * @param  role
     *         The role which should be removed atomically
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the specified role is not from the current Guild</li>
     *             <li>The role is {@code null}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> removeRoleFromMember(long userId, @Nonnull Role role)
    {
        Checks.notNull(role, "Role");
        Checks.check(role.getGuild().equals(this), "Role must be from the same guild! Trying to use role from %s in %s", role.getGuild().toString(), toString());

        Member member = getMemberById(userId);
        if (member != null)
            return removeRoleFromMember(member, role);
        if (!getSelfMember().hasPermission(Permission.MANAGE_ROLES))
            throw new InsufficientPermissionException(this, Permission.MANAGE_ROLES);
        if (!getSelfMember().canInteract(role))
            throw new HierarchyException("Can't modify a role with higher or equal highest role than yourself! Role: " + role.toString());
        Route.CompiledRoute route = Route.Guilds.REMOVE_MEMBER_ROLE.compile(getId(), Long.toUnsignedString(userId), role.getId());
        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    /**
     * Atomically removes the provided {@link net.dv8tion.jda.api.entities.Role Role} from the specified member by their user id.
     * <br><b>This can be used together with other role modification methods as it does not require an updated cache!</b>
     *
     * <p>If multiple roles should be added/removed (efficiently) in one request
     * you may use {@link #modifyMemberRoles(Member, Collection, Collection) modifyMemberRoles(Member, Collection, Collection)} or similar methods.
     *
     * <p>If the specified role is not present in the member's set of roles this does nothing.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_ROLE UNKNOWN_ROLE}
     *     <br>If the specified Role does not exist</li>
     * </ul>
     *
     * @param  userId
     *         The id of the target member who will lose the specified role
     * @param  role
     *         The role which should be removed atomically
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the specified role is not from the current Guild</li>
     *             <li>The role is {@code null}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> removeRoleFromMember(@Nonnull String userId, @Nonnull Role role)
    {
        return removeRoleFromMember(MiscUtil.parseSnowflake(userId), role);
    }

    /**
     * Modifies the {@link net.dv8tion.jda.api.entities.Role Roles} of the specified {@link net.dv8tion.jda.api.entities.Member Member}
     * by adding and removing a collection of roles.
     * <br>None of the provided roles may be the <u>Public Role</u> of the current Guild.
     * <br>If a role is both in {@code rolesToAdd} and {@code rolesToRemove} it will be removed.
     *
     * <h4>Example</h4>
     * <pre>{@code
     * public static void promote(Member member) {
     *     Guild guild = member.getGuild();
     *     List<Role> pleb = guild.getRolesByName("Pleb", true); // remove all roles named "pleb"
     *     List<Role> knight = guild.getRolesByName("Knight", true); // add all roles named "knight"
     *     // update roles in single request
     *     guild.modifyMemberRoles(member, knight, pleb).queue();
     * }
     * }</pre>
     *
     * <h4>Warning</h4>
     * <b>This may <u>not</u> be used together with any other role add/remove/modify methods for the same Member
     * within one event listener cycle! The changes made by this require cache updates which are triggered by
     * lifecycle events which are received later. This may only be called again once the specific Member has been updated
     * by a {@link net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent GenericGuildMemberEvent} targeting the same Member.</b>
     *
     * <p>This is logically equivalent to:
     * <pre>{@code
     * Set<Role> roles = new HashSet<>(member.getRoles());
     * roles.addAll(rolesToAdd);
     * roles.removeAll(rolesToRemove);
     * RestAction<Void> action = guild.modifyMemberRoles(member, roles);
     * }</pre>
     *
     * <p>You can use {@link #addRoleToMember(Member, Role)} and {@link #removeRoleFromMember(Member, Role)} to make updates
     * independent of the cache.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} that should be modified
     * @param  rolesToAdd
     *         A {@link java.util.Collection Collection} of {@link net.dv8tion.jda.api.entities.Role Roles}
     *         to add to the current Roles the specified {@link net.dv8tion.jda.api.entities.Member Member} already has, or null
     * @param  rolesToRemove
     *         A {@link java.util.Collection Collection} of {@link net.dv8tion.jda.api.entities.Role Roles}
     *         to remove from the current Roles the specified {@link net.dv8tion.jda.api.entities.Member Member} already has, or null
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the target member is {@code null}</li>
     *             <li>If any of the specified Roles is managed or is the {@code Public Role} of the Guild</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> modifyMemberRoles(@Nonnull Member member, @Nullable Collection<Role> rolesToAdd, @Nullable Collection<Role> rolesToRemove);

    /**
     * Modifies the complete {@link net.dv8tion.jda.api.entities.Role Role} set of the specified {@link net.dv8tion.jda.api.entities.Member Member}
     * <br>The provided roles will replace all current Roles of the specified Member.
     *
     * <h4>Warning</h4>
     * <b>This may <u>not</u> be used together with any other role add/remove/modify methods for the same Member
     * within one event listener cycle! The changes made by this require cache updates which are triggered by
     * lifecycle events which are received later. This may only be called again once the specific Member has been updated
     * by a {@link net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent GenericGuildMemberEvent} targeting the same Member.</b>
     *
     * <p><b>The new roles <u>must not</u> contain the Public Role of the Guild</b>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * <h4>Example</h4>
     * <pre>{@code
     * public static void removeRoles(Member member) {
     *     Guild guild = member.getGuild();
     *     // pass no role, this means we set the roles of the member to an empty array.
     *     guild.modifyMemberRoles(member).queue();
     * }
     * }</pre>
     *
     * @param  member
     *         A {@link net.dv8tion.jda.api.entities.Member Member} of which to override the Roles of
     * @param  roles
     *         New collection of {@link net.dv8tion.jda.api.entities.Role Roles} for the specified Member
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the provided arguments is {@code null}</li>
     *             <li>If any of the provided arguments is not from this Guild</li>
     *             <li>If any of the specified {@link net.dv8tion.jda.api.entities.Role Roles} is managed</li>
     *             <li>If any of the specified {@link net.dv8tion.jda.api.entities.Role Roles} is the {@code Public Role} of this Guild</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @see    #modifyMemberRoles(Member, Collection)
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> modifyMemberRoles(@Nonnull Member member, @Nonnull Role... roles)
    {
        return modifyMemberRoles(member, Arrays.asList(roles));
    }

    /**
     * Modifies the complete {@link net.dv8tion.jda.api.entities.Role Role} set of the specified {@link net.dv8tion.jda.api.entities.Member Member}
     * <br>The provided roles will replace all current Roles of the specified Member.
     *
     * <p><u>The new roles <b>must not</b> contain the Public Role of the Guild</u>
     *
     * <h4>Warning</h4>
     * <b>This may <u>not</u> be used together with any other role add/remove/modify methods for the same Member
     * within one event listener cycle! The changes made by this require cache updates which are triggered by
     * lifecycle events which are received later. This may only be called again once the specific Member has been updated
     * by a {@link net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent GenericGuildMemberEvent} targeting the same Member.</b>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * <h4>Example</h4>
     * <pre>{@code
     * public static void makeModerator(Member member) {
     *     Guild guild = member.getGuild();
     *     List<Role> roles = new ArrayList<>(member.getRoles()); // modifiable copy
     *     List<Role> modRoles = guild.getRolesByName("moderator", true); // get roles with name "moderator"
     *     roles.addAll(modRoles); // add new roles
     *     // update the member with new roles
     *     guild.modifyMemberRoles(member, roles).queue();
     * }
     * }</pre>
     *
     * @param  member
     *         A {@link net.dv8tion.jda.api.entities.Member Member} of which to override the Roles of
     * @param  roles
     *         New collection of {@link net.dv8tion.jda.api.entities.Role Roles} for the specified Member
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the provided arguments is {@code null}</li>
     *             <li>If any of the provided arguments is not from this Guild</li>
     *             <li>If any of the specified {@link net.dv8tion.jda.api.entities.Role Roles} is managed</li>
     *             <li>If any of the specified {@link net.dv8tion.jda.api.entities.Role Roles} is the {@code Public Role} of this Guild</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @see    #modifyMemberRoles(Member, Collection)
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> modifyMemberRoles(@Nonnull Member member, @Nonnull Collection<Role> roles);

    /**
     * Transfers the Guild ownership to the specified {@link net.dv8tion.jda.api.entities.Member Member}
     * <br>Only available if the currently logged in account is the owner of this Guild
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The currently logged in account lost ownership before completing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  newOwner
     *         Not-null Member to transfer ownership to
     *
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     *         If the currently logged in account is not the owner of this Guild
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the specified Member is {@code null} or not from the same Guild</li>
     *             <li>If the specified Member already is the Guild owner</li>
     *             <li>If the specified Member is a bot account ({@link net.dv8tion.jda.api.AccountType#BOT AccountType.BOT})</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> transferOwnership(@Nonnull Member newOwner);

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     * </ul>
     *
     * @param  name
     *         The name of the TextChannel to create
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL} permission
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or empty or greater than 100 characters in length
     *
     * @return A specific {@link net.dv8tion.jda.api.requests.restaction.ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new TextChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    default ChannelAction<TextChannel> createTextChannel(@Nonnull String name)
    {
        return createTextChannel(name, null);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     * </ul>
     *
     * @param  name
     *         The name of the TextChannel to create
     * @param  parent
     *         The optional parent category for this channel, or null
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL} permission
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or empty or greater than 100 characters in length;
     *         or the provided parent is not in the same guild.
     *
     * @return A specific {@link net.dv8tion.jda.api.requests.restaction.ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new TextChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    ChannelAction<TextChannel> createTextChannel(@Nonnull String name, @Nullable Category parent);

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.NewsChannel NewsChannel} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     * </ul>
     *
     * @param  name
     *         The name of the NewsChannel to create
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL} permission
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or empty or greater than 100 characters in length
     *
     * @return A specific {@link net.dv8tion.jda.api.requests.restaction.ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new NewsChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    default ChannelAction<NewsChannel> createNewsChannel(@Nonnull String name)
    {
        return createNewsChannel(name, null);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.NewsChannel NewsChannel} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     * </ul>
     *
     * @param  name
     *         The name of the NewsChannel to create
     * @param  parent
     *         The optional parent category for this channel, or null
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL} permission
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or empty or greater than 100 characters in length;
     *         or the provided parent is not in the same guild.
     *
     * @return A specific {@link net.dv8tion.jda.api.requests.restaction.ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new NewsChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    ChannelAction<NewsChannel> createNewsChannel(@Nonnull String name, @Nullable Category parent);

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     * </ul>
     *
     * @param  name
     *         The name of the VoiceChannel to create
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL} permission
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or empty or greater than 100 characters in length
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new VoiceChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    default ChannelAction<VoiceChannel> createVoiceChannel(@Nonnull String name)
    {
        return createVoiceChannel(name, null);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     * </ul>
     *
     * @param  name
     *         The name of the VoiceChannel to create
     * @param  parent
     *         The optional parent category for this channel, or null
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL} permission
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or empty or greater than 100 characters in length;
     *         or the provided parent is not in the same guild.
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new VoiceChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    ChannelAction<VoiceChannel> createVoiceChannel(@Nonnull String name, @Nullable Category parent);

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.StageChannel StageChannel} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     * </ul>
     *
     * @param  name
     *         The name of the StageChannel to create
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL} permission
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or empty or greater than 100 characters in length
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new StageChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    default ChannelAction<StageChannel> createStageChannel(@Nonnull String name)
    {
        return createStageChannel(name, null);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.StageChannel StageChannel} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     * </ul>
     *
     * @param  name
     *         The name of the StageChannel to create
     * @param  parent
     *         The optional parent category for this channel, or null
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL} permission
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or empty or greater than 100 characters in length;
     *         or the provided parent is not in the same guild.
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new StageChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    ChannelAction<StageChannel> createStageChannel(@Nonnull String name, @Nullable Category parent);

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.Category Category} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     * </ul>
     *
     * @param  name
     *         The name of the Category to create
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL} permission
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or empty or greater than 100 characters in length
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new Category before creating it
     */
    @Nonnull
    @CheckReturnValue
    ChannelAction<Category> createCategory(@Nonnull String name);

    /**
     * Creates a copy of the specified {@link GuildChannel GuildChannel}
     * in this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <br>The provided channel need not be in the same Guild for this to work!
     *
     * <p>This copies the following elements:
     * <ol>
     *     <li>Name</li>
     *     <li>Parent Category (if present)</li>
     *     <li>Voice Elements (Bitrate, Userlimit)</li>
     *     <li>Text Elements (Topic, NSFW)</li>
     *     <li>All permission overrides for Members/Roles</li>
     * </ol>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     * </ul>
     *
     * @param  <T>
     *         The channel type
     * @param  channel
     *         The {@link GuildChannel GuildChannel} to use for the copy template
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided channel is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new GuildChannel before creating it!
     *
     * @since  3.1
     *
     * @see    #createTextChannel(String)
     * @see    #createVoiceChannel(String)
     * @see    ChannelAction ChannelAction
     */
    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("unchecked") // we need to do an unchecked cast for the channel type here
    default <T extends ICopyableChannel> ChannelAction<T> createCopyOfChannel(@Nonnull T channel)
    {
        Checks.notNull(channel, "Channel");
        return (ChannelAction<T>) channel.createCopy(this);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.Role Role} in this Guild.
     * <br>It will be placed at the bottom (just over the Public Role) to avoid permission hierarchy conflicts.
     * <br>For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES MANAGE_ROLES} Permission
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The role could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_ROLES_PER_GUILD MAX_ROLES_PER_GUILD}
     *     <br>There are too many roles in this Guild</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES} Permission
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.RoleAction RoleAction}
     *         <br>Creates a new role with previously selected field values
     */
    @Nonnull
    @CheckReturnValue
    RoleAction createRole();

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.Role Role} in this {@link net.dv8tion.jda.api.entities.Guild Guild}
     * with the same settings as the given {@link net.dv8tion.jda.api.entities.Role Role}.
     * <br>The position of the specified Role does not matter in this case!
     *
     * <p>It will be placed at the bottom (just over the Public Role) to avoid permission hierarchy conflicts.
     * <br>For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES MANAGE_ROLES} Permission
     * and all {@link net.dv8tion.jda.api.Permission Permissions} the given {@link net.dv8tion.jda.api.entities.Role Role} has.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The role could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_ROLES_PER_GUILD MAX_ROLES_PER_GUILD}
     *     <br>There are too many roles in this Guild</li>
     * </ul>
     *
     * @param  role
     *         The {@link net.dv8tion.jda.api.entities.Role Role} that should be copied
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES} Permission and every Permission the provided Role has
     * @throws java.lang.IllegalArgumentException
     *         If the specified role is {@code null}
     *
     * @return {@link RoleAction RoleAction}
     *         <br>RoleAction with already copied values from the specified {@link net.dv8tion.jda.api.entities.Role Role}
     */
    @Nonnull
    @CheckReturnValue
    default RoleAction createCopyOfRole(@Nonnull Role role)
    {
        Checks.notNull(role, "Role");
        return role.createCopy(this);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.Emote Emote} in this Guild.
     * <br>If one or more Roles are specified the new Emote will only be available to Members with any of the specified Roles (see {@link Member#canInteract(Emote)})
     * <br>For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_EMOTES_AND_STICKERS MANAGE_EMOTES_AND_STICKERS} Permission.
     *
     * <p><b><u>Unicode emojis are not included as {@link net.dv8tion.jda.api.entities.Emote Emote}!</u></b>
     *
     * <p>Note that a guild is limited to 50 normal and 50 animated emotes by default.
     * Some guilds are able to add additional emotes beyond this limitation due to the
     * {@code MORE_EMOJI} feature (see {@link net.dv8tion.jda.api.entities.Guild#getFeatures() Guild.getFeatures()}).
     * <br>Due to simplicity we do not check for these limits.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The emote could not be created due to a permission discrepancy</li>
     * </ul>
     *
     * @param  name
     *         The name for the new Emote
     * @param  icon
     *         The {@link net.dv8tion.jda.api.entities.Icon} for the new Emote
     * @param  roles
     *         The {@link net.dv8tion.jda.api.entities.Role Roles} the new Emote should be restricted to
     *         <br>If no roles are provided the Emote will be available to all Members of this Guild
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_EMOTES_AND_STICKERS MANAGE_EMOTES_AND_STICKERS} Permission
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction} - Type: {@link net.dv8tion.jda.api.entities.Emote Emote}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Emote> createEmote(@Nonnull String name, @Nonnull Icon icon, @Nonnull Role... roles);


    /**
     * Creates a new {@link GuildScheduledEvent}.
     *
     * <h2>Requirements</h2>
     *
     * Events that are created are required to have a name, a location, and a start time. Depending on the
     * type of location provided, an event will be of one of three different {@link GuildScheduledEvent.Type Types}:
     * <ol>
     *     <li>
     *         {@link GuildScheduledEvent.Type#STAGE_INSTANCE Type.STAGE_INSTANCE}
     *         <br>These events are set to take place inside of a {@link net.dv8tion.jda.api.entities.StageChannel}. The
     *         following permissions are required in the specified stage channel in order to create an event there:
     *          <ul>
     *              <li>{@link net.dv8tion.jda.api.Permission#MANAGE_EVENTS Permission.MANAGE_EVENTS}</li>
     *              <li>{@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL}</li>
     *              <li>{@link net.dv8tion.jda.api.Permission#VOICE_MUTE_OTHERS Permission.VOICE_MUTE_OTHERS}</li>
     *              <li>{@link net.dv8tion.jda.api.Permission#VOICE_MOVE_OTHERS Permission.VOICE_MOVE_OTHERS}}</li>
     *         </ul>
     *     </li>
     *     <li>
     *         {@link GuildScheduledEvent.Type#VOICE Type.VOICE}
     *         <br>These events are set to take place inside of a {@link net.dv8tion.jda.api.entities.VoiceChannel}. The
     *         following permissions are required in the specified voice channel in order to create an event there:
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#MANAGE_EVENTS Permission.MANAGE_EVENTS}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#VOICE_CONNECT Permission.VOICE_CONNECT}</li>
     *         </ul>
     *     </li>
     *     <li>
     *         {@link GuildScheduledEvent.Type#EXTERNAL Type.EXTERNAL}
     *         <br>These events are set to take place at a custom location. {@link net.dv8tion.jda.api.Permission#MANAGE_EVENTS Permission.MANAGE_EVENTS}
     *         is required on the guild level in order to create this type of event. Additionally, an end time <em>must</em>
     *         also be specified.
     *     </li>
     * </ol>
     *
     * @throws java.lang.IllegalArgumentException
     *         If a required parameter is not set.
     *
     *
     * <h2>Example</h2>
     * <pre>{@code
     * guild.createScheduledEvent()
     *     .setName("Cactus Beauty Contest")
     *     .setDescription("Come and have your cacti judged! _Must be spikey to enter_")
     *     .setStartTime(OffsetDateTime.of(LocalDate.of(2025, 12, 31), LocalTime.of(12, 45), ZoneOffset.ofHours(7)))
     *     .setEndTime(OffsetDateTime.of(LocalDate.of(2025, 12, 31), LocalTime.of(15, 45), ZoneOffset.ofHours(7)))
     *     .setLocation("Mike's Backyard")
     *     .queue();
     * }</pre>
     *
     * @return A GuildScheduledEventAction object
     */
    @Nonnull
    @CheckReturnValue
    GuildScheduledEventAction createScheduledEvent();

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.api.entities.Guild#getCategories() Guild.getCategories()}
     * using a specific {@link net.dv8tion.jda.api.requests.RestAction RestAction} extension to allow moving Channels
     * {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveUp(int) up}/{@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveDown(int) down}
     * or {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     * <br>This uses <b>ascending</b> order with a 0 based index.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNNKOWN_CHANNEL}
     *     <br>One of the channels has been deleted before the completion of the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild</li>
     * </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.order.ChannelOrderAction ChannelOrderAction} - Type: {@link net.dv8tion.jda.api.entities.Category Category}
     */
    @Nonnull
    @CheckReturnValue
    ChannelOrderAction modifyCategoryPositions();

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.api.entities.Guild#getTextChannels() Guild.getTextChannels()}
     * using a specific {@link net.dv8tion.jda.api.requests.RestAction RestAction} extension to allow moving Channels
     * {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveUp(int) up}/{@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveDown(int) down}
     * or {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     * <br>This uses <b>ascending</b> order with a 0 based index.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNNKOWN_CHANNEL}
     *     <br>One of the channels has been deleted before the completion of the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild</li>
     * </ul>
     *
     * @return {@link ChannelOrderAction ChannelOrderAction} - Type: {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     */
    @Nonnull
    @CheckReturnValue
    ChannelOrderAction modifyTextChannelPositions();

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.api.entities.Guild#getVoiceChannels() Guild.getVoiceChannels()}
     * using a specific {@link net.dv8tion.jda.api.requests.RestAction RestAction} extension to allow moving Channels
     * {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveUp(int) up}/{@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveDown(int) down}
     * or {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     * <br>This uses <b>ascending</b> order with a 0 based index.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNNKOWN_CHANNEL}
     *     <br>One of the channels has been deleted before the completion of the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild</li>
     * </ul>
     *
     * @return {@link ChannelOrderAction ChannelOrderAction} - Type: {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}
     */
    @Nonnull
    @CheckReturnValue
    ChannelOrderAction modifyVoiceChannelPositions();

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.api.entities.Category#getTextChannels() Category#getTextChannels()}
     * using an extension of {@link ChannelOrderAction ChannelOrderAction}
     * specialized for ordering the nested {@link net.dv8tion.jda.api.entities.TextChannel TextChannels} of this
     * {@link net.dv8tion.jda.api.entities.Category Category}.
     * <br>Like {@code ChannelOrderAction}, the returned {@link net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction CategoryOrderAction}
     * can be used to move TextChannels {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveUp(int) up},
     * {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveDown(int) down}, or
     * {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     * <br>This uses <b>ascending</b> order with a 0 based index.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNNKOWN_CHANNEL}
     *     <br>One of the channels has been deleted before the completion of the task.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild.</li>
     * </ul>
     *
     * @param  category
     *         The {@link net.dv8tion.jda.api.entities.Category Category} to order
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannels} from.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction CategoryOrderAction} - Type: {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     */
    @Nonnull
    @CheckReturnValue
    CategoryOrderAction modifyTextChannelPositions(@Nonnull Category category);

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.api.entities.Category#getVoiceChannels() Category#getVoiceChannels()}
     * using an extension of {@link ChannelOrderAction ChannelOrderAction}
     * specialized for ordering the nested {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels} of this
     * {@link net.dv8tion.jda.api.entities.Category Category}.
     * <br>Like {@code ChannelOrderAction}, the returned {@link CategoryOrderAction CategoryOrderAction}
     * can be used to move VoiceChannels {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveUp(int) up},
     * {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveDown(int) down}, or
     * {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     * <br>This uses <b>ascending</b> order with a 0 based index.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNNKOWN_CHANNEL}
     *     <br>One of the channels has been deleted before the completion of the task.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild.</li>
     * </ul>
     *
     * @param  category
     *         The {@link net.dv8tion.jda.api.entities.Category Category} to order
     *         {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels} from.
     *
     * @return {@link CategoryOrderAction CategoryOrderAction} - Type: {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels}
     */
    @Nonnull
    @CheckReturnValue
    CategoryOrderAction modifyVoiceChannelPositions(@Nonnull Category category);

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.api.entities.Guild#getRoles() Guild.getRoles()}
     * using a specific {@link net.dv8tion.jda.api.requests.RestAction RestAction} extension to allow moving Roles
     * {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveUp(int) up}/{@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveDown(int) down}
     * or {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     *
     * <p>This uses <b>ascending</b> ordering which means the lowest role is first!
     * <br>This means the highest role appears at index {@code n - 1} and the lower role at index {@code 0}.
     * <br>Providing {@code true} to {@link #modifyRolePositions(boolean)} will result in the ordering being
     * in ascending order, with the lower role at index {@code n - 1} and the highest at index {@code 0}.
     * <br>As a note: {@link net.dv8tion.jda.api.entities.Member#getRoles() Member.getRoles()}
     * and {@link net.dv8tion.jda.api.entities.Guild#getRoles() Guild.getRoles()} are both in descending order.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_ROLE UNKNOWN_ROLE}
     *     <br>One of the roles was deleted before the completion of the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild</li>
     * </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction RoleOrderAction}
     */
    @Nonnull
    @CheckReturnValue
    default RoleOrderAction modifyRolePositions()
    {
        return modifyRolePositions(true);
    }

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.api.entities.Guild#getRoles() Guild.getRoles()}
     * using a specific {@link net.dv8tion.jda.api.requests.RestAction RestAction} extension to allow moving Roles
     * {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveUp(int) up}/{@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveDown(int) down}
     * or {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_ROLE UNKNOWN_ROLE}
     *     <br>One of the roles was deleted before the completion of the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild</li>
     * </ul>
     *
     * @param  useAscendingOrder
     *         Defines the ordering of the OrderAction. If {@code false}, the OrderAction will be in the ordering
     *         defined by Discord for roles, which is Descending. This means that the highest role appears at index {@code 0}
     *         and the lowest role at index {@code n - 1}. Providing {@code true} will result in the ordering being
     *         in ascending order, with the lower role at index {@code 0} and the highest at index {@code n - 1}.
     *         <br>As a note: {@link net.dv8tion.jda.api.entities.Member#getRoles() Member.getRoles()}
     *         and {@link net.dv8tion.jda.api.entities.Guild#getRoles() Guild.getRoles()} are both in descending order.
     *
     * @return {@link RoleOrderAction RoleOrderAction}
     */
    @Nonnull
    @CheckReturnValue
    RoleOrderAction modifyRolePositions(boolean useAscendingOrder);

    //////////////////////////

    /**
     * Represents the idle time allowed until a user is moved to the
     * AFK {@link net.dv8tion.jda.api.entities.VoiceChannel} if one is set
     * ({@link net.dv8tion.jda.api.entities.Guild#getAfkChannel() Guild.getAfkChannel()}).
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
         * Retrieves the {@link net.dv8tion.jda.api.entities.Guild.Timeout Timeout} based on the amount of seconds requested.
         * <br>If the {@code seconds} amount provided is not valid for Discord, an IllegalArgumentException will be thrown.
         *
         * @param  seconds
         *         The amount of seconds before idle timeout.
         *
         * @throws java.lang.IllegalArgumentException
         *         If the provided {@code seconds} is an invalid timeout amount.
         *
         * @return The {@link net.dv8tion.jda.api.entities.Guild.Timeout Timeout} related to the amount of seconds provided.
         */
        @Nonnull
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
     * <br><b>None</b>      {@literal ->} everyone can talk.
     * <br><b>Low</b>       {@literal ->} verified email required.
     * <br><b>Medium</b>    {@literal ->} you have to be member of discord for at least 5min.
     * <br><b>High</b>      {@literal ->} you have to be member of this guild for at least 10min.
     * <br><b>Very High</b> {@literal ->} you must have a verified phone on your discord account.
     */
    enum VerificationLevel
    {
        NONE(0),
        LOW(1),
        MEDIUM(2),
        HIGH(3),
        VERY_HIGH(4),
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
         * Used to retrieve a {@link net.dv8tion.jda.api.entities.Guild.VerificationLevel VerificationLevel} based
         * on the Discord id key.
         *
         * @param  key
         *         The Discord id key representing the requested VerificationLevel.
         *
         * @return The VerificationLevel related to the provided key, or {@link #UNKNOWN VerificationLevel.UNKNOWN} if the key is not recognized.
         */
        @Nonnull
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
         * Used to retrieve a {@link net.dv8tion.jda.api.entities.Guild.NotificationLevel NotificationLevel} based
         * on the Discord id key.
         *
         * @param  key
         *         The Discord id key representing the requested NotificationLevel.
         *
         * @return The NotificationLevel related to the provided key, or {@link #UNKNOWN NotificationLevel.UNKNOWN} if the key is not recognized.
         */
        @Nonnull
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
         * Used to retrieve a {@link net.dv8tion.jda.api.entities.Guild.MFALevel MFALevel} based
         * on the Discord id key.
         *
         * @param  key
         *         The Discord id key representing the requested MFALevel.
         *
         * @return The MFALevel related to the provided key, or {@link #UNKNOWN MFALevel.UNKNOWN} if the key is not recognized.
         */
        @Nonnull
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

    /**
     * The Explicit-Content-Filter Level of a Guild.
     * <br>This decides whom's messages should be scanned for explicit content.
     */
    enum ExplicitContentLevel
    {
        OFF(0, "Don't scan any messages."),
        NO_ROLE(1, "Scan messages from members without a role."),
        ALL(2, "Scan messages sent by all members."),

        UNKNOWN(-1, "Unknown filter level!");

        private final int key;
        private final String description;

        ExplicitContentLevel(int key, String description)
        {
            this.key = key;
            this.description = description;
        }

        /**
         * The key for this level
         *
         * @return key
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Description of this level in the official Discord Client (as of 5th May, 2017)
         *
         * @return Description for this level
         */
        @Nonnull
        public String getDescription()
        {
            return description;
        }

        @Nonnull
        public static ExplicitContentLevel fromKey(int key)
        {
            for (ExplicitContentLevel level : values())
            {
                if (level.key == key)
                    return level;
            }
            return UNKNOWN;
        }
    }

    /**
     * Represents the NSFW level for this guild.
     */
    enum NSFWLevel
    {
        /**
         * Discord has not rated this guild.
         */
        DEFAULT(0),
        /**
         * Is classified as a NSFW server
         */
        EXPLICIT(1),
        /**
         * Doesn't classify as a NSFW server
         */
        SAFE(2),
        /**
         * Is classified as NSFW and has an age restriction in place
         */
        AGE_RESTRICTED(3),
        /**
         * Placeholder for unsupported levels.
         */
        UNKNOWN(-1);

        private final int key;

        NSFWLevel(int key)
        {
            this.key = key;
        }

        /**
         * The Discord id key used to represent this NSFW level.
         *
         * @return Integer id for this NSFW level.
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Used to retrieve a {@link net.dv8tion.jda.api.entities.Guild.NSFWLevel NSFWLevel} based
         * on the Discord id key.
         *
         * @param  key
         *         The Discord id key representing the requested NSFWLevel.
         *
         * @return The NSFWLevel related to the provided key, or {@link #UNKNOWN NSFWLevel.UNKNOWN} if the key is not recognized.
         */
        @Nonnull
        public static NSFWLevel fromKey(int key)
        {
            for (NSFWLevel level : values())
            {
                if (level.getKey() == key)
                    return level;
            }
            return UNKNOWN;
        }
    }

    /**
     * The boost tier for this guild.
     * <br>Each tier unlocks new perks for a guild that can be seen in the {@link #getFeatures() features}.
     *
     * @since  4.0.0
     */
    enum BoostTier
    {
        /**
         * The default tier.
         * <br>Unlocked at 0 boosters.
         */
        NONE(0, 96000, 50),
        /**
         * The first tier.
         * <br>Unlocked at 2 boosters.
         */
        TIER_1(1, 128000, 100),
        /**
         * The second tier.
         * <br>Unlocked at 7 boosters.
         */
        TIER_2(2, 256000, 150),
        /**
         * The third tier.
         * <br>Unlocked at 14 boosters.
         */
        TIER_3(3, 384000, 250),
        /**
         * Placeholder for future tiers.
         */
        UNKNOWN(-1, Integer.MAX_VALUE, Integer.MAX_VALUE);

        private final int key;
        private final int maxBitrate;
        private final int maxEmotes;

        BoostTier(int key, int maxBitrate, int maxEmotes)
        {
            this.key = key;
            this.maxBitrate = maxBitrate;
            this.maxEmotes = maxEmotes;
        }

        /**
         * The API key used to represent this tier, identical to the ordinal.
         *
         * @return The key
         */
        public int getKey()
        {
            return key;
        }

        /**
         * The maximum bitrate that can be applied to voice channels when this tier is reached.
         *
         * @return The maximum bitrate
         *
         * @see    net.dv8tion.jda.api.entities.Guild#getMaxBitrate()
         */
        public int getMaxBitrate()
        {
            return maxBitrate;
        }

        /**
         * The maximum amount of emotes a guild can have when this tier is reached.
         *
         * @return The maximum emotes
         *
         * @see    net.dv8tion.jda.api.entities.Guild#getMaxEmotes()
         */
        public int getMaxEmotes()
        {
            return maxEmotes;
        }

        /**
         * The maximum size for files that can be uploaded to this Guild.
         *
         * @return The maximum file size of this Guild
         *
         * @see    net.dv8tion.jda.api.entities.Guild#getMaxFileSize()
         */
        public long getMaxFileSize()
        {
            if (key == 2)
                return 50 << 20;
            else if (key == 3)
                return 100 << 20;
            return Message.MAX_FILE_SIZE;
        }

        /**
         * Resolves the provided API key to the boost tier.
         *
         * @param  key
         *         The API key
         *
         * @return The BoostTier or {@link #UNKNOWN}
         */
        @Nonnull
        public static BoostTier fromKey(int key)
        {
            for (BoostTier tier : values())
            {
                if (tier.key == key)
                    return tier;
            }
            return UNKNOWN;
        }
    }

    /**
     * Represents a Ban object.
     *
     * @see #retrieveBanList()
     * @see <a href="https://discord.com/developers/docs/resources/guild#ban-object" target="_blank">Discord Docs: Ban Object</a>
     */
    class Ban
    {
        protected final User user;
        protected final String reason;

        public Ban(User user, String reason)
        {
            this.user = user;
            this.reason = reason;
        }

        /**
         * The {@link net.dv8tion.jda.api.entities.User User} that was banned
         *
         * @return The banned User
         */
        @Nonnull
        public User getUser()
        {
            return user;
        }

        /**
         * The reason why this user was banned
         *
         * @return The reason for this ban, or {@code null}
         */
        @Nullable
        public String getReason()
        {
            return reason;
        }

        @Override
        public String toString()
        {
            return "GuildBan:" + user + (reason == null ? "" : '(' + reason + ')');
        }
    }

    /**
     * Meta-Data for a Guild
     *
     * @since 4.2.0
     */
    class MetaData
    {
        private final int memberLimit;
        private final int presenceLimit;
        private final int approximatePresences;
        private final int approximateMembers;

        public MetaData(int memberLimit, int presenceLimit, int approximatePresences, int approximateMembers)
        {
            this.memberLimit = memberLimit;
            this.presenceLimit = presenceLimit;
            this.approximatePresences = approximatePresences;
            this.approximateMembers = approximateMembers;
        }

        /**
         * The active member limit for this guild.
         * <br>This limit restricts how many users can be member for this guild at once.
         *
         * @return The member limit
         */
        public int getMemberLimit()
        {
            return memberLimit;
        }

        /**
         * The active presence limit for this guild.
         * <br>This limit restricts how many users can be connected/online for this guild at once.
         *
         * @return The presence limit
         */
        public int getPresenceLimit()
        {
            return presenceLimit;
        }

        /**
         * The approximate number of online members in this guild.
         *
         * @return The approximate presence count
         */
        public int getApproximatePresences()
        {
            return approximatePresences;
        }

        /**
         * The approximate number of members in this guild.
         *
         * @return The approximate member count
         */
        public int getApproximateMembers()
        {
            return approximateMembers;
        }
    }
}
