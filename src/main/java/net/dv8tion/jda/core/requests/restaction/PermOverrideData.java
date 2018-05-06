/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.entities.PermissionOverride;
import org.json.JSONObject;
import org.json.JSONString;

public class PermOverrideData implements JSONString
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
        this.deny = deny;
    }

    public PermOverrideData(PermissionOverride override)
    {
        if (override.isMemberOverride())
        {
            this.id = override.getMember().getUser().getIdLong();
            this.type = MEMBER_TYPE;
        }
        else
        {
            this.id = override.getRole().getIdLong();
            this.type = ROLE_TYPE;
        }
        this.allow = override.getAllowedRaw();
        this.deny = override.getDeniedRaw();
    }

    @Override
    public String toJSONString()
    {
        final JSONObject o = new JSONObject();
        o.put("type",  type);
        o.put("id",    id);
        o.put("allow", allow);
        o.put("deny",  deny);
        return o.toString();
    }
}
