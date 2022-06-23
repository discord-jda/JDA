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
import net.dv8tion.jda.api.entities.AutoModerationRule;
import net.dv8tion.jda.api.entities.automod.AutoModerationField;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.AutoModerationRule rule} has been enabled/disabled.
 *
 * <p>Can be used to retrieve weather it was enabled or disabled prior to the update.
 *
 * <p>Identifier: {@code enabled}
 */
public class AutoModerationRuleEnabledUpdateEvent extends GenericAutoModerationRuleUpdateEvent<Boolean>
{
    public AutoModerationRuleEnabledUpdateEvent(@Nonnull JDA api, long responseNumber, AutoModerationRule rule, AutoModerationField field, Boolean oldValue, Boolean newValue)
    {
        super(api, responseNumber, rule, field, oldValue, newValue);
    }

    public boolean wasEnabled()
    {
        return getOldValue();
    }

    public boolean isEnabled()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    public Boolean getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public Boolean getNewValue()
    {
        return super.getNewValue();
    }
}
