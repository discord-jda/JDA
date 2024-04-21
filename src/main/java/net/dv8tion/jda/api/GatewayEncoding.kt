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

package net.dv8tion.jda.api;

/**
 * Encoding mode used by the gateway for incoming and outgoing payloads.
 */
public enum GatewayEncoding
{
    /**
     * Standard JSON format. This format uses a human-readable string encoding.
     *
     * @see <a href="https://www.json.org/json-en.html" target="_blank">JSON Specification</a>
     */
    JSON,
    /**
     * Erlang External Term Format (binary). This is an optimized format which encodes all payloads
     * in a binary stream.
     *
     * @since 4.2.1
     *
     * @see <a href="https://erlang.org/doc/apps/erts/erl_ext_dist.html" target="_blank">Erlang -- External Term Format</a>
     */
    ETF
}
