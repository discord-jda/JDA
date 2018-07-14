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
package net.dv8tion.jda.core.events.channel.priv;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;

/**
 * Indicates that a {@link net.dv8tion.jda.core.entities.PrivateChannel Private Channel} was created.
 *
 * <p>Can be used to retrieve the created private channel and its {@link net.dv8tion.jda.core.entities.User User}.
 */
public class PrivateChannelCreateEvent extends Event
{
    private final PrivateChannel channel;

    public PrivateChannelCreateEvent(JDA api, long responseNumber, PrivateChannel channel)
    {
        super(api, responseNumber);
        this.channel = channel;
    }

    /**
     * The target {@link net.dv8tion.jda.core.entities.User User}
     * <br>Shortcut for {@code getPrivateChannel().getUser()}
     *
     * @return The User
     */
    public User getUser()
    {
        return channel.getUser();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}
     *
     * @return The PrivateChannel
     */
    public PrivateChannel getChannel()
    {
        return channel;
    }
}
