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

package net.dv8tion.jda.api.events.automod;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.automod.AutoModRule;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link AutoModRule} was deleted.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require the {@link net.dv8tion.jda.api.requests.GatewayIntent#AUTO_MODERATION_CONFIGURATION AUTO_MODERATION_CONFIGURATION} intent to be enabled.
 */
public class AutoModRuleDeleteEvent extends GenericAutoModRuleEvent
{
    public AutoModRuleDeleteEvent(@Nonnull JDA api, long responseNumber, @Nonnull AutoModRule rule)
    {
        super(api, responseNumber, rule);
    }
}
