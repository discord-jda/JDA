/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.entities;

import java.time.OffsetDateTime;

public interface Member
{
    User getUser();

    OffsetDateTime getJoinDate();

    VoiceStatus getVoiceStatus();

    /**
     * Returns the current nickname of this Member for the parent Guild.
     *
     * @return
     *      The nickname or null, if no nickname is set.
     */
    String getNickname();
}
