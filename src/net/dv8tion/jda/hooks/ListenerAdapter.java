/**
 * Copyright 2015 Austin Keener & Michael Ritter
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.hooks;

import net.dv8tion.jda.events.*;
import net.dv8tion.jda.events.generic.GenericGuildEvent;
import net.dv8tion.jda.events.generic.GenericMessageEvent;

public abstract class ListenerAdapter implements EventListener
{
    @Override
    public void onEvent(Event event)
    {
        if (event instanceof ReadyEvent)
        {
            onReady(((ReadyEvent) event));
        }
        else if (event instanceof MessageCreateEvent)
        {
            onMessageCreate(((MessageCreateEvent) event));
        }
        else if (event instanceof MessageUpdateEvent)
        {
            onMessageUpdate(((MessageUpdateEvent) event));
        }
        else if (event instanceof MessageDeleteEvent)
        {
            onMessageDelete(((MessageDeleteEvent) event));
        }
        else if (event instanceof GuildCreateEvent)
        {
            onGuildCreate(((GuildCreateEvent) event));
        }
        else if (event instanceof GuildUpdateEvent)
        {
            onGuildUpdate(((GuildUpdateEvent) event));
        }
        else if (event instanceof GuildDeleteEvent)
        {
            onGuildDelete(((GuildDeleteEvent) event));
        }

        if (event instanceof GenericMessageEvent)
        {
            onGenericMessageEvent(((GenericMessageEvent) event));
        }
        else if (event instanceof GenericGuildEvent)
        {
            onGenericGuildEvent(((GenericGuildEvent) event));
        }

    }

    public void onReady(ReadyEvent event)
    {
    }

    public void onMessageCreate(MessageCreateEvent event)
    {
    }

    public void onMessageUpdate(MessageUpdateEvent event)
    {
    }

    public void onMessageDelete(MessageDeleteEvent event)
    {
    }

    public void onGuildCreate(GuildCreateEvent event)
    {
    }

    public void onGuildUpdate(GuildUpdateEvent event)
    {
    }

    public void onGuildDelete(GuildDeleteEvent event)
    {
    }

    public void onGenericMessageEvent(GenericMessageEvent event)
    {
    }

    public void onGenericGuildEvent(GenericGuildEvent event)
    {
    }
}
