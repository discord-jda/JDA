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

public class Route
{
    public static class Self
    {
        public static final Route ALL_GUILDS  = new Route(GET, "guilds");

    }

    public static class Users
    {

    }

    public static class Guilds
    {
        public static final Route GET_GUILD = new Route(GET, "guilds/{guild_id}", "guild_id");
        public static final Route GET_CHANNELS = new Route(GET, "guilds/{guild_id}/channels", "guild_id");

        public static final Route KICK_MEMBER = new Route(DELETE, "guilds/{guild_id}/members/{user_id}", "guild_id");
    }

    public static class Roles
    {

    }

    public static class Channels
    {

    }

    public static class Messages
    {

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
        String compiledRoute = String.format(compilableRoute, params);
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
            compiledRatelimitRoute = String.format(compiledRatelimitRoute, majorParams);
        }

        return new CompiledRoute(route, compiledRatelimitRoute, compiledRoute, method);
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
        return method.equals(oRoute.method) && route.equals(oRoute);
    }

    @Override
    public String toString()
    {
        return "Route(" + method + ": " + route + ")";
    }

    private class CompiledRoute
    {
        public final String route;
        public final String ratelimitRoute;
        public final String compiledRoute;
        public final HttpMethod method;

        public CompiledRoute(String route, String ratelimitRoute, String compiledRoute, HttpMethod method)
        {
            this.route = route;
            this.ratelimitRoute = ratelimitRoute;
            this.compiledRoute = compiledRoute;
            this.method = method;
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

            return method.equals(oCompiled.method) && compiledRoute.equals(oCompiled.compiledRoute);
        }

        @Override
        public String toString()
        {
            return "CompiledRoute(" + method + ": " + compiledRoute + ")";
        }
    }
}
