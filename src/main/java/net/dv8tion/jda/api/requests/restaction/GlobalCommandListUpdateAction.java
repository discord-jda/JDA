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

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.PrimaryEntryPointCommandData;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Specialized {@link RestAction} used to replace existing commands globally.
 * <br>Any commands that currently exist and are not listed through {@link #addCommands(CommandData...)} will be <b>DELETED</b>!
 * <br>If your bot has activities enabled, you <b>must</b> {@link #setPrimaryEntryPointCommand(PrimaryEntryPointCommandData) set your entry point command}.
 *
 * <p>This operation is idempotent. Commands will persist between restarts of your bot, you only have to create a command once.
 */
public interface GlobalCommandListUpdateAction extends CommandListUpdateAction
{
    @Nonnull
    @Override
    @CheckReturnValue
    GlobalCommandListUpdateAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    @CheckReturnValue
    GlobalCommandListUpdateAction deadline(long timestamp);

    @Nonnull
    @Override
    @CheckReturnValue
    GlobalCommandListUpdateAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    @CheckReturnValue
    GlobalCommandListUpdateAction addCheck(@Nonnull BooleanSupplier checks);

    @Nonnull
    @Override
    @CheckReturnValue
    GlobalCommandListUpdateAction addCommands(@Nonnull Collection<? extends CommandData> commands);

    @Nonnull
    @Override
    @CheckReturnValue
    GlobalCommandListUpdateAction addCommands(@Nonnull CommandData... commands);

    /**
     * Sets your app's activity primary entry point.
     * <br>This <b>must</b> be set if your application has activities enabled.
     * <br>Using this with activities disabled will not make the entry point appear.
     *
     * @param  entryPoint
     *         The entry point data
     *
     * @return This instance, for chaining
     */
    @Nonnull
    @CheckReturnValue
    GlobalCommandListUpdateAction setPrimaryEntryPointCommand(@Nonnull PrimaryEntryPointCommandData entryPoint);
}
