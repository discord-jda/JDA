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

package net.dv8tion.jda.api.events.sticker;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.sticker.GuildSticker;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;

/**
 * Indicates that an {@link GuildSticker} was created/removed/updated.
 *
 * <p><b>Requirements</b>
 *
 * <p>These events require the {@link net.dv8tion.jda.api.utils.cache.CacheFlag#STICKER STICKER} CacheFlag to be enabled, which requires
 * the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_EMOJIS_AND_STICKERS GUILD_EMOJIS_AND_STICKERS} intent.
 *
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 */
public abstract class GenericGuildStickerEvent extends Event
{
    protected final Guild guild;
    protected final GuildSticker sticker;

    public GenericGuildStickerEvent(@Nonnull JDA api, long responseNumber,
                                    @Nonnull Guild guild, @Nonnull GuildSticker sticker)
    {
        super(api, responseNumber);
        this.guild = guild;
        this.sticker = sticker;
    }

    /**
     * The relevant {@link GuildSticker} for this event
     *
     * @return The sticker
     */
    @Nonnull
    public GuildSticker getSticker()
    {
        return sticker;
    }

    /**
     * The {@link Guild} this sticker belongs to
     *
     * @return The relevant guild
     */
    @Nonnull
    public Guild getGuild()
    {
        return guild;
    }
}
