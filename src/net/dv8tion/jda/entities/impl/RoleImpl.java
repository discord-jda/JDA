package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Role;

/**
 * Created by Michael Ritter on 13.12.2015.
 */
public class RoleImpl implements net.dv8tion.jda.entities.Role
{
    private final String id;
    private String name;
    private int color;
    private int position;
    private int permissions;
    private boolean managed, hoist;

    public RoleImpl(String id)
    {
        this.id = id;
    }

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

    /**
     * Returns true if one of the following is true:
     *    A) The provided object is the same Role instance as this object
     *    B) The provided object is a Role object with the same id as this object.
     *    C) The provided object is a String that is equal to our id.
     */
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Role)
        {
            Role oRole = (Role) o;
            return this == oRole || this.getId().equals(oRole.getId());
        }
        else if (o instanceof String)
        {
            String oString = (String) o;
            return this.getId().equals(oString);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }

}
