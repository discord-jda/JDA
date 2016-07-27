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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.events.user;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.OnlineStatus;
import net.dv8tion.jda.entities.User;

/**
 * <b><u>UserOnlineStatusUpdateEvent</u></b><br/>
 * Fired if the {@link OnlineStatus OnlineStatus} of a {@link net.dv8tion.jda.entities.User User} changes.<br/>
 * <br/>
 * Use: Retrieve the User who's status changed and their previous status.
 */
public class UserOnlineStatusUpdateEvent extends GenericUserEvent
{
    private final OnlineStatus previousOnlineStatus;

    public UserOnlineStatusUpdateEvent(JDA api, int responseNumber, User user, OnlineStatus previousOnlineStatus)
    {
        super(api, responseNumber, user);
        this.previousOnlineStatus = previousOnlineStatus;
    }

    public OnlineStatus getPreviousOnlineStatus()
    {
        return previousOnlineStatus;
    }
}
