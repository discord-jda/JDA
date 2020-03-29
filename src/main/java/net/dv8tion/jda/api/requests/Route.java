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

package net.dv8tion.jda.api.requests;

import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import static net.dv8tion.jda.api.requests.Method.*;

/**
 * Assortment of documented routes for use with {@link RestAction}
 * <br>You must use {@link #compile(String...)} to get a compiled route for REST requests.
 *
 * <p>If you decide to make a custom route with {@link #custom(Method, String)} you should use this convention:
 * {@code "channel/{channel_id}/messages/{message_id}"}
 *
 * <br>This is important because the rate limits highly depend on the configuration used to define the major parameters.
 * If one of the major parameters is used such as {@code channel_id/guild_id/webhook_id} it must be exactly this name
 * that is used to represent them in your route template. The parameter values must then be placed in the {@link #compile(String...)}
 * call. If the route already exists, it is desirable to re-use it for more efficient rate limit handling.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Route template = Route.post("channels/{channel_id}/messages"); // better to use Route.Message.SEND_MESSAGE
 * Route.CompiledRoute route = template.compile(channelId);
 * }</pre>
 */
@SuppressWarnings("unused")
public class Route
{
    public static class Misc
    {
        public static final Route TRACK =             new Route(POST, "track");
        public static final Route GET_VOICE_REGIONS = new Route(GET,  "voice/regions");
        public static final Route GATEWAY =           new Route(GET,  "gateway");
        public static final Route GATEWAY_BOT =       new Route(GET,  "gateway/bot");
    }

    public static class Applications
    {
        // Bot only
        public static final Route GET_BOT_APPLICATION =           new Route(GET,    "oauth2/applications/@me");

        // Client only
        public static final Route GET_APPLICATIONS =              new Route(GET,    "oauth2/applications");
        public static final Route CREATE_APPLICATION =            new Route(POST,   "oauth2/applications");
        public static final Route GET_APPLICATION =               new Route(GET,    "oauth2/applications/{application_id}");
        public static final Route MODIFY_APPLICATION =            new Route(PUT,    "oauth2/applications/{application_id}");
        public static final Route DELETE_APPLICATION =            new Route(DELETE, "oauth2/applications/{application_id}");

        public static final Route CREATE_BOT =                    new Route(POST,   "oauth2/applications/{application_id}/bot");

        public static final Route RESET_APPLICATION_SECRET =      new Route(POST,   "oauth2/applications/{application_id}/reset");
        public static final Route RESET_BOT_TOKEN =               new Route(POST,   "oauth2/applications/{application_id}/bot/reset");

        public static final Route GET_AUTHORIZED_APPLICATIONS =   new Route(GET,    "oauth2/tokens");
        public static final Route GET_AUTHORIZED_APPLICATION =    new Route(GET,    "oauth2/tokens/{auth_id}");
        public static final Route DELETE_AUTHORIZED_APPLICATION = new Route(DELETE, "oauth2/tokens/{auth_id}");
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
        public static final Route MODIFY_SELF_NICK =   new Route(PATCH,  "guilds/{guild_id}/members/@me/nick");
        public static final Route PRUNABLE_COUNT =     new Route(GET,    "guilds/{guild_id}/prune");
        public static final Route PRUNE_MEMBERS =      new Route(POST,   "guilds/{guild_id}/prune");
        public static final Route GET_WEBHOOKS =       new Route(GET,    "guilds/{guild_id}/webhooks");
        public static final Route GET_GUILD_EMBED =    new Route(GET,    "guilds/{guild_id}/embed");
        public static final Route MODIFY_GUILD_EMBED = new Route(PATCH,  "guilds/{guild_id}/embed");
        public static final Route GET_GUILD_EMOTES =   new Route(GET,    "guilds/{guild_id}/emojis");
        public static final Route GET_AUDIT_LOGS =     new Route(GET,    "guilds/{guild_id}/audit-logs");
        public static final Route GET_VOICE_REGIONS =  new Route(GET,    "guilds/{guild_id}/regions");

