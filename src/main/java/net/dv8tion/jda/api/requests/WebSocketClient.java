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

package net.dv8tion.jda.api.requests;

import java.time.Duration;

public interface WebSocketClient {
    void addListener(WebSocketEventListener listener);

    void removeListener(WebSocketEventListener listener);

    void connect(String url, Duration timeout);

    void setReadTimeout(Duration timeout);

    default void disconnect(int closeCode) {
        disconnect(closeCode, null, false);
    }

    default void disconnect(int closeCode, String reason)
    {
        disconnect(closeCode, reason, false);
    }

    void disconnect(int closeCode, String reason, boolean forceClose);

    void sendBinaryMessage(byte[] message);

    void sendTextMessage(String message);

    interface WebSocketEvent {}

    @FunctionalInterface
    interface WebSocketEventListener {
        void onEvent(WebSocketEvent event);
    }

    class WebSocketConnectedEvent implements WebSocketEvent {}

    class WebSocketDisconnectedEvent implements WebSocketEvent {
        private final int closeCode;
        private final String reason;
        private final boolean closedByServer;

        public WebSocketDisconnectedEvent(int closeCode, String reason, boolean closedByServer) {
            this.closeCode = closeCode;
            this.reason = reason;
            this.closedByServer = closedByServer;
        }

        public int getCloseCode() {
            return closeCode;
        }

        public String getReason() {
            return reason;
        }

        public boolean isClosedByServer() {
            return closedByServer;
        }
    }

    class WebSocketExceptionEvent implements WebSocketEvent {
        private final Throwable cause;

        public WebSocketExceptionEvent(Throwable cause) {
            this.cause = cause;
        }

        public Throwable getCause() {
            return cause;
        }
    }

    class WebSocketBinaryMessageEvent implements WebSocketEvent {
        private final byte[] payload;

        public WebSocketBinaryMessageEvent(byte[] payload) {
            this.payload = payload;
        }

        public byte[] getPayload() {
            return payload;
        }
    }

    class WebSocketTextMessageEvent implements WebSocketEvent {
        private final byte[] payload;

        public WebSocketTextMessageEvent(byte[] payload) {
            this.payload = payload;
        }

        public byte[] getPayload() {
            return payload;
        }
    }
}
