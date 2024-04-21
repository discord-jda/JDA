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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.unions.IWebhookContainerUnion
import net.dv8tion.jda.api.managers.WebhookManager
import net.dv8tion.jda.api.requests.Request
import net.dv8tion.jda.api.requests.Response
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.internal.requests.RestActionImpl
import java.util.regex.Pattern
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * An object representing Webhooks in Discord
 *
 * @since  3.0
 *
 * @see TextChannel.retrieveWebhooks
 * @see Guild.retrieveWebhooks
 * @see JDA.retrieveWebhookById
 */
interface Webhook : ISnowflake, WebhookClient<Message?> {
    @get:Nonnull
    abstract override val jDA: JDA?

    @get:Nonnull
    val type: WebhookType?

    /**
     * Whether this webhook cannot provide [.getChannel] and [.getGuild].
     * <br></br>This means that the webhook is not local to this shard's cache and cannot provide full channel/guild references.
     *
     * @return True, if [.getChannel] and [.getGuild] would throw
     */
    @JvmField
    val isPartial: Boolean

    @JvmField
    @get:Nonnull
    val guild: Guild?

    @JvmField
    @get:Nonnull
    val channel: IWebhookContainerUnion?

    /**
     * The owner of this Webhook. This will be null for some Webhooks, such as those retrieved from Audit Logs.
     * <br></br>This requires the member to be cached. You can use [.getOwnerAsUser] to get a reference to the user instead.
     *
     * @return Possibly-null [Member][net.dv8tion.jda.api.entities.Member] instance
     * representing the owner of this Webhook.
     */
    val owner: Member?

    /**
     * The owner of this Webhook. This will be null for some Webhooks, such as those retrieved from Audit Logs.
     * <br></br>This can be non-null even when [.getOwner] is null. [.getOwner] requires the webhook to be local to this shard and in cache.
     *
     * @return Possibly-null [User][net.dv8tion.jda.api.entities.User] instance
     * representing the owner of this Webhook.
     */
    val ownerAsUser: User?

    @JvmField
    @get:Nonnull
    val defaultUser: User?

    @get:Nonnull
    val name: String?

    /**
     * The execute token for this Webhook.
     * <br></br>This can be used to modify/delete/execute
     * this Webhook.
     *
     *
     * **Note: Some Webhooks, such as those retrieved from Audit Logs, do not contain a token**
     *
     * @return The execute token for this Webhook
     */
    abstract override val token: String?

    @get:Nonnull
    val url: String?

    /**
     * The source channel for a Webhook of type [FOLLOWER][WebhookType.FOLLOWER].
     *
     * @return [ChannelReference]
     */
    val sourceChannel: ChannelReference?

    /**
     * The source guild for a Webhook of type [FOLLOWER][WebhookType.FOLLOWER].
     *
     * @return [GuildReference]
     */
    val sourceGuild: GuildReference?

    /**
     * Deletes this Webhook.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The delete was attempted after the account lost permission to view the channel.
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The delete was attempted after the account lost [Permission.MANAGE_WEBHOOKS][net.dv8tion.jda.api.Permission.MANAGE_WEBHOOKS] in
     * the channel.
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The delete was attempted after the Webhook had already been deleted.
     *
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the Webhook does not have a token, such as the Webhooks retrieved from Audit Logs and the currently
     * logged in account does not have [net.dv8tion.jda.api.Permission.MANAGE_WEBHOOKS] in this channel.
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     * <br></br>The rest action to delete this Webhook.
     */
    @Nonnull
    @CheckReturnValue
    fun delete(): AuditableRestAction<Void?>?

