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
import java.util.*;

/**
 * Abstract for all keyword trigger types.
 *
 * @param <B>
 *        The builder type
 */
@SuppressWarnings("unchecked")
public abstract class AbstractKeywordTriggerConfig<B extends AbstractKeywordTriggerConfig<B>> extends AbstractTriggerConfig<B>
{
    protected final List<String> allowList = new ArrayList<>();

    protected AbstractKeywordTriggerConfig(AutoModTriggerType triggerType)
    {
        super(triggerType);
    }

    /**
     * Add keywords to the allow list.
     * <p>Keywords added to the allow list will not be considered as a match and won't trigger the rule execution.
     *
     * <p>Keywords follow the same rules as {@link CustomKeywordTriggerConfig#addKeywords(String...)}.
     *
     * @param  keywords
     *         The keywords to allow
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the keywords is empty, blank, or null</li>
     *             <li>If more than the allowed number of keywords are added to the list
     *                 ({@value AutoModRule#MAX_ALLOWLIST_CUSTOM_AMOUNT} for custom keyword lists,
     *                  {@value AutoModRule#MAX_ALLOWLIST_PRESET_AMOUNT} for preset keyword lists)</li>
     *             <li>If any keyword is longer than {@link AutoModRule#MAX_KEYWORD_LENGTH}</li>
     *         </ul>
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    public B addAllowList(@Nonnull String... keywords)
    {
        Checks.noneNull(keywords, "Keywords");
        Checks.check(this.allowList.size() + keywords.length <= maxAllowListAmount(), "Cannot add more than %d keywords!", maxAllowListAmount());
        Arrays.stream(keywords).forEach(AbstractKeywordTriggerConfig::checkKeyword);
        Collections.addAll(allowList, keywords);
        return (B) this;
    }

    /**
     * Add keywords to the allow list.
     * <p>Keywords added to the allow list will not be considered as a match and won't trigger the rule execution.
     *
     * <p>Keywords follow the same rules as {@link CustomKeywordTriggerConfig#addKeywords(String...)}.
     *
     * @param  keywords
     *         The keywords to allow
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the keywords is empty, blank, or null</li>
     *             <li>If more than the allowed number of keywords are added to the list
     *                 ({@value AutoModRule#MAX_ALLOWLIST_CUSTOM_AMOUNT} for custom keyword lists,
     *                  {@value AutoModRule#MAX_ALLOWLIST_PRESET_AMOUNT} for preset keyword lists)</li>
     *             <li>If any keyword is longer than {@link AutoModRule#MAX_KEYWORD_LENGTH}</li>
     *         </ul>
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    public B addAllowList(@Nonnull Collection<String> keywords)
    {
        Checks.noneNull(keywords, "Keywords");
        Checks.check(this.allowList.size() + keywords.size() <= maxAllowListAmount(), "Cannot add more than %d keywords!", maxAllowListAmount());
        keywords.forEach(AbstractKeywordTriggerConfig::checkKeyword);
        allowList.addAll(keywords);
        return (B) this;
    }

    /**
     * Change the allow list to the provided keywords.
     * <p>Keywords added to the allow list will not be considered as a match and won't trigger the rule execution.
     *
     * <p>Keywords follow the same rules as {@link CustomKeywordTriggerConfig#addKeywords(String...)}.
     *
     * @param  keywords
     *         The keywords to allow
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the keywords is empty, blank, or null</li>
     *             <li>If more than the allowed number of keywords are added to the list
     *                 ({@value AutoModRule#MAX_ALLOWLIST_CUSTOM_AMOUNT} for custom keyword lists,
     *                  {@value AutoModRule#MAX_ALLOWLIST_PRESET_AMOUNT} for preset keyword lists)</li>
     *             <li>If any keyword is longer than {@link AutoModRule#MAX_KEYWORD_LENGTH}</li>
     *         </ul>
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    public B setAllowList(@Nonnull Collection<String> keywords)
    {
        Checks.noneNull(keywords, "Keywords");
        Checks.check(keywords.size() <= maxAllowListAmount(), "Cannot add more than %d keywords!", maxAllowListAmount());
        keywords.forEach(AbstractKeywordTriggerConfig::checkKeyword);
        allowList.clear();
        allowList.addAll(keywords);
        return (B) this;
    }

    protected abstract int maxAllowListAmount();

    protected static void checkKeyword(String keyword)
    {
        Checks.notEmpty(keyword, "Keyword");
        Checks.notLonger(keyword, AutoModRule.MAX_KEYWORD_LENGTH, "Keyword");
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject data = super.toData();
        data.put("allow_list", DataArray.fromCollection(allowList));
        return data;
    }
}
