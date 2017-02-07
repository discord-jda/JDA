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

import net.dv8tion.jda.client.entities.CallUser;
import net.dv8tion.jda.client.entities.CallableChannel;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.client.entities.impl.*;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.PrivateChannelImpl;
import net.dv8tion.jda.core.handle.EventCache;
import net.dv8tion.jda.core.handle.SocketHandler;
import net.dv8tion.jda.core.requests.WebSocketClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class CallCreateHandler extends SocketHandler
{
    public CallCreateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        String channelId = content.getString("channel_id");
        String messageId = content.getString("message_id");
        Region region = Region.fromKey(content.getString("region"));
        JSONArray voiceStates = content.getJSONArray("voice_states");
        JSONArray ringing = content.getJSONArray("ringing");

        CallableChannel channel = api.asClient().getGroupById(channelId);
        if (channel == null)
            channel = api.getPrivateChannelMap().get(channelId);
        if (channel == null)
        {
            EventCache.get(api).cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received a CALL_CREATE for a Group/PrivateChannel that is not yet cached. JSON: " + content);
            return null;
        }

        CallImpl call = new CallImpl(channel, messageId);
        call.setRegion(region);
        HashMap<String, CallUser> callUsers = call.getCallUserMap();

        if (channel instanceof Group)
        {
            GroupImpl group = (GroupImpl) channel;
            if (group.getCurrentCall() != null)
                WebSocketClient.LOG.fatal("Received a CALL_CREATE for a Group that already has an active call cached! JSON: " + content);
            group.setCurrentCall(call);
            group.getUserMap().forEach((userId, user) ->
            {
                CallUserImpl callUser = new CallUserImpl(call, user);
                callUsers.put(userId, callUser);

                for (int i = 0; i < ringing.length(); i++)
                {
                    if (ringing.getString(i).equals(userId))
                    {
                        callUser.setRinging(true);
                        break;
                    }
                }
            });
        }
        else
        {
            PrivateChannelImpl priv = (PrivateChannelImpl) channel;
            if (priv.getCurrentCall() != null)
                WebSocketClient.LOG.fatal("Received a CALL_CREATE for a PrivateChannel that already has an active call cached! JSON: " + content);
            priv.setCurrentCall(call);
            callUsers.put(priv.getUser().getId(), new CallUserImpl(call, priv.getUser()));
            callUsers.put(api.getSelfUser().getId(), new CallUserImpl(call, api.getSelfUser()));
        }

        for (int i = 0; i < voiceStates.length(); i++)
        {
            JSONObject voiceState = voiceStates.getJSONObject(i);
            String userId = voiceState.getString("user_id");
            CallUser cUser = callUsers.get(userId);
            CallVoiceStateImpl vState = (CallVoiceStateImpl) cUser.getVoiceState();

            vState.setInCall(true);
            vState.setSessionId(voiceState.getString("session_id"));
            vState.setSelfMuted(voiceState.getBoolean("self_mute"));
            vState.setSelfDeafened(voiceState.getBoolean("self_deaf"));

            ((JDAClientImpl) api.asClient()).getCallUserMap().put(userId, cUser);
        }
        EventCache.get(api).playbackCache(EventCache.Type.CALL, channelId);
        return null;
    }
}
