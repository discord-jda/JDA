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
package net.dv8tion.jda.internal.entities.channel.concrete

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Webhook.WebhookReference
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion
import net.dv8tion.jda.api.managers.channel.concrete.NewsChannelManager
import net.dv8tion.jda.api.requests.Request
import net.dv8tion.jda.api.requests.Response
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.api.requests.restaction.ChannelAction
import net.dv8tion.jda.api.requests.restaction.StageInstanceAction.setTopic
import net.dv8tion.jda.api.utils.data.DataObject.Companion.empty
import net.dv8tion.jda.internal.entities.GuildImpl
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractStandardGuildMessageChannelImpl
import net.dv8tion.jda.internal.managers.channel.concrete.NewsChannelManagerImpl
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.Helpers
import java.util.function.Predicate
import javax.annotation.Nonnull

class NewsChannelImpl(id: Long, guild: GuildImpl?) :
    AbstractStandardGuildMessageChannelImpl<NewsChannelImpl?>(id, guild), NewsChannel, DefaultGuildChannelUnion {
    @get:Nonnull
    override val type: ChannelType
        get() = ChannelType.NEWS

    @get:Nonnull
    override val members: List<Member>
        get() = getGuild().getMembersView().stream()
            .filter(Predicate<Member> { m: Member -> m.hasPermission(this, Permission.VIEW_CHANNEL) })
            .collect<List<Member>, Any>(Helpers.toUnmodifiableList<Member>())

    @Nonnull
    override fun follow(@Nonnull targetChannelId: String?): RestAction<WebhookReference?>? {
        Checks.notNull(targetChannelId, "Target Channel ID")
        val route = Route.Channels.FOLLOW_CHANNEL.compile(id)
        val body = empty().put("webhook_channel_id", targetChannelId)
        return RestActionImpl(jda, route, body) { response: Response, request: Request<WebhookReference?> ->
            val json = response.`object`
            WebhookReference(request.jDA, json!!.getUnsignedLong("webhook_id"), json.getUnsignedLong("channel_id"))
        }
    }

    @Nonnull
    override fun createCopy(@Nonnull guild: Guild?): ChannelAction<NewsChannel?>? {
        Checks.notNull(guild, "Guild")
        val action: ChannelAction<NewsChannel?> = guild.createNewsChannel(name).setNSFW(nsfw).setTopic(topic)
        if (guild == getGuild()) {
            val parent = parentCategory
            if (parent != null) action.setParent(parent)
            for (o in overrides.valueCollection()) {
                if (o.isMemberOverride) action.addMemberPermissionOverride(
                    o.idLong,
                    o.allowedRaw,
                    o.deniedRaw
                ) else action.addRolePermissionOverride(o.idLong, o.allowedRaw, o.deniedRaw)
            }
        }
        return action
    }

    @get:Nonnull
    override val manager: NewsChannelManager
        get() = NewsChannelManagerImpl(this)
}
