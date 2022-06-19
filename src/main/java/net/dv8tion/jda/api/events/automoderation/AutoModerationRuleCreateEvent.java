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

package net.dv8tion.jda.api.events.automoderation;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.AutoModerationRule;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.TriggerType;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

//TODO: JavaDoc
public class AutoModerationRuleCreateEvent extends GenericAutoModerationEvent {

    private final long ruleId;
    private final TriggerType triggerType;
    private final User user;
    private final TextChannel channel;
    private final Long messageId;
    private final Long alertSystemMessageId;
    private final String content;
    private final String matchedKeyword;
    private final String matchedContent;

    public AutoModerationRuleCreateEvent(@NotNull JDA api, long responseNumber, AutoModerationRule rule, long ruleId, TriggerType triggerType, User user, TextChannel channel, Long messageId, Long alertSystemMessageId, String content, String matchedKeyword, String matchedContent) {
        super(api, responseNumber, rule);
        this.ruleId = ruleId;
        this.triggerType = triggerType;
        this.user = user;
        this.channel = channel;
        this.messageId = messageId;
        this.alertSystemMessageId = alertSystemMessageId;
        this.content = content;
        this.matchedKeyword = matchedKeyword;
        this.matchedContent = matchedContent;
    }

    public long getRuleId() {
        return ruleId;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public User getUser() {
        return user;
    }

    public TextChannel getChannel() {
        return channel;
    }

    public Long getMessageId() {
        return messageId;
    }

    public Long getAlertSystemMessageId() {
        return alertSystemMessageId;
    }

    public String getContent() {
        return content;
    }

    public String getMatchedKeyword() {
        return matchedKeyword;
    }

    public String getMatchedContent() {
        return matchedContent;
    }
}

