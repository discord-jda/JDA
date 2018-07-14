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
import net.dv8tion.jda.client.events.message.group.react.GenericGroupMessageReactionEvent;
import net.dv8tion.jda.client.events.message.group.react.GroupMessageReactionAddEvent;
import net.dv8tion.jda.client.events.message.group.react.GroupMessageReactionRemoveAllEvent;
import net.dv8tion.jda.client.events.message.group.react.GroupMessageReactionRemoveEvent;
import net.dv8tion.jda.client.events.relationship.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.events.*;
import net.dv8tion.jda.core.events.channel.category.CategoryCreateEvent;
import net.dv8tion.jda.core.events.channel.category.CategoryDeleteEvent;
import net.dv8tion.jda.core.events.channel.category.GenericCategoryEvent;
import net.dv8tion.jda.core.events.channel.category.update.CategoryUpdateNameEvent;
import net.dv8tion.jda.core.events.channel.category.update.CategoryUpdatePermissionsEvent;
import net.dv8tion.jda.core.events.channel.category.update.CategoryUpdatePositionEvent;
import net.dv8tion.jda.core.events.channel.category.update.GenericCategoryUpdateEvent;
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
import net.dv8tion.jda.core.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.core.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.core.events.emote.GenericEmoteEvent;
import net.dv8tion.jda.core.events.emote.update.EmoteUpdateNameEvent;
import net.dv8tion.jda.core.events.emote.update.EmoteUpdateRolesEvent;
import net.dv8tion.jda.core.events.emote.update.GenericEmoteUpdateEvent;
import net.dv8tion.jda.core.events.guild.*;
import net.dv8tion.jda.core.events.guild.member.*;
import net.dv8tion.jda.core.events.guild.update.*;
import net.dv8tion.jda.core.events.guild.voice.*;
import net.dv8tion.jda.core.events.http.HttpRequestEvent;
import net.dv8tion.jda.core.events.message.*;
import net.dv8tion.jda.core.events.message.guild.*;
import net.dv8tion.jda.core.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.message.priv.*;
import net.dv8tion.jda.core.events.message.priv.react.GenericPrivateMessageReactionEvent;
import net.dv8tion.jda.core.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.priv.react.PrivateMessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.role.GenericRoleEvent;
import net.dv8tion.jda.core.events.role.RoleCreateEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.role.update.*;
import net.dv8tion.jda.core.events.self.*;
import net.dv8tion.jda.core.events.user.GenericUserEvent;
import net.dv8tion.jda.core.events.user.UserTypingEvent;
import net.dv8tion.jda.core.events.user.update.*;

/**
 * An abstract implementation of {@link net.dv8tion.jda.core.hooks.EventListener EventListener} which divides {@link net.dv8tion.jda.core.events.Event Events}
 * for you.
 *
 * <p><b>Example:</b>
 * <br><pre><code>
 * public class MyReadyListener extends ListenerAdapter
 * {
 *    {@literal @Override}
 *     public void onReady(ReadyEvent event)
 *     {
 *         System.out.println("I am ready to go!");
 *     }
 *
 *    {@literal @Override}
 *     public void onMessageReceived(MessageReceivedEvent event)
 *     {
 *         System.out.printf("[%s]: %s\n", event.getAuthor().getName(), event.getMessage().getContentDisplay());
 *     }
 * }
 * </code></pre>
 *
 * @see net.dv8tion.jda.core.hooks.EventListener EventListener
 * @see net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventManager
 */
public abstract class ListenerAdapter implements EventListener
{
    public void onGenericEvent(Event event) {}
    public void onGenericUpdate(UpdateEvent<?, ?> event) {}

    //JDA Events
    public void onReady(ReadyEvent event) {}
    public void onResume(ResumedEvent event) {}
    public void onReconnect(ReconnectedEvent event) {}
    public void onDisconnect(DisconnectEvent event) {}
    public void onShutdown(ShutdownEvent event) {}
    public void onStatusChange(StatusChangeEvent event) {}
    public void onException(ExceptionEvent event) {}

    //User Events
    public void onUserUpdateName(UserUpdateNameEvent event) {}
    public void onUserUpdateDiscriminator(UserUpdateDiscriminatorEvent event) {}
    public void onUserUpdateAvatar(UserUpdateAvatarEvent event) {}
    public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {}
    public void onUserUpdateGame(UserUpdateGameEvent event) {}
    public void onUserTyping(UserTypingEvent event) {}

    //Self Events. Fires only in relation to the currently logged in account.
    public void onSelfUpdateAvatar(SelfUpdateAvatarEvent event) {}
    public void onSelfUpdateEmail(SelfUpdateEmailEvent event) {}
    public void onSelfUpdateMFA(SelfUpdateMFAEvent event) {}
    public void onSelfUpdateName(SelfUpdateNameEvent event) {}
    public void onSelfUpdateVerified(SelfUpdateVerifiedEvent event) {}

    //Message Events
    //Guild (TextChannel) Message Events
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {}
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {}
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {}
    public void onGuildMessageEmbed(GuildMessageEmbedEvent event) {}
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {}
    public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {}
    public void onGuildMessageReactionRemoveAll(GuildMessageReactionRemoveAllEvent event) {}

