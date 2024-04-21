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
package net.dv8tion.jda.api.hooks

import net.dv8tion.jda.annotations.ForRemoval
import net.dv8tion.jda.annotations.ReplaceWith
import net.dv8tion.jda.api.events.*
import net.dv8tion.jda.api.events.automod.*
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.events.channel.GenericChannelEvent
import net.dv8tion.jda.api.events.channel.forum.ForumTagAddEvent
import net.dv8tion.jda.api.events.channel.forum.ForumTagRemoveEvent
import net.dv8tion.jda.api.events.channel.forum.GenericForumTagEvent
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateEmojiEvent
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateModeratedEvent
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateNameEvent
import net.dv8tion.jda.api.events.channel.forum.update.GenericForumTagUpdateEvent
import net.dv8tion.jda.api.events.channel.update.*
import net.dv8tion.jda.api.events.emoji.EmojiAddedEvent
import net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent
import net.dv8tion.jda.api.events.emoji.GenericEmojiEvent
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateNameEvent
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateRolesEvent
import net.dv8tion.jda.api.events.emoji.update.GenericEmojiUpdateEvent
import net.dv8tion.jda.api.events.entitlement.EntitlementCreateEvent
import net.dv8tion.jda.api.events.entitlement.EntitlementDeleteEvent
import net.dv8tion.jda.api.events.entitlement.EntitlementUpdateEvent
import net.dv8tion.jda.api.events.entitlement.GenericEntitlementEvent
import net.dv8tion.jda.api.events.guild.*
import net.dv8tion.jda.api.events.guild.invite.GenericGuildInviteEvent
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent
import net.dv8tion.jda.api.events.guild.member.*
import net.dv8tion.jda.api.events.guild.member.update.*
import net.dv8tion.jda.api.events.guild.override.GenericPermissionOverrideEvent
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideCreateEvent
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideDeleteEvent
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideUpdateEvent
import net.dv8tion.jda.api.events.guild.scheduledevent.*
import net.dv8tion.jda.api.events.guild.scheduledevent.update.*
import net.dv8tion.jda.api.events.guild.update.*
import net.dv8tion.jda.api.events.guild.voice.*
import net.dv8tion.jda.api.events.http.HttpRequestEvent
import net.dv8tion.jda.api.events.interaction.GenericAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.*
import net.dv8tion.jda.api.events.interaction.component.*
import net.dv8tion.jda.api.events.message.*
import net.dv8tion.jda.api.events.message.react.*
import net.dv8tion.jda.api.events.role.GenericRoleEvent
import net.dv8tion.jda.api.events.role.RoleCreateEvent
import net.dv8tion.jda.api.events.role.RoleDeleteEvent
import net.dv8tion.jda.api.events.role.update.*
import net.dv8tion.jda.api.events.self.*
import net.dv8tion.jda.api.events.session.*
import net.dv8tion.jda.api.events.stage.GenericStageInstanceEvent
import net.dv8tion.jda.api.events.stage.StageInstanceCreateEvent
import net.dv8tion.jda.api.events.stage.StageInstanceDeleteEvent
import net.dv8tion.jda.api.events.stage.update.GenericStageInstanceUpdateEvent
import net.dv8tion.jda.api.events.stage.update.StageInstanceUpdatePrivacyLevelEvent
import net.dv8tion.jda.api.events.stage.update.StageInstanceUpdateTopicEvent
import net.dv8tion.jda.api.events.sticker.GenericGuildStickerEvent
import net.dv8tion.jda.api.events.sticker.GuildStickerAddedEvent
import net.dv8tion.jda.api.events.sticker.GuildStickerRemovedEvent
import net.dv8tion.jda.api.events.sticker.update.*
import net.dv8tion.jda.api.events.thread.GenericThreadEvent
import net.dv8tion.jda.api.events.thread.ThreadHiddenEvent
import net.dv8tion.jda.api.events.thread.ThreadRevealedEvent
import net.dv8tion.jda.api.events.thread.member.GenericThreadMemberEvent
import net.dv8tion.jda.api.events.thread.member.ThreadMemberJoinEvent
import net.dv8tion.jda.api.events.thread.member.ThreadMemberLeaveEvent
import net.dv8tion.jda.api.events.user.GenericUserEvent
import net.dv8tion.jda.api.events.user.UserActivityEndEvent
import net.dv8tion.jda.api.events.user.UserActivityStartEvent
import net.dv8tion.jda.api.events.user.UserTypingEvent
import net.dv8tion.jda.api.events.user.update.*
import net.dv8tion.jda.internal.utils.ClassWalker
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.annotation.Nonnull

