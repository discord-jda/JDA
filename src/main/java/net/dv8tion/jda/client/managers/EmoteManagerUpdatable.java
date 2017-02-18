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
import org.apache.http.util.Args;
import org.json.JSONObject;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An {@link #update() updatable} manager that allows
 * to modify emote settings like the {@link #getNameField() name}.
 *
 * <p>This manager allows to modify multiple fields at once
 * by getting the {@link net.dv8tion.jda.client.managers.fields.EmoteField EmoteField} for specific
 * properties and setting or resetting their values; followed by a call of {@link #update()}!
 *
 * <p>The {@link net.dv8tion.jda.client.managers.EmoteManager EmoteManager} implementation
 * simplifies this process by giving simple setters that return the {@link #update() update} {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 *
 * <p><b>Note</b>: To {@link #update() update} this manager
 * the currently logged in account requires the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_EMOTES MANAGE_EMOTES} and
 * must be from {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}
 */
public class EmoteManagerUpdatable
{
    public static final Pattern NAME_PATTERN = Pattern.compile("^\\w+$");

    protected final EmoteImpl emote;

    protected EmoteField<String> name;
    protected EmoteField<Collection<Role>> roles;

    /**
     * Creates a new EmoteManagerUpdatable instance
     *
     * @param  emote
     *         The target {@link net.dv8tion.jda.core.entities.impl.EmoteImpl EmoteImpl} to modify
     *
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *         If the currently logged in account is not from {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}
     * @throws java.lang.IllegalStateException
     *         If the specified Emote is {@link net.dv8tion.jda.core.entities.Emote#isFake() fake} or {@link net.dv8tion.jda.core.entities.Emote#isManaged() managed}.
     */
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
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Manager
     *
     * @return the corresponding JDA instance
     */
    public JDA getJDA()
    {
        return emote.getJDA();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} this Manager's
     * {@link net.dv8tion.jda.core.entities.Emote Emote} is in.
     * <br>This is logically the same as calling {@code getEmote().getGuild()}
     *
     * @return The parent {@link net.dv8tion.jda.core.entities.Guild Guild}
     */
    public Guild getGuild()
    {
        return emote.getGuild();
    }

    /**
     * The target {@link net.dv8tion.jda.core.entities.Emote Emote}
     * that will be modified by this Manager
     *
     * @return The target Emote
     */
    public Emote getEmote()
    {
        return emote;
    }

    /**
     * An {@link net.dv8tion.jda.client.managers.fields.EmoteField EmoteField}
     * for the <b><u>name</u></b> of the selected {@link net.dv8tion.jda.core.entities.Emote Emote}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(String)}
     * on the returned {@link net.dv8tion.jda.client.managers.fields.EmoteField EmoteField} instance.
     *
     * <p>An emote name <b>must</b> be an alphanumeric-with-underscores String between 2-32 chars in length!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     * <br>Example names: {@code fmgSUP}, {@code tatDAB}
     *
     *  @return {@link net.dv8tion.jda.client.managers.fields.EmoteField EmoteField} - Type: {@code String}
     */
    public EmoteField<String> getNameField()
    {
        return name;
    }

    /**
     * An {@link net.dv8tion.jda.client.managers.fields.EmoteField EmoteField}
     * for the restriction roles of the selected {@link net.dv8tion.jda.core.entities.Emote Emote}.
     * <br>If the roles are empty this Emote will be available to everyone.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Collection)}
     * on the returned {@link net.dv8tion.jda.client.managers.fields.EmoteField EmoteField} instance.
     *
     * <p>An emote's restricted roles <b>must not</b> contain {@code null}!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     *  @return {@link net.dv8tion.jda.client.managers.fields.EmoteField EmoteField} - Type: {@link Collection}
     */
    public EmoteField<Collection<Role>> getRolesField()
    {
        return roles;
    }

    /**
     * Resets all {@link net.dv8tion.jda.client.managers.fields.EmoteField Fields}
     * for this manager instance by calling {@link net.dv8tion.jda.core.managers.fields.Field#reset() Field.reset()} sequentially
     * <br>This is automatically called by {@link #update()}
     */
    public void reset()
    {
        name.reset();
        roles.reset();
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.requests.RestAction RestAction} instance
     * that will apply <b>all</b> changes that have been made to this manager instance.
     * <br>If no changes have been made this will simply return {@link net.dv8tion.jda.core.requests.RestAction.EmptyRestAction EmptyRestAction}.
     *
     * <p>Before applying new changes it is recommended to call {@link #reset()} to reset previous changes.
     * <br>This is automatically called if this method returns successfully.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} for this
     * update include the following:
     * <ul>
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *      <br>If the target Emote was deleted before finishing the task</li>
     *
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *      <br>If the currently logged in account was removed from the Guild before finishing the task</li>
     *
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *      <br>If the currently logged in account loses the {@link net.dv8tion.jda.core.Permission#MANAGE_EMOTES MANAGE_EMOTES Permission}
     *          before finishing the task</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_EMOTES MANAGE_EMOTES}
     *         in the underlying {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     *         <br>Applies all changes that have been made in a single api-call.
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
                Args.notNull(value, "emote name");
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
                Args.notNull(value, "Role Collection");
                value.forEach(r -> Args.notNull(r, "Role in Collection"));
            }
        };
    }
}
