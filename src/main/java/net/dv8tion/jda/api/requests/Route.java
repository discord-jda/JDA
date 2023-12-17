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

package net.dv8tion.jda.api.requests;

import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EncodingUtil;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.*;

import static net.dv8tion.jda.api.requests.Method.*;

/**
 * Routes for API endpoints.
 */
@SuppressWarnings("unused")
public class Route
{
    public static class Misc
    {
        public static final Route GET_VOICE_REGIONS = new Route(GET,  "voice/regions");
        public static final Route GATEWAY =           new Route(GET,  "gateway");
        public static final Route GATEWAY_BOT =       new Route(GET,  "gateway/bot");
    }

    public static class Applications
    {
        public static final Route GET_BOT_APPLICATION =             new Route(GET, "oauth2/applications/@me");
        public static final Route GET_ROLE_CONNECTION_METADATA =    new Route(GET, "applications/{application_id}/role-connections/metadata");
        public static final Route UPDATE_ROLE_CONNECTION_METADATA = new Route(PUT, "applications/{application_id}/role-connections/metadata");
    }

    public static class Interactions
    {
        public static final Route GET_COMMANDS =    new Route(GET,     "applications/{application_id}/commands");
        public static final Route GET_COMMAND =     new Route(GET,     "applications/{application_id}/commands/{command_id}");
        public static final Route CREATE_COMMAND =  new Route(POST,    "applications/{application_id}/commands");
        public static final Route UPDATE_COMMANDS = new Route(PUT,     "applications/{application_id}/commands");
        public static final Route EDIT_COMMAND =    new Route(PATCH,   "applications/{application_id}/commands/{command_id}");
        public static final Route DELETE_COMMAND =  new Route(DELETE,  "applications/{application_id}/commands/{command_id}");

        public static final Route GET_GUILD_COMMANDS =    new Route(GET,     "applications/{application_id}/guilds/{guild_id}/commands");
        public static final Route GET_GUILD_COMMAND =     new Route(GET,     "applications/{application_id}/guilds/{guild_id}/commands/{command_id}");
        public static final Route CREATE_GUILD_COMMAND =  new Route(POST,    "applications/{application_id}/guilds/{guild_id}/commands");
        public static final Route UPDATE_GUILD_COMMANDS = new Route(PUT,     "applications/{application_id}/guilds/{guild_id}/commands");
        public static final Route EDIT_GUILD_COMMAND =    new Route(PATCH,   "applications/{application_id}/guilds/{guild_id}/commands/{command_id}");
        public static final Route DELETE_GUILD_COMMAND =  new Route(DELETE,  "applications/{application_id}/guilds/{guild_id}/commands/{command_id}");

        public static final Route GET_ALL_COMMAND_PERMISSIONS =  new Route(GET, "applications/{application_id}/guilds/{guild_id}/commands/permissions");
        public static final Route EDIT_ALL_COMMAND_PERMISSIONS = new Route(PUT, "applications/{application_id}/guilds/{guild_id}/commands/permissions");
        public static final Route GET_COMMAND_PERMISSIONS =      new Route(GET, "applications/{application_id}/guilds/{guild_id}/commands/{command_id}/permissions");
        public static final Route EDIT_COMMAND_PERMISSIONS =     new Route(PUT, "applications/{application_id}/guilds/{guild_id}/commands/{command_id}/permissions");

        public static final Route CALLBACK =        new Route(POST,   "interactions/{interaction_id}/{interaction_token}/callback", true);
        public static final Route CREATE_FOLLOWUP = new Route(POST,   "webhooks/{application_id}/{interaction_token}", true);
        public static final Route EDIT_FOLLOWUP =   new Route(PATCH,  "webhooks/{application_id}/{interaction_token}/messages/{message_id}", true);
        public static final Route DELETE_FOLLOWUP = new Route(DELETE, "webhooks/{application_id}/{interaction_token}/messages/{message_id}", true);
        public static final Route GET_MESSAGE =     new Route(GET,    "webhooks/{application_id}/{interaction_token}/messages/{message_id}", true);
    }

    public static class Self
    {
        public static final Route GET_SELF =               new Route(GET,    "users/@me");
        public static final Route MODIFY_SELF =            new Route(PATCH,  "users/@me");
        public static final Route GET_GUILDS  =            new Route(GET,    "users/@me/guilds");
        public static final Route LEAVE_GUILD =            new Route(DELETE, "users/@me/guilds/{guild_id}");
        public static final Route GET_PRIVATE_CHANNELS =   new Route(GET,    "users/@me/channels");
        public static final Route CREATE_PRIVATE_CHANNEL = new Route(POST,   "users/@me/channels");
    }

    public static class Users
    {
        public static final Route GET_USER    = new Route(GET, "users/{user_id}");
    }

