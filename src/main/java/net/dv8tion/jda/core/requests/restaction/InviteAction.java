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

package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Invite;
import net.dv8tion.jda.core.requests.*;

import org.json.JSONObject;

public class InviteAction extends RestAction<Invite>
{
    private Integer maxAge = null;
    private Integer maxUses = null;
    private Boolean temporary = null;
    private Boolean unique = null;

    public InviteAction(final JDA api, final String channelId)
    {
        super(api, Route.Invites.CREATE_INVITE.compile(channelId), null);
    }

    @Override
    protected void finalizeData()
    {
        final JSONObject object = new JSONObject();

        if (this.maxAge != null)
            object.put("max_age", (int) this.maxAge);
        if (this.maxUses != null)
            object.put("max_uses", (int) this.maxUses);
        if (this.temporary != null)
            object.put("temporary", (boolean) this.temporary);
        if (this.unique != null)
            object.put("unique", (boolean) this.unique);

        this.data = object;
    }

    @Override
    protected void handleResponse(final Response response, final Request request)
    {
        if (response.isOk())
            request.onSuccess(EntityBuilder.get(this.api).createInvite(response.getObject()));
        else
            request.onFailure(response);
    }

    /**
     * Sets the max age in seconds for the invite. Set this to {@code 0} if the invite should never expire. Default is {@code 86400} (24 hours).
     *
     * @param  maxAge
     *         The max age for this invite or {@code null} to use the default value.
     *
     * @return The current InviteAction for chaining.
     */
    public final InviteAction setMaxAge(final Integer maxAge)
    {
        if (maxAge < 0)
            throw new IllegalArgumentException("maxAge must be grater than 0!");
        this.maxAge = maxAge;
        return this;
    }

    /**
     * Sets the max uses for the invite. Set this to {@code 0} if the invite should have unlimited uses. Default is {@code 0}.
     *
     * @param  maxUses
     *         The max uses for this invite or {@code null} to use the default value.
     *
     * @return The current InviteAction for chaining.
     */
    public final InviteAction setMaxUses(final Integer maxUses)
    {
        if (maxUses < 0)
            throw new IllegalArgumentException("maxAge must be grater than 0!");
        this.maxUses = maxUses;
        return this;
    }

    /**
     * Sets whether the invite should only grant temporary membership. Default is {@code false}.
     *
     * @param  temporary
     *         whether the invite should only grant temporary membership or {@code null} to use the default value.
     *
     * @return The current InviteAction for chaining.
     */
    public final InviteAction setTemporary(final Boolean temporary)
    {
        this.temporary = temporary;
        return this;
    }

    /**
     * Sets whether discord should reuse a similar invite. Default is {@code false}.
     *
     * @param  temporary
     *         whether discord should reuse a similar invite or {@code null} to use the default value.
     *
     * @return The current InviteAction for chaining.
     */
    public final InviteAction setUnique(final Boolean unique)
    {
        this.unique = unique;
        return this;
    }
}
