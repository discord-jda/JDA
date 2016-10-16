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

package net.dv8tion.jda.client.managers;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.impl.EmoteImpl;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Set;

/**
 * Used to modify multiple properties at once or delete an Emote.<p>
 * <b>This is a <u>client only</u> function!</b>
 */
public class EmoteManagerUpdatable
{

    private Set<Role> roles = null;
    private String name = null;
    private final EmoteImpl emote;
    private EmoteManager manager = null;

    public EmoteManagerUpdatable(EmoteImpl emote)
    {
        this.emote = emote;
    }

    /**
     * Sets the name of this Emote<p>
     * <b>Must call {@link #update()} to finalize changes.</b>
     *
     * @param name
     *      The name to set for this Emote (null to keep current name)
     * @return
     *      Current instance of this Manager for chaining convenience.
     */
    public EmoteManagerUpdatable setName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Set roles this emote is active for.<p>
     * <b>Must call {@link #update()} to finalize changes.</b>
     *
     * @param roles
     *      A set of roles (all within the same guild the emote is in) / null to keep current roles
     * @return
     *      Current instance of this Manager for chaining convenience.
     */
    public EmoteManagerUpdatable setRoles(Set<Role> roles)
    {
        this.roles = roles;
        return this;
    }

    /**
     * Deletes this Emote.<p>
     * <b>This is a <u>client only</u> function!</b>
     *
     * @return
     *      An {@link net.dv8tion.jda.core.requests.RestAction RestAction&lt;Void&gt;} (to complete operation append .queue(...) or .block(...))
     * @throws AccountTypeException
     *      if the current AccountType is not Client
     * @throws PermissionException
     *      if either the Emote trying to delete is fake or we do not have the required Permissions to delete this emote
     */
    public RestAction<Void> delete()
    {
        checkAccess();
        return new RestAction<Void>(getJDA(), Route.Custom.DELETE_ROUTE.compile(String.format("guilds/%s/emojis/%s",
                emote.getGuild().getId(), emote.getGuild().getId()), emote.getId()), null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                if (response.isRateLimit())
                    request.onFailure(new RateLimitedException(request.getRoute(), response.retryAfter));
                else if (response.isError())
                    request.onFailure(response.exception);
            }
        };
    }

    /**
     * Updates the Emote of this Manager with the values set with the intermediate Methods. (set- Name/Roles)<p>
     * <b>This is a <u>client only</u> function!</b>
     *
     * @return
     *      An {@link net.dv8tion.jda.core.requests.RestAction RestAction&lt;Void&gt;} (to complete operation append .queue(...) or .block(...))
     * @throws AccountTypeException
     *      if the current AccountType is not Client
     * @throws PermissionException
     *      if either the Emote trying to update is fake or we do not have the required Permissions to update this emote
     * @throws IllegalArgumentException
     *      if the specified name in {@link #setName(String)} has less than 2 chars or more than 32 chars.
     */
    public RestAction<Void> update()
    {
        checkAccess();
        if (name != null && (name.length() < 2 || name.length() > 32))
            throw new IllegalArgumentException("Name exceeds char limit. [2 <= x <= 32]");
        JSONObject body = new JSONObject();
        if (name != null)
            body.put("name", name);
        if (roles != null)
            body.put("roles", new JSONArray(Arrays.toString(roles.parallelStream().map(ISnowflake::getId).toArray())));

        reset(); //reset because we built the JSONObject needed to update
        return new RestAction<Void>(getJDA(), Route.Custom.PATCH_ROUTE.compile(String.format("guilds/%s/emojis/%s", emote.getGuild().getId(), emote.getId())), body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                if (response.isRateLimit())
                    request.onFailure(new RateLimitedException(request.getRoute(), response.retryAfter));
                else if (response.isError())
                    request.onFailure(response.exception);
            }
        };
    }

    /**
     * Resets this Manager to default values.
     */
    public void reset()
    {
        roles = null;
        name = null;
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Emote
     *
     * @return
     *      The JDA instance of this Emote
     */
    public JDA getJDA()
    {
        return emote.getJDA();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Emote Emote} represented by this Manager.
     *
     * @return
     *      The Emote
     */
    public Emote getEmote()
    {
        return emote;
    }

    /**
     * The {@link net.dv8tion.jda.client.managers.EmoteManager Manager} for this emote<p>
     * With the EmoteManager returned you can modify this Emote's properties or delete it.
     *
     * @return
     *      The EmoteManager for this Emote
     */
    public synchronized EmoteManager asManager()
    {
        if (manager == null)
            manager = new EmoteManager(emote);
        return manager;
    }

    private void checkAccess()
    {
        if (getJDA().getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT);
        if (emote.isFake())
            throw new PermissionException("Trying to update a fake emote.");
        if (!PermissionUtil.checkPermission(emote.getGuild(),
                emote.getGuild().getMemberById(emote.getJDA().getSelfInfo().getId()), Permission.getFromOffset(30)))
            throw new PermissionException(Permission.getFromOffset(30));
    }

}
