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
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.entities.StageInstance;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.restaction.StageInstanceAction;
import net.dv8tion.jda.internal.requests.restaction.StageInstanceActionImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

public class StageChannelImpl extends VoiceChannelImpl implements StageChannel
{
    private StageInstance instance;

    public StageChannelImpl(long id, GuildImpl guild)
    {
        super(id, guild);
    }

    @Nonnull
    @Override
    public ChannelType getType()
    {
        return ChannelType.STAGE;
    }

    @Nullable
    @Override
    public StageInstance getStageInstance()
    {
        return instance;
    }

    @Nonnull
    @Override
    public StageInstanceAction createStageInstance(@Nonnull String topic)
    {
        EnumSet<Permission> permissions = getGuild().getSelfMember().getPermissions(this);
        EnumSet<Permission> required = EnumSet.of(Permission.MANAGE_CHANNEL, Permission.VOICE_MUTE_OTHERS, Permission.VOICE_MOVE_OTHERS);
        for (Permission perm : required)
        {
            if (!permissions.contains(perm))
                throw new InsufficientPermissionException(this, perm, "You must be a stage moderator to create a stage instance! Missing Permission: " + perm);
        }

        return new StageInstanceActionImpl(this).setTopic(topic);
    }

    public StageChannelImpl setStageInstance(StageInstance instance)
    {
        this.instance = instance;
        return this;
    }
}
