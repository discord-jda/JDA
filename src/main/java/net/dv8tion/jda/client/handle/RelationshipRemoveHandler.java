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

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.client.entities.Relationship;
import net.dv8tion.jda.client.entities.RelationshipType;
import net.dv8tion.jda.client.entities.impl.JDAClientImpl;
import net.dv8tion.jda.client.events.relationship.FriendRemovedEvent;
import net.dv8tion.jda.client.events.relationship.FriendRequestCanceledEvent;
import net.dv8tion.jda.client.events.relationship.FriendRequestIgnoredEvent;
import net.dv8tion.jda.client.events.relationship.UserUnblockedEvent;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.PrivateChannelImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;
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
    protected Long handleInternally(JSONObject content)
    {
        final long userId = content.getLong("id");
        RelationshipType type = RelationshipType.fromKey(content.getInt("type"));

        //Technically this could be used to detect when another user has unblocked us,
        // but it seems like functionality that may change so I'm not supporting it.
        if (type == RelationshipType.NO_RELATIONSHIP)
            return null;

        //Make sure that we get the proper relationship, not just any one cached by this userId.
        //Deals with possibly out of order RELATIONSHIP_REMOVE and RELATIONSHIP_ADD when blocking a Friend.
        Relationship relationship = api.asClient().getRelationshipById(userId, type);
        if (relationship == null)
        {
            api.getEventCache().cache(EventCache.Type.RELATIONSHIP, userId, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received a RELATIONSHIP_REMOVE for a relationship that was not yet cached! JSON: {}", content);
            return null;
        }
        api.asClient().getRelationshipMap().remove(userId);

        if (relationship.getType() == RelationshipType.FRIEND)
        {
            //The user is not in a different guild that we share
            if (api.getGuildMap().valueCollection().stream().noneMatch(g -> ((GuildImpl) g).getMembersMap().containsKey(userId)))
            {
                UserImpl user = (UserImpl) api.getUserMap().remove(userId);
                if (user.hasPrivateChannel())
                {
                    PrivateChannelImpl priv = (PrivateChannelImpl) user.getPrivateChannel();
                    user.setFake(true);
                    priv.setFake(true);
                    api.getFakeUserMap().put(user.getIdLong(), user);
                    api.getFakePrivateChannelMap().put(priv.getIdLong(), priv);
                }
                else
                {
                    //While the user might not have a private channel, if this is a client account then the user
                    // could be in a Group, and if so we need to change the User object to be fake and
                    // place it in the FakeUserMap
                    for (Group grp : api.asClient().getGroups())
                    {
                        if (grp.getNonFriendUsers().contains(user))
                        {
                            user.setFake(true);
                            api.getFakeUserMap().put(user.getIdLong(), user);
                            break;
                        }
                    }
                }
                api.getEventCache().clear(EventCache.Type.USER, userId);
            }
        }
        else
        {
            //Checks that the user is fake, has no privateChannel,and is not in any other groups
            // then we remove the fake user from the fake cache as it was only in this group
            //Note: we getGroups() which gets all groups, however we already removed the user from the current group.
            User user = relationship.getUser();
            if (user.isFake()
                    && !user.hasPrivateChannel()
                    && api.asClient().getGroups().stream().noneMatch(g -> g.getUsers().contains(user)))
            {
                api.getFakeUserMap().remove(userId);
            }
        }

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
                WebSocketClient.LOG.warn("Received a RELATIONSHIP_REMOVE with an unknown RelationshipType! JSON: {}", content);
                return null;
        }
        api.getEventCache().clear(EventCache.Type.RELATIONSHIP, userId);
        return null;
    }
}
