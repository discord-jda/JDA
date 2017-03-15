/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.core.events.self;

import net.dv8tion.jda.core.JDA;

public class SelfUpdateAvatarEvent extends GenericSelfUpdateEvent
{
    private final String oldAvatarId;

    public SelfUpdateAvatarEvent(JDA api, long responseNumber, String oldAvatarId)
    {
        super(api, responseNumber);
        this.oldAvatarId = oldAvatarId;
    }

    public String getOldAvatarId()
    {
        return oldAvatarId;
    }

    public String getOldAvatarUrl()
    {
        return oldAvatarId == null ? null : "https://cdn.discordapp.com/avatars/" + getSelfUser().getId() + "/" + oldAvatarId + ".jpg";
    }
}
