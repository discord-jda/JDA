/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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
package net.dv8tion.jda.core.requests.factory;

/**
 * Provides JDA with a strategy to use when determining a URL that the Main Websocket will use to connect to Discord.
 * Custom implementations can be used to have the actual gateway connection on a separate process, so that
 * if the bot process ever shuts down, the connection won't be lost and events can still be received or,
 * for large bots, the daily login limit isn't reached.
 * <br>This is an advanced feature, and most bots shouldn't need to implement this.
 */
public interface IGatewayProvider
{
    /**
     * Return a URL telling JDA where to connect the websocket, allowing proxying of the connection
     * to the discord gateway, such as a cache for faster bot loads and avoiding the need to re-login when
     * updating the bot.
     * <br>This method is only called once per JDA instance, before it connects.
     *
     * @return The URL JDA should connect to.
     */
    String getGatewayUrl();
}
