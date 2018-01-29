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

package net.dv8tion.jda.client.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.impl.EmoteImpl;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.impl.ManagerBase;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Decoration for a {@link net.dv8tion.jda.client.managers.EmoteManagerUpdatable EmoteManagerUpdatable} instance.
 * <br>Simplifies managing flow for convenience.
 *
 * <p>This decoration allows to modify a single field by automatically building an update {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 */
public class EmoteManager extends ManagerBase
{
    public static final int NAME = 0x1;
    public static final int ROLES = 0x2;

    protected final EmoteImpl emote;

    protected final List<String> roles = new LinkedList<>();
    protected String name;

    /**
     * Creates a new EmoteManager instance
     *
     * @param  emote
     *         The target {@link net.dv8tion.jda.core.entities.impl.EmoteImpl EmoteImpl} to modify
     *
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *         If the currently logged in account is not from {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}
     * @throws java.lang.IllegalStateException
     *         If the specified Emote is {@link net.dv8tion.jda.core.entities.Emote#isFake() fake} or {@link net.dv8tion.jda.core.entities.Emote#isManaged() managed}.
     */
    public EmoteManager(EmoteImpl emote)
    {
        super(emote.getJDA(), Route.Emotes.MODIFY_EMOTE.compile(emote.getGuild().getId(), emote.getId()));
        this.emote = emote;
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

    @Override
    @CheckReturnValue
    public EmoteManager reset(int fields)
    {
        super.reset(fields);
        if ((fields & ROLES) == ROLES)
            roles.clear();
        return this;
    }

    @Override
    @CheckReturnValue
    public EmoteManager reset(int... fields)
    {
        super.reset(fields);
        return this;
    }

    @Override
    @CheckReturnValue
    protected EmoteManager reset()
    {
        super.reset();
        roles.clear();
        return this;
    }

    /**
     * Sets the <b><u>name</u></b> of the selected {@link net.dv8tion.jda.core.entities.Emote Emote}.
     *
     * <p>An emote name <b>must</b> be between 2-32 characters long!
     * <br>Emote names may only be populated with alphanumeric (with underscore and dash).
     *
     * <p><b>Example</b>: {@code tatDab} or {@code fmgSUP}
     *
     * @param  name
     *         The new name for the selected {@link net.dv8tion.jda.core.entities.Emote Emote}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_EMOTES MANAGE_EMOTES}
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 2-32 characters long
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link EmoteManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.client.managers.EmoteManagerUpdatable#getNameField()
     * @see    net.dv8tion.jda.client.managers.EmoteManagerUpdatable#update()
     */
    @CheckReturnValue
    public EmoteManager setName(String name)
    {
        Checks.notBlank(name, "Name");
        Checks.check(name.length() >= 2 && name.length() <= 32, "Name must be between 2-32 characters long");
        this.name = name;
        set |= NAME;
        return this;
    }

    /**
     * Sets the <b><u>restriction roles</u></b> of the selected {@link net.dv8tion.jda.core.entities.Emote Emote}.
     * <br>If these are empty the Emote will be available to everyone otherwise only available to the specified roles.
     *
     * <p>An emote's restriction roles <b>must not</b> contain {@code null}!
     *
     * @param  roles
     *         The new not-null set of {@link net.dv8tion.jda.core.entities.Role Roles} for the selected {@link net.dv8tion.jda.core.entities.Emote Emote}
     *         to be restricted to
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_EMOTES MANAGE_EMOTES}
     * @throws IllegalArgumentException
     *         If any of the provided values is {@code null}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link EmoteManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.client.managers.EmoteManagerUpdatable#getRolesField()
     * @see    net.dv8tion.jda.client.managers.EmoteManagerUpdatable#update()
     */
    @CheckReturnValue
    public EmoteManager setRoles(Set<Role> roles)
    {
        Checks.notNull(roles, "Roles");
        roles.forEach((role) ->
        {
            Checks.notNull(role, "Roles");
            Checks.check(role.getGuild().equals(getGuild()), "Roles must all be from the same guild");
        });
        this.roles.clear();
        roles.stream().map(Role::getId).forEach(this.roles::add);
        set |= ROLES;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        if (!getGuild().getSelfMember().hasPermission(Permission.MANAGE_EMOTES))
            throw new InsufficientPermissionException(Permission.MANAGE_EMOTES);

        JSONObject object = new JSONObject();
        if (shouldUpdate(NAME))
            object.put("name", name);
        if (shouldUpdate(ROLES))
            object.put("roles", roles);
        reset();
        return RequestBody.create(Requester.MEDIA_TYPE_JSON, object.toString());
    }
}
