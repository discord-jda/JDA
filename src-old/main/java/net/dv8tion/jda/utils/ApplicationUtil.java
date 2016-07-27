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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.utils;

import net.dv8tion.jda.JDA;
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

    /**
     * Tries to retrieve the application-id of the Application that owns the logged in Bot-account.
     * This requires a JDA instance of a bot account for which it retrieves the parent application.
     *
     * @param jda
     *      The jda-instance of a bot account.
     * @return
     *      The application-id of the parent Application of the bot account or null on failure.
     */
    public static String getApplicationId(JDA jda) {
        Requester.Response response = ((JDAImpl) jda).getRequester().get(Requester.DISCORD_API_PREFIX + "oauth2/applications/@me");
        if (response.isOk())
        {
            return response.getObject().getString("id");
        }
        else
        {
            JDAImpl.LOG.debug("Fetching Application-id failed. Response: " + response.toString());
            return null;
        }
    }

    /**
     * Creates a OAuth invite-link used to invite bot-accounts.
     * This method does not check if the given application actually has a bot-account assigned.
     *
     * @param appId
     *      The id of the application that owns the bot
     * @param perms
     *      Possibly empty list of Permissions that should be requested via invite
     * @return
     *      The link used to invite the bot
     */
    public static String getAuthInvite(String appId, Permission... perms)
    {
        int perm = 0;
        for (Permission permission : perms)
        {
            perm = perm | (1 << permission.getOffset());
        }
        return "https://discordapp.com/oauth2/authorize?client_id=" + appId + "&scope=bot&permissions=" + perm;
    }

    /**
     * Creates a OAuth invite-link used to invite bot-accounts.
     * This requires a JDA instance of a bot account for which it retrieves the parent application.
     *
     * @param jda
     *      The JDA instance of a bot-account
     * @param perms
     *      Possibly empty list of Permissions that should be requested via invite
     * @return
     *      The link used to invite the bot or null on failure
     */
    public static String getAuthInvite(JDA jda, Permission... perms)
    {
        String applicationId = getApplicationId(jda);
        return applicationId == null ? null : getAuthInvite(applicationId, perms);
    }


    private final JDAImpl api;

    /**
     * Creates a new instance of the ApplicationUtil class.
     * This requires login-informations of the person owning the application(s).
     * <b>Do not use login-informations of a account you use as bot here.</b>
     *
     * @param email
     *      The email of the owner of the application(s)
     * @param password
     *      The email of the owner of the application(s)
     * @throws LoginException
     *      When the login-informations were incorrect
     */
    public ApplicationUtil(String email, String password) throws LoginException
    {
        api = new JDAImpl(false, false, false);
        api.setAuthToken(login(email, password));
    }

    /**
     * Retrieves all already existing Applications for the logged in Account
     * @return
     *      A Set of all already existing Applications
     */
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

    /**
     * Retrieves a specific Application of the logged in Account
     * @param id
     *      The id of the Application to retrieve
     * @return
     *      The requested Application, or null if no Application with that id existed
     */
    public Application getApplication(String id)
    {
        Requester.Response response = api.getRequester().get(Requester.DISCORD_API_PREFIX + "oauth2/applications/" + id);
        if (response.isOk())
        {
            return new Application(response.getObject());
        }
        return null;
    }

    /**
     * Creates a new Application with given name.<br>
     * Note that an account can have a max of 5 Applications assigned.
     *
     * @param appName
     *      The name of the new Application
     * @return
     *      The created Application
     */
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

        if (response.isRateLimit())
        {
            try {
                Thread.sleep(response.getObject().getLong("retry_after"));
            }
            catch(InterruptedException ignored) {}
            return login(email, password);
        }

        if (!response.isOk())
            throw new LoginException("The provided email / password combination was incorrect. Please provide valid details.");
        return response.getObject().getString("token");
    }

    /**
     * Represents a Application
     */
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

        /**
         * Returns the Id of this Application
         * @return
         *      The Id of this Application
         */
        public String getId()
        {
            return id;
        }

        /**
         * Returns the name of this Application
         * @return
         *      The name of this Application
         */
        public String getName()
        {
            return name;
        }

        /**
         * Changes the name of this Application
         * @param newName
         *      The new name of this Application
         */
        public void setName(String newName)
        {
            if(newName == null || newName.trim().length() == 0)
                throw new RuntimeException("Can't update Application-name to null / empty string!");
            updateApp(getFrame().put("name", newName));
        }

        /**
         * Returns the description of this Application
         * @return
         *      The description of this Application
         */
        public String getDescription()
        {
            return description;
        }

        /**
         * Changes the description of this Application
         * @param desc
         *      Not null description of the Application (to remove description pass empty String)
         */
        public void setDescription(String desc)
        {
            if(desc == null)
                throw new RuntimeException("Can't update Application-description to null!");
            updateApp(getFrame().put("description", desc));
        }

        /**
         * Returns the Application secret (Used for oAuth)
         * @return
         *      The Application secret
         */
        public String getSecret()
        {
            return secret;
        }

        /**
         * Returns the iconId of this Application
         * @return
         *      The iconId of this Application or null, if no icon is defined
         */
        public String getIconId()
        {
            return iconId;
        }

        /**
         * Changes the Icon of this Application.
         * @param newIcon
         *      The new icon to use, or null to remove old icon
         */
        public void setIcon(AvatarUtil.Avatar newIcon)
        {
            updateApp(getFrame().put("icon", (newIcon == null || newIcon == AvatarUtil.DELETE_AVATAR) ? JSONObject.NULL : newIcon.getEncoded()));
        }

        /**
         * Returns the icon-url of this Application
         * @return
         *      The icon-url of this Application or null, if no icon is defined
         */
        public String getIconUrl()
        {
            return iconId == null ? null : "https://cdn.discordapp.com/app-icons/" + id + '/' + iconId + ".jpg";
        }

        /**
         * Returns whether or not this Application has a bot-account assigned
         * @return
         *      True if this Application has a bot-account assigned, false otherwise
         */
        public boolean hasBot()
        {
            return bot != null;
        }

        /**
         * Returns the Bot assigned to this Application
         * @return
         *      The Bot assigned to this application, or null if no bot is assigned
         */
        public Bot getBot()
        {
            return bot;
        }

        /**
         * Creates a new Bot for this Application.
         * This is only possible, if no bot-account is already assigned.
         * The created Bot-account will have its name set to the name of the Application
         *
         * @return
         *      The created Bot
         */
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

        /**
         * Deletes this Application <b>and its assigned Bot (if present)</b>.
         */
        public void delete()
        {
            Requester.Response response = api.getRequester().delete(Requester.DISCORD_API_PREFIX + "oauth2/applications/" + id);
            if (!response.isOk())
            {
                throw new RuntimeException("Error deleting the application. Error: " + response.toString());
            }
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

        /**
         * Represents a Bot assigned to an Application
         * To change its Username, login to JDA and use the {@link net.dv8tion.jda.managers.AccountManager AccountManager}.
         */
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

            /**
             * Returns the name of this Bot
             * @return
             *      The name of this Bot
             */
            public String getName()
            {
                return name;
            }

            /**
             * Returns the discriminator of this Bot
             * @return
             *      The discriminator of this Bot
             */
            public String getDiscrim()
            {
                return discrim;
            }

            /**
             * Returns the token used to login to JDA with this Bot
             * @return
             *      The login-token of this Bot
             */
            public String getToken()
            {
                return token;
            }

            /**
             * Returns the id of this Bot
             * @return
             *      The id of this Bot
             */
            public String getId()
            {
                return id;
            }

            /**
             * Returns the avatarId of this Bot
             * @return
             *      The avatarId of this Bot or null, if no avatar is set
             */
            public String getAvatarId()
            {
                return avatarId;
            }

            /**
             * Returns the avatar-url of this Bot
             * @return
             *      The avatar-url of this Bot or null, if no avatar is set
             */
            public String getAvatarUrl()
            {
                return avatarId == null ? null : "https://cdn.discordapp.com/avatars/" + id + "/" + avatarId + ".jpg";
            }

            /**
             * Returns the oAuth link used to invite this Bot to a guild.
             * This is a shortcut to {@link ApplicationUtil#getAuthInvite(String, Permission...)}.
             *
             * @param perms
             *      Possibly empty list of Permissions that should be requested via invite
             * @return
             *      The oauth link to invite this Bot
             */
            public String getAuthInvite(Permission... perms)
            {
                return ApplicationUtil.getAuthInvite(Application.this.id, perms);
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
