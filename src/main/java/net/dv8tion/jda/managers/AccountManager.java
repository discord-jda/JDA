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
package net.dv8tion.jda.managers;

import net.dv8tion.jda.OnlineStatus;
import net.dv8tion.jda.entities.Game;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.SelfInfo;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.impl.GameImpl;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.entities.impl.SelfInfoImpl;
import net.dv8tion.jda.requests.Requester;
import net.dv8tion.jda.utils.AvatarUtil;
import org.json.JSONObject;

/**
 * Manager used to modify aspects of the logged in account's information.
 */
public class AccountManager
{

    protected AvatarUtil.Avatar avatar = null;
    protected String username = null;

    protected final JDAImpl api;

    public AccountManager(JDAImpl api)
    {
        this.api = api;
    }

    /**
     * Set the avatar of the connected account.
     * This change will only be applied, when {@link #update()} is called
     * Avatars can get generated through the methods of {@link net.dv8tion.jda.utils.AvatarUtil AvatarUtil}
     *
     * @param avatar
     *      an Avatar object, null to keep current Avatar or {@link net.dv8tion.jda.utils.AvatarUtil#DELETE_AVATAR AvatarUtil#DELETE_AVATAR} to remove the avatar
     * @return
     *      this
     */
    public AccountManager setAvatar(AvatarUtil.Avatar avatar)
    {
        this.avatar = avatar;
        return this;
    }

    /**
     * Set the username of the connected account.
     * This change will only be applied, when {@link #update()} is called
     *
     * @param username
     *      the new username or null to discard changes
     * @return
     *      this
     */
    public AccountManager setUsername(String username)
    {
        this.username = username;
        return this;
    }

    /**
     * Set currently played game of the connected account.
     * To remove playing status, supply this method with null
     * This change will be applied <b>immediately</b>
     *
     * @param game
     *      the name of the game that should be displayed or null for no game
     */
    public void setGame(String game)
    {
        if(game != null && game.trim().isEmpty())
            game = null;
        ((SelfInfoImpl) api.getSelfInfo()).setCurrentGame(game == null ? null : new GameImpl(game, null, Game.GameType.DEFAULT));
        updateStatusAndGame();
    }

    /**
     * Set currently played game of the connected account.
     * To remove playing status, supply this method with null
     * This change will be applied <b>immediately</b>
     *
     * @param title
     *      the name of the stream that should be displayed
     * @param url
     *      the url of the twitch stream
     */
    public void setStreaming(String title, String url) throws IllegalArgumentException
    {
        if(title != null && title.trim().isEmpty())
            title = null;
        if(url != null && url.trim().isEmpty())
            url = null;
        if(title != null && !Game.isValidStreamingUrl(url))
            throw new IllegalArgumentException("Streaming Url must be valid!");
        ((SelfInfoImpl) api.getSelfInfo()).setCurrentGame( title == null ? null : new GameImpl(title, url, Game.GameType.TWITCH));
        updateStatusAndGame();
    }

    /**
     * Set status of the connected account.
     * This change will be applied <b>immediately</b>
     *
     * @param idle
     *      whether the account should be displayed as idle or not
     */
    public void setIdle(boolean idle)
    {
        ((SelfInfoImpl) api.getSelfInfo()).setOnlineStatus(idle ? OnlineStatus.AWAY : OnlineStatus.ONLINE);
        updateStatusAndGame();
    }

    /**
     * Changes this user's nickname in given guild.
     * The nickname is visible to all users of the guild.
     * This requires the correct Permissions to perform
     * (either {@link net.dv8tion.jda.Permission#NICKNAME_MANAGE NICKNAME_MANAGE} or
     * {@link net.dv8tion.jda.Permission#NICKNAME_CHANGE NICKNAME_CHANGE}).
     *
     * This change will be applied <b>immediately</b>
     *
     * @param guild
     *      The guild where the nickname should be changed.
     * @param nickname
     *      The new nickname of this user, or null/"" to reset
     * @throws net.dv8tion.jda.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     * @see net.dv8tion.jda.managers.GuildManager#setNickname(User, String)
     *      for more details
     */
    public void setNickname(Guild guild, String nickname)
    {
        guild.getManager().setNickname(api.getSelfInfo(), nickname);
    }

    /**
     * Resets all queued updates. So the next call to {@link #update()} will change nothing.
     */
    public void reset()
    {
        avatar = null;
        username = null;
    }

    /**
     * Updates the profile of the connected account, sends the changed data to the Discord server.
     */
    public void update()
    {
            JSONObject object = new JSONObject();
            object.put("avatar", avatar == null ? api.getSelfInfo().getAvatarId() : (avatar == AvatarUtil.DELETE_AVATAR ? JSONObject.NULL : avatar.getEncoded()));
            object.put("username", username == null ? api.getSelfInfo().getUsername() : username);

            JSONObject result = api.getRequester().patch(Requester.DISCORD_API_PREFIX + "users/@me", object).getObject();

            if (result == null || !result.has("token"))
            {
                throw new RuntimeException("Something went wrong while changing the account settings.");
            }

            api.setAuthToken(result.getString("token"));

            this.avatar = null;
            this.username = null;
    }

    protected void updateStatusAndGame()
    {
        SelfInfo selfInfo = api.getSelfInfo();
        JSONObject game = null;
        if(selfInfo.getCurrentGame() != null)
        {
            game = new JSONObject().put("name", selfInfo.getCurrentGame().getName());
            if(selfInfo.getCurrentGame().getType()!= Game.GameType.DEFAULT)
            {
                game = game
                        .put("url", selfInfo.getCurrentGame().getUrl())
                        .put("type", selfInfo.getCurrentGame().getType().getKey());
            }
        }
        JSONObject content = new JSONObject()
                .put("game", game == null ? JSONObject.NULL : game)
                .put("idle_since", selfInfo.getOnlineStatus() == OnlineStatus.AWAY ? System.currentTimeMillis() : JSONObject.NULL);
        api.getClient().send(new JSONObject().put("op", 3).put("d", content).toString());
    }
}