/**
 * An abstract implementation of [EventListener][net.dv8tion.jda.api.hooks.EventListener] which divides [Events][net.dv8tion.jda.api.events.Event]
 * for you. You should **<u>override</u>** the methods provided by this class for your event listener implementation.
 *
 *
 * **Example:**<br></br>
 * <pre>`
 * public class MyReadyListener extends ListenerAdapter
 * {
 * @Override
 * public void onReady(ReadyEvent event)
 * {
 * System.out.println("I am ready to go!");
 * }
 *
 * @Override
 * public void onMessageReceived(MessageReceivedEvent event)
 * {
 * System.out.printf("[%s]: %s\n", event.getAuthor().getName(), event.getMessage().getContentDisplay());
 * }
 * }
`</pre> *
 *
 * @see net.dv8tion.jda.api.hooks.EventListener EventListener
 *
 * @see net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventManager
 */
abstract class ListenerAdapter : EventListener {
    fun onGenericEvent(@Nonnull event: GenericEvent?) {}
    fun onGenericUpdate(@Nonnull event: UpdateEvent<*, *>?) {}
    fun onRawGateway(@Nonnull event: RawGatewayEvent?) {}
    fun onGatewayPing(@Nonnull event: GatewayPingEvent?) {}

    //Session Events
    fun onReady(@Nonnull event: ReadyEvent?) {}
    fun onSessionInvalidate(@Nonnull event: SessionInvalidateEvent?) {}
    fun onSessionDisconnect(@Nonnull event: SessionDisconnectEvent?) {}
    fun onSessionResume(@Nonnull event: SessionResumeEvent?) {}
    fun onSessionRecreate(@Nonnull event: SessionRecreateEvent?) {}
    fun onShutdown(@Nonnull event: ShutdownEvent?) {}

    //Status Events
    fun onStatusChange(@Nonnull event: StatusChangeEvent?) {}
    fun onException(@Nonnull event: ExceptionEvent?) {}

    //Interaction Events
    open fun onSlashCommandInteraction(@Nonnull event: SlashCommandInteractionEvent?) {}
    fun onUserContextInteraction(@Nonnull event: UserContextInteractionEvent?) {}
    fun onMessageContextInteraction(@Nonnull event: MessageContextInteractionEvent?) {}
    open fun onButtonInteraction(@Nonnull event: ButtonInteractionEvent?) {}
    fun onCommandAutoCompleteInteraction(@Nonnull event: CommandAutoCompleteInteractionEvent?) {}
    fun onModalInteraction(@Nonnull event: ModalInteractionEvent?) {}
    fun onStringSelectInteraction(@Nonnull event: StringSelectInteractionEvent?) {}
    fun onEntitySelectInteraction(@Nonnull event: EntitySelectInteractionEvent?) {}

    //User Events
    fun onUserUpdateName(@Nonnull event: UserUpdateNameEvent?) {}
    fun onUserUpdateGlobalName(@Nonnull event: UserUpdateGlobalNameEvent?) {}
    @Deprecated("")
    @ForRemoval
    fun onUserUpdateDiscriminator(@Nonnull event: UserUpdateDiscriminatorEvent?) {
    }

