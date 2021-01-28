/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.api.hooks;

import net.dv8tion.jda.annotations.DeprecatedSince;
import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.api.events.*;
import net.dv8tion.jda.api.events.channel.category.CategoryCreateEvent;
import net.dv8tion.jda.api.events.channel.category.CategoryDeleteEvent;
import net.dv8tion.jda.api.events.channel.category.GenericCategoryEvent;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdatePermissionsEvent;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.category.update.GenericCategoryUpdateEvent;
import net.dv8tion.jda.api.events.channel.priv.PrivateChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.priv.PrivateChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.store.GenericStoreChannelEvent;
import net.dv8tion.jda.api.events.channel.store.StoreChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.store.StoreChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.store.update.GenericStoreChannelUpdateEvent;
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdatePermissionsEvent;
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.text.GenericTextChannelEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.text.update.*;
import net.dv8tion.jda.api.events.channel.voice.GenericVoiceChannelEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.voice.update.*;
import net.dv8tion.jda.api.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.api.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.api.events.emote.GenericEmoteEvent;
import net.dv8tion.jda.api.events.emote.update.EmoteUpdateNameEvent;
import net.dv8tion.jda.api.events.emote.update.EmoteUpdateRolesEvent;
import net.dv8tion.jda.api.events.emote.update.GenericEmoteUpdateEvent;
import net.dv8tion.jda.api.events.guild.*;
import net.dv8tion.jda.api.events.guild.invite.GenericGuildInviteEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.*;
import net.dv8tion.jda.api.events.guild.member.update.GenericGuildMemberUpdateEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdatePendingEvent;
import net.dv8tion.jda.api.events.guild.override.GenericPermissionOverrideEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideCreateEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideDeleteEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideUpdateEvent;
import net.dv8tion.jda.api.events.guild.update.*;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.events.http.HttpRequestEvent;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.events.message.guild.*;
import net.dv8tion.jda.api.events.message.guild.react.*;
import net.dv8tion.jda.api.events.message.priv.*;
import net.dv8tion.jda.api.events.message.priv.react.GenericPrivateMessageReactionEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.message.react.*;
import net.dv8tion.jda.api.events.role.GenericRoleEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.*;
import net.dv8tion.jda.api.events.self.*;
import net.dv8tion.jda.api.events.user.GenericUserEvent;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.events.user.UserTypingEvent;
import net.dv8tion.jda.api.events.user.update.*;
import net.dv8tion.jda.internal.utils.ClassWalker;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An abstract implementation of {@link net.dv8tion.jda.api.hooks.EventListener EventListener} which divides {@link net.dv8tion.jda.api.events.Event Events}
 * for you. You should <b><u>override</u></b> the methods provided by this class for your event listener implementation.
 *
 * <h2>Example:</h2>
 * <pre><code>
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
 * @see net.dv8tion.jda.api.hooks.EventListener EventListener
 * @see net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventManager
 */
@SuppressWarnings("deprecation")
public abstract class ListenerAdapter implements EventListener
{
    //Deprecated/Unused events
    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.0")
    @ReplaceWith("onPermissionOverrideUpdate(), onPermissionOverrideCreate(), and onPermissionOverrideDelete()")
    public void onTextChannelUpdatePermissions(@Nonnull TextChannelUpdatePermissionsEvent event) {}

    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.0")
    @ReplaceWith("onPermissionOverrideUpdate(), onPermissionOverrideCreate(), and onPermissionOverrideDelete()")
    public void onStoreChannelUpdatePermissions(@Nonnull StoreChannelUpdatePermissionsEvent event) {}

    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.0")
    @ReplaceWith("onPermissionOverrideUpdate(), onPermissionOverrideCreate(), and onPermissionOverrideDelete()")
    public void onVoiceChannelUpdatePermissions(@Nonnull VoiceChannelUpdatePermissionsEvent event) {}

    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.0")
    @ReplaceWith("onPermissionOverrideUpdate(), onPermissionOverrideCreate(), and onPermissionOverrideDelete()")
    public void onCategoryUpdatePermissions(@Nonnull CategoryUpdatePermissionsEvent event) {}

