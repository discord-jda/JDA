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

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.entities.ThreadMember;
import net.dv8tion.jda.api.events.thread.member.ThreadMemberJoinEvent;
import net.dv8tion.jda.api.events.thread.member.ThreadMemberLeaveEvent;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.ThreadChannelImpl;
import net.dv8tion.jda.internal.utils.UnlockHook;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ThreadMembersUpdateHandler extends SocketHandler
{
    public ThreadMembersUpdateHandler(JDAImpl api)
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
        if (thread == null)
        {
            getJDA().getEventCache().cache(EventCache.Type.CHANNEL, threadId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("THREAD_MEMBERS_UPDATE attempted to update a thread that does not exist. JSON: {}", content);
            return null;
        }

        if (!content.isNull("added_members"))
        {
            DataArray addedMembersJson = content.getArray("added_members");
            handleAddedThreadMembers(thread, addedMembersJson);
        }

        if (!content.isNull("removed_member_ids"))
        {
            List<Long> removedMemberIds = content.getArray("removed_member_ids")
                .stream(DataArray::getString)
                .map(MiscUtil::parseSnowflake)
                .collect(Collectors.toList());
            handleRemovedThreadMembers(thread, removedMemberIds);
        }

        return null;
    }

    private void handleAddedThreadMembers(ThreadChannelImpl thread, DataArray addedMembersJson)
    {
        EntityBuilder entityBuilder = api.getEntityBuilder();
        CacheView.SimpleCacheView<ThreadMember> view = thread.getThreadMemberView();

        List<ThreadMember> addedThreadMembers = new ArrayList<>();
        for (int i = 0; i < addedMembersJson.length(); i++)
        {
            DataObject threadMemberJson = addedMembersJson.getObject(i);
            ThreadMember threadMember = entityBuilder.createThreadMember((GuildImpl) thread.getGuild(), thread, threadMemberJson);
            addedThreadMembers.add(threadMember);
        }

        //TODO-Threads: We assume here that we are allowed to cache these, however, we probably need to check the ChunkFilter first as the
        // underlying Member object might have been created when creating the ThreadMember and it might not be being updated. We don't
        // want to cache ThreadMembers if the Members they're based on aren't being cached.
        try (UnlockHook lock = view.writeLock())
        {
            for (ThreadMember threadMember : addedThreadMembers)
            {
                view.getMap().put(threadMember.getIdLong(), threadMember);
            }
        }

        //Emit the events from outside the writeLock
        for (ThreadMember threadMember : addedThreadMembers)
        {
            api.handleEvent(
                new ThreadMemberJoinEvent(
                    api, responseNumber,
                    thread, threadMember));
        }
    }

    private void handleRemovedThreadMembers(ThreadChannelImpl thread, List<Long> removedMemberIds)
    {
        CacheView.SimpleCacheView<ThreadMember> view = thread.getThreadMemberView();

        //Store the removed threads into a map so that we can provide them in the events later.
        //We don't want to dispatch the events from inside the writeLock
        TLongObjectMap<ThreadMember> removedThreadMembers = new TLongObjectHashMap<>();
        try (UnlockHook lock = view.writeLock())
        {
            for (long threadMemberId : removedMemberIds)
            {
                ThreadMember threadMember = view.getMap().remove(threadMemberId);
                removedThreadMembers.put(threadMemberId, threadMember);
            }
        }

        for (long threadMemberId : removedMemberIds)
        {
            api.handleEvent(
                new ThreadMemberLeaveEvent(
                    api, responseNumber,
                    thread, threadMemberId, removedThreadMembers.remove(threadMemberId)));
        }
    }
}
