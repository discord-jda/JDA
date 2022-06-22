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

import net.dv8tion.jda.api.entities.AutoModerationRule;
import net.dv8tion.jda.api.events.automod.AutoModerationRuleCreateEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;

public class AutoModerationRuleCreateHandler extends SocketHandler
{

    public AutoModerationRuleCreateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject dataObject)
    {

        long guildId = dataObject.getLong("guild_id");
        if (api.getGuildSetupController().isLocked(guildId))
            return guildId;

        AutoModerationRule action = api.getEntityBuilder().createAutoModerationRule((GuildImpl) api.getGuildById(guildId), dataObject.getObject("action"));

        api.handleEvent(new AutoModerationRuleCreateEvent(api, responseNumber, action));
        return null;
    }
}
