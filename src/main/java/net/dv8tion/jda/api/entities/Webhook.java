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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.managers.WebhookManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Pattern;

/**
 * An object representing Webhooks in Discord
 *
 * @since  3.0
 *
 * @see    TextChannel#retrieveWebhooks()
 * @see    Guild#retrieveWebhooks()
 * @see    JDA#retrieveWebhookById(String)
 */
public interface Webhook extends ISnowflake
{
    /**
     * Pattern for a Webhook URL.
     *
     * <h4>Groups</h4>
     * <table>
     *   <caption style="display: none">Javadoc is stupid, this is not a required tag</caption>
     *   <tr>
     *     <th>Index</th>
     *     <th>Name</th>
     *     <th>Description</th>
     *   </tr>
     *   <tr>
     *     <td>0</td>
     *     <td>N/A</td>
     *     <td>The entire link</td>
     *   </tr>
     *   <tr>
     *     <td>1</td>
     *     <td>id</td>
     *     <td>The ID of the webhook</td>
     *   </tr>
     *   <tr>
     *     <td>2</td>
     *     <td>token</td>
     *     <td>The token of the webhook</td>
     *   </tr>
     * </table>
     *
     * You can use the names with {@link java.util.regex.Matcher#group(String) Matcher.group(String)}
     * and the index with {@link java.util.regex.Matcher#group(int) Matcher.group(int)}.
     */
    Pattern WEBHOOK_URL = Pattern.compile("https?://(?:[^\\s.]+\\.)?discord(?:app)?\\.com/api(?:/v\\d+)?/webhooks/(?<id>\\d+)/(?<token>[^\\s/]+)", Pattern.CASE_INSENSITIVE);

    /**
     * The JDA instance of this Webhook.
     *
     * @return The current JDA instance of this Webhook
     */
    @Nonnull
    JDA getJDA();

    /**
     * The {@link WebhookType} of this webhook.
     * <br>Webhooks of type {@link WebhookType#FOLLOWER} don't have a token.
     *
     * @return The {@link WebhookType}
     */
    @Nonnull
    WebhookType getType();

