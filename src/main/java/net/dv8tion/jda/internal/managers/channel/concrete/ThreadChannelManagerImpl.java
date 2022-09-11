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

package net.dv8tion.jda.internal.managers.channel.concrete;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager;
import net.dv8tion.jda.internal.managers.channel.ChannelManagerImpl;
import net.dv8tion.jda.internal.utils.Checks;

public class ThreadChannelManagerImpl extends ChannelManagerImpl<ThreadChannel, ThreadChannelManager> implements ThreadChannelManager
{
    public ThreadChannelManagerImpl(ThreadChannel channel)
    {
        super(channel);
    }

    @Override
    protected boolean checkPermissions()
    {
        final Member selfMember = getGuild().getSelfMember();

        Checks.checkAccess(selfMember, channel);
        if (!channel.isOwner() && !selfMember.hasPermission(channel, Permission.MANAGE_THREADS))
            throw new InsufficientPermissionException(channel, Permission.MANAGE_THREADS);

        return super.checkPermissions();
    }
}
