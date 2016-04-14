/**
 *      Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.utils;

import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.requests.Requester;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.util.HashSet;
import java.util.Set;

public class ApplicationUtil
{
    public static String getAuthInvite(String appId, Permission... perms)
    {
        int perm = 0;
        for (Permission permission : perms)
        {
            perm = perm | (1 << permission.getOffset());
        }
        return "https://discordapp.com/oauth2/authorize?&client_id=" + appId + "&scope=bot&permissions=" + perm;
    }

    private final JDAImpl api;

    public ApplicationUtil(String email, String password) throws LoginException
    {
        api = new JDAImpl(false);
        api.setAuthToken(login(email, password));
    }

    public Set<Application> getApplications()
    {
        Requester.Response response = api.getRequester().get(Requester.DISCORD_API_PREFIX + "oauth2/applications");
        Set<Application> apps = new HashSet<>();
        if (response.isOk())
        {
            JSONArray array = response.getArray();
            for (int i = 0; i < array.length(); i++)
            {
                apps.add(new Application(array.getJSONObject(i)));
            }
        }
        return apps;
    }

    public Application getApplication(String id)
    {
        Requester.Response response = api.getRequester().get(Requester.DISCORD_API_PREFIX + "oauth2/applications/" + id);
        if (response.isOk())
        {
            return new Application(response.getObject());
        }
        return null;
    }

    public Application createApplication(String appName)
    {
        Requester.Response response = api.getRequester().post(Requester.DISCORD_API_PREFIX + "oauth2/applications",
                new JSONObject().put("name", appName));
        if(response.isOk())
        {
            return new Application(response.getObject());
        }
        throw new RuntimeException("Error creating a new Application: " + response.toString());
    }

    private String login(String email, String password) throws LoginException
    {
        Requester.Response response = api.getRequester().post(Requester.DISCORD_API_PREFIX + "auth/login", new JSONObject().put("email", email).put("password", password));

        if (!response.isOk())
            throw new LoginException("The provided email / password combination was incorrect. Please provide valid details.");
        return response.getObject().getString("token");
    }

    public class Application
    {
        private String id;
        private String name;
        private String description;
        private String secret;
        private String iconId;
        private Bot bot;
        private final JSONArray redirects;

        private Application(JSONObject obj)
        {
            redirects = obj.getJSONArray("redirect_uris");
            parseFromJson(obj);
        }

        public String getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String newName)
        {
            if(newName == null || newName.trim().length() == 0)
                throw new RuntimeException("Can't update Application-name to null / empty string!");
            updateApp(getFrame().put("name", newName));
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String desc)
        {
            if(desc == null)
                throw new RuntimeException("Can't update Application-description to null!");
            updateApp(getFrame().put("description", desc));
        }

        public String getSecret()
        {
            return secret;
        }

        public String getIconId()
        {
            return iconId;
        }

        public void setIcon(AvatarUtil.Avatar newIcon)
        {
            updateApp(getFrame().put("icon", (newIcon == null || newIcon == AvatarUtil.DELETE_AVATAR) ? JSONObject.NULL : newIcon.getEncoded()));
        }

        public String getIconUrl()
        {
            return iconId == null ? null : "https://cdn.discordapp.com/app-icons/" + id + '/' + iconId + ".jpg";
        }

        public boolean hasBot()
        {
            return bot != null;
        }

        public Bot getBot()
        {
            return bot;
        }

        public Bot createBot()
        {
            if (hasBot())
            {
                throw new RuntimeException("Can't create a 2nd Bot for this Application!");
            }
            Requester.Response response = api.getRequester().post(Requester.DISCORD_API_PREFIX + "oauth2/applications/" + id + "/bot",
                    new JSONObject());
            if (response.isOk())
            {
                JSONObject botO = response.getObject();
                bot = new Bot(botO.getString("username"), botO.getString("discriminator"), botO.getString("token"),
                        botO.getString("id"), botO.isNull("avatar") ? null : botO.getString("avatar"));
                return bot;
            }
            throw new RuntimeException("Error creating a new Bot: " + response.toString());
        }

        public Bot migrateBot(String email, String password) throws LoginException
        {
            if (hasBot())
            {
                throw new RuntimeException("Can't create a 2nd Bot for this Application!");
            }
            String tokenSave = api.getAuthToken();
            api.setAuthToken(null);
            String botToken = login(email, password);
            api.setAuthToken(tokenSave);

            Requester.Response response = api.getRequester().post(Requester.DISCORD_API_PREFIX + "oauth2/applications/" + id + "/bot",
                    new JSONObject().put("token", botToken));
            if (response.isOk())
            {
                JSONObject botO = response.getObject();
                bot = new Bot(botO.getString("username"), botO.getString("discriminator"), botO.getString("token"),
                        botO.getString("id"), botO.isNull("avatar") ? null : botO.getString("avatar"));
                return bot;
            }
            throw new RuntimeException("Error creating a new Bot: " + response.toString());
        }

        private void updateApp(JSONObject o)
        {
            Requester.Response put = api.getRequester().put(Requester.DISCORD_API_PREFIX + "oauth2/applications/" + id, o);
            if (put.isOk())
            {
                parseFromJson(put.getObject());
            }
            else
            {
                throw new RuntimeException("Error updating Application: " + put.toString());
            }
        }

        private JSONObject getFrame()
        {
            return new JSONObject().put("icon", iconId).put("description", description).put("name", name).put("redirect_uris", redirects);
        }

        private void parseFromJson(JSONObject o)
        {
            id = o.getString("id");
            name = o.getString("name");
            description = o.getString("description");
            secret = o.getString("secret");
            iconId = o.isNull("icon") ? null : o.getString("icon");
            bot = null;
            if (o.has("bot"))
            {
                JSONObject botO = o.getJSONObject("bot");
                bot = new Bot(botO.getString("username"), botO.getString("discriminator"), botO.getString("token"),
                        botO.getString("id"), botO.isNull("avatar") ? null : botO.getString("avatar"));
            }
        }

        @Override
        public int hashCode()
        {
            return id.hashCode();
        }

        @Override
        public String toString()
        {
            return "Application[" + name + '(' + id + ')'
                    + (hasBot() ? ' ' + getBot().toString() : "")
                    + ']';
        }

        public class Bot {
            private String name;
            private String discrim;
            private String token;
            private String id;
            private String avatarId;

            private Bot(String name, String discrim, String token, String id, String avatarId)
            {
                this.name = name;
                this.discrim = discrim;
                this.token = token;
                this.id = id;
                this.avatarId = avatarId;
            }

            public String getName()
            {
                return name;
            }

            public String getDiscrim()
            {
                return discrim;
            }

            public String getToken()
            {
                return token;
            }

            public String getId()
            {
                return id;
            }

            public String getAvatarId()
            {
                return avatarId;
            }

            public String getAvatarUrl()
            {
                return avatarId == null ? null : "https://cdn.discordapp.com/avatars/" + id + "/" + avatarId + ".jpg";
            }

            public String getAuthInvite(Permission... perms)
            {
                return ApplicationUtil.getAuthInvite(id, perms);
            }

            @Override
            public int hashCode()
            {
                return id.hashCode();
            }

            @Override
            public String toString()
            {
                return "Bot[" + name + '(' + id + ")]";
            }
        }
    }
}
