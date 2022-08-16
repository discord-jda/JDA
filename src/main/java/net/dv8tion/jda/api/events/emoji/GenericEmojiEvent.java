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

package net.dv8tion.jda.api.events.emoji;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link RichCustomEmoji Custom Emoji} was created/removed/updated.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require the {@link net.dv8tion.jda.api.utils.cache.CacheFlag#EMOJI EMOJI} CacheFlag to be enabled, which requires
 * the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_EMOJIS_AND_STICKERS GUILD_EMOJIS_AND_STICKERS} intent.
 *
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 */
public abstract class GenericEmojiEvent extends Event
{
    protected final RichCustomEmoji emoji;

    public GenericEmojiEvent(@Nonnull JDA api, long responseNumber, @Nonnull RichCustomEmoji emoji)
    {
        super(api, responseNumber);
        this.emoji = emoji;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} where the emoji came from
     *
     * @return The origin Guild
     */
    @Nonnull
    public Guild getGuild()
    {
        return emoji.getGuild();
    }

    /**
     * The affected {@link RichCustomEmoji} for this event
     *
     * @return The emoji
     */
    @Nonnull
    public RichCustomEmoji getEmoji()
    {
        return emoji;
    }

    /**
     * Whether this emoji is managed by an integration
     *
     * @return True, if this emoji is managed by an integration
     */
    public boolean isManaged()
    {
        return emoji.isManaged();
    }
}
