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
package net.dv8tion.jda.core.events.channel.text.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.IPermissionHolder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <b><u>TextChannelUpdatePermissionsEvent</u></b><br>
 * Fired if a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}'s permission overrides change.<br>
 * <br>
 * Use: Detect when a TextChannel's permission overrides change and get affected {@link net.dv8tion.jda.core.entities.Role Roles}/{@link net.dv8tion.jda.core.entities.User Users}.
 */
public class TextChannelUpdatePermissionsEvent extends GenericTextChannelUpdateEvent
{
    private final List<IPermissionHolder> changed;

    public TextChannelUpdatePermissionsEvent(JDA api, long responseNumber, TextChannel channel, List<IPermissionHolder> permHolders)
    {
        super(api, responseNumber, channel);
        this.changed = permHolders;
    }

    public List<IPermissionHolder> getChangedPermissionHolders()
    {
        return changed;
    }

    public List<Role> getChangedRoles()
    {
        return changed.stream()
                      .filter(it -> it instanceof Role)
                      .map(Role.class::cast)
                      .collect(Collectors.toList());
    }

    public List<Member> getMembersWithPermissionChanges()
    {
        return changed.stream()
                .filter(it -> it instanceof Member)
                .map(Member.class::cast)
                .collect(Collectors.toList());
    }
}
