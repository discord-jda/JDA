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

package net.dv8tion.jda.core.utils.cache.impl;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.cache.MemberCacheView;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MemberCacheViewImpl extends AbstractCacheView<Member> implements MemberCacheView
{
    public MemberCacheViewImpl()
    {
        super(Member::getEffectiveName);
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
        List<Member> members = new LinkedList<>();
        for (Member member : elements.valueCollection())
        {
            final String nick = member.getUser().getName();
            if (ignoreCase)
            {
                if (nick.equalsIgnoreCase(name))
                    members.add(member);
            }
            else
            {
                if (nick.equals(name))
                    members.add(member);
            }
        }
        return Collections.unmodifiableList(members);
    }

    @Override
    public List<Member> getElementsByNickname(String name, boolean ignoreCase)
    {
        List<Member> members = new LinkedList<>();
        for (Member member : elements.valueCollection())
        {
            final String nick = member.getNickname();
            if (nick == null)
            {
                if (name == null)
                    members.add(member);
                continue;
            }

            if (ignoreCase)
            {
                if (nick.equalsIgnoreCase(name))
                    members.add(member);
            }
            else
            {
                if (nick.equals(name))
                    members.add(member);
            }
        }
        return Collections.unmodifiableList(members);
    }

    @Override
    public List<Member> getElementsByName(String name, boolean ignoreCase)
    {
        Checks.notEmpty(name, "Name");
        List<Member> members = new LinkedList<>();
        for (Member member : elements.valueCollection())
        {
            final String nick = member.getEffectiveName();
            if (ignoreCase)
            {
                if (nick.equalsIgnoreCase(name))
                    members.add(member);
            }
            else
            {
                if (nick.equals(name))
                    members.add(member);
            }
        }
        return Collections.unmodifiableList(members);
    }

    @Override
    public List<Member> getElementsWithRoles(Role... roles)
    {
        Checks.notNull(roles, "Roles");
        for (Role role : roles)
            Checks.notNull(role, "Roles");
        List<Member> members = new LinkedList<>();
        memberLoop: for (Member member : elements.valueCollection())
        {
            for (Role role : roles)
            {
                if (!member.getRoles().contains(role))
                    continue memberLoop;
            }
            members.add(member);
        }
        return members;
    }

    @Override
    public List<Member> getElementsWithRoles(Collection<Role> roles)
    {
        Checks.noneNull(roles, "Roles");
        List<Member> members = new LinkedList<>();
        for (Member member : elements.valueCollection())
        {
            if (member.getRoles().containsAll(roles))
                members.add(member);
        }
        return members;
    }
}