    @Deprecated
    @DeprecatedSince("4.2.0")
    @ReplaceWith("onGuildMemberRemove(GuildMemberRemoveEvent)")
    public void onGuildMemberLeave(@Nonnull GuildMemberLeaveEvent event) {}

    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.0")
    public void onSelfUpdateEmail(@Nonnull SelfUpdateEmailEvent event) {}

    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.1")
    @ReplaceWith("onResumed(ResumedEvent)")
    public void onResume(@Nonnull ResumedEvent event) {}

    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.1")
    @ReplaceWith("onReconnected(ReconnectedEvent)")
    public void onReconnect(@Nonnull ReconnectedEvent event) {}

    public void onGenericEvent(@Nonnull GenericEvent event) {}
    public void onGenericUpdate(@Nonnull UpdateEvent<?, ?> event) {}
    public void onRawGateway(@Nonnull RawGatewayEvent event) {}
    public void onGatewayPing(@Nonnull GatewayPingEvent event) {}

    //JDA Events
    public void onReady(@Nonnull ReadyEvent event) {}
    public void onResumed(@Nonnull ResumedEvent event) {}
    public void onReconnected(@Nonnull ReconnectedEvent event) {}
    public void onDisconnect(@Nonnull DisconnectEvent event) {}
    public void onShutdown(@Nonnull ShutdownEvent event) {}
    public void onStatusChange(@Nonnull StatusChangeEvent event) {}
    public void onException(@Nonnull ExceptionEvent event) {}

    //User Events
    public void onUserUpdateName(@Nonnull UserUpdateNameEvent event) {}
    public void onUserUpdateDiscriminator(@Nonnull UserUpdateDiscriminatorEvent event) {}
    public void onUserUpdateAvatar(@Nonnull UserUpdateAvatarEvent event) {}
    public void onUserUpdateOnlineStatus(@Nonnull UserUpdateOnlineStatusEvent event) {}
    public void onUserUpdateActivityOrder(@Nonnull UserUpdateActivityOrderEvent event) {}
    public void onUserUpdateFlags(@Nonnull UserUpdateFlagsEvent event) {}
    public void onUserTyping(@Nonnull UserTypingEvent event) {}
    public void onUserActivityStart(@Nonnull UserActivityStartEvent event) {}
    public void onUserActivityEnd(@Nonnull UserActivityEndEvent event) {}

    //Self Events. Fires only in relation to the currently logged in account.
    public void onSelfUpdateAvatar(@Nonnull SelfUpdateAvatarEvent event) {}
    public void onSelfUpdateMFA(@Nonnull SelfUpdateMFAEvent event) {}
    public void onSelfUpdateName(@Nonnull SelfUpdateNameEvent event) {}
    public void onSelfUpdateVerified(@Nonnull SelfUpdateVerifiedEvent event) {}

