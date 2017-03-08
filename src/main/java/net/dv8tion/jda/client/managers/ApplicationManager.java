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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.client.managers;

import net.dv8tion.jda.client.entities.Application;
import net.dv8tion.jda.client.entities.impl.ApplicationImpl;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.requests.RestAction;

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
public class ApplicationManager
{
    private final ApplicationManagerUpdatable updatable;

    public ApplicationManager(final ApplicationImpl application)
    {
        this.updatable = new ApplicationManagerUpdatable(application);
    }

    /**
     * The {@link net.dv8tion.jda.client.entities.Application Application} that will
     * be modified by this Manager instance
     *
     * @return The {@link net.dv8tion.jda.client.entities.Application Application}
     */
    public Application getApplication()
    {
        return this.updatable.getApplication();
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Manager
     *
     * @return the corresponding JDA instance
     */
    public JDA getJDA()
    {
        return this.updatable.getJDA();
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
    public RestAction<Void> setDescription(final String description)
    {
        return this.updatable.getDescriptionField().setValue(description).update();
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
    public RestAction<Void> setDoesBotRequireCodeGrant(final boolean requireCodeGrant)
    {
        return this.updatable.getDoesBotRequireCodeGrantField().setValue(requireCodeGrant).update();
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
    public RestAction<Void> setIcon(final Icon icon)
    {
        return this.updatable.getIconField().setValue(icon).update();
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
    public RestAction<Void> setIsBotPublic(final boolean botPublic)
    {
        return this.updatable.getIsBotPublicField().setValue(botPublic).update();
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
    public RestAction<Void> setName(final String name)
    {
        return this.updatable.getNameField().setValue(name).update();
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
    public RestAction<Void> setRedirectUris(final List<String> redirectUris)
    {
        return this.updatable.getRedirectUrisField().setValue(redirectUris).update();
    }
}
