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
package net.dv8tion.jda.core.events;

import com.neovisionaries.ws.client.WebSocketFrame;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.requests.CloseCode;

import java.time.OffsetDateTime;

/**
 * Indicates that JDA has been disconnected from the remote server.
 * <br>When this event is fired JDA will try to reconnect if possible
 * unless {@link net.dv8tion.jda.core.JDABuilder#setAutoReconnect(boolean) JDABuilder.setAutoReconnect(Boolean)}
 * has been provided {@code false}!
 *
 * <p>When reconnecting was successful either a {@link net.dv8tion.jda.core.events.ReconnectedEvent ReconnectEvent}
 * or a {@link net.dv8tion.jda.core.events.ResumedEvent ResumedEvent} is fired
 */
public class DisconnectEvent extends Event
{
    protected final WebSocketFrame serverCloseFrame;
    protected final WebSocketFrame clientCloseFrame;
    protected final boolean closedByServer;
    protected final OffsetDateTime disconnectTime;

    public DisconnectEvent(JDA api, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer,
                           OffsetDateTime disconnectTime)
    {
        super(api, -1);
        this.serverCloseFrame = serverCloseFrame;
        this.clientCloseFrame = clientCloseFrame;
        this.closedByServer = closedByServer;
        this.disconnectTime = disconnectTime;
    }

    /**
     * Possibly-null {@link net.dv8tion.jda.core.requests.CloseCode CloseCode}
     * representing the meaning for this DisconnectEvent
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.requests.CloseCode CloseCode}
     */
    public CloseCode getCloseCode()
    {
        return serverCloseFrame != null ? CloseCode.from(serverCloseFrame.getCloseCode()) : null;
    }

    public WebSocketFrame getServiceCloseFrame()
    {
        return serverCloseFrame;
    }

    public WebSocketFrame getClientCloseFrame()
    {
        return clientCloseFrame;
    }

    public boolean isClosedByServer()
    {
        return closedByServer;
    }

    public OffsetDateTime getDisconnectTime()
    {
        return disconnectTime;
    }
}
