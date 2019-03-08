/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.exceptions;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.internal.utils.Checks;

public class InsufficientPermissionException extends PermissionException
{
    private final long guildId;
    private final long channelId;

    public InsufficientPermissionException(Guild guild, GuildChannel channel, Permission permission)
    {
        super(permission, "Cannot perform action due to a lack of Permission. Missing permission: " + permission.toString());
        this.guildId = guild.getIdLong();
        this.channelId = channel == null ? 0 : channel.getIdLong();
    }

    public InsufficientPermissionException(Guild guild, GuildChannel channel, Permission permission, String reason)
    {
        super(permission, reason);
        this.guildId = guild.getIdLong();
        this.channelId = channel == null ? 0 : channel.getIdLong();
    }

    public long getGuildId()
    {
        return guildId;
    }

    public long getChannelId()
    {
        return channelId;
    }

    public Guild getGuild(JDA api)
    {
        Checks.notNull(api, "JDA");
        return api.getGuildById(guildId);
    }

    public GuildChannel getChannel(JDA api)
    {
        Checks.notNull(api, "JDA");
        GuildChannel channel = api.getTextChannelById(channelId);
        if (channel == null)
            channel = api.getVoiceChannelById(channelId);
        if (channel == null)
            channel = api.getCategoryById(channelId);
        return channel;
    }
}
