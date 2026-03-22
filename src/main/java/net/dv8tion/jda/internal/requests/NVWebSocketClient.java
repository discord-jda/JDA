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
 *
 */

package net.dv8tion.jda.internal.requests;

import com.neovisionaries.ws.client.*;
import net.dv8tion.jda.api.requests.WebSocketClient;
import net.dv8tion.jda.internal.utils.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UncheckedIOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NVWebSocketClient implements WebSocketClient {
    private static final Logger log = LoggerFactory.getLogger(NVWebSocketClient.class);
    private final Set<WebSocketEventListener> listeners = new HashSet<>();
    private final WebSocketFactory webSocketFactory;
    private WebSocket webSocket;

    public NVWebSocketClient(WebSocketFactory factory) {
        this.webSocketFactory = factory;
    }

    @Override
    public void addListener(WebSocketEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(WebSocketEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void connect(String url, Duration timeout) {
        try {
            WebSocketFactory factory = new WebSocketFactory(webSocketFactory);
            IOUtil.setServerName(factory, url);
            webSocket = factory.setSocketTimeout((int) timeout.toMillis())
                    .createSocket(url)
                    .setDirectTextMessage(true)
                    .addHeader("Accept-Encoding", "gzip")
                    .addListener(new NVWebSocketEventListener())
                    .connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setReadTimeout(Duration timeout) {
        Socket socket = webSocket.getSocket();
        if (socket != null) {
            try {
                socket.setSoTimeout((int) timeout.toMillis());
            } catch (SocketException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public void disconnect(int closeCode, String reason, boolean forceClose) {
        if (webSocket != null) {
            Socket socket = webSocket.getSocket();
            if (socket != null) {
                try {
                    socket.setSoTimeout(1000);
                } catch (SocketException ignored) {

                }
            }

            if (reason != null) {
                if (forceClose) {
                    webSocket.disconnect(closeCode, reason);
                } else {
                    webSocket.sendClose(closeCode, reason);
                }
            } else {
                if (forceClose) {
                    webSocket.disconnect(closeCode);
                } else {
                    webSocket.sendClose(closeCode);
                }
            }
        }

        webSocket = null;
    }

    @Override
    public void sendBinaryMessage(byte[] message) {
        webSocket.sendBinary(message);
    }

    @Override
    public void sendTextMessage(String message) {
        webSocket.sendText(message);
        webSocket.flush();
    }

    private void dispatch(WebSocketEvent event) {
        for (WebSocketEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    private class NVWebSocketEventListener extends WebSocketAdapter {
        @Override
        public void onConnected(WebSocket webSocket, Map<String, List<String>> headers) {
            dispatch(new WebSocketConnectedEvent());
        }

        @Override
        public void onDisconnected(
                WebSocket webSocket, WebSocketFrame serverFrame, WebSocketFrame clientFrame, boolean closedByServer) {
            if (closedByServer && serverFrame != null) {
                dispatch(
                        new WebSocketDisconnectedEvent(serverFrame.getCloseCode(), serverFrame.getCloseReason(), true));
            } else if (clientFrame != null) {
                dispatch(new WebSocketDisconnectedEvent(
                        clientFrame.getCloseCode(), clientFrame.getCloseReason(), closedByServer));
            } else {
                dispatch(new WebSocketDisconnectedEvent(1006, null, closedByServer));
            }
        }

        @Override
        public void onTextMessage(WebSocket webSocket, byte[] bytes) {
            dispatch(new WebSocketTextMessageEvent(bytes));
        }

        @Override
        public void onBinaryMessage(WebSocket webSocket, byte[] bytes) {
            dispatch(new WebSocketBinaryMessageEvent(bytes));
        }
    }
}
