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

package net.dv8tion.jda.client.requests.restaction;

import net.dv8tion.jda.client.entities.Application;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import okhttp3.RequestBody;
import org.json.JSONObject;

import java.util.function.BooleanSupplier;

/**
 * Extension of {@link net.dv8tion.jda.core.requests.RestAction RestAction} specifically
 * designed to create a {@link net.dv8tion.jda.client.entities.Application Application}.
 * This extension allows setting properties before executing the action.
 *
 * @since  3.0
 * @author Aljoscha Grebe
 */
public class ApplicationAction extends RestAction<Application>
{
    protected String description = null;
    protected Icon icon = null;
    protected String name = null;

    public ApplicationAction(final JDAImpl api, String name)
    {
        super(api, Route.Applications.CREATE_APPLICATION.compile());

       this.setName(name);
    }

    @Override
    public ApplicationAction setCheck(BooleanSupplier checks)
    {
        return (ApplicationAction) super.setCheck(checks);
    }

    @Override
    protected RequestBody finalizeData()
    {
        final JSONObject object = new JSONObject();

        object.put("name", this.name); // required

        if (this.description != null && !this.description.isEmpty())
            object.put("description", this.description);
        if (this.icon != null)
            object.put("icon", this.icon.getEncoding());

        return getRequestBody(object);
    }

    @Override
    protected void handleResponse(final Response response, final Request<Application> request)
    {
        if (response.isOk())
        {
            request.onSuccess(api.getEntityBuilder().createApplication(response.getObject()));
        }
        else
            request.onFailure(response);
    }

    /**
     * Sets the <b><u>description</u></b> for the new {@link net.dv8tion.jda.client.entities.Application Application}.
     * Passing {@code null} or an empty {@link String} will reset the description.
     *
     * <p>A description <b>must not</b> be than 400 characters long!
     *
     * @param  description
     *         The description for new {@link net.dv8tion.jda.client.entities.Application Application}
     *
     * @throws IllegalArgumentException
     *         If the provided description is more than 400 characters long
     *
     * @return The current ApplicationAction for chaining
     */
    public ApplicationAction setDescription(final String description)
    {
        if (description != null && description.length() > 400)
            throw new IllegalArgumentException("The description must not be more than 400 characters!");

        this.description = description;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Icon Icon} of the selected {@link net.dv8tion.jda.client.entities.Application Application}.
     *
     * @param  icon
     *         The {@link net.dv8tion.jda.core.entities.Icon Icon} for new {@link net.dv8tion.jda.client.entities.Application Application}
     *
     * @return The current ApplicationAction for chaining
     */
    public ApplicationAction setIcon(final Icon icon)
    {
        this.icon = icon;
        return this;
    }

    /**
     * Sets the <b><u>name</u></b> for the new {@link net.dv8tion.jda.client.entities.Application Application}.
     *
     * <p>A name <b>must not</b> be {@code null} nor less than 2 characters or more than 32 characters long!
     *
     * @param  name
     *         The name for new {@link net.dv8tion.jda.client.entities.Application Application}
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null}, less than 2 or more than 32 characters long
     *
     * @return The current ApplicationAction for chaining
     */
    public ApplicationAction setName(final String name)
    {
        if (name == null || name.length() < 2 || name.length() > 32)
            throw new IllegalArgumentException("The application name must not be null and in the range of 2-32!");
        this.name = name;
        return this;
    }

}
