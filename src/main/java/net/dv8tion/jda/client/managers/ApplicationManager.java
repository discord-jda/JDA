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

public class ApplicationManager
{
    private final ApplicationManagerUpdatable updatable;

    public ApplicationManager(final ApplicationImpl application)
    {
        this.updatable = new ApplicationManagerUpdatable(application);
    }

    public Application getApplication()
    {
        return this.updatable.getApplication();
    }

    public JDA getJDA()
    {
        return this.updatable.getJDA();
    }

    public RestAction<Application> setBotPublic(final boolean botPublic)
    {
        return this.updatable.getIsBotPublicField().setValue(botPublic).update();
    }

    /**
     * Changes the description of this Application
     * @param description
     *      Not null description of the Application (to remove description pass empty String)
     * @return
     */
    public RestAction<Application> setDescription(final String description)
    {
        return this.updatable.getDescriptionField().setValue(description).update();
    }

    public RestAction<Application> setDoesBotRequireCodeGrant(final boolean requireCodeGrant)
    {
        return this.updatable.getDoesBotRequireCodeGrantField().setValue(requireCodeGrant).update();
    }

    /**
     * Changes the Icon of this Application.
     * @param icon
     *      The new icon to use, or null to remove old icon
     * @return
     */
    public RestAction<Application> setIcon(final Icon icon)
    {
        return this.updatable.getIconField().setValue(icon).update();
    }

    public RestAction<Application> setIsBotPublic(final boolean botPublic)
    {
        return this.updatable.getIsBotPublicField().setValue(botPublic).update();
    }

    /**
     * Changes the name of this Application
     * @param name
     *      The new name of this Application
     * @return
     */
    public RestAction<Application> setName(final String name)
    {
        return this.updatable.getNameField().setValue(name).update();
    }

    public RestAction<Application> setRedirectUris(final List<String> redirectUris)
    {
        return this.updatable.getRedirectUrisField().setValue(redirectUris).update();
    }

    public RestAction<Application> setRpcOrigins(final List<String> rpcOrigins)
    {
        return this.updatable.getRedirectUrisField().setValue(rpcOrigins).update();
    }

}
