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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.audio.hooks;

public enum ConnectionStatus
{
    NOT_CONNECTED,
    CONNECTING_AWAITING_ENDPOINT,
    CONNECTING_AWAITING_WEBSOCKET_CONNECT,
    CONNECTING_AWAITING_AUTHENTICATING,
    CONNECTING_ATTEMPTING_UDP_DISCOVERY,
    CONNECTING_AWAITING_READY,
    CONNECTED,

    AUDIO_REGION_CHANGE,

    ERROR_LOST_CONNECTION,
    ERROR_WEBSOCKET_UNABLE_TO_CONNECT,
    ERROR_UDP_UNABLE_TO_CONNECT,
    ERROR_CONNECTION_TIMEOUT;
}
