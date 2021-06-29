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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.entities.StageInstance;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.StageInstanceManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.managers.StageInstanceManagerImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.util.EnumSet;

public class StageInstanceImpl implements StageInstance
{
    private final long id;
    private StageChannel channel;
    private StageInstanceManager manager;

    private String topic;
    private PrivacyLevel privacyLevel;
    private boolean discoverable;

    public StageInstanceImpl(long id, StageChannel channel)
    {
        this.id = id;
        this.channel = channel;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return getChannel().getGuild();
    }

    @Nonnull
    @Override
    public StageChannel getChannel()
    {
        StageChannel real = channel.getJDA().getStageChannelById(channel.getIdLong());
        if (real != null)
            channel = real;
        return channel;
    }

    @Nonnull
    @Override
    public String getTopic()
    {
        return topic;
    }

    @Nonnull
    @Override
    public PrivacyLevel getPrivacyLevel()
    {
        return privacyLevel;
    }

    @Override
    public boolean isDiscoverable()
    {
        return discoverable;
    }

    @Nonnull
    @Override
    public RestAction<Void> delete()
    {
        checkPermissions();
        Route.CompiledRoute route = Route.StageInstances.DELETE_INSTANCE.compile(channel.getId());
        return new RestActionImpl<>(channel.getJDA(), route);
    }

    @Nonnull
    @Override
    public RestAction<Void> requestToSpeak()
    {
        Guild guild = getGuild();
        Route.CompiledRoute route = Route.Guilds.UPDATE_VOICE_STATE.compile(guild.getId(), "@me");
        DataObject body = DataObject.empty().put("channel_id", channel.getId());
        // Stage moderators can bypass the request queue by just unsuppressing
        if (guild.getSelfMember().hasPermission(getChannel(), Permission.VOICE_MUTE_OTHERS))
            body.putNull("request_to_speak_timestamp").put("suppress", false);
        else
            body.put("request_to_speak_timestamp", OffsetDateTime.now().toString());

        if (!channel.equals(guild.getSelfMember().getVoiceState().getChannel()))
            throw new IllegalStateException("Cannot request to speak without being connected to the stage channel!");
        return new RestActionImpl<>(channel.getJDA(), route, body);
    }

    @Nonnull
    @Override
    public RestAction<Void> cancelRequestToSpeak()
    {
        Guild guild = getGuild();
        Route.CompiledRoute route = Route.Guilds.UPDATE_VOICE_STATE.compile(guild.getId(), "@me");
        DataObject body = DataObject.empty()
                .putNull("request_to_speak_timestamp")
                .put("suppress", true)
                .put("channel_id", channel.getId());

        if (!channel.equals(guild.getSelfMember().getVoiceState().getChannel()))
            throw new IllegalStateException("Cannot cancel request to speak without being connected to the stage channel!");
        return new RestActionImpl<>(channel.getJDA(), route, body);
    }

    @Nonnull
    @Override
    public StageInstanceManager getManager()
    {
        checkPermissions();
        if (manager == null)
            manager = new StageInstanceManagerImpl(this);
        return manager;
    }

    public StageInstanceImpl setTopic(String topic)
    {
        this.topic = topic;
        return this;
    }

    public StageInstanceImpl setPrivacyLevel(PrivacyLevel privacyLevel)
    {
        this.privacyLevel = privacyLevel;
        return this;
    }

    public StageInstanceImpl setDiscoverable(boolean discoverable)
    {
        this.discoverable = discoverable;
        return this;
    }

    private void checkPermissions()
    {
        EnumSet<Permission> permissions = getGuild().getSelfMember().getPermissions(getChannel());
        EnumSet<Permission> required = EnumSet.of(Permission.MANAGE_CHANNEL, Permission.VOICE_MUTE_OTHERS, Permission.VOICE_MOVE_OTHERS);
        for (Permission perm : required)
        {
            if (!permissions.contains(perm))
                throw new InsufficientPermissionException(getChannel(), perm, "You must be a stage moderator to manage a stage instance! Missing Permission: " + perm);
        }
    }
}