        public static final Route GET_INTEGRATIONS =   new Route(GET,    "guilds/{guild_id}/integrations");
        public static final Route CREATE_INTEGRATION = new Route(POST,   "guilds/{guild_id}/integrations");
        public static final Route DELETE_INTEGRATION = new Route(DELETE, "guilds/{guild_id}/integrations/{integration_id}");
        public static final Route MODIFY_INTEGRATION = new Route(PATCH,  "guilds/{guild_id}/integrations/{integration_id}");
        public static final Route SYNC_INTEGRATION =   new Route(POST,   "guilds/{guild_id}/integrations/{integration_id}/sync");

        public static final Route ADD_MEMBER_ROLE =    new Route(PUT,    "guilds/{guild_id}/members/{user_id}/roles/{role_id}");
        public static final Route REMOVE_MEMBER_ROLE = new Route(DELETE, "guilds/{guild_id}/members/{user_id}/roles/{role_id}");
        public static final Route CREATE_GUILD = new Route(POST,   "guilds");
        public static final Route DELETE_GUILD = new Route(DELETE, "guilds/{guild_id}/delete");
    }

    public static class Emotes
    {
        // These are all client endpoints and thus don't need defined major parameters
        public static final Route MODIFY_EMOTE = new Route(PATCH,  "guilds/{guild_id}/emojis/{emote_id}");
        public static final Route DELETE_EMOTE = new Route(DELETE, "guilds/{guild_id}/emojis/{emote_id}");
        public static final Route CREATE_EMOTE = new Route(POST,   "guilds/{guild_id}/emojis");

        public static final Route GET_EMOTES   = new Route(GET,    "guilds/{guild_id}/emojis");
        public static final Route GET_EMOTE    = new Route(GET,    "guilds/{guild_id}/emojis/{emoji_id}");
    }

    public static class Webhooks
    {
        public static final Route GET_WEBHOOK          = new Route(GET,    "webhooks/{webhook_id}");
        public static final Route GET_TOKEN_WEBHOOK    = new Route(GET,    "webhooks/{webhook_id}/{token}");
        public static final Route DELETE_WEBHOOK       = new Route(DELETE, "webhooks/{webhook_id}");
        public static final Route DELETE_TOKEN_WEBHOOK = new Route(DELETE, "webhooks/{webhook_id}/{token}");
        public static final Route MODIFY_WEBHOOK       = new Route(PATCH,  "webhooks/{webhook_id}");
        public static final Route MODIFY_TOKEN_WEBHOOK = new Route(PATCH,  "webhooks/{webhook_id}/{token}");

        // Separate
        public static final Route EXECUTE_WEBHOOK        = new Route(POST, "webhooks/{webhook_id}/{token}");
        public static final Route EXECUTE_WEBHOOK_SLACK  = new Route(POST, "webhooks/{webhook_id}/{token}/slack");
        public static final Route EXECUTE_WEBHOOK_GITHUB = new Route(POST, "webhooks/{webhook_id}/{token}/github");
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
        public static final Route GET_WEBHOOKS =         new Route(GET,    "channels/{channel_id}/webhooks");
        public static final Route CREATE_WEBHOOK =       new Route(POST,   "channels/{channel_id}/webhooks");
        public static final Route CREATE_PERM_OVERRIDE = new Route(PUT,    "channels/{channel_id}/permissions/{permoverride_id}");
        public static final Route MODIFY_PERM_OVERRIDE = new Route(PUT,    "channels/{channel_id}/permissions/{permoverride_id}");
        public static final Route DELETE_PERM_OVERRIDE = new Route(DELETE, "channels/{channel_id}/permissions/{permoverride_id}");