    public static class Guilds
    {
        public static final Route GET_GUILD =          new Route(GET,    "guilds/{guild_id}");
        public static final Route MODIFY_GUILD =       new Route(PATCH,  "guilds/{guild_id}");
        public static final Route GET_VANITY_URL =     new Route(GET,    "guilds/{guild_id}/vanity-url");
        public static final Route CREATE_CHANNEL =     new Route(POST,   "guilds/{guild_id}/channels");
        public static final Route GET_CHANNELS =       new Route(GET,    "guilds/{guild_id}/channels");
        public static final Route MODIFY_CHANNELS =    new Route(PATCH,  "guilds/{guild_id}/channels");
        public static final Route MODIFY_ROLES =       new Route(PATCH,  "guilds/{guild_id}/roles");
        public static final Route GET_BANS =           new Route(GET,    "guilds/{guild_id}/bans");
        public static final Route GET_BAN =            new Route(GET,    "guilds/{guild_id}/bans/{user_id}");
        public static final Route UNBAN =              new Route(DELETE, "guilds/{guild_id}/bans/{user_id}");
        public static final Route BAN =                new Route(PUT,    "guilds/{guild_id}/bans/{user_id}");
        public static final Route KICK_MEMBER =        new Route(DELETE, "guilds/{guild_id}/members/{user_id}");
        public static final Route MODIFY_MEMBER =      new Route(PATCH,  "guilds/{guild_id}/members/{user_id}");
        public static final Route ADD_MEMBER =         new Route(PUT,    "guilds/{guild_id}/members/{user_id}");
        public static final Route GET_MEMBER =         new Route(GET,    "guilds/{guild_id}/members/{user_id}");
        public static final Route MODIFY_SELF =        new Route(PATCH,  "guilds/{guild_id}/members/@me");
        public static final Route PRUNABLE_COUNT =     new Route(GET,    "guilds/{guild_id}/prune");
        public static final Route PRUNE_MEMBERS =      new Route(POST,   "guilds/{guild_id}/prune");
        public static final Route GET_WEBHOOKS =       new Route(GET,    "guilds/{guild_id}/webhooks");
        public static final Route GET_GUILD_EMBED =    new Route(GET,    "guilds/{guild_id}/embed");
        public static final Route MODIFY_GUILD_EMBED = new Route(PATCH,  "guilds/{guild_id}/embed");
        public static final Route GET_GUILD_EMOJIS =   new Route(GET,    "guilds/{guild_id}/emojis");
        public static final Route GET_AUDIT_LOGS =     new Route(GET,    "guilds/{guild_id}/audit-logs");
        public static final Route GET_VOICE_REGIONS =  new Route(GET,    "guilds/{guild_id}/regions");
        public static final Route UPDATE_VOICE_STATE = new Route(PATCH,  "guilds/{guild_id}/voice-states/{user_id}");

        public static final Route GET_INTEGRATIONS =   new Route(GET,    "guilds/{guild_id}/integrations");
        public static final Route CREATE_INTEGRATION = new Route(POST,   "guilds/{guild_id}/integrations");
        public static final Route DELETE_INTEGRATION = new Route(DELETE, "guilds/{guild_id}/integrations/{integration_id}");
        public static final Route MODIFY_INTEGRATION = new Route(PATCH,  "guilds/{guild_id}/integrations/{integration_id}");
        public static final Route SYNC_INTEGRATION =   new Route(POST,   "guilds/{guild_id}/integrations/{integration_id}/sync");

        public static final Route ADD_MEMBER_ROLE =    new Route(PUT,    "guilds/{guild_id}/members/{user_id}/roles/{role_id}");
        public static final Route REMOVE_MEMBER_ROLE = new Route(DELETE, "guilds/{guild_id}/members/{user_id}/roles/{role_id}");

        public static final Route LIST_ACTIVE_THREADS = new Route(GET,   "guilds/{guild_id}/threads/active");

        public static final Route GET_SCHEDULED_EVENT       = new Route(GET,    "guilds/{guild_id}/scheduled-events/{scheduled_event_id}");
        public static final Route GET_SCHEDULED_EVENTS      = new Route(GET,    "guilds/{guild_id}/scheduled-events");
        public static final Route CREATE_SCHEDULED_EVENT    = new Route(POST,   "guilds/{guild_id}/scheduled-events");
        public static final Route MODIFY_SCHEDULED_EVENT    = new Route(PATCH,  "guilds/{guild_id}/scheduled-events/{scheduled_event_id}");
        public static final Route DELETE_SCHEDULED_EVENT    = new Route(DELETE, "guilds/{guild_id}/scheduled-events/{scheduled_event_id}");
        public static final Route GET_SCHEDULED_EVENT_USERS = new Route(GET,    "guilds/{guild_id}/scheduled-events/{scheduled_event_id}/users");

