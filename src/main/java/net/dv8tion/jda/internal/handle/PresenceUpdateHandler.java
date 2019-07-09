/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ClientType;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.events.user.update.*;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.UserImpl;
import net.dv8tion.jda.internal.utils.Helpers;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class PresenceUpdateHandler extends SocketHandler
{

    public PresenceUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        GuildImpl guild = null;
        //Do a pre-check to see if this is for a Guild, and if it is, if the guild is currently locked or not cached.
        if (!content.isNull("guild_id"))
        {
            final long guildId = content.getLong("guild_id");
            if (getJDA().getGuildSetupController().isLocked(guildId))
                return guildId;
            guild = (GuildImpl) getJDA().getGuildById(guildId);
            if (guild == null)
            {
                getJDA().getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
                EventCache.LOG.debug("Received a PRESENCE_UPDATE for a guild that is not yet cached! " +
                    "GuildId: " + guildId + " UserId: " + content.getObject("user").get("id"));
                return null;
            }
        }

        DataObject jsonUser = content.getObject("user");
        final long userId = jsonUser.getLong("id");
        UserImpl user = (UserImpl) getJDA().getUsersView().get(userId);

        //If we do know about the user, lets update the user's specific info.
        // Afterwards, we will see if we already have them cached in the specific guild
        // or Relation. If not, we'll cache the OnlineStatus and Activity for later handling
        // unless OnlineStatus is OFFLINE, in which case we probably received this event
        // due to a User leaving a guild or no longer being a relation.
        if (user != null)
        {
            if (jsonUser.hasKey("username"))
            {
                String name = jsonUser.getString("username");
                String discriminator = jsonUser.get("discriminator").toString();
                String avatarId = jsonUser.getString("avatar", null);
                String oldAvatar = user.getAvatarId();

                if (!user.getName().equals(name))
                {
                    String oldUsername = user.getName();
                    user.setName(name);
                    getJDA().handleEvent(
                        new UserUpdateNameEvent(
                            getJDA(), responseNumber,
                            user, oldUsername));
                }
                if (!user.getDiscriminator().equals(discriminator))
                {
                    String oldDiscriminator = user.getDiscriminator();
                    user.setDiscriminator(discriminator);
                    getJDA().handleEvent(
                        new UserUpdateDiscriminatorEvent(
                            getJDA(), responseNumber,
                            user, oldDiscriminator));
                }
                if (!Objects.equals(avatarId, oldAvatar))
                {
                    String oldAvatarId = user.getAvatarId();
                    user.setAvatarId(avatarId);
                    getJDA().handleEvent(
                        new UserUpdateAvatarEvent(
                            getJDA(), responseNumber,
                            user, oldAvatarId));
                }
            }

            //Now that we've update the User's info, lets see if we need to set the specific Presence information.
            // This is stored in the Member or Relation objects.
            final DataArray activityArray = !getJDA().isCacheFlagSet(CacheFlag.ACTIVITY) || content.isNull("activities") ? null : content.getArray("activities");
            List<Activity> newActivities = new ArrayList<>();
            boolean parsedGame = false;
            try
            {
                if (activityArray != null)
                {
                    for (int i = 0; i < activityArray.length(); i++)
                        newActivities.add(EntityBuilder.createActivity(activityArray.getObject(i)));
                    parsedGame = true;
                }
            }
            catch (Exception ex)
            {
                if (EntityBuilder.LOG.isDebugEnabled())
                    EntityBuilder.LOG.warn("Encountered exception trying to parse a presence! UserID: {} JSON: {}", userId, activityArray, ex);
                else
                    EntityBuilder.LOG.warn("Encountered exception trying to parse a presence! UserID: {} Message: {} Enable debug for details", userId, ex.getMessage());
            }

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
                    if (getJDA().isCacheFlagSet(CacheFlag.CLIENT_STATUS) && !content.isNull("client_status"))
                    {
                        DataObject json = content.getObject("client_status");
                        EnumSet<ClientType> types = EnumSet.of(ClientType.UNKNOWN);
                        for (String key : json.keys())
                        {
                            ClientType type = ClientType.fromKey(key);
                            types.add(type);
                            String raw = String.valueOf(json.get(key));
                            OnlineStatus clientStatus = OnlineStatus.fromKey(raw);
                            member.setOnlineStatus(type, clientStatus);
                        }
                        for (ClientType type : EnumSet.complementOf(types))
                            member.setOnlineStatus(type, null); // set remaining types to offline
                    }
                    //The member is already cached, so modify the presence values and fire events as needed.
                    if (!member.getOnlineStatus().equals(status))
                    {
                        OnlineStatus oldStatus = member.getOnlineStatus();
                        member.setOnlineStatus(status);
                        getJDA().handleEvent(
                            new UserUpdateOnlineStatusEvent(
                                getJDA(), responseNumber,
                                user, guild, oldStatus));
                    }
                    if (parsedGame)
                    {
                        List<Activity> oldActivities = member.getActivities();
                        boolean unorderedEquals = Helpers.deepEqualsUnordered(oldActivities, newActivities);
                        if (unorderedEquals)
                        {
                            boolean deepEquals = Helpers.deepEquals(oldActivities, newActivities);
                            if (!deepEquals)
                            {
                                member.setActivities(newActivities);
                                getJDA().handleEvent(
                                    new UserUpdateActivityOrderEvent(
                                        getJDA(), responseNumber,
                                        oldActivities, member));
                            }
                        }
                        else
                        {
                            member.setActivities(newActivities);
                            oldActivities = new ArrayList<>(oldActivities); // create modifiable copy
                            List<Activity> startedActivities = new ArrayList<>();
                            for (Activity activity : newActivities)
                            {
                                if (!oldActivities.remove(activity))
                                    startedActivities.add(activity);
                            }

                            for (Activity activity : startedActivities)
                            {
                                getJDA().handleEvent(
                                    new UserActivityStartEvent(
                                        getJDA(), responseNumber,
                                        member, activity));
                            }

                            for (Activity activity : oldActivities)
                            {
                                getJDA().handleEvent(
                                    new UserActivityEndEvent(
                                        getJDA(), responseNumber,
                                        member, activity));
                            }
                        }
                    }
                }
            }
            /*
            else
            {
                //In this case, this PRESENCE_UPDATE is for a Relation.
            }
            */
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