    fun onUserUpdateAvatar(@Nonnull event: UserUpdateAvatarEvent?) {}
    fun onUserUpdateOnlineStatus(@Nonnull event: UserUpdateOnlineStatusEvent?) {}
    fun onUserUpdateActivityOrder(@Nonnull event: UserUpdateActivityOrderEvent?) {}
    fun onUserUpdateFlags(@Nonnull event: UserUpdateFlagsEvent?) {}
    fun onUserTyping(@Nonnull event: UserTypingEvent?) {}
    fun onUserActivityStart(@Nonnull event: UserActivityStartEvent?) {}
    fun onUserActivityEnd(@Nonnull event: UserActivityEndEvent?) {}
    fun onUserUpdateActivities(@Nonnull event: UserUpdateActivitiesEvent?) {}

    //Self Events. Fires only in relation to the currently logged in account.
    fun onSelfUpdateAvatar(@Nonnull event: SelfUpdateAvatarEvent?) {}
    fun onSelfUpdateMFA(@Nonnull event: SelfUpdateMFAEvent?) {}
    fun onSelfUpdateName(@Nonnull event: SelfUpdateNameEvent?) {}
    fun onSelfUpdateGlobalName(@Nonnull event: SelfUpdateGlobalNameEvent?) {}
    fun onSelfUpdateVerified(@Nonnull event: SelfUpdateVerifiedEvent?) {}

    //Message Events
    open fun onMessageReceived(@Nonnull event: MessageReceivedEvent?) {}
    fun onMessageUpdate(@Nonnull event: MessageUpdateEvent?) {}
    fun onMessageDelete(@Nonnull event: MessageDeleteEvent?) {}
    fun onMessageBulkDelete(@Nonnull event: MessageBulkDeleteEvent?) {}
    fun onMessageEmbed(@Nonnull event: MessageEmbedEvent?) {}
    open fun onMessageReactionAdd(@Nonnull event: MessageReactionAddEvent?) {}
    fun onMessageReactionRemove(@Nonnull event: MessageReactionRemoveEvent?) {}
    fun onMessageReactionRemoveAll(@Nonnull event: MessageReactionRemoveAllEvent?) {}
    fun onMessageReactionRemoveEmoji(@Nonnull event: MessageReactionRemoveEmojiEvent?) {}

    //PermissionOverride Events
    fun onPermissionOverrideDelete(@Nonnull event: PermissionOverrideDeleteEvent?) {}
    fun onPermissionOverrideUpdate(@Nonnull event: PermissionOverrideUpdateEvent?) {}
    fun onPermissionOverrideCreate(@Nonnull event: PermissionOverrideCreateEvent?) {}

    //StageInstance Event
    fun onStageInstanceDelete(@Nonnull event: StageInstanceDeleteEvent?) {}
    fun onStageInstanceUpdateTopic(@Nonnull event: StageInstanceUpdateTopicEvent?) {}
    fun onStageInstanceUpdatePrivacyLevel(@Nonnull event: StageInstanceUpdatePrivacyLevelEvent?) {}
    fun onStageInstanceCreate(@Nonnull event: StageInstanceCreateEvent?) {}

    //Channel Events
    fun onChannelCreate(@Nonnull event: ChannelCreateEvent?) {}
    fun onChannelDelete(@Nonnull event: ChannelDeleteEvent?) {}

