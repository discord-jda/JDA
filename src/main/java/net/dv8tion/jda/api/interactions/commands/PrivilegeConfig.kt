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
package net.dv8tion.jda.api.interactions.commands

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import javax.annotation.Nonnull

/**
 * A PrivilegeConfig is the collection of moderator defined [privileges][IntegrationPrivilege] set on a specific application and its commands
 * that define what channels the application can be used in and what users/roles are allowed to use it.
 * These privileges are set by moderators in the guild who have access to the guild's integrations page and permissions to edit them.
 *
 * @see Guild.retrieveCommandPrivileges
 */
class PrivilegeConfig(
    /**
     * The guild in which this PrivilegeConfig is applied in.
     *
     * @return Guild in which this PrivilegeConfig is applied in.
     */
    @get:Nonnull
    @param:Nonnull val guild: Guild, @Nonnull privileges: Map<String?, List<IntegrationPrivilege>>?
) {

    /**
     * Map containing all privileges, with the command-id as the Key, and a List of [IntegrationPrivilege] as Value.
     *
     * <br></br>**If [.getApplicationPrivileges] is not null, this will also contain the privileges applied directly
     * on this application with [SelfUser.getApplicationId] as the Key.**
     *
     * @return Unmodifiable Map containing all privileges on this guild.
     */
    @get:Nonnull
    val asMap: Map<String?, List<IntegrationPrivilege>>

    init {
        asMap = Collections.unmodifiableMap(privileges)
    }

    @get:Nonnull
    val jDA: JDA
        /**
         * The JDA-instance.
         *
         * @return The JDA-instance.
         */
        get() = guild.getJDA()
    val applicationPrivileges: List<IntegrationPrivilege>?
        /**
         * The [IntegrationPrivileges][IntegrationPrivilege] that have been applied to this application in this guild.
         *
         * <br></br>**If the privileges are "Synced" (No custom config applied), this will return null.**
         *
         *
         * This does not include privileges applied to a command itself. Use [.getCommandPrivileges] for that.
         *
         * @return Immutable List containing all IntegrationPrivileges that have been applied to this application in this guild.
         */
        get() = getCommandPrivileges(jDA.getSelfUser().applicationId)

    /**
     * The [IntegrationPrivileges][IntegrationPrivilege] that have been applied to the command with the given id in this guild.
     *
     * <br></br>**If the privileges are "Synced" (No custom config applied), or a command with this id doesn't exist, this will return null.**
     *
     *
     * This does not include privileges applied to the application directly. Use [.getApplicationPrivileges] for that.
     *
     * @param  id
     * The id of the command
     *
     * @throws IllegalArgumentException
     * If the provided id is null
     *
     * @return Immutable List containing all IntegrationPrivileges that have been applied to the command with the given id in this guild.
     */
    fun getCommandPrivileges(@Nonnull id: String?): List<IntegrationPrivilege>? {
        Checks.notNull(id, "Id")
        return asMap[id]
    }

    /**
     * The [IntegrationPrivileges][IntegrationPrivilege] that have been applied to the supplied [Command].
     *
     * <br></br>**If the privileges are "Synced" (No custom config applied), or this command no longer exists, this will return null.**
     *
     *
     * This does not include privileges applied to the application directly. Use [.getApplicationPrivileges] for that.
     *
     * @param  command
     * The [Command] to get the privileges from
     *
     * @throws IllegalArgumentException
     * If the provided command is null
     *
     * @return Immutable List containing all IntegrationPrivileges that have been applied to the command in this guild.
     */
    fun getCommandPrivileges(@Nonnull command: Command): List<IntegrationPrivilege>? {
        Checks.notNull(command, "Command")
        return asMap[command.id]
    }
}