    //Private Message Events
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {}
    public void onPrivateMessageUpdate(PrivateMessageUpdateEvent event) {}
    public void onPrivateMessageDelete(PrivateMessageDeleteEvent event) {}
    public void onPrivateMessageEmbed(PrivateMessageEmbedEvent event) {}
    public void onPrivateMessageReactionAdd(PrivateMessageReactionAddEvent event) {}
    public void onPrivateMessageReactionRemove(PrivateMessageReactionRemoveEvent event) {}

    //Combined Message Events (Combines Guild and Private message into 1 event)
    public void onMessageReceived(MessageReceivedEvent event) {}
    public void onMessageUpdate(MessageUpdateEvent event) {}
    public void onMessageDelete(MessageDeleteEvent event) {}
    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {}
    public void onMessageEmbed(MessageEmbedEvent event) {}
    public void onMessageReactionAdd(MessageReactionAddEvent event) {}
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {}
    public void onMessageReactionRemoveAll(MessageReactionRemoveAllEvent event) {}

    //TextChannel Events
    public void onTextChannelDelete(TextChannelDeleteEvent event) {}
    public void onTextChannelUpdateName(TextChannelUpdateNameEvent event) {}
    public void onTextChannelUpdateTopic(TextChannelUpdateTopicEvent event) {}
    public void onTextChannelUpdatePosition(TextChannelUpdatePositionEvent event) {}
    public void onTextChannelUpdatePermissions(TextChannelUpdatePermissionsEvent event) {}
    public void onTextChannelUpdateNSFW(TextChannelUpdateNSFWEvent event) {}
    public void onTextChannelUpdateParent(TextChannelUpdateParentEvent event) {}
    public void onTextChannelCreate(TextChannelCreateEvent event) {}

    //VoiceChannel Events
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {}
    public void onVoiceChannelUpdateName(VoiceChannelUpdateNameEvent event) {}
    public void onVoiceChannelUpdatePosition(VoiceChannelUpdatePositionEvent event) {}
    public void onVoiceChannelUpdateUserLimit(VoiceChannelUpdateUserLimitEvent event) {}
    public void onVoiceChannelUpdateBitrate(VoiceChannelUpdateBitrateEvent event) {}
    public void onVoiceChannelUpdatePermissions(VoiceChannelUpdatePermissionsEvent event) {}
    public void onVoiceChannelUpdateParent(VoiceChannelUpdateParentEvent event) {}
    public void onVoiceChannelCreate(VoiceChannelCreateEvent event) {}

    //Category Events
    public void onCategoryDelete(CategoryDeleteEvent event) {}
    public void onCategoryUpdateName(CategoryUpdateNameEvent event) {}
    public void onCategoryUpdatePosition(CategoryUpdatePositionEvent event) {}
    public void onCategoryUpdatePermissions(CategoryUpdatePermissionsEvent event) {}
    public void onCategoryCreate(CategoryCreateEvent event) {}

    //PrivateChannel Events
    public void onPrivateChannelCreate(PrivateChannelCreateEvent event) {}
    public void onPrivateChannelDelete(PrivateChannelDeleteEvent event) {}

    //Guild Events
    public void onGuildJoin(GuildJoinEvent event) {}
    public void onGuildLeave(GuildLeaveEvent event) {}
    public void onGuildAvailable(GuildAvailableEvent event) {}
    public void onGuildUnavailable(GuildUnavailableEvent event) {}
    public void onUnavailableGuildJoined(UnavailableGuildJoinedEvent event) {}
    public void onGuildBan(GuildBanEvent event) {}
    public void onGuildUnban(GuildUnbanEvent event) {}

    //Guild Update Events
    public void onGuildUpdateAfkChannel(GuildUpdateAfkChannelEvent event) {}
    public void onGuildUpdateSystemChannel(GuildUpdateSystemChannelEvent event) {}
    public void onGuildUpdateAfkTimeout(GuildUpdateAfkTimeoutEvent event) {}
    public void onGuildUpdateExplicitContentLevel(GuildUpdateExplicitContentLevelEvent event) {}
    public void onGuildUpdateIcon(GuildUpdateIconEvent event) {}
    public void onGuildUpdateMFALevel(GuildUpdateMFALevelEvent event) {}
    public void onGuildUpdateName(GuildUpdateNameEvent event){}
    public void onGuildUpdateNotificationLevel(GuildUpdateNotificationLevelEvent event) {}
    public void onGuildUpdateOwner(GuildUpdateOwnerEvent event) {}
    public void onGuildUpdateRegion(GuildUpdateRegionEvent event) {}
    public void onGuildUpdateSplash(GuildUpdateSplashEvent event) {}
    public void onGuildUpdateVerificationLevel(GuildUpdateVerificationLevelEvent event) {}
    public void onGuildUpdateFeatures(GuildUpdateFeaturesEvent event) {}

    //Guild Member Events
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {}
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {}
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {}
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {}
    public void onGuildMemberNickChange(GuildMemberNickChangeEvent event) {}

