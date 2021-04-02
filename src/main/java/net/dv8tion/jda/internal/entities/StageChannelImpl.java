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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.StageChannel;

import javax.annotation.Nonnull;

public class StageChannelImpl extends VoiceChannelImpl implements StageChannel
{
    private String topic = "";

    public StageChannelImpl(long id, GuildImpl guild)
    {
        super(id, guild);
    }

    @Nonnull
    @Override
    public ChannelType getType()
    {
        return ChannelType.STAGE;
    }

    @Nonnull
    @Override
    public String getTopic()
    {
        return topic;
    }

    public StageChannelImpl setTopic(String topic)
    {
        this.topic = topic;
        return this;
    }
}
