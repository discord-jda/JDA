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
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.automod.AutoModerationField;
import net.dv8tion.jda.api.entities.automod.AutoModerationRule;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Indicates that a {@link AutoModerationRule rule} updated the exempt channels.
 *
 * <p>Can be used to retrieve the old exempt channels.
 *
 * <p>Identifier: {@code exempt_channels}
 */
public class AutoModerationRuleUpdateExemptChannelsEvent extends GenericAutoModerationUpdateRuleEvent<List<GuildChannel>>
{
    public AutoModerationRuleUpdateExemptChannelsEvent(@Nonnull JDA api, long responseNumber, AutoModerationRule rule, AutoModerationField field, List<GuildChannel> oldValue, List<GuildChannel> newValue)
    {
        super(api, responseNumber, rule, field, oldValue, newValue);
    }

    public List<GuildChannel> getOldExemptChannels()
    {
        return getOldValue();
    }

    public List<GuildChannel> getNewExemptChannels()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    public List<GuildChannel> getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public List<GuildChannel> getNewValue()
    {
        return super.getNewValue();
    }
}
