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
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.events.sticker.GenericGuildStickerEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that an {@link GuildSticker} was updated.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require the {@link net.dv8tion.jda.api.utils.cache.CacheFlag#STICKER STICKER} CacheFlag to be enabled, which requires
 * the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_EMOJIS_AND_STICKERS GUILD_EMOJIS_AND_STICKERS} intent.
 *
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 */
public abstract class GenericGuildStickerUpdateEvent<T> extends GenericGuildStickerEvent implements UpdateEvent<GuildSticker, T>
{
    protected final String identifier;
    protected final T previous;
    protected final T next;

    public GenericGuildStickerUpdateEvent(@Nonnull JDA api, long responseNumber,
                                          @Nonnull Guild guild, @Nonnull GuildSticker sticker,
                                          @Nonnull String identifier, T oldValue, T newValue)
    {
        super(api, responseNumber, guild, sticker);
        this.identifier = identifier;
        this.previous = oldValue;
        this.next = newValue;
    }

    @Nonnull
    @Override
    public String getPropertyIdentifier()
    {
        return identifier;
    }

    @Nonnull
    @Override
    public GuildSticker getEntity()
    {
        return getSticker();
    }

    @Nullable
    @Override
    public T getOldValue()
    {
        return previous;
    }

    @Nullable
    @Override
    public T getNewValue()
    {
        return next;
    }
}
