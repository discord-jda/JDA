/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.requests;

import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.Helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.dv8tion.jda.core.requests.Method.*;

@SuppressWarnings("unused")
public class Route
{
    public static class Misc
    {
        public static final Route TRACK =             new Route(POST, true, "track");
        public static final Route GET_VOICE_REGIONS = new Route(GET,  true, "voice/regions");
        public static final Route GATEWAY =           new Route(GET,  true, "gateway");
        public static final Route GATEWAY_BOT =       new Route(GET,  true, "gateway/bot");
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
        public static final Route GET_SELF =               new Route(GET, true, "users/@me");
        public static final Route MODIFY_SELF =            new Route(PATCH,     "users/@me");
        public static final Route GET_GUILDS  =            new Route(GET,       "users/@me/guilds");
        public static final Route LEAVE_GUILD =            new Route(DELETE,    "users/@me/guilds/{guild_id}");
        public static final Route GET_PRIVATE_CHANNELS =   new Route(GET,       "users/@me/channels");
        public static final Route CREATE_PRIVATE_CHANNEL = new Route(POST,      "users/@me/channels");

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
        public static final Route GET_GUILD =          new Route(GET,    "guilds/{guild_id}",                   "guild_id");
        public static final Route MODIFY_GUILD =       new Route(PATCH,  "guilds/{guild_id}",                   "guild_id");
        public static final Route GET_VANITY_URL =     new Route(GET,    "guilds/{guild_id}/vanity-url",        "guild_id");
        public static final Route CREATE_CHANNEL =     new Route(POST,   "guilds/{guild_id}/channels",          "guild_id");
        public static final Route GET_CHANNELS =       new Route(GET,    "guilds/{guild_id}/channels",          "guild_id");
        public static final Route MODIFY_CHANNELS =    new Route(PATCH,  "guilds/{guild_id}/channels",          "guild_id");
        public static final Route MODIFY_ROLES =       new Route(PATCH,  "guilds/{guild_id}/roles",             "guild_id");
        public static final Route GET_BANS =           new Route(GET,    "guilds/{guild_id}/bans",              "guild_id");
        public static final Route UNBAN =              new Route(DELETE, "guilds/{guild_id}/bans/{user_id}",    "guild_id");
        public static final Route BAN =                new Route(PUT,    "guilds/{guild_id}/bans/{user_id}",    "guild_id");
        public static final Route KICK_MEMBER =        new Route(DELETE, "guilds/{guild_id}/members/{user_id}", "guild_id");
        public static final Route MODIFY_MEMBER =      new Route(PATCH,  "guilds/{guild_id}/members/{user_id}", "guild_id");
        // TODO: no headers
        public static final Route ADD_MEMBER =         new Route(PUT,    "guilds/{guild_id}/members/{user_id}", "guild_id");
        public static final Route MODIFY_SELF_NICK =   new Route(PATCH,  "guilds/{guild_id}/members/@me/nick",  "guild_id");
        public static final Route PRUNABLE_COUNT =     new Route(GET,    "guilds/{guild_id}/prune",             "guild_id");
        public static final Route PRUNE_MEMBERS =      new Route(POST,   "guilds/{guild_id}/prune",             "guild_id");
        public static final Route GET_WEBHOOKS =       new Route(GET,    "guilds/{guild_id}/webhooks",          "guild_id");
        public static final Route GET_GUILD_EMBED =    new Route(GET,    "guilds/{guild_id}/embed",             "guild_id");
        public static final Route MODIFY_GUILD_EMBED = new Route(PATCH,  "guilds/{guild_id}/embed",             "guild_id");
        public static final Route GET_GUILD_EMOTES =   new Route(GET,    "guilds/{guild_id}/emojis",            "guild_id");
        public static final Route GET_AUDIT_LOGS =     new Route(GET, true, "guilds/{guild_id}/audit-logs",        "guild_id");
        public static final Route GET_VOICE_REGIONS =  new Route(GET, true, "guilds/{guild_id}/regions",           "guild_id");

