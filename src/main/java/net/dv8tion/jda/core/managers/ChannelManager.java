/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.requests.RestAction;

public class ChannelManager
{
    protected final ChannelManagerUpdatable updatable;

    public ChannelManager(Channel channel)
    {
        this.updatable = new ChannelManagerUpdatable(channel);
    }

    public JDA getJDA()
    {
        return updatable.getJDA();
    }

    public Channel getChannel()
    {
        return updatable.getChannel();
    }

    public Guild getGuild()
    {
        return updatable.getGuild();
    }

    public RestAction<Void> setName(String name)
    {
        return updatable.getNameField().setValue(name).update();
    }

    public RestAction<Void> setTopic(String topic)
    {
        return updatable.getTopicField().setValue(topic).update();
    }

    public RestAction<Void> setUserLimit(int userLimit)
    {
        return updatable.getUserLimitField().setValue(userLimit).update();
    }

    public RestAction<Void> setBitrate(int bitrate)
    {
        return updatable.getBitrateField().setValue(bitrate).update();
    }
}
