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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.fields.GuildField;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.json.JSONObject;

public class GuildManagerUpdatable
{    
    protected final Guild guild;
//    protected AvatarUtil.Avatar icon = null;
//    protected AvatarUtil.Avatar splash

    protected GuildField<String> name;
    protected GuildField<Guild.Timeout> timeout;
    protected GuildField<Region> region;
    protected GuildField<VoiceChannel> afkChannel;
    protected GuildField<Guild.VerificationLevel> verificationLevel;
    protected GuildField<Guild.NotificationLevel> defaultNotificationLevel;
    protected GuildField<Guild.MFALevel> mfaLevel;

    public GuildManagerUpdatable(Guild guild)
    {
        this.guild = guild;
        setupFields();
    }

    public JDA getJDA()
    {
        return guild.getJDA();
    }

    public Guild getGuild()
    {
        return guild;
    }

    public GuildField<String> getNameField()
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_SERVER);

        return name;
    }

    public GuildField<Region> getRegionField()
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_SERVER);

        return region;
    }

//    /**
//     * Changes the icon of this Guild.<br>
//     * You can create the icon via the {@link net.dv8tion.jda.utils.AvatarUtil AvatarUtil} class.
//     * Passing in null will keep the current icon,
//     * while {@link net.dv8tion.jda.utils.AvatarUtil#DELETE_AVATAR DELETE_AVATAR} removes the current one.
//     *
//     * This change will only be applied, if {@link #update()} is called.
//     * So multiple changes can be made at once.
//     *
//     * @param avatar
//     *          the new icon, null to keep current, or AvatarUtil.DELETE_AVATAR to delete
//     * @return
//     *      This {@link net.dv8tion.jda.core.managers.GuildManager GuildManager} instance. Useful for chaining.
//     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
//     *      if the guild is temporarily unavailable
//     */
//    public GuildManager setIcon(AvatarUtil.Avatar avatar)
//    {
//        checkAvailable();
//        checkPermission(Permission.MANAGE_SERVER);
//
//        this.icon = avatar;
//        return this;
//    }

    public GuildField<VoiceChannel> getAfkChannelField()
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_SERVER);

        return afkChannel;
    }

    /**
     * Valid timeouts are: 60, 300, 900, 1800, 3600.
     */
    public GuildField<Guild.Timeout> getAfkTimeoutField()
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_SERVER);

        return timeout;
    }

    public GuildField<Guild.VerificationLevel> getVerificationLevelField()
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_SERVER);

        return verificationLevel;
    }

    public GuildField<Guild.NotificationLevel> getDefaultNotificationLevelField()
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_SERVER);

        return defaultNotificationLevel;
    }

    public GuildField<Guild.MFALevel> getRequiredMFALevelField()
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_SERVER);

        return mfaLevel;
    }

    /**
     * Resets all queued updates. So the next call to {@link #update()} will change nothing.
     */
    public void reset()
    {
        this.name.reset();
        this.region.reset();
        this.timeout.reset();
        this.afkChannel.reset();
        this.verificationLevel.reset();
        this.defaultNotificationLevel.reset();
        this.mfaLevel.reset();
    }

    /**
     * This method will apply all accumulated changes received by setters
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
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
//        if (icon != null)
//            frame.put("icon", icon == AvatarUtil.DELETE_AVATAR ? JSONObject.NULL : icon.getEncoded());
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
                {
                    request.onSuccess(null);
                }
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
//                || icon != null
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
                checkNull(value, "guild name");
                if (value.length() < 2 || value.length() > 100)
                    throw new IllegalArgumentException("Provided guild name must be 2 to 100 characters in length");
            }
        };

        this.timeout = new GuildField<Guild.Timeout>(this, guild::getAfkTimeout)
        {
            @Override
            public void checkValue(Guild.Timeout value)
            {
                checkNull(value, "Timeout");
            }
        };

        this.region = new GuildField<Region>(this, guild::getRegion)
        {
            @Override
            public void checkValue(Region value)
            {
                checkNull(value, "Region");
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
                checkNull(value, "VerificationLevel");
                if (value == Guild.VerificationLevel.UNKNOWN)
                    throw new IllegalArgumentException("Cannot set Guild VerificationLevel to UNKNOWN");
            }
        };

        this.defaultNotificationLevel = new GuildField<Guild.NotificationLevel>(this, guild::getDefaultNotificationLevel)
        {
            @Override
            public void checkValue(Guild.NotificationLevel value)
            {
                checkNull(value, "NotificationLevel");
                if (value == Guild.NotificationLevel.UNKNOWN)
                    throw new IllegalArgumentException("Cannot set NotificationLevel to UNKNOWN");
            }
        };

        this.mfaLevel = new GuildField<Guild.MFALevel>(this, guild::getRequiredMFALevel)
        {
            @Override
            public void checkValue(Guild.MFALevel value)
            {
                checkNull(value, "MFALevel");
                if (value == Guild.MFALevel.UNKNOWN)
                    throw new IllegalArgumentException("Cannot set MFALevel to UNKNOWN");
            }
        };
    }
}
