/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.managers.WebhookManager;
import net.dv8tion.jda.core.managers.WebhookManagerUpdatable;
import net.dv8tion.jda.core.requests.RestAction;

/**
 * An object representing Webhooks in Discord
 *
 * @since  3.0
 * @author Florian Spieß
 */
public interface Webhook extends ISnowflake
{

    /**
     * The JDA instance of this Webhook.
     *
     * @return The current JDA instance of this Webhook
     */
    JDA getJDA();

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} instance
     * for this Webhook.
     * <br>This is a shortcut for <code>{@link #getChannel()}.getGuild()</code>.
     *
     * @return The current Guild of this Webhook
     */
    Guild getGuild();

    /**
     * The {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} instance
     * this Webhook is attached to.
     *
     * @return The current TextChannel of this Webhook
     */
    TextChannel getChannel();

    /**
     * The owner of this Webhook.
     *
     * @return A {@link net.dv8tion.jda.core.entities.Member Member} instance
     *         representing the owner of this Webhook
     */
    Member getOwner();

    /**
     * The default User for this Webhook.
     *
     * <p>The {@link net.dv8tion.jda.core.entities.User User} returned is always {@code fake}.
     * <br>This User is used for all messages posted to the Webhook route (found in {@link #getUrl()}),
     * it holds the default references for the message authors of messages by this Webhook.
     *
     * <p>When {@code POST}ing to a Webhook route the name/avatar of this default user
     * can be overridden.
     *
     * @return A fake {@link net.dv8tion.jda.core.entities.User User} instance
     *         representing the default webhook user.
     *
     * @see    <a href="https://discordapp.com/developers/docs/resources/webhook#execute-webhook">Execute Webhook Docs</a>
     */
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
    String getName();

    /**
     * The execute token for this Webhook.
     * <br>This can be used to modify/delete/execute
     * this Webhook.
     *
     * @return The execute token for this Webhook
     */
    String getToken();

    /**
     * The {@code POST} route for this Webhook.
     * <br>This contains the {@link #getToken() token} and {@link #getId() id}
     * of this Webhook.
     *
     * <p>The route returned by this method does not need permission checks
     * to be executed.
     * <br>It is implied that Webhook messages always have all permissions
     * including {@link net.dv8tion.jda.core.Permission#MESSAGE_MENTION_EVERYONE mentioning everyone}.
     *
     * <p>Webhook executions are limited with 5 requests per second.
     * The response contains rate limit headers that should be handled
     * by execution frameworks. (<a href="https://discordapp.com/developers/docs/topics/rate-limits">Learn More</a>)
     *
     * @return The execution route for this Webhook.
     */
    String getUrl();

    /**
     * Deletes this Webhook.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction} - Type: Void
     *         <br>The rest action to delete this Webhook.
     */
    RestAction<Void> delete();

    /**
     * The {@link net.dv8tion.jda.core.managers.WebhookManager Manager}
     * for this Webhook.
     * <br>This Manager <b>does not</b> require to update, it provides set methods
     * to atomically modify fields of this Webhook.
     *
     * @return An instance of {@link net.dv8tion.jda.core.managers.WebhookManager WebhookManager}
     *         for this Webhook
     */
    WebhookManager getManager();

    /**
     * The {@link net.dv8tion.jda.core.managers.WebhookManager Manager}
     * for this Webhook.
     * <br>This Manager <b>does</b> require to update, it provides get methods
     * to retrieve {@link net.dv8tion.jda.core.managers.fields.WebhookField fields} of this Webhook which can be modified and updated.
     *
     * @return An instance of {@link net.dv8tion.jda.core.managers.WebhookManagerUpdatable WebhookManagerUpdatable}
     *         for this Webhook
     */
    WebhookManagerUpdatable getManagerUpdatable();
}