    //Channel Update Events
    fun onChannelUpdateBitrate(@Nonnull event: ChannelUpdateBitrateEvent?) {}
    fun onChannelUpdateName(@Nonnull event: ChannelUpdateNameEvent?) {}
    fun onChannelUpdateFlags(@Nonnull event: ChannelUpdateFlagsEvent?) {}
    fun onChannelUpdateNSFW(@Nonnull event: ChannelUpdateNSFWEvent?) {}
    fun onChannelUpdateParent(@Nonnull event: ChannelUpdateParentEvent?) {}
    fun onChannelUpdatePosition(@Nonnull event: ChannelUpdatePositionEvent?) {}
    fun onChannelUpdateRegion(@Nonnull event: ChannelUpdateRegionEvent?) {}
    fun onChannelUpdateSlowmode(@Nonnull event: ChannelUpdateSlowmodeEvent?) {}
    fun onChannelUpdateDefaultThreadSlowmode(@Nonnull event: ChannelUpdateDefaultThreadSlowmodeEvent?) {}
    fun onChannelUpdateDefaultReaction(@Nonnull event: ChannelUpdateDefaultReactionEvent?) {}
    fun onChannelUpdateDefaultSortOrder(@Nonnull event: ChannelUpdateDefaultSortOrderEvent?) {}
    fun onChannelUpdateDefaultLayout(@Nonnull event: ChannelUpdateDefaultLayoutEvent?) {}
    fun onChannelUpdateTopic(@Nonnull event: ChannelUpdateTopicEvent?) {}
    fun onChannelUpdateVoiceStatus(@Nonnull event: ChannelUpdateVoiceStatusEvent?) {}
    fun onChannelUpdateType(@Nonnull event: ChannelUpdateTypeEvent?) {}
    fun onChannelUpdateUserLimit(@Nonnull event: ChannelUpdateUserLimitEvent?) {}
    fun onChannelUpdateArchived(@Nonnull event: ChannelUpdateArchivedEvent?) {}
    fun onChannelUpdateArchiveTimestamp(@Nonnull event: ChannelUpdateArchiveTimestampEvent?) {}
    fun onChannelUpdateAutoArchiveDuration(@Nonnull event: ChannelUpdateAutoArchiveDurationEvent?) {}
    fun onChannelUpdateLocked(@Nonnull event: ChannelUpdateLockedEvent?) {}
    fun onChannelUpdateInvitable(@Nonnull event: ChannelUpdateInvitableEvent?) {}
    fun onChannelUpdateAppliedTags(@Nonnull event: ChannelUpdateAppliedTagsEvent?) {}

    //Forum Tag Events
    fun onForumTagAdd(@Nonnull event: ForumTagAddEvent?) {}
    fun onForumTagRemove(@Nonnull event: ForumTagRemoveEvent?) {}
    fun onForumTagUpdateName(@Nonnull event: ForumTagUpdateNameEvent?) {}
    fun onForumTagUpdateEmoji(@Nonnull event: ForumTagUpdateEmojiEvent?) {}
    fun onForumTagUpdateModerated(@Nonnull event: ForumTagUpdateModeratedEvent?) {}

    //Thread Events
    fun onThreadRevealed(@Nonnull event: ThreadRevealedEvent?) {}
    fun onThreadHidden(@Nonnull event: ThreadHiddenEvent?) {}

    //Thread Member Events
    fun onThreadMemberJoin(@Nonnull event: ThreadMemberJoinEvent?) {}
    fun onThreadMemberLeave(@Nonnull event: ThreadMemberLeaveEvent?) {}

    //Guild Events
    fun onGuildReady(@Nonnull event: GuildReadyEvent?) {}
    fun onGuildTimeout(@Nonnull event: GuildTimeoutEvent?) {}
    fun onGuildJoin(@Nonnull event: GuildJoinEvent?) {}
    fun onGuildLeave(@Nonnull event: GuildLeaveEvent?) {}
    fun onGuildAvailable(@Nonnull event: GuildAvailableEvent?) {}
    fun onGuildUnavailable(@Nonnull event: GuildUnavailableEvent?) {}
    fun onUnavailableGuildJoined(@Nonnull event: UnavailableGuildJoinedEvent?) {}
    fun onUnavailableGuildLeave(@Nonnull event: UnavailableGuildLeaveEvent?) {}
    fun onGuildBan(@Nonnull event: GuildBanEvent?) {}
    fun onGuildUnban(@Nonnull event: GuildUnbanEvent?) {}
    fun onGuildAuditLogEntryCreate(@Nonnull event: GuildAuditLogEntryCreateEvent?) {}
    fun onGuildMemberRemove(@Nonnull event: GuildMemberRemoveEvent?) {}

