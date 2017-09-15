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

import java.util.Collection;
import java.util.List;

public interface ProjectedMemberCacheView extends CacheView<Member>
{
    List<Member> getElementsById(long id);

    default List<Member> getElementsById(String id)
    {
        return getElementsById(MiscUtil.parseSnowflake(id));
    }

    List<Member> getElementsByUsername(String name, boolean ignoreCase);

    default List<Member> getElementsByUsername(String name)
    {
        return getElementsByUsername(name, false);
    }

    List<Member> getElementsByNickname(String name, boolean ignoreCase);

    default List<Member> getElementsByNickname(String name)
    {
        return getElementsByNickname(name, false);
    }

    List<Member> getElementsWithRoles(Role... roles);

    List<Member> getElementsWithRoles(Collection<Role> roles);
}
