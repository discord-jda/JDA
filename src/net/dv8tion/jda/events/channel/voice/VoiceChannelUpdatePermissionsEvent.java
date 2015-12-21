/**
 *    Copyright 2015 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.events.channel.voice;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.impl.PermissionOverride;
import net.dv8tion.jda.entities.impl.VoiceChannelImpl;

public class VoiceChannelUpdatePermissionsEvent extends GenericVoiceChannelUpdateEvent
{
    protected Map<Role, PermissionOverride> roleOverrideCreated;
    protected Map<Role, PermissionOverride> roleOverrideChangedNew = null;
    protected Map<Role, PermissionOverride> roleOverrideChangedOld;
    protected Map<Role, PermissionOverride> roleOverrideDeleted;
    protected Map<User, PermissionOverride> userOverrideCreated;
    protected Map<User, PermissionOverride> userOverrideChangedNew = null;
    protected Map<User, PermissionOverride> userOverrideChangedOld;
    protected Map<User, PermissionOverride> userOverrideDeleted;

    public VoiceChannelUpdatePermissionsEvent(JDA api, int responseNumber, VoiceChannel channel,
            Map<Role, PermissionOverride> roleOverrideCreated,
            Map<Role, PermissionOverride> roleOverrideChangedOld,
            Map<Role, PermissionOverride> roleOverrideDeleted,
            Map<User, PermissionOverride> userOverrideCreated,
            Map<User, PermissionOverride> userOverrideChangedOld,
            Map<User, PermissionOverride> userOverrideDeleted)
    {
        super(api, responseNumber, channel);
        this.roleOverrideCreated = roleOverrideCreated;
        this.roleOverrideChangedOld = roleOverrideChangedOld;
        this.roleOverrideDeleted = roleOverrideDeleted;
        this.userOverrideCreated = userOverrideCreated;
        this.userOverrideChangedOld = userOverrideChangedOld;
        this.userOverrideDeleted = userOverrideDeleted;
    }

    public Map<Role, PermissionOverride> getRoleOverridesCreated()
    {
        return Collections.unmodifiableMap(roleOverrideCreated);
    }

    public Map<Role, PermissionOverride> getRoleOverridesChangedOld()
    {
        return Collections.unmodifiableMap(roleOverrideChangedOld);
    }

    public Map<Role, PermissionOverride> getRoleOverridesChangedNew()
    {
        if (this.roleOverrideChangedNew == null)
        {
            HashMap<Role, PermissionOverride> roleOverrideNew = new HashMap<>();
            for (Role role : roleOverrideChangedOld.keySet())
            {
                roleOverrideNew.put(role, ((VoiceChannelImpl) channel).getRolePermissionOverrides().get(role));
            }
            this.roleOverrideChangedNew = Collections.unmodifiableMap(roleOverrideNew);
        }
        return this.roleOverrideChangedNew;
    }

    public Map<Role, PermissionOverride> getRoleOverridesDeleted()
    {
        return Collections.unmodifiableMap(roleOverrideDeleted);
    }

    public Map<User, PermissionOverride> getUserOverridesCreated()
    {
        return Collections.unmodifiableMap(userOverrideCreated);
    }

    public Map<User, PermissionOverride> getUserOverridesChangedNew()
    {
        if (this.userOverrideChangedNew == null)
        {
            HashMap<User, PermissionOverride> userOverrideNew = new HashMap<>();
            for (User user : userOverrideChangedOld.keySet())
            {
                userOverrideNew.put(user, ((VoiceChannelImpl) channel).getUserPermissionOverrides().get(user));
            }
            this.userOverrideChangedNew = Collections.unmodifiableMap(userOverrideNew);
        }
        return this.userOverrideChangedNew;
    }

    public Map<User, PermissionOverride> getUserOverridesChangedOld()
    {
        return Collections.unmodifiableMap(userOverrideChangedOld);
    }

    public Map<User, PermissionOverride> getUserOverridesDeleted()
    {
        return Collections.unmodifiableMap(userOverrideDeleted);
    }
}
