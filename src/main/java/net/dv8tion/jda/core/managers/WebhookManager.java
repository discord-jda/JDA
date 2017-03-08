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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.requests.RestAction;

/**
 * Facade for a {@link net.dv8tion.jda.core.managers.WebhookManagerUpdatable WebhookManagerUpdatable} instance.
 * <br>Simplifies managing flow for convenience.
 *
 * <p>This decoration allows to modify a single field by automatically building an update {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 */
public class WebhookManager
{
    protected WebhookManagerUpdatable manager;

    /**
     * Creates a new WebhookManager instance
     *
     * @param webhook
     *        The target {@link net.dv8tion.jda.core.entities.Webhook Webhook} to modify
     */
    public WebhookManager(Webhook webhook)
    {
        this.manager = new WebhookManagerUpdatable(webhook);
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Manager
     *
     * @return the corresponding JDA instance
     */
    public JDA getJDA()
    {
        return manager.getJDA();
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
        return getWebhook().getGuild();
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
        return getWebhook().getChannel();
    }

    /**
     * The target {@link net.dv8tion.jda.core.entities.Webhook Webhook}
     * that will be modified by this manager
     *
     * @return The target {@link net.dv8tion.jda.core.entities.Webhook Webhook}
     */
    public Webhook getWebhook()
    {
        return manager.getWebhook();
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
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_WEBHOOKS MANAGE_WEBHOOKS}
     * @throws IllegalArgumentException
     *         If the provided name is {@code null}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link WebhookManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.WebhookManagerUpdatable#getNameField()
     * @see    net.dv8tion.jda.core.managers.WebhookManagerUpdatable#update()
     */
    public RestAction<Void> setName(String name)
    {
        return manager.getNameField().setValue(name).update();
    }

    /**
     * Sets the <b><u>default avatar</u></b> of the selected {@link net.dv8tion.jda.core.entities.Webhook Webhook}.
     * <br>Wraps {@link net.dv8tion.jda.core.managers.WebhookManagerUpdatable#getAvatarField()}
     *
     * @param  icon
     *         The new default avatar {@link net.dv8tion.jda.core.entities.Icon Icon}
     *         for the selected {@link net.dv8tion.jda.core.entities.Webhook Webhook}
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_WEBHOOKS MANAGE_WEBHOOKS}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link WebhookManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.WebhookManagerUpdatable#getAvatarField()
     * @see    net.dv8tion.jda.core.managers.WebhookManagerUpdatable#update()
     */
    public RestAction<Void> setAvatar(Icon icon)
    {
        return manager.getAvatarField().setValue(icon).update();
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
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_WEBHOOKS MANAGE_WEBHOOKS}
     *         in either the current or the specified TextChannel
     * @throws IllegalArgumentException
     *         If the provided channel is {@code null} or from a different Guild
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link WebhookManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.WebhookManagerUpdatable#getChannelField()
     * @see    net.dv8tion.jda.core.managers.WebhookManagerUpdatable#update()
     */
    public RestAction<Void> setChannel(TextChannel channel)
    {
        return manager.getChannelField().setValue(channel).update();
    }

}
