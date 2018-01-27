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

import net.dv8tion.jda.client.entities.Relationship;
import net.dv8tion.jda.client.events.relationship.FriendAddedEvent;
import net.dv8tion.jda.client.events.relationship.FriendRequestReceivedEvent;
import net.dv8tion.jda.client.events.relationship.FriendRequestSentEvent;
import net.dv8tion.jda.client.events.relationship.UserBlockedEvent;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.handle.EventCache;
import net.dv8tion.jda.core.handle.SocketHandler;
import net.dv8tion.jda.core.requests.WebSocketClient;
import org.json.JSONObject;

public class RelationshipAddHandler extends SocketHandler
{
    public RelationshipAddHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        Relationship relationship = api.getEntityBuilder().createRelationship(content);
        if (relationship == null)
        {
            WebSocketClient.LOG.warn("Received a RELATIONSHIP_ADD with an unknown type! JSON: {}", content);
            return null;
        }
        switch (relationship.getType())
        {
            case FRIEND:
                api.getEventManager().handle(
                        new FriendAddedEvent(
                                api, responseNumber,
                                relationship));
                break;
            case BLOCKED:
                api.getEventManager().handle(
                        new UserBlockedEvent(
                                api, responseNumber,
                                relationship));
                break;
            case INCOMING_FRIEND_REQUEST:
                api.getEventManager().handle(
                        new FriendRequestReceivedEvent(
                                api, responseNumber,
                                relationship));
                break;
            case OUTGOING_FRIEND_REQUEST:
                api.getEventManager().handle(
                        new FriendRequestSentEvent(
                                api, responseNumber,
                                relationship));
                break;
            default:
                WebSocketClient.LOG.warn("Received a RELATIONSHIP_ADD with an unknown type! JSON: {}", content);
                return null;
        }
        api.getEventCache().playbackCache(EventCache.Type.RELATIONSHIP, relationship.getUser().getIdLong());
        api.getEventCache().playbackCache(EventCache.Type.USER, relationship.getUser().getIdLong());
        return null;
    }
}
