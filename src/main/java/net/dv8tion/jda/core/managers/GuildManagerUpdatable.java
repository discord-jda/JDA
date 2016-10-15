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

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.GuildManager.Timeout;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.json.JSONObject;

import java.util.Objects;

public class GuildManagerUpdatable
{    
    protected final Guild guild;

    protected Timeout timeout = null;
    protected String name = null;
    protected Region region = null;
//    protected AvatarUtil.Avatar icon = null;
//    protected AvatarUtil.Avatar splash
    protected VoiceChannel afkChannel;
    protected Guild.VerificationLevel verificationLevel = null;
    protected Guild.NotificationLevel defaultNotificationLevel = null;
    protected Guild.MFALevel mfaLevel = null;

    /**
     * Creates a {@link GuildManagerUpdatable} that can be used to manage
     * different aspects of the provided {@link net.dv8tion.jda.core.entities.Guild}.
     *
     * @param guild
     *          The {@link net.dv8tion.jda.core.entities.Guild} which the manager deals with.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public GuildManagerUpdatable(Guild guild)
    {
        checkAvailable();
        this.guild = guild;
        this.afkChannel = guild.getAfkChannel();
    }

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.Guild Guild} object of this Manager. Useful if this Manager was returned via a create function
     *
     * @return
     *      the {@link net.dv8tion.jda.core.entities.Guild Guild} of this Manager
     */
    public Guild getGuild()
    {
        return guild;
    }

    /**
     * Changes the name of this Guild.
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param name
     *          the new name of the Guild, or null to keep current one
     * @return
     *      This {@link GuildManagerUpdatable GuildManager} instance. Useful for chaining.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public GuildManagerUpdatable setName(String name)
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_SERVER);

        if (guild.getName().equals(name))
        {
            this.name = null;
        }
        else
        {
            this.name = name;
        }
        return this;
    }

    /**
     * Changes the {@link net.dv8tion.jda.core.Region Region} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param region
     *          the new {@link net.dv8tion.jda.core.Region Region}, or null to keep current one
     * @return
     *      This {@link GuildManagerUpdatable GuildManager} instance. Useful for chaining.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public GuildManagerUpdatable setRegion(Region region)
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_SERVER);

        if (region == guild.getRegion() || region == Region.UNKNOWN)
        {
            this.region = null;
        }
        else
        {
            this.region = region;
        }
        return this;
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

    /**
     * Changes the AFK {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} of this Guild
     * If passed null, this will disable the AFK-Channel.
     *
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param channel
     *          the new afk-channel
     * @return
     *      This {@link GuildManagerUpdatable GuildManager} instance. Useful for chaining.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public GuildManagerUpdatable setAfkChannel(VoiceChannel channel)
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_SERVER);

        if (channel != null && !guild.equals(channel.getGuild()))
        {
            throw new IllegalArgumentException("Given VoiceChannel is not member of modifying Guild");
        }
        this.afkChannel = channel;
        return this;
    }

    /**
     * Changes the AFK Timeout of this Guild
     * After given timeout (in seconds) Users being AFK in voice are being moved to the AFK-Channel
     * Valid timeouts are: 60, 300, 900, 1800, 3600.
     *
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param timeout
     *      the new afk timeout, or null to keep current one
     * @return
     *      This {@link GuildManagerUpdatable GuildManager} instance. Useful for chaining.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public GuildManagerUpdatable setAfkTimeout(Timeout timeout)
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_SERVER);

        this.timeout = timeout;
        return this;
    }

    /**
     * Changes the Verification-Level of this Guild.
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param level
     *          the new Verification-Level of the Guild, or null to keep current one
     * @return
     *      This {@link GuildManagerUpdatable GuildManager} instance. Useful for chaining.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public GuildManagerUpdatable setVerificationLevel(Guild.VerificationLevel level)
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_SERVER);

        if (guild.getVerificationLevel() == level)
        {
            this.verificationLevel = null;
        }
        else
        {
            this.verificationLevel = level;
        }
        return this;
    }

    public GuildManagerUpdatable setDefaultNotificationLevel(Guild.NotificationLevel level)
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_SERVER);

        if (guild.getDefaultNotificationLevel() != level)
            this.defaultNotificationLevel = level;
        else
            this.defaultNotificationLevel = null;

        return this;
    }

    public GuildManagerUpdatable setRequiredMFALevel(Guild.MFALevel level)
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_SERVER);

        if (guild.getRequiredMFALevel() != level)
            this.mfaLevel = level;
        else
            this.mfaLevel = null;

        return this;
    }

    /**
     * Resets all queued updates. So the next call to {@link #update()} will change nothing.
     */
    public void reset()
    {
        this.name = null;
        this.region = null;
        this.timeout = null;
//        this.icon = null;
        this.afkChannel = guild.getAfkChannel();
        this.verificationLevel = null;
        this.defaultNotificationLevel = null;
        this.mfaLevel = null;
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
            return RestAction.EMPTY_REST_ACTION;

        JSONObject frame = new JSONObject().put("name", guild.getName());
        if (name != null)
            frame.put("name", name);
        if (region != null)
            frame.put("region", region.getKey());
        if (timeout != null)
            frame.put("afk_timeout", timeout.getSeconds());
//        if (icon != null)
//            frame.put("icon", icon == AvatarUtil.DELETE_AVATAR ? JSONObject.NULL : icon.getEncoded());
        if (!Objects.equals(afkChannel, guild.getAfkChannel()))
            frame.put("afk_channel_id", afkChannel == null ? JSONObject.NULL : afkChannel.getId());
        if (verificationLevel != null)
            frame.put("verification_level", verificationLevel.getKey());
        if (defaultNotificationLevel != null)
            frame.put("default_notification_level", defaultNotificationLevel.getKey());
        if (mfaLevel != null)
            frame.put("mfa_level", mfaLevel.getKey());

        reset(); //now that we've built our JSON object, reset the manager back to the non-modified state
        Route.CompiledRoute route = Route.Guilds.MODIFY_GUILD.compile(guild.getId());
        return new RestAction<Void>(guild.getJDA(), route, frame) {
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
        return name != null
                || region != null
                || timeout != null
//                || icon != null
                || !Objects.equals(afkChannel, guild.getAfkChannel())
                || verificationLevel != null
                || defaultNotificationLevel != null
                || mfaLevel != null;
    }

    protected void checkAvailable()
    {
        if (!guild.isAvailable())
            throw new GuildUnavailableException();
    }

    protected void checkPermission(Permission perm)
    {
        if (!PermissionUtil.checkPermission(guild, guild.getMember(getGuild().getJDA().getSelfInfo()), perm))
            throw new PermissionException(perm);
    }
}
