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
package net.dv8tion.jda.core.hooks;

import net.dv8tion.jda.client.events.call.CallCreateEvent;
import net.dv8tion.jda.client.events.call.CallDeleteEvent;
import net.dv8tion.jda.client.events.call.GenericCallEvent;
import net.dv8tion.jda.client.events.call.update.CallUpdateRegionEvent;
import net.dv8tion.jda.client.events.call.update.CallUpdateRingingUsersEvent;
import net.dv8tion.jda.client.events.call.update.GenericCallUpdateEvent;
import net.dv8tion.jda.client.events.call.voice.*;
import net.dv8tion.jda.client.events.group.*;
import net.dv8tion.jda.client.events.group.update.GenericGroupUpdateEvent;
import net.dv8tion.jda.client.events.group.update.GroupUpdateIconEvent;
import net.dv8tion.jda.client.events.group.update.GroupUpdateNameEvent;
import net.dv8tion.jda.client.events.group.update.GroupUpdateOwnerEvent;
import net.dv8tion.jda.client.events.message.group.*;
import net.dv8tion.jda.client.events.relationship.*;
import net.dv8tion.jda.core.events.*;
import net.dv8tion.jda.core.events.channel.priv.PrivateChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.priv.PrivateChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.text.GenericTextChannelEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.text.update.*;
import net.dv8tion.jda.core.events.channel.voice.GenericVoiceChannelEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.voice.update.*;
import net.dv8tion.jda.core.events.guild.*;
import net.dv8tion.jda.core.events.guild.member.*;
import net.dv8tion.jda.core.events.guild.update.*;
import net.dv8tion.jda.core.events.guild.voice.*;
import net.dv8tion.jda.core.events.message.*;
import net.dv8tion.jda.core.events.message.guild.*;
import net.dv8tion.jda.core.events.message.priv.*;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.role.GenericRoleEvent;
import net.dv8tion.jda.core.events.role.RoleCreateEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.role.update.*;
import net.dv8tion.jda.core.events.self.*;
import net.dv8tion.jda.core.events.user.*;

import java.util.LinkedList;
import java.util.List;

