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
package net.dv8tion.jda.api.events.message;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Indicates that a bulk deletion is executed in a {@link net.dv8tion.jda.api.entities.GuildMessageChannel GuildMessageChannel}.
 * <br>Set {@link net.dv8tion.jda.api.JDABuilder#setBulkDeleteSplittingEnabled(boolean)} to false in order to enable this event.
 * 
 * <p>Can be used to detect that a large chunk of Messages is deleted in a GuildMessageChannel. Providing a list of Message IDs and the specific GuildMessageChannel.
 *
 * <h2>Requirements</h2>
 *
 * <p>This event requires at least one of the following intents (Will not fire at all if neither is enabled):
 * <ul>
 *     <li>{@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MESSAGES GUILD_MESSAGES} to work in guild message channels</li>
 *     <li>{@link net.dv8tion.jda.api.requests.GatewayIntent#DIRECT_MESSAGES DIRECT_MESSAGES} to work in private channels</li>
 * </ul>
 */
public class MessageBulkDeleteEvent extends Event
{
    protected final GuildMessageChannel channel;
    protected final List<String> messageIds;

    public MessageBulkDeleteEvent(@Nonnull JDA api, long responseNumber, @Nonnull GuildMessageChannel channel, @Nonnull List<String> messageIds)
    {
        super(api, responseNumber);
        this.channel = channel;
        this.messageIds = Collections.unmodifiableList(messageIds);
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.GuildMessageChannel GuildMessageChannel} where the messages have been deleted
     *
     * @return The TextChannel
     */
    @Nonnull
    public GuildMessageChannel getChannel()
    {
        return channel;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} where the messages were deleted.
     *
     * @return The Guild
     */
    @Nonnull
    public Guild getGuild()
    {
        return channel.getGuild();
    }
    
    /**
     * List of messages that have been deleted.
     *
     * @return The list of message ids
     */
    @Nonnull
    public List<String> getMessageIds()
    {
        return messageIds;
    }
}
