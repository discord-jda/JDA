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

import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.channel.priv.PrivateChannelCreateEvent;
import net.dv8tion.jda.events.channel.text.GenericTextChannelEvent;
import net.dv8tion.jda.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.events.channel.text.TextChannelUpdateEvent;
import net.dv8tion.jda.events.channel.voice.GenericVoiceChannelEvent;
import net.dv8tion.jda.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.events.channel.voice.VoiceChannelUpdateEvent;
import net.dv8tion.jda.events.guild.GenericGuildEvent;
import net.dv8tion.jda.events.guild.GuildCreateEvent;
import net.dv8tion.jda.events.guild.GuildDeleteEvent;
import net.dv8tion.jda.events.guild.GuildUpdateEvent;
import net.dv8tion.jda.events.message.GenericMessageEvent;
import net.dv8tion.jda.events.message.MessageDeleteEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.events.message.MessageUpdateEvent;
import net.dv8tion.jda.events.user.GenericUserEvent;
import net.dv8tion.jda.events.user.UserAvatarUpdateEvent;
import net.dv8tion.jda.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.events.user.UserNameUpdateEvent;
import net.dv8tion.jda.events.user.UserOnlineStatusUpdateEvent;
import net.dv8tion.jda.events.user.UserTypingEvent;

public abstract class ListenerAdapter implements EventListener
{
    @Override
    public void onEvent(Event event)
    {
        //JDA Events
        if (event instanceof ReadyEvent)
            onReady((ReadyEvent) event);

        //Message Events
        else if (event instanceof MessageUpdateEvent)
            onMessageUpdate((MessageUpdateEvent) event);
        else if (event instanceof MessageReceivedEvent)
            onMessageReceived((MessageReceivedEvent) event);
        else if (event instanceof MessageDeleteEvent)
            onMessageDelete((MessageDeleteEvent) event);

        //User Events
        else if (event instanceof UserNameUpdateEvent)
            onUserNameUpdate((UserNameUpdateEvent) event);
        else if (event instanceof UserAvatarUpdateEvent)
            onUserAvatarUpdate((UserAvatarUpdateEvent) event);
        else if (event instanceof UserGameUpdateEvent)
            onUserGameUpdate((UserGameUpdateEvent) event);
        else if (event instanceof UserOnlineStatusUpdateEvent)
            onUserOnlineStatusUpdate((UserOnlineStatusUpdateEvent) event);
        else if (event instanceof UserTypingEvent)
            onUserTyping((UserTypingEvent) event);
        else if (event instanceof GenericUserEvent)
            onGenericUserEvent((GenericUserEvent) event);

        //TextChannel Events
        else if (event instanceof TextChannelCreateEvent)
            onTextChannelCreate((TextChannelCreateEvent) event);
        else if (event instanceof TextChannelUpdateEvent)
            onTextChannelUpdate((TextChannelUpdateEvent) event);
        else if (event instanceof TextChannelDeleteEvent)
            onTextChannelDelete((TextChannelDeleteEvent) event);

        //VoiceChannel Events
        else if (event instanceof VoiceChannelCreateEvent)
            onVoiceChannelCreate((VoiceChannelCreateEvent) event);
        else if (event instanceof VoiceChannelUpdateEvent)
            onVoiceChannelUpdate((VoiceChannelUpdateEvent) event);
        else if (event instanceof VoiceChannelDeleteEvent)
            onVoiceChannelDelete((VoiceChannelDeleteEvent) event);

        //PrivateChannel Events
        else if (event instanceof PrivateChannelCreateEvent)
            onPrivateChannelCreate((PrivateChannelCreateEvent) event);

        //Guild Events
        else if (event instanceof GuildCreateEvent)
            onGuildCreate((GuildCreateEvent) event);
        else if (event instanceof GuildUpdateEvent)
            onGuildUpdate((GuildUpdateEvent) event);
        else if (event instanceof GuildDeleteEvent)
            onGuildDelete((GuildDeleteEvent) event);

        //Generic Events
        if (event instanceof GenericMessageEvent)
            onGenericMessageEvent((GenericMessageEvent) event);
        else if (event instanceof GenericTextChannelEvent)
            onGenericTextChannelEvent((GenericTextChannelEvent) event);
        else if (event instanceof GenericVoiceChannelEvent)
            onGenericVoiceChannelEvent((GenericVoiceChannelEvent) event);
        else if (event instanceof GenericGuildEvent)
            onGenericGuildEvent((GenericGuildEvent) event);
    }

    //JDA Events
    public void onReady(ReadyEvent event) {}

    //User Events
    public void onUserNameUpdate(UserNameUpdateEvent event) {}
    public void onUserAvatarUpdate(UserAvatarUpdateEvent event) {}
    public void onUserOnlineStatusUpdate(UserOnlineStatusUpdateEvent event) {}
    public void onUserGameUpdate(UserGameUpdateEvent event) {}
    public void onUserTyping(UserTypingEvent event) {}

    //Message Events
    public void onMessageReceived(MessageReceivedEvent event) {}
    public void onMessageUpdate(MessageUpdateEvent event) {}
    public void onMessageDelete(MessageDeleteEvent event) {}

    //TextChannel Events
    public void onTextChannelDelete(TextChannelDeleteEvent event) {}
    public void onTextChannelUpdate(TextChannelUpdateEvent event) {}
    public void onTextChannelCreate(TextChannelCreateEvent event) {}

    //VoiceChannel Events
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {}
    public void onVoiceChannelUpdate(VoiceChannelUpdateEvent event) {}
    public void onVoiceChannelCreate(VoiceChannelCreateEvent event) {}

    //PrivateChannel Events
    public void onPrivateChannelCreate(PrivateChannelCreateEvent event) {}

    //Guild Events
    public void onGuildCreate(GuildCreateEvent event) {}
    public void onGuildUpdate(GuildUpdateEvent event) {}
    public void onGuildDelete(GuildDeleteEvent event) {}

    //Generic Events
    public void onGenericUserEvent(GenericUserEvent event) {}
    public void onGenericMessageEvent(GenericMessageEvent event) {}
    public void onGenericTextChannelEvent(GenericTextChannelEvent event) {}
    public void onGenericVoiceChannelEvent(GenericVoiceChannelEvent event) {}
    public void onGenericGuildEvent(GenericGuildEvent event) {}
}
