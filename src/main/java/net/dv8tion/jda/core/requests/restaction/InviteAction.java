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

package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Invite;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.apache.http.util.Args;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

/**
 * {@link net.dv8tion.jda.core.entities.Invite Invite} Builder system created as an extension of {@link net.dv8tion.jda.core.requests.RestAction}
 * <br>Provides an easy way to gather and deliver information to Discord to create {@link net.dv8tion.jda.core.entities.Invite Invites}.
 */
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
     * {@code null} will reset this to the default value.
     *
     * @param  maxAge
     *         The max age for this invite or {@code null} to use the default value.
     *
     * @throws IllegalArgumentException
     *         If maxAge is negative.
     *
     * @return The current InviteAction for chaining.
     */
    public final InviteAction setMaxAge(final Integer maxAge)
    {
        if (maxAge != null)
            Args.notNegative(maxAge, "maxAge");

        this.maxAge = maxAge;
        return this;
    }

    /**
     * Sets the max age for the invite. Set this to {@code 0} if the invite should never expire. Default is {@code 86400} (24 hours).
     * {@code null} will reset this to the default value.
     *
     * @param  maxAge
     *         The max age for this invite or {@code null} to use the default value.
     * @param  timeUnit
     *         The {@link java.util.concurrent.TimeUnit TimeUnit} type of {@code maxAge}.
     *
     * @throws IllegalArgumentException
     *         If maxAge is negative or maxAge is positive and timeUnit is null.
     *
     * @return The current InviteAction for chaining.
     */
    public final InviteAction setMaxAge(final Long maxAge, final TimeUnit timeUnit)
    {
        if (maxAge == null)
            return this.setMaxAge(null);

        Args.notNegative(maxAge, "maxAge");
        Args.notNull(timeUnit, "timeUnit");

        return this.setMaxAge(Math.toIntExact(timeUnit.toSeconds(maxAge)));
    }

    /**
     * Sets the max uses for the invite. Set this to {@code 0} if the invite should have unlimited uses. Default is {@code 0}.
     * {@code null} will reset this to the default value.
     *
     * @param  maxUses
     *         The max uses for this invite or {@code null} to use the default value.
     *
     * @throws IllegalArgumentException
     *         If maxUses is negative.
     *
     * @return The current InviteAction for chaining.
     */
    public final InviteAction setMaxUses(final Integer maxUses)
    {
        if (maxUses != null)
            Args.notNegative(maxUses, "maxUses");

        this.maxUses = maxUses;
        return this;
    }

    /**
     * Sets whether the invite should only grant temporary membership. Default is {@code false}.
     *
     * @param  temporary
     *         Whether the invite should only grant temporary membership or {@code null} to use the default value.
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
     * @param  unique
     *         Whether discord should reuse a similar invite or {@code null} to use the default value.
     *
     * @return The current InviteAction for chaining.
     */
    public final InviteAction setUnique(final Boolean unique)
    {
        this.unique = unique;
        return this;
    }
}
