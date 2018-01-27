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

package net.dv8tion.jda.client.events.group.update;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;

public class GroupUpdateIconEvent extends GenericGroupUpdateEvent
{
    protected final String oldIconId;

    public GroupUpdateIconEvent(JDA api, long responseNumber, Group group, String oldIconId)
    {
        super(api, responseNumber, group);
        this.oldIconId = oldIconId;
    }

    public String getOldIconId()
    {
        return oldIconId;
    }

    public String getOldIconUrl()
    {
        return oldIconId == null ? null :
                "https://cdn.discordapp.com/channel-icons/" + group.getId() + "/" + oldIconId + ".jpg";
    }
}
