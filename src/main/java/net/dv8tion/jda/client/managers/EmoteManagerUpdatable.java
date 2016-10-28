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

import net.dv8tion.jda.client.managers.fields.EmoteField;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.impl.EmoteImpl;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.json.JSONObject;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Used to modify multiple properties at once or delete an Emote.<p>
 * <b>This is a <u>client only</u> function!</b>
 */
public class EmoteManagerUpdatable
{
    public static final Pattern NAME_PATTERN = Pattern.compile("^\\w+$");

    protected final EmoteImpl emote;

    protected EmoteField<String> name;
    protected EmoteField<Collection<Role>> roles;

    public EmoteManagerUpdatable(EmoteImpl emote)
    {
        if (emote.getJDA().getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT);
        if (emote.isFake())
            throw new IllegalStateException("The emote you are trying to update is not an actual emote we have access to (it is fake)!");
        if (emote.isManaged())
            throw new IllegalStateException("You cannot modify a managed emote!");
        this.emote = emote;
        setupFields();
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
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} this emote is in
     *
     * @return
     *      The Guild of the respected Emote
     */
    public Guild getGuild()
    {
        return emote.getGuild();
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
     * Sets the name of this Emote<p>
     * <b>Must call {@link #update()} to finalize changes.</b>
     *
     * @param name
     *      The name to set for this Emote (null to keep current name)
     * @return
     *      Current instance of this Manager for chaining convenience.
     */
    public EmoteField<String> getNameField()
    {
        return name;
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
    public EmoteField<Collection<Role>> getRolesField()
    {
        return roles;
    }

    /**
     * Resets this Manager to default values.
     */
    public void reset()
    {
        name.reset();
        roles.reset();
    }

    /**
     * Updates the Emote of this Manager with the values set with the intermediate Methods. (set- Name/Roles)<p>
     * <b>This is a <u>client only</u> function!</b>
     *
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: {@link java.lang.Void}<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: None
     * @throws PermissionException
     *      if we do not have the required Permissions to update this emote ({@link net.dv8tion.jda.core.Permission#MANAGE_EMOTES Permission.MANAGE_EMOTES})
     */
    public RestAction<Void> update()
    {
        checkPermission(Permission.MANAGE_EMOTES);

        if (!needsUpdate())
            return new RestAction.EmptyRestAction<>(null);

        JSONObject body = new JSONObject();

        if (name.shouldUpdate())
            body.put("name", name.getValue());
        if (roles.shouldUpdate())
            body.put("roles", roles.getValue().stream().map(ISnowflake::getId).collect(Collectors.toList()));

        reset(); //reset because we built the JSONObject needed to update
        Route.CompiledRoute route = Route.Emotes.MODIFY_EMOTE.compile(getGuild().getId(), emote.getId());
        return new RestAction<Void>(getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    protected boolean needsUpdate()
    {
        return name.shouldUpdate()
                || roles.shouldUpdate();
    }

    protected void checkPermission(Permission perm)
    {
        if (!PermissionUtil.checkPermission(getGuild(), getGuild().getSelfMember(), perm))
            throw new PermissionException(perm);
    }

    protected void setupFields()
    {
        name = new EmoteField<String>(this, emote::getName)
        {
            @Override
            public void checkValue(String value)
            {
                checkNull(value, "emote name");
                if (value.length() < 2 || value.length() > 32)
                    throw new IllegalArgumentException("Emote name must be 2 to 32 characters in length");

                Matcher nameMatcher = NAME_PATTERN.matcher(value);
                if (!nameMatcher.find())
                    throw new IllegalArgumentException("Provided name must be Alphanumeric characters and underscores. (a-z A-Z 0-9 _)");
            }
        };

        roles = new EmoteField<Collection<Role>>(this, emote::getRoles)
        {
            @Override
            public void checkValue(Collection<Role> value)
            {
                checkNull(value, "Role Collection");
                value.forEach(r -> checkNull(r, "Role in Collection"));
            }
        };
    }
}