        public static final Route GET_INTEGRATIONS =   new Route(GET,    "guilds/{guild_id}/integrations",                       "guild_id");
        public static final Route CREATE_INTEGRATION = new Route(POST,   "guilds/{guild_id}/integrations",                       "guild_id");
        public static final Route DELETE_INTEGRATION = new Route(DELETE, "guilds/{guild_id}/integrations/{integration_id}",      "guild_id");
        public static final Route MODIFY_INTEGRATION = new Route(PATCH,  "guilds/{guild_id}/integrations/{integration_id}",      "guild_id");
        public static final Route SYNC_INTEGRATION =   new Route(POST,   "guilds/{guild_id}/integrations/{integration_id}/sync", "guild_id");

        public static final Route ADD_MEMBER_ROLE =    new Route(PUT,    "guilds/{guild_id}/members/{user_id}/roles/{role_id}",  "guild_id");
        public static final Route REMOVE_MEMBER_ROLE = new Route(DELETE, "guilds/{guild_id}/members/{user_id}/roles/{role_id}",  "guild_id");


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
        public static final Route EXECUTE_WEBHOOK        = new Route(POST, "webhooks/{webhook_id}/{token}",        "webhook_id");
        public static final Route EXECUTE_WEBHOOK_SLACK  = new Route(POST, "webhooks/{webhook_id}/{token}/slack",  "webhook_id");
        public static final Route EXECUTE_WEBHOOK_GITHUB = new Route(POST, "webhooks/{webhook_id}/{token}/github", "webhook_id");
    }

    public static class Roles
    {
        public static final Route GET_ROLES =   new Route(GET,    "guilds/{guild_id}/roles",           "guild_id");
        public static final Route CREATE_ROLE = new Route(POST,   "guilds/{guild_id}/roles",           "guild_id");
        public static final Route GET_ROLE =    new Route(GET,    "guilds/{guild_id}/roles/{role_id}", "guild_id");
        public static final Route MODIFY_ROLE = new Route(PATCH,  "guilds/{guild_id}/roles/{role_id}", "guild_id");
        public static final Route DELETE_ROLE = new Route(DELETE, "guilds/{guild_id}/roles/{role_id}", "guild_id");
    }

    public static class Channels
    {
        public static final Route DELETE_CHANNEL =       new Route(DELETE, "channels/{channel_id}");
        public static final Route MODIFY_CHANNEL =       new Route(PATCH,  "channels/{channel_id}",        "channel_id");
        public static final Route SEND_TYPING =          new Route(POST,   "channels/{channel_id}/typing", "channel_id");
        public static final Route GET_PERMISSIONS =      new Route(GET,    "channels/{channel_id}/permissions",                   "channel_id");
        public static final Route GET_PERM_OVERRIDE =    new Route(GET,    "channels/{channel_id}/permissions/{permoverride_id}", "channel_id");
        public static final Route GET_WEBHOOKS =         new Route(GET,    "channels/{channel_id}/webhooks",                      "channel_id");
        public static final Route CREATE_WEBHOOK =       new Route(POST,   "channels/{channel_id}/webhooks",                      "channel_id");
        public static final Route CREATE_PERM_OVERRIDE = new Route(PUT,    "channels/{channel_id}/permissions/{permoverride_id}", "channel_id");
        public static final Route MODIFY_PERM_OVERRIDE = new Route(PUT,    "channels/{channel_id}/permissions/{permoverride_id}", "channel_id");
        public static final Route DELETE_PERM_OVERRIDE = new Route(DELETE, "channels/{channel_id}/permissions/{permoverride_id}", "channel_id");

        // Client Only
        public static final Route GET_RECIPIENTS =   new Route(GET,    "channels/{channel_id}/recipients");
        public static final Route GET_RECIPIENT =    new Route(GET,    "channels/{channel_id}/recipients/{user_id}");
        public static final Route ADD_RECIPIENT =    new Route(PUT,    "channels/{channel_id}/recipients/{user_id}");
        public static final Route REMOVE_RECIPIENT = new Route(DELETE, "channels/{channel_id}/recipients/{user_id}");
        public static final Route START_CALL =       new Route(POST,   "channels/{channel_id}/call/ring");
        public static final Route STOP_CALL =        new Route(POST,   "channels/{channel_id}/call/stop_ringing"); // aka deny or end call
    }

