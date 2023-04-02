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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildWelcomeScreen;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.GuildWelcomeScreenManager;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GuildWelcomeScreenManagerImpl extends ManagerBase<GuildWelcomeScreenManager> implements GuildWelcomeScreenManager
{
    private final Guild guild;

    protected boolean enabled;
    protected String description;
    protected final List<GuildWelcomeScreen.Channel> channels = new ArrayList<>(GuildWelcomeScreen.MAX_WELCOME_CHANNELS);

    public GuildWelcomeScreenManagerImpl(Guild guild)
    {
        super(guild.getJDA(), Route.Guilds.MODIFY_WELCOME_SCREEN.compile(guild.getId()));
        this.guild = guild;
        if (isPermissionChecksEnabled())
            checkPermissions();
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildWelcomeScreenManagerImpl reset(long fields)
    {
        super.reset(fields);
        if ((fields & ENABLED) == ENABLED)
            this.enabled = false; //Most important is the flag being removed anyway
        if ((fields & DESCRIPTION) == DESCRIPTION)
            this.description = null;
        if ((fields & CHANNELS) == CHANNELS)
            this.channels.clear();
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildWelcomeScreenManagerImpl reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildWelcomeScreenManagerImpl reset()
    {
        super.reset(ENABLED | DESCRIPTION | CHANNELS);
        return this;
    }

    @Nonnull
    @Override
    public GuildWelcomeScreenManager setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        set |= ENABLED;
        return this;
    }

    @Nonnull
    @Override
    public GuildWelcomeScreenManager setDescription(@Nullable String description)
    {
        if (description != null)
            Checks.notLonger(description, GuildWelcomeScreen.MAX_DESCRIPTION_LENGTH, "Description");
        this.description = description;
        set |= DESCRIPTION;
        return this;
    }

    @Nonnull
    @Override
    public List<GuildWelcomeScreen.Channel> getWelcomeChannels()
    {
        return Collections.unmodifiableList(channels);
    }

    @Nonnull
    @Override
    public GuildWelcomeScreenManager clearWelcomeChannels()
    {
        withLock(channels, List::clear);
        set |= CHANNELS;
        return this;
    }

    @Nonnull
    @Override
    public GuildWelcomeScreenManager setWelcomeChannels(@Nonnull Collection<? extends GuildWelcomeScreen.Channel> channels)
    {
        Checks.noneNull(channels, "Welcome channels");
        Checks.check(channels.size() <= GuildWelcomeScreen.MAX_WELCOME_CHANNELS, "Cannot have more than %d welcome channels", GuildWelcomeScreen.MAX_WELCOME_CHANNELS);
        withLock(this.channels, c ->
        {
            c.clear();
            c.addAll(channels);
        });
        set |= CHANNELS;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject object = DataObject.empty();
        if (shouldUpdate(ENABLED))
            object.put("enabled", enabled);
        if (shouldUpdate(DESCRIPTION))
            object.put("description", description);
        withLock(this.channels, (list) ->
        {
            if (shouldUpdate(CHANNELS))
                object.put("welcome_channels", DataArray.fromCollection(list));
        });
        reset();
        return getRequestBody(object);
    }

    @Override
    protected boolean checkPermissions()
    {
        if (!getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new InsufficientPermissionException(getGuild(), Permission.MANAGE_SERVER);
        return super.checkPermissions();
    }
}
