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
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Specialized {@link RestAction} used to create or update commands.
 * <br>If a command with the specified name already exists, it will be replaced!
 *
 * <p>This operation is <b>not</b> idempotent!
 * Commands will persist between restarts of your bot, you only have to create a command once.
 */
public interface CommandCreateAction extends RestAction<Command>, SlashCommandData
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
    @CheckReturnValue
    CommandCreateAction deadline(long timestamp);

    @Nonnull
    @Override
    @CheckReturnValue
    CommandCreateAction setDefaultEnabled(boolean enabled);

    @Nonnull
    @Override
    @CheckReturnValue
    CommandCreateAction setName(@Nonnull String name);

    @Nonnull
    @Override
    @CheckReturnValue
    CommandCreateAction setDescription(@Nonnull String description);

    @Nonnull
    @Override
    @CheckReturnValue
    CommandCreateAction addOptions(@Nonnull OptionData... options);

    @Nonnull
    @Override
    @CheckReturnValue
    default CommandCreateAction addOptions(@Nonnull Collection<? extends OptionData> options)
    {
        return (CommandCreateAction) SlashCommandData.super.addOptions(options);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    default CommandCreateAction addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required, boolean autoComplete)
    {
        return (CommandCreateAction) SlashCommandData.super.addOption(type, name, description, required, autoComplete);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    default CommandCreateAction addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required)
    {
        return (CommandCreateAction) SlashCommandData.super.addOption(type, name, description, required);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    default CommandCreateAction addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description)
    {
        return (CommandCreateAction) SlashCommandData.super.addOption(type, name, description, false);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    CommandCreateAction addSubcommands(@Nonnull SubcommandData... subcommands);

    @Nonnull
    @Override
    @CheckReturnValue
    default CommandCreateAction addSubcommands(@Nonnull Collection<? extends SubcommandData> subcommands)
    {
        return (CommandCreateAction) SlashCommandData.super.addSubcommands(subcommands);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    CommandCreateAction addSubcommandGroups(@Nonnull SubcommandGroupData... groups);

    @Nonnull
    @Override
    @CheckReturnValue
    default CommandCreateAction addSubcommandGroups(@Nonnull Collection<? extends SubcommandGroupData> groups)
    {
        return (CommandCreateAction) SlashCommandData.super.addSubcommandGroups(groups);
    }
}