    //Guild Update Events
    fun onGuildUpdateAfkChannel(@Nonnull event: GuildUpdateAfkChannelEvent?) {}
    fun onGuildUpdateSystemChannel(@Nonnull event: GuildUpdateSystemChannelEvent?) {}
    fun onGuildUpdateRulesChannel(@Nonnull event: GuildUpdateRulesChannelEvent?) {}
    fun onGuildUpdateCommunityUpdatesChannel(@Nonnull event: GuildUpdateCommunityUpdatesChannelEvent?) {}
    fun onGuildUpdateAfkTimeout(@Nonnull event: GuildUpdateAfkTimeoutEvent?) {}
    fun onGuildUpdateExplicitContentLevel(@Nonnull event: GuildUpdateExplicitContentLevelEvent?) {}
    fun onGuildUpdateIcon(@Nonnull event: GuildUpdateIconEvent?) {}
    fun onGuildUpdateMFALevel(@Nonnull event: GuildUpdateMFALevelEvent?) {}
    fun onGuildUpdateName(@Nonnull event: GuildUpdateNameEvent?) {}
    fun onGuildUpdateNotificationLevel(@Nonnull event: GuildUpdateNotificationLevelEvent?) {}
    fun onGuildUpdateOwner(@Nonnull event: GuildUpdateOwnerEvent?) {}
    fun onGuildUpdateSplash(@Nonnull event: GuildUpdateSplashEvent?) {}
    fun onGuildUpdateVerificationLevel(@Nonnull event: GuildUpdateVerificationLevelEvent?) {}
    fun onGuildUpdateLocale(@Nonnull event: GuildUpdateLocaleEvent?) {}
    fun onGuildUpdateFeatures(@Nonnull event: GuildUpdateFeaturesEvent?) {}
    fun onGuildUpdateVanityCode(@Nonnull event: GuildUpdateVanityCodeEvent?) {}
    fun onGuildUpdateBanner(@Nonnull event: GuildUpdateBannerEvent?) {}
    fun onGuildUpdateDescription(@Nonnull event: GuildUpdateDescriptionEvent?) {}
    fun onGuildUpdateBoostTier(@Nonnull event: GuildUpdateBoostTierEvent?) {}
    fun onGuildUpdateBoostCount(@Nonnull event: GuildUpdateBoostCountEvent?) {}
    fun onGuildUpdateMaxMembers(@Nonnull event: GuildUpdateMaxMembersEvent?) {}
    fun onGuildUpdateMaxPresences(@Nonnull event: GuildUpdateMaxPresencesEvent?) {}
    fun onGuildUpdateNSFWLevel(@Nonnull event: GuildUpdateNSFWLevelEvent?) {}

    //Scheduled Event Events
    fun onScheduledEventUpdateDescription(@Nonnull event: ScheduledEventUpdateDescriptionEvent?) {}
    fun onScheduledEventUpdateEndTime(@Nonnull event: ScheduledEventUpdateEndTimeEvent?) {}
    fun onScheduledEventUpdateLocation(@Nonnull event: ScheduledEventUpdateLocationEvent?) {}
    fun onScheduledEventUpdateName(@Nonnull event: ScheduledEventUpdateNameEvent?) {}
    fun onScheduledEventUpdateStartTime(@Nonnull event: ScheduledEventUpdateStartTimeEvent?) {}
    fun onScheduledEventUpdateStatus(@Nonnull event: ScheduledEventUpdateStatusEvent?) {}
    fun onScheduledEventUpdateImage(@Nonnull event: ScheduledEventUpdateImageEvent?) {}
    fun onScheduledEventCreate(@Nonnull event: ScheduledEventCreateEvent?) {}
    fun onScheduledEventDelete(@Nonnull event: ScheduledEventDeleteEvent?) {}
    fun onScheduledEventUserAdd(@Nonnull event: ScheduledEventUserAddEvent?) {}
    fun onScheduledEventUserRemove(@Nonnull event: ScheduledEventUserRemoveEvent?) {}

    //Guild Invite Events
    fun onGuildInviteCreate(@Nonnull event: GuildInviteCreateEvent?) {}
    fun onGuildInviteDelete(@Nonnull event: GuildInviteDeleteEvent?) {}

