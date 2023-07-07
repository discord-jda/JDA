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

import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.api.events.*;
import net.dv8tion.jda.api.events.automod.AutoModExecutionEvent;
import net.dv8tion.jda.api.events.automod.AutoModRuleCreateEvent;
import net.dv8tion.jda.api.events.automod.AutoModRuleDeleteEvent;
import net.dv8tion.jda.api.events.automod.AutoModRuleUpdateEvent;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.GenericChannelEvent;
import net.dv8tion.jda.api.events.channel.forum.ForumTagAddEvent;
import net.dv8tion.jda.api.events.channel.forum.ForumTagRemoveEvent;
import net.dv8tion.jda.api.events.channel.forum.GenericForumTagEvent;
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateEmojiEvent;
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateModeratedEvent;
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.forum.update.GenericForumTagUpdateEvent;
import net.dv8tion.jda.api.events.channel.update.*;
import net.dv8tion.jda.api.events.emoji.EmojiAddedEvent;
import net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent;
import net.dv8tion.jda.api.events.emoji.GenericEmojiEvent;
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateNameEvent;
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateRolesEvent;
import net.dv8tion.jda.api.events.emoji.update.GenericEmojiUpdateEvent;
import net.dv8tion.jda.api.events.guild.*;
import net.dv8tion.jda.api.events.guild.invite.GenericGuildInviteEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.*;
import net.dv8tion.jda.api.events.guild.member.update.*;
import net.dv8tion.jda.api.events.guild.override.GenericPermissionOverrideEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideCreateEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideDeleteEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideUpdateEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventCreateEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventDeleteEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventUserAddEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventUserRemoveEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.*;
import net.dv8tion.jda.api.events.guild.update.*;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.events.http.HttpRequestEvent;
import net.dv8tion.jda.api.events.interaction.GenericAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.interaction.component.*;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.events.message.react.*;
import net.dv8tion.jda.api.events.role.GenericRoleEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.*;
import net.dv8tion.jda.api.events.self.*;
import net.dv8tion.jda.api.events.session.*;
import net.dv8tion.jda.api.events.stage.GenericStageInstanceEvent;
import net.dv8tion.jda.api.events.stage.StageInstanceCreateEvent;
import net.dv8tion.jda.api.events.stage.StageInstanceDeleteEvent;
import net.dv8tion.jda.api.events.stage.update.GenericStageInstanceUpdateEvent;
import net.dv8tion.jda.api.events.stage.update.StageInstanceUpdatePrivacyLevelEvent;
import net.dv8tion.jda.api.events.stage.update.StageInstanceUpdateTopicEvent;
import net.dv8tion.jda.api.events.sticker.GenericGuildStickerEvent;
import net.dv8tion.jda.api.events.sticker.GuildStickerAddedEvent;
import net.dv8tion.jda.api.events.sticker.GuildStickerRemovedEvent;
import net.dv8tion.jda.api.events.sticker.update.*;
import net.dv8tion.jda.api.events.thread.GenericThreadEvent;
import net.dv8tion.jda.api.events.thread.ThreadHiddenEvent;
import net.dv8tion.jda.api.events.thread.ThreadRevealedEvent;
import net.dv8tion.jda.api.events.thread.member.GenericThreadMemberEvent;
import net.dv8tion.jda.api.events.thread.member.ThreadMemberJoinEvent;
import net.dv8tion.jda.api.events.thread.member.ThreadMemberLeaveEvent;
import net.dv8tion.jda.api.events.user.GenericUserEvent;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.events.user.UserTypingEvent;
import net.dv8tion.jda.api.events.user.update.*;
import net.dv8tion.jda.internal.utils.ClassWalker;

import javax.annotation.Nonnull;
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
 * <p><b>Example:</b><br>
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
public abstract class ListenerAdapter implements EventListener
{
    public void onGenericEvent(@Nonnull GenericEvent event) {}
    public void onGenericUpdate(@Nonnull UpdateEvent<?, ?> event) {}
    public void onRawGateway(@Nonnull RawGatewayEvent event) {}
    public void onGatewayPing(@Nonnull GatewayPingEvent event) {}

    //Session Events
    public void onReady(@Nonnull ReadyEvent event) {}
    public void onSessionInvalidate(@Nonnull SessionInvalidateEvent event) {}
    public void onSessionDisconnect(@Nonnull SessionDisconnectEvent event) {}
    public void onSessionResume(@Nonnull SessionResumeEvent event) {}
    public void onSessionRecreate(@Nonnull SessionRecreateEvent event) {}
    public void onShutdown(@Nonnull ShutdownEvent event) {}