    //Message Events
    //Guild (TextChannel) Message Events
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {}
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent event) {}
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {}
    public void onGuildMessageEmbed(@Nonnull GuildMessageEmbedEvent event) {}
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {}
    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {}
    public void onGuildMessageReactionRemoveAll(@Nonnull GuildMessageReactionRemoveAllEvent event) {}
    public void onGuildMessageReactionRemoveEmote(@Nonnull GuildMessageReactionRemoveEmoteEvent event) {}

    //Private Message Events
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {}
    public void onPrivateMessageUpdate(@Nonnull PrivateMessageUpdateEvent event) {}
    public void onPrivateMessageDelete(@Nonnull PrivateMessageDeleteEvent event) {}
    public void onPrivateMessageEmbed(@Nonnull PrivateMessageEmbedEvent event) {}
    public void onPrivateMessageReactionAdd(@Nonnull PrivateMessageReactionAddEvent event) {}
    public void onPrivateMessageReactionRemove(@Nonnull PrivateMessageReactionRemoveEvent event) {}

    //Combined Message Events (Combines Guild and Private message into 1 event)
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {}
    public void onMessageUpdate(@Nonnull MessageUpdateEvent event) {}
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {}
    public void onMessageBulkDelete(@Nonnull MessageBulkDeleteEvent event) {}
    public void onMessageEmbed(@Nonnull MessageEmbedEvent event) {}
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {}
    public void onMessageReactionRemove(@Nonnull MessageReactionRemoveEvent event) {}
    public void onMessageReactionRemoveAll(@Nonnull MessageReactionRemoveAllEvent event) {}
    public void onMessageReactionRemoveEmote(@Nonnull MessageReactionRemoveEmoteEvent event) {}

    //PermissionOverride Events
    public void onPermissionOverrideDelete(@Nonnull PermissionOverrideDeleteEvent event) {}
    public void onPermissionOverrideUpdate(@Nonnull PermissionOverrideUpdateEvent event) {}
    public void onPermissionOverrideCreate(@Nonnull PermissionOverrideCreateEvent event) {}

    //StoreChannel Events
    public void onStoreChannelDelete(@Nonnull StoreChannelDeleteEvent event) {}
    public void onStoreChannelUpdateName(@Nonnull StoreChannelUpdateNameEvent event) {}
    public void onStoreChannelUpdatePosition(@Nonnull StoreChannelUpdatePositionEvent event) {}
    public void onStoreChannelCreate(@Nonnull StoreChannelCreateEvent event) {}

    //TextChannel Events
    public void onTextChannelDelete(@Nonnull TextChannelDeleteEvent event) {}
    public void onTextChannelUpdateName(@Nonnull TextChannelUpdateNameEvent event) {}
    public void onTextChannelUpdateTopic(@Nonnull TextChannelUpdateTopicEvent event) {}
    public void onTextChannelUpdatePosition(@Nonnull TextChannelUpdatePositionEvent event) {}
    public void onTextChannelUpdateNSFW(@Nonnull TextChannelUpdateNSFWEvent event) {}
    public void onTextChannelUpdateParent(@Nonnull TextChannelUpdateParentEvent event) {}
    public void onTextChannelUpdateSlowmode(@Nonnull TextChannelUpdateSlowmodeEvent event) {}
    public void onTextChannelUpdateNews(@Nonnull TextChannelUpdateNewsEvent event) {}
    public void onTextChannelCreate(@Nonnull TextChannelCreateEvent event) {}

    //VoiceChannel Events
    public void onVoiceChannelDelete(@Nonnull VoiceChannelDeleteEvent event) {}
    public void onVoiceChannelUpdateName(@Nonnull VoiceChannelUpdateNameEvent event) {}
    public void onVoiceChannelUpdatePosition(@Nonnull VoiceChannelUpdatePositionEvent event) {}
    public void onVoiceChannelUpdateUserLimit(@Nonnull VoiceChannelUpdateUserLimitEvent event) {}
    public void onVoiceChannelUpdateBitrate(@Nonnull VoiceChannelUpdateBitrateEvent event) {}
    public void onVoiceChannelUpdateParent(@Nonnull VoiceChannelUpdateParentEvent event) {}
    public void onVoiceChannelCreate(@Nonnull VoiceChannelCreateEvent event) {}

    //Category Events
    public void onCategoryDelete(@Nonnull CategoryDeleteEvent event) {}
    public void onCategoryUpdateName(@Nonnull CategoryUpdateNameEvent event) {}
    public void onCategoryUpdatePosition(@Nonnull CategoryUpdatePositionEvent event) {}
    public void onCategoryCreate(@Nonnull CategoryCreateEvent event) {}

    //PrivateChannel Events
    public void onPrivateChannelCreate(@Nonnull PrivateChannelCreateEvent event) {}
    public void onPrivateChannelDelete(@Nonnull PrivateChannelDeleteEvent event) {}

    //Guild Events
    public void onGuildReady(@Nonnull GuildReadyEvent event) {}
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {}
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {}
    public void onGuildAvailable(@Nonnull GuildAvailableEvent event) {}
    public void onGuildUnavailable(@Nonnull GuildUnavailableEvent event) {}
    public void onUnavailableGuildJoined(@Nonnull UnavailableGuildJoinedEvent event) {}
    public void onUnavailableGuildLeave(@Nonnull UnavailableGuildLeaveEvent event) {}
    public void onGuildBan(@Nonnull GuildBanEvent event) {}
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {}
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {}

    //Guild Update Events
    public void onGuildUpdateAfkChannel(@Nonnull GuildUpdateAfkChannelEvent event) {}
    public void onGuildUpdateSystemChannel(@Nonnull GuildUpdateSystemChannelEvent event) {}
    public void onGuildUpdateAfkTimeout(@Nonnull GuildUpdateAfkTimeoutEvent event) {}
    public void onGuildUpdateExplicitContentLevel(@Nonnull GuildUpdateExplicitContentLevelEvent event) {}
    public void onGuildUpdateIcon(@Nonnull GuildUpdateIconEvent event) {}
    public void onGuildUpdateMFALevel(@Nonnull GuildUpdateMFALevelEvent event) {}
    public void onGuildUpdateName(@Nonnull GuildUpdateNameEvent event){}
    public void onGuildUpdateNotificationLevel(@Nonnull GuildUpdateNotificationLevelEvent event) {}
    public void onGuildUpdateOwner(@Nonnull GuildUpdateOwnerEvent event) {}
    public void onGuildUpdateRegion(@Nonnull GuildUpdateRegionEvent event) {}
    public void onGuildUpdateSplash(@Nonnull GuildUpdateSplashEvent event) {}
    public void onGuildUpdateVerificationLevel(@Nonnull GuildUpdateVerificationLevelEvent event) {}
    public void onGuildUpdateLocale(@Nonnull GuildUpdateLocaleEvent event) {}
    public void onGuildUpdateFeatures(@Nonnull GuildUpdateFeaturesEvent event) {}
    public void onGuildUpdateVanityCode(@Nonnull GuildUpdateVanityCodeEvent event) {}
    public void onGuildUpdateBanner(@Nonnull GuildUpdateBannerEvent event) {}
    public void onGuildUpdateDescription(@Nonnull GuildUpdateDescriptionEvent event) {}
    public void onGuildUpdateBoostTier(@Nonnull GuildUpdateBoostTierEvent event) {}
    public void onGuildUpdateBoostCount(@Nonnull GuildUpdateBoostCountEvent event) {}
    public void onGuildUpdateMaxMembers(@Nonnull GuildUpdateMaxMembersEvent event) {}
    public void onGuildUpdateMaxPresences(@Nonnull GuildUpdateMaxPresencesEvent event) {}

    //Guild Invite Events
    public void onGuildInviteCreate(@Nonnull GuildInviteCreateEvent event) {}
    public void onGuildInviteDelete(@Nonnull GuildInviteDeleteEvent event) {}

    //Guild Member Events
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {}
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {}
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {}

    //Guild Member Update Events
    public void onGuildMemberUpdate(@Nonnull GuildMemberUpdateEvent event) {}
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {}
    public void onGuildMemberUpdateBoostTime(@Nonnull GuildMemberUpdateBoostTimeEvent event) {}
    public void onGuildMemberUpdatePending(@Nonnull GuildMemberUpdatePendingEvent event) {}

    //Guild Voice Events
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {}
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {}
    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent event) {}
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {}
    public void onGuildVoiceMute(@Nonnull GuildVoiceMuteEvent event) {}
    public void onGuildVoiceDeafen(@Nonnull GuildVoiceDeafenEvent event) {}
    public void onGuildVoiceGuildMute(@Nonnull GuildVoiceGuildMuteEvent event) {}
    public void onGuildVoiceGuildDeafen(@Nonnull GuildVoiceGuildDeafenEvent event) {}
    public void onGuildVoiceSelfMute(@Nonnull GuildVoiceSelfMuteEvent event) {}
    public void onGuildVoiceSelfDeafen(@Nonnull GuildVoiceSelfDeafenEvent event) {}
    public void onGuildVoiceSuppress(@Nonnull GuildVoiceSuppressEvent event) {}
    public void onGuildVoiceStream(@Nonnull GuildVoiceStreamEvent event) {}

    //Role events
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {}
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {}

    //Role Update Events
    public void onRoleUpdateColor(@Nonnull RoleUpdateColorEvent event) {}
    public void onRoleUpdateHoisted(@Nonnull RoleUpdateHoistedEvent event) {}
    public void onRoleUpdateMentionable(@Nonnull RoleUpdateMentionableEvent event) {}
    public void onRoleUpdateName(@Nonnull RoleUpdateNameEvent event) {}
    public void onRoleUpdatePermissions(@Nonnull RoleUpdatePermissionsEvent event) {}
    public void onRoleUpdatePosition(@Nonnull RoleUpdatePositionEvent event) {}

    //Emote Events
    public void onEmoteAdded(@Nonnull EmoteAddedEvent event) {}
    public void onEmoteRemoved(@Nonnull EmoteRemovedEvent event) {}

    //Emote Update Events
    public void onEmoteUpdateName(@Nonnull EmoteUpdateNameEvent event) {}
    public void onEmoteUpdateRoles(@Nonnull EmoteUpdateRolesEvent event) {}

    // Debug Events
    public void onHttpRequest(@Nonnull HttpRequestEvent event) {}

    //Generic Events
    public void onGenericMessage(@Nonnull GenericMessageEvent event) {}
    public void onGenericMessageReaction(@Nonnull GenericMessageReactionEvent event) {}
    public void onGenericGuildMessage(@Nonnull GenericGuildMessageEvent event) {}
    public void onGenericGuildMessageReaction(@Nonnull GenericGuildMessageReactionEvent event) {}
    public void onGenericPrivateMessage(@Nonnull GenericPrivateMessageEvent event) {}
    public void onGenericPrivateMessageReaction(@Nonnull GenericPrivateMessageReactionEvent event) {}
    public void onGenericUser(@Nonnull GenericUserEvent event) {}
    public void onGenericUserPresence(@Nonnull GenericUserPresenceEvent event) {}
    public void onGenericSelfUpdate(@Nonnull GenericSelfUpdateEvent event) {}
    public void onGenericStoreChannel(@Nonnull GenericStoreChannelEvent event) {}
    public void onGenericStoreChannelUpdate(@Nonnull GenericStoreChannelUpdateEvent event) {}
    public void onGenericTextChannel(@Nonnull GenericTextChannelEvent event) {}
    public void onGenericTextChannelUpdate(@Nonnull GenericTextChannelUpdateEvent event) {}
    public void onGenericVoiceChannel(@Nonnull GenericVoiceChannelEvent event) {}
    public void onGenericVoiceChannelUpdate(@Nonnull GenericVoiceChannelUpdateEvent event) {}
    public void onGenericCategory(@Nonnull GenericCategoryEvent event) {}
    public void onGenericCategoryUpdate(@Nonnull GenericCategoryUpdateEvent event) {}
    public void onGenericGuild(@Nonnull GenericGuildEvent event) {}
    public void onGenericGuildUpdate(@Nonnull GenericGuildUpdateEvent event) {}
    public void onGenericGuildInvite(@Nonnull GenericGuildInviteEvent event) {}
    public void onGenericGuildMember(@Nonnull GenericGuildMemberEvent event) {}
    public void onGenericGuildMemberUpdate(@Nonnull GenericGuildMemberUpdateEvent event) {}
    public void onGenericGuildVoice(@Nonnull GenericGuildVoiceEvent event) {}
    public void onGenericRole(@Nonnull GenericRoleEvent event) {}
    public void onGenericRoleUpdate(@Nonnull GenericRoleUpdateEvent event) {}
    public void onGenericEmote(@Nonnull GenericEmoteEvent event) {}
    public void onGenericEmoteUpdate(@Nonnull GenericEmoteUpdateEvent event) {}
    public void onGenericPermissionOverride(@Nonnull GenericPermissionOverrideEvent event) {}

    @Override
    public final void onEvent(@Nonnull GenericEvent event)
    {
        onGenericEvent(event);
        if (event instanceof UpdateEvent)
            onGenericUpdate((UpdateEvent<?, ?>) event);

        //TODO: Remove once deprecated methods are removed
        if (event instanceof ResumedEvent)
            onResume((ResumedEvent) event);
        else if (event instanceof ReconnectedEvent)
            onReconnect((ReconnectedEvent) event);

        Class<ListenerAdapter> handle = ListenerAdapter.class;
        for (Class<?> clazz : ClassWalker.range(event.getClass(), GenericEvent.class))
        {
            String name = clazz.getSimpleName();
            try
            {
                name = "on" + name.substring(0, name.length() - "Event".length());
                Method method = handle.getDeclaredMethod(name, clazz);
                method.invoke(this, event);
            }
            catch (NoSuchMethodException | IllegalAccessException ignored) {} // this means this is probably a custom event!
            catch (InvocationTargetException e)
            {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException)
                    throw (RuntimeException) cause;
                if (cause instanceof Error)
                    throw (Error) cause;
                throw new RuntimeException(cause);
            }
        }
    }
}