    //Guild Member Events
    fun onGuildMemberJoin(@Nonnull event: GuildMemberJoinEvent?) {}
    fun onGuildMemberRoleAdd(@Nonnull event: GuildMemberRoleAddEvent?) {}
    fun onGuildMemberRoleRemove(@Nonnull event: GuildMemberRoleRemoveEvent?) {}

    //Guild Member Update Events
    fun onGuildMemberUpdate(@Nonnull event: GuildMemberUpdateEvent?) {}
    fun onGuildMemberUpdateNickname(@Nonnull event: GuildMemberUpdateNicknameEvent?) {}
    fun onGuildMemberUpdateAvatar(@Nonnull event: GuildMemberUpdateAvatarEvent?) {}
    fun onGuildMemberUpdateBoostTime(@Nonnull event: GuildMemberUpdateBoostTimeEvent?) {}
    fun onGuildMemberUpdatePending(@Nonnull event: GuildMemberUpdatePendingEvent?) {}
    fun onGuildMemberUpdateFlags(@Nonnull event: GuildMemberUpdateFlagsEvent?) {}
    fun onGuildMemberUpdateTimeOut(@Nonnull event: GuildMemberUpdateTimeOutEvent?) {}

    //Guild Voice Events
    fun onGuildVoiceUpdate(@Nonnull event: GuildVoiceUpdateEvent?) {}
    fun onGuildVoiceMute(@Nonnull event: GuildVoiceMuteEvent?) {}
    fun onGuildVoiceDeafen(@Nonnull event: GuildVoiceDeafenEvent?) {}
    fun onGuildVoiceGuildMute(@Nonnull event: GuildVoiceGuildMuteEvent?) {}
    fun onGuildVoiceGuildDeafen(@Nonnull event: GuildVoiceGuildDeafenEvent?) {}
    fun onGuildVoiceSelfMute(@Nonnull event: GuildVoiceSelfMuteEvent?) {}
    fun onGuildVoiceSelfDeafen(@Nonnull event: GuildVoiceSelfDeafenEvent?) {}
    fun onGuildVoiceSuppress(@Nonnull event: GuildVoiceSuppressEvent?) {}
    fun onGuildVoiceStream(@Nonnull event: GuildVoiceStreamEvent?) {}
    fun onGuildVoiceVideo(@Nonnull event: GuildVoiceVideoEvent?) {}
    fun onGuildVoiceRequestToSpeak(@Nonnull event: GuildVoiceRequestToSpeakEvent?) {}

    //Guild AutoMod Events
    fun onAutoModExecution(@Nonnull event: AutoModExecutionEvent?) {}
    fun onAutoModRuleCreate(@Nonnull event: AutoModRuleCreateEvent?) {}
    fun onAutoModRuleUpdate(@Nonnull event: AutoModRuleUpdateEvent?) {}
    fun onAutoModRuleDelete(@Nonnull event: AutoModRuleDeleteEvent?) {}

    //Role events
    fun onRoleCreate(@Nonnull event: RoleCreateEvent?) {}
    fun onRoleDelete(@Nonnull event: RoleDeleteEvent?) {}

    //Role Update Events
    fun onRoleUpdateColor(@Nonnull event: RoleUpdateColorEvent?) {}
    fun onRoleUpdateHoisted(@Nonnull event: RoleUpdateHoistedEvent?) {}
    fun onRoleUpdateIcon(@Nonnull event: RoleUpdateIconEvent?) {}
    fun onRoleUpdateMentionable(@Nonnull event: RoleUpdateMentionableEvent?) {}
    fun onRoleUpdateName(@Nonnull event: RoleUpdateNameEvent?) {}
    fun onRoleUpdatePermissions(@Nonnull event: RoleUpdatePermissionsEvent?) {}
    fun onRoleUpdatePosition(@Nonnull event: RoleUpdatePositionEvent?) {}

