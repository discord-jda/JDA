package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.Permission;

/**
 * Created by Michael Ritter on 13.12.2015.
 */
public class RoleImpl implements net.dv8tion.jda.entities.Role
{
    private String id;
    private String name;
    private int color;
    private int position;
    private int permissions;
    private boolean managed, hoist;

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getColor()
    {
        return color;
    }

    @Override
    public int getPosition()
    {
        return position;
    }

    @Override
    public boolean hasPermission(Permission perm)
    {
        return ((1 << perm.getOffset()) & permissions) > 0;
    }

    @Override
    public boolean isManaged()
    {
        return managed;
    }

    @Override
    public boolean isHoist()
    {
        return hoist;
    }

    public RoleImpl setId(String id)
    {
        this.id = id;
        return this;
    }

    public RoleImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public RoleImpl setColor(int color)
    {
        this.color = color;
        return this;
    }

    public RoleImpl setPosition(int position)
    {
        this.position = position;
        return this;
    }

    public RoleImpl setPermissions(int permissions)
    {
        this.permissions = permissions;
        return this;
    }

    public RoleImpl setManaged(boolean managed)
    {
        this.managed = managed;
        return this;
    }

    public RoleImpl setHoist(boolean hoist)
    {
        this.hoist = hoist;
        return this;
    }
}
