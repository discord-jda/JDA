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

package net.dv8tion.jda.api.events.automoderation.update;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.AutoModerationField;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.events.automoderation.GenericAutoModerationEvent;
import net.dv8tion.jda.api.entities.AutoModerationRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GenericAutoModerationRuleUpdateEvent<T> extends GenericAutoModerationEvent implements UpdateEvent<AutoModerationRule, T> {

    protected final AutoModerationField field;
    protected final T oldValue;
    protected final T newValue;

    public GenericAutoModerationRuleUpdateEvent(@NotNull JDA api, long responseNumber, AutoModerationRule rule, AutoModerationField field, T oldValue, T newValue) {
        super(api, responseNumber, rule);
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @NotNull
    @Override
    public String getPropertyIdentifier() {
        return field.getFieldName();
    }

    @NotNull
    @Override
    public AutoModerationRule getEntity() {
        return getRule();
    }

    @Nullable
    @Override
    public T getOldValue() {
        return oldValue;
    }

    @Nullable
    @Override
    public T getNewValue() {
        return newValue;
    }
}