    //Status Events
    public void onStatusChange(@Nonnull StatusChangeEvent event) {}
    public void onException(@Nonnull ExceptionEvent event) {}

    //Interaction Events
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {}
    public void onUserContextInteraction(@Nonnull UserContextInteractionEvent event) {}
    public void onMessageContextInteraction(@Nonnull MessageContextInteractionEvent event) {}
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {}
    public void onCommandAutoCompleteInteraction(@Nonnull CommandAutoCompleteInteractionEvent event) {}
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {}
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {}
    public void onEntitySelectInteraction(@Nonnull EntitySelectInteractionEvent event) {}

    //User Events
    public void onUserUpdateName(@Nonnull UserUpdateNameEvent event) {}
    public void onUserUpdateGlobalName(@Nonnull UserUpdateGlobalNameEvent event) {}
    @Deprecated
    @ForRemoval
    public void onUserUpdateDiscriminator(@Nonnull UserUpdateDiscriminatorEvent event) {}
    public void onUserUpdateAvatar(@Nonnull UserUpdateAvatarEvent event) {}
    public void onUserUpdateOnlineStatus(@Nonnull UserUpdateOnlineStatusEvent event) {}
    public void onUserUpdateActivityOrder(@Nonnull UserUpdateActivityOrderEvent event) {}
    public void onUserUpdateFlags(@Nonnull UserUpdateFlagsEvent event) {}
    public void onUserTyping(@Nonnull UserTypingEvent event) {}
    public void onUserActivityStart(@Nonnull UserActivityStartEvent event) {}
    public void onUserActivityEnd(@Nonnull UserActivityEndEvent event) {}
    public void onUserUpdateActivities(@Nonnull UserUpdateActivitiesEvent event) {}

    //Self Events. Fires only in relation to the currently logged in account.
    public void onSelfUpdateAvatar(@Nonnull SelfUpdateAvatarEvent event) {}
    public void onSelfUpdateMFA(@Nonnull SelfUpdateMFAEvent event) {}
    public void onSelfUpdateName(@Nonnull SelfUpdateNameEvent event) {}
    public void onSelfUpdateGlobalName(@Nonnull SelfUpdateGlobalNameEvent event) {}
    public void onSelfUpdateVerified(@Nonnull SelfUpdateVerifiedEvent event) {}

    //Message Events
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {}
    public void onMessageUpdate(@Nonnull MessageUpdateEvent event) {}
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {}
    public void onMessageBulkDelete(@Nonnull MessageBulkDeleteEvent event) {}
    public void onMessageEmbed(@Nonnull MessageEmbedEvent event) {}
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {}
    public void onMessageReactionRemove(@Nonnull MessageReactionRemoveEvent event) {}
    public void onMessageReactionRemoveAll(@Nonnull MessageReactionRemoveAllEvent event) {}
    public void onMessageReactionRemoveEmoji(@Nonnull MessageReactionRemoveEmojiEvent event) {}

    //PermissionOverride Events
    public void onPermissionOverrideDelete(@Nonnull PermissionOverrideDeleteEvent event) {}
    public void onPermissionOverrideUpdate(@Nonnull PermissionOverrideUpdateEvent event) {}
    public void onPermissionOverrideCreate(@Nonnull PermissionOverrideCreateEvent event) {}

    //StageInstance Event
    public void onStageInstanceDelete(@Nonnull StageInstanceDeleteEvent event) {}
    public void onStageInstanceUpdateTopic(@Nonnull StageInstanceUpdateTopicEvent event) {}
    public void onStageInstanceUpdatePrivacyLevel(@Nonnull StageInstanceUpdatePrivacyLevelEvent event) {}
    public void onStageInstanceCreate(@Nonnull StageInstanceCreateEvent event) {}

    //Channel Events
    public void onChannelCreate(@Nonnull ChannelCreateEvent event) {}
    public void onChannelDelete(@Nonnull ChannelDeleteEvent event) {}

