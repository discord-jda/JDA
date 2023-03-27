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

import javax.annotation.Nonnull;

public class MentionSpamTriggerConfig extends AbstractTriggerConfig<MentionSpamTriggerConfig> implements TriggerConfig
{
    private int mentionLimit;

    public MentionSpamTriggerConfig(int mentionLimit)
    {
        super(AutoModTriggerType.MENTION_SPAM);
        this.mentionLimit = mentionLimit;
    }

    @Nonnull
    public MentionSpamTriggerConfig setMentionLimit(int mentionLimit)
    {
        Checks.positive(mentionLimit, "Mention Limit");
        Checks.check(mentionLimit <= AutoModRule.MAX_MENTION_LIMIT, "Mention Limit cannot be higher than %d. Provided: %d", AutoModRule.MAX_MENTION_LIMIT, mentionLimit);
        this.mentionLimit = mentionLimit;
        return this;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject data = super.toData();
        data.put("mention_total_limit", mentionLimit);
        return data;
    }
}
