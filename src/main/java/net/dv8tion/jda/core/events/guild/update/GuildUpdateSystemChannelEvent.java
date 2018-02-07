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

package net.dv8tion.jda.core.events.guild.update;

import edu.umd.cs.findbugs.annotations.Nullable;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * <b><u>GuildUpdateSystemChannelEvent</u></b><br>
 * Fired if the {@link net.dv8tion.jda.core.entities.Guild#getSystemChannel() system channel} of a {@link net.dv8tion.jda.core.entities.Guild Guild} has been changed.<br>
 * <br>
 * Use: Retrieve the {@link #getOldSystemChannel() previous system channel} after a change.
 */
public class GuildUpdateSystemChannelEvent extends GenericGuildUpdateEvent
{
    private final TextChannel oldSystemChannel;

    public GuildUpdateSystemChannelEvent(JDA api, long responseNumber, Guild guild, @Nullable TextChannel oldSystemChannel)
    {
        super(api, responseNumber, guild);
        this.oldSystemChannel = oldSystemChannel;
    }

    /**
     * Returns the previous system channel.
     * 
     * @return The previous system channel
     */
    @Nullable
    public TextChannel getOldSystemChannel()
    {
        return oldSystemChannel;
    }
}
