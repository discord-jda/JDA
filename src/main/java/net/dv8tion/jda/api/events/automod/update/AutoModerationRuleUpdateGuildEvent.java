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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.automod.AutoModerationField;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.AutoModerationRule rule} updated the guild.
 *
 * <p>Can be used to retrieve the old guild.
 *
 * <p>Identifier: {@code guild_id}
 */
public class AutoModerationRuleUpdateGuildEvent extends GenericAutoModerationUpdateRuleEvent<Guild>
{
    public AutoModerationRuleUpdateGuildEvent(@Nonnull JDA api, long responseNumber, AutoModerationRule rule, AutoModerationField field, Guild oldValue, Guild newValue)
    {
        super(api, responseNumber, rule, field, oldValue, newValue);
    }

    public Guild getOldGuild()
    {
        return getOldValue();
    }

    public Guild getNewGuild()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    public Guild getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public Guild getNewValue()
    {
        return super.getNewValue();
    }

    @NotNull
    @Override
    public AutoModerationRule getEntity()
    {
        return getRule();
    }
}