    /**
     * Deletes this Webhook.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The delete was attempted after the account lost permission to view the channel.
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The delete was attempted after the account lost [Permission.MANAGE_WEBHOOKS][net.dv8tion.jda.api.Permission.MANAGE_WEBHOOKS] in
     * the channel.
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The delete was attempted after the Webhook had already been deleted.
     *
     *  * [INVALID_WEBHOOK_TOKEN][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_WEBHOOK_TOKEN]
     * <br></br>If the provided webhook token is not valid.
     *
     *
     * @param  token
     * The webhook token (this is not the bot authorization token!)
     *
     * @throws IllegalArgumentException
     * If the provided token is null
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     * <br></br>The rest action to delete this Webhook.
     *
     * @since  4.0.0
     */
    @Nonnull
    @CheckReturnValue
    fun delete(@Nonnull token: String?): AuditableRestAction<Void?>?

    @JvmField
    @get:Nonnull
    val manager: WebhookManager?

    /**
     * Partial Webhook which can be [resolved][.resolve] to a [Webhook].
     *
     * @see .resolve
     */
    class WebhookReference(
        private val api: JDA, private val webhookId: Long,
        /**
         * The ID for the channel this webhook belongs to
         *
         * @return The ID for the channel this webhook belongs to
         */
        val channelIdLong: Long
    ) : ISnowflake {

        override fun getIdLong(): Long {
            return webhookId
        }

        /**
         * The ID for the channel this webhook belongs to
         *
         * @return The ID for the channel this webhook belongs to
         */
        @Nonnull
        fun getChannelId(): String {
            return java.lang.Long.toUnsignedString(channelIdLong)
        }

        /**
         * Resolves this reference to a [Webhook] instance.
         * <br></br>The resulting instance may not provide a [.getChannel] and [.getGuild] due to API limitation.
         *
         *
         * The resulting webhook can also not be executed because the API does not provide a token.
         *
         * @return [RestAction] - Type: [Webhook]
         */
        @Nonnull
        @CheckReturnValue
        fun resolve(): RestAction<Webhook?> {
            val route = Route.Webhooks.GET_WEBHOOK.compile(id)
            return RestActionImpl(
                api, route
            ) { response: Response, request: Request<Webhook?> ->
                request.jda.entityBuilder.createWebhook(
                    response.getObject(),
                    true
                )
            }
        }
    }

    /**
     * Partial Channel which references the source channel for a follower webhook.
     */
    class ChannelReference(
        private override val id: Long,
        /**
         * The source channel's name
         *
         * @return The channel name
         */
        @get:Nonnull val name: String
    ) : ISnowflake {

        override fun getIdLong(): Long {
            return id
        }
    }

    /**
     * Partial Guild which references the source guild for a follower webhook.
     */
    class GuildReference(
        private override val id: Long,
        /**
         * The source guild's name
         *
         * @return The guild name
         */
        @get:Nonnull val name: String
    ) : ISnowflake {

        override fun getIdLong(): Long {
            return id
        }
    }

    companion object {
        /**
         * Pattern for a Webhook URL.
         *
         *
         * **Groups**<br></br>
         * <table>
         * <caption style="display: none">Javadoc is stupid, this is not a required tag</caption>
         * <tr>
         * <th>Index</th>
         * <th>Name</th>
         * <th>Description</th>
        </tr> *
         * <tr>
         * <td>0</td>
         * <td>N/A</td>
         * <td>The entire link</td>
        </tr> *
         * <tr>
         * <td>1</td>
         * <td>id</td>
         * <td>The ID of the webhook</td>
        </tr> *
         * <tr>
         * <td>2</td>
         * <td>token</td>
         * <td>The token of the webhook</td>
        </tr> *
        </table> *
         *
         * You can use the names with [Matcher.group(String)][java.util.regex.Matcher.group]
         * and the index with [Matcher.group(int)][java.util.regex.Matcher.group].
         */
        val WEBHOOK_URL = Pattern.compile(
            "https?://(?:[^\\s.]+\\.)?discord(?:app)?\\.com/api(?:/v\\d+)?/webhooks/(?<id>\\d+)/(?<token>[^\\s/]+)",
            Pattern.CASE_INSENSITIVE
        )
    }
}
