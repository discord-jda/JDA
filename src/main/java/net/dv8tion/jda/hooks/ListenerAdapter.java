/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
import net.dv8tion.jda.events.InviteReceivedEvent;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.channel.priv.PrivateChannelCreateEvent;
import net.dv8tion.jda.events.channel.text.*;
import net.dv8tion.jda.events.channel.voice.*;
import net.dv8tion.jda.events.guild.*;
import net.dv8tion.jda.events.guild.member.*;
import net.dv8tion.jda.events.guild.role.*;
import net.dv8tion.jda.events.message.*;
import net.dv8tion.jda.events.message.guild.*;
import net.dv8tion.jda.events.message.priv.*;
import net.dv8tion.jda.events.user.*;
import net.dv8tion.jda.events.voice.*;

public abstract class ListenerAdapter implements EventListener
{
    //JDA Events
    public void onReady(ReadyEvent event) {}

    //User Events
    public void onUserNameUpdate(UserNameUpdateEvent event) {}
    public void onUserAvatarUpdate(UserAvatarUpdateEvent event) {}
    public void onUserOnlineStatusUpdate(UserOnlineStatusUpdateEvent event) {}
    public void onUserGameUpdate(UserGameUpdateEvent event) {}
    public void onUserTyping(UserTypingEvent event) {}

    //Message Events
    //Guild (TextChannel) Message Events
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {}
    public void onGuildMessageAcknowledged(GuildMessageAcknowledgedEvent event) {}
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {}
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {}
    public void onGuildMessageEmbed(GuildMessageEmbedEvent event) {}
    //Private Message Events
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {}
    public void onPrivateMessageAcknowledged(PrivateMessageAcknowledgedEvent event) {}
    public void onPrivateMessageUpdate(PrivateMessageUpdateEvent event) {}
    public void onPrivateMessageDelete(PrivateMessageDeleteEvent event) {}
    public void onPrivateMessageEmbed(PrivateMessageEmbedEvent event) {}
    //Combined Message Events (Combines Guild and Private message into 1 event)
    public void onMessageReceived(MessageReceivedEvent event) {}
    public void onMessageAcknowledged(MessageAcknowledgedEvent event) {}
    public void onMessageUpdate(MessageUpdateEvent event) {}
    public void onMessageDelete(MessageDeleteEvent event) {}
    public void onMessageEmbed(MessageEmbedEvent event) {}

    public void onInviteReceived(InviteReceivedEvent event) {}

    //TextChannel Events
    public void onTextChannelDelete(TextChannelDeleteEvent event) {}
    public void onTextChannelUpdateName(TextChannelUpdateNameEvent event) {}
    public void onTextChannelUpdateTopic(TextChannelUpdateTopicEvent event) {}
    public void onTextChannelUpdatePosition(TextChannelUpdatePositionEvent event) {}
    public void onTextChannelUpdatePermissions(TextChannelUpdatePermissionsEvent event) {}
    public void onTextChannelCreate(TextChannelCreateEvent event) {}

    //VoiceChannel Events
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {}
    public void onVoiceChannelUpdateName(VoiceChannelUpdateNameEvent event) {}
    public void onVoiceChannelUpdatePosition(VoiceChannelUpdatePositionEvent event) {}
    public void onVoiceChannelUpdatePermissions(VoiceChannelUpdatePermissionsEvent event) {}
    public void onVoiceChannelCreate(VoiceChannelCreateEvent event) {}

    //PrivateChannel Events
    public void onPrivateChannelCreate(PrivateChannelCreateEvent event) {}

    //Guild Events
    public void onGuildJoin(GuildJoinEvent event) {}
    public void onGuildUpdate(GuildUpdateEvent event) {}
    public void onGuildLeave(GuildLeaveEvent event) {}
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {}
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {}
    public void onGuildMemberBan(GuildMemberBanEvent event) {}
    public void onGuildMemberUnban(GuildMemberUnbanEvent event) {}
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {}
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {}
    public void onGuildRoleCreate(GuildRoleCreateEvent event) {}
    public void onGuildRoleDelete(GuildRoleDeleteEvent event) {}

