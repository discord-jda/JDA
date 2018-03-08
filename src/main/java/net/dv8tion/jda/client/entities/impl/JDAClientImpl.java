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

package net.dv8tion.jda.client.entities.impl;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.client.JDAClient;
import net.dv8tion.jda.client.entities.*;
import net.dv8tion.jda.client.requests.restaction.ApplicationAction;
import net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;
import net.dv8tion.jda.core.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.core.utils.cache.impl.SnowflakeCacheViewImpl;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JDAClientImpl implements JDAClient
{
    protected final JDAImpl api;
    protected final SnowflakeCacheViewImpl<Group> groups = new SnowflakeCacheViewImpl<>(Group.class, Group::getName);
    protected final TLongObjectMap<Relationship> relationships = MiscUtil.newLongMap();
    protected final TLongObjectMap<CallUser> callUsers = MiscUtil.newLongMap();
    protected UserSettingsImpl userSettings;

    public JDAClientImpl(JDAImpl api)
    {
        this.api = api;
        this.userSettings = new UserSettingsImpl(api);
    }

    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public SnowflakeCacheView<Group> getGroupCache()
    {
        return groups;
    }

    @Override
    public List<Relationship> getRelationships()
    {
        return Collections.unmodifiableList(
                new ArrayList<>(
                        relationships.valueCollection()));
    }

    @Override
    public List<Relationship> getRelationships(RelationshipType type)
    {
        return Collections.unmodifiableList(relationships.valueCollection().stream()
                .filter(r -> r.getType().equals(type))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Relationship> getRelationships(RelationshipType type, String name, boolean ignoreCase)
    {
        return Collections.unmodifiableList(relationships.valueCollection().stream()
                .filter(r -> r.getType().equals(type))
                .filter(r -> (ignoreCase
                        ? r.getUser().getName().equalsIgnoreCase(name)
                        : r.getUser().getName().equals(name)))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Relationship> getRelationshipsByName(String name, boolean ignoreCase)
    {
        return Collections.unmodifiableList(relationships.valueCollection().stream()
                .filter(r -> (ignoreCase
                        ? r.getUser().getName().equalsIgnoreCase(name)
                        : r.getUser().getName().equals(name)))
                .collect(Collectors.toList()));
    }

    @Override
    public Relationship getRelationship(User user)
    {
        return getRelationshipById(user.getIdLong());
    }

    @Override
    public Relationship getRelationship(Member member)
    {
        return getRelationship(member.getUser());
    }

    @Override
    public Relationship getRelationshipById(String id)
    {
        return relationships.get(MiscUtil.parseSnowflake(id));
    }

    @Override
    public Relationship getRelationshipById(long id)
    {
        return relationships.get(id);
    }

    @Override
    public Relationship getRelationshipById(String id, RelationshipType type)
    {
        Relationship relationship = getRelationshipById(id);
        if (relationship != null && relationship.getType() == type)
            return relationship;
        else
            return null;
    }

    @Override
    public Relationship getRelationshipById(long id, RelationshipType type)
    {
        Relationship relationship = getRelationshipById(id);
        if (relationship != null && relationship.getType() == type)
            return relationship;
        else
            return null;
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<Friend> getFriends()
    {
        return (List<Friend>) (List) getRelationships(RelationshipType.FRIEND);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Friend> getFriendsByName(String name, boolean ignoreCase)
    {
        return (List<Friend>) (List) getRelationships(RelationshipType.FRIEND, name, ignoreCase);
    }

    @Override
    public Friend getFriend(User user)
    {
        return getFriendById(user.getIdLong());
    }

    @Override
    public Friend getFriend(Member member)
    {
        return getFriend(member.getUser());
    }

    @Override
    public Friend getFriendById(String id)
    {
        return (Friend) getRelationshipById(id, RelationshipType.FRIEND);
    }

    @Override
    public Friend getFriendById(long id)
    {
        return (Friend) getRelationshipById(id, RelationshipType.FRIEND);
    }

    @Override
    public MentionPaginationAction getRecentMentions()
    {
        return new MentionPaginationAction(getJDA());
    }

    @Override
    public MentionPaginationAction getRecentMentions(Guild guild)
    {
        Checks.notNull(guild, "Guild");
        if (!guild.isAvailable())
            throw new GuildUnavailableException("Cannot retrieve recent mentions for this Guild due to it being temporarily unavailable!");
        return new MentionPaginationAction(guild);
    }

    @Override
    public UserSettings getSettings()
    {
        return userSettings;
    }

    public TLongObjectMap<Group> getGroupMap()
    {
        return groups.getMap();
    }

    public TLongObjectMap<Relationship> getRelationshipMap()
    {
        return relationships;
    }

    public TLongObjectMap<CallUser> getCallUserMap()
    {
        return callUsers;
    }

    @Override
    public ApplicationAction createApplication(String name)
    {
        return new ApplicationAction(api, name);
    }

    @Override
    public RestAction<List<Application>> getApplications()
    {
        Route.CompiledRoute route = Route.Applications.GET_APPLICATIONS.compile();
        return new RestAction<List<Application>>(api, route)
        {
            @Override
            protected void handleResponse(Response response, Request<List<Application>> request)
            {
                if (response.isOk())
                {
                    JSONArray array = response.getArray();
                    List<Application> applications = new ArrayList<>(array.length());
                    EntityBuilder entityBuilder = api.getEntityBuilder();

                    for (int i = 0; i < array.length(); i++)
                        applications.add(entityBuilder.createApplication(array.getJSONObject(i)));

                    request.onSuccess(Collections.unmodifiableList(applications));
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public RestAction<Application> getApplicationById(String id)
    {
        Checks.notEmpty(id, "id");

        Route.CompiledRoute route = Route.Applications.GET_APPLICATION.compile(id);
        return new RestAction<Application>(api, route)
        {
            @Override
            protected void handleResponse(Response response, Request<Application> request)
            {
                if (response.isOk())
                    request.onSuccess(api.getEntityBuilder().createApplication(response.getObject()));
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<List<AuthorizedApplication>> getAuthorizedApplications()
    {
        Route.CompiledRoute route = Route.Applications.GET_AUTHORIZED_APPLICATIONS.compile();
        return new RestAction<List<AuthorizedApplication>>(api, route)
        {
            @Override
            protected void handleResponse(Response response, Request<List<AuthorizedApplication>> request)
            {
                if (response.isOk())
                {
                    JSONArray array = response.getArray();
                    List<AuthorizedApplication> applications = new ArrayList<>(array.length());
                    EntityBuilder entityBuilder = api.getEntityBuilder();

                    for (int i = 0; i < array.length(); i++)
                        applications.add(entityBuilder.createAuthorizedApplication(array.getJSONObject(i)));

                    request.onSuccess(Collections.unmodifiableList(applications));
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public RestAction<AuthorizedApplication> getAuthorizedApplicationById(String id)
    {
        Checks.notEmpty(id, "id");

        Route.CompiledRoute route = Route.Applications.GET_AUTHORIZED_APPLICATION.compile(id);
        return new RestAction<AuthorizedApplication>(api, route)
        {
            @Override
            protected void handleResponse(Response response, Request<AuthorizedApplication> request)
            {
                if (response.isOk())
                    request.onSuccess(api.getEntityBuilder().createAuthorizedApplication(response.getObject()));
                else
                    request.onFailure(response);
            }
        };
    }
}
