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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.automod.AutoModerationField;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.AutoModerationRule rule} updated its creator(user).
 *
 * <p>Can be used to retrieve the old creator(user).
 *
 * <p>Identifier: {@code user_id}
 */
public class AutoModerationRuleUserUpdateEvent extends GenericAutoModerationRuleUpdateEvent<User>
{
    public AutoModerationRuleUserUpdateEvent(@NotNull JDA api, long responseNumber, AutoModerationRule rule, AutoModerationField field, User oldValue, User newValue)
    {
        super(api, responseNumber, rule, field, oldValue, newValue);
    }

    public User getOldUser()
    {
        return getOldValue();
    }

    public User getNewUser()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    public User getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public User getNewValue()
    {
        return super.getNewValue();
    }
}
