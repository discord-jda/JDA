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
/**
 * Union types used for simple casting to more concrete types.
 */
package net.dv8tion.jda.api.entities.channel.unions

import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.requests.restaction.InviteAction
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.entities.Invite
import net.dv8tion.jda.api.managers.channel.attribute.ISlowmodeChannelManager
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel
import net.dv8tion.jda.api.requests.restaction.ChannelAction
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager
import net.dv8tion.jda.api.managers.channel.attribute.IPositionableChannelManager
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.api.utils.cache.ChannelCacheView
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.MediaChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.attribute.IMemberContainer
import net.dv8tion.jda.api.entities.channel.ChannelFlag
import net.dv8tion.jda.api.entities.channel.unions.IThreadContainerUnion
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion
import net.dv8tion.jda.api.entities.channel.forums.ForumTag
import net.dv8tion.jda.api.entities.ThreadMember
import net.dv8tion.jda.api.requests.restaction.CacheRestAction
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadMemberPaginationAction
import java.time.OffsetDateTime
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel.AutoArchiveDuration
import net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager
import java.util.FormattableFlags
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel
import net.dv8tion.jda.api.entities.channel.attribute.IVoiceStatusChannel
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer
import net.dv8tion.jda.api.managers.channel.attribute.IPostContainerManager
import net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.requests.restaction.ForumPostAction
import net.dv8tion.jda.api.managers.channel.attribute.IPermissionContainerManager
import net.dv8tion.jda.api.entities.IPermissionHolder
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction
import net.dv8tion.jda.api.entities.StageInstance
import net.dv8tion.jda.api.requests.restaction.StageInstanceAction
import java.util.EnumSet
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.internal.requests.restaction.StageInstanceActionImpl
import net.dv8tion.jda.api.managers.channel.concrete.StageChannelManager
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake
import net.dv8tion.jda.internal.entities.ForumTagSnowflakeImpl
import net.dv8tion.jda.api.entities.channel.attribute.ICopyableChannel
import net.dv8tion.jda.api.entities.channel.attribute.IPositionableChannel
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel
import net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction
import net.dv8tion.jda.api.managers.channel.concrete.CategoryManager
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer
import net.dv8tion.jda.api.managers.channel.concrete.ForumChannelManager
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.entities.channel.forums.BaseForumTag
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.requests.restaction.ThreadChannelAction
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadChannelPaginationAction
import net.dv8tion.jda.api.managers.channel.concrete.MediaChannelManager
import net.dv8tion.jda.api.managers.channel.attribute.ICategorizableChannelManager
import net.dv8tion.jda.api.entities.Webhook.WebhookReference
import net.dv8tion.jda.api.requests.Route.CompiledRoute
import net.dv8tion.jda.internal.requests.RestActionImpl
import java.util.function.BiFunction
import net.dv8tion.jda.api.managers.channel.concrete.NewsChannelManager
import net.dv8tion.jda.api.entities.Webhook
import net.dv8tion.jda.api.requests.restaction.WebhookAction
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.managers.channel.ChannelManager
import net.dv8tion.jda.api.managers.channel.middleman.AudioChannelManager
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.internal.requests.restaction.MessageCreateActionImpl
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl
import net.dv8tion.jda.api.entities.MessageHistory
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction
import net.dv8tion.jda.internal.requests.restaction.pagination.MessagePaginationActionImpl
import net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction
import net.dv8tion.jda.internal.requests.restaction.pagination.ReactionPaginationActionImpl
import java.util.LinkedList
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.requests.restaction.MessageEditAction
import net.dv8tion.jda.internal.requests.restaction.MessageEditActionImpl
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.utils.AttachedFile
import net.dv8tion.jda.api.managers.channel.middleman.StandardGuildMessageChannelManager
import net.dv8tion.jda.api.entities.channel.attribute.IInviteContainer
import net.dv8tion.jda.api.managers.channel.middleman.StandardGuildChannelManager
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.audit.AuditLogKey
import net.dv8tion.jda.internal.utils.EntityString
import net.dv8tion.jda.api.entities.channel.concrete.GroupChannel