        public static final Route GET_WELCOME_SCREEN    = new Route(GET,   "guilds/{guild_id}/welcome-screen");
        public static final Route MODIFY_WELCOME_SCREEN = new Route(PATCH, "guilds/{guild_id}/welcome-screen");

        public static final Route CREATE_GUILD = new Route(POST, "guilds");
        public static final Route DELETE_GUILD = new Route(POST, "guilds/{guild_id}/delete");
    }

    public static class Emojis
    {
        // These are all client endpoints and thus don't need defined major parameters
        public static final Route MODIFY_EMOJI = new Route(PATCH,  "guilds/{guild_id}/emojis/{emoji_id}");
        public static final Route DELETE_EMOJI = new Route(DELETE, "guilds/{guild_id}/emojis/{emoji_id}");
        public static final Route CREATE_EMOJI = new Route(POST,   "guilds/{guild_id}/emojis");

        public static final Route GET_EMOJIS = new Route(GET, "guilds/{guild_id}/emojis");
        public static final Route GET_EMOJI = new Route(GET,    "guilds/{guild_id}/emojis/{emoji_id}");
    }

    public static class Stickers
    {
        public static final Route GET_GUILD_STICKERS = new Route(GET,    "guilds/{guild_id}/stickers");
        public static final Route GET_GUILD_STICKER =  new Route(GET,    "guilds/{guild_id}/stickers/{sticker_id}");

        public static final Route MODIFY_GUILD_STICKER = new Route(PATCH,  "guilds/{guild_id}/stickers/{sticker_id}");
        public static final Route DELETE_GUILD_STICKER = new Route(DELETE, "guilds/{guild_id}/stickers/{sticker_id}");
        public static final Route CREATE_GUILD_STICKER = new Route(POST,   "guilds/{guild_id}/stickers");

        public static final Route GET_STICKER = new Route(GET, "stickers/{sticker_id}");
        public static final Route LIST_PACKS  = new Route(GET, "sticker-packs");
    }

    public static class Webhooks
    {
        public static final Route GET_WEBHOOK          = new Route(GET,    "webhooks/{webhook_id}");
        public static final Route GET_TOKEN_WEBHOOK    = new Route(GET,    "webhooks/{webhook_id}/{token}");
        public static final Route DELETE_WEBHOOK       = new Route(DELETE, "webhooks/{webhook_id}");
        public static final Route DELETE_TOKEN_WEBHOOK = new Route(DELETE, "webhooks/{webhook_id}/{token}");
        public static final Route MODIFY_WEBHOOK       = new Route(PATCH,  "webhooks/{webhook_id}");
        public static final Route MODIFY_TOKEN_WEBHOOK = new Route(PATCH,  "webhooks/{webhook_id}/{token}");

        public static final Route EXECUTE_WEBHOOK        = new Route(POST,   "webhooks/{webhook_id}/{token}");
        public static final Route EXECUTE_WEBHOOK_FETCH  = new Route(GET,    "webhooks/{webhook_id}/{token}/messages/{message_id}");
        public static final Route EXECUTE_WEBHOOK_EDIT   = new Route(PATCH,  "webhooks/{webhook_id}/{token}/messages/{message_id}");
        public static final Route EXECUTE_WEBHOOK_DELETE = new Route(DELETE, "webhooks/{webhook_id}/{token}/messages/{message_id}");
        public static final Route EXECUTE_WEBHOOK_SLACK  = new Route(POST,   "webhooks/{webhook_id}/{token}/slack");
        public static final Route EXECUTE_WEBHOOK_GITHUB = new Route(POST,   "webhooks/{webhook_id}/{token}/github");
    }

    public static class Roles
    {
        public static final Route GET_ROLES =   new Route(GET,    "guilds/{guild_id}/roles");
        public static final Route CREATE_ROLE = new Route(POST,   "guilds/{guild_id}/roles");
        public static final Route GET_ROLE =    new Route(GET,    "guilds/{guild_id}/roles/{role_id}");
        public static final Route MODIFY_ROLE = new Route(PATCH,  "guilds/{guild_id}/roles/{role_id}");
        public static final Route DELETE_ROLE = new Route(DELETE, "guilds/{guild_id}/roles/{role_id}");
    }

