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

package net.dv8tion.jda.internal.audio;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.internal.utils.EntityString;

public class ConnectionRequest
{
    protected final long guildId;
    protected long nextAttemptEpoch;
    protected ConnectionStage stage;
    protected long channelId;

    public ConnectionRequest(Guild guild)
    {
        this.stage = ConnectionStage.DISCONNECT;
        this.guildId = guild.getIdLong();
    }

    public ConnectionRequest(AudioChannel channel, ConnectionStage stage)
    {
        this.channelId = channel.getIdLong();
        this.guildId = channel.getGuild().getIdLong();
        this.stage = stage;
        this.nextAttemptEpoch = System.currentTimeMillis();
    }

    public void setStage(ConnectionStage stage)
    {
        this.stage = stage;
    }

    public void setChannel(AudioChannel channel)
    {
        this.channelId = channel.getIdLong();
    }

    public void setNextAttemptEpoch(long epochMillis)
    {
        this.nextAttemptEpoch = epochMillis;
    }

    public AudioChannel getChannel(JDA api)
    {
        return (AudioChannel) api.getGuildChannelById(channelId);
    }

    public long getChannelId()
    {
        return channelId;
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

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setType(stage)
                .addMetadata("guildId", Long.toUnsignedString(guildId))
                .addMetadata("channelId", Long.toUnsignedString(channelId))
                .toString();
    }
}
