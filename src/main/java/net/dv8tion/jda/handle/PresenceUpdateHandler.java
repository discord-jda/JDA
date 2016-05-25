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
package net.dv8tion.jda.handle;

import net.dv8tion.jda.OnlineStatus;
import net.dv8tion.jda.entities.Game;
import net.dv8tion.jda.entities.impl.GameImpl;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.entities.impl.UserImpl;
import net.dv8tion.jda.events.user.*;
import net.dv8tion.jda.requests.GuildLock;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class PresenceUpdateHandler extends SocketHandler
{

    public PresenceUpdateHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        if (!content.has("guild_id"))
        {
            return null;
        }
        if (GuildLock.get(api).isLocked(content.getString("guild_id")))
        {
            return content.getString("guild_id");
        }

        JSONObject jsonUser = content.getJSONObject("user");
        String id = jsonUser.getString("id");
        UserImpl user = (UserImpl) api.getUserMap().get(id);

        if (user == null)
        {
            //This is basically only received when a user has left a guild. Discord doesn't always send events
            // in order, so sometimes we get the presence updated after the GuildMemberLeave event has been received
            // thus there is no user which to apply a presence update to.
            //We don't cache this event because it is completely unneeded, furthermore it can (and probably will)
            // cause problems if the user rejoins the guild.
            return null;
        }

        if (jsonUser.has("username"))
        {
            String username = jsonUser.getString("username");
            String discriminator = jsonUser.get("discriminator").toString();
            String avatarId = jsonUser.isNull("avatar") ? null : jsonUser.getString("avatar");

            if (!user.getUsername().equals(username))
            {
                String oldUsername = user.getUsername();
                user.setUserName(username);
                user.setDiscriminator(discriminator);
                api.getEventManager().handle(
                        new UserNameUpdateEvent(
                                api, responseNumber,
                                user, oldUsername));
            }
            String oldAvatar = user.getAvatarId();
            if (!(avatarId == null && oldAvatar == null) && !StringUtils.equals(avatarId, oldAvatar))
            {
                String oldAvatarId = user.getAvatarId();
                user.setAvatarId(avatarId);
                api.getEventManager().handle(
                        new UserAvatarUpdateEvent(
                                api, responseNumber,
                                user, oldAvatarId));
            }
        }

        String gameName = null;
        String gameUrl = null;
        Game.GameType type = null;
        if ( !content.isNull("game") && !content.getJSONObject("game").isNull("name") )
        {
            gameName = content.getJSONObject("game").get("name").toString();
            gameUrl = ( content.getJSONObject("game").isNull("url") ? null : content.getJSONObject("game").get("url").toString() );
            try
            {
                type = content.getJSONObject("game").isNull("type")
                    ? Game.GameType.DEFAULT
                    : Game.GameType.fromKey(Integer.parseInt(content.getJSONObject("game").get("type").toString()));
            }
            catch (NumberFormatException ex)
            {
                type = Game.GameType.DEFAULT;
            }
        }
        Game nextGame = ( gameName == null ? null : new GameImpl(gameName, gameUrl, type));
        OnlineStatus status = OnlineStatus.fromKey(content.getString("status"));

        if (!user.getOnlineStatus().equals(status))
        {
            OnlineStatus oldStatus = user.getOnlineStatus();
            user.setOnlineStatus(status);
            api.getEventManager().handle(
                    new UserOnlineStatusUpdateEvent(
                            api, responseNumber,
                            user, oldStatus));
        }
        if(user.getCurrentGame() == null ? nextGame != null : !user.getCurrentGame().equals(nextGame))
        {
            Game oldGame = user.getCurrentGame();
            user.setCurrentGame(nextGame);
            api.getEventManager().handle(
                    new UserGameUpdateEvent(
                            api, responseNumber,
                            user, oldGame));
        }
        api.getEventManager().handle(
                new GenericUserEvent(
                        api, responseNumber,
                        user));
        return null;
    }
}