    //Guild Voice Events
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {}
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {}
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {}
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {}
    public void onGuildVoiceMute(GuildVoiceMuteEvent event) {}
    public void onGuildVoiceDeafen(GuildVoiceDeafenEvent event) {}
    public void onGuildVoiceGuildMute(GuildVoiceGuildMuteEvent event) {}
    public void onGuildVoiceGuildDeafen(GuildVoiceGuildDeafenEvent event) {}
    public void onGuildVoiceSelfMute(GuildVoiceSelfMuteEvent event) {}
    public void onGuildVoiceSelfDeafen(GuildVoiceSelfDeafenEvent event) {}
    public void onGuildVoiceSuppress(GuildVoiceSuppressEvent event) {}

    //Role events
    public void onRoleCreate(RoleCreateEvent event) {}
    public void onRoleDelete(RoleDeleteEvent event) {}

    //Role Update Events
    public void onRoleUpdateColor(RoleUpdateColorEvent event) {}
    public void onRoleUpdateHoisted(RoleUpdateHoistedEvent event) {}
    public void onRoleUpdateMentionable(RoleUpdateMentionableEvent event) {}
    public void onRoleUpdateName(RoleUpdateNameEvent event) {}
    public void onRoleUpdatePermissions(RoleUpdatePermissionsEvent event) {}
    public void onRoleUpdatePosition(RoleUpdatePositionEvent event) {}

    //Emote Events
    public void onEmoteAdded(EmoteAddedEvent event) {}
    public void onEmoteRemoved(EmoteRemovedEvent event) {}

    //Emote Update Events
    public void onEmoteUpdateName(EmoteUpdateNameEvent event) {}
    public void onEmoteUpdateRoles(EmoteUpdateRolesEvent event) {}

    // Debug Events
    public void onHttpRequest(HttpRequestEvent event) {}

    //Generic Events
    public void onGenericMessage(GenericMessageEvent event) {}
    public void onGenericMessageReaction(GenericMessageReactionEvent event) {}
    public void onGenericGuildMessage(GenericGuildMessageEvent event) {}
    public void onGenericGuildMessageReaction(GenericGuildMessageReactionEvent event) {}
    public void onGenericPrivateMessage(GenericPrivateMessageEvent event) {}
    public void onGenericPrivateMessageReaction(GenericPrivateMessageReactionEvent event) {}
    public void onGenericUser(GenericUserEvent event) {}
    public void onGenericUserPresence(GenericUserPresenceEvent event) {}
    public void onGenericSelfUpdate(GenericSelfUpdateEvent event) {}
    public void onGenericTextChannel(GenericTextChannelEvent event) {}
    public void onGenericTextChannelUpdate(GenericTextChannelUpdateEvent event) {}
    public void onGenericVoiceChannel(GenericVoiceChannelEvent event) {}
    public void onGenericVoiceChannelUpdate(GenericVoiceChannelUpdateEvent event) {}
    public void onGenericCategory(GenericCategoryEvent event) {}
    public void onGenericCategoryUpdate(GenericCategoryUpdateEvent event) {}
    public void onGenericGuild(GenericGuildEvent event) {}
    public void onGenericGuildUpdate(GenericGuildUpdateEvent event) {}
    public void onGenericGuildMember(GenericGuildMemberEvent event) {}
    public void onGenericGuildVoice(GenericGuildVoiceEvent event) {}
    public void onGenericRole(GenericRoleEvent event) {}
    public void onGenericRoleUpdate(GenericRoleUpdateEvent event) {}
    public void onGenericEmote(GenericEmoteEvent event) {}
    public void onGenericEmoteUpdate(GenericEmoteUpdateEvent event) {}

    // ==========================================================================================
    // |                                   Client Only Events                                   |
    // ==========================================================================================

    //Relationship Events
    public void onFriendAdded(FriendAddedEvent event) {}
    public void onFriendRemoved(FriendRemovedEvent event) {}
    public void onUserBlocked(UserBlockedEvent event) {}
    public void onUserUnblocked(UserUnblockedEvent event) {}
    public void onFriendRequestSent(FriendRequestSentEvent event) {}
    public void onFriendRequestCanceled(FriendRequestCanceledEvent event) {}
    public void onFriendRequestReceived(FriendRequestReceivedEvent event) {}
    public void onFriendRequestIgnored(FriendRequestIgnoredEvent event) {}

    //Group Events
    public void onGroupJoin(GroupJoinEvent event) {}
    public void onGroupLeave(GroupLeaveEvent event) {}
    public void onGroupUserJoin(GroupUserJoinEvent event) {}
    public void onGroupUserLeave(GroupUserLeaveEvent event) {}

    //Group Message Events
    public void onGroupMessageReceived(GroupMessageReceivedEvent event) {}
    public void onGroupMessageUpdate(GroupMessageUpdateEvent event) {}
    public void onGroupMessageDelete(GroupMessageDeleteEvent event) {}
    public void onGroupMessageEmbed(GroupMessageEmbedEvent event) {}
    public void onGroupMessageReactionAdd(GroupMessageReactionAddEvent event) {}
    public void onGroupMessageReactionRemove(GroupMessageReactionRemoveEvent event) {}
    public void onGroupMessageReactionRemoveAll(GroupMessageReactionRemoveAllEvent event) {}

    //Group Update Events
    public void onGroupUpdateIcon(GroupUpdateIconEvent event) {}
    public void onGroupUpdateName(GroupUpdateNameEvent event){}
    public void onGroupUpdateOwner(GroupUpdateOwnerEvent event) {}

