/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.utils.cache.impl;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.cache.MemberCacheView;

import java.util.*;

public class MemberCacheViewImpl extends AbstractCacheView<Member> implements MemberCacheView
{
    public MemberCacheViewImpl()
    {
        super(Member.class, Member::getEffectiveName);
    }

    @Override
    public Member getElementById(long id)
    {
        return elements.get(id);
    }

    @Override
    public List<Member> getElementsByUsername(String name, boolean ignoreCase)
    {
        Checks.notEmpty(name, "Name");
        List<Member> members = new ArrayList<>();
        for (Member member : this)
        {
            final String nick = member.getUser().getName();
            if (equals(ignoreCase, nick, name))
                members.add(member);
        }
        return Collections.unmodifiableList(members);
    }

    @Override
    public List<Member> getElementsByNickname(String name, boolean ignoreCase)
    {
        List<Member> members = new ArrayList<>();
        for (Member member : this)
        {
            final String nick = member.getNickname();
            if (nick == null)
            {
                if (name == null)
                    members.add(member);
                continue;
            }

            if (equals(ignoreCase, nick, name))
                members.add(member);
        }
        return Collections.unmodifiableList(members);
    }

    @Override
    public List<Member> getElementsByName(String name, boolean ignoreCase)
    {
        Checks.notEmpty(name, "Name");
        List<Member> members = new ArrayList<>();
        for (Member member : this)
        {
            final String nick = member.getEffectiveName();
            if (equals(ignoreCase, nick, name))
                members.add(member);
        }
        return Collections.unmodifiableList(members);
    }

    @Override
    public List<Member> getElementsWithRoles(Role... roles)
    {
        Checks.notNull(roles, "Roles");
        for (Role role : roles)
            Checks.notNull(role, "Roles");
        List<Member> members = new ArrayList<>();
        List<Role> match = Arrays.asList(roles);
        for (Member member : this)
        {
            if (member.getRoles().containsAll(match))
                members.add(member);
        }
        return members;
    }

    @Override
    public List<Member> getElementsWithRoles(Collection<Role> roles)
    {
        Checks.noneNull(roles, "Roles");
        List<Member> members = new ArrayList<>();
        for (Member member : this)
        {
            if (member.getRoles().containsAll(roles))
                members.add(member);
        }
        return members;
    }
}
