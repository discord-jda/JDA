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
import net.dv8tion.jda.api.entities.automod.EventType;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.AutoModerationRule rule} updated its event type.
 *
 * <p>Can be used to retrieve the old event type.
 *
 * <p>Identifier: {@code event_type}
 */
public class AutoModerationRuleEventTypeUpdateEvent extends GenericAutoModerationRuleUpdateEvent<EventType>
{
    public AutoModerationRuleEventTypeUpdateEvent(@Nonnull JDA api, long responseNumber, AutoModerationRule rule, AutoModerationField field, EventType oldValue, EventType newValue)
    {
        super(api, responseNumber, rule, field, oldValue, newValue);
    }

    public EventType getOldEventType()
    {
        return getOldValue();
    }

    public EventType getNewEventType()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    public EventType getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public EventType getNewValue()
    {
        return super.getNewValue();
    }
}