    //Channel Update Events
    public void onChannelUpdateBitrate(@Nonnull ChannelUpdateBitrateEvent event) {}
    public void onChannelUpdateName(@Nonnull ChannelUpdateNameEvent event) {}
    public void onChannelUpdateFlags(@Nonnull ChannelUpdateFlagsEvent event) {}
    public void onChannelUpdateNSFW(@Nonnull ChannelUpdateNSFWEvent event) {}
    public void onChannelUpdateParent(@Nonnull ChannelUpdateParentEvent event) {}
    public void onChannelUpdatePosition(@Nonnull ChannelUpdatePositionEvent event) {}
    public void onChannelUpdateRegion(@Nonnull ChannelUpdateRegionEvent event) {}
    public void onChannelUpdateSlowmode(@Nonnull ChannelUpdateSlowmodeEvent event) {}
    public void onChannelUpdateDefaultThreadSlowmode(@Nonnull ChannelUpdateDefaultThreadSlowmodeEvent event) {}
    public void onChannelUpdateDefaultReaction(@Nonnull ChannelUpdateDefaultReactionEvent event) {}
//    public void onChannelUpdateDefaultSortOrder(@Nonnull ChannelUpdateDefaultSortOrderEvent event) {}
    public void onChannelUpdateDefaultLayout(@Nonnull ChannelUpdateDefaultLayoutEvent event) {}
    public void onChannelUpdateTopic(@Nonnull ChannelUpdateTopicEvent event) {}
    public void onChannelUpdateType(@Nonnull ChannelUpdateTypeEvent event) {}
    public void onChannelUpdateUserLimit(@Nonnull ChannelUpdateUserLimitEvent event) {}
    public void onChannelUpdateArchived(@Nonnull ChannelUpdateArchivedEvent event) {}
    public void onChannelUpdateArchiveTimestamp(@Nonnull ChannelUpdateArchiveTimestampEvent event) {}
    public void onChannelUpdateAutoArchiveDuration(@Nonnull ChannelUpdateAutoArchiveDurationEvent event) {}
    public void onChannelUpdateLocked(@Nonnull ChannelUpdateLockedEvent event) {}
    public void onChannelUpdateInvitable(@Nonnull ChannelUpdateInvitableEvent event) {}
    public void onChannelUpdateAppliedTags(@Nonnull ChannelUpdateAppliedTagsEvent event) {}

    //Forum Tag Events
    public void onForumTagAdd(@Nonnull ForumTagAddEvent event) {}
    public void onForumTagRemove(@Nonnull ForumTagRemoveEvent event) {}
    public void onForumTagUpdateName(@Nonnull ForumTagUpdateNameEvent event) {}
    public void onForumTagUpdateEmoji(@Nonnull ForumTagUpdateEmojiEvent event) {}
    public void onForumTagUpdateModerated(@Nonnull ForumTagUpdateModeratedEvent event) {}

    //Thread Events
    public void onThreadRevealed(@Nonnull ThreadRevealedEvent event) {}
    public void onThreadHidden(@Nonnull ThreadHiddenEvent event) {}

    //Thread Member Events
    public void onThreadMemberJoin(@Nonnull ThreadMemberJoinEvent event) {}
    public void onThreadMemberLeave(@Nonnull ThreadMemberLeaveEvent event) {}

    //Guild Events
    public void onGuildReady(@Nonnull GuildReadyEvent event) {}
    public void onGuildTimeout(@Nonnull GuildTimeoutEvent event) {}
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {}
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {}
    public void onGuildAvailable(@Nonnull GuildAvailableEvent event) {}
    public void onGuildUnavailable(@Nonnull GuildUnavailableEvent event) {}
    public void onUnavailableGuildJoined(@Nonnull UnavailableGuildJoinedEvent event) {}
    public void onUnavailableGuildLeave(@Nonnull UnavailableGuildLeaveEvent event) {}
    public void onGuildBan(@Nonnull GuildBanEvent event) {}
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {}
    public void onGuildAuditLogEntryCreate(@Nonnull GuildAuditLogEntryCreateEvent event) {}
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {}

    //Guild Update Events
    public void onGuildUpdateAfkChannel(@Nonnull GuildUpdateAfkChannelEvent event) {}
    public void onGuildUpdateSystemChannel(@Nonnull GuildUpdateSystemChannelEvent event) {}
    public void onGuildUpdateRulesChannel(@Nonnull GuildUpdateRulesChannelEvent event) {}
    public void onGuildUpdateCommunityUpdatesChannel(@Nonnull GuildUpdateCommunityUpdatesChannelEvent event) {}
    public void onGuildUpdateAfkTimeout(@Nonnull GuildUpdateAfkTimeoutEvent event) {}
    public void onGuildUpdateExplicitContentLevel(@Nonnull GuildUpdateExplicitContentLevelEvent event) {}
    public void onGuildUpdateIcon(@Nonnull GuildUpdateIconEvent event) {}
    public void onGuildUpdateMFALevel(@Nonnull GuildUpdateMFALevelEvent event) {}
    public void onGuildUpdateName(@Nonnull GuildUpdateNameEvent event){}
    public void onGuildUpdateNotificationLevel(@Nonnull GuildUpdateNotificationLevelEvent event) {}
    public void onGuildUpdateOwner(@Nonnull GuildUpdateOwnerEvent event) {}
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
    public void onGuildUpdateNSFWLevel(@Nonnull GuildUpdateNSFWLevelEvent event) {}