        public static final Route SEND_TYPING =          new Route(POST,   "channels/{channel_id}/typing");
        public static final Route GET_PERMISSIONS =      new Route(GET,    "channels/{channel_id}/permissions");
        public static final Route GET_PERM_OVERRIDE =    new Route(GET,    "channels/{channel_id}/permissions/{permoverride_id}");
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
        public static final Route CLEAR_EMOTE_REACTIONS = new Route(DELETE, "channels/{channel_id}/messages/{message_id}/reactions/{reaction_code}");

        public static final Route DELETE_MESSAGE =      new Route(DELETE, "channels/{channel_id}/messages/{message_id}");
        public static final Route GET_MESSAGE_HISTORY = new Route(GET,    "channels/{channel_id}/messages");

        //Bot only
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

    /**
     * Constructs a custom template route with the defined method and path.
     *
     * <p>You should use this convention: {@code "channel/{channel_id}/messages/{message_id}"} for the path.
     * <br>This is important because the rate limits highly depend on the configuration used to define the major parameters.
     * If one of the major parameters is used such as {@code channel_id/guild_id/webhook_id} it must be exactly this name
     * that is used to represent them in your route template. The parameter values must then be placed in the {@link #compile(String...)}
     * call. If the route already exists, it is desirable to re-use it for more efficient rate limit handling.
     *
     * @param  method
     *         The HTTP method
     * @param  path
     *         The target path
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the method is null</li>
     *             <li>If the route is null, empty, or includes whitespace</li>
     *             <li>If the path format has an non-symmetric placement of {@code '{'/'}'}</li>
     *         </ul>
     *
     * @return The new Route template
     */
    @Nonnull
    public static Route custom(@Nonnull Method method, @Nonnull String path)
    {
        Checks.notNull(method, "Method");
        Checks.notEmpty(path, "Route");
        Checks.noWhitespace(path, "Route");
        return new Route(method, path);
    }

    /**
     * Constructs a custom template route with the defined path using the method {@link Method#DELETE}.
     *
     * <p>You should use this convention: {@code "channel/{channel_id}/messages/{message_id}"} for the path.
     * <br>This is important because the rate limits highly depend on the configuration used to define the major parameters.
     * If one of the major parameters is used such as {@code channel_id/guild_id/webhook_id} it must be exactly this name
     * that is used to represent them in your route template. The parameter values must then be placed in the {@link #compile(String...)}
     * call. If the route already exists, it is desirable to re-use it for more efficient rate limit handling.
     *
     * @param  path
     *         The target path
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the method is null</li>
     *             <li>If the route is null, empty, or includes whitespace</li>
     *             <li>If the path format has an non-symmetric placement of {@code '{'/'}'}</li>
     *         </ul>
     *
     * @return The new Route template
     */
    @Nonnull
    public static Route delete(@Nonnull String path)
    {
        return custom(DELETE, path);
    }

    /**
     * Constructs a custom template route with the defined path using the method {@link Method#POST}.
     *
     * <p>You should use this convention: {@code "channel/{channel_id}/messages/{message_id}"} for the path.
     * <br>This is important because the rate limits highly depend on the configuration used to define the major parameters.
     * If one of the major parameters is used such as {@code channel_id/guild_id/webhook_id} it must be exactly this name
     * that is used to represent them in your route template. The parameter values must then be placed in the {@link #compile(String...)}
     * call. If the route already exists, it is desirable to re-use it for more efficient rate limit handling.
     *
     * @param  path
     *         The target path
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the method is null</li>
     *             <li>If the route is null, empty, or includes whitespace</li>
     *             <li>If the path format has an non-symmetric placement of {@code '{'/'}'}</li>
     *         </ul>
     *
     * @return The new Route template
     */
    @Nonnull
    public static Route post(@Nonnull String path)
    {
        return custom(POST, path);
    }

