/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    public void onTextChannelUpdatePermissions(@NotNull TextChannelUpdatePermissionsEvent event) {}

    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.0")
    @ReplaceWith("onPermissionOverrideUpdate(), onPermissionOverrideCreate(), and onPermissionOverrideDelete()")
    public void onStoreChannelUpdatePermissions(@NotNull StoreChannelUpdatePermissionsEvent event) {}

    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.0")
    @ReplaceWith("onPermissionOverrideUpdate(), onPermissionOverrideCreate(), and onPermissionOverrideDelete()")
    public void onVoiceChannelUpdatePermissions(@NotNull VoiceChannelUpdatePermissionsEvent event) {}

    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.0")
    @ReplaceWith("onPermissionOverrideUpdate(), onPermissionOverrideCreate(), and onPermissionOverrideDelete()")
    public void onCategoryUpdatePermissions(@NotNull CategoryUpdatePermissionsEvent event) {}

    @Deprecated
    @DeprecatedSince("4.2.0")
    @ReplaceWith("onGuildMemberRemove(GuildMemberRemoveEvent)")
    public void onGuildMemberLeave(@NotNull GuildMemberLeaveEvent event) {}

    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.0")
    public void onSelfUpdateEmail(@NotNull SelfUpdateEmailEvent event) {}

    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.1")
    @ReplaceWith("onResumed(ResumedEvent)")
    public void onResume(@NotNull ResumedEvent event) {}

    @Deprecated
    @ForRemoval
    @DeprecatedSince("4.2.1")
    @ReplaceWith("onReconnected(ReconnectedEvent)")
    public void onReconnect(@NotNull ReconnectedEvent event) {}

    public void onGenericEvent(@NotNull GenericEvent event) {}
    public void onGenericUpdate(@NotNull UpdateEvent<?, ?> event) {}
    public void onRawGateway(@NotNull RawGatewayEvent event) {}
    public void onGatewayPing(@NotNull GatewayPingEvent event) {}

    //JDA Events
    public void onReady(@NotNull ReadyEvent event) {}
    public void onResumed(@NotNull ResumedEvent event) {}
    public void onReconnected(@NotNull ReconnectedEvent event) {}
    public void onDisconnect(@NotNull DisconnectEvent event) {}
    public void onShutdown(@NotNull ShutdownEvent event) {}
    public void onStatusChange(@NotNull StatusChangeEvent event) {}
    public void onException(@NotNull ExceptionEvent event) {}

    //User Events
    public void onUserUpdateName(@NotNull UserUpdateNameEvent event) {}
    public void onUserUpdateDiscriminator(@NotNull UserUpdateDiscriminatorEvent event) {}
    public void onUserUpdateAvatar(@NotNull UserUpdateAvatarEvent event) {}
    public void onUserUpdateOnlineStatus(@NotNull UserUpdateOnlineStatusEvent event) {}
    public void onUserUpdateActivityOrder(@NotNull UserUpdateActivityOrderEvent event) {}
    public void onUserUpdateFlags(@NotNull UserUpdateFlagsEvent event) {}
    public void onUserTyping(@NotNull UserTypingEvent event) {}
    public void onUserActivityStart(@NotNull UserActivityStartEvent event) {}
    public void onUserActivityEnd(@NotNull UserActivityEndEvent event) {}
    public void onUserUpdateActivities(@NotNull UserUpdateActivitiesEvent event) {}

    //Self Events. Fires only in relation to the currently logged in account.
    public void onSelfUpdateAvatar(@NotNull SelfUpdateAvatarEvent event) {}
    public void onSelfUpdateMFA(@NotNull SelfUpdateMFAEvent event) {}
    public void onSelfUpdateName(@NotNull SelfUpdateNameEvent event) {}
    public void onSelfUpdateVerified(@NotNull SelfUpdateVerifiedEvent event) {}

    //Message Events
    //Guild (TextChannel) Message Events
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {}
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {}
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {}
    public void onGuildMessageEmbed(@NotNull GuildMessageEmbedEvent event) {}
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {}
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {}
    public void onGuildMessageReactionRemoveAll(@NotNull GuildMessageReactionRemoveAllEvent event) {}
    public void onGuildMessageReactionRemoveEmote(@NotNull GuildMessageReactionRemoveEmoteEvent event) {}

    //Private Message Events
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {}
    public void onPrivateMessageUpdate(@NotNull PrivateMessageUpdateEvent event) {}
    public void onPrivateMessageDelete(@NotNull PrivateMessageDeleteEvent event) {}
    public void onPrivateMessageEmbed(@NotNull PrivateMessageEmbedEvent event) {}
    public void onPrivateMessageReactionAdd(@NotNull PrivateMessageReactionAddEvent event) {}
    public void onPrivateMessageReactionRemove(@NotNull PrivateMessageReactionRemoveEvent event) {}

    //Combined Message Events (Combines Guild and Private message into 1 event)
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {}
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {}
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {}
    public void onMessageBulkDelete(@NotNull MessageBulkDeleteEvent event) {}
    public void onMessageEmbed(@NotNull MessageEmbedEvent event) {}
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {}
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {}
    public void onMessageReactionRemoveAll(@NotNull MessageReactionRemoveAllEvent event) {}
    public void onMessageReactionRemoveEmote(@NotNull MessageReactionRemoveEmoteEvent event) {}

    //PermissionOverride Events
    public void onPermissionOverrideDelete(@NotNull PermissionOverrideDeleteEvent event) {}
    public void onPermissionOverrideUpdate(@NotNull PermissionOverrideUpdateEvent event) {}
    public void onPermissionOverrideCreate(@NotNull PermissionOverrideCreateEvent event) {}

    //StoreChannel Events
    public void onStoreChannelDelete(@NotNull StoreChannelDeleteEvent event) {}
    public void onStoreChannelUpdateName(@NotNull StoreChannelUpdateNameEvent event) {}
    public void onStoreChannelUpdatePosition(@NotNull StoreChannelUpdatePositionEvent event) {}
    public void onStoreChannelCreate(@NotNull StoreChannelCreateEvent event) {}

    //TextChannel Events
    public void onTextChannelDelete(@NotNull TextChannelDeleteEvent event) {}
    public void onTextChannelUpdateName(@NotNull TextChannelUpdateNameEvent event) {}
    public void onTextChannelUpdateTopic(@NotNull TextChannelUpdateTopicEvent event) {}
    public void onTextChannelUpdatePosition(@NotNull TextChannelUpdatePositionEvent event) {}
    public void onTextChannelUpdateNSFW(@NotNull TextChannelUpdateNSFWEvent event) {}
    public void onTextChannelUpdateParent(@NotNull TextChannelUpdateParentEvent event) {}
    public void onTextChannelUpdateSlowmode(@NotNull TextChannelUpdateSlowmodeEvent event) {}
    public void onTextChannelUpdateNews(@NotNull TextChannelUpdateNewsEvent event) {}
    public void onTextChannelCreate(@NotNull TextChannelCreateEvent event) {}

    //VoiceChannel Events
    public void onVoiceChannelDelete(@NotNull VoiceChannelDeleteEvent event) {}
    public void onVoiceChannelUpdateName(@NotNull VoiceChannelUpdateNameEvent event) {}
    public void onVoiceChannelUpdatePosition(@NotNull VoiceChannelUpdatePositionEvent event) {}
    public void onVoiceChannelUpdateUserLimit(@NotNull VoiceChannelUpdateUserLimitEvent event) {}
    public void onVoiceChannelUpdateBitrate(@NotNull VoiceChannelUpdateBitrateEvent event) {}
    public void onVoiceChannelUpdateParent(@NotNull VoiceChannelUpdateParentEvent event) {}
    public void onVoiceChannelCreate(@NotNull VoiceChannelCreateEvent event) {}

    //Category Events
    public void onCategoryDelete(@NotNull CategoryDeleteEvent event) {}
    public void onCategoryUpdateName(@NotNull CategoryUpdateNameEvent event) {}
    public void onCategoryUpdatePosition(@NotNull CategoryUpdatePositionEvent event) {}
    public void onCategoryCreate(@NotNull CategoryCreateEvent event) {}

    //PrivateChannel Events

    /**
     * @deprecated This event is no longer supported by discord
     */
    @Deprecated
    @ForRemoval(deadline = "4.4.0")
    @DeprecatedSince("4.3.0")
    public void onPrivateChannelCreate(@NotNull PrivateChannelCreateEvent event) {}
    @Deprecated
    @ForRemoval(deadline = "4.4.0")
    @DeprecatedSince("4.3.0")
    public void onPrivateChannelDelete(@NotNull PrivateChannelDeleteEvent event) {}

    //Guild Events
    public void onGuildReady(@NotNull GuildReadyEvent event) {}
    public void onGuildTimeout(@NotNull GuildTimeoutEvent event) {}
    public void onGuildJoin(@NotNull GuildJoinEvent event) {}
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {}
    public void onGuildAvailable(@NotNull GuildAvailableEvent event) {}
    public void onGuildUnavailable(@NotNull GuildUnavailableEvent event) {}
    public void onUnavailableGuildJoined(@NotNull UnavailableGuildJoinedEvent event) {}
    public void onUnavailableGuildLeave(@NotNull UnavailableGuildLeaveEvent event) {}
    public void onGuildBan(@NotNull GuildBanEvent event) {}
    public void onGuildUnban(@NotNull GuildUnbanEvent event) {}
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {}

    //Guild Update Events
    public void onGuildUpdateAfkChannel(@NotNull GuildUpdateAfkChannelEvent event) {}
    public void onGuildUpdateSystemChannel(@NotNull GuildUpdateSystemChannelEvent event) {}
    public void onGuildUpdateRulesChannel(@NotNull GuildUpdateRulesChannelEvent event) {}
    public void onGuildUpdateCommunityUpdatesChannel(@NotNull GuildUpdateCommunityUpdatesChannelEvent event) {}
    public void onGuildUpdateAfkTimeout(@NotNull GuildUpdateAfkTimeoutEvent event) {}
    public void onGuildUpdateExplicitContentLevel(@NotNull GuildUpdateExplicitContentLevelEvent event) {}
    public void onGuildUpdateIcon(@NotNull GuildUpdateIconEvent event) {}
    public void onGuildUpdateMFALevel(@NotNull GuildUpdateMFALevelEvent event) {}
    public void onGuildUpdateName(@NotNull GuildUpdateNameEvent event){}
    public void onGuildUpdateNotificationLevel(@NotNull GuildUpdateNotificationLevelEvent event) {}
    public void onGuildUpdateOwner(@NotNull GuildUpdateOwnerEvent event) {}
    public void onGuildUpdateRegion(@NotNull GuildUpdateRegionEvent event) {}
    public void onGuildUpdateSplash(@NotNull GuildUpdateSplashEvent event) {}
    public void onGuildUpdateVerificationLevel(@NotNull GuildUpdateVerificationLevelEvent event) {}
    public void onGuildUpdateLocale(@NotNull GuildUpdateLocaleEvent event) {}
    public void onGuildUpdateFeatures(@NotNull GuildUpdateFeaturesEvent event) {}
    public void onGuildUpdateVanityCode(@NotNull GuildUpdateVanityCodeEvent event) {}
    public void onGuildUpdateBanner(@NotNull GuildUpdateBannerEvent event) {}
    public void onGuildUpdateDescription(@NotNull GuildUpdateDescriptionEvent event) {}
    public void onGuildUpdateBoostTier(@NotNull GuildUpdateBoostTierEvent event) {}
    public void onGuildUpdateBoostCount(@NotNull GuildUpdateBoostCountEvent event) {}
    public void onGuildUpdateMaxMembers(@NotNull GuildUpdateMaxMembersEvent event) {}
    public void onGuildUpdateMaxPresences(@NotNull GuildUpdateMaxPresencesEvent event) {}

    //Guild Invite Events
    public void onGuildInviteCreate(@NotNull GuildInviteCreateEvent event) {}
    public void onGuildInviteDelete(@NotNull GuildInviteDeleteEvent event) {}

    //Guild Member Events
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {}
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {}
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {}

    //Guild Member Update Events
    public void onGuildMemberUpdate(@NotNull GuildMemberUpdateEvent event) {}
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {}
    public void onGuildMemberUpdateBoostTime(@NotNull GuildMemberUpdateBoostTimeEvent event) {}
    public void onGuildMemberUpdatePending(@NotNull GuildMemberUpdatePendingEvent event) {}

    //Guild Voice Events
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {}
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {}
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {}
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {}
    public void onGuildVoiceMute(@NotNull GuildVoiceMuteEvent event) {}
    public void onGuildVoiceDeafen(@NotNull GuildVoiceDeafenEvent event) {}
    public void onGuildVoiceGuildMute(@NotNull GuildVoiceGuildMuteEvent event) {}
    public void onGuildVoiceGuildDeafen(@NotNull GuildVoiceGuildDeafenEvent event) {}
    public void onGuildVoiceSelfMute(@NotNull GuildVoiceSelfMuteEvent event) {}
    public void onGuildVoiceSelfDeafen(@NotNull GuildVoiceSelfDeafenEvent event) {}
    public void onGuildVoiceSuppress(@NotNull GuildVoiceSuppressEvent event) {}
    public void onGuildVoiceStream(@NotNull GuildVoiceStreamEvent event) {}

    //Role events
    public void onRoleCreate(@NotNull RoleCreateEvent event) {}
    public void onRoleDelete(@NotNull RoleDeleteEvent event) {}

    //Role Update Events
    public void onRoleUpdateColor(@NotNull RoleUpdateColorEvent event) {}
    public void onRoleUpdateHoisted(@NotNull RoleUpdateHoistedEvent event) {}
    public void onRoleUpdateMentionable(@NotNull RoleUpdateMentionableEvent event) {}
    public void onRoleUpdateName(@NotNull RoleUpdateNameEvent event) {}
    public void onRoleUpdatePermissions(@NotNull RoleUpdatePermissionsEvent event) {}
    public void onRoleUpdatePosition(@NotNull RoleUpdatePositionEvent event) {}

    //Emote Events
    public void onEmoteAdded(@NotNull EmoteAddedEvent event) {}
    public void onEmoteRemoved(@NotNull EmoteRemovedEvent event) {}

    //Emote Update Events
    public void onEmoteUpdateName(@NotNull EmoteUpdateNameEvent event) {}
    public void onEmoteUpdateRoles(@NotNull EmoteUpdateRolesEvent event) {}

    // Debug Events
    public void onHttpRequest(@NotNull HttpRequestEvent event) {}

    //Generic Events
    public void onGenericMessage(@NotNull GenericMessageEvent event) {}
    public void onGenericMessageReaction(@NotNull GenericMessageReactionEvent event) {}
    public void onGenericGuildMessage(@NotNull GenericGuildMessageEvent event) {}
    public void onGenericGuildMessageReaction(@NotNull GenericGuildMessageReactionEvent event) {}
    public void onGenericPrivateMessage(@NotNull GenericPrivateMessageEvent event) {}
    public void onGenericPrivateMessageReaction(@NotNull GenericPrivateMessageReactionEvent event) {}
    public void onGenericUser(@NotNull GenericUserEvent event) {}
    public void onGenericUserPresence(@NotNull GenericUserPresenceEvent event) {}
    public void onGenericSelfUpdate(@NotNull GenericSelfUpdateEvent event) {}
    public void onGenericStoreChannel(@NotNull GenericStoreChannelEvent event) {}
    public void onGenericStoreChannelUpdate(@NotNull GenericStoreChannelUpdateEvent event) {}
    public void onGenericTextChannel(@NotNull GenericTextChannelEvent event) {}
    public void onGenericTextChannelUpdate(@NotNull GenericTextChannelUpdateEvent event) {}
    public void onGenericVoiceChannel(@NotNull GenericVoiceChannelEvent event) {}
    public void onGenericVoiceChannelUpdate(@NotNull GenericVoiceChannelUpdateEvent event) {}
    public void onGenericCategory(@NotNull GenericCategoryEvent event) {}
    public void onGenericCategoryUpdate(@NotNull GenericCategoryUpdateEvent event) {}
    public void onGenericGuild(@NotNull GenericGuildEvent event) {}
    public void onGenericGuildUpdate(@NotNull GenericGuildUpdateEvent event) {}
    public void onGenericGuildInvite(@NotNull GenericGuildInviteEvent event) {}
    public void onGenericGuildMember(@NotNull GenericGuildMemberEvent event) {}
    public void onGenericGuildMemberUpdate(@NotNull GenericGuildMemberUpdateEvent event) {}
    public void onGenericGuildVoice(@NotNull GenericGuildVoiceEvent event) {}
    public void onGenericRole(@NotNull GenericRoleEvent event) {}
    public void onGenericRoleUpdate(@NotNull GenericRoleUpdateEvent event) {}
    public void onGenericEmote(@NotNull GenericEmoteEvent event) {}
    public void onGenericEmoteUpdate(@NotNull GenericEmoteUpdateEvent event) {}
    public void onGenericPermissionOverride(@NotNull GenericPermissionOverrideEvent event) {}

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final ConcurrentMap<Class<?>, MethodHandle> methods = new ConcurrentHashMap<>();
    private static final Set<Class<?>> unresolved;
    static
    {
        unresolved = ConcurrentHashMap.newKeySet();
        Collections.addAll(unresolved,
            Object.class, // Objects aren't events
            Event.class, // onEvent is final and would never be found
            UpdateEvent.class, // onGenericUpdate has already been called
            GenericEvent.class // onGenericEvent has already been called
        );
    }

    @Override
    public final void onEvent(@NotNull GenericEvent event)
    {
        onGenericEvent(event);
        if (event instanceof UpdateEvent)
            onGenericUpdate((UpdateEvent<?, ?>) event);

        //TODO: Remove once deprecated methods are removed
        if (event instanceof ResumedEvent)
            onResume((ResumedEvent) event);
        else if (event instanceof ReconnectedEvent)
            onReconnect((ReconnectedEvent) event);

        for (Class<?> clazz : ClassWalker.range(event.getClass(), GenericEvent.class))
        {
            if (unresolved.contains(clazz))
                continue;
            MethodHandle mh = methods.computeIfAbsent(clazz, ListenerAdapter::findMethod);
            if (mh == null)
            {
                unresolved.add(clazz);
                continue;
            }

            try
            {
                mh.invoke(this, event);
            }
            catch (Throwable throwable)
            {
                if (throwable instanceof RuntimeException)
                    throw (RuntimeException) throwable;
                if (throwable instanceof Error)
                    throw (Error) throwable;
                throw new IllegalStateException(throwable);
            }
        }
    }

    private static MethodHandle findMethod(Class<?> clazz)
    {
        String name = clazz.getSimpleName();
        MethodType type = MethodType.methodType(Void.TYPE, clazz);
        try
        {
            name = "on" + name.substring(0, name.length() - "Event".length());
            return lookup.findVirtual(ListenerAdapter.class, name, type);
        }
        catch (NoSuchMethodException | IllegalAccessException ignored) {} // this means this is probably a custom event!
        return null;
    }
}