    //Guild Role Update Events
    public void onGuildRoleUpdate(GuildRoleUpdateEvent event) {}
    public void onGuildRoleUpdateName(GuildRoleUpdateNameEvent event) {}
    public void onGuildRoleUpdateColor(GuildRoleUpdateColorEvent event) {}
    public void onGuildRoleUpdatePosition(GuildRoleUpdatePositionEvent event) {}
    public void onGuildRoleUpdatePermission(GuildRoleUpdatePermissionEvent event) {}
    public void onGuildRoleUpdateGrouped(GuildRoleUpdateGroupedEvent event) {}

    //Voice Events
    public void onVoiceSelfMute(VoiceSelfMuteEvent event) {}
    public void onVoiceSelfDeaf(VoiceSelfDeafEvent event) {}
    public void onVoiceServerMute(VoiceServerMuteEvent event) {}
    public void onVoiceServerDeaf(VoiceServerDeafEvent event) {}
    public void onVoiceMute(VoiceMuteEvent event) {}
    public void onVoiceDeaf(VoiceDeafEvent event) {}
    public void onVoiceJoin(VoiceJoinEvent event) {}
    public void onVoiceLeave(VoiceLeaveEvent event) {}

    //Generic Events
    public void onGenericUserEvent(GenericUserEvent event) {}
    public void onGenericMessage(GenericMessageEvent event) {}
    public void onGenericGuildMessage(GenericGuildMessageEvent event) {}
    public void onGenericPrivateMessage(GenericPrivateMessageEvent event) {}
    public void onGenericTextChannel(GenericTextChannelEvent event) {}
    public void onGenericTextChannelUpdate(GenericTextChannelUpdateEvent event) {}
    public void onGenericVoiceChannel(GenericVoiceChannelEvent event) {}
    public void onGenericVoiceChannelUpdate(GenericVoiceChannelUpdateEvent event) {}
    public void onGenericGuildMember(GenericGuildMemberEvent event) {}
    public void onGenericGuild(GenericGuildEvent event) {}
    public void onGenericVoice(GenericVoiceEvent event) {}
    public void onGenericGuildRoleUpdate(GenericGuildRoleUpdateEvent event) {}