    //Call Events
    public void onCallCreate(CallCreateEvent event) {}
    public void onCallDelete(CallDeleteEvent event) {}

    //Call Update Events
    public void onCallUpdateRegion(CallUpdateRegionEvent event) {}
    public void onCallUpdateRingingUsers(CallUpdateRingingUsersEvent event) {}

    //Call Voice Events
    public void onCallVoiceJoin(CallVoiceJoinEvent event) {}
    public void onCallVoiceLeave(CallVoiceLeaveEvent event) {}
    public void onCallVoiceSelfMute(CallVoiceSelfMuteEvent event) {}
    public void onCallVoiceSelfDeafen(CallVoiceSelfDeafenEvent event) {}

    //Client Only Generic Events
    public void onGenericRelationship(GenericRelationshipEvent event) {}
    public void onGenericRelationshipAdd(GenericRelationshipAddEvent event) {}
    public void onGenericRelationshipRemove(GenericRelationshipRemoveEvent event) {}
    public void onGenericGroup(GenericGroupEvent event) {}
    public void onGenericGroupMessage(GenericGroupMessageEvent event) {}
    public void onGenericGroupMessageReaction(GenericGroupMessageReactionEvent event) {}
    public void onGenericGroupUpdate(GenericGroupUpdateEvent event) {}
    public void onGenericCall(GenericCallEvent event) {}
    public void onGenericCallUpdate(GenericCallUpdateEvent event) {}
    public void onGenericCallVoice(GenericCallVoiceEvent event) {}

