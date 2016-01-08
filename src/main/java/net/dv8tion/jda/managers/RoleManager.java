/**
 *      Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import net.dv8tion.jda.entities.impl.RoleImpl;
import net.dv8tion.jda.exceptions.PermissionException;
import net.dv8tion.jda.handle.EntityBuilder;
import net.dv8tion.jda.utils.PermissionUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RoleManager
{
    private final Role role;

    private String name = null;
    private int color = -1;
    private Boolean grouped = null;
    private int perms;

    public RoleManager(Role role)
    {
        this.role = role;
        perms = ((RoleImpl) role).getPermissionsRaw();
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
     * Moves this Role up or down in the list of Roles (changing position attribute)
     * This change takes effect immediately!
     *
     * @param offset
     *      the amount of positions to move up (offset &lt; 0) or down (offset &gt; 0)
     * @return
     *      this
     */
    public RoleManager move(int offset)
    {
        checkPermission(Permission.MANAGE_ROLES);

        List<Role> newOrder = new ArrayList<>();
        role.getGuild().getRoles().stream().filter(r -> r != role && r != role.getGuild().getPublicRole())
                .sorted((c1, c2) -> Integer.compare(c1.getPosition(), c2.getPosition())).forEachOrdered(newOrder::add);
        int pos = Math.min(0, Math.max(newOrder.size(), role.getPosition() + offset));
        newOrder.add(pos, role);
        JSONArray arr = new JSONArray();
        for (int i = 0; i < newOrder.size(); i++)
        {
            arr.put(new JSONObject().put("position", i + 1).put("id", newOrder.get(i).getId()));
        }
        ((JDAImpl) role.getJDA()).getRequester().patchA("https://discordapp.com/api/guilds/" + role.getGuild().getId() + "/roles", arr);
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

        for (Permission perm : perms)
        {
            this.perms = this.perms & (~(1 << perm.getOffset()));
        }
        return this;
    }

    public void update()
    {
        checkPermission(Permission.MANAGE_ROLES);

        JSONObject frame = getFrame();
        if(name != null)
            frame.put("name", name);
        if(color >= 0)
            frame.put("color", color);
        if(grouped != null)
            frame.put("hoist", grouped.booleanValue());
        update(frame);
    }

    /**
     * Deletes this Role
     * This change takes effect immediately!
     */
    public void delete()
    {
        checkPermission(Permission.MANAGE_ROLES);

        ((JDAImpl) role.getJDA()).getRequester().delete("https://discordapp.com/api/guilds/" + role.getGuild().getId() + "/roles/" + role.getId());
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
        JSONObject response = ((JDAImpl) role.getJDA()).getRequester().patch("https://discordapp.com/api/guilds/" + role.getGuild().getId() + "/roles/" + role.getId(), object);
        if (response == null || !response.has("id"))
        {
            throw new RuntimeException("Setting values of Role " + role.getName() + " with ID " + role.getId()
                    + " failed... Reason: " + (response == null ? "Unknown" : response.toString()));
        }
        new EntityBuilder(((JDAImpl) role.getJDA())).createRole(response, role.getGuild().getId());
    }

    private void checkPermission(Permission perm)
    {
        if (!PermissionUtil.checkPermission(role.getJDA().getSelfInfo(), perm, role.getGuild()))
            throw new PermissionException(perm);
    }
}