    /**
     * Constructs a custom template route with the defined path using the method {@link Method#PUT}.
     *
     * <p>You should use this convention: {@code "channel/{channel_id}/messages/{message_id}"} for the path.
     * <br>This is important because the rate limits highly depend on the configuration used to define the major parameters.
     * If one of the major parameters is used such as {@code channel_id/guild_id/webhook_id} it must be exactly this name
     * that is used to represent them in your route template. The parameter values must then be placed in the {@link #compile(String...)}
     * call. If the route already exists, it is desirable to re-use it for more efficient rate limit handling.
     *
     * @param  path
     *         The target path
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the method is null</li>
     *             <li>If the route is null, empty, or includes whitespace</li>
     *             <li>If the path format has an non-symmetric placement of {@code '{'/'}'}</li>
     *         </ul>
     *
     * @return The new Route template
     */
    @Nonnull
    public static Route put(@Nonnull String path)
    {
        return custom(PUT, path);
    }

    /**
     * Constructs a custom template route with the defined path using the method {@link Method#PATCH}.
     *
     * <p>You should use this convention: {@code "channel/{channel_id}/messages/{message_id}"} for the path.
     * <br>This is important because the rate limits highly depend on the configuration used to define the major parameters.
     * If one of the major parameters is used such as {@code channel_id/guild_id/webhook_id} it must be exactly this name
     * that is used to represent them in your route template. The parameter values must then be placed in the {@link #compile(String...)}
     * call. If the route already exists, it is desirable to re-use it for more efficient rate limit handling.
     *
     * @param  path
     *         The target path
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the method is null</li>
     *             <li>If the route is null, empty, or includes whitespace</li>
     *             <li>If the path format has an non-symmetric placement of {@code '{'/'}'}</li>
     *         </ul>
     *
     * @return The new Route template
     */
    @Nonnull
    public static Route patch(@Nonnull String path)
    {
        return custom(PATCH, path);
    }

    /**
     * Constructs a custom template route with the defined path using the method {@link Method#GET}.
     *
     * <p>You should use this convention: {@code "channel/{channel_id}/messages/{message_id}"} for the path.
     * <br>This is important because the rate limits highly depend on the configuration used to define the major parameters.
     * If one of the major parameters is used such as {@code channel_id/guild_id/webhook_id} it must be exactly this name
     * that is used to represent them in your route template. The parameter values must then be placed in the {@link #compile(String...)}
     * call. If the route already exists, it is desirable to re-use it for more efficient rate limit handling.
     *
     * @param  path
     *         The target path
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the method is null</li>
     *             <li>If the route is null, empty, or includes whitespace</li>
     *             <li>If the path format has an non-symmetric placement of {@code '{'/'}'}</li>
     *         </ul>
     *
     * @return The new Route template
     */
    @Nonnull
    public static Route get(@Nonnull String path)
    {
        return custom(GET, path);
    }

    private static final String majorParameters = "guild_id:channel_id:webhook_id";
    private final String route;
    private final Method method;
    private final int paramCount;

    private Route(Method method, String route)
    {
        this.method = method;
        this.route = route;
        this.paramCount = Helpers.countMatches(route, '{'); //All parameters start with {

        if (paramCount != Helpers.countMatches(route, '}'))
            throw new IllegalArgumentException("An argument does not have both {}'s for route: " + method + "  " + route);
    }

    /**
     * The HTTP method used for this endpoint.
     *
     * @return The HTTP method
     */
    public Method getMethod()
    {
        return method;
    }

    /**
     * The route template
     *
     * @return The template
     */
    public String getRoute()
    {
        return route;
    }

    /**
     * The number of parameters used in {@link #compile(String...)}.
     *
     * @return The number of parameters
     */
    public int getParamCount()
    {
        return paramCount;
    }

