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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.managers.GuildStickerManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import org.jetbrains.annotations.Contract;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Custom guild sticker created by a user.
 */
public interface GuildSticker extends RichSticker
{
    /**
     * Whether this sticker is currently available.
     * <br>A sticker becomes unavailable when the boost level of a guild drops and the slot becomes unusable.
     *
     * @return True, if this sticker is available
     */
    boolean isAvailable();

    /**
     * The ID of the guild this sticker belongs to.
     *
     * @return The guild id
     */
    long getGuildIdLong();

    /**
     * The ID of the guild this sticker belongs to.
     *
     * @return The guild id
     */
    @Nonnull
    default String getGuildId()
    {
        return Long.toUnsignedString(getGuildIdLong());
    }

    /**
     * The {@link Guild} this sticker belongs to.
     * <br>This is null if the guild is not cached on creation,
     * Which is often the case for {@link net.dv8tion.jda.api.JDA#retrieveSticker(StickerSnowflake) JDA.retrieveSticker(...)}.
     *
     * @return Possibly-null guild of the sticker
     */
    @Nullable
    Guild getGuild();

    /**
     * The user who created this sticker.
     * <br>This is null if the sticker is retrieved from cache,
     * since the owner is only provided for explicitly requested stickers.
     *
     * @return Possibly-null sticker owner
     *
     * @see    #retrieveOwner()
     */
    @Nullable
    User getOwner();

    /**
     * Retrieves the sticker owner.
     * <br>If {@link #getOwner()} is present, this will directly return the owner in a completed {@link RestAction} without making a request.
     * The user information might be outdated, you can use {@link #retrieveOwner(boolean)} to force an update.
     *
     * @return {@link RestAction} - Type: {@link User}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<User> retrieveOwner()
    {
        return retrieveOwner(false);
    }

    /**
     * Retrieves the sticker owner.
     * <br>If {@link #getOwner()} is present, this will directly return the owner in a completed {@link RestAction} without making a request.
     *
     * @param  update
     *         True, if the request should ignore the cache and always fetch the latest information
     *
     * @return {@link RestAction} - Type: {@link User}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<User> retrieveOwner(boolean update);

    /**
     * Deletes this sticker from the guild.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_EMOTES_AND_STICKERS MANAGE_EMOTES_AND_STICKERS} in the guild.
     *
     * @return {@link AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> delete();

    /**
     * Modify this sticker using {@link GuildStickerManager}.
     * <br>You can update multiple fields at once, by calling the respective setters before executing the request.
     *
     * @throws IllegalStateException
     *         If {@link #getGuild()} is null
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_EMOTES_AND_STICKERS MANAGE_EMOTES_AND_STICKERS} in the guild.
     *
     * @return {@link GuildStickerManager}
     */
    @Nonnull
    @CheckReturnValue
    @Contract("->new")
    GuildStickerManager getManager();
}