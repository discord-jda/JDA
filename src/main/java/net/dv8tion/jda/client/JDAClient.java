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

package net.dv8tion.jda.client;

import net.dv8tion.jda.client.entities.*;
import net.dv8tion.jda.client.requests.restaction.ApplicationAction;
import net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.cache.SnowflakeCacheView;

import javax.annotation.CheckReturnValue;
import java.util.List;

public interface JDAClient
{
    JDA getJDA();

    SnowflakeCacheView<Group> getGroupCache();
    default List<Group> getGroups()
    {
        return getGroupCache().asList();
    }
    default List<Group> getGroupsByName(String name, boolean ignoreCase)
    {
        return getGroupCache().getElementsByName(name, ignoreCase);
    }
    default Group getGroupById(String id)
    {
        return getGroupCache().getElementById(id);
    }
    default Group getGroupById(long id)
    {
        return getGroupCache().getElementById(id);
    }

    List<Relationship> getRelationships();
    List<Relationship> getRelationships(RelationshipType type);
    List<Relationship> getRelationships(RelationshipType type, String name, boolean ignoreCase);
    List<Relationship> getRelationshipsByName(String name, boolean ignoreCase);
    Relationship getRelationship(User user);
    Relationship getRelationship(Member member);
    Relationship getRelationshipById(String id);
    Relationship getRelationshipById(long id);
    Relationship getRelationshipById(String id, RelationshipType type);
    Relationship getRelationshipById(long id, RelationshipType type);

    List<Friend> getFriends();
    List<Friend> getFriendsByName(String name, boolean ignoreCase);
    Friend getFriend(User user);
    Friend getFriend(Member member);
    Friend getFriendById(String id);
    Friend getFriendById(long id);

    /**
     * Retrieves the recent mentions for the currently logged in
     * client account.
     *
     * <p>The returned {@link net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction MentionPaginationAction}
     * allows to filter by whether the messages mention everyone or a role.
     *
     * @return {@link net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction MentionPaginationAction}
     */
    @CheckReturnValue
    MentionPaginationAction getRecentMentions();

    /**
     * Retrieves the recent mentions for the currently logged in
     * client account.
     *
     * <p>The returned {@link net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction MentionPaginationAction}
     * allows to filter by whether the messages mention everyone or a role.
     *
     * <p><b>To target recent mentions from all over Discord use {@link #getRecentMentions()} instead!</b>
     *
     * @param  guild
     *         The {@link net.dv8tion.jda.core.entities.Guild Guild} to narrow recent mentions to
     *
     * @throws java.lang.IllegalArgumentException
     *         If the specified Guild is {@code null}
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the specified Guild is not currently {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction MentionPaginationAction}
     */
    @CheckReturnValue
    MentionPaginationAction getRecentMentions(Guild guild);

    UserSettings getSettings();

    /**
     * Creates a new {@link net.dv8tion.jda.client.entities.Application Application} for this user account
     * with the given name.
     *
     * <p>A name <b>must not</b> be {@code null} nor less than 2 characters or more than 32 characters long!
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MAX_OAUTH_APPS MAX_OAUTH_APPS}
     *     <br>OAuth2 application limit reached</li>
     * </ul>
     *
     * @param  name
     *         The name for new {@link net.dv8tion.jda.client.entities.Application Application}
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null}, less than 2 or more than 32 characters long
     * 
     * @return A specific {@link net.dv8tion.jda.client.requests.restaction.ApplicationAction ApplicationAction}
     *         <br>This action allows to set fields for the new application before creating it
     */
    @CheckReturnValue
    ApplicationAction createApplication(String name);

    /**
     * Retrieves all {@link net.dv8tion.jda.client.entities.Application Applications} owned by this user account.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: 
     *         {@link java.util.List List}{@literal <}{@link net.dv8tion.jda.client.entities.Application Application}{@literal >}
     *         <br>A list of all Applications owned by this user account.
     */
    @CheckReturnValue
    RestAction<List<Application>> getApplications();

    /**
     * Retrieves a specific {@link net.dv8tion.jda.client.entities.Application Application} owned by this user account.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_APPLICATION UNKNOWN_APPLICATION}
     *     <br>The Application did not exist (possibly deleted).</li>
     * </ul>
     *
     * @param  id
     *         The id for the {@link net.dv8tion.jda.client.entities.Application Application}
     * 
     * @throws IllegalArgumentException
     *         If the provided id is {@code null} or empty
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.client.entities.Application Application}
     *         <br>The Application behind the provided id.
     */
    @CheckReturnValue
    RestAction<Application> getApplicationById(String id);

    /**
     * Retrieves all {@link net.dv8tion.jda.client.entities.AuthorizedApplication AuthorizedApplications} authorized by this user account.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: 
     *         List{@literal <}{@link net.dv8tion.jda.client.entities.AuthorizedApplication AuthorizedApplication}{@literal >}
     *         <br>A list of all AuthorizedApplications authorized by this user account.
     */
    @CheckReturnValue
    RestAction<List<AuthorizedApplication>> getAuthorizedApplications();

    /**
     * Retrieves a specific {@link net.dv8tion.jda.client.entities.AuthorizedApplication AuthorizedApplication} authorized by this user account.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_TOKEN UNKNOWN_TOKEN}
     *     <br>The Application either doesn't exist or isn't authorized by this user account.</li>
     * </ul>
     *
     * @param  id
     *         The id of the {@link net.dv8tion.jda.client.entities.AuthorizedApplication AuthorizedApplication}
     * 
     * @throws IllegalArgumentException If the provided id is {@code null} or empty
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: 
     *         {@link net.dv8tion.jda.client.entities.AuthorizedApplication AuthorizedApplication}
     *         <br>The Application behind the provided id.
     */
    @CheckReturnValue
    RestAction<AuthorizedApplication> getAuthorizedApplicationById(String id);
}