    public static class Channels
    {
        public static final Route DELETE_CHANNEL =       new Route(DELETE, "channels/{channel_id}");
        public static final Route MODIFY_CHANNEL =       new Route(PATCH,  "channels/{channel_id}");
        public static final Route GET_CHANNEL =          new Route(GET,    "channels/{channel_id}");
        public static final Route GET_WEBHOOKS =         new Route(GET,    "channels/{channel_id}/webhooks");
        public static final Route CREATE_WEBHOOK =       new Route(POST,   "channels/{channel_id}/webhooks");
        public static final Route CREATE_PERM_OVERRIDE = new Route(PUT,    "channels/{channel_id}/permissions/{permoverride_id}");
        public static final Route MODIFY_PERM_OVERRIDE = new Route(PUT,    "channels/{channel_id}/permissions/{permoverride_id}");
        public static final Route DELETE_PERM_OVERRIDE = new Route(DELETE, "channels/{channel_id}/permissions/{permoverride_id}");
        public static final Route SET_STATUS =           new Route(PUT,    "channels/{channel_id}/voice-status");

        public static final Route SEND_TYPING =          new Route(POST,   "channels/{channel_id}/typing");
        public static final Route GET_PERMISSIONS =      new Route(GET,    "channels/{channel_id}/permissions");
        public static final Route GET_PERM_OVERRIDE =    new Route(GET,    "channels/{channel_id}/permissions/{permoverride_id}");
        public static final Route FOLLOW_CHANNEL =       new Route(POST,   "channels/{channel_id}/followers");

        public static final Route CREATE_THREAD_FROM_MESSAGE =              new Route(POST,     "channels/{channel_id}/messages/{message_id}/threads");
        public static final Route CREATE_THREAD =                           new Route(POST,     "channels/{channel_id}/threads");
        public static final Route JOIN_THREAD =                             new Route(PUT,      "channels/{channel_id}/thread-members/@me");
        public static final Route ADD_THREAD_MEMBER =                       new Route(PUT,      "channels/{channel_id}/thread-members/{user_id}");
        public static final Route LEAVE_THREAD =                            new Route(DELETE,   "channels/{channel_id}/thread-members/@me");
        public static final Route REMOVE_THREAD_MEMBER =                    new Route(DELETE,   "channels/{channel_id}/thread-members/{user_id}");
        public static final Route GET_THREAD_MEMBER =                       new Route(GET,      "channels/{channel_id}/thread-members/{user_id}");
        public static final Route LIST_THREAD_MEMBERS =                     new Route(GET,      "channels/{channel_id}/thread-members");
        public static final Route LIST_PUBLIC_ARCHIVED_THREADS =            new Route(GET,      "channels/{channel_id}/threads/archived/public");
        public static final Route LIST_PRIVATE_ARCHIVED_THREADS =           new Route(GET,      "channels/{channel_id}/threads/archived/private");
        public static final Route LIST_JOINED_PRIVATE_ARCHIVED_THREADS =    new Route(GET,      "channels/{channel_id}/users/@me/threads/archived/private");
    }

    public static class StageInstances
    {
        public static final Route GET_INSTANCE =    new Route(GET,    "stage-instances/{channel_id}");
        public static final Route DELETE_INSTANCE = new Route(DELETE, "stage-instances/{channel_id}");
        public static final Route UPDATE_INSTANCE = new Route(PATCH,  "stage-instances/{channel_id}");
        public static final Route CREATE_INSTANCE = new Route(POST,   "stage-instances");
    }

    public static class AutoModeration
    {
        public static final Route LIST_RULES =  new Route(GET,    "guilds/{guild_id}/auto-moderation/rules");
        public static final Route GET_RULE =    new Route(GET,    "guilds/{guild_id}/auto-moderation/rules/{rule_id}");
        public static final Route CREATE_RULE = new Route(POST,   "guilds/{guild_id}/auto-moderation/rules");
        public static final Route UPDATE_RULE = new Route(PATCH,  "guilds/{guild_id}/auto-moderation/rules/{rule_id}");
        public static final Route DELETE_RULE = new Route(DELETE, "guilds/{guild_id}/auto-moderation/rules/{rule_id}");
    }

    public static class Messages
    {
        public static final Route EDIT_MESSAGE =          new Route(PATCH,  "channels/{channel_id}/messages/{message_id}"); // requires special handling, same bucket but different endpoints
        public static final Route SEND_MESSAGE =          new Route(POST,   "channels/{channel_id}/messages");
        public static final Route GET_PINNED_MESSAGES =   new Route(GET,    "channels/{channel_id}/pins");
        public static final Route ADD_PINNED_MESSAGE =    new Route(PUT,    "channels/{channel_id}/pins/{message_id}");
        public static final Route REMOVE_PINNED_MESSAGE = new Route(DELETE, "channels/{channel_id}/pins/{message_id}");

