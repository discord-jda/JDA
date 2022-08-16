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

/**
 * Events that track {@link net.dv8tion.jda.api.events.emoji.EmojiAddedEvent added emojis}
 * and {@link net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent removed emojis}.
 * <br>Important to remember that {@link net.dv8tion.jda.api.entities.emoji.RichCustomEmoji Custom Emojis} are not the same as unicode emoji!
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require the {@link net.dv8tion.jda.api.utils.cache.CacheFlag#EMOJI EMOJI} CacheFlag to be enabled, which requires
 * the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_EMOJIS_AND_STICKERS GUILD_EMOJIS_AND_STICKERS} intent.
 *
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 */
package net.dv8tion.jda.api.events.emoji;
