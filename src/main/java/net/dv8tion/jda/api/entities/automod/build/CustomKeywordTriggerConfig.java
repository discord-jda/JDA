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
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for a {@link net.dv8tion.jda.api.entities.automod.AutoModTriggerType#KEYWORD KEYWORD} trigger.
 */
public class CustomKeywordTriggerConfig extends AbstractKeywordTriggerConfig<CustomKeywordTriggerConfig>
{
    protected final Set<String> keywords = new HashSet<>();
    protected final Set<String> patterns = new HashSet<>();

    protected CustomKeywordTriggerConfig()
    {
        super(AutoModTriggerType.KEYWORD);
    }

    /**
     * Add more keywords match against.
     * <br>Keywords are matched case-insensitively, and may also contain whitespace.
     *
     * <p>You can use wildcards at the keyword boundaries to extend the matches:
     * <br>{@code "foo*"} can match {@code "foo"}, {@code "foobar"}, {@code "foo-bar"}, etc.
     * <br>{@code "*foo*"} can match {@code "foo"}, {@code "foobar"}, {@code "barfoo"}, etc.
     * <br>{@code "*foo"} can match {@code "foo"}, {@code "barfoo"}, {@code "bar-foo"}, etc.
     *
     * <p>You can also use regex patterns using {@link #patternFilter(String...)}.
     *
     * @param  keywords
     *         The keywords to match
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the keywords are empty, blank, or null</li>
     *             <li>If more than {@value AutoModRule#MAX_KEYWORD_AMOUNT} keywords are added</li>
     *             <li>If any of the keywords is longer than {@value AutoModRule#MAX_KEYWORD_LENGTH} characters</li>
     *         </ul>
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    public CustomKeywordTriggerConfig addKeywords(@Nonnull String... keywords)
    {
        Checks.noneNull(keywords, "Keywords");
        Checks.check(this.keywords.size() + keywords.length <= AutoModRule.MAX_KEYWORD_AMOUNT, "Cannot add more than %d keywords!", AutoModRule.MAX_KEYWORD_AMOUNT);
        for (String keyword : keywords)
            checkKeyword(keyword);

        Collections.addAll(this.keywords, keywords);
        return this;
    }

    /**
     * Add more keywords match against.
     * <br>Keywords are matched case-insensitively, and may also contain whitespace.
     *
     * <p>You can use wildcards at the keyword boundaries to extend the matches:
     * <br>{@code "foo*"} can match {@code "foo"}, {@code "foobar"}, {@code "foo-bar"}, etc.
     * <br>{@code "*foo*"} can match {@code "foo"}, {@code "foobar"}, {@code "barfoo"}, etc.
     * <br>{@code "*foo"} can match {@code "foo"}, {@code "barfoo"}, {@code "bar-foo"}, etc.
     *
     * <p>You can also use regex patterns using {@link #patternFilter(Collection)}.
     *
     * @param  keywords
     *         The keywords to match
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the keywords are empty, blank, or null</li>
     *             <li>If more than {@value AutoModRule#MAX_KEYWORD_AMOUNT} keywords are added</li>
     *             <li>If any of the keywords is longer than {@value AutoModRule#MAX_KEYWORD_LENGTH} characters</li>
     *         </ul>
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    public CustomKeywordTriggerConfig addKeywords(@Nonnull Collection<String> keywords)
    {
        Checks.noneNull(keywords, "Keywords");
        Checks.check(this.keywords.size() + keywords.size() <= AutoModRule.MAX_KEYWORD_AMOUNT, "Cannot add more than %d keywords!", AutoModRule.MAX_KEYWORD_AMOUNT);
        for (String keyword : keywords)
            checkKeyword(keyword);

        this.keywords.addAll(keywords);
        return this;
    }

    /**
     * Changes the keywords to match against to the new list.
     * <br>Keywords are matched case-insensitively, and may also contain whitespace.
     *
     * <p>You can use wildcards at the keyword boundaries to extend the matches:
     * <br>{@code "foo*"} can match {@code "foo"}, {@code "foobar"}, {@code "foo-bar"}, etc.
     * <br>{@code "*foo*"} can match {@code "foo"}, {@code "foobar"}, {@code "barfoo"}, etc.
     * <br>{@code "*foo"} can match {@code "foo"}, {@code "barfoo"}, {@code "bar-foo"}, etc.
     *
     * <p>You can also use regex patterns using {@link #patternFilter(Collection)}.
     *
     * @param  keywords
     *         The keywords to match
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the keywords are empty, blank, or null</li>
     *             <li>If more than {@value AutoModRule#MAX_KEYWORD_AMOUNT} keywords are added</li>
     *             <li>If any of the keywords is longer than {@value AutoModRule#MAX_KEYWORD_LENGTH} characters</li>
     *         </ul>
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    public CustomKeywordTriggerConfig setKeywords(@Nonnull Collection<String> keywords)
    {
        Checks.noneNull(keywords, "Keywords");
        Checks.check(keywords.size() <= AutoModRule.MAX_KEYWORD_AMOUNT, "Cannot add more than %d keywords!", AutoModRule.MAX_KEYWORD_AMOUNT);
        for (String keyword : keywords)
            checkKeyword(keyword);

        this.keywords.clear();
        this.keywords.addAll(keywords);
        return this;
    }


    /**
     * Add keywords regex patterns to match against.
     * <br>Keyword patterns are matched case-insensitively, and may also contain whitespace.
     *
     * <p>Patterns may use anything supported by the rust regex crate.
     * You can use a validator such as <a href="https://rustexp.lpil.uk/" target="_blank">Rustexp</a> to validate your pattern.
     *
     * <p>You can also use simple substring keywords using {@link #keywordFilter(String...)}.
     *
     * @param  patterns
     *         The keyword patterns to match
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the patterns are empty, blank, or null</li>
     *             <li>If more than {@value AutoModRule#MAX_PATTERN_AMOUNT} patterns are added</li>
     *             <li>If any of the patterns is longer than {@value AutoModRule#MAX_PATTERN_LENGTH} characters</li>
     *         </ul>
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    public CustomKeywordTriggerConfig addPatterns(@Nonnull String... patterns)
    {
        Checks.noneNull(patterns, "Patterns");
        Checks.check(this.patterns.size() + patterns.length <= AutoModRule.MAX_PATTERN_AMOUNT, "Cannot add more than %d patterns!", AutoModRule.MAX_PATTERN_AMOUNT);
        for (String pattern : patterns)
            checkPattern(pattern);

        Collections.addAll(this.patterns, patterns);
        return this;
    }

    /**
     * Add keywords regex patterns to match against.
     * <br>Keyword patterns are matched case-insensitively, and may also contain whitespace.
     *
     * <p>Patterns may use anything supported by the rust regex crate.
     * You can use a validator such as <a href="https://rustexp.lpil.uk/" target="_blank">Rustexp</a> to validate your pattern.
     *
     * <p>You can also use simple substring keywords using {@link #keywordFilter(String...)}.
     *
     * @param  patterns
     *         The keyword patterns to match
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the patterns are empty, blank, or null</li>
     *             <li>If more than {@value AutoModRule#MAX_PATTERN_AMOUNT} patterns are added</li>
     *             <li>If any of the patterns is longer than {@value AutoModRule#MAX_PATTERN_LENGTH} characters</li>
     *         </ul>
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    public CustomKeywordTriggerConfig addPatterns(@Nonnull Collection<String> patterns)
    {
        Checks.noneNull(patterns, "Patterns");
        Checks.check(this.patterns.size() + patterns.size() <= AutoModRule.MAX_PATTERN_AMOUNT, "Cannot add more than %d patterns!", AutoModRule.MAX_PATTERN_AMOUNT);
        for (String pattern : patterns)
            checkPattern(pattern);

        this.patterns.addAll(patterns);
        return this;
    }

    /**
     * Change the list of keywords regex patterns to match against.
     * <br>Keyword patterns are matched case-insensitively, and may also contain whitespace.
     *
     * <p>Patterns may use anything supported by the rust regex crate.
     * You can use a validator such as <a href="https://rustexp.lpil.uk/" target="_blank">Rustexp</a> to validate your pattern.
     *
     * <p>You can also use simple substring keywords using {@link #keywordFilter(String...)}.
     *
     * @param  patterns
     *         The keyword patterns to match
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the patterns are empty, blank, or null</li>
     *             <li>If more than {@value AutoModRule#MAX_PATTERN_AMOUNT} patterns are added</li>
     *             <li>If any of the patterns is longer than {@value AutoModRule#MAX_PATTERN_LENGTH} characters</li>
     *         </ul>
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    public CustomKeywordTriggerConfig setPatterns(@Nonnull Collection<String> patterns)
    {
        Checks.noneNull(patterns, "Patterns");
        Checks.check(patterns.size() <= AutoModRule.MAX_PATTERN_AMOUNT, "Cannot add more than %d patterns!", AutoModRule.MAX_PATTERN_AMOUNT);
        for (String pattern : patterns)
            checkPattern(pattern);

        this.patterns.clear();
        this.patterns.addAll(patterns);
        return this;
    }

    protected static void checkPattern(String pattern)
    {
        Checks.notBlank(pattern, "Pattern");
        Checks.notLonger(pattern, AutoModRule.MAX_PATTERN_LENGTH, "Pattern");
    }

    @Override
    protected int maxAllowListAmount()
    {
        return AutoModRule.MAX_ALLOWLIST_CUSTOM_AMOUNT;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        Checks.check(!keywords.isEmpty() || !patterns.isEmpty(), "Must have at least one keyword or pattern!");
        DataObject data = super.toData();
        data.put("keyword_filter", DataArray.fromCollection(keywords));
        data.put("regex_patterns", DataArray.fromCollection(patterns));
        return data;
    }
}
