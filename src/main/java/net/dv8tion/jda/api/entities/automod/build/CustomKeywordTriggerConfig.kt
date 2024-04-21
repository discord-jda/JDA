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
import javax.annotation.Nonnull

/**
 * Configuration for a [KEYWORD][net.dv8tion.jda.api.entities.automod.AutoModTriggerType.KEYWORD] trigger.
 */
class CustomKeywordTriggerConfig :
    AbstractKeywordTriggerConfig<CustomKeywordTriggerConfig?>(AutoModTriggerType.KEYWORD) {
    protected val keywords: MutableSet<String?> = HashSet()
    protected val patterns: MutableSet<String?> = HashSet()

    /**
     * Add more keywords match against.
     * <br></br>Keywords are matched case-insensitively, and may also contain whitespace.
     *
     *
     * You can use wildcards at the keyword boundaries to extend the matches:
     * <br></br>`"foo*"` can match `"foo"`, `"foobar"`, `"foo-bar"`, etc.
     * <br></br>`"*foo*"` can match `"foo"`, `"foobar"`, `"barfoo"`, etc.
     * <br></br>`"*foo"` can match `"foo"`, `"barfoo"`, `"bar-foo"`, etc.
     *
     *
     * You can also use regex patterns using [.patternFilter].
     *
     * @param  keywords
     * The keywords to match
     *
     * @throws IllegalArgumentException
     *
     *  * If any of the keywords are empty, blank, or null
     *  * If more than {@value AutoModRule#MAX_KEYWORD_AMOUNT} keywords are added
     *  * If any of the keywords is longer than {@value AutoModRule#MAX_KEYWORD_LENGTH} characters
     *
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    fun addKeywords(@Nonnull vararg keywords: String?): CustomKeywordTriggerConfig {
        Checks.noneNull(keywords, "Keywords")
        Checks.check(
            this.keywords.size + keywords.size <= AutoModRule.MAX_KEYWORD_AMOUNT,
            "Cannot add more than %d keywords!",
            AutoModRule.MAX_KEYWORD_AMOUNT
        )
        for (keyword in keywords) AbstractKeywordTriggerConfig.Companion.checkKeyword(keyword)
        Collections.addAll(this.keywords, *keywords)
        return this
    }

    /**
     * Add more keywords match against.
     * <br></br>Keywords are matched case-insensitively, and may also contain whitespace.
     *
     *
     * You can use wildcards at the keyword boundaries to extend the matches:
     * <br></br>`"foo*"` can match `"foo"`, `"foobar"`, `"foo-bar"`, etc.
     * <br></br>`"*foo*"` can match `"foo"`, `"foobar"`, `"barfoo"`, etc.
     * <br></br>`"*foo"` can match `"foo"`, `"barfoo"`, `"bar-foo"`, etc.
     *
     *
     * You can also use regex patterns using [.patternFilter].
     *
     * @param  keywords
     * The keywords to match
     *
     * @throws IllegalArgumentException
     *
     *  * If any of the keywords are empty, blank, or null
     *  * If more than {@value AutoModRule#MAX_KEYWORD_AMOUNT} keywords are added
     *  * If any of the keywords is longer than {@value AutoModRule#MAX_KEYWORD_LENGTH} characters
     *
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    fun addKeywords(@Nonnull keywords: Collection<String?>): CustomKeywordTriggerConfig {
        Checks.noneNull(keywords, "Keywords")
        Checks.check(
            this.keywords.size + keywords.size <= AutoModRule.MAX_KEYWORD_AMOUNT,
            "Cannot add more than %d keywords!",
            AutoModRule.MAX_KEYWORD_AMOUNT
        )
        for (keyword in keywords) AbstractKeywordTriggerConfig.Companion.checkKeyword(keyword)
        this.keywords.addAll(keywords)
        return this
    }

    /**
     * Changes the keywords to match against to the new list.
     * <br></br>Keywords are matched case-insensitively, and may also contain whitespace.
     *
     *
     * You can use wildcards at the keyword boundaries to extend the matches:
     * <br></br>`"foo*"` can match `"foo"`, `"foobar"`, `"foo-bar"`, etc.
     * <br></br>`"*foo*"` can match `"foo"`, `"foobar"`, `"barfoo"`, etc.
     * <br></br>`"*foo"` can match `"foo"`, `"barfoo"`, `"bar-foo"`, etc.
     *
     *
     * You can also use regex patterns using [.patternFilter].
     *
     * @param  keywords
     * The keywords to match
     *
     * @throws IllegalArgumentException
     *
     *  * If any of the keywords are empty, blank, or null
     *  * If more than {@value AutoModRule#MAX_KEYWORD_AMOUNT} keywords are added
     *  * If any of the keywords is longer than {@value AutoModRule#MAX_KEYWORD_LENGTH} characters
     *
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    fun setKeywords(@Nonnull keywords: Collection<String?>): CustomKeywordTriggerConfig {
        Checks.noneNull(keywords, "Keywords")
        Checks.check(
            keywords.size <= AutoModRule.MAX_KEYWORD_AMOUNT,
            "Cannot add more than %d keywords!",
            AutoModRule.MAX_KEYWORD_AMOUNT
        )
        for (keyword in keywords) AbstractKeywordTriggerConfig.Companion.checkKeyword(keyword)
        this.keywords.clear()
        this.keywords.addAll(keywords)
        return this
    }

    /**
     * Add keywords regex patterns to match against.
     * <br></br>Keyword patterns are matched case-insensitively, and may also contain whitespace.
     *
     *
     * Patterns may use anything supported by the rust regex crate.
     * You can use a validator such as [Rustexp](https://rustexp.lpil.uk/) to validate your pattern.
     *
     *
     * You can also use simple substring keywords using [.keywordFilter].
     *
     * @param  patterns
     * The keyword patterns to match
     *
     * @throws IllegalArgumentException
     *
     *  * If any of the patterns are empty, blank, or null
     *  * If more than {@value AutoModRule#MAX_PATTERN_AMOUNT} patterns are added
     *  * If any of the patterns is longer than {@value AutoModRule#MAX_PATTERN_LENGTH} characters
     *
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    fun addPatterns(@Nonnull vararg patterns: String?): CustomKeywordTriggerConfig {
        Checks.noneNull(patterns, "Patterns")
        Checks.check(
            this.patterns.size + patterns.size <= AutoModRule.MAX_PATTERN_AMOUNT,
            "Cannot add more than %d patterns!",
            AutoModRule.MAX_PATTERN_AMOUNT
        )
        for (pattern in patterns) checkPattern(pattern)
        Collections.addAll(this.patterns, *patterns)
        return this
    }

    /**
     * Add keywords regex patterns to match against.
     * <br></br>Keyword patterns are matched case-insensitively, and may also contain whitespace.
     *
     *
     * Patterns may use anything supported by the rust regex crate.
     * You can use a validator such as [Rustexp](https://rustexp.lpil.uk/) to validate your pattern.
     *
     *
     * You can also use simple substring keywords using [.keywordFilter].
     *
     * @param  patterns
     * The keyword patterns to match
     *
     * @throws IllegalArgumentException
     *
     *  * If any of the patterns are empty, blank, or null
     *  * If more than {@value AutoModRule#MAX_PATTERN_AMOUNT} patterns are added
     *  * If any of the patterns is longer than {@value AutoModRule#MAX_PATTERN_LENGTH} characters
     *
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    fun addPatterns(@Nonnull patterns: Collection<String?>): CustomKeywordTriggerConfig {
        Checks.noneNull(patterns, "Patterns")
        Checks.check(
            this.patterns.size + patterns.size <= AutoModRule.MAX_PATTERN_AMOUNT,
            "Cannot add more than %d patterns!",
            AutoModRule.MAX_PATTERN_AMOUNT
        )
        for (pattern in patterns) checkPattern(pattern)
        this.patterns.addAll(patterns)
        return this
    }

    /**
     * Change the list of keywords regex patterns to match against.
     * <br></br>Keyword patterns are matched case-insensitively, and may also contain whitespace.
     *
     *
     * Patterns may use anything supported by the rust regex crate.
     * You can use a validator such as [Rustexp](https://rustexp.lpil.uk/) to validate your pattern.
     *
     *
     * You can also use simple substring keywords using [.keywordFilter].
     *
     * @param  patterns
     * The keyword patterns to match
     *
     * @throws IllegalArgumentException
     *
     *  * If any of the patterns are empty, blank, or null
     *  * If more than {@value AutoModRule#MAX_PATTERN_AMOUNT} patterns are added
     *  * If any of the patterns is longer than {@value AutoModRule#MAX_PATTERN_LENGTH} characters
     *
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    fun setPatterns(@Nonnull patterns: Collection<String?>): CustomKeywordTriggerConfig {
        Checks.noneNull(patterns, "Patterns")
        Checks.check(
            patterns.size <= AutoModRule.MAX_PATTERN_AMOUNT,
            "Cannot add more than %d patterns!",
            AutoModRule.MAX_PATTERN_AMOUNT
        )
        for (pattern in patterns) checkPattern(pattern)
        this.patterns.clear()
        this.patterns.addAll(patterns)
        return this
    }

    override fun maxAllowListAmount(): Int {
        return AutoModRule.MAX_ALLOWLIST_CUSTOM_AMOUNT
    }

    @Nonnull
    override fun toData(): DataObject {
        Checks.check(!keywords.isEmpty() || !patterns.isEmpty(), "Must have at least one keyword or pattern!")
        val data = super.toData()
        data.put("keyword_filter", DataArray.fromCollection(keywords))
        data.put("regex_patterns", DataArray.fromCollection(patterns))
        return data
    }

    companion object {
        protected fun checkPattern(pattern: String?) {
            Checks.notBlank(pattern, "Pattern")
            Checks.notLonger(pattern, AutoModRule.MAX_PATTERN_LENGTH, "Pattern")
        }
    }
}
