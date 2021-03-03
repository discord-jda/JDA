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

package net.dv8tion.jda.api.events.message.guild.react;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;

import javax.annotation.Nonnull;

/**
 * Indicates that the reactions for a {@link net.dv8tion.jda.api.entities.Message Message} were cleared by a moderator in a guild.
 *
 * <p>Can be used to detect when the reaction of a message are cleared by a moderator.
 *
 * <h2>Requirements</h2>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MESSAGE_REACTIONS GUILD_MESSAGE_REACTIONS} intent to be enabled.
 */
public class GuildMessageReactionRemoveAllEvent extends GenericGuildMessageEvent
{
    public GuildMessageReactionRemoveAllEvent(@Nonnull JDA api, long responseNumber, long messageId, @Nonnull TextChannel channel)
    {
        super(api, responseNumber, messageId, channel);
    }
}