    //Scheduled Event Events
    public void onScheduledEventUpdateDescription(@Nonnull ScheduledEventUpdateDescriptionEvent event) {}
    public void onScheduledEventUpdateEndTime(@Nonnull ScheduledEventUpdateEndTimeEvent event) {}
    public void onScheduledEventUpdateLocation(@Nonnull ScheduledEventUpdateLocationEvent event) {}
    public void onScheduledEventUpdateName(@Nonnull ScheduledEventUpdateNameEvent event) {}
    public void onScheduledEventUpdateStartTime(@Nonnull ScheduledEventUpdateStartTimeEvent event) {}
    public void onScheduledEventUpdateStatus(@Nonnull ScheduledEventUpdateStatusEvent event) {}

    public void onScheduledEventCreate(@Nonnull ScheduledEventCreateEvent event) {}
    public void onScheduledEventDelete(@Nonnull ScheduledEventDeleteEvent event) {}
    public void onScheduledEventUserAdd(@Nonnull ScheduledEventUserAddEvent event) {}
    public void onScheduledEventUserRemove(@Nonnull ScheduledEventUserRemoveEvent event) {}

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
    public void onGuildMemberUpdateAvatar(@Nonnull GuildMemberUpdateAvatarEvent event) {}
    public void onGuildMemberUpdateBoostTime(@Nonnull GuildMemberUpdateBoostTimeEvent event) {}
    public void onGuildMemberUpdatePending(@Nonnull GuildMemberUpdatePendingEvent event) {}
    public void onGuildMemberUpdateFlags(@Nonnull GuildMemberUpdateFlagsEvent event) {}
    public void onGuildMemberUpdateTimeOut(@Nonnull GuildMemberUpdateTimeOutEvent event) {}

    //Guild Voice Events
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {}
    public void onGuildVoiceMute(@Nonnull GuildVoiceMuteEvent event) {}
    public void onGuildVoiceDeafen(@Nonnull GuildVoiceDeafenEvent event) {}
    public void onGuildVoiceGuildMute(@Nonnull GuildVoiceGuildMuteEvent event) {}
    public void onGuildVoiceGuildDeafen(@Nonnull GuildVoiceGuildDeafenEvent event) {}
    public void onGuildVoiceSelfMute(@Nonnull GuildVoiceSelfMuteEvent event) {}
    public void onGuildVoiceSelfDeafen(@Nonnull GuildVoiceSelfDeafenEvent event) {}
    public void onGuildVoiceSuppress(@Nonnull GuildVoiceSuppressEvent event) {}
    public void onGuildVoiceStream(@Nonnull GuildVoiceStreamEvent event) {}
    public void onGuildVoiceVideo(@Nonnull GuildVoiceVideoEvent event) {}
    public void onGuildVoiceRequestToSpeak(@Nonnull GuildVoiceRequestToSpeakEvent event) {}

    //Guild AutoMod Events
    public void onAutoModExecution(@Nonnull AutoModExecutionEvent event) {}
    public void onAutoModRuleCreate(@Nonnull AutoModRuleCreateEvent event) {}
    public void onAutoModRuleUpdate(@Nonnull AutoModRuleUpdateEvent event) {}
    public void onAutoModRuleDelete(@Nonnull AutoModRuleDeleteEvent event) {}

    //Role events
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {}
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {}

    //Role Update Events
    public void onRoleUpdateColor(@Nonnull RoleUpdateColorEvent event) {}
    public void onRoleUpdateHoisted(@Nonnull RoleUpdateHoistedEvent event) {}
    public void onRoleUpdateIcon(@Nonnull RoleUpdateIconEvent event) {}
    public void onRoleUpdateMentionable(@Nonnull RoleUpdateMentionableEvent event) {}
    public void onRoleUpdateName(@Nonnull RoleUpdateNameEvent event) {}
    public void onRoleUpdatePermissions(@Nonnull RoleUpdatePermissionsEvent event) {}
    public void onRoleUpdatePosition(@Nonnull RoleUpdatePositionEvent event) {}

    //Emoji Events
    public void onEmojiAdded(@Nonnull EmojiAddedEvent event) {}
    public void onEmojiRemoved(@Nonnull EmojiRemovedEvent event) {}

    //Emoji Update Events
    public void onEmojiUpdateName(@Nonnull EmojiUpdateNameEvent event) {}
    public void onEmojiUpdateRoles(@Nonnull EmojiUpdateRolesEvent event) {}

