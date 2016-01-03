/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
