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

package net.dv8tion.jda.api.managers.channel;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.managers.Manager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Manager providing functionality to update one or more fields for a {@link GuildChannel GuildChannel}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setName("github-log")
 *        .setTopic("logs for github commits")
 *        .setNSFW(false)
 *        .queue();
 * manager.reset(ChannelManager.PARENT | ChannelManager.NAME)
 *        .setName("nsfw-commits")
 *        .queue();
 * manager.setTopic("Java is to Javascript as wall is to wallet")
 *        .queue();
 * }</pre>
 *
 * @see GuildChannel#getManager()
 */
public interface ChannelManager<T extends GuildChannel, M extends ChannelManager<T, M>> extends Manager<M>
{
    /** Used to reset the name field */
    long NAME                  = 1;
    /** Used to reset the parent field */
    long PARENT                = 1 << 1;
    /** Used to reset the topic field */
    long TOPIC                 = 1 << 2;
    /** Used to reset the position field */
    long POSITION              = 1 << 3;
    /** Used to reset the nsfw field */
    long NSFW                  = 1 << 4;
    /** Used to reset the userlimit field */
    long USERLIMIT             = 1 << 5;
    /** Used to reset the bitrate field */
    long BITRATE               = 1 << 6;
    /** Used to reset the permission field */
    long PERMISSION            = 1 << 7;
    /** Used to reset the rate-limit per user field */
    long SLOWMODE              = 1 << 8;
    /** Used to reset the channel type field */
    long TYPE                  = 1 << 9;
    /** Used to reset the region field */
    long REGION                = 1 << 10;
    /** Used to reset the auto-archive-duration field */
    long AUTO_ARCHIVE_DURATION = 1 << 11;
    /** Used to reset the archived field */
    long ARCHIVED              = 1 << 12;
    /** Used to reset the locked field */
    long LOCKED                = 1 << 13;
    /** Used to reset the invitable field */
    long INVITEABLE            = 1 << 14;
    /** Used to reset the available tags field */
    long AVAILABLE_TAGS        = 1 << 15;
    /** Used to reset the applied tags field */
    long APPLIED_TAGS          = 1 << 16;
    /** Used to reset the pinned state field */
    long PINNED                = 1 << 17;
    /** Used to reset the require tag state field */
    long REQUIRE_TAG           = 1 << 18;
    /** Used to reset the default reaction emoji field */
    long DEFAULT_REACTION      = 1 << 19;
    /** Used to reset the default layout field */
    long DEFAULT_LAYOUT        = 1 << 20;
    /** Used to reset the default sort order field */
    long DEFAULT_SORT_ORDER    = 1 << 21;
    /** Used to reset the hide media download option flag */
    long HIDE_MEDIA_DOWNLOAD_OPTIONS = 1 << 22;
    /** Used to reset the default thread slowmode of a thread container */
    long DEFAULT_THREAD_SLOWMODE = 1 << 23;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(ChannelManager.NAME | ChannelManager.PARENT);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #PARENT}</li>
     *     <li>{@link #TOPIC}</li>
     *     <li>{@link #POSITION}</li>
     *     <li>{@link #NSFW}</li>
     *     <li>{@link #SLOWMODE}</li>
     *     <li>{@link #USERLIMIT}</li>
     *     <li>{@link #BITRATE}</li>
     *     <li>{@link #PERMISSION}</li>
     *     <li>{@link #TYPE}</li>
     *     <li>{@link #REGION}</li>
     *     <li>{@link #AUTO_ARCHIVE_DURATION}</li>
     *     <li>{@link #ARCHIVED}</li>
     *     <li>{@link #LOCKED}</li>
     *     <li>{@link #INVITEABLE}</li>
     *     <li>{@link #AVAILABLE_TAGS}</li>
     *     <li>{@link #APPLIED_TAGS}</li>
     *     <li>{@link #PINNED}</li>
     *     <li>{@link #REQUIRE_TAG}</li>
     *     <li>{@link #DEFAULT_REACTION}</li>
     *     <li>{@link #DEFAULT_LAYOUT}</li>
     *     <li>{@link #DEFAULT_SORT_ORDER}</li>
     *     <li>{@link #HIDE_MEDIA_DOWNLOAD_OPTIONS}</li>
     *     <li>{@link #DEFAULT_THREAD_SLOWMODE}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @Override
    M reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br>Example: {@code manager.reset(ChannelManager.NAME, ChannelManager.PARENT);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #PARENT}</li>
     *     <li>{@link #TOPIC}</li>
     *     <li>{@link #POSITION}</li>
     *     <li>{@link #NSFW}</li>
     *     <li>{@link #USERLIMIT}</li>
     *     <li>{@link #BITRATE}</li>
     *     <li>{@link #PERMISSION}</li>
     *     <li>{@link #TYPE}</li>
     *     <li>{@link #REGION}</li>
     *     <li>{@link #AUTO_ARCHIVE_DURATION}</li>
     *     <li>{@link #ARCHIVED}</li>
     *     <li>{@link #LOCKED}</li>
     *     <li>{@link #INVITEABLE}</li>
     *     <li>{@link #AVAILABLE_TAGS}</li>
     *     <li>{@link #APPLIED_TAGS}</li>
     *     <li>{@link #PINNED}</li>
     *     <li>{@link #REQUIRE_TAG}</li>
     *     <li>{@link #DEFAULT_REACTION}</li>
     *     <li>{@link #DEFAULT_LAYOUT}</li>
     *     <li>{@link #DEFAULT_SORT_ORDER}</li>
     *     <li>{@link #HIDE_MEDIA_DOWNLOAD_OPTIONS}</li>
     *     <li>{@link #DEFAULT_THREAD_SLOWMODE}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @Override
    M reset(long... fields);

    /**
     * The {@link GuildChannel GuildChannel} that will
     * be modified by this Manager instance
     *
     * @return The {@link GuildChannel GuildChannel}
     */
    @Nonnull
    T getChannel();

    /**
     * The {@link Guild Guild} this Manager's
     * {@link GuildChannel GuildChannel} is in.
     * <br>This is logically the same as calling {@code getChannel().getGuild()}
     *
     * @return The parent {@link Guild Guild}
     */
    @Nonnull
    default Guild getGuild()
    {
        return getChannel().getGuild();
    }

    /**
     * Sets the <b><u>name</u></b> of the selected {@link GuildChannel GuildChannel}.
     *
     * <p>A channel name <b>must not</b> be {@code null} nor empty or more than {@value Channel#MAX_NAME_LENGTH} characters long!
     * <br>TextChannel names may only be populated with alphanumeric (with underscore and dash).
     *
     * <p><b>Example</b>: {@code mod-only} or {@code generic_name}
     * <br>Characters will automatically be lowercased by Discord for text channels!
     *
     * @param  name
     *         The new name for the selected {@link GuildChannel GuildChannel}
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 1-{@value Channel#MAX_NAME_LENGTH} characters long
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    M setName(@Nonnull String name);
}
