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

package net.dv8tion.jda.core.audio;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class ConnectionRequest
{
    protected final long guildId;
    protected long nextAttemptEpoch;
    protected ConnectionStage stage;
    protected VoiceChannel channel;

    public ConnectionRequest(Guild guild)
    {
        this.stage = ConnectionStage.DISCONNECT;
        this.guildId = guild.getIdLong();
    }

    public ConnectionRequest(VoiceChannel channel, ConnectionStage stage)
    {
        this.channel = channel;
        this.guildId = channel.getGuild().getIdLong();
        this.stage = stage;
        this.nextAttemptEpoch = System.currentTimeMillis();
    }

    public void setStage(ConnectionStage stage)
    {
        this.stage = stage;
    }

    public void setChannel(VoiceChannel channel)
    {
        this.channel = channel;
    }

    public void setNextAttemptEpoch(long epochMillis)
    {
        this.nextAttemptEpoch = epochMillis;
    }

    public VoiceChannel getChannel()
    {
        return channel;
    }

    public ConnectionStage getStage()
    {
        return stage;
    }

    public long getNextAttemptEpoch()
    {
        return nextAttemptEpoch;
    }

    public long getGuildIdLong()
    {
        return guildId;
    }
}
