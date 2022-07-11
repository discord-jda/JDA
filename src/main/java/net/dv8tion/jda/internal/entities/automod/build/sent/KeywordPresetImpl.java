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
import net.dv8tion.jda.api.entities.automod.AutoModerationRule;
import net.dv8tion.jda.api.entities.automod.EventType;
import net.dv8tion.jda.api.entities.automod.KeywordPresetType;
import net.dv8tion.jda.api.entities.automod.TriggerType;
import net.dv8tion.jda.api.entities.automod.build.sent.Keyword;
import net.dv8tion.jda.api.entities.automod.build.sent.KeywordPreset;
import net.dv8tion.jda.internal.entities.automod.AutoModerationRuleImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class KeywordPresetImpl implements KeywordPreset
{
    private final AutoModerationRule autoModerationRule;

    public KeywordPresetImpl(String name)
    {
        this.autoModerationRule = new AutoModerationRuleImpl(name);
    }

    @Override
    public KeywordPreset setKeywordPresets(@NotNull EnumSet<KeywordPresetType> keyword)
    {
        return null;
    }

    @NotNull
    @Override
    public KeywordPreset setEventType(@NotNull EventType eventType)
    {
        autoModerationRule.setEventType(eventType);
        return this;
    }

    @NotNull
    @Override
    public KeywordPreset setTriggerType(@NotNull TriggerType triggerType)
    {
        autoModerationRule.setTriggerType(triggerType);
        return this;
    }

    @NotNull
    @Override
    public KeywordPreset setEnabled(boolean enabled)
    {
        autoModerationRule.setEnabled(enabled);
        return this;
    }

    @Nullable
    @Override
    public KeywordPreset setExemptRoles(@NotNull List<Role> exemptRoles)
    {
        autoModerationRule.setExemptRoles(exemptRoles);
        return this;
    }

    @Nullable
    @Override
    public KeywordPreset setExemptChannels(@NotNull List<GuildChannel> exemptChannels)
    {
        autoModerationRule.setExemptChannels(exemptChannels);
        return this;
    }
}
