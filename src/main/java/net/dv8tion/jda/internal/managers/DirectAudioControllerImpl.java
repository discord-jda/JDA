/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.internal.managers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.DirectAudioController;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.WebSocketClient;
import net.dv8tion.jda.internal.utils.cache.UpstreamReference;

public class DirectAudioControllerImpl implements DirectAudioController
{
    private final UpstreamReference<JDAImpl> api;

    public DirectAudioControllerImpl(JDAImpl api)
    {
        this.api = new UpstreamReference<>(api);
    }

    @Override
    public JDAImpl getJDA()
    {
        return api.get();
    }

    @Override
    public void connect(VoiceChannel channel)
    {
        JDAImpl jda = getJDA();
        WebSocketClient client = jda.getClient();
        client.queueAudioConnect(channel);
    }

    @Override
    public void disconnect(Guild guild)
    {
        JDAImpl jda = getJDA();
        WebSocketClient client = jda.getClient();
        client.queueAudioDisconnect(guild);
    }

    @Override
    public void reconnect(VoiceChannel channel)
    {
        JDAImpl jda = getJDA();
        WebSocketClient client = jda.getClient();
        client.queueAudioReconnect(channel);
    }

    @Override
    public void update(Guild guild, VoiceChannel channel)
    {
        JDAImpl jda = getJDA();
        WebSocketClient client = jda.getClient();
        client.updateAudioConnection(guild.getIdLong(), channel);
    }
}
