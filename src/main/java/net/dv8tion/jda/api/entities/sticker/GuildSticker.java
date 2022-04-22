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

package net.dv8tion.jda.api.entities.sticker;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import java.util.Set;

/*
{
    "id": "901811858401554432",
    "name": "ratjam",
    "tags": "sunglasses",
    "type": 2,
    "format_type": 2,
    "description": "",
    "asset": "",
    "available": true,
    "guild_id": "836683869255499837",
    "user": {"id": "86699011792191488", "username": "Minn", "avatar": "2a2c6ed8f0cb8fdd9b1b34eb926b32e6", "avatar_decoration": null, "discriminator": "6688", "public_flags": 512}
}
 */
public interface GuildSticker extends Sticker
{
    @Nonnull
    Sticker.Type getType();

    /**
     * Set of tags of the sticker. Tags can be used instead of the name of the sticker as aliases.
     *
     * @return Possibly-empty unmodifiable Set of tags of the sticker
     */
    @Nonnull
    Set<String> getTags();

    /**
     * The description of the sticker, or empty String if the sticker doesn't have one.
     *
     * @return Possibly-empty String containing the description of the sticker
     */
    @Nonnull
    String getDescription();

    boolean isAvailable();

    @Nonnull
    Guild getGuild();

    @Nonnull
    User getOwner();

    /*

     * The ID of the pack the sticker is from.
     *
     * <p>If this sticker is from a guild, this will be the guild id instead.
     *
     * @return the ID of the pack the sticker is from
     *
    getPackId()

     */
}