    // Application command permission update events
    public void onGenericPrivilegeUpdate(@Nonnull GenericPrivilegeUpdateEvent event) {}
    public void onApplicationCommandUpdatePrivileges(@Nonnull ApplicationCommandUpdatePrivilegesEvent event) {}
    public void onApplicationUpdatePrivileges(@Nonnull ApplicationUpdatePrivilegesEvent event) {}

    //Sticker Events
    public void onGuildStickerAdded(@Nonnull GuildStickerAddedEvent event) {}
    public void onGuildStickerRemoved(@Nonnull GuildStickerRemovedEvent event) {}

    //Sticker Update Events
    public void onGuildStickerUpdateName(@Nonnull GuildStickerUpdateNameEvent event) {}
    public void onGuildStickerUpdateTags(@Nonnull GuildStickerUpdateTagsEvent event) {}
    public void onGuildStickerUpdateDescription(@Nonnull GuildStickerUpdateDescriptionEvent event) {}
    public void onGuildStickerUpdateAvailable(@Nonnull GuildStickerUpdateAvailableEvent event) {}

    // Debug Events
    public void onHttpRequest(@Nonnull HttpRequestEvent event) {}

    //Generic Events
    public void onGenericSessionEvent(@Nonnull GenericSessionEvent event) {}
    public void onGenericInteractionCreate(@Nonnull GenericInteractionCreateEvent event) {}
    public void onGenericAutoCompleteInteraction(@Nonnull GenericAutoCompleteInteractionEvent event) {}
    public void onGenericComponentInteractionCreate(@Nonnull GenericComponentInteractionCreateEvent event) {}
    public void onGenericCommandInteraction(@Nonnull GenericCommandInteractionEvent event) {}
    public void onGenericContextInteraction(@Nonnull GenericContextInteractionEvent<?> event) {}
    public void onGenericSelectMenuInteraction(@Nonnull GenericSelectMenuInteractionEvent event) {}
    public void onGenericMessage(@Nonnull GenericMessageEvent event) {}
    public void onGenericMessageReaction(@Nonnull GenericMessageReactionEvent event) {}
    public void onGenericUser(@Nonnull GenericUserEvent event) {}
    public void onGenericUserPresence(@Nonnull GenericUserPresenceEvent event) {}
    public void onGenericSelfUpdate(@Nonnull GenericSelfUpdateEvent event) {}
    public void onGenericStageInstance(@Nonnull GenericStageInstanceEvent event) {}
    public void onGenericStageInstanceUpdate(@Nonnull GenericStageInstanceUpdateEvent event) {}
    public void onGenericChannel(@Nonnull GenericChannelEvent event) {}
    public void onGenericChannelUpdate(@Nonnull GenericChannelUpdateEvent<?> event) {}
    public void onGenericThread(@Nonnull GenericThreadEvent event) {}
    public void onGenericThreadMember(@Nonnull GenericThreadMemberEvent event) {}
    public void onGenericGuild(@Nonnull GenericGuildEvent event) {}
    public void onGenericGuildUpdate(@Nonnull GenericGuildUpdateEvent event) {}
    public void onGenericGuildInvite(@Nonnull GenericGuildInviteEvent event) {}
    public void onGenericGuildMember(@Nonnull GenericGuildMemberEvent event) {}
    public void onGenericGuildMemberUpdate(@Nonnull GenericGuildMemberUpdateEvent event) {}
    public void onGenericGuildVoice(@Nonnull GenericGuildVoiceEvent event) {}
    public void onGenericRole(@Nonnull GenericRoleEvent event) {}
    public void onGenericRoleUpdate(@Nonnull GenericRoleUpdateEvent event) {}
    public void onGenericEmoji(@Nonnull GenericEmojiEvent event) {}
    public void onGenericEmojiUpdate(@Nonnull GenericEmojiUpdateEvent event) {}
    public void onGenericGuildSticker(@Nonnull GenericGuildStickerEvent event) {}
    public void onGenericGuildStickerUpdate(@Nonnull GenericGuildStickerUpdateEvent event) {}
    public void onGenericPermissionOverride(@Nonnull GenericPermissionOverrideEvent event) {}
    public void onGenericScheduledEventUpdate(@Nonnull GenericScheduledEventUpdateEvent event) {}
    public void onGenericForumTag(@Nonnull GenericForumTagEvent event) {}
    public void onGenericForumTagUpdate(@Nonnull GenericForumTagUpdateEvent event) {}

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
    public final void onEvent(@Nonnull GenericEvent event)
    {
        onGenericEvent(event);
        if (event instanceof UpdateEvent)
            onGenericUpdate((UpdateEvent<?, ?>) event);

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
