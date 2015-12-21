/**
 * Created by Michael Ritter on 15.12.2015.
 */
package net.dv8tion.jda.entities.impl;

public class PermissionOverride
{
    private final int allow, deny;

    public PermissionOverride(int allow, int deny)
    {
        this.allow = allow;
        this.deny = deny;
    }

    public int apply(int current)
    {
        current = current | allow;
        current = current & (~deny);
        return current;
    }

    public PermissionOverride after(PermissionOverride before)
    {
        int allow = this.allow | before.allow;
        int deny = (this.deny | before.deny) & (~allow);
        return new PermissionOverride(allow, deny);
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof PermissionOverride))
            return false;

        PermissionOverride oPermOver = (PermissionOverride) o;
        return this.allow == oPermOver.allow && this.deny == oPermOver.deny;
    }
}
