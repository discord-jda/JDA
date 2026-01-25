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

import net.dv8tion.jda.api.requests.WebSocketClient;

public abstract class NVWebSocketClientListenerAdapter implements WebSocketClient.WebSocketEventListener {
    @Override
    public void onEvent(WebSocketClient.WebSocketEvent event) {
        try {
            if (event instanceof WebSocketClient.WebSocketConnectedEvent) {
                onWebSocketConnectedEvent((WebSocketClient.WebSocketConnectedEvent) event);
            }
            if (event instanceof WebSocketClient.WebSocketDisconnectedEvent) {
                onWebSocketDisconnectedEvent((WebSocketClient.WebSocketDisconnectedEvent) event);
            }
            if (event instanceof WebSocketClient.WebSocketExceptionEvent) {
                onWebSocketExceptionEvent((WebSocketClient.WebSocketExceptionEvent) event);
            }
            if (event instanceof WebSocketClient.WebSocketBinaryMessageEvent) {
                onWebSocketBinaryMessageEvent((WebSocketClient.WebSocketBinaryMessageEvent) event);
            }
            if (event instanceof WebSocketClient.WebSocketTextMessageEvent) {
                onWebSocketTextMessageEvent((WebSocketClient.WebSocketTextMessageEvent) event);
            }
        } catch (Exception e) {
            onWebSocketExceptionEvent(new WebSocketClient.WebSocketExceptionEvent(e));
        }
    }

    protected abstract void onWebSocketConnectedEvent(WebSocketClient.WebSocketConnectedEvent event) throws Exception;

    protected abstract void onWebSocketDisconnectedEvent(WebSocketClient.WebSocketDisconnectedEvent event)
            throws Exception;

    protected abstract void onWebSocketExceptionEvent(WebSocketClient.WebSocketExceptionEvent event);

    protected abstract void onWebSocketBinaryMessageEvent(WebSocketClient.WebSocketBinaryMessageEvent event)
            throws Exception;

    protected abstract void onWebSocketTextMessageEvent(WebSocketClient.WebSocketTextMessageEvent event)
            throws Exception;
}
