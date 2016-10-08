/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
import net.dv8tion.jda.client.entities.RelationshipType;
import net.dv8tion.jda.client.entities.impl.JDAClientImpl;
import net.dv8tion.jda.client.events.relationship.FriendRemovedEvent;
import net.dv8tion.jda.client.events.relationship.FriendRequestCanceledEvent;
import net.dv8tion.jda.client.events.relationship.FriendRequestIgnoredEvent;
import net.dv8tion.jda.client.events.relationship.UserUnblockedEvent;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.handle.EventCache;
import net.dv8tion.jda.core.handle.SocketHandler;
import net.dv8tion.jda.core.requests.WebSocketClient;
import org.json.JSONObject;

public class RelationshipRemoveHandler extends SocketHandler
{
    public RelationshipRemoveHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        String userId = content.getString("id");
        RelationshipType type = RelationshipType.fromKey(content.getInt("type"));

        //Technically this could be used to detect when another user has unblocked us,
        // but it seems like functionality that may change so I'm not supporting it.
        if (type == RelationshipType.NO_RELATIONSHIP)
            return null;

        Relationship relationship = api.asClient().getRelationshipById(userId, type);
        if (relationship == null)
        {
            EventCache.get(api).cache(EventCache.Type.RELATIONSHIP, userId, () ->
            {
                handle(responseNumber, allContent);
            });
            EventCache.LOG.debug("Received a RELATIONSHIP_REMOVE for a relationship that was not yet cached! JSON: " + content);
            return null;
        }
        ((JDAClientImpl) api.asClient()).getRelationshipMap().remove(userId);

        switch (type)
        {
            case FRIEND:
                api.getEventManager().handle(
                        new FriendRemovedEvent(
                                api, responseNumber,
                                relationship));
                break;
            case BLOCKED:
                api.getEventManager().handle(
                        new UserUnblockedEvent(
                                api, responseNumber,
                                relationship));
                break;
            case INCOMING_FRIEND_REQUEST:
                api.getEventManager().handle(
                        new FriendRequestIgnoredEvent(
                                api, responseNumber,
                                relationship));
                break;
            case OUTGOING_FRIEND_REQUEST:
                api.getEventManager().handle(
                        new FriendRequestCanceledEvent(
                                api, responseNumber,
                                relationship));
                break;
            default:
                WebSocketClient.LOG.warn("Received a RELATIONSHIP_REMOVE with an unknown RelationshipType! JSON: " + content);
                return null;
        }
        return null;
    }
}
