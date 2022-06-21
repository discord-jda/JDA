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

import net.dv8tion.jda.api.events.automoderation.AutoModerationRuleDeleteEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.api.entities.AutoModerationRule;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.requests.WebSocketClient;

public class AutoModerationRuleDeleteHandler extends SocketHandler
{

    public AutoModerationRuleDeleteHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {

        long guildId = content.getLong("guild_id");
        if (getJDA().getGuildSetupController().isLocked(guildId))
            return guildId;

        GuildImpl guild = (GuildImpl) getJDA().getGuildById(guildId);

        long ruleId = content.getLong("id");
        AutoModerationRule rule = guild.getAutoModerationRulesView().remove(ruleId);

        if (rule == null || guild == null)
        {
            WebSocketClient.LOG.debug("AUTO_MODERATION_RULE_DELETE attempted to delete an auto moderation rule that is not yet cached. JSON: {}", content);
            return null;
        }

        getJDA().handleEvent(new AutoModerationRuleDeleteEvent(getJDA(), responseNumber, rule));

        getJDA().getEventCache().clear(EventCache.Type.AUTO_MODERATION, ruleId);
        return null;
    }
}
