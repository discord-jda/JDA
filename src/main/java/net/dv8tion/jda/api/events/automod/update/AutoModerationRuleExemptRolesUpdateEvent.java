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
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.automod.AutoModerationField;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.AutoModerationRule rule} updated the exempt roles.
 *
 * <p>Can be used to retrieve the old exempt roles.
 *
 * <p>Identifier: {@code exempt_roles}
 */
public class AutoModerationRuleExemptRolesUpdateEvent extends GenericAutoModerationRuleUpdateEvent<List<Role>>
{
    public AutoModerationRuleExemptRolesUpdateEvent(@Nonnull JDA api, long responseNumber, AutoModerationRule rule, AutoModerationField field, List<Role> oldValue, List<Role> newValue)
    {
        super(api, responseNumber, rule, field, oldValue, newValue);
    }

    public List<Role> getOldExemptRoles()
    {
        return getOldValue();
    }

    public List<Role> getNewExemptRoles()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    public List<Role> getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public List<Role> getNewValue()
    {
        return super.getNewValue();
    }
}
