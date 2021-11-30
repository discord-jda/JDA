/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.internal.requests.restaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.InviteAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class InviteActionImpl extends AuditableRestActionImpl<Invite> implements InviteAction
{
    private Integer maxAge = null;
    private Integer maxUses = null;
    private Boolean temporary = null;
    private Boolean unique = null;
    private Long targetApplication = null;
    private Long targetUser = null;
    private Invite.TargetType targetType = null;

    public InviteActionImpl(final JDA api, final String channelId)
    {
        super(api, Route.Invites.CREATE_INVITE.compile(channelId));
    }

    @Nonnull
    @Override
    public InviteActionImpl setCheck(BooleanSupplier checks)
    {
        return (InviteActionImpl) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public InviteActionImpl timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (InviteActionImpl) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public InviteActionImpl deadline(long timestamp)
    {
        return (InviteActionImpl) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public InviteActionImpl setMaxAge(final Integer maxAge)
    {
        if (maxAge != null)
            Checks.notNegative(maxAge, "maxAge");

        this.maxAge = maxAge;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public InviteActionImpl setMaxAge(final Long maxAge, @Nonnull final TimeUnit timeUnit)
    {
        if (maxAge == null)
            return this.setMaxAge(null);

        Checks.notNegative(maxAge, "maxAge");
        Checks.notNull(timeUnit, "timeUnit");

        return this.setMaxAge(Math.toIntExact(timeUnit.toSeconds(maxAge)));
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public InviteActionImpl setMaxUses(final Integer maxUses)
    {
        if (maxUses != null)
            Checks.notNegative(maxUses, "maxUses");

        this.maxUses = maxUses;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public InviteActionImpl setTemporary(final Boolean temporary)
    {
        this.temporary = temporary;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public InviteActionImpl setUnique(final Boolean unique)
    {
        this.unique = unique;
        return this;
    }

    @Nonnull
    @Override
    public InviteAction setTargetApplication(final long applicationId)
    {
        if (applicationId == 0)
        {
            this.targetType = null;
            this.targetApplication = null;
            return this;
        }

        this.targetType = Invite.TargetType.EMBEDDED_APPLICATION;
        this.targetApplication = applicationId;
        return this;
    }

    @Nonnull
    @Override
    public InviteAction setTargetStream(final long userId)
    {
        if (userId == 0)
        {
            this.targetType = null;
            this.targetUser = null;
            return this;
        }

        this.targetType = Invite.TargetType.STREAM;
        this.targetUser = userId;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject object = DataObject.empty();

        if (this.maxAge != null)
            object.put("max_age", this.maxAge);
        if (this.maxUses != null)
            object.put("max_uses", this.maxUses);
        if (this.temporary != null)
            object.put("temporary", this.temporary);
        if (this.unique != null)
            object.put("unique", this.unique);
        if (this.targetType != null)
            object.put("target_type", targetType.getId());
        if (this.targetUser != null)
            object.put("target_user_id", targetUser);
        if (this.targetApplication != null)
            object.put("target_application_id", targetApplication);

        return getRequestBody(object);
    }

    @Override
    protected void handleSuccess(final Response response, final Request<Invite> request)
    {
        request.onSuccess(this.api.getEntityBuilder().createInvite(response.getObject()));
    }
}