        public static final Route ADD_REACTION =          new Route(PUT,    "channels/{channel_id}/messages/{message_id}/reactions/{reaction_code}/{user_id}");
        public static final Route REMOVE_REACTION =       new Route(DELETE, "channels/{channel_id}/messages/{message_id}/reactions/{reaction_code}/{user_id}");
        public static final Route REMOVE_ALL_REACTIONS =  new Route(DELETE, "channels/{channel_id}/messages/{message_id}/reactions");
        public static final Route GET_REACTION_USERS =    new Route(GET,    "channels/{channel_id}/messages/{message_id}/reactions/{reaction_code}");
        public static final Route CLEAR_EMOJI_REACTIONS = new Route(DELETE, "channels/{channel_id}/messages/{message_id}/reactions/{reaction_code}");

        public static final Route DELETE_MESSAGE =      new Route(DELETE, "channels/{channel_id}/messages/{message_id}");
        public static final Route GET_MESSAGE_HISTORY = new Route(GET,    "channels/{channel_id}/messages");
        public static final Route CROSSPOST_MESSAGE =   new Route(POST,   "channels/{channel_id}/messages/{message_id}/crosspost");

        public static final Route GET_MESSAGE =     new Route(GET,  "channels/{channel_id}/messages/{message_id}");
        public static final Route DELETE_MESSAGES = new Route(POST, "channels/{channel_id}/messages/bulk-delete");
    }

    public static class Invites
    {
        public static final Route GET_INVITE =          new Route(GET,    "invites/{code}");
        public static final Route GET_GUILD_INVITES =   new Route(GET,    "guilds/{guild_id}/invites");
        public static final Route GET_CHANNEL_INVITES = new Route(GET,    "channels/{channel_id}/invites");
        public static final Route CREATE_INVITE =       new Route(POST,   "channels/{channel_id}/invites");
        public static final Route DELETE_INVITE =       new Route(DELETE, "invites/{code}");
    }

    public static class Templates
    {
        public static final Route GET_TEMPLATE =               new Route(GET,    "guilds/templates/{code}");
        public static final Route SYNC_TEMPLATE =              new Route(PUT,    "guilds/{guild_id}/templates/{code}");
        public static final Route CREATE_TEMPLATE =            new Route(POST,   "guilds/{guild_id}/templates");
        public static final Route MODIFY_TEMPLATE =            new Route(PATCH,  "guilds/{guild_id}/templates/{code}");
        public static final Route DELETE_TEMPLATE =            new Route(DELETE, "guilds/{guild_id}/templates/{code}");
        public static final Route GET_GUILD_TEMPLATES =        new Route(GET,    "guilds/{guild_id}/templates");
        public static final Route CREATE_GUILD_FROM_TEMPLATE = new Route(POST,   "guilds/templates/{code}");
    }

    /**
     * Create a route template for the given HTTP method.
     *
     * <p>Route syntax should include valid argument placeholders of the format: {@code '{' argument_name '}'}
     * <br>The rate-limit handling in JDA relies on the correct names of major parameters:
     * <ul>
     *     <li>{@code channel_id} for channel routes</li>
     *     <li>{@code guild_id} for guild routes</li>
     *     <li>{@code webhook_id} for webhook routes</li>
     *     <li>{@code interaction_token} for interaction routes</li>
     * </ul>
     *
     * For example, to compose the route to create a message in a channel:
     * <pre>{@code
     * Route route = Route.custom(Method.POST, "channels/{channel_id}/messages");
     * }</pre>
     *
     * <p>To compile the route, use {@link #compile(String...)} with the positional arguments.
     * <pre>{@code
     * Route.CompiledRoute compiled = route.compile(channelId);
     * }</pre>
     *
     * @param  method
     *         The HTTP method
     * @param  route
     *         The route template with valid argument placeholders
     *
     * @throws IllegalArgumentException
     *         If null is provided or the route is invalid (containing spaces or empty)
     *
     * @return The custom route template
     */
    @Nonnull
    public static Route custom(@Nonnull Method method, @Nonnull String route)
    {
        Checks.notNull(method, "Method");
        Checks.notEmpty(route, "Route");
        Checks.noWhitespace(route, "Route");
        return new Route(method, route);
    }

    /**
     * Create a route template for the with the {@link Method#DELETE DELETE} method.
     *
     * <p>Route syntax should include valid argument placeholders of the format: {@code '{' argument_name '}'}
     * <br>The rate-limit handling in JDA relies on the correct names of major parameters:
     * <ul>
     *     <li>{@code channel_id} for channel routes</li>
     *     <li>{@code guild_id} for guild routes</li>
     *     <li>{@code webhook_id} for webhook routes</li>
     *     <li>{@code interaction_token} for interaction routes</li>
     * </ul>
     *
     * For example, to compose the route to delete a message in a channel:
     * <pre>{@code
     * Route route = Route.custom(Method.DELETE, "channels/{channel_id}/messages/{message_id}");
     * }</pre>
     *
     * <p>To compile the route, use {@link #compile(String...)} with the positional arguments.
     * <pre>{@code
     * Route.CompiledRoute compiled = route.compile(channelId, messageId);
     * }</pre>
     *
     * @param  route
     *         The route template with valid argument placeholders
     *
     * @throws IllegalArgumentException
     *         If null is provided or the route is invalid (containing spaces or empty)
     *
     * @return The custom route template
     */
    @Nonnull
    public static Route delete(@Nonnull String route)
    {
        return custom(DELETE, route);
    }

