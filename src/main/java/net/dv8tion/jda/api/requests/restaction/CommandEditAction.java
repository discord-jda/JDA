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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Specialized {@link RestAction} used to edit an existing command.
 */
public interface CommandEditAction extends RestAction<Command>
{
    @Nonnull
    @Override
    @CheckReturnValue
    CommandEditAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    @CheckReturnValue
    CommandEditAction addCheck(@Nonnull BooleanSupplier checks);

    @Nonnull
    @Override
    @CheckReturnValue
    CommandEditAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    @CheckReturnValue
    CommandEditAction deadline(long timestamp);

    /**
     * Replace the command with the provided {@link CommandData}.
     *
     * @param  commandData
     *         The data for the command
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The CommandEditAction instance, for chaining
     *
     * @see    Commands
     * @see    CommandCreateAction
     */
    @Nonnull
    @CheckReturnValue
    CommandEditAction apply(@Nonnull CommandData commandData);

    /**
     * Configure the name
     *
     * @param  name
     *         The lowercase alphanumeric (with dash) name, 1-32 characters. Use null to keep the current name.
     *
     * @throws IllegalArgumentException
     *         If the name is not alphanumeric or not between 1-32 characters
     *
     * @return The CommandEditAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    CommandEditAction setName(@Nullable String name);

    /**
     * Sets whether this command is only usable in a guild (Default: false).
     * <br>This only has an effect if this command is registered globally.
     *
     * @param  guildOnly
     *         Whether to restrict this command to guilds
     *
     * @return The CommandEditAction instance, for chaining
     *
     * @deprecated Replaced with {@link #setContexts(InteractionContextType...)}
     */
    @Nonnull
    @Deprecated
    @CheckReturnValue
    CommandEditAction setGuildOnly(boolean guildOnly);

    /**
     * Sets the contexts in which this command can be executed (Default: Guild and Bot DMs).
     * <br>This only has an effect if this command is registered globally.
     *
     * @param  contexts
     *         The contexts in which this command can be executed
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    default CommandEditAction setContexts(@Nonnull InteractionContextType... contexts)
    {
        return setContexts(Arrays.asList(contexts));
    }

    /**
     * Sets the contexts in which this command can be executed (Default: Guild and Bot DMs).
     * <br>This only has an effect if this command is registered globally.
     *
     * @param  contexts
     *         The contexts in which this command can be executed
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    CommandEditAction setContexts(@Nonnull Collection<InteractionContextType> contexts);

    /**
     * Sets the integration types on which this command can be installed on (Default: Guilds).
     * <br>This only has an effect if this command is registered globally.
     *
     * @param  integrationTypes
     *         The integration types on which this command can be installed on
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    default CommandEditAction setIntegrationTypes(@Nonnull IntegrationType... integrationTypes)
    {
        return setIntegrationTypes(Arrays.asList(integrationTypes));
    }

    /**
     * Sets the integration types on which this command can be installed on (Default: Guilds).
     * <br>This only has an effect if this command is registered globally.
     *
     * @param  integrationTypes
     *         The integration types on which this command can be installed on
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    CommandEditAction setIntegrationTypes(@Nonnull Collection<IntegrationType> integrationTypes);

    /**
     * Sets whether this command should only be usable in NSFW (age-restricted) channels.
     * <br>Default: false
     *
     * <p>Note: Age-restricted commands will not show up in direct messages by default unless the user enables them in their settings.
     *
     * @param  nsfw
     *         True, to make this command nsfw
     *
     * @return The CommandEditAction instance, for chaining
     *
     * @see <a href="https://support.discord.com/hc/en-us/articles/10123937946007" target="_blank">Age-Restricted Commands FAQ</a>
     */
    @Nonnull
    @CheckReturnValue
    CommandEditAction setNSFW(boolean nsfw);

