/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.requests;

import com.mashape.unirest.http.HttpMethod;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mashape.unirest.http.HttpMethod.DELETE;
import static com.mashape.unirest.http.HttpMethod.GET;
import static com.mashape.unirest.http.HttpMethod.PATCH;
import static com.mashape.unirest.http.HttpMethod.POST;
import static com.mashape.unirest.http.HttpMethod.PUT;

public class Route
{
    public static class Self
    {
        public static final Route GET_SELF =               new Route(GET,    "users/@me");
        public static final Route UPDATE_SELF =            new Route(PATCH,  "users/@me");
        public static final Route GET_GUILDS  =            new Route(GET,    "users/@me/guilds");
        public static final Route LEAVE_GUILD =            new Route(DELETE, "users/@me/guilds/{guild_id}");
        public static final Route GET_PRIVATE_CHANNELS =   new Route(GET,    "users/@me/channels");
        public static final Route CREATE_PRIVATE_CHANNEL = new Route(POST,   "users/@me/channels");
        public static final Route GATEWAY =                new Route(GET,    "gateway");
    }

    public static class Users
    {
        public static final Route GET_USER = new Route(GET, "users/{user_id}");
    }

    public static class Guilds
    {
        public static final Route GET_GUILD =       new Route(GET,    "guilds/{guild_id}",                   "guild_id");
        public static final Route MODIFY_GUILD =    new Route(PATCH,  "guilds/{guild_id}",                   "guild_id");
        public static final Route GET_CHANNELS =    new Route(GET,    "guilds/{guild_id}/channels",          "guild_id");
        public static final Route MODIFY_CHANNELS = new Route(PATCH,  "guilds/{guild_id}/channels",          "guild_id");
        public static final Route GET_BANS =        new Route(GET,    "guilds/{guild_id}/bans",              "guild_id");
        public static final Route CREATE_BAN =      new Route(PUT,    "guilds/{guild_id}/bans/{user_id}",    "guild_id");
        public static final Route REMOVE_BAN =      new Route(DELETE, "guilds/{guild_id}/bans/{user_id}",    "guild_id");
        public static final Route KICK_MEMBER =     new Route(DELETE, "guilds/{guild_id}/members/{user_id}", "guild_id");
        public static final Route MODIFY_MEMBER =   new Route(PATCH,  "guilds/{guild_id}/members/{user_id}", "guild_id");
        public static final Route MODIFY_NICKNAME = new Route(PATCH,  "guilds/{guild_id}/members/{user_id}/nick", "guild_id");

        //Client Only
        public static final Route CREATE_GUILD =    new Route(POST,   "guilds");
        public static final Route DELETE_GUILD =    new Route(POST,   "guilds/{guild_id}/delete");
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
        public static final Route DELETE_CHANNEL = new Route(DELETE, "channels/{channel_id}",        "channel_id");
        public static final Route MODIFY_CHANNEL = new Route(PATCH,  "channels/{channel_id}",        "channel_id");
        public static final Route SEND_TYPING =    new Route(POST,   "channels/{channel_id}/typing", "channel_id");
        public static final Route GET_PERMISSIONS =      new Route(GET,    "channels/{channel_id}/permissions",                   "channel_id");
        public static final Route GET_PERM_OVERRIDE =    new Route(GET,    "channels/{channel_id}/permissions/{permoverride_id}", "channel_id");
        public static final Route CREATE_PERM_OVERRIDE = new Route(PUT,    "channels/{channel_id}/permissions/{permoverride_id}", "channel_id");
        public static final Route MODIFY_PERM_OVERRIDE = new Route(PATCH,  "channels/{channel_id}/permissions/{permoverride_id}", "channel_id");
        public static final Route DELETE_PERM_OVERRIDE = new Route(DELETE, "channels/{channel_id}/permissions/{permoverride_id}", "channel_id");
    }

    public static class Messages
    {
        public static final Route GET_MESSAGES =    new Route(GET,      "channels/{channel_id}/messages",              "channel_id");
        public static final Route SEND_MESSAGE =    new Route(POST,     "channels/{channel_id}/messages",              "channel_id");
        public static final Route EDIT_MESSAGE =  new Route(PATCH,    "channels/{channel_id}/messages/{message_id}", "channel_id");
        public static final Route DELETE_MESSAGE =  new Route(DELETE,   "channels/{channel_id}/messages/{message_id}", "channel_id");
        public static final Route GET_PINNED_MESSAGES = new Route(GET,  "channels/{channel_id}/pins",                  "channel_id");
        public static final Route ADD_PINNED_MESSAGE =  new Route(PUT,  "channels/{channel_id}/pins/{message_id}",     "channel_id");
        public static final Route REMOVE_PINNED_MESSAGE = new Route(DELETE, "channels/{channel_id}/pins/{message_id}", "channel_id");


        //Bot only
        public static final Route GET_MESSAGE =     new Route(GET, "channels/{channel_id}/messages/{message_id}", "channel_id");
        public static final Route DELETE_MESSAGES = new Route(PUT, "channels/{channel_id}/messages/bulk_delete",  "channel_id");
    }

    public static class Invites
    {
        public static final Route GET_INVITE =          new Route(GET,    "invites/{code}");
        public static final Route DELETE_INVITE =       new Route(DELETE, "invites/{code}");
        public static final Route GET_GUILD_INVITES =   new Route(GET,    "guilds/{guild_id}/invites",     "guild_id");
        public static final Route GET_CHANNEL_INVITES = new Route(GET,    "channels/{channel_id}/invites", "channel_id");
        public static final Route CREATE_INVITE =       new Route(POST,   "channels/{channel_id}/invites", "channel_id");

        //Client Only
        public static final Route ACCEPT_INVITE =       new Route(POST,   "invites/{code}");
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
    private final HttpMethod method;
    private final List<Integer> majorParamIndexes = new ArrayList<>();

    private Route(HttpMethod method, String route, String... majorParameters)
    {
        this.method = method;
        this.route = route;
        this.paramCount = StringUtils.countMatches(route, '{'); //All parameters start with {

        if (paramCount != StringUtils.countMatches(route, '}'))
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

    public HttpMethod getMethod()
    {
        return method;
    }

    public String getRoute()
    {
        return route;
    }

    public String getRatelimitRoute()
    {
        return ratelimitRoute;
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

        private CompiledRoute(Route baseRoute, String ratelimitRoute, String compiledRoute)
        {
            this.baseRoute = baseRoute;
            this.ratelimitRoute = ratelimitRoute;
            this.compiledRoute = compiledRoute;
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

        public HttpMethod getMethod()
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

    //TODO: Remove once all Routes have been implemented.
    public static void main(String[] args)
    {
        System.out.println("Forcing static load to test for invalid {} parameters!");
        Route r;
        r = Self.CREATE_PRIVATE_CHANNEL;
        r = Users.GET_USER;
        r = Guilds.CREATE_BAN;
        r = Roles.CREATE_ROLE;
        r = Channels.CREATE_PERM_OVERRIDE;
        r = Messages.ADD_PINNED_MESSAGE;
        r = Invites.ACCEPT_INVITE;
        System.out.println("Done!");
    }
}
