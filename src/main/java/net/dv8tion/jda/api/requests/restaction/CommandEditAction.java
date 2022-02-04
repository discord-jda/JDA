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

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
     * Whether this command is available to everyone by default.
     * <br>If this is disabled, you need to explicitly whitelist users and roles per guild.
     *
     * @param  enabled
     *         True, if this command is enabled by default for everyone. (Default: true)
     *
     * @return The CommandEditAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    CommandEditAction setDefaultEnabled(boolean enabled);

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
