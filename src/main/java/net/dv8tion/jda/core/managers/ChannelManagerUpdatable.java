
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

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.fields.ChannelField;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.utils.Checks;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;

/**
 * An {@link #update() updatable} manager that allows
 * to modify channel settings like the {@link #getNameField() name}
 * or for {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} or the {@link #getTopicField() topic}.
 *
 * <p>This manager allows to modify multiple fields at once
 * by getting the {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelFields} for specific
 * properties and setting or resetting their values; followed by a call of {@link #update()}!
 *
 * <p>The {@link net.dv8tion.jda.core.managers.ChannelManager ChannelManager} implementation
 * simplifies this process by giving simple setters that return the {@link #update() update} {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 *
 * <p><b>Note</b>: To {@link #update() update} this manager
 * the currently logged in account requires the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL}
 */
@Deprecated
public class ChannelManagerUpdatable
{
    protected final Channel channel;

    protected ChannelField<String> name;
    protected ChannelField<String> topic;
    protected ChannelField<Integer> userLimit;
    protected ChannelField<Integer> bitrate;
    protected ChannelField<Boolean> nsfw;
    protected ChannelField<Category> parent;
    protected ChannelField<Integer> position;

    /**
     * Creates a new ChannelManagerUpdatable instance
     *
     * @param channel
     *        The {@link net.dv8tion.jda.core.entities.Channel Channel} to modify
     */
    public ChannelManagerUpdatable(Channel channel)
    {
        this.channel = channel;
        setupFields();
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Manager
     *
     * @return the corresponding JDA instance
     */
    public JDA getJDA()
    {
        return channel.getJDA();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Channel Channel} that will
     * be modified by this Manager instance
     *
     * @return The {@link net.dv8tion.jda.core.entities.Channel Channel}
     */
    public Channel getChannel()
    {
        return channel;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} this Manager's
     * {@link net.dv8tion.jda.core.entities.Channel Channel} is in.
     * <br>This is logically the same as calling {@code getChannel().getGuild()}
     *
     * @return The parent {@link net.dv8tion.jda.core.entities.Guild Guild}
     */
    public Guild getGuild()
    {
        return channel.getGuild();
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField}
     * for the <b><u>name</u></b> of the selected {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(String)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField} instance.
     *
     * <p>A channel name <b>must not</b> be {@code null} nor less than 2 characters or more than 100 characters long!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     *  @return {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField} - Type: {@code String}
     */
    public ChannelField<String> getNameField()
    {
        return name;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField}
     * for the <b><u>{@link net.dv8tion.jda.core.entities.Category Parent Category}</u></b>
     * of the selected {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Category)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField} instance.
     *
     * @throws java.lang.UnsupportedOperationException
     *         If this is the manager of a {@link net.dv8tion.jda.core.entities.Category Category}
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField}
     *         - Type: {@link net.dv8tion.jda.core.entities.Category Category}
     *
     * @since  3.4.0
     */
    public ChannelField<Category> getParentField()
    {
        if (channel instanceof Category)
            throw new UnsupportedOperationException("Setting the parent is not allowed on categories!");
        return parent;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField}
     * for the <b><u>topic</u></b> of the selected {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(String)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField} instance.
     *
     * <p>A channel topic <b>must not</b> be more than {@code 1024} characters long!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * <p><b>This is only available to {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}</b>
     *
     * @throws UnsupportedOperationException
     *         If the selected {@link net.dv8tion.jda.core.entities.Channel Channel}'s type is not {@link net.dv8tion.jda.core.entities.ChannelType#TEXT TEXT}
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField} - Type: {@code String}
     */
    public ChannelField<String> getTopicField()
    {
        if (channel.getType() != ChannelType.TEXT)
            throw new UnsupportedOperationException("Setting a Topic is only allowed on TextChannels!");

        return topic;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField}
     * for the <b><u>user-limit</u></b> of the selected {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Integer)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField} instance.
     *
     * <p>A channel user-limit <b>must not</b> be negative nor greater than {@code 99}!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * <p><b>This is only available to {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}</b>
     *
     * @throws UnsupportedOperationException
     *         If the selected {@link net.dv8tion.jda.core.entities.Channel Channel}'s type is not {@link net.dv8tion.jda.core.entities.ChannelType#VOICE VOICE}
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField} - Type: {@code Integer}
     */
    public ChannelField<Integer> getUserLimitField()
    {
        if (channel.getType() != ChannelType.VOICE)
            throw new UnsupportedOperationException("Setting user limit is only allowed on VoiceChannels!");

        return userLimit;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField}
     * for the <b><u>nsfw flag</u></b> of the selected {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Boolean)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField} instance.
     *
     * <p><b>This is only available to {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}</b>
     *
     * @throws UnsupportedOperationException
     *         If the selected {@link net.dv8tion.jda.core.entities.Channel Channel}'s type is not {@link net.dv8tion.jda.core.entities.ChannelType#TEXT TEXT}
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField} - Type: {@code boolean}
     */
    public ChannelField<Boolean> getNSFWField()
    {
        if (channel.getType() != ChannelType.TEXT)
            throw new UnsupportedOperationException("Setting the nsfw flag is only allowed on TextChannels!");

        return nsfw;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField}
     * for the <b><u>bitrate</u></b> of the selected {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Integer)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField} instance.
     *
     * <p>A channel bitrate <b>must not</b> be less than {@code 8000} and cannot exceed {@code 96000} (for non-vip Guilds)!
     * {@link net.dv8tion.jda.core.entities.Guild#getFeatures() VIP Guilds} allow a bitrate for up to {@code 128000}.
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * <p><b>This is only available to {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}</b>
     *
     * @throws UnsupportedOperationException
     *         If the selected {@link net.dv8tion.jda.core.entities.Channel Channel}'s type is not {@link net.dv8tion.jda.core.entities.ChannelType#VOICE VOICE}
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField} - Type: {@code Integer}
     *
     * @see    net.dv8tion.jda.core.entities.Guild#getFeatures()
     */
    public ChannelField<Integer> getBitrateField()
    {
        if (channel.getType() != ChannelType.VOICE)
            throw new UnsupportedOperationException("Setting user limit is only allowed on VoiceChannels!");

        return bitrate;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField}
     * for the <b><u>position</u></b> of the selected {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * <p><b>To modify multiple channels you should use
     * <code>Guild.{@link net.dv8tion.jda.core.managers.GuildController getController()}.{@link GuildController#modifyTextChannelPositions() modifyTextChannelPositions()}</code>
     * instead! This is not the same as looping through channels and using this to update positions!</b>
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Integer)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField} instance.
     *
     * <p>A channel position <b>must not</b> be {@code null}!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.ChannelField ChannelField} - Type: {@code Integer}
     */
    public ChannelField<Integer> getPositionField()
    {
        if (!getGuild().getSelfMember().hasPermission(Permission.MANAGE_CHANNEL))
            throw new InsufficientPermissionException(Permission.MANAGE_CHANNEL);
        return position;
    }

    /**
     * Resets all {@link net.dv8tion.jda.core.managers.fields.ChannelField Fields}
     * for this manager instance by calling {@link net.dv8tion.jda.core.managers.fields.Field#reset() Field.reset()} sequentially
     * <br>This is automatically called by {@link #update()}
     */
    public void reset()
    {
        this.name.reset();
        if (channel instanceof TextChannel)
        {
            this.parent.reset();
            this.topic.reset();
        }
        else if (channel instanceof VoiceChannel)
        {
            this.parent.reset();
            this.bitrate.reset();
            this.userLimit.reset();
        }
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
     *      <br>If the Channel was deleted before finishing the task</li>
     *
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *      <br>If the currently logged in account was removed from the Guild before finishing the task</li>
     *
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *      <br>If the currently logged in account loses the {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL Permission}
     *          before finishing the task</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL}
     *         in the underlying {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Applies all changes that have been made in a single api-call.
     */
    @CheckReturnValue
    public AuditableRestAction<Void> update()
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        if (!needToUpdate())
            return new AuditableRestAction.EmptyRestAction<>(getJDA(), null);

        JSONObject frame = new JSONObject().put("name", channel.getName());
        if (name.shouldUpdate())
            frame.put("name", name.getValue());
        if (position.shouldUpdate())
            frame.put("position", position.getValue());
        if (topic != null && topic.shouldUpdate())
            frame.put("topic", topic.getValue() == null ? JSONObject.NULL : topic.getValue());
        if (nsfw != null && nsfw.shouldUpdate())
            frame.put("nsfw", nsfw.getValue());
        if (userLimit != null && userLimit.shouldUpdate())
            frame.put("user_limit", userLimit.getValue());
        if (bitrate != null && bitrate.shouldUpdate())
            frame.put("bitrate", bitrate.getValue());
        if (parent != null && parent.shouldUpdate())
            frame.put("parent_id", parent.getValue() == null ? JSONObject.NULL : parent.getValue().getIdLong());

        reset();    //now that we've built our JSON object, reset the manager back to the non-modified state
        Route.CompiledRoute route = Route.Channels.MODIFY_CHANNEL.compile(channel.getId());
        return new AuditableRestAction<Void>(channel.getJDA(), route, frame)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    protected boolean needToUpdate()
    {
        return name.shouldUpdate()
                || position.shouldUpdate()
                || (parent != null && parent.shouldUpdate())
                || (topic != null && topic.shouldUpdate())
                || (userLimit != null && userLimit.shouldUpdate())
                || (bitrate != null && bitrate.shouldUpdate())
                || (nsfw != null && nsfw.shouldUpdate());
    }

    protected void checkPermission(Permission perm)
    {
        if (!getGuild().getSelfMember().hasPermission(channel, perm))
            throw new InsufficientPermissionException(perm);
    }

    protected void setupFields()
    {
        this.name = new ChannelField<String>(this, channel::getName)
        {
            @Override
            public void checkValue(String value)
            {
                Checks.notEmpty(value, "name");
                if (value.length() < 2 || value.length() > 100)
                    throw new IllegalArgumentException("Provided channel name must be 2 to 100 characters in length");
            }
        };

        this.position = new ChannelField<Integer>(this, channel::getPositionRaw)
        {
            @Override
            public void checkValue(Integer value)
            {
                Checks.notNull(value, "Position");
            }
        };

        if (!(channel instanceof Category))
        {
            this.parent = new ChannelField<Category>(this, channel::getParent)
            {
                @Override
                public void checkValue(Category value)
                {
                    if (value != null)
                        Checks.check(value.getGuild().equals(getGuild()), "Category is not from same Guild!");
                }
            };
        }

        if (channel instanceof TextChannel)
        {
            TextChannel tc = (TextChannel) channel;
            this.topic = new ChannelField<String>(this, tc::getTopic)
            {
                @Override
                public void checkValue(String value)
                {
                    if (value != null && value.length() > 1024)
                        throw new IllegalArgumentException("Provided topic must less than or equal to 1024 characters in length");
                }
            };

            this.nsfw = new ChannelField<Boolean>(this, tc::isNSFW)
            {
                @Override
                public void checkValue(Boolean value)
                {
                    if (value == null)
                        throw new IllegalArgumentException("NSFW flag must not be null");
                }
            };
        }
        else if (channel instanceof VoiceChannel)
        {
            VoiceChannel vc = (VoiceChannel) channel;
            this.userLimit = new ChannelField<Integer>(this, vc::getUserLimit)
            {
                @Override
                public void checkValue(Integer value)
                {
                    Checks.notNull(value, "user limit");
                    if (value < 0 || value > 99)
                        throw new IllegalArgumentException("Provided user limit must be 0 to 99.");
                }
            };

            this.bitrate = new ChannelField<Integer>(this, vc::getBitrate)
            {
                @Override
                public void checkValue(Integer value)
                {
                    Checks.notNull(value, "bitrate");
                    if (value < 8000 || value > (getGuild().getFeatures().contains("VIP_REGIONS") ? 128000 : 96000))
                        throw new IllegalArgumentException("Provided bitrate must be 8000 to 96000 (128000 for VIP Guilds)");
                }
            };
        }

    }
}
