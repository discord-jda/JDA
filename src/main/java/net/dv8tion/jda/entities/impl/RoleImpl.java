/**
 * Copyright 2015-2016 Austin Keener & Michael Ritter
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.managers.RoleManager;

import java.util.List;

public class RoleImpl implements net.dv8tion.jda.entities.Role {
    private final String id;
    private final Guild guild;
    private String name;
    private int color;
    private int position;
    private int permissions;
    private boolean managed, grouped;

    public RoleImpl(String id, Guild guild) {
        this.id = id;
        this.guild = guild;
    }

    @Override
    public Guild getGuild() {
        return guild;
    }

    @Override
    public JDA getJDA() {
        return guild.getJDA();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getPermissionsRaw() {
        return permissions;
    }

    @Override
    public List<Permission> getPermissions() {
        return Permission.getPermissions(permissions);
    }

    public RoleImpl setPermissions(int permissions) {
        this.permissions = permissions;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    public RoleImpl setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public int getColor() {
        return color;
    }

    public RoleImpl setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    public int getPosition() {
        return position;
    }

    public RoleImpl setPosition(int position) {
        this.position = position;
        return this;
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return ((1 << perm.getOffset()) & permissions) > 0 || ((1 << Permission.MANAGE_ROLES.getOffset()) & permissions) > 0;
    }

    @Override
    public boolean isManaged() {
        return managed;
    }

    public RoleImpl setManaged(boolean managed) {
        this.managed = managed;
        return this;
    }

    @Override
    public boolean isGrouped() {
        return grouped;
    }

    public RoleImpl setGrouped(boolean grouped) {
        this.grouped = grouped;
        return this;
    }

    @Override
    public RoleManager getManager() {
        return new RoleManager(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Role))
            return false;
        Role oRole = (Role) o;
        return this == oRole || this.getId().equals(oRole.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "R:" + getName() + '(' + getId() + ')';
    }
}
