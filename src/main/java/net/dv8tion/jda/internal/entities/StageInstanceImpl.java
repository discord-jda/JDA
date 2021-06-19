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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.entities.StageInstance;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;

public class StageInstanceImpl implements StageInstance
{
    private final long id;
    private StageChannel channel;

    private String topic;
    private PrivacyLevel privacyLevel;
    private boolean discoverable;

    public StageInstanceImpl(long id, StageChannel channel)
    {
        this.id = id;
        this.channel = channel;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public Guild getGuild()
    {
        return getChannel().getGuild();
    }

    @Override
    public StageChannel getChannel()
    {
        StageChannel real = channel.getJDA().getStageChannelById(channel.getIdLong());
        if (real != null)
            channel = real;
        return channel;
    }

    @Override
    public String getTopic()
    {
        return topic;
    }

    @Override
    public PrivacyLevel getPrivacyLevel()
    {
        return privacyLevel;
    }

    @Override
    public boolean isDiscoverable()
    {
        return discoverable;
    }

    @Override
    public RestAction<Void> delete()
    {
        Route.CompiledRoute route = Route.StageInstances.DELETE_INSTANCE.compile(channel.getId());
        return new RestActionImpl<>(channel.getJDA(), route);
    }

    public StageInstanceImpl setTopic(String topic)
    {
        this.topic = topic;
        return this;
    }

    public StageInstanceImpl setPrivacyLevel(PrivacyLevel privacyLevel)
    {
        this.privacyLevel = privacyLevel;
        return this;
    }

    public StageInstanceImpl setDiscoverable(boolean discoverable)
    {
        this.discoverable = discoverable;
        return this;
    }
}