    /**
     * Create a route template for the with the {@link Method#POST POST} method.
     *
     * <p>Route syntax should include valid argument placeholders of the format: {@code '{' argument_name '}'}
     * <br>The rate-limit handling in JDA relies on the correct names of major parameters:
     * <ul>
     *     <li>{@code channel_id} for channel routes</li>
     *     <li>{@code guild_id} for guild routes</li>
     *     <li>{@code webhook_id} for webhook routes</li>
     *     <li>{@code interaction_token} for interaction routes</li>
     * </ul>
     *
     * For example, to compose the route to create a message in a channel:
     * <pre>{@code
     * Route route = Route.custom(Method.POST, "channels/{channel_id}/messages");
     * }</pre>
     *
     * <p>To compile the route, use {@link #compile(String...)} with the positional arguments.
     * <pre>{@code
     * Route.CompiledRoute compiled = route.compile(channelId);
     * }</pre>
     *
     * @param  route
     *         The route template with valid argument placeholders
     *
     * @throws IllegalArgumentException
     *         If null is provided or the route is invalid (containing spaces or empty)
     *
     * @return The custom route template
     */
    @Nonnull
    public static Route post(@Nonnull String route)
    {
        return custom(POST, route);
    }

    /**
     * Create a route template for the with the {@link Method#PUT PUT} method.
     *
     * <p>Route syntax should include valid argument placeholders of the format: {@code '{' argument_name '}'}
     * <br>The rate-limit handling in JDA relies on the correct names of major parameters:
     * <ul>
     *     <li>{@code channel_id} for channel routes</li>
     *     <li>{@code guild_id} for guild routes</li>
     *     <li>{@code webhook_id} for webhook routes</li>
     *     <li>{@code interaction_token} for interaction routes</li>
     * </ul>
     *
     * For example, to compose the route to ban a user in a guild:
     * <pre>{@code
     * Route route = Route.custom(Method.PUT, "guilds/{guild_id}/bans/{user_id}");
     * }</pre>
     *
     * <p>To compile the route, use {@link #compile(String...)} with the positional arguments.
     * <pre>{@code
     * Route.CompiledRoute compiled = route.compile(guildId, userId);
     * }</pre>
     *
     * @param  route
     *         The route template with valid argument placeholders
     *
     * @throws IllegalArgumentException
     *         If null is provided or the route is invalid (containing spaces or empty)
     *
     * @return The custom route template
     */
    @Nonnull
    public static Route put(@Nonnull String route)
    {
        return custom(PUT, route);
    }

    /**
     * Create a route template for the with the {@link Method#PATCH PATCH} method.
     *
     * <p>Route syntax should include valid argument placeholders of the format: {@code '{' argument_name '}'}
     * <br>The rate-limit handling in JDA relies on the correct names of major parameters:
     * <ul>
     *     <li>{@code channel_id} for channel routes</li>
     *     <li>{@code guild_id} for guild routes</li>
     *     <li>{@code webhook_id} for webhook routes</li>
     *     <li>{@code interaction_token} for interaction routes</li>
     * </ul>
     *
     * For example, to compose the route to edit a message in a channel:
     * <pre>{@code
     * Route route = Route.custom(Method.PATCH, "channels/{channel_id}/messages/{message_id}");
     * }</pre>
     *
     * <p>To compile the route, use {@link #compile(String...)} with the positional arguments.
     * <pre>{@code
     * Route.CompiledRoute compiled = route.compile(channelId, messageId);
     * }</pre>
     *
     * @param  route
     *         The route template with valid argument placeholders
     *
     * @throws IllegalArgumentException
     *         If null is provided or the route is invalid (containing spaces or empty)
     *
     * @return The custom route template
     */
    @Nonnull
    public static Route patch(@Nonnull String route)
    {
        return custom(PATCH, route);
    }