    /**
     * Whether this webhook cannot provide {@link #getChannel()} and {@link #getGuild()}.
     * <br>This means that the webhook is not local to this shard's cache and cannot provide full channel/guild references.
     *
     * @return True, if {@link #getChannel()} and {@link #getGuild()} would throw
     */
    boolean isPartial();

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} instance
     * for this Webhook.
     * <br>This is a shortcut for <code>{@link #getChannel()}.getGuild()</code>.
     *
     * @throws IllegalStateException
     *         If this webhooks {@link #isPartial() is partial}
     *
     * @return The current Guild of this Webhook
     */
    @Nonnull
    Guild getGuild();

    /**
     * The {@link net.dv8tion.jda.api.entities.BaseGuildMessageChannel BaseGuildMessageChannel} instance this Webhook is attached to.
     *
     * @throws IllegalStateException
     *         If this webhooks {@link #isPartial() is partial}
     *
     * @return The current TextChannel of this Webhook
     */
    @Nonnull
    //TODO-v5: might be a problem exposing the Base class here as something like Threads could get Webhook support and break our stuff..
    BaseGuildMessageChannel getChannel();

    /**
     * The owner of this Webhook. This will be null for some Webhooks, such as those retrieved from Audit Logs.
     * <br>This requires the member to be cached. You can use {@link #getOwnerAsUser()} to get a reference to the user instead.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Member Member} instance
     *         representing the owner of this Webhook.
     */
    @Nullable
    Member getOwner();

    /**
     * The owner of this Webhook. This will be null for some Webhooks, such as those retrieved from Audit Logs.
     * <br>This can be non-null even when {@link #getOwner()} is null. {@link #getOwner()} requires the webhook to be local to this shard and in cache.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.User User} instance
     *         representing the owner of this Webhook.
     */
    @Nullable
    User getOwnerAsUser();

    /**
     * The default User for this Webhook.
     *
     * <p>The {@link net.dv8tion.jda.api.entities.User User} returned is always fake and cannot be interacted with.
     * <br>This User is used for all messages posted to the Webhook route (found in {@link #getUrl()}),
     * it holds the default references for the message authors of messages by this Webhook.
     *
     * <p>When {@code POST}ing to a Webhook route the name/avatar of this default user
     * can be overridden.
     *
     * @return A fake {@link net.dv8tion.jda.api.entities.User User} instance
     *         representing the default webhook user.
     *
     * @see    <a href="https://discord.com/developers/docs/resources/webhook#execute-webhook">Execute Webhook Docs</a>
     */
    @Nonnull
    User getDefaultUser();

    /**
     * The name of this Webhook.
     * <br>This will be displayed by default as the author name
     * of every message by this Webhook.
     *
     * <p>This is a shortcut for <code>{@link #getDefaultUser()}.getName()</code>.
     *
     * @return The name of this Webhook
     */
    @Nonnull
    String getName();

    /**
     * The execute token for this Webhook.
     * <br>This can be used to modify/delete/execute
     * this Webhook.
     *
     * <p><b>Note: Some Webhooks, such as those retrieved from Audit Logs, do not contain a token</b>
     *
     * @return The execute token for this Webhook
     */
    @Nullable
    String getToken();

    /**
     * The {@code POST} route for this Webhook.
     * <br>This contains the {@link #getToken() token} and {@link #getId() id}
     * of this Webhook. Some Webhooks without tokens (such as those retrieved from Audit Logs)
     * will return a URL without a token.
     *
     * <p>The route returned by this method does not need permission checks
     * to be executed.
     * <br>It is implied that Webhook messages always have all permissions
     * including {@link net.dv8tion.jda.api.Permission#MESSAGE_MENTION_EVERYONE mentioning everyone}.
     *
     * <p>Webhook executions are limited with 5 requests per second.
     * The response contains rate limit headers that should be handled
     * by execution frameworks. (<a href="https://discord.com/developers/docs/topics/rate-limits">Learn More</a>)
     *
     * @return The execution route for this Webhook.
     */
    @Nonnull
    String getUrl();

    /**
     * The source channel for a Webhook of type {@link WebhookType#FOLLOWER FOLLOWER}.
     *
     * @return {@link ChannelReference}
     */
    @Nullable
    ChannelReference getSourceChannel();

    /**
     * The source guild for a Webhook of type {@link WebhookType#FOLLOWER FOLLOWER}.
     *
     * @return {@link GuildReference}
     */
    @Nullable
    GuildReference getSourceGuild();

    /**
     * Deletes this Webhook.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The delete was attempted after the account lost permission to view the channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The delete was attempted after the account lost {@link net.dv8tion.jda.api.Permission#MANAGE_WEBHOOKS Permission.MANAGE_WEBHOOKS} in
     *         the channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The delete was attempted after the Webhook had already been deleted.</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the Webhook does not have a token, such as the Webhooks retrieved from Audit Logs and the currently
     *         logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_WEBHOOKS} in this channel.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>The rest action to delete this Webhook.
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> delete();

    /**
     * Deletes this Webhook.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The delete was attempted after the account lost permission to view the channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The delete was attempted after the account lost {@link net.dv8tion.jda.api.Permission#MANAGE_WEBHOOKS Permission.MANAGE_WEBHOOKS} in
     *         the channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The delete was attempted after the Webhook had already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_WEBHOOK_TOKEN INVALID_WEBHOOK_TOKEN}
     *     <br>If the provided webhook token is not valid.</li>
     * </ul>
     *
     * @param  token
     *         The webhook token (this is not the bot authorization token!)
     *
     * @throws IllegalArgumentException
     *         If the provided token is null
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>The rest action to delete this Webhook.
     *
     * @since  4.0.0
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> delete(@Nonnull String token);

    /**
     * The {@link WebhookManager WebhookManager} for this Webhook.
     * <br>You can modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction.queue()}.
     *
     * <p>This is a lazy idempotent getter. The manager is retained after the first call.
     * This getter is not thread-safe and would require guards by the user.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_WEBHOOKS Permission.MANAGE_WEBHOOKS}
     *
     * @return The {@link WebhookManager WebhookManager} for this Webhook
     */
    @Nonnull
    WebhookManager getManager();

    /**
     * Partial Webhook which can be {@link #resolve() resolved} to a {@link Webhook}.
     *
     * @see #resolve()
     */
    class WebhookReference implements ISnowflake
    {
        private final JDA api;
        private final long webhookId, channelId;

        public WebhookReference(JDA api, long webhookId, long channelId)
        {
            this.api = api;
            this.webhookId = webhookId;
            this.channelId = channelId;
        }

        @Override
        public long getIdLong()
        {
            return webhookId;
        }

        /**
         * The ID for the channel this webhook belongs to
         *
         * @return The ID for the channel this webhook belongs to
         */
        @Nonnull
        public String getChannelId()
        {
            return Long.toUnsignedString(channelId);
        }

        /**
         * The ID for the channel this webhook belongs to
         *
         * @return The ID for the channel this webhook belongs to
         */
        public long getChannelIdLong()
        {
            return channelId;
        }

        /**
         * Resolves this reference to a {@link Webhook} instance.
         * <br>The resulting instance may not provide a {@link #getChannel()} and {@link #getGuild()} due to API limitation.
         *
         * <p>The resulting webhook can also not be executed because the API does not provide a token.
         *
         * @return {@link RestAction} - Type: {@link Webhook}
         */
        @Nonnull
        @CheckReturnValue
        public RestAction<Webhook> resolve()
        {
            Route.CompiledRoute route = Route.Webhooks.GET_WEBHOOK.compile(getId());
            return new RestActionImpl<>(api, route,
                (response, request) -> request.getJDA().getEntityBuilder().createWebhook(response.getObject(), true));
        }
    }

    /**
     * Partial Channel which references the source channel for a follower webhook.
     */
    class ChannelReference implements ISnowflake
    {
        private final long id;
        private final String name;

        public ChannelReference(long id, String name)
        {
            this.id = id;
            this.name = name;
        }

        @Override
        public long getIdLong()
        {
            return id;
        }

        /**
         * The source channel's name
         *
         * @return The channel name
         */
        @Nonnull
        public String getName()
        {
            return name;
        }
    }

    /**
     * Partial Guild which references the source guild for a follower webhook.
     */
    class GuildReference implements ISnowflake
    {
        private final long id;
        private final String name;

        public GuildReference(long id, String name)
        {
            this.id = id;
            this.name = name;
        }

        @Override
        public long getIdLong()
        {
            return id;
        }

        /**
         * The source guild's name
         *
         * @return The guild name
         */
        @Nonnull
        public String getName()
        {
            return name;
        }
    }
}