    public static class Messages
    {
        public static final Route SEND_MESSAGE =          new Route(POST,   "channels/{channel_id}/messages",              "channel_id");
        public static final Route EDIT_MESSAGE =          new Route(PATCH,  "channels/{channel_id}/messages/{message_id}", "channel_id");
        public static final Route GET_PINNED_MESSAGES =   new Route(GET,    "channels/{channel_id}/pins",                  "channel_id");
        public static final Route ADD_PINNED_MESSAGE =    new Route(PUT,    "channels/{channel_id}/pins/{message_id}",     "channel_id");
        public static final Route REMOVE_PINNED_MESSAGE = new Route(DELETE, "channels/{channel_id}/pins/{message_id}",     "channel_id");

        public static final Route ADD_REACTION =             new Route(PUT,    new RateLimit(1, 250),
                                                                               "channels/{channel_id}/messages/{message_id}/reactions/{reaction_code}/@me",       "channel_id");
        public static final Route REMOVE_OWN_REACTION =      new Route(DELETE, "channels/{channel_id}/messages/{message_id}/reactions/{reaction_code}/@me",       "channel_id");
        public static final Route REMOVE_REACTION =          new Route(DELETE, "channels/{channel_id}/messages/{message_id}/reactions/{reaction_code}/{user_id}", "channel_id");
        public static final Route REMOVE_ALL_REACTIONS =     new Route(DELETE, "channels/{channel_id}/messages/{message_id}/reactions",                           "channel_id");
        public static final Route GET_REACTION_USERS =       new Route(GET,    "channels/{channel_id}/messages/{message_id}/reactions/{reaction_code}",           "channel_id");

        public static final Route DELETE_MESSAGE =      new Route(DELETE, true, "channels/{channel_id}/messages/{message_id}", "channel_id");
        public static final Route GET_MESSAGE_HISTORY = new Route(GET,    true, "channels/{channel_id}/messages",              "channel_id");

        //Bot only
        public static final Route GET_MESSAGE =     new Route(GET, true, "channels/{channel_id}/messages/{message_id}", "channel_id");
        public static final Route DELETE_MESSAGES = new Route(POST,      "channels/{channel_id}/messages/bulk-delete",  "channel_id");

        //Client only
        public static final Route ACK_MESSAGE = new Route(POST, "channels/{channel_id}/messages/{message_id}/ack");
    }

    public static class Invites
    {
        public static final Route GET_INVITE =          new Route(GET, true, "invites/{code}");
        public static final Route GET_GUILD_INVITES =   new Route(GET, true, "guilds/{guild_id}/invites",     "guild_id");
        public static final Route GET_CHANNEL_INVITES = new Route(GET, true, "channels/{channel_id}/invites", "channel_id");
        public static final Route CREATE_INVITE =       new Route(POST,      "channels/{channel_id}/invites", "channel_id");
        public static final Route DELETE_INVITE =       new Route(DELETE,    "invites/{code}");
    }

    public static class Custom
    {
        public static final Route DELETE_ROUTE = new Route(DELETE, "{}");
        public static final Route GET_ROUTE =    new Route(GET, "{}");
        public static final Route POST_ROUTE =   new Route(POST, "{}");
        public static final Route PUT_ROUTE =    new Route(PUT, "{}");
        public static final Route PATCH_ROUTE =  new Route(PATCH, "{}");
    }

    private final String route;
    private final String ratelimitRoute;
    private final String compilableRoute;
    private final int paramCount;
    private final Method method;
    private final List<Integer> majorParamIndexes = new ArrayList<>();
    private final RateLimit ratelimit;
    private final boolean missingHeaders;

    private Route(Method method, String route, String... majorParameters)
    {
        this(method, null, false, route, majorParameters);
    }

    private Route(Method method, RateLimit rateLimit, String route, String... majorParameters)
    {
        this(method, rateLimit, false, route, majorParameters);
    }

    private Route(Method method, boolean missingHeaders, String route, String... majorParameters)
    {
        this(method, null, missingHeaders, route, majorParameters);
    }

