/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.util.LinkedList;

/**
 * Implements a Least-Recently-Used (LRU) cache (independent of guilds).
 * <br>The cache policy will keep track of how many members are currently cached and will removed the least recently
 * cached member once the maximum is reached.
 */
public class LRUCachePolicy implements MemberCachePolicy
{
    private final int max;
    private final LinkedList<Long> cached = new LinkedList<>();

    public LRUCachePolicy(int max)
    {
        this.max = max;
    }

    @Override
    public synchronized boolean cacheMember(Member member)
    {
        long id = member.getIdLong();
        int index = cached.indexOf(id);

        // Special case, the member is the most recently used already
        if (index == 0)
            return true;

        if (index > 0)
        {
            // Promote member to most recently used
            cached.remove(index);
        }
        else if (cached.size() >= max)
        {
            // Unload LRU user
            long remove = cached.removeLast();
            member.getJDA().unloadUser(remove);
        }

        cached.addFirst(id);
        return true;
    }
}
