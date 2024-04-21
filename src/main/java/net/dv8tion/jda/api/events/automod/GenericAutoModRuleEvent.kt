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
package net.dv8tion.jda.api.events.automod

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.automod.AutoModRule
import net.dv8tion.jda.api.events.Event
import javax.annotation.Nonnull

/**
 * Indicates that an [AutoModRule] was created/removed/updated.
 *
 *
 * **Requirements**<br></br>
 *
 *
 * These events require the [AUTO_MODERATION_CONFIGURATION][net.dv8tion.jda.api.requests.GatewayIntent.AUTO_MODERATION_CONFIGURATION] intent to be enabled.
 * <br></br>These events will only fire for guilds where the bot has the [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] permission.
 */
open class GenericAutoModRuleEvent(
    @Nonnull api: JDA, responseNumber: Long,
    /**
     * The [AutoModRule] that was created/removed/updated.
     *
     * @return The [AutoModRule]
     */
    @get:Nonnull
    @param:Nonnull val rule: AutoModRule?
) : Event(api, responseNumber)
