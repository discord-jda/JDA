/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.entities.impl.UserImpl;
import net.dv8tion.jda.events.user.*;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class PresenceUpdateHandler extends SocketHandler
{

    public PresenceUpdateHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    public void handle(JSONObject content)
    {
        JSONObject jsonUser = content.getJSONObject("user");
        String id = jsonUser.getString("id");
        UserImpl user = (UserImpl) api.getUserMap().get(id);

        if (user == null)
        {
            //User for update doesn't exist, ignoring
            return;
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

        String gameName = (content.isNull("game") || content.getJSONObject("game").isNull("name"))
                ? null : content.getJSONObject("game").get("name").toString();
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
        if (!StringUtils.equals(user.getCurrentGame(), gameName))
        {
            String oldGameName = user.getCurrentGame();
            user.setCurrentGame(gameName);
            api.getEventManager().handle(
                    new UserGameUpdateEvent(
                            api, responseNumber,
                            user, oldGameName));
        }
        api.getEventManager().handle(
                new GenericUserEvent(
                        api, responseNumber,
                        user));
    }
}
