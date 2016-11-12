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
package net.dv8tion.jda.events;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.events.message.GenericMessageEvent;
import net.dv8tion.jda.utils.InviteUtil;

/**
 * <b><u>InviteReceivedEvent</u></b><br>
 * Fired if we received a message that contains an invite url. (Example: https://discord.gg/0hMr4ce0tIl3SLv5)<br>
 * <br>
 * Use: Detect messages containing an invite and providing an {@link net.dv8tion.jda.utils.InviteUtil.Invite Invite} instance.
 */
public class InviteReceivedEvent extends GenericMessageEvent
{
    private final InviteUtil.Invite invite;

    public InviteReceivedEvent(JDA api, int responseNumber, Message message, InviteUtil.Invite invite)
    {
        super(api, responseNumber, message);
        this.invite = invite;
    }

    public InviteUtil.Invite getInvite()
    {
        return invite;
    }

    public boolean isPrivate()
    {
        return message.isPrivate();
    }
}