public interface IEventManager
{
    List<Class<? extends Event>> eventList = new LinkedList<Class<? extends Event>>()
    {
        {
            add(Event.class);

            //JDA Events
            add(ReadyEvent.class);
            add(ResumedEvent.class);
            add(ReconnectedEvent.class);
            add(DisconnectEvent.class);
            add(ShutdownEvent.class);
            add(StatusChangeEvent.class);

            //User Events
            add(UserNameUpdateEvent.class);
            add(UserAvatarUpdateEvent.class);
            add(UserOnlineStatusUpdateEvent.class);
            add(UserGameUpdateEvent.class);
            add(UserTypingEvent.class);

            //Self Events. Fires only in relation to the currently logged in account.
            add(SelfUpdateAvatarEvent.class);
            add(SelfUpdateEmailEvent.class);
            add(SelfUpdateMFAEvent.class);
            add(SelfUpdateNameEvent.class);
            add(SelfUpdateVerifiedEvent.class);

            //Message Events
            //Guild (TextChannel) Message Events
            add(GuildMessageReceivedEvent.class);
            add(GuildMessageUpdateEvent.class);
            add(GuildMessageDeleteEvent.class);
            add(GuildMessageEmbedEvent.class);

            //Private Message Events
            add(PrivateMessageReceivedEvent.class);
            add(PrivateMessageUpdateEvent.class);
            add(PrivateMessageDeleteEvent.class);
            add(PrivateMessageEmbedEvent.class);

            //Combined Message Events (Combines Guild and Private message into 1 event)
            add(MessageReceivedEvent.class);
            add(MessageUpdateEvent.class);
            add(MessageDeleteEvent.class);
            add(MessageBulkDeleteEvent.class);
            add(MessageEmbedEvent.class);
            add(MessageReactionAddEvent.class);
            add(MessageReactionRemoveEvent.class);
            add(MessageReactionRemoveAllEvent.class);

//    add(InviteReceivedEvent.class);

            //TextChannel Events
            add(TextChannelDeleteEvent.class);
            add(TextChannelUpdateNameEvent.class);
            add(TextChannelUpdateTopicEvent.class);
            add(TextChannelUpdatePositionEvent.class);
            add(TextChannelUpdatePermissionsEvent.class);
            add(TextChannelCreateEvent.class);

            //VoiceChannel Events
            add(VoiceChannelDeleteEvent.class);
            add(VoiceChannelUpdateNameEvent.class);
            add(VoiceChannelUpdatePositionEvent.class);
            add(VoiceChannelUpdateUserLimitEvent.class);
            add(VoiceChannelUpdateBitrateEvent.class);
            add(VoiceChannelUpdatePermissionsEvent.class);
            add(VoiceChannelCreateEvent.class);

            //PrivateChannel Events
            add(PrivateChannelCreateEvent.class);
            add(PrivateChannelDeleteEvent.class);

            //Guild Events
            add(GuildJoinEvent.class);
            add(GuildLeaveEvent.class);
            add(GuildAvailableEvent.class);
            add(GuildUnavailableEvent.class);
            add(UnavailableGuildJoinedEvent.class);
            add(GuildBanEvent.class);
            add(GuildUnbanEvent.class);

            //Guild Update Events
            add(GuildUpdateAfkChannelEvent.class);
            add(GuildUpdateAfkTimeoutEvent.class);
            add(GuildUpdateIconEvent.class);
            add(GuildUpdateMFALevelEvent.class);
            add(GuildUpdateNameEvent.class);
            add(GuildUpdateNotificationLevelEvent.class);
            add(GuildUpdateOwnerEvent.class);
            add(GuildUpdateRegionEvent.class);
            add(GuildUpdateSplashEvent.class);
            add(GuildUpdateVerificationLevelEvent.class);

            //Guild Member Events
            add(GuildMemberJoinEvent.class);
            add(GuildMemberLeaveEvent.class);
            add(GuildMemberRoleAddEvent.class);
            add(GuildMemberRoleRemoveEvent.class);
            add(GuildMemberNickChangeEvent.class);

            //Guild Voice Events
            add(GuildVoiceJoinEvent.class);
            add(GuildVoiceMoveEvent.class);
            add(GuildVoiceLeaveEvent.class);
            add(GuildVoiceMuteEvent.class);
            add(GuildVoiceDeafenEvent.class);
            add(GuildVoiceGuildMuteEvent.class);
            add(GuildVoiceGuildDeafenEvent.class);
            add(GuildVoiceSelfMuteEvent.class);
            add(GuildVoiceSelfDeafenEvent.class);
            add(GuildVoiceSuppressEvent.class);

            //Role events
            add(RoleCreateEvent.class);
            add(RoleDeleteEvent.class);

            //Role Update Events
            add(RoleUpdateColorEvent.class);
            add(RoleUpdateHoistedEvent.class);
            add(RoleUpdateMentionableEvent.class);
            add(RoleUpdateNameEvent.class);
            add(RoleUpdatePermissionsEvent.class);
            add(RoleUpdatePositionEvent.class);

//    //Audio System Events
//    add(AudioConnectEvent.class);
//    add(AudioDisconnectEvent.class);
//    add(AudioUnableToConnectEvent.class);
//    add(AudioTimeoutEvent.class);
//    add(AudioRegionChangeEvent.class);

            //Generic Events
            add(GenericMessageEvent.class);
            add(GenericMessageReactionEvent.class);
            add(GenericGuildMessageEvent.class);
            add(GenericPrivateMessageEvent.class);
            add(GenericUserEvent.class);
            add(GenericSelfUpdateEvent.class);
            add(GenericTextChannelEvent.class);
            add(GenericTextChannelUpdateEvent.class);
            add(GenericVoiceChannelEvent.class);
            add(GenericVoiceChannelUpdateEvent.class);
            add(GenericGuildEvent.class);
            add(GenericGuildUpdateEvent.class);
            add(GenericGuildMemberEvent.class);
            add(GenericGuildVoiceEvent.class);
            add(GenericRoleEvent.class);
            add(GenericRoleUpdateEvent.class);
//    add(GenericAudioEvent.class);


            // ==========================================================================================
            // |                                   Client Only Events                                   |
            // ==========================================================================================

            //Relationship Events
            add(FriendAddedEvent.class);
            add(FriendRemovedEvent.class);
            add(UserBlockedEvent.class);
            add(UserUnblockedEvent.class);
            add(FriendRequestSentEvent.class);
            add(FriendRequestCanceledEvent.class);
            add(FriendRequestReceivedEvent.class);
            add(FriendRequestIgnoredEvent.class);

            //Group Events
            add(GroupJoinEvent.class);
            add(GroupLeaveEvent.class);
            add(GroupUserJoinEvent.class);
            add(GroupUserLeaveEvent.class);

            //Group Message Events
            add(GroupMessageReceivedEvent.class);
            add(GroupMessageUpdateEvent.class);
            add(GroupMessageDeleteEvent.class);
            add(GroupMessageEmbedEvent.class);

            //Group Update Events
            add(GroupUpdateIconEvent.class);
            add(GroupUpdateNameEvent.class);
            add(GroupUpdateOwnerEvent.class);

            //Call Events
            add(CallCreateEvent.class);
            add(CallDeleteEvent.class);

            //Call Update Events
            add(CallUpdateRegionEvent.class);
            add(CallUpdateRingingUsersEvent.class);

            //Call Voice Events
            add(CallVoiceJoinEvent.class);
            add(CallVoiceLeaveEvent.class);
            add(CallVoiceSelfMuteEvent.class);
            add(CallVoiceSelfDeafenEvent.class);

            //Client Only Generic Events
            add(GenericRelationshipEvent.class);
            add(GenericRelationshipAddEvent.class);
            add(GenericRelationshipRemoveEvent.class);
            add(GenericGroupEvent.class);
            add(GenericGroupMessageEvent.class);
            add(GenericGroupUpdateEvent.class);
            add(GenericCallEvent.class);
            add(GenericCallUpdateEvent.class);
            add(GenericCallVoiceEvent.class);

        }
    };

    void register(Object listener);

    void unregister(Object listener);

    void handle(Event event);

    List<Object> getRegisteredListeners();
}
