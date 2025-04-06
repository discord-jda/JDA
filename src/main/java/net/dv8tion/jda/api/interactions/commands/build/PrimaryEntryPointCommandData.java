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
import net.dv8tion.jda.api.interactions.commands.build.attributes.IDescribedCommandData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.requests.restaction.GlobalCommandListUpdateAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.interactions.PrimaryEntryPointCommandDataImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

/**
 * Builder for Entry Point Commands.
 * <br>Use {@link Commands#primaryEntryPoint(String, String)} to create instances of this interface.
 *
 * <p>This command can only be added via {@link GlobalCommandListUpdateAction#setPrimaryEntryPointCommand(PrimaryEntryPointCommandData)}.
 *
 * @see Commands#primaryEntryPoint(String, String)
 */
public interface PrimaryEntryPointCommandData extends CommandData, IDescribedCommandData
{
    @Nonnull
    @Override
    PrimaryEntryPointCommandData setLocalizationFunction(@Nonnull LocalizationFunction localizationFunction);

    @Nonnull
    @Override
    PrimaryEntryPointCommandData setName(@Nonnull String name);

    @Nonnull
    @Override
    PrimaryEntryPointCommandData setNameLocalization(@Nonnull DiscordLocale locale, @Nonnull String name);

    @Nonnull
    @Override
    PrimaryEntryPointCommandData setNameLocalizations(@Nonnull Map<DiscordLocale, String> map);

    @Nonnull
    @Override
    PrimaryEntryPointCommandData setDescription(@Nonnull String description);

    @Nonnull
    @Override
    PrimaryEntryPointCommandData setDescriptionLocalization(@Nonnull DiscordLocale locale, @Nonnull String description);

    @Nonnull
    @Override
    PrimaryEntryPointCommandData setDescriptionLocalizations(@Nonnull Map<DiscordLocale, String> map);

    @Nonnull
    @Override
    PrimaryEntryPointCommandData setDefaultPermissions(@Nonnull DefaultMemberPermissions permission);

    @Nonnull
    @Override
    @Deprecated
    PrimaryEntryPointCommandData setGuildOnly(boolean guildOnly);

    @Nonnull
    @Override
    default PrimaryEntryPointCommandData setContexts(@Nonnull InteractionContextType... contexts)
    {
        return (PrimaryEntryPointCommandData) CommandData.super.setContexts(contexts);
    }

    @Nonnull
    @Override
    PrimaryEntryPointCommandData setContexts(@Nonnull Collection<InteractionContextType> contexts);

    @Nonnull
    @Override
    default PrimaryEntryPointCommandData setIntegrationTypes(@Nonnull IntegrationType... integrationTypes)
    {
        return (PrimaryEntryPointCommandData) CommandData.super.setIntegrationTypes(integrationTypes);
    }

    @Nonnull
    @Override
    PrimaryEntryPointCommandData setIntegrationTypes(@Nonnull Collection<IntegrationType> integrationTypes);

    @Nonnull
    @Override
    PrimaryEntryPointCommandData setNSFW(boolean nsfw);

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
    PrimaryEntryPointCommandData setHandler(@Nonnull Handler handler);

    /**
     * Converts the provided {@link Command} into a {@link PrimaryEntryPointCommandData} instance.
     *
     * <p>This only works if the command is of type {@link Command.Type#PRIMARY_ENTRY_POINT}!
     *
     * @param  command
     *         The command to convert
     *
     * @throws IllegalArgumentException
     *         If null is provided or the command has illegal configuration
     *
     * @return An instance of {@link PrimaryEntryPointCommandData}
     */
    @Nonnull
    @SuppressWarnings("DataFlowIssue") // Handler cannot be null if command type is correct
    static PrimaryEntryPointCommandData fromCommand(@Nonnull Command command)
    {
        Checks.notNull(command, "Command");
        if (command.getType() != Command.Type.PRIMARY_ENTRY_POINT)
            throw new IllegalArgumentException("Cannot convert command of type " + command.getType() + " to EntryPointCommandData!");

        PrimaryEntryPointCommandDataImpl data = new PrimaryEntryPointCommandDataImpl(command.getName(), command.getDescription());
        CommandDataImpl.applyBaseData(data, command);
        CommandDataImpl.applyDescribedCommandData(data, command);

        data.setHandler(command.getHandler());
        return data;
    }

    /**
     * Parses the provided serialization back into a {@link PrimaryEntryPointCommandData} instance.
     * <br>This is the reverse function for {@link PrimaryEntryPointCommandData#toData()}.
     *
     * @param  object
     *         The serialized {@link DataObject} representing the command
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     * @throws IllegalArgumentException
     *         If any of the values are failing the respective checks such as length
     *
     * @return The parsed {@link PrimaryEntryPointCommandData} instance, which can be further configured through setters
     */
    @Nonnull
    static PrimaryEntryPointCommandData fromData(@Nonnull DataObject object)
    {
        Checks.notNull(object, "DataObject");

        String name = object.getString("name");
        Command.Type commandType = Command.Type.fromId(object.getInt("type", 1));
        Checks.check(commandType == Command.Type.PRIMARY_ENTRY_POINT, "Cannot convert command '" + name + "' of type " + commandType + " to PrimaryEntryPointCommandData!");

        PrimaryEntryPointCommandDataImpl data = new PrimaryEntryPointCommandDataImpl(name, object.getString("description"));

        CommandDataImpl.applyBaseData(data, object);
        CommandDataImpl.applyDescribedCommandData(data, object);

        data.setHandler(Handler.fromValue(object.getLong("handler")));
        return data;
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
