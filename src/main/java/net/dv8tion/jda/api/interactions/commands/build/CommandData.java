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

package net.dv8tion.jda.api.interactions.commands.build;

import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.attributes.ILocalizedCommandData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.UnmodifiableView;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Builder for Application Commands.
 * <br>Use the factory methods provided by {@link Commands} to create instances of this interface.
 *
 * @see Commands
 */
public interface CommandData extends ILocalizedCommandData, SerializableData
{
    /**
     * The maximum length the name of a command can be. ({@value})
     */
    int MAX_NAME_LENGTH = 32;

    /**
     * The maximum amount of options/subcommands/groups that can be added to a command or subcommand. ({@value})
     */
    int MAX_OPTIONS = 25;

    @Nonnull
    @Override
    CommandData setLocalizationFunction(@Nonnull LocalizationFunction localizationFunction);

    /**
     * Configure the command name.
     *
     * @param  name
     *         The name, 1-{@value #MAX_NAME_LENGTH} characters (lowercase and alphanumeric for {@link Command.Type#SLASH})
     *
     * @throws IllegalArgumentException
     *         If the name is not between 1-{@value #MAX_NAME_LENGTH} characters long, or not lowercase and alphanumeric for slash commands
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    CommandData setName(@Nonnull String name);

    /**
     * Sets a {@link DiscordLocale language-specific} localization of this command's name.
     *
     * @param  locale
     *         The locale to associate the translated name with
     *
     * @param  name
     *         The translated name to put
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the locale is null</li>
     *             <li>If the name is null</li>
     *             <li>If the locale is {@link DiscordLocale#UNKNOWN}</li>
     *             <li>If the name does not pass the corresponding {@link #setName(String) name check}</li>
     *         </ul>
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    CommandData setNameLocalization(@Nonnull DiscordLocale locale, @Nonnull String name);

    /**
     * Sets multiple {@link DiscordLocale language-specific} localizations of this command's name.
     *
     * @param  map
     *         The map from which to transfer the translated names
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the map is null</li>
     *             <li>If the map contains an {@link DiscordLocale#UNKNOWN} key</li>
     *             <li>If the map contains a name which does not pass the corresponding {@link #setName(String) name check}</li>
     *         </ul>
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    CommandData setNameLocalizations(@Nonnull Map<DiscordLocale, String> map);

    /**
     * Sets the {@link net.dv8tion.jda.api.Permission Permissions} that a user must have in a specific channel to be able to use this command.
     * <br>By default, everyone can use this command ({@link DefaultMemberPermissions#ENABLED}). Additionally, a command can be disabled for everyone but admins via {@link DefaultMemberPermissions#DISABLED}.
     * <p>These configurations can be overwritten by moderators in each guild. See {@link Command#retrievePrivileges(net.dv8tion.jda.api.entities.Guild)} to get moderator defined overrides.
     *
     * @param  permission
     *         {@link DefaultMemberPermissions} representing the default permissions of this command.
     *
     * @return The builder instance, for chaining
     *
     * @see DefaultMemberPermissions#ENABLED
     * @see DefaultMemberPermissions#DISABLED
     */
    @Nonnull
    CommandData setDefaultPermissions(@Nonnull DefaultMemberPermissions permission);

    /**
     * Sets whether this command is only usable in a guild (Default: false).
     * <br>This only has an effect if this command is registered globally.
     *
     * @param  guildOnly
     *         Whether to restrict this command to guilds
     *
     * @return The builder instance, for chaining
     *
     * @deprecated Replaced with {@link #setContexts(InteractionContextType...)}
     */
    @Nonnull
    @Deprecated
    @ReplaceWith("setContexts(InteractionContextType.GUILD)")
    CommandData setGuildOnly(boolean guildOnly);

    /**
     * Sets the contexts in which this command can be used (Default: Guild and Bot DMs).
     * <br>This only has an effect if this command is registered globally.
     *
     * @param  contexts
     *         The contexts in which this command can be used
     *
     * @throws IllegalArgumentException
     *         If {@code null} or no interaction context types were passed
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    default CommandData setContexts(@Nonnull InteractionContextType... contexts)
    {
        Checks.notEmpty(contexts, "Contexts");
        return setContexts(Arrays.asList(contexts));
    }

    /**
     * Sets the contexts in which this command can be used (Default: Guild and Bot DMs).
     * <br>This only has an effect if this command is registered globally.
     *
     * @param  contexts
     *         The contexts in which this command can be used
     *
     * @throws IllegalArgumentException
     *         If {@code null} or no interaction context types were passed
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    CommandData setContexts(@Nonnull Collection<InteractionContextType> contexts);

    /**
     * Sets the integration types on which this command can be installed on (Default: Guilds).
     * <br>This only has an effect if this command is registered globally.
     *
     * @param  integrationTypes
     *         The integration types on which this command can be installed on
     *
     * @throws IllegalArgumentException
     *         If {@code null} or no integration types were passed
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    default CommandData setIntegrationTypes(@Nonnull IntegrationType... integrationTypes)
    {
        Checks.notEmpty(integrationTypes, "Integration types");
        return setIntegrationTypes(Arrays.asList(integrationTypes));
    }

    /**
     * Sets the integration types on which this command can be installed on (Default: Guilds).
     * <br>This only has an effect if this command is registered globally.
     *
     * @param  integrationTypes
     *         The integration types on which this command can be installed on
     *
     * @throws IllegalArgumentException
     *         If {@code null} or no integration types were passed
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    CommandData setIntegrationTypes(@Nonnull Collection<IntegrationType> integrationTypes);

    /**
     * Sets whether this command should only be usable in NSFW (age-restricted) channels.
     * <br>Default: false
     *
     * <p>Note: Age-restricted commands will not show up in direct messages by default unless the user enables them in their settings.
     *
     * @param  nsfw
     *         True, to make this command nsfw
     *
     * @return The builder instance, for chaining
     *
     * @see <a href="https://support.discord.com/hc/en-us/articles/10123937946007" target="_blank">Age-Restricted Commands FAQ</a>
     */
    @Nonnull
    CommandData setNSFW(boolean nsfw);

