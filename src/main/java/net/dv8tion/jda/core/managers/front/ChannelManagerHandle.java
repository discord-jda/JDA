/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.managers.front;

import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.managers.ChannelManager;

public interface ChannelManagerHandle //todo docs
{
    /**
     * Returns the {@link net.dv8tion.jda.core.managers.ChannelManager ChannelManager} for this Channel.
     * <br>In the ChannelManager, you can modify the name, topic and position of this Channel.
     *
     * @return The ChannelManager of this Channel
     */
    ChannelManager getManager();

    default ChannelManager setName(String name)
    {
        return getManager().setName(name);
    }

    default ChannelManager setParent(Category parent)
    {
        return getManager().setParent(parent);
    }

    default ChannelManager setBitrate(int bitrate)
    {
        return getManager().setBitrate(bitrate);
    }

    default ChannelManager setTopic(String topic)
    {
        return getManager().setTopic(topic);
    }

    default ChannelManager setUserLimit(int userlimit)
    {
        return getManager().setUserLimit(userlimit);
    }

    default ChannelManager setNSFW(boolean nsfw)
    {
        return getManager().setNSFW(nsfw);
    }

    default ChannelManager setPosition(int position)
    {
        return getManager().setPosition(position);
    }
}
