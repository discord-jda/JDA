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

import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.entities.Command.OptionType;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

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

    @Nonnull
    @CheckReturnValue
    CommandCreateAction setName(@Nonnull String name);

    @Nonnull
    @CheckReturnValue
    CommandCreateAction setDescription(@Nonnull String description);

    @Nonnull
    @CheckReturnValue
    CommandCreateAction addOption(@Nonnull String name, @Nonnull String description, @Nonnull OptionType type, @Nonnull Consumer<? super OptionBuilder> builder);

    @Nonnull
    @CheckReturnValue
    default CommandCreateAction addOption(@Nonnull String name, @Nonnull String description, @Nonnull OptionType type)
    {
        return addOption(name, description, type, Helpers.emptyConsumer());
    }

    interface OptionBuilder
    {
        OptionBuilder setRequired(boolean required); // note: required options must come first
        OptionBuilder setDefault(boolean isDefault); // there can only be one default (maybe make this special) | default may not be set by a SUB_COMMAND or SUB_COMMAND_GROUP
        OptionBuilder addChoice(String name, String value);
        OptionBuilder addChoice(String name, int value);

        OptionBuilder addOption(String name, String description, OptionType type, Consumer<? super OptionBuilder> builder);
        default OptionBuilder addOption(String name, String description, OptionType type)
        {
            return addOption(name, description, type, Helpers.emptyConsumer());
        }
    }
}
