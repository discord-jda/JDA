/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
 * <b><u>PrivateChannelDeleteEvent</u></b><br>
 * Fired if a {@link net.dv8tion.jda.core.entities.PrivateChannel Private Channel} was deleted.<br>
 * <br>
 * Use: Retrieve the issuing {@link net.dv8tion.jda.core.entities.User User}.
 */
public class PrivateChannelDeleteEvent extends Event
{
    protected final PrivateChannel channel;

    public PrivateChannelDeleteEvent(JDA api, long responseNumber, PrivateChannel channel)
    {
        super(api, responseNumber);
        this.channel = channel;
    }

    public User getUser()
    {
        return channel.getUser();
    }

    public PrivateChannel getPrivateChannel()
    {
        return channel;
    }
}
