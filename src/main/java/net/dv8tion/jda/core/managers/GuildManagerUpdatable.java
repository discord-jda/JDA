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
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.fields.GuildField;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.apache.http.util.Args;
import org.json.JSONObject;

/**
 * An {@link #update() updatable} manager that allows
 * to modify guild settings like the {@link #getNameField() name} or the {@link #getSplashField() splash}.
 *
 * <p>This manager allows to modify multiple fields at once
 * by getting the {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} for specific
 * properties and setting or resetting their values; followed by a call of {@link #update()}!
 *
 * <p>The {@link net.dv8tion.jda.core.managers.GuildManager GuildManager} implementation
 * simplifies this process by giving simple setters that return the {@link #update() update} {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 *
 * <p><b>Note</b>: To {@link #update() update} this manager
 * the currently logged in account requires the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_SERVER MANAGE_SERVER}
 *
 * <p><b>To use moderation abilities like creating Roles or banning Members use {@link net.dv8tion.jda.core.managers.GuildController GuildController}</b>
 */
public class GuildManagerUpdatable
{    
    protected final Guild guild;

    protected GuildField<String> name;
    protected GuildField<Icon> icon;
    protected GuildField<Icon> splash;
    protected GuildField<Region> region;
    protected GuildField<VoiceChannel> afkChannel;
    protected GuildField<Guild.VerificationLevel> verificationLevel;
    protected GuildField<Guild.NotificationLevel> defaultNotificationLevel;
    protected GuildField<Guild.MFALevel> mfaLevel;
    protected GuildField<Guild.Timeout> timeout;