    private Route(Method method, RateLimit rateLimit, boolean missingHeaders, String route, String... majorParameters)
    {
        this.method = method;
        this.missingHeaders = missingHeaders;
        this.ratelimit = rateLimit;
        this.route = route;
        this.paramCount = Helpers.countMatches(route, '{'); //All parameters start with {

        if (paramCount != Helpers.countMatches(route, '}'))
            throw new IllegalArgumentException("An argument does not have both {}'s for route: " + method + "  " + route);

        //Create a String.format compilable route for parameter compiling.
        compilableRoute = route.replaceAll("\\{.*?\\}","%s");

        //If this route has major parameters that are unique markers for the ratelimit route, then we need to
        // create a ratelimit compilable route. This goes through and replaces the parameters specified by majorParameters
        // and records the parameter index so that when we compile it later we can select the proper parameters
        // from the ones provided to make sure we inject in the proper indexes.
        if (majorParameters.length != 0)
        {
            int paramIndex = 0;
            String replaceRoute = route;
            Pattern keyP = Pattern.compile("\\{(.*?)\\}");
            Matcher keyM = keyP.matcher(route);
            //Search the route for all parameters
            while(keyM.find())
            {
                String param = keyM.group(1);
                //Attempt to match the found parameter with any of our majorParameters
                for (String majorParam : majorParameters)
                {
                    //If the parameter is a major parameter, replace it with a string token and record its
                    // parameter index for later ratelimitRoute compiling.
                    if (param.equals(majorParam))
                    {
                        replaceRoute = replaceRoute.replace(keyM.group(0), "%s");
                        majorParamIndexes.add(paramIndex);
                    }
                }
                paramIndex++;
            }
            ratelimitRoute = replaceRoute;
        }
        else
            ratelimitRoute = route;
    }

    public Method getMethod()
    {
        return method;
    }

    public String getRoute()
    {
        return route;
    }

    public boolean isMissingHeaders()
    {
        return missingHeaders;
    }

    public String getRatelimitRoute()
    {
        return ratelimitRoute;
    }

    public final RateLimit getRatelimit()
    {
        return this.ratelimit;
    }

    public String getCompilableRoute()
    {
        return compilableRoute;
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
        String compiledRoute = String.format(compilableRoute, (Object[]) params);
        String compiledRatelimitRoute = ratelimitRoute;

        //If this route has major parameters which help to uniquely distinguish it from others of this route type then
        // compile it using the major parameter indexes we discovered in the constructor.
        if (!majorParamIndexes.isEmpty())
        {

            String[] majorParams = new String[majorParamIndexes.size()];
            for (int i = 0; i < majorParams.length; i++)
            {
                majorParams[i] = params[majorParamIndexes.get(i)];
            }
            compiledRatelimitRoute = String.format(compiledRatelimitRoute, (Object[]) majorParams);
        }

        return new CompiledRoute(this, compiledRatelimitRoute, compiledRoute);
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
        return "Route(" + method + ": " + route + ")";
    }

    public class CompiledRoute
    {
        private final Route baseRoute;
        private final String ratelimitRoute;
        private final String compiledRoute;
        private final boolean hasQueryParams; 

        private CompiledRoute(Route baseRoute, String ratelimitRoute, String compiledRoute, boolean hasQueryParams)
        {
            this.baseRoute = baseRoute;
            this.ratelimitRoute = ratelimitRoute;
            this.compiledRoute = compiledRoute;
            this.hasQueryParams = hasQueryParams;
        }

        private CompiledRoute(Route baseRoute, String ratelimitRoute, String compiledRoute)
        {
            this(baseRoute, ratelimitRoute, compiledRoute, false);
        }

        public CompiledRoute withQueryParams(String... params)
        {
            Checks.check(params.length >= 2, "params length must be at least 2");
            Checks.check(params.length % 2 == 0, "params length must be a multiple of 2");

            StringBuilder newRoute = new StringBuilder(compiledRoute);

            for (int i = 0; i < params.length; i++)
                newRoute.append(!hasQueryParams && i == 0 ? '?' : '&').append(params[i]).append('=').append(params[++i]);

            return new CompiledRoute(baseRoute, ratelimitRoute, newRoute.toString(), true);
        }

        public String getRatelimitRoute()
        {
            return ratelimitRoute;
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
    
    public static class RateLimit
    {
        final int usageLimit;
        final int resetTime; // in ms

        public RateLimit(int usageLimit, int resetTime)
        {
            this.usageLimit = usageLimit;
            this.resetTime = resetTime;
        }

        public final int getUsageLimit()
        {
            return this.usageLimit;
        }

        public final int getResetTime()
        {
            return this.resetTime;
        }
    }
}