    @Override
    public void onEvent(Event event)
    {
        //JDA Events
        if (event instanceof ReadyEvent)
            onReady((ReadyEvent) event);

        //Message Events
        //Guild (TextChannel) Message Events
        else if (event instanceof GuildMessageReceivedEvent)
            onGuildMessageReceived((GuildMessageReceivedEvent) event);
        else if (event instanceof GuildMessageAcknowledgedEvent)
            onGuildMessageAcknowledged((GuildMessageAcknowledgedEvent) event);
        else if (event instanceof GuildMessageUpdateEvent)
            onGuildMessageUpdate((GuildMessageUpdateEvent) event);
        else if (event instanceof GuildMessageDeleteEvent)
            onGuildMessageDelete((GuildMessageDeleteEvent) event);
        else if (event instanceof GuildMessageEmbedEvent)
            onGuildMessageEmbed((GuildMessageEmbedEvent) event);
        //Private Message Events
        else if (event instanceof PrivateMessageReceivedEvent)
            onPrivateMessageReceived((PrivateMessageReceivedEvent) event);
        else if (event instanceof PrivateMessageAcknowledgedEvent)
            onPrivateMessageAcknowledged((PrivateMessageAcknowledgedEvent) event);
        else if (event instanceof PrivateMessageUpdateEvent)
            onPrivateMessageUpdate((PrivateMessageUpdateEvent) event);
        else if (event instanceof PrivateMessageDeleteEvent)
            onPrivateMessageDelete((PrivateMessageDeleteEvent) event);
        else if (event instanceof PrivateMessageEmbedEvent)
            onPrivateMessageEmbed((PrivateMessageEmbedEvent) event);
        //Combined Message Events (Combines Guild and Private message into 1 event)
        else if (event instanceof MessageReceivedEvent)
            onMessageReceived((MessageReceivedEvent) event);
        else if (event instanceof MessageAcknowledgedEvent)
            onMessageAcknowledged((MessageAcknowledgedEvent) event);
        else if (event instanceof MessageUpdateEvent)
            onMessageUpdate((MessageUpdateEvent) event);
        else if (event instanceof MessageDeleteEvent)
            onMessageDelete((MessageDeleteEvent) event);
        else if (event instanceof MessageEmbedEvent)
            onMessageEmbed((MessageEmbedEvent) event);
        //Invite Messages
        else if (event instanceof InviteReceivedEvent)
            onInviteReceived(((InviteReceivedEvent) event));

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
        else if (event instanceof GenericUserEvent)         //We check this here so that we don't catch 4 different update events.
            onGenericUserEvent((GenericUserEvent) event);   //Must be after all the others because they are subclasses of the Generic.

        //TextChannel Events
        else if (event instanceof TextChannelCreateEvent)
            onTextChannelCreate((TextChannelCreateEvent) event);
        else if (event instanceof TextChannelUpdateNameEvent)
            onTextChannelUpdateName((TextChannelUpdateNameEvent) event);
        else if (event instanceof TextChannelUpdateTopicEvent)
            onTextChannelUpdateTopic((TextChannelUpdateTopicEvent) event);
        else if (event instanceof TextChannelUpdatePositionEvent)
            onTextChannelUpdatePosition((TextChannelUpdatePositionEvent) event);
        else if (event instanceof TextChannelDeleteEvent)
            onTextChannelDelete((TextChannelDeleteEvent) event);
        else if (event instanceof TextChannelUpdatePermissionsEvent)
            onTextChannelUpdatePermissions((TextChannelUpdatePermissionsEvent) event);

        //VoiceChannel Events
        else if (event instanceof VoiceChannelCreateEvent)
            onVoiceChannelCreate((VoiceChannelCreateEvent) event);
        else if (event instanceof VoiceChannelUpdateNameEvent)
            onVoiceChannelUpdateName((VoiceChannelUpdateNameEvent) event);
        else if (event instanceof VoiceChannelUpdatePositionEvent)
            onVoiceChannelUpdatePosition((VoiceChannelUpdatePositionEvent) event);
        else if (event instanceof VoiceChannelUpdatePermissionsEvent)
            onVoiceChannelUpdatePermissions((VoiceChannelUpdatePermissionsEvent) event);
        else if (event instanceof VoiceChannelDeleteEvent)
            onVoiceChannelDelete((VoiceChannelDeleteEvent) event);

        //PrivateChannel Events
        else if (event instanceof PrivateChannelCreateEvent)
            onPrivateChannelCreate((PrivateChannelCreateEvent) event);

        //Guild Events
        else if (event instanceof GuildJoinEvent)
            onGuildJoin((GuildJoinEvent) event);
        else if (event instanceof GuildUpdateEvent)
            onGuildUpdate((GuildUpdateEvent) event);
        else if (event instanceof GuildLeaveEvent)
            onGuildLeave((GuildLeaveEvent) event);
        else if (event instanceof GuildMemberJoinEvent)
            onGuildMemberJoin((GuildMemberJoinEvent) event);
        else if (event instanceof GuildMemberBanEvent)
            onGuildMemberBan((GuildMemberBanEvent) event);
        else if (event instanceof GuildMemberUnbanEvent)
            onGuildMemberUnban((GuildMemberUnbanEvent) event);
        else if (event instanceof GuildMemberRoleAddEvent)
            onGuildMemberRoleAdd((GuildMemberRoleAddEvent) event);
        else if (event instanceof GuildMemberRoleRemoveEvent)
            onGuildMemberRoleRemove((GuildMemberRoleRemoveEvent) event);
        else if (event instanceof GuildRoleCreateEvent)
            onGuildRoleCreate((GuildRoleCreateEvent) event);
        else if (event instanceof GuildRoleDeleteEvent)
            onGuildRoleDelete((GuildRoleDeleteEvent) event);

        //GuildRoleUpdateEvents
        else if (event instanceof GuildRoleUpdateNameEvent)
            onGuildRoleUpdateName(((GuildRoleUpdateNameEvent) event));
        else if (event instanceof GuildRoleUpdateColorEvent)
            onGuildRoleUpdateColor(((GuildRoleUpdateColorEvent) event));
        else if (event instanceof GuildRoleUpdatePositionEvent)
            onGuildRoleUpdatePosition(((GuildRoleUpdatePositionEvent) event));
        else if (event instanceof GuildRoleUpdatePermissionEvent)
            onGuildRoleUpdatePermission(((GuildRoleUpdatePermissionEvent) event));
        else if (event instanceof GuildRoleUpdateGroupedEvent)
            onGuildRoleUpdateGrouped(((GuildRoleUpdateGroupedEvent) event));

        //Voice Events
        else if (event instanceof VoiceSelfMuteEvent)
            onVoiceSelfMute((VoiceSelfMuteEvent) event);
        else if (event instanceof VoiceServerMuteEvent)
            onVoiceServerMute((VoiceServerMuteEvent) event);
        else if (event instanceof VoiceSelfDeafEvent)
            onVoiceSelfDeaf((VoiceSelfDeafEvent) event);
        else if (event instanceof VoiceServerDeafEvent)
            onVoiceServerDeaf((VoiceServerDeafEvent) event);
        else if (event instanceof VoiceJoinEvent)
            onVoiceJoin((VoiceJoinEvent) event);
        else if (event instanceof VoiceLeaveEvent)
            onVoiceLeave((VoiceLeaveEvent) event);

        //Leave needs to be checked in a separate if-statement so that the Ban and Kick events will also fire this.
        if (event instanceof GuildMemberLeaveEvent)
            onGuildMemberLeave((GuildMemberLeaveEvent) event);

        //Grouped Mute/Deaf events
        if (event instanceof VoiceMuteEvent)
            onVoiceMute((VoiceMuteEvent) event);
        else if (event instanceof VoiceDeafEvent)
            onVoiceDeaf((VoiceDeafEvent) event);

        //Single GuildRoleUpdate event
        if (event instanceof GuildRoleUpdateEvent)
            onGuildRoleUpdate(((GuildRoleUpdateEvent) event));

        //Generic Events
        //Start a new if statement so that these are no overridden by the above events.
        if (event instanceof GenericPrivateMessageEvent)
            onGenericPrivateMessage((GenericPrivateMessageEvent) event);
        else if (event instanceof GenericGuildMessageEvent)
            onGenericGuildMessage((GenericGuildMessageEvent) event);
        else if (event instanceof GenericTextChannelUpdateEvent)
            onGenericTextChannelUpdate((GenericTextChannelUpdateEvent) event);
        else if (event instanceof GenericVoiceChannelUpdateEvent)
            onGenericVoiceChannelUpdate((GenericVoiceChannelUpdateEvent) event);
        else if (event instanceof GenericGuildMemberEvent)
            onGenericGuildMember((GenericGuildMemberEvent) event);
        else if (event instanceof GenericVoiceEvent)
            onGenericVoice((GenericVoiceEvent) event);
        else if (event instanceof GenericGuildRoleUpdateEvent)
            onGenericGuildRoleUpdate(((GenericGuildRoleUpdateEvent) event));

        //Generic events that have generic subclasses (the subclasses as above).
        if (event instanceof GenericGuildEvent)
            onGenericGuild((GenericGuildEvent) event);
        else if (event instanceof GenericTextChannelEvent)
            onGenericTextChannel((GenericTextChannelEvent) event);
        else if (event instanceof GenericVoiceChannelEvent)
            onGenericVoiceChannel((GenericVoiceChannelEvent) event);
        else if (event instanceof GenericMessageEvent)
            onGenericMessage((GenericMessageEvent) event);
    }
}
