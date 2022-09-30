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

package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.sticker.GuildSticker;
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * Manager providing functionality to update one or more fields for {@link GuildSticker}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setName("catDance")
 *        .setDescription("Cat dancing")
 *        .queue();
 * manager.reset(GuildStickerManager.NAME | GuildStickerManager.TAGS)
 *        .setName("dogDance")
 *        .setTags("dancing", "dog")
 *        .queue();
 * }</pre>
 *
 * @see GuildSticker#getManager()
 * @see Guild#editSticker(StickerSnowflake)
 */
public interface GuildStickerManager extends Manager<GuildStickerManager>
{
    /** Used to reset name field */
    long NAME = 1;
    /** Used to reset description field */
    long DESCRIPTION = 1 << 1;
    /** Used to reset tags field */
    long TAGS = 1 << 2;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(GuildStickerManager.NAME | GuildStickerManager.TAGS);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #DESCRIPTION}</li>
     *     <li>{@link #TAGS}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return GuildStickerManager for chaining convenience
     */
    @Nonnull
    @Override
    GuildStickerManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br>Example: {@code manager.reset(GuildStickerManager.NAME, GuildStickerManager.TAGS);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #DESCRIPTION}</li>
     *     <li>{@link #TAGS}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return GuildStickerManager for chaining convenience
     */
    @Nonnull
    @Override
    GuildStickerManager reset(long... fields);

    /**
     * The {@link Guild} this Manager's {@link GuildSticker} is in.
     *
     * <p>This is null if {@link GuildSticker#getManager()} is used on a sticker with an uncached guild.
     *
     * @return The {@link Guild Guild}, or null if not present.
     *
     * @see    #getGuildId()
     */
    @Nullable
    Guild getGuild();

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
     * Sets the <b><u>name</u></b> of the sticker.
     *
     * <p>A sticker name <b>must</b> be between 2-30 characters long!
     *
     * <p><b>Example</b>: {@code catDance} or {@code dogWave}
     *
     * @param  name
     *         The new name for the sticker (2-30 characters)
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 2-30 characters long
     *
     * @return GuildStickerManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildStickerManager setName(@Nonnull String name);


    /**
     * Sets the <b><u>description</u></b> of the sticker.
     *
     * <p>A sticker description <b>must</b> be between 2-100 characters long!
     *
     * @param  description
     *         The new description for the sticker (2-100 characters)
     *
     * @throws IllegalArgumentException
     *         If the provided description is {@code null} or not between 2-100 characters long
     *
     * @return GuildStickerManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildStickerManager setDescription(@Nonnull String description);

    /**
     * Sets the <b><u>tags</u></b> of the sticker.
     * <br>These are used for auto-complete when sending a message in the client, and for the sticker picker menu.
     *
     * <p>The combined list of sticker tags <b>must</b> at most be 200 characters long!
     *
     * <p><b>Example</b>: {@code catDance} or {@code dogWave}
     *
     * @param  tags
     *         The new tags for the sticker (up to 200 characters)
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code tags} is {@code null}</li>
     *             <li>If {@code tags} is empty</li>
     *             <li>If {@code tags} contains {@code null} or empty strings</li>
     *             <li>If the concatenated tags are more than 200 characters long (including commas between tags)</li>
     *         </ul>
     *
     * @return GuildStickerManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildStickerManager setTags(@Nonnull Collection<String> tags);

    /**
     * Sets the <b><u>tags</u></b> of the sticker.
     * <br>These are used for auto-complete when sending a message in the client, and for the sticker picker menu.
     *
     * <p>The combined list of sticker tags <b>must</b> at most be 200 characters long!
     *
     * <p><b>Example</b>: {@code catDance} or {@code dogWave}
     *
     * @param  tags
     *         The new tags for the sticker (up to 200 characters)
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code tags} is {@code null}</li>
     *             <li>If {@code tags} is empty</li>
     *             <li>If {@code tags} contains {@code null} or empty strings</li>
     *             <li>If the concatenated tags are more than 200 characters long (including commas between tags)</li>
     *         </ul>
     *
     * @return GuildStickerManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default GuildStickerManager setTags(@Nonnull String... tags)
    {
        Checks.noneNull(tags, "Tags");
        return setTags(Arrays.asList(tags));
    }
}
