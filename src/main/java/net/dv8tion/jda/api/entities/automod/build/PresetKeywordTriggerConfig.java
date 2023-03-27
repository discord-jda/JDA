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

import net.dv8tion.jda.api.entities.automod.AutoModRule;
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

public class PresetKeywordTriggerConfig extends AbstractKeywordTriggerConfig<PresetKeywordTriggerConfig>
{
    private final EnumSet<AutoModRule.KeywordPreset> presets = EnumSet.noneOf(AutoModRule.KeywordPreset.class);

    protected PresetKeywordTriggerConfig()
    {
        super(AutoModTriggerType.KEYWORD_PRESET);
    }

    @Nonnull
    public PresetKeywordTriggerConfig enablePresets(@Nonnull AutoModRule.KeywordPreset... presets)
    {
        Checks.noneNull(presets, "Presets");
        Collections.addAll(this.presets, presets);
        return this;
    }

    @Nonnull
    public PresetKeywordTriggerConfig enablePresets(@Nonnull Collection<AutoModRule.KeywordPreset> presets)
    {
        Checks.noneNull(presets, "Presets");
        this.presets.addAll(presets);
        return this;
    }

    @Nonnull
    public PresetKeywordTriggerConfig disablePresets(@Nonnull AutoModRule.KeywordPreset... presets)
    {
        Checks.noneNull(presets, "Presets");
        for (AutoModRule.KeywordPreset preset : presets)
            this.presets.remove(preset);
        return this;
    }

    @Nonnull
    public PresetKeywordTriggerConfig disablePresets(@Nonnull Collection<AutoModRule.KeywordPreset> presets)
    {
        Checks.noneNull(presets, "Presets");
        this.presets.removeAll(presets);
        return this;
    }

    @Override
    protected int maxAllowListAmount()
    {
        return AutoModRule.MAX_ALLOWLIST_PRESET_AMOUNT;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject data = super.toData();
        presets.remove(AutoModRule.KeywordPreset.UNKNOWN);
        data.put("presets", DataArray.fromCollection(presets));
        return data;
    }
}
