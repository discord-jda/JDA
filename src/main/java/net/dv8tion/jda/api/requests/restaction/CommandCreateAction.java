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
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Specialized {@link RestAction} used to create or update commands.
 * <br>If a command with the specified name already exists, it will be replaced!
 */
public interface CommandCreateAction extends RestAction<Command>
{
    @Nonnull
    @Override
    CommandCreateAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    CommandCreateAction addCheck(@Nonnull BooleanSupplier checks);

    @Nonnull
    @Override
    CommandCreateAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    CommandCreateAction deadline(long timestamp);

    /**
     * Whether this command is available to everyone by default.
     * <br>If this is disabled, you need to explicitly whitelist users and roles per guild.
     *
     * @param  enabled
     *         True, if this command is enabled by default for everyone. (Default: true)
     *
     * @return The CommandCreateAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    CommandCreateAction setDefaultEnabled(boolean enabled);

    /**
     * Change the name of the command to the provided name.
     *
     * @param  name
     *         The lowercase alphanumeric (with dash) name, 1-32 characters
     *
     * @throws IllegalArgumentException
     *         If the name is null, not alphanumeric, or not between 1-32 characters
     *
     * @return The CommandCreateAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    CommandCreateAction setName(@Nonnull String name);

    /**
     * Change the description of the command.
     * <br>This is visible to the user in the client and should give a meaningful description of this command.
     *
     * @param  description
     *         The description, 1-100 characters
     *
     * @throws IllegalArgumentException
     *         If the name is not lowercase, alphanumeric (with dash), or 1-32 characters long
     *
     * @return The CommandCreateAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    CommandCreateAction setDescription(@Nonnull String description);

    /**
     * Add up to 25 options to this command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *         The options to add
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
     * @return The CommandCreateAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    CommandCreateAction addOptions(@Nonnull OptionData... options);

    /**
     * Add up to 25 options to this command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *         The options to add
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
     * @return The CommandCreateAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    default CommandCreateAction addOptions(@Nonnull Collection<? extends OptionData> options)
    {
        Checks.noneNull(options, "Option");
        return addOptions(options.toArray(new OptionData[0]));
    }

    /**
     * Add one option to this command.
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
     * @return The CommandCreateAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    default CommandCreateAction addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required)
    {
        return addOptions(new OptionData(type, name, description).setRequired(required));
    }

    /**
     * Add one option to this command.
     * <br>The option is set to be non-required! You can use {@link #addOption(OptionType, String, String, boolean)} to add a required option instead.
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
     * @return The CommandCreateAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    default CommandCreateAction addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description)
    {
        return addOption(type, name, description, false);
    }

    /**
     * Add up to 25 {@link SubcommandData Subcommands} to this command.
     *
     * @param  subcommands
     *         The subcommands to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommands are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandCreateAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    CommandCreateAction addSubcommands(@Nonnull SubcommandData... subcommands);

    /**
     * Add up to 25 {@link SubcommandData Subcommands} to this command.
     *
     * @param  subcommands
     *         The subcommands to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommands are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandCreateAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    default CommandCreateAction addSubcommands(@Nonnull Collection<? extends SubcommandData> subcommands)
    {
        Checks.noneNull(subcommands, "Subcommand");
        return addSubcommands(subcommands.toArray(new SubcommandData[0]));
    }

    /**
     * Add up to 25 {@link SubcommandGroupData Subcommand-Groups} to this command.
     *
     * @param  groups
     *         The subcommand groups to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommand groups are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandCreateAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    CommandCreateAction addSubcommandGroups(@Nonnull SubcommandGroupData... groups);

    /**
     * Add up to 25 {@link SubcommandGroupData Subcommand-Groups} to this command.
     *
     * @param  groups
     *         The subcommand groups to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommand groups are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandCreateAction instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    default CommandCreateAction addSubcommandGroups(@Nonnull Collection<? extends SubcommandGroupData> groups)
    {
        Checks.noneNull(groups, "Subcommand group");
        return addSubcommandGroups(groups.toArray(new SubcommandGroupData[0]));
    }
}