    /**
     * Compiles a usable route with the provided parameters.
     * <br>Each parameter should have a {@code "{token}"} in the route template.
     *
     * <p>Major parameters such as {@code channel_id/guild_id/webhook_id} will be used to determine buckets for rate limit handling.
     * All other parameters are only used to properly identify the endpoint for the request and have no impact on the rate limit.
     *
     * @param  params
     *         The parameters to use
     *
     * @throws IllegalArgumentException
     *         If the parameter count does not match the number of required parameters (tokens) in the route template
     *
     * @return The CompiledRoute to use for the request
     */
    public CompiledRoute compile(String... params)
    {
        if (params.length != paramCount)
        {
            throw new IllegalArgumentException("Error Compiling Route: [" + route + "], incorrect amount of parameters provided." +
                    "Expected: " + paramCount + ", Provided: " + params.length);
        }

        //Compile the route for interfacing with discord.

        StringBuilder majorParameter = new StringBuilder(majorParameters);
        StringBuilder compiledRoute = new StringBuilder(route);
        for (int i = 0; i < paramCount; i++)
        {
            int paramStart = compiledRoute.indexOf("{");
            int paramEnd = compiledRoute.indexOf("}");
            String paramName = compiledRoute.substring(paramStart+1, paramEnd);
            int majorParamIndex = majorParameter.indexOf(paramName);
            if (majorParamIndex > -1)
                majorParameter.replace(majorParamIndex, majorParamIndex + paramName.length(), params[i]);

            compiledRoute.replace(paramStart, paramEnd + 1, params[i]);
        }

        return new CompiledRoute(this, compiledRoute.toString(), majorParameter.toString());
    }

    @Override
    public int hashCode()
    {
        return (route + method.toString()).hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Route))
            return false;

        Route oRoute = (Route) o;
        return method.equals(oRoute.method) && route.equals(oRoute.route);
    }

    @Override
    public String toString()
    {
        return method + "/" + route;
    }

    /**
     * Compiled Route used for handling rate limits.
     *
     * @see #withQueryParams(String...)
     */
    public class CompiledRoute
    {
        private final Route baseRoute;
        private final String major;
        private final String compiledRoute;
        private final boolean hasQueryParams; 

        private CompiledRoute(Route baseRoute, String compiledRoute, String major, boolean hasQueryParams)
        {
            this.baseRoute = baseRoute;
            this.compiledRoute = compiledRoute;
            this.major = major;
            this.hasQueryParams = hasQueryParams;
        }

        private CompiledRoute(Route baseRoute, String compiledRoute, String major)
        {
            this(baseRoute, compiledRoute, major, false);
        }

        /**
         * Add query parameters such as {@code before} or {@code reason}.
         *
         * <h2>Example</h2>
         * <pre>{@code
         * Route template = Route.Messages.GET_MESSAGE_HISTORY;
         * Route.CompiledRoute route = template.compile(channelId);
         * // Don't forget to re-assign the route, this returns a new route
         * route = route.withQueryParams("before", messageId, "limit", 10);
         * }</pre>
         *
         * @param  params
         *         The query parameters
         *
         * @return The <b>new</b> CompiledRoute instance with the applied parameters
         */
        @Nonnull
        @CheckReturnValue
        public CompiledRoute withQueryParams(String... params)
        {
            Checks.check(params.length >= 2, "params length must be at least 2");
            Checks.check(params.length % 2 == 0, "params length must be a multiple of 2");

            StringBuilder newRoute = new StringBuilder(compiledRoute);

            for (int i = 0; i < params.length; i++)
                newRoute.append(!hasQueryParams && i == 0 ? '?' : '&').append(params[i]).append('=').append(params[++i]);

            return new CompiledRoute(baseRoute, newRoute.toString(), major, true);
        }

        /**
         * The major parameters string used to determine the rate limit bucket
         *
         * @return The major parameters
         */
        public String getMajorParameters()
        {
            return major;
        }

        /**
         * The compiled HTTP route with the applied parameters.
         *
         * @return The compiled route
         */
        public String getCompiledRoute()
        {
            return compiledRoute;
        }

        /**
         * The base route template without the applied parameters.
         *
         * @return The base route template
         */
        public Route getBaseRoute()
        {
            return baseRoute;
        }

        /**
         * The HTTP method of this endpoint
         *
         * @return The HTTP method
         */
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
            return "CompiledRoute(" + method + ": " + compiledRoute + ")";
        }
    }
}
