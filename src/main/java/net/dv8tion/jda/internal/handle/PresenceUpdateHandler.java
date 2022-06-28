/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import net.dv8tion.jda.api.events.user.update.UserUpdateActivitiesEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivityOrderEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.MemberPresenceImpl;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.dv8tion.jda.internal.utils.UnlockHook;
import org.slf4j.Logger;

import javax.annotation.Nullable;
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
        if (api.getCacheFlags().stream().noneMatch(CacheFlag::isPresence))
            return null;

        //Do a pre-check to see if this is for a Guild, and if it is, if the guild is currently locked or not cached.
        final long guildId = content.getUnsignedLong("guild_id");
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

        CacheView.SimpleCacheView<MemberPresenceImpl> presences = guild.getPresenceView();
        if (presences == null)
            return null; // technically this should be impossible
        DataObject jsonUser = content.getObject("user");
        final long userId = jsonUser.getUnsignedLong("id");
        MemberImpl member = (MemberImpl) guild.getMemberById(userId);
        MemberPresenceImpl presence = presences.get(userId);
        OnlineStatus status = OnlineStatus.fromKey(content.getString("status"));
        if (status == OnlineStatus.OFFLINE)
            presences.remove(userId);
        if (presence == null)
        {
            presence = new MemberPresenceImpl();
            if (status != OnlineStatus.OFFLINE)
            {
                try (UnlockHook lock = presences.writeLock())
                {
                    presences.getMap().put(userId, presence);
                }
            }
        }

        //Now that we've update the User's info, lets see if we need to set the specific Presence information.
        // This is stored in the Member objects.
        //We set the activities to null to prevent parsing if the cache was disabled
        final DataArray activityArray = !getJDA().isCacheFlagSet(CacheFlag.ACTIVITY) || content.isNull("activities") ? null : content.getArray("activities");
        List<Activity> newActivities = new ArrayList<>();
        boolean parsedActivity = parseActivities(userId, activityArray, newActivities);

        if (getJDA().isCacheFlagSet(CacheFlag.CLIENT_STATUS) && !content.isNull("client_status"))
            handleClientStatus(content, presence);

        // Check if activities changed
        if (parsedActivity)
            handleActivities(newActivities, member, presence);

        //The member is already cached, so modify the presence values and fire events as needed.

        if (presence.getOnlineStatus() != status)
        {
            OnlineStatus oldStatus = presence.getOnlineStatus();
            presence.setOnlineStatus(status);
            if (member != null)
            {
                getJDA().getEntityBuilder().updateMemberCache(member);
                getJDA().handleEvent(
                    new UserUpdateOnlineStatusEvent(
                        getJDA(), responseNumber,
                        member, oldStatus));
            }
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

    private void handleActivities(List<Activity> newActivities, @Nullable MemberImpl member, MemberPresenceImpl presence)
    {
        List<Activity> oldActivities = presence.getActivities();
        presence.setActivities(newActivities);
        if (member == null)
            return;
        boolean unorderedEquals = Helpers.deepEqualsUnordered(oldActivities, newActivities);
        if (unorderedEquals)
        {
            boolean deepEquals = Helpers.deepEquals(oldActivities, newActivities);
            if (!deepEquals)
            {
                getJDA().handleEvent(
                    new UserUpdateActivityOrderEvent(
                        getJDA(), responseNumber,
                        oldActivities, member));
            }
        }
        else
        {
            getJDA().getEntityBuilder().updateMemberCache(member);
            List<Activity> stoppedActivities = new ArrayList<>(oldActivities); // create modifiable copy
            List<Activity> startedActivities = new ArrayList<>();
            for (Activity activity : newActivities)
            {
                if (!stoppedActivities.remove(activity))
                    startedActivities.add(activity);
            }

            for (Activity activity : startedActivities)
            {
                getJDA().handleEvent(
                    new UserActivityStartEvent(
                        getJDA(), responseNumber,
                        member, activity));
            }

            for (Activity activity : stoppedActivities)
            {
                getJDA().handleEvent(
                    new UserActivityEndEvent(
                        getJDA(), responseNumber,
                        member, activity));
            }

            getJDA().handleEvent(
                new UserUpdateActivitiesEvent(
                    getJDA(), responseNumber,
                    member, oldActivities));
        }
    }

    private void handleClientStatus(DataObject content, MemberPresenceImpl presence)
    {
        DataObject json = content.getObject("client_status");
        EnumSet<ClientType> types = EnumSet.of(ClientType.UNKNOWN);
        for (String key : json.keys())
        {
            ClientType type = ClientType.fromKey(key);
            types.add(type);
            String raw = String.valueOf(json.get(key));
            OnlineStatus clientStatus = OnlineStatus.fromKey(raw);
            presence.setOnlineStatus(type, clientStatus);
        }
        for (ClientType type : EnumSet.complementOf(types))
            presence.setOnlineStatus(type, null); // set remaining types to offline
    }
}
