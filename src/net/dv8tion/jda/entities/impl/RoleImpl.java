package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.Permission;

/**
 * Created by Michael Ritter on 13.12.2015.
 */
public class RoleImpl implements net.dv8tion.jda.entities.Role
{
    private int position;
    private String name;
    private int permissions;
    private boolean managed, hoist;
    private String id;
    private int color;

    @Override
    public int getPosition()
    {
        return position;
    }

    public void setPosition(int position)
    {
        this.position = position;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public boolean hasPermission(Permission perm)
    {
        return ((1 << perm.getOffset()) & permissions) > 0;
    }

    public void setPermissions(int permissions)
    {
        this.permissions = permissions;
    }

    @Override
    public boolean isManaged()
    {
        return managed;
    }

    public void setManaged(boolean managed)
    {
        this.managed = managed;
    }

    @Override
    public boolean isHoist()
    {
        return hoist;
    }

    public void setHoist(boolean hoist)
    {
        this.hoist = hoist;
    }

    @Override
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public int getColor()
    {
        return color;
    }

    public void setColor(int color)
    {
        this.color = color;
    }
}