    //Emoji Events
    fun onEmojiAdded(@Nonnull event: EmojiAddedEvent?) {}
    fun onEmojiRemoved(@Nonnull event: EmojiRemovedEvent?) {}

    //Emoji Update Events
    fun onEmojiUpdateName(@Nonnull event: EmojiUpdateNameEvent?) {}
    fun onEmojiUpdateRoles(@Nonnull event: EmojiUpdateRolesEvent?) {}

    // Application command permission update events
    fun onGenericPrivilegeUpdate(@Nonnull event: GenericPrivilegeUpdateEvent?) {}
    fun onApplicationCommandUpdatePrivileges(@Nonnull event: ApplicationCommandUpdatePrivilegesEvent?) {}
    fun onApplicationUpdatePrivileges(@Nonnull event: ApplicationUpdatePrivilegesEvent?) {}

    //Sticker Events
    fun onGuildStickerAdded(@Nonnull event: GuildStickerAddedEvent?) {}
    fun onGuildStickerRemoved(@Nonnull event: GuildStickerRemovedEvent?) {}

    //Sticker Update Events
    fun onGuildStickerUpdateName(@Nonnull event: GuildStickerUpdateNameEvent?) {}
    fun onGuildStickerUpdateTags(@Nonnull event: GuildStickerUpdateTagsEvent?) {}
    fun onGuildStickerUpdateDescription(@Nonnull event: GuildStickerUpdateDescriptionEvent?) {}
    fun onGuildStickerUpdateAvailable(@Nonnull event: GuildStickerUpdateAvailableEvent?) {}

    // Entitlement events
    fun onEntitlementCreate(@Nonnull event: EntitlementCreateEvent?) {}
    fun onEntitlementUpdate(@Nonnull event: EntitlementUpdateEvent?) {}
    fun onEntitlementDelete(@Nonnull event: EntitlementDeleteEvent?) {}

    // Debug Events
    fun onHttpRequest(@Nonnull event: HttpRequestEvent?) {}

    //Generic Events
    @Deprecated("")
    @ReplaceWith("onGenericSession(event)")
    @ForRemoval(deadline = "5.0.0")
    fun onGenericSessionEvent(@Nonnull event: GenericSessionEvent?) {
    }

