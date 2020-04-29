/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
package net.dv8tion.jda.api.events.channel.text.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}'s topic changed.
 *
 * <p>Can be used to detect when a TextChannel topic changes and get its previous topic.
 *
 * <p>Identifier: {@code topic}
 */
public class TextChannelUpdateTopicEvent extends GenericTextChannelUpdateEvent<String>
{
    public static final String IDENTIFIER = "topic";

    public TextChannelUpdateTopicEvent(@Nonnull JDA api, long responseNumber, @Nonnull TextChannel channel, @Nullable String oldTopic)
    {
        super(api, responseNumber, channel, oldTopic, channel.getTopic(), IDENTIFIER);
    }

    /**
     * The old topic
     *
     * @return The old topic, or null
     */
    @Nullable
    public String getOldTopic()
    {
        return getOldValue();
    }

    /**
     * The new topic
     *
     * @return The new topic, or null
     */
    @Nullable
    public String getNewTopic()
    {
        return getNewValue();
    }
}
