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

package net.dv8tion.jda.api.entities.automod.build;

import net.dv8tion.jda.api.entities.automod.AutoModEventType;
import net.dv8tion.jda.api.entities.automod.AutoModResponse;
import net.dv8tion.jda.api.entities.automod.AutoModRule;
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.List;

public class AutoModRuleData implements SerializableData
{
    private final AutoModTriggerType triggerType;
    private final AutoModEventType eventType;
    private final String name;

    private boolean enabled = true;
    private int mentionLimit = -1;
    private List<AutoModResponse> actions;
    private long[] exemptRoles;
    private long[] exemptChannels;
    private List<String> filteredKeywords;
    private List<String> filteredRegex;
    private EnumSet<AutoModRule.KeywordPreset> filteredPresets;
    private List<String> allowlist;

    protected AutoModRuleData(AutoModTriggerType triggerType, AutoModEventType eventType, String name)
    {
        this.triggerType = triggerType;
        this.eventType = eventType;
        this.name = name;
    }

    protected AutoModRuleData setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    protected AutoModRuleData setMentionLimit(int mentionLimit)
    {
        this.mentionLimit = mentionLimit;
        return this;
    }

    protected AutoModRuleData setActions(List<AutoModResponse> actions)
    {
        this.actions = actions;
        return this;
    }

    protected AutoModRuleData setExemptRoles(long... exemptRoles)
    {
        this.exemptRoles = exemptRoles;
        return this;
    }

    protected AutoModRuleData setExemptChannels(long... exemptChannels)
    {
        this.exemptChannels = exemptChannels;
        return this;
    }

    protected AutoModRuleData setFilteredKeywords(List<String> filteredKeywords)
    {
        this.filteredKeywords = filteredKeywords;
        return this;
    }

    protected AutoModRuleData setFilteredRegex(List<String> filteredRegex)
    {
        this.filteredRegex = filteredRegex;
        return this;
    }

    protected AutoModRuleData setFilteredPresets(EnumSet<AutoModRule.KeywordPreset> filteredPresets)
    {
        this.filteredPresets = filteredPresets;
        return this;
    }

    protected AutoModRuleData setAllowlist(List<String> allowlist)
    {
        this.allowlist = allowlist;
        return this;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject data = DataObject.empty()
                .put("name", name)
                .put("trigger_type", triggerType.getKey())
                .put("event_type", eventType.getKey());

        data.put("enabled", enabled);

        if (actions != null)
            data.put("actions", DataArray.fromCollection(actions));
        else
            data.put("actions", DataArray.empty());

        if (exemptRoles != null)
        {
            DataArray array = DataArray.empty();
            for (long id : exemptRoles)
                array.add(id);
            data.put("exempt_roles", array);
        }
        else
        {
            data.put("exempt_roles", DataArray.empty());
        }

        if (exemptChannels != null)
        {
            DataArray array = DataArray.empty();
            for (long id : exemptChannels)
                array.add(id);
            data.put("exempt_channels", array);
        }
        else
        {
            data.put("exempt_channels", DataArray.empty());
        }

        DataObject metadata = DataObject.empty();

        switch (triggerType)
        {
        case MENTION:
            if (mentionLimit != -1)
                metadata.put("mention_total_limit", mentionLimit);
            break;
        case SPAM:
            break;
        case KEYWORD:
            if (filteredKeywords != null)
                metadata.put("filtered_keywords", DataArray.fromCollection(filteredKeywords));
            else
                metadata.put("filtered_keywords", DataArray.empty());
            if (filteredRegex != null)
                metadata.put("filtered_regex", DataArray.fromCollection(filteredRegex));
            else
                metadata.put("filtered_regex", DataArray.empty());
            if (allowlist != null)
                metadata.put("allow_list", DataArray.fromCollection(allowlist));
            else
                metadata.put("allow_list", DataArray.empty());
            break;
        case KEYWORD_PRESET:
            if (filteredPresets != null)
                metadata.put("filtered_presets", DataArray.fromCollection(filteredPresets));
            else
                metadata.put("filtered_presets", DataArray.empty());
            if (allowlist != null)
                metadata.put("allow_list", DataArray.fromCollection(allowlist));
            else
                metadata.put("allow_list", DataArray.empty());
            break;
        }

        data.put("trigger_metadata", metadata);

        return data;
    }
}
