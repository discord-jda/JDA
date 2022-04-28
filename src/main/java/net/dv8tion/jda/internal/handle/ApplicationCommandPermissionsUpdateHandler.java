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

import net.dv8tion.jda.api.events.interaction.command.ApplicationCommandUpdatePermissionsEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;

import java.util.Objects;

public class ApplicationCommandPermissionsUpdateHandler extends SocketHandler
{
    public ApplicationCommandPermissionsUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        GuildImpl guild = null;
        if (!content.isNull("guild_id"))
        {
            long guildId = content.getUnsignedLong("guild_id");
            guild = (GuildImpl) getJDA().getGuildById(guildId);
            if (getJDA().getGuildSetupController().isLocked(guildId))
                return guildId;
            else if (guild == null)
                return null;
        }

        api.handleEvent(new ApplicationCommandUpdatePermissionsEvent(api, responseNumber, Objects.requireNonNull(guild), content));
        return null;
    }
}
