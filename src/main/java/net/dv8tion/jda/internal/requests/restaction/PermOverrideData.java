/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.internal.requests.restaction;

import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nonnull;

public class PermOverrideData implements SerializableData
{
    public static final int ROLE_TYPE = 0;
    public static final int MEMBER_TYPE = 1;
    public final int type;
    public final long id;
    public final long allow;
    public final long deny;

    public PermOverrideData(int type, long id, long allow, long deny)
    {
        this.type = type;
        this.id = id;
        this.allow = allow;
        this.deny = deny & ~allow;
    }

    public PermOverrideData(PermissionOverride override)
    {
        this.id = override.idLong;
        this.type = override.isMemberOverride ? MEMBER_TYPE : ROLE_TYPE;
        this.allow = override.allowedRaw;
        this.deny = override.deniedRaw;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        final DataObject o = DataObject.empty();
        o.put("type",  type);
        o.put("id",    id);
        o.put("allow", allow);
        o.put("deny",  deny);
        return o;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof PermOverrideData))
            return false;

        PermOverrideData other = (PermOverrideData) obj;
        return other.id == this.id;
    }
}
