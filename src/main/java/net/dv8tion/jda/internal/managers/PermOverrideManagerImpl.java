/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.PermOverrideManager;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.cache.SnowflakeReference;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class PermOverrideManagerImpl extends ManagerBase<PermOverrideManager> implements PermOverrideManager
{
    protected final SnowflakeReference<PermissionOverride> override;
    protected final boolean role;

    protected long allowed;
    protected long denied;

    /**
     * Creates a new PermOverrideManager instance
     *
     * @param override
     *        The {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride} to manage
     */
    public PermOverrideManagerImpl(PermissionOverride override)
    {
        super(override.getJDA(),
              Route.Channels.MODIFY_PERM_OVERRIDE.compile(
                  override.getChannel().getId(), override.getId()));
        this.override = setupReferent(override);
        this.role = override.isRoleOverride();
        this.allowed = override.getAllowedRaw();
        this.denied = override.getDeniedRaw();
        if (isPermissionChecksEnabled())
            checkPermissions();
    }

    private SnowflakeReference<PermissionOverride> setupReferent(PermissionOverride override)
    {
        JDA api = override.getJDA();
        GuildChannel channel = override.getChannel();
        long channelId = channel.getIdLong();
        ChannelType type = channel.getType();
        boolean role = override.isRoleOverride();
        return new SnowflakeReference<>(override, (holderId) -> {
            GuildChannel targetChannel = api.getGuildChannelById(type, channelId);
            if (targetChannel == null)
                return null;
            Guild guild = targetChannel.getGuild();
            IPermissionHolder holder;
            if (role)
                holder = guild.getRoleById(holderId);
            else
                holder = guild.getMemberById(holderId);
            if (holder == null)
                return null;
            return targetChannel.getPermissionOverride(holder);
        });
    }

    private void setupValues()
    {
        if (!shouldUpdate(ALLOWED))
            this.allowed = getPermissionOverride().getAllowedRaw();
        if (!shouldUpdate(DENIED))
            this.denied = getPermissionOverride().getDeniedRaw();
    }

    @Nonnull
    @Override
    public PermissionOverride getPermissionOverride()
    {
        return override.resolve();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public PermOverrideManagerImpl reset(long fields)
    {
        super.reset(fields);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public PermOverrideManagerImpl reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public PermOverrideManagerImpl reset()
    {
        super.reset();
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public PermOverrideManagerImpl grant(long permissions)
    {
        if (permissions == 0)
            return this;
        setupValues();
        this.allowed |= permissions;
        this.denied &= ~permissions;
        this.set |= PERMISSIONS;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public PermOverrideManagerImpl deny(long permissions)
    {
        if (permissions == 0)
            return this;
        setupValues();
        this.denied |= permissions;
        this.allowed &= ~permissions;
        this.set |= PERMISSIONS;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public PermOverrideManagerImpl clear(long permissions)
    {
        setupValues();
        if ((allowed & permissions) != 0)
        {
            this.allowed &= ~permissions;
            this.set |= ALLOWED;
        }

        if ((denied & permissions) != 0)
        {
            this.denied &= ~permissions;
            this.set |= DENIED;
        }

        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        String targetId = override.getId();
        // setup missing values here
        setupValues();
        RequestBody data = getRequestBody(
            DataObject.empty()
                .put("id", targetId)
                .put("type", role ? "role" : "member")
                .put("allow", this.allowed)
                .put("deny",  this.denied));
        reset();
        return data;
    }

    @Override
    protected boolean checkPermissions()
    {
        if (!getGuild().getSelfMember().hasPermission(getChannel(), Permission.MANAGE_PERMISSIONS))
            throw new InsufficientPermissionException(getChannel(), Permission.MANAGE_PERMISSIONS);
        return super.checkPermissions();
    }
}