    /**
     * Create a route template for the with the {@link Method#GET GET} method.
     *
     * <p>Route syntax should include valid argument placeholders of the format: {@code '{' argument_name '}'}
     * <br>The rate-limit handling in JDA relies on the correct names of major parameters:
     * <ul>
     *     <li>{@code channel_id} for channel routes</li>
     *     <li>{@code guild_id} for guild routes</li>
     *     <li>{@code webhook_id} for webhook routes</li>
     *     <li>{@code interaction_token} for interaction routes</li>
     * </ul>
     *
     * For example, to compose the route to get a message in a channel:
     * <pre>{@code
     * Route route = Route.custom(Method.GET, "channels/{channel_id}/messages/{message_id}");
     * }</pre>
     *
     * <p>To compile the route, use {@link #compile(String...)} with the positional arguments.
     * <pre>{@code
     * Route.CompiledRoute compiled = route.compile(channelId, messageId);
     * }</pre>
     *
     * @param  route
     *         The route template with valid argument placeholders
     *
     * @throws IllegalArgumentException
     *         If null is provided or the route is invalid (containing spaces or empty)
     *
     * @return The custom route template
     */
    @Nonnull
    public static Route get(@Nonnull String route)
    {
        return custom(GET, route);
    }

    /**
     * The known major parameters used for rate-limits.
     *
     * <p>Instead of {@code webhook_id + webhook_token}, we use {@code interaction_token} for interaction routes.
     *
     * @see <a href="https://discord.com/developers/docs/topics/rate-limits" target="_blank">Rate Limit Documentation</a>
     */
    public static final List<String> MAJOR_PARAMETER_NAMES = Helpers.listOf(
        "guild_id", "channel_id", "webhook_id", "interaction_token"
    );

    private final Method method;
    private final int paramCount;
    private final String[] template;
    private final boolean isInteraction;

    private Route(Method method, String route, boolean isInteraction)
    {
        this.method = method;
        this.template = Helpers.split(route, "/");
        this.isInteraction = isInteraction;

        // Validate route syntax
        int paramCount = 0;
        for (String element : this.template)
        {
            int opening = Helpers.countMatches(element, '{');
            int closing = Helpers.countMatches(element, '}');
            if (element.startsWith("{") && element.endsWith("}"))
            {
                // Ensure the brackets are only on the start and end
                // Valid: {guild_id}
                // Invalid: {guild_id}abc
                // Invalid: {{guild_id}}
                Checks.check(closing == 1 && opening == 1, "Route element has invalid syntax: '%s'", element);
                paramCount += 1;
            }
            else if (opening > 0 || closing > 0)
            {
                // Handle potential stray brackets
                // Invalid: guilds{/guild_id} -> ["guilds{", "guild_id}"]
                throw new IllegalArgumentException("Route element has invalid syntax: '" + element + "'");
            }
        }
        this.paramCount = paramCount;

    }

    private Route(Method method, String route)
    {
        this(method, route, false);
    }

    /**
     * Whether this route is a route related to interactions.
     * <br>Interactions have some special handling, since they are exempt from global rate-limits and are limited to 15 minute uptime.
     *
     * @return True, if this route is for interactions
     */
    public boolean isInteractionBucket()
    {
        return isInteraction;
    }

    /**
     * The {@link Method} of this route template.
     * <br>Multiple routes with different HTTP methods can share a rate-limit.
     *
     * @return The HTTP method
     */
    @Nonnull
    public Method getMethod()
    {
        return method;
    }

    /**
     * The route template with argument placeholders.
     *
     * @return The route template
     */
    @Nonnull
    public String getRoute()
    {
        return String.join("/", template);
    }

    /**
     * The number of parameters for this route, not including query parameters.
     *
     * @return The parameter count
     */
    public int getParamCount()
    {
        return paramCount;
    }

