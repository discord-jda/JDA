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

package net.dv8tion.jda.internal.entities.automod.build.sent;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.automod.*;
import net.dv8tion.jda.api.entities.automod.build.sent.GenericKeyWord;
import net.dv8tion.jda.internal.entities.automod.ActionMetadataImpl;
import net.dv8tion.jda.internal.entities.automod.AutoModerationActionImpl;
import net.dv8tion.jda.internal.entities.automod.AutoModerationRuleImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GenericKeyWordImpl implements GenericKeyWord
{
    private final AutoModerationRule autoModerationRule;

    public GenericKeyWordImpl(String name)
    {
        this.autoModerationRule = new AutoModerationRuleImpl(name);
    }

    @Nonnull
    @Override
    public GenericKeyWord setEventType(@Nonnull EventType eventType)
    {
        autoModerationRule.setEventType(eventType);
        return this;
    }

    @Nonnull
    @Override
    public GenericKeyWord setTriggerType(@Nonnull TriggerType triggerType)
    {
        autoModerationRule.setTriggerType(triggerType);
        return this;
    }

    @Nonnull
    @Override
    public GenericKeyWord setEnabled(boolean enabled)
    {
        autoModerationRule.setEnabled(enabled);
        return this;
    }

    @Nonnull
    @Override
    public GenericKeyWord setAction(@Nonnull AutoModerationActionType type, @Nullable GuildChannel channel, @Nullable Duration duration)
    {
        List<AutoModerationAction> actions = new ArrayList<>();

        AutoModerationAction action = new AutoModerationActionImpl();

        action.setType(type);

        ActionMetadata metadata = new ActionMetadataImpl();


        if (channel != null)
            metadata.setChannel(channel);

        if (duration != null)
            metadata.setDuration(duration);

        action.setActionMetadata(metadata);

        actions.add(action);

        autoModerationRule.setActions(actions);
        return this;
    }

    @Nullable
    @Override
    public GenericKeyWord setExemptRoles(@Nonnull List<Role> exemptRoles)
    {
        autoModerationRule.setExemptRoles(exemptRoles);
        return this;
    }

    @Nullable
    @Override
    public GenericKeyWord setExemptChannels(@Nonnull List<GuildChannel> exemptChannels)
    {
        autoModerationRule.setExemptChannels(exemptChannels);
        return this;
    }
}
