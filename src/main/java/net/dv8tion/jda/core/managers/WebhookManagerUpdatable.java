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
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.fields.WebhookField;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.apache.http.util.Args;
import org.json.JSONObject;

/**
 * An {@link #update() updatable} manager that allows
 * to modify webhook settings like the {@link #getNameField() default name} or the {@link #getAvatarField() default avatar}.
 *
 * <p>This manager allows to modify multiple fields at once
 * by getting the {@link net.dv8tion.jda.core.managers.fields.WebhookField WebhookField} for specific
 * properties and setting or resetting their values; followed by a call of {@link #update()}!
 *
 * <p>The {@link net.dv8tion.jda.core.managers.WebhookManager WebhookManager} implementation
 * simplifies this process by giving simple setters that return the {@link #update() update} {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 *
 * <p><b>Note</b>: To {@link #update() update} this manager
 * the currently logged in account requires the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_WEBHOOKS MANAGE_WEBHOOKS} in the parent TextChannel
 */
public class WebhookManagerUpdatable
{
    protected final Webhook webhook;

    protected WebhookField<String> name;
    protected WebhookField<Icon> avatar;
    protected WebhookField<TextChannel> channel;

    /**
     * Creates a new WebhookManagerUpdatable instance
     *
     * @param webhook
     *        The target {@link net.dv8tion.jda.core.entities.Webhook Webhook} to modify
     */
    public WebhookManagerUpdatable(Webhook webhook)
    {
        this.webhook = webhook;
        setupFields();
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Manager
     *
     * @return the corresponding JDA instance
     */
    public JDA getJDA()
    {
        return webhook.getJDA();
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
     * A {@link net.dv8tion.jda.core.managers.fields.WebhookField WebhookField}
     * for the <b><u>name</u></b> of the selected {@link net.dv8tion.jda.core.entities.Webhook Webhook}'s {@link Webhook#getDefaultUser() default User}.
     * <br>Default value: Not given
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(String)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.WebhookField WebhookField} instance.
     *
     * <p>A Webhook name <b>must bot</b> be {@code null}!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.WebhookField WebhookField} - Type: {@code String}
     */
    public WebhookField<String> getNameField()
    {
        return name;
    }

    /**
     * A {@link net.dv8tion.jda.core.managers.fields.WebhookField WebhookField}
     * for the <b><u>avatar</u></b> of the selected {@link net.dv8tion.jda.core.entities.Webhook Webhook}'s {@link Webhook#getDefaultUser() default User}.
     * <br>Default value: {@code null}
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Icon)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.WebhookField WebhookField} instance.
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.WebhookField WebhookField} - Type: {@link net.dv8tion.jda.core.entities.Icon}
     */
    public WebhookField<Icon> getAvatarField()
    {
        return avatar;
    }

    /**
     * A {@link net.dv8tion.jda.core.managers.fields.WebhookField WebhookField}
     * for the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     * of the selected {@link net.dv8tion.jda.core.entities.Webhook Webhook}'s {@link Webhook#getDefaultUser() default User}.
     * <br>Default value: Not given
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(TextChannel)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.WebhookField WebhookField} instance.
     *
     * <p>A Webhook channel <b>must bot</b> be {@code null} and <b>must</b> be from the same {@link net.dv8tion.jda.core.entities.Guild Guild}!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.WebhookField WebhookField} - Type: {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     */
    public WebhookField<TextChannel> getChannelField()
    {
        return channel;
    }

    /**
     * Resets all {@link net.dv8tion.jda.core.managers.fields.Field Fields}
     * for this manager instance by calling {@link net.dv8tion.jda.core.managers.fields.Field#reset() Field.reset()} sequentially
     * <br>This is automatically called by {@link #update()}
     */
    public void reset()
    {
        name.reset();
        avatar.reset();
        channel.reset();
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.requests.RestAction RestAction} instance
     * that will apply <b>all</b> changes that have been made to this manager instance.
     * <br>If no changes have been made this will simply return {@link net.dv8tion.jda.core.requests.RestAction.EmptyRestAction EmptyRestAction}.
     *
     * <p>Before applying new changes it is recommended to call {@link #reset()} to reset previous changes.
     * <br>This is automatically called if this method returns successfully.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} for this
     * update include the following:
     * <ul>
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *      <br>If the TextChannel was deleted before finishing the task</li>
     *
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *      <br>If the currently logged in account was removed from the Guild before finishing the task</li>
     *
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *      <br>If the currently logged in account loses the {@link net.dv8tion.jda.core.Permission#MANAGE_WEBHOOKS MANAGE_WEBHOOKS Permission}</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_WEBHOOKS MANAGE_WEBHOOKS}
     *         in either the current or selected new TextChannel.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     *         <br>Applies all changes that have been made in a single api-call.
     */
    public RestAction<Void> update()
    {
        Member self = getGuild().getSelfMember();
        if (!self.hasPermission(webhook.getChannel(), Permission.MANAGE_WEBHOOKS))
            throw new PermissionException(Permission.MANAGE_WEBHOOKS);
        if (channel.isSet() && !self.hasPermission(channel.getValue(), Permission.MANAGE_WEBHOOKS))
            throw new PermissionException(Permission.MANAGE_WEBHOOKS, "Permission not available in selected new channel");
        if (!shouldUpdate())
            return new RestAction.EmptyRestAction<>(null);

        JSONObject data = new JSONObject();
        data.put("name", name.getOriginalValue());

        if (channel.shouldUpdate())
            data.put("channel_id", channel.getValue().getId());
        if (name.shouldUpdate())
            data.put("name", name.getValue());
        if (avatar.shouldUpdate())
        {
            Icon value = avatar.getValue();
            data.put("avatar", value != null ? value.getEncoding() : JSONObject.NULL);
        }

        Route.CompiledRoute route = Route.Webhooks.MODIFY_WEBHOOK.compile(webhook.getId());
        return new RestAction<Void>(getJDA(), route, data)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    protected boolean shouldUpdate()
    {
        return name.shouldUpdate()
                || avatar.shouldUpdate()
                || channel.shouldUpdate();
    }

    protected void setupFields()
    {
        name = new WebhookField<String>(this, webhook::getName)
        {
            @Override
            public void checkValue(String value)
            {
                Args.notNull(value, "default name");
            }
        };

        avatar = new WebhookField<Icon>(this, null)
        {
            @Override
            public void checkValue(Icon value) { }

            @Override
            public Icon getOriginalValue()
            {
                throw new UnsupportedOperationException("Cannot easily provide the original Avatar. Use User#getIconUrl() and download it yourself.");
            }

            @Override
            public boolean shouldUpdate()
            {
                return isSet();
            }
        };

        channel = new WebhookField<TextChannel>(this, webhook::getChannel)
        {
            @Override
            public void checkValue(TextChannel value)
            {
                Args.notNull(value, "channel");
                Args.check(value.equals(getChannel()), "Channel is not from the same Guild!");
            }
        };
    }
}
