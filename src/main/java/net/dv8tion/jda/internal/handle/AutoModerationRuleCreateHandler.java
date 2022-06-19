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

package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.automoderation.AutoModerationRuleCreateEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

public class AutoModerationRuleCreateHandler extends SocketHandler {

    public AutoModerationRuleCreateHandler(JDAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject dataObject) {

        long guildId = dataObject.getLong("guild_id");
        JDAImpl jda = getJDA();

        AutoModerationRule action = jda.getEntityBuilder().createAutoModerationRule(guildId, dataObject.getObject("action"));

        long ruleId = dataObject.getLong("id");
        TriggerType triggerType = TriggerType.fromValue(dataObject.getInt("rule_trigger_type"));

        long userId = dataObject.getLong("user_id");

        User user = jda.getUserById(userId);

        Long channelId = !dataObject.isNull("channel_id") ? dataObject.getLong("channel_id") : null;

        TextChannel channel = null;
        if (channelId != null) {
            channel = jda.getTextChannelById(channelId);
        }

        Long message_id = !dataObject.isNull("message_id") ? dataObject.getLong("message_id") : null;

        Long alertSystemMessageId = !dataObject.isNull("alert_system_message_id") ? dataObject.getLong("alert_system_message_id") : null;
        String content = dataObject.getString("content");
        String matchedKeyword = !dataObject.isNull("matched_keyword") ? dataObject.getString("matched_keyword") : null;
        String matchedContent = !dataObject.isNull("matched_content") ? dataObject.getString("matched_content") : null;



        jda.handleEvent(new AutoModerationRuleCreateEvent(jda, responseNumber, action, ruleId, triggerType, user, channel, message_id, alertSystemMessageId, content, matchedKeyword, matchedContent));
        return null;
    }
}
