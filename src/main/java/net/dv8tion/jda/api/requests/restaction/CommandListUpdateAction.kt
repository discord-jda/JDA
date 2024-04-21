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
package net.dv8tion.jda.api.requests.restaction

import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.internal.utils.*
import java.util.*
import java.util.concurrent.*
import java.util.function.BooleanSupplier
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Specialized [RestAction] used to replace existing commands of a guild or globally.
 * <br></br>Any commands that currently exist and are not listed through [.addCommands] will be **DELETED**!
 *
 *
 * This operation is idempotent. Commands will persist between restarts of your bot, you only have to create a command once.
 */
interface CommandListUpdateAction : RestAction<List<Command?>?> {
    @Nonnull
    override fun timeout(timeout: Long, @Nonnull unit: TimeUnit): CommandListUpdateAction?
    @Nonnull
    override fun deadline(timestamp: Long): CommandListUpdateAction?
    @Nonnull
    override fun setCheck(checks: BooleanSupplier?): CommandListUpdateAction?
    @Nonnull
    override fun addCheck(@Nonnull checks: BooleanSupplier): CommandListUpdateAction?

    /**
     * Adds up to
     * {@value Commands#MAX_SLASH_COMMANDS} slash commands,
     * {@value Commands#MAX_USER_COMMANDS} user context commands, and
     * {@value Commands#MAX_MESSAGE_COMMANDS} message context commands.
     *
     *
     * When a command is not listed in this request, it will be deleted.
     *
     * @param  commands
     * The [commands][CommandData] to add
     *
     * @throws IllegalArgumentException
     * If null or more than
     * {@value Commands#MAX_SLASH_COMMANDS} slash commands,
     * {@value Commands#MAX_USER_COMMANDS} user context commands, or
     * {@value Commands#MAX_MESSAGE_COMMANDS} message context commands, are provided
     *
     * @return The CommandUpdateAction instance, for chaining
     *
     * @see Commands.slash
     * @see Commands.message
     * @see Commands.user
     */
    @Nonnull
    @CheckReturnValue
    fun addCommands(@Nonnull commands: Collection<CommandData?>?): CommandListUpdateAction?

    /**
     * Adds up to
     * {@value Commands#MAX_SLASH_COMMANDS} slash commands,
     * {@value Commands#MAX_USER_COMMANDS} user context commands, and
     * {@value Commands#MAX_MESSAGE_COMMANDS} message context commands.
     *
     *
     * When a command is not listed in this request, it will be deleted.
     *
     * @param  commands
     * The [commands][CommandData] to add
     *
     * @throws IllegalArgumentException
     * If null or more than
     * {@value Commands#MAX_SLASH_COMMANDS} slash commands,
     * {@value Commands#MAX_USER_COMMANDS} user context commands, or
     * {@value Commands#MAX_MESSAGE_COMMANDS} message context commands, are provided
     *
     * @return The CommandUpdateAction instance, for chaining
     *
     * @see Commands.slash
     * @see Commands.message
     * @see Commands.user
     */
    @Nonnull
    @CheckReturnValue
    fun addCommands(@Nonnull vararg commands: CommandData?): CommandListUpdateAction? {
        Checks.noneNull(commands, "Command")
        return addCommands(Arrays.asList(*commands))
    }
}
