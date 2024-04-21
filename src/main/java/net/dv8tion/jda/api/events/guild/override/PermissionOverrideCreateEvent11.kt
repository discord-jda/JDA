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
package net.dv8tion.jda.api.events.guild.override

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.PermissionOverride
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer
import javax.annotation.Nonnull

/**
 * Indicates that a [PermissionOverride] in a [guild channel][IPermissionContainer] has been created.
 *
 *
 * Can be used to retrieve the new override.
 */
class PermissionOverrideCreateEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull channel: IPermissionContainer,
    @Nonnull override: PermissionOverride
) : GenericPermissionOverrideEvent(api, responseNumber, channel, override)
