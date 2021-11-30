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

package net.dv8tion.jda.internal.managers;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.managers.WebhookManager;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class WebhookManagerImpl extends ManagerBase<WebhookManager> implements WebhookManager
{
    protected final Webhook webhook;
    protected String name;
    protected String channel;
    protected Icon avatar;

    /**
     * Creates a new WebhookManager instance
     *
     * @param webhook
     *        The target {@link net.dv8tion.jda.api.entities.Webhook Webhook} to modify
     */
    public WebhookManagerImpl(Webhook webhook)
    {
        super(webhook.getJDA(), Route.Webhooks.MODIFY_WEBHOOK.compile(webhook.getId()));
        this.webhook = webhook;
        if (isPermissionChecksEnabled())
            checkPermissions();
    }

    @Nonnull
    @Override
    public Webhook getWebhook()
    {
        return webhook;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public WebhookManagerImpl reset(long fields)
    {
        super.reset(fields);
        if ((fields & NAME) == NAME)
            this.name = null;
        if ((fields & CHANNEL) == CHANNEL)
            this.channel = null;
        if ((fields & AVATAR) == AVATAR)
            this.avatar = null;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public WebhookManagerImpl reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public WebhookManagerImpl reset()
    {
        super.reset();
        this.name = null;
        this.channel = null;
        this.avatar = null;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public WebhookManagerImpl setName(@Nonnull String name)
    {
        Checks.notBlank(name, "Name");
        this.name = name;
        set |= NAME;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public WebhookManagerImpl setAvatar(Icon icon)
    {
        this.avatar = icon;
        set |= AVATAR;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public WebhookManagerImpl setChannel(@Nonnull TextChannel channel)
    {
        Checks.notNull(channel, "Channel");
        Checks.check(channel.getGuild().equals(getGuild()), "Channel is not from the same guild");
        this.channel = channel.getId();
        set |= CHANNEL;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject data = DataObject.empty();
        if (shouldUpdate(NAME))
            data.put("name", name);
        if (shouldUpdate(CHANNEL))
            data.put("channel_id", channel);
        if (shouldUpdate(AVATAR))
            data.put("avatar", avatar == null ? null : avatar.getEncoding());

        return getRequestBody(data);
    }

    @Override
    protected boolean checkPermissions()
    {
        Member selfMember = getGuild().getSelfMember();
        BaseGuildMessageChannel channel = getChannel();
        if (!selfMember.hasAccess(channel))
            throw new MissingAccessException(channel, Permission.VIEW_CHANNEL);
        if (!selfMember.hasPermission(channel, Permission.MANAGE_WEBHOOKS))
            throw new InsufficientPermissionException(channel, Permission.MANAGE_WEBHOOKS);
        return super.checkPermissions();
    }
}
