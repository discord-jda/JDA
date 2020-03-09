/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import net.dv8tion.jda.api.events.user.update.UserUpdateActivityOrderEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.UserImpl;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class PresenceUpdateHandler extends SocketHandler
{
    private static final Logger log = JDALogger.getLog(PresenceUpdateHandler.class);

    public PresenceUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        // Ignore events for relationships, presences are guild only to us
        if (content.isNull("guild_id"))
        {
            log.debug("Received PRESENCE_UPDATE without guild_id. Ignoring event.");
            return null;
        }

        //Do a pre-check to see if this is for a Guild, and if it is, if the guild is currently locked or not cached.
        final long guildId = content.getLong("guild_id");
        if (getJDA().getGuildSetupController().isLocked(guildId))
            return guildId;
        GuildImpl guild = (GuildImpl) getJDA().getGuildById(guildId);
        if (guild == null)
        {
            getJDA().getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Received a PRESENCE_UPDATE for a guild that is not yet cached! GuildId:{} UserId: {}",
                                 guildId, content.getObject("user").get("id"));
            return null;
        }

        DataObject jsonUser = content.getObject("user");
        final long userId = jsonUser.getLong("id");
        UserImpl user = (UserImpl) getJDA().getUsersView().get(userId);

        // The user is not yet known to us, maybe due to lazy loading. Try creating it.
        if (user == null)
        {
            // If this presence update doesn't have a user or the status is offline we ignore it
            if (jsonUser.isNull("username") || "offline".equals(content.get("status")))
                return null;
            // We should have somewhat enough information to create this member, so lets do it!
            user = (UserImpl) createMember(content, guildId, guild, jsonUser).getUser();
        }

        if (jsonUser.hasKey("username"))
        {
            // username implies this is an update to a user - fire events and update properties
            getJDA().getEntityBuilder().updateUser(user, jsonUser);
        }

        //Now that we've update the User's info, lets see if we need to set the specific Presence information.
        // This is stored in the Member objects.
        //We set the activities to null to prevent parsing if the cache was disabled
        final DataArray activityArray = !getJDA().isCacheFlagSet(CacheFlag.ACTIVITY) || content.isNull("activities") ? null : content.getArray("activities");
        List<Activity> newActivities = new ArrayList<>();
        boolean parsedActivity = parseActivities(userId, activityArray, newActivities);

        MemberImpl member = (MemberImpl) guild.getMember(user);
        //Create member from presence if not offline
        if (member == null)
        {
            if (jsonUser.isNull("username") || "offline".equals(content.get("status")))
            {
                log.trace("Ignoring incomplete PRESENCE_UPDATE for member with id {} in guild with id {}", userId, guildId);
                return null;
            }
            member = createMember(content, guildId, guild, jsonUser);
        }

        if (getJDA().isCacheFlagSet(CacheFlag.CLIENT_STATUS) && !content.isNull("client_status"))
            handleClientStatus(content, member);

        // Check if activities changed
        if (parsedActivity)
            handleActivities(newActivities, member);

        //The member is already cached, so modify the presence values and fire events as needed.
        OnlineStatus status = OnlineStatus.fromKey(content.getString("status"));
        if (!member.getOnlineStatus().equals(status))
        {
            OnlineStatus oldStatus = member.getOnlineStatus();
            member.setOnlineStatus(status);
            getJDA().getEntityBuilder().updateMemberCache(member);
            getJDA().handleEvent(
                new UserUpdateOnlineStatusEvent(
                    getJDA(), responseNumber,
                    member, oldStatus));
        }
        return null;
    }

    private boolean parseActivities(long userId, DataArray activityArray, List<Activity> newActivities)
    {
        boolean parsedActivity = false;
        try
        {
            if (activityArray != null)
            {
                for (int i = 0; i < activityArray.length(); i++)
                    newActivities.add(EntityBuilder.createActivity(activityArray.getObject(i)));
                parsedActivity = true;
            }
        }
        catch (Exception ex)
        {
            if (EntityBuilder.LOG.isDebugEnabled())
                EntityBuilder.LOG.warn("Encountered exception trying to parse a presence! UserID: {} JSON: {}", userId, activityArray, ex);
            else
                EntityBuilder.LOG.warn("Encountered exception trying to parse a presence! UserID: {} Message: {} Enable debug for details", userId, ex.getMessage());
        }
        return parsedActivity;
    }

    private MemberImpl createMember(DataObject content, long guildId, GuildImpl guild, DataObject jsonUser)
    {
        DataObject memberJson = DataObject.empty();

        String nick = content.opt("nick").map(Object::toString).orElse(null);
        DataArray roles = content.optArray("roles").orElse(null);
        String onlineStatus = content.getString("status");
        // unfortunately this information is missing
        String joinDate = content.getString("joined_at", null);

        memberJson.put("user", jsonUser)
                  .put("status", onlineStatus)
                  .put("roles", roles)
                  .put("nick", nick)
                  .put("joined_at", joinDate);
        log.trace("Creating member from PRESENCE_UPDATE for userId: {} and guildId: {}", jsonUser.getUnsignedLong("id"), guild.getId());
        return getJDA().getEntityBuilder().createMember(guild, memberJson);
    }

    private void handleActivities(List<Activity> newActivities, MemberImpl member)
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
            getJDA().getEntityBuilder().updateMemberCache(member);
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

    private void handleClientStatus(DataObject content, MemberImpl member)
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
}
