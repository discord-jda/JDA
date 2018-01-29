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
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import java.util.LinkedList;
import java.util.List;

/**
 * Facade for an {@link net.dv8tion.jda.client.managers.ApplicationManagerUpdatable ApplicationManagerUpdatable} instance.
 * <br>Simplifies managing flow for convenience.
 *
 * <p>This decoration allows to modify a single field by automatically building an update {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 * 
 * @since  3.0
 * @author Aljoscha Grebe
 */
public class ApplicationManager extends ManagerBase
{
    public static final int DESCRIPTION  = 0x1;
    public static final int ICON         = 0x2;
    public static final int NAME         = 0x4;
    public static final int REDIRECT_URI = 0x8;
    public static final int PUBLIC       = 0x10;
    public static final int CODE_GRANT   = 0x20;

    protected final ApplicationImpl application;

    protected final List<String> redirectUris = new LinkedList<>();
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

    @Override
    @CheckReturnValue
    public ApplicationManager reset(int fields)
    {
        super.reset(fields);
        if ((fields & ICON) == ICON)
            icon = null;
        return this;
    }

    @Override
    @CheckReturnValue
    public ApplicationManager reset(int... fields)
    {
        super.reset(fields);
        return this;
    }

    @Override
    @CheckReturnValue
    public ApplicationManager reset()
    {
        super.reset();
        icon = null;
        return this;
    }

    /**
     * Sets the <b><u>description</u></b> of the selected {@link net.dv8tion.jda.client.entities.Application Application}.
     * <br>Wraps {@link net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#getDescriptionField() ApplicationManagerUpdatable#getDescriptionField()}.
     *
     * <p>A description <b>must not</b> be than 400 characters long!
     *
     * @param  description
     *         The new description for the selected {@link net.dv8tion.jda.client.entities.Application Application}
     *
     * @throws IllegalArgumentException
     *         If the provided description is more than 400 characters long
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link ApplicationManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#getDescriptionField()
     * @see    net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#update()
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
     * <br>Wraps {@link net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#getDoesBotRequireCodeGrantField() ApplicationManagerUpdatable#getDoesBotRequireCodeGrantField()}.
     *
     * @param  requireCodeGrant
     *         The new state for the selected {@link net.dv8tion.jda.client.entities.Application Application's} bot
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link ApplicationManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#getDoesBotRequireCodeGrantField()
     * @see    net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#update()
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
     * <br>Wraps {@link net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#getIconField() ApplicationManagerUpdatable#getIconField()}.
     *
     * @param  icon
     *         The new {@link net.dv8tion.jda.core.entities.Icon Icon}
     *         for the selected {@link net.dv8tion.jda.client.entities.Application Application}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link ApplicationManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#getIconField()
     * @see    net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#update()
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
     * <br>Wraps {@link net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#getIsBotPublicField() ApplicationManagerUpdatable#getIsBotPublicField()}.
     *
     * @param  botPublic
     *         The new state for the selected {@link net.dv8tion.jda.client.entities.Application Application's} bot
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link ApplicationManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#getIsBotPublicField()
     * @see    net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#update()
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
     * <br>Wraps {@link net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#getNameField() ApplicationManagerUpdatable#getNameField()}.
     *
     * <p>A name <b>must not</b> be {@code null} nor less than 2 characters or more than 32 characters long!
     *
     * @param  name
     *         The new name for the selected {@link net.dv8tion.jda.client.entities.Application Application}
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null}, less than 2 or more than 32 characters long
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link ApplicationManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#getNameField()
     * @see    net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#update()
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
     * <br>Wraps {@link net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#getRedirectUrisField() ApplicationManagerUpdatable#getRedirectUrisField()}.
     * 
     * <p>The {@link java.util.List List} as well as all redirect uris <b>must not</b> be {@code null}!
     * 
     * @param  redirectUris
     *         The new redirect uris
     *         for the selected {@link net.dv8tion.jda.client.entities.Application Application}
     *
     * @throws IllegalArgumentException
     *         If either the provided {@link java.util.List List} or one of the uris is {@code null}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link ApplicationManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#getIconField()
     * @see    net.dv8tion.jda.client.managers.ApplicationManagerUpdatable#update()
     */
    @CheckReturnValue
    public ApplicationManager setRedirectUris(final List<String> redirectUris)
    {
        Checks.noneNull(redirectUris, "Redirects");
        this.redirectUris.clear();
        this.redirectUris.addAll(redirectUris);
        set |= REDIRECT_URI;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        JSONObject body = new JSONObject();

        body.put("description", shouldUpdate(DESCRIPTION)
            ? opt(description)
            : application.getDescription());

        body.put("bot_require_code_grant", shouldUpdate(CODE_GRANT)
            ? isCodeGrant
            : application.doesBotRequireCodeGrant());

        body.put("icon", shouldUpdate(ICON)
            ? (icon == null ? JSONObject.NULL : icon.getEncoding())
            : application.getIconUrl());

        body.put("bot_public", shouldUpdate(PUBLIC)
            ? isPublic
            : application.isBotPublic());

        body.put("name", shouldUpdate(NAME)
            ? name
            : application.getName());

        body.put("redirect_uris", shouldUpdate(REDIRECT_URI)
            ? redirectUris
            : application.getRedirectUris());

        reset();
        return RequestBody.create(Requester.MEDIA_TYPE_JSON, body.toString());
    }
}
