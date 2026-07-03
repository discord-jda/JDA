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

package net.dv8tion.jda.api.endpoints;

import net.dv8tion.jda.annotations.ExperimentalRestApi;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.internal.endpoints.MessageApiImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

/**
 * Entrypoint for REST-only APIs.
 *
 * <h3>Stability note</h3>
 * The API and ABI of this interface and all objects returned by it, are <b>subject to changes without warning</b>.
 */
@ExperimentalRestApi
public interface ApiEndpoints {
    /**
     * Creates a {@link MessageApi} instance to interact with the messages of the provided channel.
     *
     * @param  jda
     *         The JDA instance
     * @param  channelId
     *         The ID of the channel to interact in
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}
     *
     * @return The {@link MessageApi} instance
     *
     * @see <a href="https://docs.discord.com/developers/resources/message" target="_blank">Message Resource - Discord docs</a>
     */
    @Nonnull
    static MessageApi messages(@Nonnull JDA jda, long channelId) {
        Checks.notNull(jda, "JDA");
        return new MessageApiImpl(jda, Long.toUnsignedString(channelId));
    }

    /**
     * Creates a {@link MessageApi} instance to interact with the messages of the provided channel.
     *
     * @param  jda
     *         The JDA instance
     * @param  channelId
     *         The ID of the channel to interact in
     *
     * @throws IllegalArgumentException
     *         If an argument is {@code null}
     *
     * @return The {@link MessageApi} instance
     *
     * @see <a href="https://docs.discord.com/developers/resources/message" target="_blank">Message Resource - Discord docs</a>
     */
    @Nonnull
    static MessageApi messages(@Nonnull JDA jda, @Nonnull String channelId) {
        Checks.notNull(jda, "JDA");
        Checks.isSnowflake(channelId, "Channel ID");
        return new MessageApiImpl(jda, channelId);
    }
}
