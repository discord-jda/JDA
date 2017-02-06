/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.core.handle;

import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.impl.*;
import net.dv8tion.jda.core.events.user.UserAvatarUpdateEvent;
import net.dv8tion.jda.core.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.core.events.user.UserNameUpdateEvent;
import net.dv8tion.jda.core.events.user.UserOnlineStatusUpdateEvent;
import net.dv8tion.jda.core.requests.GuildLock;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class PresenceUpdateHandler extends SocketHandler
{

    public PresenceUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        //Do a pre-check to see if this is for a Guild, and if it is, if the guild is currently locked.
        if (content.has("guild_id") && GuildLock.get(api).isLocked(content.getString("guild_id")))
        {
            return content.getString("guild_id");
        }

        JSONObject jsonUser = content.getJSONObject("user");
        String userId = jsonUser.getString("id");
        UserImpl user = (UserImpl) api.getUserMap().get(userId);

        //If we do know about the user, lets update the user's specific info.
        // Afterwards, we will see if we already have them cached in the specific guild
        // or Relation. If not, we'll cache the OnlineStatus and Game for later handling
        // unless OnlineStatus is OFFLINE, in which case we probably received this event
        // due to a User leaving a guild or no longer being a relation.
        if (user != null)
        {
            if (jsonUser.has("username"))
            {
                String name = jsonUser.getString("username");
                String discriminator = jsonUser.get("discriminator").toString();
                String avatarId = jsonUser.isNull("avatar") ? null : jsonUser.getString("avatar");

                if (!user.getName().equals(name))
                {
                    String oldUsername = user.getName();
                    String oldDiscriminator = user.getDiscriminator();
                    user.setName(name);
                    user.setDiscriminator(discriminator);
                    api.getEventManager().handle(
                            new UserNameUpdateEvent(
                                    api, responseNumber,
                                    user, oldUsername, oldDiscriminator));
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

            //Now that we've update the User's info, lets see if we need to set the specific Presence information.
            // This is stored in the Member or Relation objects.
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
            Game nextGame = (gameName == null
                    ? null
                    : new GameImpl(gameName, gameUrl, type));
            OnlineStatus status = OnlineStatus.fromKey(content.getString("status"));

            //If we are in a Guild, then we will use Member.
            // If we aren't we'll be dealing with the Relation system.
            if (content.has("guild_id"))
            {
                GuildImpl guild = (GuildImpl) api.getGuildById(content.getString("guild_id"));
                MemberImpl member = (MemberImpl) guild.getMember(user);

                //If the Member is null, then User isn't in the Guild.
                //This is either because this PRESENCE_UPDATE was received before the GUILD_MEMBER_ADD event
                // or because a Member recently left and this PRESENCE_UPDATE came after the GUILD_MEMBER_REMOVE event.
                //If it is because a Member recently left, then the status should be OFFLINE. As such, we will ignore
                // the event if this is the case. If the status isn't OFFLINE, we will cache and use it when the
                // Member object is setup during GUILD_MEMBER_ADD
                if (member == null)
                {
                    //Cache the presence and return to finish up.
                    if (status != OnlineStatus.OFFLINE)
                    {
                        guild.getCachedPresenceMap().put(userId, content);
                        return null;
                    }
                }
                else
                {
                    //The member is already cached, so modify the presence values and fire events as needed.
                    if (!member.getOnlineStatus().equals(status))
                    {
                        OnlineStatus oldStatus = member.getOnlineStatus();
                        member.setOnlineStatus(status);
                        api.getEventManager().handle(
                                new UserOnlineStatusUpdateEvent(
                                        api, responseNumber,
                                        user, guild, oldStatus));
                    }
                    if(member.getGame() == null ? nextGame != null : !member.getGame().equals(nextGame))
                    {
                        Game oldGame = member.getGame();
                        member.setGame(nextGame);
                        api.getEventManager().handle(
                                new UserGameUpdateEvent(
                                        api, responseNumber,
                                        user, guild, oldGame));
                    }
                }
            }
            else
            {
                //In this case, this PRESENCE_UPDATE is for a Relation.

            }
        }
        else
        {
            //In this case, we don't have the User cached, which means that we can't update the User's information.
            // This is most likely because this PRESENCE_UPDATE came before the GUILD_MEMBER_ADD that would have added
            // this User to our User cache. Or, it could have come after a GUILD_MEMBER_REMOVE that caused the User
            // to be removed from JDA's central User cache because there were no more connected Guilds. If this is
            // the case, then the OnlineStatus will be OFFLINE and we can ignore this event.
            //Either way, we don't have the User cached so we need to cache the Presence information if
            // the OnlineStatus is not OFFLINE.

            //If the OnlineStatus is OFFLINE, ignore the event and return.
            OnlineStatus status = OnlineStatus.fromKey(content.getString("status"));
            if (status == OnlineStatus.OFFLINE)
                return null;

            //If this was for a Guild, cache it in the Guild for later use in GUILD_MEMBER_ADD
            if (content.has("guild_id"))
            {
                GuildImpl guild = (GuildImpl) api.getGuildById(content.getString("guild_id"));
                guild.getCachedPresenceMap().put(userId, content);
            }
            else
            {
                //cache in relationship stuff
            }
        }
        return null;
    }
}