    /**
     * The current command name
     *
     * @return The command name
     */
    @Nonnull
    String getName();

    /**
     * The localizations of this command's name for {@link DiscordLocale various languages}.
     *
     * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized name
     */
    @Nonnull
    LocalizationMap getNameLocalizations();

    /**
     * The {@link Command.Type}
     *
     * @return The {@link Command.Type}
     */
    @Nonnull
    Command.Type getType();

    /**
     * Gets the {@link DefaultMemberPermissions} of this command.
     * <br>If no permissions have been set, this returns {@link DefaultMemberPermissions#ENABLED}.
     *
     * @return DefaultMemberPermissions of this command.
     *
     * @see    DefaultMemberPermissions#ENABLED
     * @see    DefaultMemberPermissions#DISABLED
     */
    @Nonnull
    DefaultMemberPermissions getDefaultPermissions();

    /**
     * Whether the command can only be used inside a guild.
     * <br>Always true for guild commands.
     *
     * @return True, if this command is restricted to guilds.
     *
     * @deprecated Replaced with {@link #getContexts()}
     */
    @Deprecated
    @ReplaceWith("getContexts().equals(EnumSet.of(InteractionContextType.GUILD))")
    boolean isGuildOnly();

    /**
     * The contexts in which this command can be used.
     *
     * @return The contexts in which this command can be used
     */
    @Nonnull
    @UnmodifiableView
    Set<InteractionContextType> getContexts();

    /**
     * Gets the integration types on which this command can be installed on.
     *
     * @return The integration types on which this command can be installed on
     */
    @Nonnull
    @UnmodifiableView
    Set<IntegrationType> getIntegrationTypes();

    /**
     * Whether this command should only be usable in NSFW (age-restricted) channels
     *
     * @return True, if this command is restricted to NSFW channels
     *
     * @see <a href="https://support.discord.com/hc/en-us/articles/10123937946007" target="_blank">Age-Restricted Commands FAQ</a>
     */
    boolean isNSFW();

    /**
     * Converts the provided {@link Command} into a CommandData instance.
     *
     * @param  command
     *         The command to convert
     *
     * @throws IllegalArgumentException
     *         If null is provided or the command has illegal configuration
     *
     * @return An instance of CommandData
     *
     * @see    SlashCommandData#fromCommand(Command)
     */
    @Nonnull
    static CommandData fromCommand(@Nonnull Command command)
    {
        Checks.notNull(command, "Command");
        switch (command.getType())
        {
        case SLASH:
            return SlashCommandData.fromCommand(command);
        case USER:
        case MESSAGE:
        {
            CommandDataImpl data = new CommandDataImpl(command.getType(), command.getName());
            CommandDataImpl.applyBaseData(data, command);
            return data;
        }
        case PRIMARY_ENTRY_POINT:
            return PrimaryEntryPointCommandData.fromCommand(command);
        }

        throw new UnsupportedOperationException("Cannot create a command from an unknown type: " + command.getType());
    }

    /**
     * Parses the provided serialization back into an CommandData instance.
     * <br>This is the reverse function for {@link CommandData#toData()}.
     *
     * @param  object
     *         The serialized {@link DataObject} representing the command
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     * @throws IllegalArgumentException
     *         If any of the values are failing the respective checks such as length
     *
     * @return The parsed CommandData instance, which can be further configured through setters
     *
     * @see    SlashCommandData#fromData(DataObject)
     * @see    Commands#fromList(Collection)
     */
    @Nonnull
    static CommandData fromData(@Nonnull DataObject object)
    {
        Checks.notNull(object, "DataObject");

        final int rawType = object.getInt("type", 1);
        Command.Type commandType = Command.Type.fromId(rawType);
        switch (commandType)
        {
        case SLASH:
            return SlashCommandData.fromData(object);
        case USER:
        case MESSAGE:
        {
            CommandDataImpl data = new CommandDataImpl(commandType, object.getString("name"));
            CommandDataImpl.applyBaseData(data, object);
            return data;
        }
        case PRIMARY_ENTRY_POINT:
            return PrimaryEntryPointCommandData.fromData(object);
        }

        throw new UnsupportedOperationException("Cannot create a command from an unknown type: " + rawType);
    }
}
