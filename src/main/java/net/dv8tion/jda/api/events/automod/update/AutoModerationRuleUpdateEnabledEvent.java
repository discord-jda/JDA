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

package net.dv8tion.jda.api.events.automod.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.automod.AutoModerationField;
import net.dv8tion.jda.api.entities.automod.AutoModerationRule;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link AutoModerationRule rule} has been enabled/disabled.
 *
 * <p>Can be used to retrieve weather it was enabled or disabled prior to the update.
 *
 * <p>Identifier: {@link AutoModerationField#ENABLED}
 */
public class AutoModerationRuleUpdateEnabledEvent extends GenericAutoModerationUpdateRuleEvent<Boolean>
{
    public AutoModerationRuleUpdateEnabledEvent(@Nonnull JDA api, long responseNumber, AutoModerationRule rule, AutoModerationField field, Boolean oldValue, Boolean newValue)
    {
        super(api, responseNumber, rule, field, oldValue, newValue);
    }

    /**
     * Retrieves weather the rule was enabled or disabled prior to the update.
     *
     * @return True, if the rule was enabled prior to the update. False, if it was disabled.
     */
    public boolean wasEnabled()
    {
        return getOldValue();
    }

    /**
     * Retrieves weather the rule is enabled or disabled after the update.
     *
     * @return True, if the rule is enabled after the update. False, if it is disabled.
     */
    public boolean isEnabled()
    {
        return getNewValue();
    }
}
