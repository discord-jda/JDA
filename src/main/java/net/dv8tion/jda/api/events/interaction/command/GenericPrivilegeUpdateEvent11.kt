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
package net.dv8tion.jda.api.events.interaction.command

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege
import net.dv8tion.jda.api.interactions.commands.privileges.PrivilegeTargetType
import java.util.*
import javax.annotation.Nonnull

/**
 * Indicates that the privileges of an integration or its commands changed.
 *
 *
 * Can be used to get affected [Guild] and the new [IntegrationPrivileges][IntegrationPrivilege]
 */
abstract class GenericPrivilegeUpdateEvent(
    @Nonnull api: JDA, responseNumber: Long, @Nonnull guild: Guild?,
    /**
     * The target-id.
     *
     *
     * This can either be the id of an integration, or of a command.
     *
     * @return The target-id.
     *
     * @see .getTargetType
     */
    val targetIdLong: Long,
    /**
     * The id of the application of which privileges have been changed.
     *
     * @return id of the application of which privileges have been changed.
     */
    val applicationIdLong: Long, @Nonnull privileges: List<IntegrationPrivilege>?
) : GenericGuildEvent(api, responseNumber, guild) {

    /**
     * The list of new [IntegrationPrivileges][IntegrationPrivilege].
     *
     * @return Unmodifiable list containing the new IntegrationPrivileges.
     */
    @get:Nonnull
    val privileges: List<IntegrationPrivilege>

    init {
        this.privileges = Collections.unmodifiableList(privileges)
    }

    @get:Nonnull
    abstract val targetType: PrivilegeTargetType?

    /**
     * The target-id.
     *
     *
     * This can either be the id of an integration, or of a command.
     *
     * @return The target-id.
     *
     * @see .getTargetType
     */
    @Nonnull
    fun getTargetId(): String {
        return java.lang.Long.toUnsignedString(targetIdLong)
    }

    /**
     * The id of the application of which privileges have been changed.
     *
     * @return id of the application of which privileges have been changed.
     */
    @Nonnull
    fun getApplicationId(): String {
        return java.lang.Long.toUnsignedString(applicationIdLong)
    }
}
