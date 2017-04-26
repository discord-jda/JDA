/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.core.events.channel.voice.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.List;

/**
 * <b><u>VoiceChannelUpdatePermissionsEvent</u></b><br>
 * Fired if a {@link VoiceChannel VoiceChannel}'s permission overrides change.<br>
 * <br>
 * Use: Get affected VoiceChannel, affected Guild and affected {@link net.dv8tion.jda.core.entities.Role Roles}/{@link net.dv8tion.jda.core.entities.User Users}.
 */
public class VoiceChannelUpdatePermissionsEvent extends GenericVoiceChannelUpdateEvent
{
    private final List<Role> changedRoles;
    private final List<Member> changedMemberRoles;
    public VoiceChannelUpdatePermissionsEvent(JDA api, long responseNumber, VoiceChannel channel, List<Role> changedRoles, List<Member> changedMemberRoles)
    {
        super(api, responseNumber, channel);
        this.changedRoles = changedRoles;
        this.changedMemberRoles = changedMemberRoles;
    }

    public List<Role> getChangedRoles()
    {
        return changedRoles;
    }

    public List<Member> getMemberWithPermissionChanges()
    {
        return changedMemberRoles;
    }
}
