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
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Manager providing functionality to update one or more fields for an {@link net.dv8tion.jda.core.entities.Emote Emote}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setName("minn")
 *        .setRoles(null)
 *        .queue();
 * manager.reset(EmoteManager.NAME | EmoteManager.ROLES)
 *        .setName("dv8")
 *        .setRoles(roles)
 *        .queue();
 * }</pre>
 *
 * @see net.dv8tion.jda.core.entities.Emote#getManager()
 */
public class EmoteManager extends ManagerBase
{
    /** Used to reset the name field */
    public static final long NAME = 0x1;

    /** Used to reset the roles field */
    public static final long ROLES = 0x2;

    protected final EmoteImpl emote;

    protected final List<String> roles = new ArrayList<>();
    protected String name;

    /**
     * Creates a new EmoteManager instance
     *
     * @param  emote
     *         The target {@link net.dv8tion.jda.core.entities.impl.EmoteImpl EmoteImpl} to modify
     *
     * @throws java.lang.IllegalStateException
     *         If the specified Emote is {@link net.dv8tion.jda.core.entities.Emote#isFake() fake} or {@link net.dv8tion.jda.core.entities.Emote#isManaged() managed}.
     */
    public EmoteManager(EmoteImpl emote)
    {
        super(emote.getJDA(), Route.Emotes.MODIFY_EMOTE.compile(emote.getGuild().getId(), emote.getId()));
        if (emote.isFake())
            throw new IllegalStateException("Cannot modify a fake emote");
        this.emote = emote;
        if (isPermissionChecksEnabled())
            checkPermissions();
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
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(EmoteManager.NAME | EmoteManager.ROLES);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #ROLES}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return EmoteManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public EmoteManager reset(long fields)
    {
        super.reset(fields);
        if ((fields & ROLES) == ROLES)
            withLock(this.roles, List::clear);
        if ((fields & NAME) == NAME)
            this.name = null;
        return this;
    }

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(EmoteManager.NAME, EmoteManager.ROLES);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #ROLES}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return EmoteManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public EmoteManager reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    /**
     * Resets all fields for this manager.
     *
     * @return EmoteManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    protected EmoteManager reset()
    {
        super.reset();
        withLock(this.roles, List::clear);
        this.name = null;
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
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 2-32 characters long
     *
     * @return EmoteManager for chaining convenience
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
     *         The new set of {@link net.dv8tion.jda.core.entities.Role Roles} for the selected {@link net.dv8tion.jda.core.entities.Emote Emote}
     *         to be restricted to, or {@code null} to clear the roles
     *
     * @throws IllegalArgumentException
     *         If any of the provided values is {@code null}
     *
     * @return EmoteManager for chaining convenience
     */
    @CheckReturnValue
    public EmoteManager setRoles(Set<Role> roles)
    {
        if (roles == null)
        {
            withLock(this.roles, List::clear);
        }
        else
        {
            Checks.notNull(roles, "Roles");
            roles.forEach((role) ->
            {
                Checks.notNull(role, "Roles");
                Checks.check(role.getGuild().equals(getGuild()), "Roles must all be from the same guild");
            });
            withLock(this.roles, (list) ->
            {
                list.clear();
                roles.stream().map(Role::getId).forEach(list::add);
            });
        }
        set |= ROLES;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        JSONObject object = new JSONObject();
        if (shouldUpdate(NAME))
            object.put("name", name);
        withLock(this.roles, (list) ->
        {
            if (shouldUpdate(ROLES))
                object.put("roles", list);
        });

        reset();
        return getRequestBody(object);
    }

    @Override
    protected boolean checkPermissions()
    {
        if (!getGuild().getSelfMember().hasPermission(Permission.MANAGE_EMOTES))
            throw new InsufficientPermissionException(Permission.MANAGE_EMOTES);
        return super.checkPermissions();
    }
}
