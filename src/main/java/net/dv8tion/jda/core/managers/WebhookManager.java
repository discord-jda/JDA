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
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;

/**
 * Manager providing functionality to update one or more fields for a {@link net.dv8tion.jda.core.entities.Webhook Webhook}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setName("GitHub Webhook")
 *        .setChannel(channel)
 *        .queue();
 * manager.reset(WebhookManager.NAME | WebhookManager.AVATAR)
 *        .setName("Meme Feed")
 *        .setAvatar(null)
 *        .queue();
 * }</pre>
 *
 * @see net.dv8tion.jda.core.entities.Webhook#getManager()
 */
public class WebhookManager extends ManagerBase
{
    /** Used to reset the name field */
    public static final long NAME    = 0x1;
    /** Used to reset the channel field */
    public static final long CHANNEL = 0x2;
    /** Used to reset the avatar field */
    public static final long AVATAR  = 0x4;

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
        super(webhook.getJDA(), Route.Webhooks.MODIFY_WEBHOOK.compile(webhook.getId()));
        this.webhook = webhook;
        if (isPermissionChecksEnabled())
            checkPermissions();
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

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(WebhookManager.CHANNEL | WebhookManager.NAME);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #AVATAR}</li>
     *     <li>{@link #CHANNEL}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return WebhookManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public WebhookManager reset(long fields)
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

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(WebhookManager.CHANNEL, WebhookManager.NAME);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #AVATAR}</li>
     *     <li>{@link #CHANNEL}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return WebhookManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public WebhookManager reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    /**
     * Resets all fields for this manager.
     *
     * @return WebhookManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public WebhookManager reset()
    {
        super.reset();
        this.name = null;
        this.channel = null;
        this.avatar = null;
        return this;
    }

    /**
     * Sets the <b><u>default name</u></b> of the selected {@link net.dv8tion.jda.core.entities.Webhook Webhook}.
     *
     * <p>A webhook name <b>must not</b> be {@code null} or blank!
     *
     * @param  name
     *         The new default name for the selected {@link net.dv8tion.jda.core.entities.Webhook Webhook}
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or blank
     *
     * @return WebhookManager for chaining convenience
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
     *
     * @param  icon
     *         The new default avatar {@link net.dv8tion.jda.core.entities.Icon Icon}
     *         for the selected {@link net.dv8tion.jda.core.entities.Webhook Webhook}
     *         or {@code null} to reset
     *
     * @return WebhookManager for chaining convenience
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
     *
     * <p>A webhook channel <b>must not</b> be {@code null} and <b>must</b> be in the same {@link net.dv8tion.jda.core.entities.Guild Guild}!
     *
     * @param  channel
     *         The new {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         for the selected {@link net.dv8tion.jda.core.entities.Webhook Webhook}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_WEBHOOKS MANAGE_WEBHOOKS}
     *         in the specified TextChannel
     * @throws IllegalArgumentException
     *         If the provided channel is {@code null} or from a different Guild
     *
     * @return WebhookManager for chaining convenience
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
        JSONObject data = new JSONObject();
        if (shouldUpdate(NAME))
            data.put("name", name);
        if (shouldUpdate(CHANNEL))
            data.put("channel_id", channel);
        if (shouldUpdate(AVATAR))
            data.put("avatar", avatar == null ? JSONObject.NULL : avatar.getEncoding());

        return getRequestBody(data);
    }

    @Override
    protected boolean checkPermissions()
    {
        if (!getGuild().getSelfMember().hasPermission(getChannel(), Permission.MANAGE_WEBHOOKS))
            throw new InsufficientPermissionException(Permission.MANAGE_WEBHOOKS);
        return super.checkPermissions();
    }
}
