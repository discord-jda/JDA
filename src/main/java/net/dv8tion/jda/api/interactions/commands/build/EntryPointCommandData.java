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
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.attributes.*;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Builder for Entry Point Commands.
 * <br>Use the factory methods provided by {@link Commands} to create instances of this interface.
 *
 * @see Commands
 */
public interface EntryPointCommandData
        extends IDescribedCommandData, INamedCommandData, IScopedCommandData, IRestrictedCommandData,
        IAgeRestrictedCommandData, SerializableData
{
    //TODO docs
    enum Handler
    {
        UNKNOWN(-1),
        /**
         * Lets this app handle the activity start via an interaction.
         */
        APP_HANDLER(1),
        /**
         * Lets Discord handle the activity start,
         * and sends a follow-up message without coordinating with this app.
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
    EntryPointCommandData setNSFW(boolean nsfw);

    //TODO docs
    @Nonnull
    EntryPointCommandData setHandler(Handler handler);

    //TODO docs
    @Nonnull
    static EntryPointCommandData fromCommand(@Nonnull Command command)
    {
        //TODO
        throw new UnsupportedOperationException();
    }

    //TODO docs
    @Nonnull
    static EntryPointCommandData fromData(@Nonnull DataObject object)
    {
        //TODO
        throw new UnsupportedOperationException();
    }
}
