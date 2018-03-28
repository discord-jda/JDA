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

import net.dv8tion.jda.client.entities.Application;
import net.dv8tion.jda.client.entities.impl.ApplicationImpl;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.managers.impl.ManagerBase;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager providing functionality to update one or more fields for an {@link net.dv8tion.jda.client.entities.Application Application}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setName("Yui")
 *        .setDescription("Simple but outdated bot")
 *        .queue();
 * manager.reset(ApplicationManager.NAME | ApplicationManager.PUBLIC)
 *        .setName("BooBot")
 *        .setDescription("Even more outdated bot")
 *        .queue();
 * }</pre>
 *
 * @since  3.0
 * @author Aljoscha Grebe
 *
 * @see    net.dv8tion.jda.client.entities.Application#getManager()
 */
public class ApplicationManager extends ManagerBase
{
    /** Used to reset the description field */
    public static final long DESCRIPTION  = 0x1;
    /** Used to reset the icon field */
    public static final long ICON         = 0x2;
    /** Used to reset the name field */
    public static final long NAME         = 0x4;
    /** Used to reset the redirect uri field */
    public static final long REDIRECT_URI = 0x8;
    /** Used to reset the public field */
    public static final long PUBLIC       = 0x10;
    /** Used to reset the code grant field */
    public static final long CODE_GRANT   = 0x20;

    protected final ApplicationImpl application;

    protected final List<String> redirectUris = new ArrayList<>();
    protected String name;
    protected String description;
    protected Icon icon;
    protected boolean isPublic;
    protected boolean isCodeGrant;

    public ApplicationManager(final ApplicationImpl application)
    {
        super(application.getJDA(), Route.Applications.MODIFY_APPLICATION.compile(application.getId()));
        this.application = application;
    }

