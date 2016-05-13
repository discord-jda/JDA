/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.managers;

import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.PermissionOverride;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.exceptions.PermissionException;
import net.dv8tion.jda.requests.Requester;
import org.json.JSONObject;

public class PermissionOverrideManager
{
    private final PermissionOverride override;
    private int allow, deny;

    /**
     * Creates a {@link net.dv8tion.jda.managers.PermissionOverrideManager} that can be used to manage
     * grants/denies of the provided {@link net.dv8tion.jda.entities.PermissionOverride}.
     *
     * @param override
     *          The {@link net.dv8tion.jda.entities.PermissionOverride} which the manager deals with.
     */
    public PermissionOverrideManager(PermissionOverride override)
    {
        this.override = override;
        this.allow = override.getAllowedRaw();
        this.deny = override.getDeniedRaw();
        checkPermission(Permission.MANAGE_PERMISSIONS);
    }

    //TODO: find a good system for this
    /**
     * Sets this Overrides allow/deny flags according to given PermissionOverride (copy behaviour)
     *
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param overwrite
     *      The PermissionOverride that should be copied
     * @return
     *      this
     */
    public PermissionOverrideManager overwrite(PermissionOverride overwrite)
    {
        throw new UnsupportedOperationException("Method temporarily disabled");
//        this.allow = overwrite.getAllowedRaw();
//        this.deny = overwrite.getDeniedRaw();
//        return this;
    }

    /**
     * Sets this Override to grant given Permissions
     *
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param perms
     *      The Permissions that should be granted
     * @return
     *      this
     */
    public PermissionOverrideManager grant(Permission... perms)
    {
        for (Permission perm : perms)
        {
            checkPermission(perm);
        }
        for (Permission perm : perms)
        {
            allow = allow | (1<<perm.getOffset());
        }
        deny = deny & (~allow);
        return this;
    }

    /**
     * Sets this Override to deny given Permissions
     *
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param perms
     *      The Permissions that should be denied
     * @return
     *      this
     */
    public PermissionOverrideManager deny(Permission... perms)
    {
        for (Permission perm : perms)
        {
            checkPermission(perm);
        }
        for (Permission perm : perms)
        {
            deny = deny | (1<<perm.getOffset());
        }
        allow = allow & (~deny);
        return this;
    }

    /**
     * Resets the allow/deny status for one or more {@link net.dv8tion.jda.Permission Permissions}.
     * This means, that this neither explicitly allows nor denies given Permissions.
     *
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param perms
     *      The Permissions that should be reset
     * @return
     *      this
     */
    public PermissionOverrideManager reset(Permission... perms)
    {
        for (Permission perm : perms)
        {
            checkPermission(perm);
        }
        for (Permission perm : perms)
        {
            allow = allow & (~(1 << perm.getOffset()));
            deny = deny & (~(1 << perm.getOffset()));
        }
        return this;
    }

    /**
     * Deletes this PermissionOverride
     * This method takes immediate effect
     */
    public void delete()
    {
        String targetId = override.isRoleOverride() ? override.getRole().getId() : override.getUser().getId();
        ((JDAImpl) override.getJDA()).getRequester()
                .delete(Requester.DISCORD_API_PREFIX + "channels/" + override.getChannel().getId() + "/permissions/" + targetId);
    }

    /**
     * Resets all queued updates. So the next call to {@link #update()} will change nothing.
     */
    public void reset() {
        allow = override.getAllowedRaw();
        deny = override.getDeniedRaw();
    }

    /**
     * This method will apply all accumulated changes received by setters
     */
    public void update()
    {
        if (this.allow == override.getAllowedRaw() && this.deny == override.getDeniedRaw())
        {
            return;
        }
        String targetId = override.isRoleOverride() ? override.getRole().getId() : override.getUser().getId();
        ((JDAImpl) override.getJDA()).getRequester()
                .put(Requester.DISCORD_API_PREFIX + "channels/" + override.getChannel().getId() + "/permissions/" + targetId,
                        new JSONObject()
                                .put("allow", allow)
                                .put("deny", deny)
                                .put("id", targetId)
                                .put("type", override.isRoleOverride() ? "role" : "member"));
    }

    private void checkPermission(Permission permission)
    {
        if(!override.getChannel().checkPermission(override.getJDA().getSelfInfo(), permission))
            throw new PermissionException(permission);
    }
}