    /**
     * Creates a new GuildManagerUpdatable instance
     *
     * @param guild
     *        The {@link net.dv8tion.jda.core.entities.Guild Guild} that should be modified
     */
    public GuildManagerUpdatable(Guild guild)
    {
        this.guild = guild;
        setupFields();
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Manager
     *
     * @return the corresponding JDA instance
     */
    public JDA getJDA()
    {
        return guild.getJDA();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} object of this Manager.
     * Useful if this Manager was returned via a create function
     *
     * @return The {@link net.dv8tion.jda.core.entities.Guild Guild} of this Manager
     */
    public Guild getGuild()
    {
        return guild;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField}
     * for the <b><u>name</u></b> of the selected {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(String)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} instance.
     *
     * <p>A guild name <b>must not</b> be {@code null} nor less than 2 characters or more than 100 characters long!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the Guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} - Type: {@code String}
     */
    public GuildField<String> getNameField()
    {
        checkAvailable();

        return name;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField}
     * for the {@link net.dv8tion.jda.core.Region Region} of the selected {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Region)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} instance.
     *
     * <p>A guild region <b>must not</b> be {@code null} nor {@link net.dv8tion.jda.core.Region#UNKNOWN Region.UNKNOWN}!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the Guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} - Type: {@link net.dv8tion.jda.core.Region Region}
     */
    public GuildField<Region> getRegionField()
    {
        checkAvailable();

        return region;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField}
     * for the {@link net.dv8tion.jda.core.entities.Icon Icon} of the selected {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>To reset the Icon of a Guild provide {@code null} to {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Icon)}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Icon)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} instance.
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the Guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} - Type: {@link net.dv8tion.jda.core.entities.Icon Icon}
     */
    public GuildField<Icon> getIconField()
    {
        checkAvailable();

        return icon;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField}
     * for the <b><u>splash {@link net.dv8tion.jda.core.entities.Icon Icon}</u></b> of the selected {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>To reset the splash of a Guild provide {@code null} to {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Icon)}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Icon)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} instance.
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the Guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} - Type: {@link net.dv8tion.jda.core.entities.Icon Icon}
     */
    public GuildField<Icon> getSplashField()
    {
        checkAvailable();

        return splash;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField}
     * for the <b><u>AFK {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}</u></b> of the selected {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>To reset the channel of a Guild provide {@code null} to {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(VoiceChannel)}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(VoiceChannel)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} instance.
     *
     * <p>A guild afk channel <b>must</b> be from this Guild!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the Guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} - Type: {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     */
    public GuildField<VoiceChannel> getAfkChannelField()
    {
        checkAvailable();

        return afkChannel;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField}
     * for the <b><u>AFK {@link net.dv8tion.jda.core.entities.Guild.Timeout Timeout}</u></b> of the selected {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>Valid timeouts (in seconds) are 60, 300, 900, 1800, 3600. Default value is {@code 300} (5 minutes)
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Guild.Timeout)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} instance.
     *
     * <p>A guild afk timeout <b>must not</b> be {@code null}!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the Guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} - Type: {@link net.dv8tion.jda.core.entities.Guild.Timeout Guild.Timeout}
     */
    public GuildField<Guild.Timeout> getAfkTimeoutField()
    {
        checkAvailable();

        return timeout;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField}
     * for the <b><u>{@link net.dv8tion.jda.core.entities.Guild.VerificationLevel Verification Level}</u></b> of the selected {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>The default value is {@link net.dv8tion.jda.core.entities.Guild.VerificationLevel#NONE NONE}
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Guild.VerificationLevel)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} instance.
     *
     * <p>A guild verification level <b>must not</b> be {@code null} or {@link net.dv8tion.jda.core.entities.Guild.VerificationLevel#UNKNOWN UNKNOWN}!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the Guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} - Type: {@link net.dv8tion.jda.core.entities.Guild.VerificationLevel Guild.VerificationLevel}
     */
    public GuildField<Guild.VerificationLevel> getVerificationLevelField()
    {
        checkAvailable();

        return verificationLevel;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField}
     * for the <b><u>{@link net.dv8tion.jda.core.entities.Guild.NotificationLevel Notification Level}</u></b> of the selected {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>The default value is {@link net.dv8tion.jda.core.entities.Guild.NotificationLevel#ALL_MESSAGES ALL_MESSAGES}
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Guild.NotificationLevel)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} instance.
     *
     * <p>A guild notification level <b>must not</b> be {@code null} or {@link net.dv8tion.jda.core.entities.Guild.NotificationLevel#UNKNOWN UNKNOWN}!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the Guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} - Type: {@link net.dv8tion.jda.core.entities.Guild.NotificationLevel Guild.NotificationLevel}
     */
    public GuildField<Guild.NotificationLevel> getDefaultNotificationLevelField()
    {
        checkAvailable();

        return defaultNotificationLevel;
    }

    /**
     * An {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField}
     * for the <b><u>{@link net.dv8tion.jda.core.entities.Guild.NotificationLevel Notification Level}</u></b> of the selected {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>The default value is {@link net.dv8tion.jda.core.entities.Guild.MFALevel#NONE NONE}
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Guild.MFALevel)}
     * on the returned {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} instance.
     *
     * <p>A guild mfa level <b>must not</b> be {@code null} or {@link net.dv8tion.jda.core.entities.Guild.MFALevel#UNKNOWN UNKNOWN}!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the Guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.managers.fields.GuildField GuildField} - Type: {@link net.dv8tion.jda.core.entities.Guild.MFALevel Guild.MFALevel}
     */
    public GuildField<Guild.MFALevel> getRequiredMFALevelField()
    {
        checkAvailable();
        return mfaLevel;
    }

    /**
     * Resets all queued updates. So the next call to {@link #update()} will change nothing.
     * <br>This is automatically called by {@link #update()}
     */
    public void reset()
    {
        this.name.reset();
        this.region.reset();
        this.timeout.reset();
        this.icon.reset();
        this.splash.reset();
        this.afkChannel.reset();
        this.verificationLevel.reset();
        this.defaultNotificationLevel.reset();
        this.mfaLevel.reset();
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
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_GUILD UNKNOWN_GUILD}
     *      <br>If the Guild was deleted before finishing the task</li>
     *
     *      <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *      <br>If the currently logged in account loses the {@link net.dv8tion.jda.core.Permission#MANAGE_SERVER MANAGE_SERVER Permission}
     *          before finishing the task</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_SERVER MANAGE_SERVER}
     *         in the underlying {@link net.dv8tion.jda.core.entities.Guild Guild}
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the Guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     *         <br>Applies all changes that have been made in a single api-call.
     */
    public RestAction<Void> update()
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_SERVER);

        if (!needToUpdate())
            return new RestAction.EmptyRestAction<>(null);

        JSONObject body = new JSONObject().put("name", guild.getName());
        if (name.shouldUpdate())
            body.put("name", name.getValue());
        if (region.shouldUpdate())
            body.put("region", region.getValue().getKey());
        if (timeout.shouldUpdate())
            body.put("afk_timeout", timeout.getValue().getSeconds());
        if (icon.shouldUpdate())
            body.put("icon", icon.getValue() == null ? JSONObject.NULL : icon.getValue().getEncoding());
        if (splash.shouldUpdate())
            body.put("splash", splash.getValue() == null ? JSONObject.NULL : splash.getValue().getEncoding());
        if (afkChannel.shouldUpdate())
            body.put("afk_channel_id", afkChannel.getValue() == null ? JSONObject.NULL : afkChannel.getValue().getId());
        if (verificationLevel.shouldUpdate())
            body.put("verification_level", verificationLevel.getValue().getKey());
        if (defaultNotificationLevel.shouldUpdate())
            body.put("default_notification_level", defaultNotificationLevel.getValue().getKey());
        if (mfaLevel.shouldUpdate())
            body.put("mfa_level", mfaLevel.getValue().getKey());

        reset(); //now that we've built our JSON object, reset the manager back to the non-modified state
        Route.CompiledRoute route = Route.Guilds.MODIFY_GUILD.compile(guild.getId());
        return new RestAction<Void>(guild.getJDA(), route, body)
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

    protected boolean needToUpdate()
    {
        return name.shouldUpdate()
                || region.shouldUpdate()
                || timeout.shouldUpdate()
                || icon.shouldUpdate()
                || splash.shouldUpdate()
                || afkChannel.shouldUpdate()
                || verificationLevel.shouldUpdate()
                || defaultNotificationLevel.shouldUpdate()
                || mfaLevel.shouldUpdate();
    }

    protected void checkAvailable()
    {
        if (!guild.isAvailable())
            throw new GuildUnavailableException();
    }

    protected void checkPermission(Permission perm)
    {
        if (!PermissionUtil.checkPermission(guild, guild.getSelfMember(), perm))
            throw new PermissionException(perm);
    }

    protected void setupFields()
    {
        this.name = new GuildField<String>(this, guild::getName)
        {
            @Override
            public void checkValue(String value)
            {
                Args.notNull(value, "guild name");
                if (value.length() < 2 || value.length() > 100)
                    throw new IllegalArgumentException("Provided guild name must be 2 to 100 characters in length");
            }
        };

        this.timeout = new GuildField<Guild.Timeout>(this, guild::getAfkTimeout)
        {
            @Override
            public void checkValue(Guild.Timeout value)
            {
                Args.notNull(value, "Timeout");
            }
        };

        this.icon = new GuildField<Icon>(this, null)
        {
            @Override
            public void checkValue(Icon value) { }

            @Override
            public Icon getOriginalValue()
            {
                throw new UnsupportedOperationException("Cannot easily provide the original Icon. Use Guild#getIconUrl() and download it yourself.");
            }

            @Override
            public boolean shouldUpdate()
            {
                return isSet();
            }
        };

        this.splash = new GuildField<Icon>(this, null)
        {
            @Override
            public void checkValue(Icon value) { }

            @Override
            public Icon getOriginalValue()
            {
                throw new UnsupportedOperationException("Cannot easily provide the original Splash. Use Guild#getSplashUrl() and download it yourself.");
            }

            @Override
            public boolean shouldUpdate()
            {
                return isSet();
            }
        };

        this.region = new GuildField<Region>(this, guild::getRegion)
        {
            @Override
            public void checkValue(Region value)
            {
                Args.notNull(value, "Region");
                if (value == Region.UNKNOWN)
                    throw new IllegalArgumentException("Cannot set Guild Region to UNKNOWN!");
            }
        };

        this.afkChannel = new GuildField<VoiceChannel>(this, guild::getAfkChannel)
        {
            @Override
            public void checkValue(VoiceChannel value)
            {
                if (value != null && !guild.equals(value.getGuild()))
                    throw new IllegalArgumentException("Provided AFK Channel is not from this Guild!");
            }
        };

        this.verificationLevel = new GuildField<Guild.VerificationLevel>(this, guild::getVerificationLevel)
        {
            @Override
            public void checkValue(Guild.VerificationLevel value)
            {
                Args.notNull(value, "VerificationLevel");
                if (value == Guild.VerificationLevel.UNKNOWN)
                    throw new IllegalArgumentException("Cannot set Guild VerificationLevel to UNKNOWN");
            }
        };

        this.defaultNotificationLevel = new GuildField<Guild.NotificationLevel>(this, guild::getDefaultNotificationLevel)
        {
            @Override
            public void checkValue(Guild.NotificationLevel value)
            {
                Args.notNull(value, "NotificationLevel");
                if (value == Guild.NotificationLevel.UNKNOWN)
                    throw new IllegalArgumentException("Cannot set NotificationLevel to UNKNOWN");
            }
        };

        this.mfaLevel = new GuildField<Guild.MFALevel>(this, guild::getRequiredMFALevel)
        {
            @Override
            public void checkValue(Guild.MFALevel value)
            {
                Args.notNull(value, "MFALevel");
                if (value == Guild.MFALevel.UNKNOWN)
                    throw new IllegalArgumentException("Cannot set MFALevel to UNKNOWN");
            }
        };
    }
}
