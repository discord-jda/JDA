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

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.attributes.*;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.requests.restaction.GlobalCommandListUpdateAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.interactions.EntryPointCommandDataImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.localization.LocalizationUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

/**
 * Builder for Entry Point Commands.
 * <br>Use {@link Commands#entryPoint(String, String)} to create instances of this interface.
 *
 * <p>This command can only be added via {@link GlobalCommandListUpdateAction#setEntryPointCommand(EntryPointCommandData)}.
 *
 * @see Commands#entryPoint(String, String)
 */
public interface EntryPointCommandData
        extends IDescribedCommandData, INamedCommandData, IScopedCommandData, IPermissionRestrictedCommandData,
        IAgeRestrictedCommandData, SerializableData
{
    @Nonnull
    @Override
    EntryPointCommandData setLocalizationFunction(@Nonnull LocalizationFunction localizationFunction);

    @Nonnull
    @Override
    EntryPointCommandData setName(@Nonnull String name);

    @Nonnull
    @Override
    EntryPointCommandData setNameLocalization(@Nonnull DiscordLocale locale, @Nonnull String name);

    @Nonnull
    @Override
    EntryPointCommandData setNameLocalizations(@Nonnull Map<DiscordLocale, String> map);

    @Nonnull
    @Override
    EntryPointCommandData setDescription(@Nonnull String description);

    @Nonnull
    @Override
    EntryPointCommandData setDescriptionLocalization(@Nonnull DiscordLocale locale, @Nonnull String description);

    @Nonnull
    @Override
    EntryPointCommandData setDescriptionLocalizations(@Nonnull Map<DiscordLocale, String> map);

    @Nonnull
    @Override
    EntryPointCommandData setDefaultPermissions(@Nonnull DefaultMemberPermissions permission);

    @Nonnull
    @Override
    EntryPointCommandData setGuildOnly(boolean guildOnly);

    @Nonnull
    @Override
    default EntryPointCommandData setContexts(@Nonnull InteractionContextType... contexts)
    {
        return (EntryPointCommandData) IScopedCommandData.super.setContexts(contexts);
    }

    @Nonnull
    @Override
    EntryPointCommandData setContexts(@Nonnull Collection<InteractionContextType> contexts);

    @Nonnull
    @Override
    default EntryPointCommandData setIntegrationTypes(@Nonnull IntegrationType... integrationTypes)
    {
        return (EntryPointCommandData) IScopedCommandData.super.setIntegrationTypes(integrationTypes);
    }

    @Nonnull
    @Override
    EntryPointCommandData setIntegrationTypes(@Nonnull Collection<IntegrationType> integrationTypes);

    @Nonnull
    @Override
    EntryPointCommandData setNSFW(boolean nsfw);

    /**
     * Sets the handler of this entry point command.
     * This defines the behavior when this command is used.
     *
     * @param  handler
     *         The handler type for this command
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    EntryPointCommandData setHandler(@Nonnull Handler handler);

    /**
     * Converts the provided {@link Command} into a {@link EntryPointCommandData} instance.
     *
     * @param  command
     *         The command to convert
     *
     * @throws IllegalArgumentException
     *         If null is provided or the command has illegal configuration
     *
     * @return An instance of {@link EntryPointCommandData}
     */
    @Nonnull
    @SuppressWarnings("DataFlowIssue") // Handler cannot be null if command type is correct
    static EntryPointCommandData fromCommand(@Nonnull Command command)
    {
        Checks.notNull(command, "Command");
        if (command.getType() != Command.Type.PRIMARY_ENTRY_POINT)
            throw new IllegalArgumentException("Cannot convert command of type " + command.getType() + " to EntryPointCommandData!");

        EntryPointCommandDataImpl data = new EntryPointCommandDataImpl(command.getName(), command.getDescription());
        data.setContexts(command.getContexts());
        data.setIntegrationTypes(command.getIntegrationTypes());
        data.setNSFW(command.isNSFW());
        data.setDefaultPermissions(command.getDefaultPermissions());
        //Command localizations are unmodifiable, make a copy
        data.setNameLocalizations(command.getNameLocalizations().toMap());
        data.setDescriptionLocalizations(command.getDescriptionLocalizations().toMap());
        data.setHandler(command.getHandler());
        return data;
    }

    /**
     * Parses the provided serialization back into a {@link EntryPointCommandData} instance.
     * <br>This is the reverse function for {@link EntryPointCommandData#toData()}.
     *
     * @param  object
     *         The serialized {@link DataObject} representing the command
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     * @throws IllegalArgumentException
     *         If any of the values are failing the respective checks such as length
     *
     * @return The parsed {@link EntryPointCommandData} instance, which can be further configured through setters
     */
    @Nonnull
    static EntryPointCommandData fromData(@Nonnull DataObject object)
    {
        Checks.notNull(object, "DataObject");

        Command.Type commandType = Command.Type.fromId(object.getInt("type", 1));
        Checks.check(commandType == Command.Type.PRIMARY_ENTRY_POINT, "Cannot convert command of type " + commandType + " to EntryPointCommandData!");

        String name = object.getString("name");
        String description = object.getString("description");
        EntryPointCommandDataImpl command = new EntryPointCommandDataImpl(name, description);
        if (!object.isNull("contexts"))
        {
            command.setContexts(object.getArray("contexts")
                    .stream(DataArray::getString)
                    .map(InteractionContextType::fromKey)
                    .collect(Helpers.toUnmodifiableEnumSet(InteractionContextType.class)));
        }
        else if (!object.isNull("dm_permission"))
            command.setGuildOnly(!object.getBoolean("dm_permission"));
        else
            command.setContexts(Helpers.unmodifiableEnumSet(InteractionContextType.GUILD, InteractionContextType.BOT_DM));

        if (!object.isNull("integration_types"))
        {
            command.setIntegrationTypes(object.getArray("integration_types")
                    .stream(DataArray::getString)
                    .map(IntegrationType::fromKey)
                    .collect(Helpers.toUnmodifiableEnumSet(IntegrationType.class)));
        }
        else
            command.setIntegrationTypes(Helpers.unmodifiableEnumSet(IntegrationType.GUILD_INSTALL));

        command.setNSFW(object.getBoolean("nsfw"));

        command.setDefaultPermissions(
                object.isNull("default_member_permissions")
                        ? DefaultMemberPermissions.ENABLED
                        : DefaultMemberPermissions.enabledFor(object.getLong("default_member_permissions"))
        );

        command.setNameLocalizations(LocalizationUtils.mapFromProperty(object, "name_localizations"));
        command.setDescriptionLocalizations(LocalizationUtils.mapFromProperty(object, "description_localizations"));

        command.setHandler(Handler.fromValue(object.getLong("handler")));

        return command;
    }

    /**
     * Defines the behavior of an Entry Point Command.
     */
    enum Handler
    {
        UNKNOWN(-1),
        /**
         * Lets this app handle the activity start via an interaction.
         */
        APP_HANDLER(1),
        /**
         * Lets Discord handle the activity interaction automatically
         * by launching the activity associated with this app,
         * and sends a follow-up message into the chat to indicate that an activity was launched.
         */
        DISCORD_LAUNCH_ACTIVITY(2);

        private final int value;

        Handler(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }

        /**
         * Converts the value to the corresponding handler.
         *
         * @param  value
         *         The value of the handler
         *
         * @return {@link Handler}
         */
        @Nonnull
        public static Handler fromValue(long value)
        {
            for (Handler handler : values())
            {
                if (handler.value == value)
                    return handler;
            }
            return UNKNOWN;
        }
    }
}
