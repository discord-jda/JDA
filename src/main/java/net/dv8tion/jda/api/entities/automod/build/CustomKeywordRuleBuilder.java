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
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.*;

public class CustomKeywordRuleBuilder extends AbstractKeywordRuleBuilder<CustomKeywordRuleBuilder>
{
    protected final Set<String> keywords = new HashSet<>();
    protected final Set<String> patterns = new HashSet<>();

    public CustomKeywordRuleBuilder(@Nonnull String name)
    {
        super(AutoModTriggerType.KEYWORD, AutoModEventType.MESSAGE_SEND, name);
    }

    @Nonnull
    public CustomKeywordRuleBuilder addKeywords(@Nonnull String... keywords)
    {
        Checks.noneNull(keywords, "Keywords");
        for (String keyword : keywords)
            checkKeyword(keyword);

        Collections.addAll(this.keywords, keywords);
        return this;
    }

    @Nonnull
    public CustomKeywordRuleBuilder addKeywords(@Nonnull Collection<String> keywords)
    {
        Checks.noneNull(keywords, "Keywords");
        for (String keyword : keywords)
            checkKeyword(keyword);

        this.keywords.addAll(keywords);
        return this;
    }

    @Nonnull
    public CustomKeywordRuleBuilder setKeywords(@Nonnull Collection<String> keywords)
    {
        Checks.noneNull(keywords, "Keywords");
        for (String keyword : keywords)
            checkKeyword(keyword);

        this.keywords.clear();
        this.keywords.addAll(keywords);
        return this;
    }


    @Nonnull
    public CustomKeywordRuleBuilder addPatterns(@Nonnull String... patterns)
    {
        Checks.noneNull(patterns, "Patterns");
        for (String pattern : patterns)
            checkPattern(pattern);

        Collections.addAll(this.patterns, patterns);
        return this;
    }

    @Nonnull
    public CustomKeywordRuleBuilder addPatterns(@Nonnull Collection<String> patterns)
    {
        Checks.noneNull(patterns, "Patterns");
        for (String pattern : patterns)
            checkPattern(pattern);

        this.patterns.addAll(patterns);
        return this;
    }

    @Nonnull
    public CustomKeywordRuleBuilder setPatterns(@Nonnull Collection<String> patterns)
    {
        Checks.noneNull(patterns, "Patterns");
        for (String pattern : patterns)
            checkPattern(pattern);

        this.patterns.clear();
        this.patterns.addAll(patterns);
        return this;
    }

    protected static void checkKeyword(String keyword)
    {
        Checks.notEmpty(keyword, "Keyword");
    }

    protected static void checkPattern(String pattern)
    {
        Checks.notEmpty(pattern, "Pattern");
    }

    @Nonnull
    @Override
    public AutoModRuleData build()
    {
        AutoModRuleData rule = super.build();
        rule.setFilteredKeywords(new ArrayList<>(keywords));
        rule.setFilteredRegex(new ArrayList<>(patterns));
        return rule;
    }
}
