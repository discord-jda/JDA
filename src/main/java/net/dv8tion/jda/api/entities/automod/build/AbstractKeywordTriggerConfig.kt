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
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import java.util.function.Consumer
import javax.annotation.Nonnull

/**
 * Abstract for all keyword trigger types.
 *
 * @param <B>
 * The builder type
</B> */
abstract class AbstractKeywordTriggerConfig<B : AbstractKeywordTriggerConfig<B>?> protected constructor(triggerType: AutoModTriggerType) :
    AbstractTriggerConfig<B>(triggerType) {
    protected val allowList: MutableList<String?> = ArrayList()

    /**
     * Add keywords to the allow list.
     *
     * Keywords added to the allow list will not be considered as a match and won't trigger the rule execution.
     *
     *
     * Keywords follow the same rules as [CustomKeywordTriggerConfig.addKeywords].
     *
     * @param  keywords
     * The keywords to allow
     *
     * @throws IllegalArgumentException
     *
     *  * If any of the keywords is empty, blank, or null
     *  * If more than the allowed number of keywords are added to the list
     * ({@value AutoModRule#MAX_ALLOWLIST_CUSTOM_AMOUNT} for custom keyword lists,
     * {@value AutoModRule#MAX_ALLOWLIST_PRESET_AMOUNT} for preset keyword lists)
     *  * If any keyword is longer than [AutoModRule.MAX_KEYWORD_LENGTH]
     *
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    fun addAllowList(@Nonnull vararg keywords: String): B {
        Checks.noneNull(keywords, "Keywords")
        Checks.check(
            allowList.size + keywords.size <= maxAllowListAmount(),
            "Cannot add more than %d keywords!",
            maxAllowListAmount()
        )
        Arrays.stream(keywords).forEach { keyword: String? -> checkKeyword(keyword) }
        Collections.addAll(allowList, *keywords)
        return this as B
    }

    /**
     * Add keywords to the allow list.
     *
     * Keywords added to the allow list will not be considered as a match and won't trigger the rule execution.
     *
     *
     * Keywords follow the same rules as [CustomKeywordTriggerConfig.addKeywords].
     *
     * @param  keywords
     * The keywords to allow
     *
     * @throws IllegalArgumentException
     *
     *  * If any of the keywords is empty, blank, or null
     *  * If more than the allowed number of keywords are added to the list
     * ({@value AutoModRule#MAX_ALLOWLIST_CUSTOM_AMOUNT} for custom keyword lists,
     * {@value AutoModRule#MAX_ALLOWLIST_PRESET_AMOUNT} for preset keyword lists)
     *  * If any keyword is longer than [AutoModRule.MAX_KEYWORD_LENGTH]
     *
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    fun addAllowList(@Nonnull keywords: Collection<String?>): B {
        Checks.noneNull(keywords, "Keywords")
        Checks.check(
            allowList.size + keywords.size <= maxAllowListAmount(),
            "Cannot add more than %d keywords!",
            maxAllowListAmount()
        )
        keywords.forEach(Consumer { keyword: String? -> checkKeyword(keyword) })
        allowList.addAll(keywords)
        return this as B
    }

    /**
     * Change the allow list to the provided keywords.
     *
     * Keywords added to the allow list will not be considered as a match and won't trigger the rule execution.
     *
     *
     * Keywords follow the same rules as [CustomKeywordTriggerConfig.addKeywords].
     *
     * @param  keywords
     * The keywords to allow
     *
     * @throws IllegalArgumentException
     *
     *  * If any of the keywords is empty, blank, or null
     *  * If more than the allowed number of keywords are added to the list
     * ({@value AutoModRule#MAX_ALLOWLIST_CUSTOM_AMOUNT} for custom keyword lists,
     * {@value AutoModRule#MAX_ALLOWLIST_PRESET_AMOUNT} for preset keyword lists)
     *  * If any keyword is longer than [AutoModRule.MAX_KEYWORD_LENGTH]
     *
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    fun setAllowList(@Nonnull keywords: Collection<String?>): B {
        Checks.noneNull(keywords, "Keywords")
        Checks.check(keywords.size <= maxAllowListAmount(), "Cannot add more than %d keywords!", maxAllowListAmount())
        keywords.forEach(Consumer { keyword: String? -> checkKeyword(keyword) })
        allowList.clear()
        allowList.addAll(keywords)
        return this as B
    }

    protected abstract fun maxAllowListAmount(): Int
    @Nonnull
    override fun toData(): DataObject {
        val data = super.toData()
        data.put("allow_list", DataArray.fromCollection(allowList))
        return data
    }

    companion object {
        protected fun checkKeyword(keyword: String?) {
            Checks.notEmpty(keyword, "Keyword")
            Checks.notLonger(keyword, AutoModRule.MAX_KEYWORD_LENGTH, "Keyword")
        }
    }
}
