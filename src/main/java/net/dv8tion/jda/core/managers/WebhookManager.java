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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.impl.ManagerBase;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;

/**
 * Facade for a {@link net.dv8tion.jda.core.managers.WebhookManagerUpdatable WebhookManagerUpdatable} instance.
 * <br>Simplifies managing flow for convenience.
 *
 * <p>This decoration allows to modify a single field by automatically building an update {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 */
public class WebhookManager extends ManagerBase
{
    public static final int NAME    = 0x1;
    public static final int CHANNEL = 0x2;
    public static final int AVATAR  = 0x4;

    protected final Webhook webhook;

    protected String name;
    protected String channel;
    protected Icon avatar;

    /**
     * Creates a new WebhookManager instance
     *
     * @param webhook
     *        The target {@link net.dv8tion.jda.core.entities.Webhook Webhook} to modify
     */
    public WebhookManager(Webhook webhook)
    {
        super(webhook.getJDA(), Route.Webhooks.MODIFY_TOKEN_WEBHOOK.compile(webhook.getId(), webhook.getToken()));
        this.webhook = webhook;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} this Manager's
     * {@link net.dv8tion.jda.core.entities.Webhook Webhook} is in.
     * <br>This is logically the same as calling {@code getWebhook().getGuild()}
     *
     * @return The parent {@link net.dv8tion.jda.core.entities.Guild Guild}
     */
    public Guild getGuild()
    {
        return webhook.getGuild();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} this Manager's
     * {@link net.dv8tion.jda.core.entities.Webhook Webhook} is in.
     * <br>This is logically the same as calling {@code getWebhook().getChannel()}
     *
     * @return The parent {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     */
    public TextChannel getChannel()
    {
        return webhook.getChannel();
    }

    /**
     * The target {@link net.dv8tion.jda.core.entities.Webhook Webhook}
     * that will be modified by this manager
     *
     * @return The target {@link net.dv8tion.jda.core.entities.Webhook Webhook}
     */
    public Webhook getWebhook()
    {
        return webhook;
    }

    @Override
    public WebhookManager reset(int fields)
    {
        super.reset(fields);
        //the avatar encoding is expected to have a high memory footprint, so we clear it here
        if ((fields & AVATAR) == AVATAR)
            avatar = null;
        return this;
    }

    @Override
    public WebhookManager reset(int... fields)
    {
        super.reset(fields);
        return this;
    }

    @Override
    public WebhookManager reset()
    {
        super.reset();
        avatar = null;
        return this;
    }

    /**
     * Sets the <b><u>default name</u></b> of the selected {@link net.dv8tion.jda.core.entities.Webhook Webhook}.
     * <br>Wraps {@link WebhookManagerUpdatable#getNameField()}
     *
     * <p>A webhook name <b>must not</b> be {@code null}!
     *
     * @param  name
     *         The new default name for the selected {@link net.dv8tion.jda.core.entities.Webhook Webhook}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_WEBHOOKS MANAGE_WEBHOOKS}
     * @throws IllegalArgumentException
     *         If the provided name is {@code null}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link WebhookManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.WebhookManagerUpdatable#getNameField()
     * @see    net.dv8tion.jda.core.managers.WebhookManagerUpdatable#update()
     */
    @CheckReturnValue
    public WebhookManager setName(String name)
    {
        Checks.notBlank(name, "Name");
        this.name = name;
        set |= NAME;
        return this;
    }

    /**
     * Sets the <b><u>default avatar</u></b> of the selected {@link net.dv8tion.jda.core.entities.Webhook Webhook}.
     * <br>Wraps {@link net.dv8tion.jda.core.managers.WebhookManagerUpdatable#getAvatarField()}
     *
     * @param  icon
     *         The new default avatar {@link net.dv8tion.jda.core.entities.Icon Icon}
     *         for the selected {@link net.dv8tion.jda.core.entities.Webhook Webhook}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_WEBHOOKS MANAGE_WEBHOOKS}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link WebhookManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.WebhookManagerUpdatable#getAvatarField()
     * @see    net.dv8tion.jda.core.managers.WebhookManagerUpdatable#update()
     */
    @CheckReturnValue
    public WebhookManager setAvatar(Icon icon)
    {
        this.avatar = icon;
        set |= AVATAR;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} of the selected {@link net.dv8tion.jda.core.entities.Webhook Webhook}.
     * <br>Wraps {@link WebhookManagerUpdatable#getChannelField()}
     *
     * <p>A webhook channel <b>must not</b> be {@code null} and <b>must</b> be in the same {@link net.dv8tion.jda.core.entities.Guild Guild}!
     *
     * @param  channel
     *         The new {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         for the selected {@link net.dv8tion.jda.core.entities.Webhook Webhook}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_WEBHOOKS MANAGE_WEBHOOKS}
     *         in either the current or the specified TextChannel
     * @throws IllegalArgumentException
     *         If the provided channel is {@code null} or from a different Guild
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link WebhookManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.WebhookManagerUpdatable#getChannelField()
     * @see    net.dv8tion.jda.core.managers.WebhookManagerUpdatable#update()
     */
    @CheckReturnValue
    public WebhookManager setChannel(TextChannel channel)
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
        if (!getGuild().getSelfMember().hasPermission(getChannel(), Permission.MANAGE_WEBHOOKS))
            throw new InsufficientPermissionException(Permission.MANAGE_WEBHOOKS);

        JSONObject data = new JSONObject();
        if (shouldUpdate(NAME))
            data.put("name", name);
        if (shouldUpdate(CHANNEL))
            data.put("channel_id", channel);
        if (shouldUpdate(AVATAR))
            data.put("avatar", avatar == null ? JSONObject.NULL : avatar.getEncoding());

        return RequestBody.create(Requester.MEDIA_TYPE_JSON, data.toString());
    }
}
