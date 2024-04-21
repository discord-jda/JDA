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
package net.dv8tion.jda.api.entities.automod.build

import net.dv8tion.jda.api.entities.automod.AutoModRule
import net.dv8tion.jda.api.entities.automod.AutoModRule.KeywordPreset
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.Helpers
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import javax.annotation.Nonnull

/**
 * Configuration for a [KEYWORD_PRESET][AutoModTriggerType.KEYWORD_PRESET] trigger.
 */
class PresetKeywordTriggerConfig :
    AbstractKeywordTriggerConfig<PresetKeywordTriggerConfig?>(AutoModTriggerType.KEYWORD_PRESET) {
    private val presets = EnumSet.noneOf(KeywordPreset::class.java)

    /**
     * Enable the provided keyword preset lists.
     *
     * @param  presets
     * The keyword presets to enable
     *
     * @throws IllegalArgumentException
     * If any of the provided presets is null or [UNKNOWN][net.dv8tion.jda.api.entities.automod.AutoModRule.KeywordPreset.UNKNOWN]
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    fun enablePresets(@Nonnull vararg presets: KeywordPreset?): PresetKeywordTriggerConfig {
        Checks.notNull(presets, "Presets")
        for (preset in presets) checkKnown(preset)
        Collections.addAll(this.presets, *presets)
        return this
    }

    /**
     * Enable the provided keyword preset lists.
     *
     * @param  presets
     * The keyword presets to enable
     *
     * @throws IllegalArgumentException
     * If any of the provided presets is null or [UNKNOWN][net.dv8tion.jda.api.entities.automod.AutoModRule.KeywordPreset.UNKNOWN]
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    fun enablePresets(@Nonnull presets: Collection<KeywordPreset?>): PresetKeywordTriggerConfig {
        Checks.notNull(presets, "Presets")
        presets.forEach(Consumer { preset: KeywordPreset? -> checkKnown(preset) })
        this.presets.addAll(presets)
        return this
    }

    /**
     * Disable the provided keyword preset lists.
     *
     * @param  presets
     * The keyword presets to disable
     *
     * @throws IllegalArgumentException
     * If any of the provided presets is null
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    fun disablePresets(@Nonnull vararg presets: KeywordPreset?): PresetKeywordTriggerConfig {
        Checks.noneNull(presets, "Presets")
        for (preset in presets) this.presets.remove(preset)
        return this
    }

    /**
     * Disable the provided keyword preset lists.
     *
     * @param  presets
     * The keyword presets to disable
     *
     * @throws IllegalArgumentException
     * If any of the provided presets is null
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    fun disablePresets(@Nonnull presets: Collection<KeywordPreset?>?): PresetKeywordTriggerConfig {
        Checks.noneNull(presets, "Presets")
        this.presets.removeAll(presets!!)
        return this
    }

    override fun maxAllowListAmount(): Int {
        return AutoModRule.MAX_ALLOWLIST_PRESET_AMOUNT
    }

    @Nonnull
    override fun toData(): DataObject {
        val data = super.toData()
        data.put(
            "presets", presets.stream().map<Int>(Function<KeywordPreset?, Int> { KeywordPreset.getKey() }).collect(
                Helpers.toDataArray<Int>()
            )
        )
        return data
    }

    companion object {
        private fun checkKnown(preset: KeywordPreset?) {
            Checks.notNull(preset, "Presets")
            Checks.check(preset != KeywordPreset.UNKNOWN, "Cannot use unknown preset")
        }
    }
}
