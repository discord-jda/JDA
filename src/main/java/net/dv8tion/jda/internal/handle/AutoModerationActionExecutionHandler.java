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
import net.dv8tion.jda.api.entities.automod.AutoModerationAction;
import net.dv8tion.jda.api.entities.automod.TriggerType;
import net.dv8tion.jda.api.events.automod.AutoModerationActionExecutionEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;

public class AutoModerationActionExecutionHandler extends SocketHandler
{
    public AutoModerationActionExecutionHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject dataObject)
    {
        long guildId = dataObject.getLong("guild_id");
        if (api.getGuildSetupController().isLocked(guildId))
            return guildId;

        Guild guild = api.getGuildById(guildId);

        AutoModerationAction action = api.getEntityBuilder().createAutoModerationAction((GuildImpl) guild, dataObject.getObject("action"));

        AutoModerationRule rule = guild.getAutoModerationRuleById(dataObject.getString("rule_id"));

        TriggerType triggerType = TriggerType.fromValue(dataObject.getInt("rule_trigger_type"));

        User trigger = api.getUserById(dataObject.getString("user_id"));

        GuildChannel channel = null;
        if (!dataObject.isNull("channel_id"))
        {
            channel = api.getGuildChannelById(dataObject.getString("channel_id"));
        }

        Long messageId = null;
        if (!dataObject.isNull("message_id"))
            messageId = dataObject.getLong("message_id");

        Long alertSystemMessageId = null;
        if (!dataObject.isNull("alert_system_message_id"))
            alertSystemMessageId = dataObject.getLong("alert_system_message_id");

        String content = dataObject.getString("content");

        String matchedKeyword = null;
        if (!dataObject.isNull("matched_keyword"))
            matchedKeyword = dataObject.getString("matched_keyword");

        String matchedContent = null;
        if (!dataObject.isNull("matched_content"))
            matchedContent = dataObject.getString("matched_content");

        api.handleEvent(new AutoModerationActionExecutionEvent(api, responseNumber, rule, action, trigger, triggerType, channel,
                messageId, alertSystemMessageId, content, matchedKeyword, matchedContent));
        return null;
    }
}
