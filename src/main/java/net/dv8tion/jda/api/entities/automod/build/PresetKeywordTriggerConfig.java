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
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

/**
 * Configuration for a {@link AutoModTriggerType#KEYWORD_PRESET KEYWORD_PRESET} trigger.
 */
public class PresetKeywordTriggerConfig extends AbstractKeywordTriggerConfig<PresetKeywordTriggerConfig>
{
    private final EnumSet<AutoModRule.KeywordPreset> presets = EnumSet.noneOf(AutoModRule.KeywordPreset.class);

    protected PresetKeywordTriggerConfig()
    {
        super(AutoModTriggerType.KEYWORD_PRESET);
    }

    /**
     * Enable the provided keyword preset lists.
     *
     * @param  presets
     *         The keyword presets to enable
     *
     * @throws IllegalArgumentException
     *         If any of the provided presets is null or {@link net.dv8tion.jda.api.entities.automod.AutoModRule.KeywordPreset#UNKNOWN UNKNOWN}
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    public PresetKeywordTriggerConfig enablePresets(@Nonnull AutoModRule.KeywordPreset... presets)
    {
        Checks.notNull(presets, "Presets");
        for (AutoModRule.KeywordPreset preset : presets)
            checkKnown(preset);
        Collections.addAll(this.presets, presets);
        return this;
    }

    /**
     * Enable the provided keyword preset lists.
     *
     * @param  presets
     *         The keyword presets to enable
     *
     * @throws IllegalArgumentException
     *         If any of the provided presets is null or {@link net.dv8tion.jda.api.entities.automod.AutoModRule.KeywordPreset#UNKNOWN UNKNOWN}
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    public PresetKeywordTriggerConfig enablePresets(@Nonnull Collection<AutoModRule.KeywordPreset> presets)
    {
        Checks.notNull(presets, "Presets");
        presets.forEach(PresetKeywordTriggerConfig::checkKnown);
        this.presets.addAll(presets);
        return this;
    }

    /**
     * Disable the provided keyword preset lists.
     *
     * @param  presets
     *         The keyword presets to disable
     *
     * @throws IllegalArgumentException
     *         If any of the provided presets is null
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    public PresetKeywordTriggerConfig disablePresets(@Nonnull AutoModRule.KeywordPreset... presets)
    {
        Checks.noneNull(presets, "Presets");
        for (AutoModRule.KeywordPreset preset : presets)
            this.presets.remove(preset);
        return this;
    }

    /**
     * Disable the provided keyword preset lists.
     *
     * @param  presets
     *         The keyword presets to disable
     *
     * @throws IllegalArgumentException
     *         If any of the provided presets is null
     *
     * @return The current config for chaining convenience
     */
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

    private static void checkKnown(AutoModRule.KeywordPreset preset)
    {
        Checks.notNull(preset, "Presets");
        Checks.check(preset != AutoModRule.KeywordPreset.UNKNOWN, "Cannot use unknown preset");
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject data = super.toData();
        data.put("presets", presets.stream().map(AutoModRule.KeywordPreset::getKey).collect(Helpers.toDataArray()));
        return data;
    }
}
