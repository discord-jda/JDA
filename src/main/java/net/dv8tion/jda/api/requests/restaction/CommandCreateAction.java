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
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

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
    CommandCreateAction addOption(@Nonnull OptionData data);

    @Nonnull
    @CheckReturnValue
    CommandCreateAction addSubcommand(@Nonnull SubcommandData data);

    @Nonnull
    @CheckReturnValue
    CommandCreateAction addSubcommandGroup(@Nonnull SubcommandGroupData data);
}
