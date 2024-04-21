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
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege
import net.dv8tion.jda.api.interactions.commands.privileges.PrivilegeTargetType
import javax.annotation.Nonnull

/**
 * Indicates that the [Privileges][IntegrationPrivilege] of an application changed.
 * <br></br>If the moderator updates the privileges of a specific command, a [ApplicationCommandUpdatePrivilegesEvent] will be fired instead.
 *
 *
 * Can be used to get affected Guild and [List] of new [Privileges][IntegrationPrivilege]
 */
class ApplicationUpdatePrivilegesEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull guild: Guild?,
    applicationId: Long,
    @Nonnull privileges: List<IntegrationPrivilege>?
) : GenericPrivilegeUpdateEvent(api, responseNumber, guild, applicationId, applicationId, privileges) {
    @get:Nonnull
    override val targetType: PrivilegeTargetType?
        get() = PrivilegeTargetType.INTEGRATION
}