    /**
     * The {@link net.dv8tion.jda.client.entities.Application Application} that will
     * be modified by this Manager instance
     *
     * @return The {@link net.dv8tion.jda.client.entities.Application Application}
     */
    public Application getApplication()
    {
        return application;
    }

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(ApplicationManager.NAME | ApplicationManager.ROLES);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #ICON}</li>
     *     <li>{@link #DESCRIPTION}</li>
     *     <li>{@link #REDIRECT_URI}</li>
     *     <li>{@link #PUBLIC}</li>
     *     <li>{@link #CODE_GRANT}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return ApplicationManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public ApplicationManager reset(long fields)
    {
        super.reset(fields);
        if ((fields & ICON) == ICON)
            icon = null;
        if ((fields & REDIRECT_URI) == REDIRECT_URI)
            withLock(this.redirectUris, List::clear);
        return this;
    }

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(ApplicationManager.NAME, ApplicationManager.ICON);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #ICON}</li>
     *     <li>{@link #DESCRIPTION}</li>
     *     <li>{@link #REDIRECT_URI}</li>
     *     <li>{@link #PUBLIC}</li>
     *     <li>{@link #CODE_GRANT}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return ApplicationManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public ApplicationManager reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    /**
     * Resets all fields for this manager.
     *
     * @return ApplicationManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public ApplicationManager reset()
    {
        super.reset();
        icon = null;
        withLock(this.redirectUris, List::clear);
        return this;
    }

    /**
     * Sets the <b><u>description</u></b> of the selected {@link net.dv8tion.jda.client.entities.Application Application}.
     *
     * <p>A description <b>must not</b> be longer than 400 characters long!
     *
     * @param  description
     *         The new description for the selected {@link net.dv8tion.jda.client.entities.Application Application}
     *         or {@code null} to reset
     *
     * @throws IllegalArgumentException
     *         If the provided description is more than 400 characters long
     *
     * @return ApplicationManager for chaining convenience
     */
    @CheckReturnValue
    public ApplicationManager setDescription(final String description)
    {
        Checks.check(description == null || description.length() <= 400, "Description must be less or equal to 400 characters in length");
        this.description = description;
        set |= DESCRIPTION;
        return this;
    }

    /**
     * Sets the <b><u>code grant state</u></b> of the selected {@link net.dv8tion.jda.client.entities.Application Application's} bot.
     *
     * @param  requireCodeGrant
     *         The new state for the selected {@link net.dv8tion.jda.client.entities.Application Application's} bot
     *
     * @return ApplicationManager for chaining convenience
     */
    @CheckReturnValue
    public ApplicationManager setDoesBotRequireCodeGrant(final boolean requireCodeGrant)
    {
        this.isCodeGrant = requireCodeGrant;
        set |= CODE_GRANT;
        return this;
    }

    /**
     * Sets the <b><u>icon</u></b> of the selected {@link net.dv8tion.jda.client.entities.Application Application}.
     *
     * @param  icon
     *         The new {@link net.dv8tion.jda.core.entities.Icon Icon}
     *         for the selected {@link net.dv8tion.jda.client.entities.Application Application}
     *         or {@code null} to reset
     *
     * @return ApplicationManager for chaining convenience
     */
    @CheckReturnValue
    public ApplicationManager setIcon(final Icon icon)
    {
        this.icon = icon;
        set |= ICON;
        return this;
    }

    /**
     * Sets the <b><u>public state</u></b> of the selected {@link net.dv8tion.jda.client.entities.Application Application's} bot.
     *
     * @param  botPublic
     *         The new state for the selected {@link net.dv8tion.jda.client.entities.Application Application's} bot
     *
     * @return ApplicationManager for chaining convenience
     */
    @CheckReturnValue
    public ApplicationManager setIsBotPublic(final boolean botPublic)
    {
        this.isPublic = botPublic;
        set |= PUBLIC;
        return this;
    }

    /**
     * Sets the <b><u>name</u></b> of the selected {@link net.dv8tion.jda.client.entities.Application Application}.
     *
     * <p>A name <b>must not</b> be {@code null} nor less than 2 characters or more than 32 characters long!
     *
     * @param  name
     *         The new name for the selected {@link net.dv8tion.jda.client.entities.Application Application}
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null}, less than 2 or more than 32 characters long
     *
     * @return ApplicationManager for chaining convenience
     */
    @CheckReturnValue
    public ApplicationManager setName(final String name)
    {
        Checks.notBlank(name, "Name");
        Checks.check(name.length() >= 2 && name.length() <= 32, "Name must be between 2-32 characters long");
        this.name = name;
        set |= NAME;
        return this;
    }

    /**
     * Sets the <b><u>redirect uris</u></b> of the selected {@link net.dv8tion.jda.client.entities.Application Application}.
     *
     * <p>The redirect uris <b>must not</b> be {@code null}!
     * 
     * @param  redirectUris
     *         The new redirect uris
     *         for the selected {@link net.dv8tion.jda.client.entities.Application Application}
     *         or {@code null} to reset
     *
     * @throws IllegalArgumentException
     *         If one of the uris is {@code null}
     *
     * @return ApplicationManager for chaining convenience
     */
    @CheckReturnValue
    public ApplicationManager setRedirectUris(final List<String> redirectUris)
    {
        if (redirectUris == null)
        {
            withLock(this.redirectUris, List::clear);
        }
        else
        {
            Checks.noneNull(redirectUris, "Redirects");
            withLock(this.redirectUris, (list) ->
            {
                list.clear();
                list.addAll(redirectUris);
            });
        }
        set |= REDIRECT_URI;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        JSONObject body = new JSONObject();

        body.put("description", shouldUpdate(DESCRIPTION)
            ? opt(description) : application.getDescription());

        body.put("bot_require_code_grant", shouldUpdate(CODE_GRANT)
            ? isCodeGrant : application.doesBotRequireCodeGrant());

        body.put("icon", shouldUpdate(ICON)
            ? (icon == null ? JSONObject.NULL : icon.getEncoding())
            : application.getIconUrl());

        body.put("bot_public", shouldUpdate(PUBLIC)
            ? isPublic : application.isBotPublic());

        body.put("name", shouldUpdate(NAME)
            ? name : application.getName());

        withLock(this.redirectUris, (list) ->
            body.put("redirect_uris", shouldUpdate(REDIRECT_URI)
                ? list : application.getRedirectUris()));

        reset();
        return getRequestBody(body);
    }
}
