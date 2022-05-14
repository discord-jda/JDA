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

package net.dv8tion.jda.api.events.sticker.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.sticker.GuildSticker;

import javax.annotation.Nonnull;

/**
 * Indicates that the description of a {@link GuildSticker} changed.
 *
 * <p>Can be used to retrieve the old description
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.utils.cache.CacheFlag#STICKER STICKER} CacheFlag to be enabled, which requires
 * the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_EMOJIS_AND_STICKERS GUILD_EMOJIS_AND_STICKERS} intent.
 *
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 *
 * <p>Identifier: {@code description}
 */
public class GuildStickerUpdateDescriptionEvent extends GenericGuildStickerUpdateEvent<String>
{
    public static final String IDENTIFIER = "description";

    public GuildStickerUpdateDescriptionEvent(@Nonnull JDA api, long responseNumber,
                                       @Nonnull Guild guild, @Nonnull GuildSticker sticker, @Nonnull String oldValue)
    {
        super(api, responseNumber, guild, sticker, IDENTIFIER, oldValue, sticker.getDescription());
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public String getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public String getNewValue()
    {
        return super.getNewValue();
    }
}