    @Override
    public final void onEvent(Event event)
    {
        onGenericEvent(event);
        if (event instanceof UpdateEvent)
            onGenericUpdate((UpdateEvent<?, ?>) event);
        //JDA Events
        if (event instanceof ReadyEvent)
            onReady((ReadyEvent) event);
        else if (event instanceof ResumedEvent)
            onResume((ResumedEvent) event);
        else if (event instanceof ReconnectedEvent)
            onReconnect((ReconnectedEvent) event);
        else if (event instanceof DisconnectEvent)
            onDisconnect((DisconnectEvent) event);
        else if (event instanceof ShutdownEvent)
            onShutdown((ShutdownEvent) event);
        else if (event instanceof StatusChangeEvent)
            onStatusChange((StatusChangeEvent) event);
        else if (event instanceof ExceptionEvent)
            onException((ExceptionEvent) event);

        //Message Events
        //Guild (TextChannel) Message Events
        else if (event instanceof GuildMessageReceivedEvent)
            onGuildMessageReceived((GuildMessageReceivedEvent) event);
        else if (event instanceof GuildMessageUpdateEvent)
            onGuildMessageUpdate((GuildMessageUpdateEvent) event);
        else if (event instanceof GuildMessageDeleteEvent)
            onGuildMessageDelete((GuildMessageDeleteEvent) event);
        else if (event instanceof GuildMessageEmbedEvent)
            onGuildMessageEmbed((GuildMessageEmbedEvent) event);
        else if (event instanceof GuildMessageReactionAddEvent)
            onGuildMessageReactionAdd((GuildMessageReactionAddEvent) event);
        else if (event instanceof GuildMessageReactionRemoveEvent)
            onGuildMessageReactionRemove((GuildMessageReactionRemoveEvent) event);
        else if (event instanceof GuildMessageReactionRemoveAllEvent)
            onGuildMessageReactionRemoveAll((GuildMessageReactionRemoveAllEvent) event);

        //Private Message Events
        else if (event instanceof PrivateMessageReceivedEvent)
            onPrivateMessageReceived((PrivateMessageReceivedEvent) event);
        else if (event instanceof PrivateMessageUpdateEvent)
            onPrivateMessageUpdate((PrivateMessageUpdateEvent) event);
        else if (event instanceof PrivateMessageDeleteEvent)
            onPrivateMessageDelete((PrivateMessageDeleteEvent) event);
        else if (event instanceof PrivateMessageEmbedEvent)
            onPrivateMessageEmbed((PrivateMessageEmbedEvent) event);
        else if (event instanceof PrivateMessageReactionAddEvent)
            onPrivateMessageReactionAdd((PrivateMessageReactionAddEvent) event);
        else if (event instanceof PrivateMessageReactionRemoveEvent)
            onPrivateMessageReactionRemove((PrivateMessageReactionRemoveEvent) event);

        //Combined Message Events (Combines Guild and Private message into 1 event)
        else if (event instanceof MessageReceivedEvent)
            onMessageReceived((MessageReceivedEvent) event);
        else if (event instanceof MessageUpdateEvent)
            onMessageUpdate((MessageUpdateEvent) event);
        else if (event instanceof MessageDeleteEvent)
            onMessageDelete((MessageDeleteEvent) event);
        else if (event instanceof MessageBulkDeleteEvent)
            onMessageBulkDelete((MessageBulkDeleteEvent) event);
        else if (event instanceof MessageEmbedEvent)
            onMessageEmbed((MessageEmbedEvent) event);
        else if (event instanceof MessageReactionAddEvent)
            onMessageReactionAdd((MessageReactionAddEvent) event);
        else if (event instanceof MessageReactionRemoveEvent)
            onMessageReactionRemove((MessageReactionRemoveEvent) event);
        else if (event instanceof MessageReactionRemoveAllEvent)
            onMessageReactionRemoveAll((MessageReactionRemoveAllEvent) event);

        //User Events
        else if (event instanceof UserUpdateNameEvent)
            onUserUpdateName((UserUpdateNameEvent) event);
        else if (event instanceof UserUpdateDiscriminatorEvent)
            onUserUpdateDiscriminator((UserUpdateDiscriminatorEvent) event);
        else if (event instanceof UserUpdateAvatarEvent)
            onUserUpdateAvatar((UserUpdateAvatarEvent) event);
        else if (event instanceof UserUpdateGameEvent)
            onUserUpdateGame((UserUpdateGameEvent) event);
        else if (event instanceof UserUpdateOnlineStatusEvent)
            onUserUpdateOnlineStatus((UserUpdateOnlineStatusEvent) event);
        else if (event instanceof UserTypingEvent)
            onUserTyping((UserTypingEvent) event);

        //Self Events
        else if (event instanceof SelfUpdateAvatarEvent)
            onSelfUpdateAvatar((SelfUpdateAvatarEvent) event);
        else if (event instanceof SelfUpdateEmailEvent)
            onSelfUpdateEmail((SelfUpdateEmailEvent) event);
        else if (event instanceof SelfUpdateMFAEvent)
            onSelfUpdateMFA((SelfUpdateMFAEvent) event);
        else if (event instanceof SelfUpdateNameEvent)
            onSelfUpdateName((SelfUpdateNameEvent) event);
        else if (event instanceof SelfUpdateVerifiedEvent)
            onSelfUpdateVerified((SelfUpdateVerifiedEvent) event);

        //TextChannel Events
        else if (event instanceof TextChannelCreateEvent)
            onTextChannelCreate((TextChannelCreateEvent) event);
        else if (event instanceof TextChannelUpdateNameEvent)
            onTextChannelUpdateName((TextChannelUpdateNameEvent) event);
        else if (event instanceof TextChannelUpdateTopicEvent)
            onTextChannelUpdateTopic((TextChannelUpdateTopicEvent) event);
        else if (event instanceof TextChannelUpdatePositionEvent)
            onTextChannelUpdatePosition((TextChannelUpdatePositionEvent) event);
        else if (event instanceof TextChannelUpdatePermissionsEvent)
            onTextChannelUpdatePermissions((TextChannelUpdatePermissionsEvent) event);
        else if (event instanceof TextChannelUpdateNSFWEvent)
            onTextChannelUpdateNSFW((TextChannelUpdateNSFWEvent) event);
        else if (event instanceof TextChannelUpdateParentEvent)
            onTextChannelUpdateParent((TextChannelUpdateParentEvent) event);
        else if (event instanceof TextChannelDeleteEvent)
        onTextChannelDelete((TextChannelDeleteEvent) event);

        //VoiceChannel Events
        else if (event instanceof VoiceChannelCreateEvent)
            onVoiceChannelCreate((VoiceChannelCreateEvent) event);
        else if (event instanceof VoiceChannelUpdateNameEvent)
            onVoiceChannelUpdateName((VoiceChannelUpdateNameEvent) event);
        else if (event instanceof VoiceChannelUpdatePositionEvent)
            onVoiceChannelUpdatePosition((VoiceChannelUpdatePositionEvent) event);
        else if (event instanceof VoiceChannelUpdateUserLimitEvent)
            onVoiceChannelUpdateUserLimit((VoiceChannelUpdateUserLimitEvent) event);
        else if (event instanceof VoiceChannelUpdateBitrateEvent)
            onVoiceChannelUpdateBitrate((VoiceChannelUpdateBitrateEvent) event);
        else if (event instanceof VoiceChannelUpdatePermissionsEvent)
            onVoiceChannelUpdatePermissions((VoiceChannelUpdatePermissionsEvent) event);
        else if (event instanceof VoiceChannelUpdateParentEvent)
            onVoiceChannelUpdateParent((VoiceChannelUpdateParentEvent) event);
        else if (event instanceof VoiceChannelDeleteEvent)
            onVoiceChannelDelete((VoiceChannelDeleteEvent) event);

        //Category Events
        else if (event instanceof CategoryCreateEvent)
            onCategoryCreate((CategoryCreateEvent) event);
        else if (event instanceof CategoryUpdateNameEvent)
            onCategoryUpdateName((CategoryUpdateNameEvent) event);
        else if (event instanceof CategoryUpdatePositionEvent)
            onCategoryUpdatePosition((CategoryUpdatePositionEvent) event);
        else if (event instanceof CategoryUpdatePermissionsEvent)
            onCategoryUpdatePermissions((CategoryUpdatePermissionsEvent) event);
        else if (event instanceof CategoryDeleteEvent)
            onCategoryDelete((CategoryDeleteEvent) event);

        //PrivateChannel Events
        else if (event instanceof PrivateChannelCreateEvent)
            onPrivateChannelCreate((PrivateChannelCreateEvent) event);
        else if (event instanceof PrivateChannelDeleteEvent)
            onPrivateChannelDelete((PrivateChannelDeleteEvent) event);

        //Guild Events
        else if (event instanceof GuildJoinEvent)
            onGuildJoin((GuildJoinEvent) event);
        else if (event instanceof GuildLeaveEvent)
            onGuildLeave((GuildLeaveEvent) event);
        else if (event instanceof GuildAvailableEvent)
            onGuildAvailable((GuildAvailableEvent) event);
        else if (event instanceof GuildUnavailableEvent)
            onGuildUnavailable((GuildUnavailableEvent) event);
        else if (event instanceof UnavailableGuildJoinedEvent)
            onUnavailableGuildJoined((UnavailableGuildJoinedEvent) event);
        else if (event instanceof GuildBanEvent)
            onGuildBan((GuildBanEvent) event);
        else if (event instanceof GuildUnbanEvent)
            onGuildUnban((GuildUnbanEvent) event);

        //Guild Update Events
        else if (event instanceof GuildUpdateAfkChannelEvent)
            onGuildUpdateAfkChannel((GuildUpdateAfkChannelEvent) event);
        else if (event instanceof GuildUpdateSystemChannelEvent)
            onGuildUpdateSystemChannel((GuildUpdateSystemChannelEvent) event);
        else if (event instanceof GuildUpdateAfkTimeoutEvent)
            onGuildUpdateAfkTimeout((GuildUpdateAfkTimeoutEvent) event);
        else if (event instanceof GuildUpdateExplicitContentLevelEvent)
            onGuildUpdateExplicitContentLevel((GuildUpdateExplicitContentLevelEvent) event);
        else if (event instanceof GuildUpdateIconEvent)
            onGuildUpdateIcon((GuildUpdateIconEvent) event);
        else if (event instanceof GuildUpdateMFALevelEvent)
            onGuildUpdateMFALevel((GuildUpdateMFALevelEvent) event);
        else if (event instanceof GuildUpdateNameEvent)
            onGuildUpdateName((GuildUpdateNameEvent) event);
        else if (event instanceof GuildUpdateNotificationLevelEvent)
            onGuildUpdateNotificationLevel((GuildUpdateNotificationLevelEvent) event);
        else if (event instanceof GuildUpdateOwnerEvent)
            onGuildUpdateOwner((GuildUpdateOwnerEvent) event);
        else if (event instanceof GuildUpdateRegionEvent)
            onGuildUpdateRegion((GuildUpdateRegionEvent) event);
        else if (event instanceof GuildUpdateSplashEvent)
            onGuildUpdateSplash((GuildUpdateSplashEvent) event);
        else if (event instanceof GuildUpdateVerificationLevelEvent)
            onGuildUpdateVerificationLevel((GuildUpdateVerificationLevelEvent) event);
        else if (event instanceof GuildUpdateFeaturesEvent)
            onGuildUpdateFeatures((GuildUpdateFeaturesEvent) event);

        //Guild Member Events
        else if (event instanceof GuildMemberJoinEvent)
            onGuildMemberJoin((GuildMemberJoinEvent) event);
        else if (event instanceof GuildMemberLeaveEvent)
            onGuildMemberLeave((GuildMemberLeaveEvent) event);
        else if (event instanceof GuildMemberRoleAddEvent)
            onGuildMemberRoleAdd((GuildMemberRoleAddEvent) event);
        else if (event instanceof GuildMemberRoleRemoveEvent)
            onGuildMemberRoleRemove((GuildMemberRoleRemoveEvent) event);
        else if (event instanceof GuildMemberNickChangeEvent)
            onGuildMemberNickChange((GuildMemberNickChangeEvent) event);

        //Guild Voice Events
        else if (event instanceof GuildVoiceJoinEvent)
            onGuildVoiceJoin((GuildVoiceJoinEvent) event);
        else if (event instanceof GuildVoiceMoveEvent)
            onGuildVoiceMove((GuildVoiceMoveEvent) event);
        else if (event instanceof GuildVoiceLeaveEvent)
            onGuildVoiceLeave((GuildVoiceLeaveEvent) event);
        else if (event instanceof GuildVoiceMuteEvent)
            onGuildVoiceMute((GuildVoiceMuteEvent) event);
        else if (event instanceof GuildVoiceDeafenEvent)
            onGuildVoiceDeafen((GuildVoiceDeafenEvent) event);
        else if (event instanceof GuildVoiceGuildMuteEvent)
            onGuildVoiceGuildMute((GuildVoiceGuildMuteEvent) event);
        else if (event instanceof GuildVoiceGuildDeafenEvent)
            onGuildVoiceGuildDeafen((GuildVoiceGuildDeafenEvent) event);
        else if (event instanceof GuildVoiceSelfMuteEvent)
            onGuildVoiceSelfMute((GuildVoiceSelfMuteEvent) event);
        else if (event instanceof GuildVoiceSelfDeafenEvent)
            onGuildVoiceSelfDeafen((GuildVoiceSelfDeafenEvent) event);
        else if (event instanceof GuildVoiceSuppressEvent)
            onGuildVoiceSuppress((GuildVoiceSuppressEvent) event);

        //Role Events
        else if (event instanceof RoleCreateEvent)
            onRoleCreate((RoleCreateEvent) event);
        else if (event instanceof RoleDeleteEvent)
            onRoleDelete((RoleDeleteEvent) event);

        //Role Update Events
        else if (event instanceof RoleUpdateColorEvent)
            onRoleUpdateColor(((RoleUpdateColorEvent) event));
        else if (event instanceof RoleUpdateHoistedEvent)
            onRoleUpdateHoisted(((RoleUpdateHoistedEvent) event));
        else if (event instanceof RoleUpdateMentionableEvent)
            onRoleUpdateMentionable((RoleUpdateMentionableEvent) event);
        else if (event instanceof RoleUpdateNameEvent)
            onRoleUpdateName(((RoleUpdateNameEvent) event));
        else if (event instanceof RoleUpdatePermissionsEvent)
            onRoleUpdatePermissions(((RoleUpdatePermissionsEvent) event));
        else if (event instanceof RoleUpdatePositionEvent)
            onRoleUpdatePosition(((RoleUpdatePositionEvent) event));

        //Emote Events
        else if (event instanceof EmoteAddedEvent)
            onEmoteAdded((EmoteAddedEvent) event);
        else if (event instanceof EmoteRemovedEvent)
            onEmoteRemoved((EmoteRemovedEvent) event);

        //Emote Update Events
        else if (event instanceof EmoteUpdateNameEvent)
            onEmoteUpdateName((EmoteUpdateNameEvent) event);
        else if (event instanceof EmoteUpdateRolesEvent)
            onEmoteUpdateRoles((EmoteUpdateRolesEvent) event);

        // Debug Events
        else if (event instanceof HttpRequestEvent)
            onHttpRequest((HttpRequestEvent) event);

        //Generic subclasses - combining multiple events
        if (event instanceof GuildVoiceUpdateEvent)
            onGuildVoiceUpdate((GuildVoiceUpdateEvent) event);

        //Generic Events
        //Start a new if statement so that these are no overridden by the above events.
        if (event instanceof GenericMessageReactionEvent)
            onGenericMessageReaction((GenericMessageReactionEvent) event);
        else if (event instanceof GenericPrivateMessageReactionEvent)
            onGenericPrivateMessageReaction((GenericPrivateMessageReactionEvent) event);
        else if (event instanceof GenericTextChannelUpdateEvent)
            onGenericTextChannelUpdate((GenericTextChannelUpdateEvent) event);
        else if (event instanceof GenericCategoryUpdateEvent)
            onGenericCategoryUpdate((GenericCategoryUpdateEvent) event);
        else if (event instanceof GenericGuildMessageReactionEvent)
            onGenericGuildMessageReaction((GenericGuildMessageReactionEvent) event);
        else if (event instanceof GenericVoiceChannelUpdateEvent)
            onGenericVoiceChannelUpdate((GenericVoiceChannelUpdateEvent) event);
        else if (event instanceof GenericGuildUpdateEvent)
            onGenericGuildUpdate((GenericGuildUpdateEvent) event);
        else if (event instanceof GenericGuildMemberEvent)
            onGenericGuildMember((GenericGuildMemberEvent) event);
        else if (event instanceof GenericGuildVoiceEvent)
            onGenericGuildVoice((GenericGuildVoiceEvent) event);
        else if (event instanceof GenericRoleUpdateEvent)
            onGenericRoleUpdate(((GenericRoleUpdateEvent) event));
        else if (event instanceof GenericEmoteUpdateEvent)
            onGenericEmoteUpdate((GenericEmoteUpdateEvent) event);
        else if (event instanceof GenericUserPresenceEvent)
            onGenericUserPresence((GenericUserPresenceEvent) event);

        //Generic events that have generic subclasses (the subclasses as above).
        if (event instanceof GenericMessageEvent)
            onGenericMessage((GenericMessageEvent) event);
        else if (event instanceof GenericPrivateMessageEvent)
            onGenericPrivateMessage((GenericPrivateMessageEvent) event);
        else if (event instanceof GenericGuildMessageEvent)
            onGenericGuildMessage((GenericGuildMessageEvent) event);
        else if (event instanceof GenericUserEvent)
            onGenericUser((GenericUserEvent) event);
        else if (event instanceof GenericSelfUpdateEvent)
            onGenericSelfUpdate((GenericSelfUpdateEvent) event);
        else if (event instanceof GenericTextChannelEvent)
            onGenericTextChannel((GenericTextChannelEvent) event);
        else if (event instanceof GenericVoiceChannelEvent)
            onGenericVoiceChannel((GenericVoiceChannelEvent) event);
        else if (event instanceof GenericCategoryEvent)
            onGenericCategory((GenericCategoryEvent) event);
        else if (event instanceof GenericRoleEvent)
            onGenericRole((GenericRoleEvent) event);
        else if (event instanceof GenericEmoteEvent)
            onGenericEmote((GenericEmoteEvent) event);

        //Generic events that have 2 levels of generic subclasses
        if (event instanceof GenericGuildEvent)
            onGenericGuild((GenericGuildEvent) event);

        if (event.getJDA().getAccountType() == AccountType.CLIENT)
        {
            //Relationship Events
            if (event instanceof FriendAddedEvent)
                onFriendAdded((FriendAddedEvent) event);
            else if (event instanceof FriendRemovedEvent)
                onFriendRemoved((FriendRemovedEvent) event);
            else if (event instanceof UserBlockedEvent)
                onUserBlocked((UserBlockedEvent) event);
            else if (event instanceof UserUnblockedEvent)
                onUserUnblocked((UserUnblockedEvent) event);
            else if (event instanceof FriendRequestSentEvent)
                onFriendRequestSent((FriendRequestSentEvent) event);
            else if (event instanceof FriendRequestCanceledEvent)
                onFriendRequestCanceled((FriendRequestCanceledEvent) event);
            else if (event instanceof FriendRequestReceivedEvent)
                onFriendRequestReceived((FriendRequestReceivedEvent) event);
            else if (event instanceof FriendRequestIgnoredEvent)
                onFriendRequestIgnored((FriendRequestIgnoredEvent) event);

            //Group Events
            else if (event instanceof GroupJoinEvent)
                onGroupJoin((GroupJoinEvent) event);
            else if (event instanceof GroupLeaveEvent)
                onGroupLeave((GroupLeaveEvent) event);
            else if (event instanceof GroupUserJoinEvent)
                onGroupUserJoin((GroupUserJoinEvent) event);
            else if (event instanceof GroupUserLeaveEvent)
                onGroupUserLeave((GroupUserLeaveEvent) event);

            //Group Message Events
            if (event instanceof GroupMessageReceivedEvent)
                onGroupMessageReceived((GroupMessageReceivedEvent) event);
            else if (event instanceof GroupMessageUpdateEvent)
                onGroupMessageUpdate((GroupMessageUpdateEvent) event);
            else if (event instanceof GroupMessageDeleteEvent)
                onGroupMessageDelete((GroupMessageDeleteEvent) event);
            else if (event instanceof GroupMessageEmbedEvent)
                onGroupMessageEmbed((GroupMessageEmbedEvent) event);
            else if (event instanceof GroupMessageReactionAddEvent)
                onGroupMessageReactionAdd((GroupMessageReactionAddEvent) event);
            else if (event instanceof GroupMessageReactionRemoveEvent)
                onGroupMessageReactionRemove((GroupMessageReactionRemoveEvent) event);
            else if (event instanceof GroupMessageReactionRemoveAllEvent)
                onGroupMessageReactionRemoveAll((GroupMessageReactionRemoveAllEvent) event);

            //Group Update Events
            else if (event instanceof GroupUpdateIconEvent)
                onGroupUpdateIcon((GroupUpdateIconEvent) event);
            else if (event instanceof GroupUpdateNameEvent)
                onGroupUpdateName((GroupUpdateNameEvent) event);
            else if (event instanceof GroupUpdateOwnerEvent)
                onGroupUpdateOwner((GroupUpdateOwnerEvent) event);

            //Call Events
            else if (event instanceof CallCreateEvent)
                onCallCreate((CallCreateEvent) event);
            else if (event instanceof CallDeleteEvent)
                onCallDelete((CallDeleteEvent) event);

            //Call Update Events
            else if (event instanceof CallUpdateRegionEvent)
                onCallUpdateRegion((CallUpdateRegionEvent) event);
            else if (event instanceof CallUpdateRingingUsersEvent)
                onCallUpdateRingingUsers((CallUpdateRingingUsersEvent) event);

            //Call Voice Events
            else if (event instanceof CallVoiceJoinEvent)
                onCallVoiceJoin((CallVoiceJoinEvent) event);
            else if (event instanceof CallVoiceLeaveEvent)
                onCallVoiceLeave((CallVoiceLeaveEvent) event);
            else if (event instanceof CallVoiceSelfMuteEvent)
                onCallVoiceSelfMute((CallVoiceSelfMuteEvent) event);
            else if (event instanceof CallVoiceSelfDeafenEvent)
                onCallVoiceSelfDeafen((CallVoiceSelfDeafenEvent) event);

            //Client Only Child-Generic Events
            if (event instanceof GenericRelationshipAddEvent)
                onGenericRelationshipAdd((GenericRelationshipAddEvent) event);
            else if (event instanceof GenericRelationshipRemoveEvent)
                onGenericRelationshipRemove((GenericRelationshipRemoveEvent) event);
            else if (event instanceof GenericGroupMessageReactionEvent)
                onGenericGroupMessageReaction((GenericGroupMessageReactionEvent) event);
            else if (event instanceof GenericGroupUpdateEvent)
                onGenericGroupUpdate((GenericGroupUpdateEvent) event);
            else if (event instanceof GenericCallUpdateEvent)
                onGenericCallUpdate((GenericCallUpdateEvent) event);
            else if (event instanceof GenericCallVoiceEvent)
                onGenericCallVoice((GenericCallVoiceEvent) event);

            //Client Only Generic Events
            //Subclass of GenericGroupEvent must be in different if/else block
            // cannot be in block above due to GenericGroupMessageReactionEvent being a child
            if (event instanceof GenericGroupMessageEvent)
                onGenericGroupMessage((GenericGroupMessageEvent) event);

            if (event instanceof GenericRelationshipEvent)
                onGenericRelationship((GenericRelationshipEvent) event);
            else if (event instanceof GenericGroupEvent)
                onGenericGroup((GenericGroupEvent) event);
            else if (event instanceof GenericCallEvent)
                onGenericCall((GenericCallEvent) event);
        }
    }
}
