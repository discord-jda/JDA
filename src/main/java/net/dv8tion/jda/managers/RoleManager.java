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

import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.entities.impl.RoleImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RoleManager
{
    private final Role role;

    public RoleManager(Role role)
    {
        this.role = role;
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
     *
     * @param name
     *      The new name of the Role
     * @return
     *      this
     */
    public RoleManager setName(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Role name must not be null!");
        }
        if (name.equals(role.getName()))
        {
            return this;
        }
        update(getFrame().put("name", name));
        return this;
    }

    /**
     * Sets the color of this Role.
     *
     * @param color
     *      The new color of the Role
     * @return
     *      this
     */
    public RoleManager setColor(int color)
    {
        if (color == role.getColor())
        {
            return this;
        }
        update(getFrame().put("color", color));
        return this;
    }

    /**
     * Sets, whether this Role should be grouped in the member-overview.
     *
     * @param group
     *      Whether or not to group this Role
     * @return
     *      this
     */
    public RoleManager setGrouped(boolean group)
    {
        if (group == role.isGrouped())
        {
            return this;
        }
        update(getFrame().put("hoist", group));
        return this;
    }

    /**
     * Moves this Role up or down in the list of Roles (changing position attribute)
     *
     * @param offset
     *      the amount of positions to move up (offset &lt; 0) or down (offset &gt; 0)
     * @return
     *      this
     */
    public RoleManager move(int offset)
    {
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
     * Deletes this Role
     */
    public void delete()
    {
        ((JDAImpl) role.getJDA()).getRequester().delete("https://discordapp.com/api/guilds/" + role.getGuild().getId() + "/roles/" + role.getId());
    }

    private JSONObject getFrame()
    {
        return new JSONObject()
                .put("name", role.getName())
                .put("color", role.getColor())
                .put("hoist", role.isGrouped())
                .put("permissions", ((RoleImpl) role).getPermissions());
    }

    private void update(JSONObject object)
    {
        ((JDAImpl) role.getJDA()).getRequester().patch("https://discordapp.com/api/guilds/" + role.getGuild().getId() + "/roles/" + role.getId(), object);
    }
}
