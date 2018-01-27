/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
import net.dv8tion.jda.client.entities.impl.CallUserImpl;
import net.dv8tion.jda.client.entities.impl.GroupImpl;
import net.dv8tion.jda.client.events.group.GroupUserJoinEvent;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.handle.EventCache;
import net.dv8tion.jda.core.handle.SocketHandler;
import org.json.JSONObject;

public class ChannelRecipientAddHandler extends SocketHandler
{
    public ChannelRecipientAddHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        final long groupId = content.getLong("channel_id");
        JSONObject userJson = content.getJSONObject("user");

        GroupImpl group = (GroupImpl) api.asClient().getGroupById(groupId);
        if (group == null)
        {
            api.getEventCache().cache(EventCache.Type.CHANNEL, groupId, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received a CHANNEL_RECIPIENT_ADD for a group that is not yet cached! JSON: {}", content);
            return null;
        }

        User user = api.getEntityBuilder().createFakeUser(userJson, true);
        group.getUserMap().put(user.getIdLong(), user);

        CallImpl call = (CallImpl) group.getCurrentCall();
        if (call != null)
        {
            call.getCallUserMap().put(user.getIdLong(), new CallUserImpl(call, user));
        }

        api.getEventManager().handle(
                new GroupUserJoinEvent(
                        api, responseNumber,
                        group, user));

        api.getEventCache().playbackCache(EventCache.Type.USER, user.getIdLong());
        return null;
    }
}
