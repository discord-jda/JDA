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

package net.dv8tion.jda.internal.entities.automod.build.sent;


import net.dv8tion.jda.api.entities.automod.EventType;
import net.dv8tion.jda.api.entities.automod.TriggerMetadata;
import net.dv8tion.jda.api.entities.automod.TriggerType;
import net.dv8tion.jda.api.entities.automod.build.sent.MentionTotalLimit;
import net.dv8tion.jda.internal.entities.automod.TriggerMetadataImpl;
import net.dv8tion.jda.internal.utils.Checks;

public class MentionTotalLimitImpl extends GenericMetadataImpl implements MentionTotalLimit
{

    public MentionTotalLimitImpl(String name, EventType eventType)
    {
        super(name, eventType, TriggerType.MENTION_SPAM);
    }

    @Override
    public MentionTotalLimit setMentionLimit(int limit)
    {
        Checks.check(limit > 0 && limit <= 50, "Limit must be between 1 and 50");
        TriggerMetadata triggerMetadata = new TriggerMetadataImpl();
        triggerMetadata.setMentionTotalLimit(limit);
        this.triggerMetadata = triggerMetadata;
        return this;
    }
}