    /**
     * Sets the {@link net.dv8tion.jda.api.Permission Permissions} that a user must have in a specific channel to be able to use this command.
     * <br>By default, everyone can use this command ({@link DefaultMemberPermissions#ENABLED}). Additionally, a command can be disabled for everyone but admins via {@link DefaultMemberPermissions#DISABLED}.
     * <p>These configurations can be overwritten by moderators in each guild. See {@link Command#retrievePrivileges(net.dv8tion.jda.api.entities.Guild)} to get moderator defined overrides.
     *
     * @param  permission
     *         {@link DefaultMemberPermissions} representing the default permissions of this command.
     *
     * @return The CommandEditAction instance, for chaining
     *
     * @see DefaultMemberPermissions#ENABLED
     * @see DefaultMemberPermissions#DISABLED
     */
    @Nonnull
    @CheckReturnValue
    CommandEditAction setDefaultPermissions(@Nonnull DefaultMemberPermissions permission);

    /**
     * Configure the description
     *
     * @param  description
     *         The description, 1-100 characters. Use null to keep the current description.
     *
     * @throws IllegalArgumentException
     *         If the name is null or not between 1-100 characters
     *
     * @return The CommandEditAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    CommandEditAction setDescription(@Nullable String description);

    /**
     * Removes all existing options/subcommands/groups from this command.
     *
     * @return The CommandEditAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    CommandEditAction clearOptions();

    /**
     * Adds up to 25 options to this command.
     * <br>This will replace any existing options/subcommands/groups on the command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *         The {@link OptionData Options} to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If you try to mix subcommands/options/groups in one command.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The CommandEditAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    CommandEditAction addOptions(@Nonnull OptionData... options);

    /**
     * Adds up to 25 options to this command.
     * <br>This will replace any existing options/subcommands/groups on the command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *         The {@link OptionData Options} to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If you try to mix subcommands/options/groups in one command.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The CommandEditAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    default CommandEditAction addOptions(@Nonnull Collection<? extends OptionData> options)
    {
        Checks.noneNull(options, "Options");
        return addOptions(options.toArray(new OptionData[0]));
    }

    /**
     * Adds an option to this command.
     * <br>This will replace any existing options/subcommands/groups on the command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-32 characters
     * @param  description
     *         The option description, 1-100 characters
     * @param  required
     *         Whether this option is required (See {@link OptionData#setRequired(boolean)})
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If you try to mix subcommands/options/groups in one command.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The CommandEditAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    default CommandEditAction addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required)
    {
        return addOptions(new OptionData(type, name, description).setRequired(required));
    }

    /**
     * Adds an option to this command.
     * <br>This will replace any existing options/subcommands/groups on the command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-32 characters
     * @param  description
     *         The option description, 1-100 characters
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If you try to mix subcommands/options/groups in one command.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The CommandEditAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    default CommandEditAction addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description)
    {
        return addOption(type, name, description, false);
    }

    /**
     * Add up to 25 {@link SubcommandData Subcommands} to this command.
     * <br>This will replace any existing options/subcommands/groups on the command.
     *
     * @param  subcommands
     *         The subcommands to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommands are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandEditAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    CommandEditAction addSubcommands(@Nonnull SubcommandData... subcommands);

    /**
     * Add up to 25 {@link SubcommandData Subcommands} to this command.
     * <br>This will replace any existing options/subcommands/groups on the command.
     *
     * @param  subcommands
     *         The subcommands to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommands are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandEditAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    default CommandEditAction addSubcommands(@Nonnull Collection<? extends SubcommandData> subcommands)
    {
        Checks.noneNull(subcommands, "Subcommands");
        return addSubcommands(subcommands.toArray(new SubcommandData[0]));
    }

    /**
     * Add up to 25 {@link SubcommandGroupData Subcommand-Groups} to this command.
     * <br>This will replace any existing options/subcommands/groups on the command.
     *
     * @param  groups
     *         The subcommand groups to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommand groups are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandEditAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    CommandEditAction addSubcommandGroups(@Nonnull SubcommandGroupData... groups);

    /**
     * Add up to 25 {@link SubcommandGroupData Subcommand-Groups} to this command.
     * <br>This will replace any existing options/subcommands/groups on the command.
     *
     * @param  groups
     *         The subcommand groups to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommand groups are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandEditAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    default CommandEditAction addSubcommandGroups(@Nonnull Collection<? extends SubcommandGroupData> groups)
    {
        Checks.noneNull(groups, "SubcommandGroups");
        return addSubcommandGroups(groups.toArray(new SubcommandGroupData[0]));
    }
}
