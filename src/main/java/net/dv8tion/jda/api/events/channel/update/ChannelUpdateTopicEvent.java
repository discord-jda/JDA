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

package net.dv8tion.jda.api.events.channel.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelField;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link Channel Channel's} topic has been updated.
 *
 * <p>Can be used to retrieve the old topic and the new one.
 *
 * <p>Limited to {@link NewsChannel NewsChannels}, {@link TextChannel TextChannels}, and {@link ForumChannel ForumChannels}.
 *
 * @see StandardGuildMessageChannel#getTopic()
 * @see ForumChannel#getTopic()
 */
public class ChannelUpdateTopicEvent extends GenericChannelUpdateEvent<String>
{
    public static final ChannelField FIELD = ChannelField.TOPIC;
    public static final String IDENTIFIER = FIELD.getFieldName();

    public ChannelUpdateTopicEvent(@Nonnull JDA api, long responseNumber, Channel channel, String oldValue, String newValue)
    {
        super(api, responseNumber, channel, FIELD, oldValue, newValue);
    }
}
