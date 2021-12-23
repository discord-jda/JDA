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

package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.events.channel.update.*;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.ThreadChannelImpl;
import net.dv8tion.jda.internal.utils.Helpers;

import java.util.Objects;

public class ThreadUpdateHandler extends SocketHandler
{
    public ThreadUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        long guildId = content.getLong("guild_id");
        if (api.getGuildSetupController().isLocked(guildId))
            return guildId;

        final long threadId = content.getLong("id");
        ThreadChannelImpl thread = (ThreadChannelImpl) getJDA().getThreadChannelById(threadId);


        //If the thread is missing then that means that the bot started up while the thread was archived
        // thus it didn't get the thread. Now that it's been unarchived we've been given the entire thread and need to build it.
        //Refer to the documentation for more info: https://discord.com/developers/docs/topics/threads#unarchiving-a-thread
        if (thread == null)
        {
            //Technically, when the ThreadChannel is unarchived the archive_timestamp (getTimeArchiveInfoLastModified) changes
            // as well, but we don't have the original value because we didn't have the thread in memory, so we can't
            // provide an entirely accurate ChannelUpdateArchiveTimestampEvent. Not sure how much that'll matter.
            thread = (ThreadChannelImpl) api.getEntityBuilder().createThreadChannel(content, guildId);
            api.handleEvent(
                new ChannelUpdateArchivedEvent(
                    api, responseNumber,
                    thread, true, false));

            return null;
        }

        final DataObject threadMetadata = content.getObject("thread_metadata");
        final String name = content.getString("name");
        final ThreadChannel.AutoArchiveDuration autoArchiveDuration = ThreadChannel.AutoArchiveDuration.fromKey(threadMetadata.getInt("auto_archive_duration"));
        final boolean locked = threadMetadata.getBoolean("locked");
        final boolean archived = threadMetadata.getBoolean("archived");
        final boolean invitable = threadMetadata.getBoolean("invitable");
        final long archiveTimestamp = Helpers.toTimestamp(threadMetadata.getString("archive_timestamp"));
        final int slowmode = content.getInt("rate_limit_per_user", 0);

        final String oldName = thread.getName();
        final ThreadChannel.AutoArchiveDuration oldAutoArchiveDuration = thread.getAutoArchiveDuration();
        final boolean oldLocked = thread.isLocked();
        final boolean oldArchived = thread.isArchived();
        final boolean oldInvitable = !thread.isPublic() && thread.isInvitable();
        final long oldArchiveTimestamp = thread.getArchiveTimestamp();
        final int oldSlowmode = thread.getSlowmode();

        //TODO should these be Thread specific events?
        if (!Objects.equals(oldName, name))
        {
            thread.setName(name);
            api.handleEvent(
                new ChannelUpdateNameEvent(
                    getJDA(), responseNumber,
                    thread, oldName, name));
        }
        if (oldSlowmode != slowmode)
        {
            thread.setSlowmode(slowmode);
            api.handleEvent(
                new ChannelUpdateSlowmodeEvent(
                    api, responseNumber,
                    thread, oldSlowmode, slowmode));
        }
        if (oldAutoArchiveDuration != autoArchiveDuration)
        {
            thread.setAutoArchiveDuration(autoArchiveDuration);
            api.handleEvent(
                new ChannelUpdateAutoArchiveDurationEvent(
                    api, responseNumber,
                    thread, oldAutoArchiveDuration, autoArchiveDuration));
        }
        if (oldLocked != locked)
        {
            thread.setLocked(locked);
            api.handleEvent(
                new ChannelUpdateLockedEvent(
                    api, responseNumber,
                    thread, oldLocked, locked));
        }
        if (oldArchived != archived)
        {
            thread.setArchived(archived);
            api.handleEvent(
                new ChannelUpdateArchivedEvent(
                    api, responseNumber,
                    thread, oldArchived, archived));
        }
        if (oldArchiveTimestamp != archiveTimestamp)
        {
            thread.setArchiveTimestamp(archiveTimestamp);
            api.handleEvent(
                new ChannelUpdateArchiveTimestampEvent(
                    api, responseNumber,
                    thread, oldArchiveTimestamp, archiveTimestamp));
        }
        if (oldInvitable != invitable)
        {
            thread.setInvitable(invitable);
            api.handleEvent(
                new ChannelUpdateInvitableEvent(
                    api, responseNumber,
                    thread, oldInvitable, invitable));
        }

        return null;
    }
}