    fun onGenericSession(@Nonnull event: GenericSessionEvent?) {}
    fun onGenericInteractionCreate(@Nonnull event: GenericInteractionCreateEvent?) {}
    fun onGenericAutoCompleteInteraction(@Nonnull event: GenericAutoCompleteInteractionEvent?) {}
    fun onGenericComponentInteractionCreate(@Nonnull event: GenericComponentInteractionCreateEvent?) {}
    fun onGenericCommandInteraction(@Nonnull event: GenericCommandInteractionEvent?) {}
    fun onGenericContextInteraction(@Nonnull event: GenericContextInteractionEvent<*>?) {}
    fun onGenericSelectMenuInteraction(@Nonnull event: GenericSelectMenuInteractionEvent<*, *>?) {}
    fun onGenericMessage(@Nonnull event: GenericMessageEvent?) {}
    fun onGenericMessageReaction(@Nonnull event: GenericMessageReactionEvent?) {}
    fun onGenericUser(@Nonnull event: GenericUserEvent?) {}
    fun onGenericUserPresence(@Nonnull event: GenericUserPresenceEvent?) {}
    fun onGenericUserUpdate(@Nonnull event: GenericUserUpdateEvent<*>?) {}
    fun onGenericSelfUpdate(@Nonnull event: GenericSelfUpdateEvent<*>?) {}
    fun onGenericStageInstance(@Nonnull event: GenericStageInstanceEvent?) {}
    fun onGenericStageInstanceUpdate(@Nonnull event: GenericStageInstanceUpdateEvent<*>?) {}
    fun onGenericChannel(@Nonnull event: GenericChannelEvent?) {}
    fun onGenericChannelUpdate(@Nonnull event: GenericChannelUpdateEvent<*>?) {}
    fun onGenericThread(@Nonnull event: GenericThreadEvent?) {}
    fun onGenericThreadMember(@Nonnull event: GenericThreadMemberEvent?) {}
    fun onGenericGuild(@Nonnull event: GenericGuildEvent?) {}
    fun onGenericGuildUpdate(@Nonnull event: GenericGuildUpdateEvent<*>?) {}
    fun onGenericGuildInvite(@Nonnull event: GenericGuildInviteEvent?) {}
    fun onGenericGuildMember(@Nonnull event: GenericGuildMemberEvent?) {}
    fun onGenericGuildMemberUpdate(@Nonnull event: GenericGuildMemberUpdateEvent<*>?) {}
    fun onGenericGuildVoice(@Nonnull event: GenericGuildVoiceEvent?) {}
    fun onGenericAutoModRule(@Nonnull event: GenericAutoModRuleEvent?) {}
    fun onGenericRole(@Nonnull event: GenericRoleEvent?) {}
    fun onGenericRoleUpdate(@Nonnull event: GenericRoleUpdateEvent<*>?) {}
    fun onGenericEmoji(@Nonnull event: GenericEmojiEvent?) {}
    fun onGenericEmojiUpdate(@Nonnull event: GenericEmojiUpdateEvent<*>?) {}
    fun onGenericGuildSticker(@Nonnull event: GenericGuildStickerEvent?) {}
    fun onGenericGuildStickerUpdate(@Nonnull event: GenericGuildStickerUpdateEvent<*>?) {}
    fun onGenericEntitlement(@Nonnull event: GenericEntitlementEvent?) {}
    fun onGenericPermissionOverride(@Nonnull event: GenericPermissionOverrideEvent?) {}
    fun onGenericScheduledEventUpdate(@Nonnull event: GenericScheduledEventUpdateEvent<*>?) {}
    fun onGenericScheduledEventGateway(@Nonnull event: GenericScheduledEventGatewayEvent?) {}
    fun onGenericScheduledEventUser(@Nonnull event: GenericScheduledEventUserEvent?) {}
    fun onGenericForumTag(@Nonnull event: GenericForumTagEvent?) {}
    fun onGenericForumTagUpdate(@Nonnull event: GenericForumTagUpdateEvent<*>?) {}
    override fun onEvent(@Nonnull event: GenericEvent) {
        onGenericEvent(event)
        if (event is UpdateEvent<*, *>) onGenericUpdate(event)
        for (clazz in ClassWalker.range(event.javaClass, GenericEvent::class.java)) {
            if (unresolved!!.contains(clazz)) continue
            val mh = methods.computeIfAbsent(clazz) { clazz: Class<*> -> findMethod(clazz) }
            if (mh == null) {
                unresolved.add(clazz)
                continue
            }
            try {
                mh.invoke(this, event)
            } catch (throwable: Throwable) {
                if (throwable is RuntimeException) throw throwable
                if (throwable is Error) throw throwable
                throw IllegalStateException(throwable)
            }
        }
    }

    companion object {
        private val lookup = MethodHandles.lookup()
        private val methods: ConcurrentMap<Class<*>, MethodHandle?> = ConcurrentHashMap()
        private val unresolved: MutableSet<Class<*>>? = null

        init {
            unresolved = ConcurrentHashMap.newKeySet()
            Collections.addAll(
                unresolved,
                Any::class.java,  // Objects aren't events
                Event::class.java,  // onEvent is final and would never be found
                UpdateEvent::class.java,  // onGenericUpdate has already been called
                GenericEvent::class.java // onGenericEvent has already been called
            )
        }

        private fun findMethod(clazz: Class<*>): MethodHandle? {
            var name = clazz.getSimpleName()
            val type = MethodType.methodType(Void.TYPE, clazz)
            try {
                name = "on" + name.substring(0, name.length - "Event".length)
                return lookup.findVirtual(ListenerAdapter::class.java, name, type)
            } catch (ignored: NoSuchMethodException) {
            } // this means this is probably a custom event!
            catch (ignored: IllegalAccessException) {
            }
            return null
        }
    }
}
