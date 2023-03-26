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
import net.dv8tion.jda.api.entities.automod.AutoModRule;
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.*;

public abstract class AbstractKeywordRuleBuilder<B extends AbstractKeywordRuleBuilder<B>> extends AbstractAutoModRuleBuilder<B>
{
    protected final List<String> allowList = new ArrayList<>();

    protected AbstractKeywordRuleBuilder(AutoModTriggerType triggerType, AutoModEventType eventType, String name)
    {
        super(triggerType, eventType, name);
    }

    @Nonnull
    public B addAllowList(@Nonnull String... keywords)
    {
        Checks.noneNull(keywords, "Keywords");
        Checks.check(this.allowList.size() + keywords.length <= maxAllowListAmount(), "Cannot add more than %d keywords!", maxAllowListAmount());
        Arrays.stream(keywords).forEach(AbstractKeywordRuleBuilder::checkKeyword);
        Collections.addAll(allowList, keywords);
        return (B) this;
    }

    @Nonnull
    public B addAllowList(@Nonnull Collection<String> keywords)
    {
        Checks.noneNull(keywords, "Keywords");
        Checks.check(this.allowList.size() + keywords.size() <= maxAllowListAmount(), "Cannot add more than %d keywords!", maxAllowListAmount());
        keywords.forEach(AbstractKeywordRuleBuilder::checkKeyword);
        allowList.addAll(keywords);
        return (B) this;
    }

    @Nonnull
    public B setAllowList(@Nonnull Collection<String> keywords)
    {
        Checks.noneNull(keywords, "Keywords");
        Checks.check(keywords.size() <= maxAllowListAmount(), "Cannot add more than %d keywords!", maxAllowListAmount());
        keywords.forEach(AbstractKeywordRuleBuilder::checkKeyword);
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
    public AutoModRuleData build()
    {
        AutoModRuleData rule = super.build();
        rule.setAllowlist(allowList);
        return rule;
    }
}
