/**
 *    Copyright 2015 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.hooks;

import net.dv8tion.jda.events.*;
import net.dv8tion.jda.events.generic.*;

public abstract class ListenerAdapter implements EventListener
{
    @Override
    public void onEvent(Event event)
    {
        //JDA Events
        if (event instanceof ReadyEvent)
            onReady(((ReadyEvent) event));

        //Message Events
        else if (event instanceof MessageUpdateEvent)
            onMessageUpdate(((MessageUpdateEvent) event));
        else if (event instanceof MessageReceivedEvent)
            onMessageReceived(((MessageReceivedEvent) event));
        else if (event instanceof MessageDeleteEvent)
            onMessageDelete(((MessageDeleteEvent) event));

        //User Update Events
        else if (event instanceof UserNameUpdateEvent)
            onUserNameUpdateEvent(event);
        else if (event instanceof UserAvatarUpdateEvent)
            onUserAvatarUpdateEvent(event);
        else if (event instanceof UserGameUpdateEvent)
            onUserGameUpdateEvent(event);
        else if (event instanceof UserOnlineStatusUpdateEvent)
            onUserOnlineStatusUpdateEvent(event);
        else if (event instanceof GenericUserUpdateEvent)
            onGenericUserUpdateEvent(event);

        //Guild Events
        else if (event instanceof GuildCreateEvent)
            onGuildCreate(((GuildCreateEvent) event));
        else if (event instanceof GuildUpdateEvent)
            onGuildUpdate(((GuildUpdateEvent) event));
        else if (event instanceof GuildDeleteEvent)
            onGuildDelete(((GuildDeleteEvent) event));

        //Generic Events
        if (event instanceof GenericMessageEvent)
            onGenericMessageEvent(((GenericMessageEvent) event));
        else if (event instanceof GenericGuildEvent)
            onGenericGuildEvent(((GenericGuildEvent) event));
    }

    //JDA Events
    public void onReady(ReadyEvent event) {}

    //User Update Events
    public void onUserNameUpdateEvent(Event event) {}
    public void onUserAvatarUpdateEvent(Event event) {}
    public void onUserOnlineStatusUpdateEvent(Event event) {}
    public void onUserGameUpdateEvent(Event event) {}

    //Message Events
    public void onMessageReceived(MessageReceivedEvent event) {}
    public void onMessageUpdate(MessageUpdateEvent event) {}
    public void onMessageDelete(MessageDeleteEvent event) {}

    //Guild Events
    public void onGuildCreate(GuildCreateEvent event) {}
    public void onGuildUpdate(GuildUpdateEvent event) {}
    public void onGuildDelete(GuildDeleteEvent event) {}

    //Generic Events
    public void onGenericUserUpdateEvent(Event event) {}
    public void onGenericMessageEvent(GenericMessageEvent event) {}
    public void onGenericGuildEvent(GenericGuildEvent event) {}
}
