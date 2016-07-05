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
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.exceptions.PermissionException;
import net.dv8tion.jda.handle.EntityBuilder;
import net.dv8tion.jda.requests.Requester;
import net.dv8tion.jda.utils.PermissionUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RoleManager
{
    private final Role role;

    private String name = null;
    private int color = -1;
    private Boolean grouped = null;
    private Boolean mentionable;
    private int perms;

    public RoleManager(Role role)
    {
        this.role = role;
        perms = role.getPermissionsRaw();
    }

    /**
     * Returns the {@link net.dv8tion.jda.entities.Role Role} object of this Manager. Useful if this Manager was returned via a create function
     * @return
     *      the Role of this Manager
     */
    public Role getRole()
    {
        return role;
    }

    /**
     * Sets the name of this Role.
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param name
     *      The new name of the Role, or null to keep current one
     * @return
     *      this
     */
    public RoleManager setName(String name)
    {
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition();

        if (role.getName().equals(name))
        {
            this.name = null;
        }
        else
        {
            this.name = name;
        }
        return this;
    }

    /**
     * Sets the <code>int</code> representation of the permissions for this {@link net.dv8tion.jda.entities.Role Role}.<br>
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param perms
     *      int containing offset permissions of this role
     * @return
     *      this
     * @see
     *      <a href="https://discordapp.com/developers/docs/topics/permissions">Discord Permission Documentation</a>
     */
    public RoleManager setPermissionsRaw(int perms)
    {
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition();

        for (Permission perm : Permission.getPermissions(perms))
        {
            checkPermission(perm);   
        }

        this.perms = perms;
        return this;
    }

    /**
     * Sets the color of this Role.
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param color
     *      The new color of the Role, or -1 to keep current one
     * @return
     *      this
     */
    public RoleManager setColor(int color)
    {
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition();

        if (color == role.getColor() || color < 0)
        {
            this.color = -1;
        }
        else
        {
            this.color = Math.min(0xFFFFFF, color);
        }
        return this;
    }

    /**
     * Sets the color of this Role.
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param color
     *      The new color of the Role, or null to keep current one
     * @return
     *      this
     */
    public RoleManager setColor(Color color)
    {
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition();
        return setColor(color == null ? -1 : color.getRGB() & 0xFFFFFF);
    }

    /**
     * Sets, whether this Role should be grouped in the member-overview.
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param group
     *      Whether or not to group this Role, or null to keep current grouping status
     * @return
     *      this
     */
    public RoleManager setGrouped(Boolean group)
    {
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition();

        if (group == null || group == role.isGrouped())
        {
            this.grouped = null;
        }
        else
        {
            this.grouped = group;
        }
        return this;
    }

    /**
     * Sets, whether this Role should be mentionable.
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param group
     *      Whether or not this should be mentionable, or null to keep current grouping status
     * @return
     *      this
     */
    public RoleManager setMentionable(Boolean mention)
    {
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition();

        if (mention == null || mention == role.isMentionable())
        {
            this.mentionable = null;
        }
        else
        {
            this.mentionable = mention;
        }
        return this;
    }

    /**
     * Moves this Role up or down in the list of Roles (changing position attribute)
     * This change takes effect immediately!
     *
     * @param newPosition
     *      the amount of positions to move up (offset &lt; 0) or down (offset &gt; 0)
     * @return
     *      this
     */
    public RoleManager move(int newPosition)
    {
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition();
        int maxRolePosition = role.getGuild().getRolesForUser(role.getJDA().getSelfInfo()).get(0).getPosition();
        if (newPosition >= maxRolePosition)
            throw new PermissionException("Cannot move to a position equal to or higher than the highest role that you have access to.");

        if (newPosition < 0 || newPosition == role.getPosition())
            return this;

        Map<Integer, Role> newPositions = new HashMap<>();
        Map<Integer, Role> currentPositions = role.getGuild().getRoles().stream()
                .collect(Collectors.toMap(
                    role -> role.getPosition(),
                    role -> role));

        //Remove the @everyone role from our working set.
        currentPositions.remove(-1);
        int searchIndex = newPosition > role.getPosition() ? newPosition : newPosition;
        int index = 0;
        for (Role r : currentPositions.values())
        {
            if (r == role)
                continue;
            if (index == searchIndex)
            {
                newPositions.put(index, role);
                index++;
            }
            newPositions.put(index, r);
            index++;
        }
        //If the role was moved to the very top, this will make sure it is properly handled.
        if (!newPositions.containsValue(role))
            newPositions.put(newPosition, role);

        for (int i = 0; i < newPositions.size(); i++)
        {
            if (currentPositions.get(i) == newPositions.get(i))
                newPositions.remove(i);
        }

        JSONArray rolePositions = new JSONArray();
        newPositions.forEach((pos, r) ->
        {
            rolePositions.put(new JSONObject()
                    .put("id", r.getId())
                    .put("position", pos + 1));
        });
        ((JDAImpl) role.getJDA()).getRequester().patch(Requester.DISCORD_API_PREFIX + "guilds/" + role.getGuild().getId() + "/roles", rolePositions);
        return this;
    }

    /**
     * Gives this Role one or more {@link net.dv8tion.jda.Permission Permissions}.
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param perms
     *      the Permissions to give this Role
     * @return
     *      this
     */
    public RoleManager give(Permission... perms)
    {
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition();
        //we need to have all perms ourself
        for (Permission perm : perms)
        {
            checkPermission(perm);
        }

        for (Permission perm : perms)
        {
            this.perms = this.perms | (1 << perm.getOffset());
        }
        return this;
    }

    /**
     * Removes one or more {@link net.dv8tion.jda.Permission Permissions} from this Role.
     * This change will only be applied, if {@link #update()} is called.
     * So multiple changes can be made at once.
     *
     * @param perms
     *      the Permissions to remove from this Role
     * @return
     *      this
     */
    public RoleManager revoke(Permission... perms)
    {
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition();
        for (Permission perm : perms)
        {
            checkPermission(perm);
        }

        for (Permission perm : perms)
        {
            this.perms = this.perms & (~(1 << perm.getOffset()));
        }
        return this;
    }

    /**
     * Resets all queued updates. So the next call to {@link #update()} will change nothing.
     */
    public void reset() {
        name = null;
        color = -1;
        grouped = null;
        perms = role.getPermissionsRaw();
    }

    /**
     * This method will apply all accumulated changes received by setters
     */
    public void update()
    {
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition();

        JSONObject frame = getFrame();
        if(name != null)
            frame.put("name", name);
        if(color >= 0)
            frame.put("color", color);
        if(grouped != null)
            frame.put("hoist", grouped.booleanValue());
        if(mentionable != null)
            frame.put("mentionable", mentionable.booleanValue());
        update(frame);
    }

    /**
     * Deletes this Role
     * This change takes effect immediately!
     */
    public void delete()
    {
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition();

        ((JDAImpl) role.getJDA()).getRequester().delete(Requester.DISCORD_API_PREFIX + "guilds/" + role.getGuild().getId() + "/roles/" + role.getId());
    }

    private JSONObject getFrame()
    {
        return new JSONObject()
                .put("name", role.getName())
                .put("color", role.getColor())
                .put("hoist", role.isGrouped())
                .put("permissions", perms);
    }

    private void update(JSONObject object)
    {
        Requester.Response response = ((JDAImpl) role.getJDA()).getRequester().patch(Requester.DISCORD_API_PREFIX + "guilds/" + role.getGuild().getId() + "/roles/" + role.getId(), object);
        if (!response.isOk())
        {
            throw new RuntimeException("Setting values of Role " + role.getName() + " with ID " + role.getId()
                    + " failed... Reason: "+response.toString());
        }
        new EntityBuilder(((JDAImpl) role.getJDA())).createRole(response.getObject(), role.getGuild().getId());
    }

    private void checkPermission(Permission perm)
    {
        if (!PermissionUtil.checkPermission(role.getJDA().getSelfInfo(), perm, role.getGuild()))
            throw new PermissionException(perm);
    }

    private void checkPosition()
    {
        if(!PermissionUtil.canInteract(role.getJDA().getSelfInfo(), role))
            throw new PermissionException("Can't modify role >= highest self-role");
    }
}