    /**
     * Compile the route with provided parameters.
     * <br>The number of parameters must match the number of placeholders in the route template.
     * The provided arguments are positional and will replace the placeholders of the template in order of appearance.
     *
     * <p>Use {@link CompiledRoute#withQueryParams(String...)} to add query parameters to the route.
     *
     * @param  params
     *         The parameters to compile the route with
     *
     * @throws IllegalArgumentException
     *         If the number of parameters does not match the number of placeholders, or null is provided
     *
     * @return The compiled route, ready to use for rate-limit handling
     */
    @Nonnull
    public CompiledRoute compile(@Nonnull String... params)
    {
        Checks.noneNull(params, "Arguments");
        Checks.check(
            params.length == paramCount,
            "Error Compiling Route: [%s], incorrect amount of parameters provided. Expected: %d, Provided: %d",
            this, paramCount, params.length
        );

        StringJoiner major = new StringJoiner(":").setEmptyValue("n/a");
        StringJoiner compiledRoute = new StringJoiner("/");

        int paramIndex = 0;
        for (String element : template)
        {
            if (element.charAt(0) == '{')
            {
                String name = element.substring(1, element.length() - 1);
                String value = params[paramIndex++];
                if (MAJOR_PARAMETER_NAMES.contains(name))
                {
                    if (value.length() > 30) // probably a long interaction_token, hash it to keep logs clean (not useful anyway)
                        major.add(name + "=" + Integer.toUnsignedString(value.hashCode()));
                    else
                        major.add(name + "=" + value);
                }
                compiledRoute.add(EncodingUtil.encodeUTF8(value));
            }
            else
            {
                compiledRoute.add(element);
            }
        }

        return new CompiledRoute(this, compiledRoute.toString(), major.toString());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(method, Arrays.hashCode(template));
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Route))
            return false;

        Route oRoute = (Route) o;
        return method.equals(oRoute.method) && Arrays.equals(template, oRoute.template);
    }

    @Override
    public String toString()
    {
        return method + "/" + getRoute();
    }

    /**
     * A route compiled with arguments.
     *
     * @see    Route#compile(String...)
     */
    public class CompiledRoute
    {
        private final Route baseRoute;
        private final String major;
        private final String compiledRoute;
        private final List<String> query;

        private CompiledRoute(Route baseRoute, String compiledRoute, String major)
        {
            this.baseRoute = baseRoute;
            this.compiledRoute = compiledRoute;
            this.major = major;
            this.query = null;
        }

        private CompiledRoute(CompiledRoute original, List<String> query)
        {
            this.baseRoute = original.baseRoute;
            this.compiledRoute = original.compiledRoute;
            this.major = original.major;
            this.query = query;
        }

        /**
         * Returns a copy of this CompiledRoute with the provided parameters added as query.
         * <br>This will use <a href="https://en.wikipedia.org/wiki/Percent-encoding" target="_blank">percent-encoding</a>
         * for all provided <em>values</em> but not for the keys.
         *
         * <p><b>Example Usage</b><br>
         * <pre>{@code
         * Route.CompiledRoute history = Route.GET_MESSAGE_HISTORY.compile(channelId);
         *
         * // returns a new route
         * route = history.withQueryParams(
         *   "limit", 100
         * );
         * // adds another parameter ontop of limit
         * route = route.withQueryParams(
         *   "after", messageId
         * );
         *
         * // now the route has both limit and after, you can also do this in one call:
         * route = history.withQueryParams(
         *   "limit", 100,
         *   "after", messageId
         * );
         * }</pre>
         *
         * @param  params
         *         The parameters to add as query, alternating key and value (see example)
         *
         * @throws IllegalArgumentException
         *         If the number of arguments is not even or null is provided
         *
         * @return A copy of this CompiledRoute with the provided parameters added as query
         */
        @Nonnull
        @CheckReturnValue
        public CompiledRoute withQueryParams(@Nonnull String... params)
        {
            Checks.notNull(params, "Params");
            Checks.check(params.length >= 2, "Params length must be at least 2");
            Checks.check((params.length & 1) == 0, "Params length must be a multiple of 2");

            List<String> newQuery;
            if (query == null)
            {
                newQuery = new ArrayList<>(params.length / 2);
            }
            else
            {
                newQuery = new ArrayList<>(query.size() + params.length / 2);
                newQuery.addAll(query);
            }

            // Assuming names don't need encoding
            for (int i = 0; i < params.length; i += 2)
            {
                Checks.notEmpty(params[i], "Query key [" + i/2 + "]");
                Checks.notNull(params[i + 1], "Query value [" + i/2 + "]");
                newQuery.add(params[i] + '=' + EncodingUtil.encodeUTF8(params[i + 1]));
            }

            return new CompiledRoute(this, newQuery);
        }

        /**
         * The string of major parameters used by this route.
         * <br>This is important for rate-limit handling.
         *
         * @return The string of major parameters used by this route
         */
        @Nonnull
        public String getMajorParameters()
        {
            return major;
        }

        /**
         * The compiled route string of the endpoint,
         * including all arguments and query parameters.
         *
         * @return The compiled route string of the endpoint
         */
        @Nonnull
        public String getCompiledRoute()
        {
            if (query == null)
                return compiledRoute;
            // Append query to url
            return compiledRoute + '?' + String.join("&", query);
        }

        /**
         * The route template with the original placeholders.
         *
         * @return The route template with the original placeholders
         */
        @Nonnull
        public Route getBaseRoute()
        {
            return baseRoute;
        }

        /**
         * The HTTP method.
         *
         * @return The HTTP method
         */
        @Nonnull
        public Method getMethod()
        {
            return baseRoute.method;
        }

        @Override
        public int hashCode()
        {
            return (compiledRoute + method.toString()).hashCode();
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof CompiledRoute))
                return false;

            CompiledRoute oCompiled = (CompiledRoute) o;

            return baseRoute.equals(oCompiled.getBaseRoute()) && compiledRoute.equals(oCompiled.compiledRoute);
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .setType(method)
                    .addMetadata("compiledRoute", compiledRoute)
                    .toString();
        }
    }
}
