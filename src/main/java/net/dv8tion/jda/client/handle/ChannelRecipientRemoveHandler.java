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

package net.dv8tion.jda.client.handle;

import net.dv8tion.jda.client.entities.impl.CallImpl;
import net.dv8tion.jda.client.entities.impl.GroupImpl;
import net.dv8tion.jda.client.events.group.GroupUserLeaveEvent;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.handle.EventCache;
import net.dv8tion.jda.core.handle.SocketHandler;
import org.json.JSONObject;

public class ChannelRecipientRemoveHandler extends SocketHandler
{
    public ChannelRecipientRemoveHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        String groupId = content.getString("channel_id");
        String userId = content.getJSONObject("user").getString("id");

        GroupImpl group = (GroupImpl) api.asClient().getGroupById(groupId);
        if (group == null)
        {
            EventCache.get(api).cache(EventCache.Type.CHANNEL, groupId, () ->
            {
                handle(responseNumber, allContent);
            });
            EventCache.LOG.debug("Received a CHANNEL_RECIPIENT_REMOVE for a group that is not yet cached! JSON: " + content);
            return null;
        }

        User user = group.getUserMap().remove(userId);
        if (user == null)
        {
            EventCache.get(api).cache(EventCache.Type.USER, userId, () ->
            {
                handle(responseNumber, allContent);
            });
            EventCache.LOG.debug("Received a CHANNEL_RECIPIENT_REMOVE for a user that is not yet cached in the group! JSON: " + content);
            return null;
        }

        CallImpl call = (CallImpl) group.getCurrentCall();
        if (call != null)
        {
            call.getCallUserMap().remove(userId);
        }

        //User is fake, has no privateChannel, is not in a relationship, and is not in any other groups
        // then we remove the fake user from the fake cache as it was only in this group
        //Note: we getGroups() which gets all groups, however we already removed the user from the current group.
        if (user.isFake()
                && !user.hasPrivateChannel()
                && api.asClient().getRelationshipById(userId) == null
                && api.asClient().getGroups().stream().allMatch(g -> !g.getUsers().contains(user)))
        {
            api.getFakeUserMap().remove(userId);
        }
        api.getEventManager().handle(
                new GroupUserLeaveEvent(
                        api, responseNumber,
                        group, user));
        return null;
    }
}
