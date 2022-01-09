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

package net.dv8tion.jda.internal.requests;

import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

import static net.dv8tion.jda.internal.requests.Method.*;

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

        public static final Route CALLBACK =        new Route(POST,   "interactions/{interaction_id}/{interaction_token}/callback");
        public static final Route CREATE_FOLLOWUP = new Route(POST,   "webhooks/{application_id}/{interaction_token}");
        public static final Route EDIT_FOLLOWUP =   new Route(PATCH,  "webhooks/{application_id}/{interaction_token}/messages/{message_id}");
        public static final Route DELETE_FOLLOWUP = new Route(DELETE, "webhooks/{application_id}/{interaction_token}/messages/{message_id}");
        public static final Route GET_ORIGINAL =    new Route(GET,    "webhooks/{application_id}/{interaction_token}/messages/@original");
    }

    public static class Self
    {
        public static final Route GET_SELF =               new Route(GET,    "users/@me");
        public static final Route MODIFY_SELF =            new Route(PATCH,  "users/@me");
        public static final Route GET_GUILDS  =            new Route(GET,    "users/@me/guilds");
        public static final Route LEAVE_GUILD =            new Route(DELETE, "users/@me/guilds/{guild_id}");
        public static final Route GET_PRIVATE_CHANNELS =   new Route(GET,    "users/@me/channels");
        public static final Route CREATE_PRIVATE_CHANNEL = new Route(POST,   "users/@me/channels");

        // Client only
        public static final Route USER_SETTINGS =       new Route(GET, "users/@me/settings");
        public static final Route GET_CONNECTIONS =     new Route(GET, "users/@me/connections");
        public static final Route FRIEND_SUGGESTIONS =  new Route(GET, "friend-suggestions");
        public static final Route GET_RECENT_MENTIONS = new Route(GET, "users/@me/mentions");
    }

    public static class Users
    {
        public static final Route GET_USER    = new Route(GET, "users/{user_id}");
        public static final Route GET_PROFILE = new Route(GET, "users/{user_id}/profile");
        public static final Route GET_NOTE    = new Route(GET, "users/@me/notes/{user_id}");
        public static final Route SET_NOTE    = new Route(PUT, "users/@me/notes/{user_id}");
    }

    public static class Relationships
    {
        public static final Route GET_RELATIONSHIPS =   new Route(GET,    "users/@me/relationships"); // Get Friends/Blocks/Incoming/Outgoing
        public static final Route GET_RELATIONSHIP =    new Route(GET,    "users/@me/relationships/{user_id}");
        public static final Route ADD_RELATIONSHIP =    new Route(PUT,    "users/@me/relationships/{user_id}"); // Add Friend/ Block
        public static final Route DELETE_RELATIONSHIP = new Route(DELETE, "users/@me/relationships/{user_id}"); // Delete Block/Unfriend/Ignore Request/Cancel Outgoing
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
        public static final Route GET_GUILD_EMOTES =   new Route(GET,    "guilds/{guild_id}/emojis");
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

        //Client Only
        public static final Route CREATE_GUILD = new Route(POST, "guilds");
        public static final Route DELETE_GUILD = new Route(POST, "guilds/{guild_id}/delete");
        public static final Route ACK_GUILD =    new Route(POST, "guilds/{guild_id}/ack");

        public static final Route MODIFY_NOTIFICATION_SETTINGS = new Route(PATCH, "users/@me/guilds/{guild_id}/settings");
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
        public static final Route EXECUTE_WEBHOOK        = new Route(POST,   "webhooks/{webhook_id}/{token}");
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
        public static final Route GET_WEBHOOKS =         new Route(GET,    "channels/{channel_id}/webhooks");
        public static final Route CREATE_WEBHOOK =       new Route(POST,   "channels/{channel_id}/webhooks");
        public static final Route CREATE_PERM_OVERRIDE = new Route(PUT,    "channels/{channel_id}/permissions/{permoverride_id}");
        public static final Route MODIFY_PERM_OVERRIDE = new Route(PUT,    "channels/{channel_id}/permissions/{permoverride_id}");
        public static final Route DELETE_PERM_OVERRIDE = new Route(DELETE, "channels/{channel_id}/permissions/{permoverride_id}");

        public static final Route SEND_TYPING =          new Route(POST,   "channels/{channel_id}/typing");
        public static final Route GET_PERMISSIONS =      new Route(GET,    "channels/{channel_id}/permissions");
        public static final Route GET_PERM_OVERRIDE =    new Route(GET,    "channels/{channel_id}/permissions/{permoverride_id}");
        public static final Route FOLLOW_CHANNEL =       new Route(POST,   "channels/{channel_id}/followers");

        public static final Route CREATE_THREAD_WITH_MESSAGE =              new Route(POST,     "channels/{channel_id}/messages/{message_id}/threads");
        public static final Route CREATE_THREAD_WITHOUT_MESSAGE =           new Route(POST,     "channels/{channel_id}/threads");
        public static final Route JOIN_THREAD =                             new Route(PUT,      "channels/{channel_id}/thread-members/@me");
        public static final Route ADD_THREAD_MEMBER =                       new Route(PUT,      "channels/{channel_id}/thread-members/{user_id}");
        public static final Route LEAVE_THREAD =                            new Route(DELETE,   "channels/{channel_id}/thread-members/@me");
        public static final Route REMOVE_THREAD_MEMBER =                    new Route(DELETE,   "channels/{channel_id}/thread-members/{user_id}");
        public static final Route GET_THREAD_MEMBER =                       new Route(GET,      "channels/{channel_id}/thread-members/{user_id}");
        public static final Route LIST_THREAD_MEMBERS =                     new Route(GET,      "channels/{channel_id}/thread-members");
        public static final Route LIST_PUBLIC_ARCHIVED_THREADS =            new Route(GET,      "channels/{channel_id}/threads/archived/public");
        public static final Route LIST_PRIVATE_ARCHIVED_THREADS =           new Route(GET,      "channels/{channel_id}/threads/archived/private");
        public static final Route LIST_JOINED_PRIVATE_ARCHIVED_THREADS =    new Route(GET,      "channels/{channel_id}/users/@me/threads/archived/private");

        // Client Only
        public static final Route GET_RECIPIENTS =   new Route(GET,    "channels/{channel_id}/recipients");
        public static final Route GET_RECIPIENT =    new Route(GET,    "channels/{channel_id}/recipients/{user_id}");
        public static final Route ADD_RECIPIENT =    new Route(PUT,    "channels/{channel_id}/recipients/{user_id}");
        public static final Route REMOVE_RECIPIENT = new Route(DELETE, "channels/{channel_id}/recipients/{user_id}");
        public static final Route START_CALL =       new Route(POST,   "channels/{channel_id}/call/ring");
        public static final Route STOP_CALL =        new Route(POST,   "channels/{channel_id}/call/stop_ringing"); // aka deny or end call
    }

    public static class StageInstances
    {
        public static final Route GET_INSTANCE =    new Route(GET,    "stage-instances/{channel_id}");
        public static final Route DELETE_INSTANCE = new Route(DELETE, "stage-instances/{channel_id}");
        public static final Route UPDATE_INSTANCE = new Route(PATCH,  "stage-instances/{channel_id}");
        public static final Route CREATE_INSTANCE = new Route(POST,   "stage-instances");
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
        public static final Route CROSSPOST_MESSAGE =   new Route(POST,   "channels/{channel_id}/messages/{message_id}/crosspost");

        //Bot only
        public static final Route GET_MESSAGE =     new Route(GET,  "channels/{channel_id}/messages/{message_id}");
        public static final Route DELETE_MESSAGES = new Route(POST, "channels/{channel_id}/messages/bulk-delete");

        //Client only
        public static final Route ACK_MESSAGE = new Route(POST, "channels/{channel_id}/messages/{message_id}/ack");
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

    @Nonnull
    public static Route custom(@Nonnull Method method, @Nonnull String route)
    {
        Checks.notNull(method, "Method");
        Checks.notEmpty(route, "Route");
        Checks.noWhitespace(route, "Route");
        return new Route(method, route);
    }

    @Nonnull
    public static Route delete(@Nonnull String route)
    {
        return custom(DELETE, route);
    }

    @Nonnull
    public static Route post(@Nonnull String route)
    {
        return custom(POST, route);
    }

    @Nonnull
    public static Route put(@Nonnull String route)
    {
        return custom(PUT, route);
    }

    @Nonnull
    public static Route patch(@Nonnull String route)
    {
        return custom(PATCH, route);
    }

    @Nonnull
    public static Route get(@Nonnull String route)
    {
        return custom(GET, route);
    }

    private static final String majorParameters = "guild_id:channel_id:webhook_id:interaction_token";
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

    public Method getMethod()
    {
        return method;
    }

    public String getRoute()
    {
        return route;
    }

    public int getParamCount()
    {
        return paramCount;
    }

    public CompiledRoute compile(String... params)
    {
        if (params.length != paramCount)
        {
            throw new IllegalArgumentException("Error Compiling Route: [" + route + "], incorrect amount of parameters provided." +
                    "Expected: " + paramCount + ", Provided: " + params.length);
        }

        //Compile the route for interfacing with discord.
        Set<String> major = new HashSet<>();
        StringBuilder compiledRoute = new StringBuilder(route);
        for (int i = 0; i < paramCount; i++)
        {
            int paramStart = compiledRoute.indexOf("{");
            int paramEnd = compiledRoute.indexOf("}");
            String paramName = compiledRoute.substring(paramStart+1, paramEnd);
            if (majorParameters.contains(paramName))
            {
                if (params[i].length() > 30) // probably a long interaction_token, hash it to keep logs clean (not useful anyway)
                    major.add(paramName + "=" + Integer.toUnsignedString(params[i].hashCode()));
                else
                    major.add(paramName + "=" + params[i]);
            }

            compiledRoute.replace(paramStart, paramEnd + 1, params[i]);
        }

        return new CompiledRoute(this, compiledRoute.toString(), major.isEmpty() ? "n/a" : String.join(":", major));
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

        public String getMajorParameters()
        {
            return major;
        }

        public String getCompiledRoute()
        {
            return compiledRoute;
        }

        public Route getBaseRoute()
        {
            return baseRoute;
        }

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
