/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.utils.cache;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// does not inherit SnowflakeCacheView due to Member not being an ISnowflake
public interface MemberCacheView extends Iterable<Member>
{
    List<Member> asList();

    Set<Member> asSet();

    int size();

    boolean isEmpty();

    default Member getElementById(String id)
    {
        return getElementById(MiscUtil.parseSnowflake(id));
    }

    Member getElementById(long id);

    default List<Member> getElementsByUsername(String name)
    {
        return getElementsByUsername(name, false);
    }

    List<Member> getElementsByUsername(String name, boolean ignoreCase);

    default List<Member> getElementsByNickname(String name)
    {
        return getElementsByNickname(name, false);
    }

    List<Member> getElementsByNickname(String name, boolean ignoreCase);

    default List<Member> getElementsByEffectiveName(String name)
    {
        return getElementsByEffectiveName(name, false);
    }

    List<Member> getElementsByEffectiveName(String name, boolean ignoreCase);

    List<Member> getElementsWithRoles(Role... roles);

    List<Member> getElementsWithRoles(Collection<Role> roles);

    default Stream<Member> stream()
    {
        return StreamSupport.stream(spliterator(), false);
    }

    default Stream<Member> parallelStream()
    {
        return StreamSupport.stream(spliterator(), true);
    }

    @Override
    default Iterator<Member> iterator()
    {
        return new MemberCacheIterator(this);
    }

    class MemberCacheIterator implements Iterator<Member>
    {
        protected final List<Member> list;
        protected int index = 0;

        MemberCacheIterator(MemberCacheView view)
        {
            this.list = view.asList();
        }

        @Override
        public boolean hasNext()
        {
            return index < list.size();
        }

        @Override
        public Member next()
        {
            if (!hasNext())
                throw new NoSuchElementException("Iteration has reached the end.");
            return list.get(index++);
        }
    }
}
