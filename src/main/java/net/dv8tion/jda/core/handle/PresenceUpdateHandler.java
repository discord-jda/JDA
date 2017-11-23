/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

import net.dv8tion.jda.client.JDAClient;
import net.dv8tion.jda.client.entities.impl.FriendImpl;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.impl.*;
import net.dv8tion.jda.core.events.user.UserAvatarUpdateEvent;
import net.dv8tion.jda.core.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.core.events.user.UserNameUpdateEvent;
import net.dv8tion.jda.core.events.user.UserOnlineStatusUpdateEvent;
import org.json.JSONObject;

import java.util.Objects;

public class PresenceUpdateHandler extends SocketHandler
{

    public PresenceUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        GuildImpl guild = null;
        //Do a pre-check to see if this is for a Guild, and if it is, if the guild is currently locked or not cached.
        if (!content.isNull("guild_id"))
        {
            final long guildId = content.getLong("guild_id");
            if (api.getGuildLock().isLocked(guildId))
                return guildId;
            guild = (GuildImpl) api.getGuildById(guildId);
            if (guild == null)
            {
                api.getEventCache().cache(EventCache.Type.GUILD, guildId, () -> handle(responseNumber, allContent));
                EventCache.LOG.debug("Received a PRESENCE_UPDATE for a guild that is not yet cached! " +
                    "GuildId: " + guildId + " UserId: " + content.getJSONObject("user").get("id"));
                return null;
            }
        }

        JSONObject jsonUser = content.getJSONObject("user");
        final long userId = jsonUser.getLong("id");
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
                String avatarId = jsonUser.optString("avatar", null);

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
                if (!Objects.equals(avatarId, oldAvatar))
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
            final JSONObject game = content.optJSONObject("game");
            if (game != null && !game.isNull("name"))
            {
                gameName = game.get("name").toString();
                gameUrl = game.isNull("url") ? null : game.get("url").toString();
                try
                {
                    type = game.isNull("type")
                            ? Game.GameType.DEFAULT
                            : Game.GameType.fromKey(Integer.parseInt(game.get("type").toString()));
                }
                catch (NumberFormatException ex)
                {
                    type = Game.GameType.DEFAULT;
                }
            }
            Game nextGame = gameName == null ? null : api.getEntityBuilder().createGame(gameName, gameUrl, type);
            OnlineStatus status = OnlineStatus.fromKey(content.getString("status"));

            //If we are in a Guild, then we will use Member.
            // If we aren't we'll be dealing with the Relation system.
            if (guild != null)
            {
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
                    if (!Objects.equals(member.getGame(), nextGame))
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
                if (api.getAccountType() != AccountType.CLIENT)
                    return null;
                JDAClient client = api.asClient();
                FriendImpl friend = (FriendImpl) client.getFriendById(userId);

                if (friend != null)
                {
                    if (!friend.getOnlineStatus().equals(status))
                    {
                        OnlineStatus oldStatus = friend.getOnlineStatus();
                        friend.setOnlineStatus(status);
                        api.getEventManager().handle(
                            new UserOnlineStatusUpdateEvent(
                                api, responseNumber,
                                user, null, oldStatus));
                    }
                    if (!Objects.equals(friend.getGame(), nextGame))
                    {
                        Game oldGame = friend.getGame();
                        friend.setGame(nextGame);
                        api.getEventManager().handle(
                            new UserGameUpdateEvent(
                                api, responseNumber,
                                user, null, oldGame));
                    }
                }
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

            //If this was for a Guild, cache it in the Guild for later use in GUILD_MEMBER_ADD
            if (status != OnlineStatus.OFFLINE && guild != null)
                guild.getCachedPresenceMap().put(userId, content);
        }
        return null;
    }
}
