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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.managers.GuildWelcomeScreenManager;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.entities.GuildWelcomeScreenImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * The welcome screen of a {@link Guild}.
 * This welcome screen will be shown to all members after joining the Guild.
 *
 * @see Guild#retrieveWelcomeScreen()
 * @see Invite.Guild#getWelcomeScreen()
 */
public interface GuildWelcomeScreen
{
    /** The maximum length of a welcome screen description ({@value}) */
    int MAX_DESCRIPTION_LENGTH = 140;

    /** The maximum amount of welcome channel a welcome screen can show ({@value}) */
    int MAX_WELCOME_CHANNELS = 5;

    /**
     * The {@link Guild Guild}, or {@code null} if this welcome screen came from an {@link Invite}
     *
     * @return The Guild, or {@code null}
     */
    @Nullable
    Guild getGuild();

    /**
     * Returns the {@link GuildWelcomeScreenManager Manager} for this guild's welcome screen.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER Permission.MANAGE_SERVER}
     * @throws IllegalStateException
     *         If welcome screen came from an {@link Invite}
     *
     * @return The GuildWelcomeScreenManager for this guild's welcome screen
     *
     * @see Guild#modifyWelcomeScreen()
     */
    @Nonnull
    GuildWelcomeScreenManager getManager();

    /**
     * The server description shown in the welcome screen.
     * <br>This will be {@code null} if the welcome screen has no description.
     *
     * @return The server description shown in the welcome screen or {@code null}
     */
    @Nullable
    String getDescription();

    /**
     * The channels shown in the welcome screen.
     *
     * @return Possibly-empty, unmodifiable list of the channels shown in the welcome screen
     */
    @Nonnull
    List<Channel> getChannels();

    /**
     * POJO for the recommended channels information provided by a welcome screen.
     * <br>Recommended channels are shown in the welcome screen after joining a server.
     *
     * @see GuildWelcomeScreen#getChannels()
     */
    interface Channel extends ISnowflake, SerializableData
    {
        /** Maximum length of a channel description ({@value}) */
        int MAX_DESCRIPTION_LENGTH = 42;

        /**
         * Constructs a new welcome channel.
         *
         * @param  channel
         *         The Discord channel to be presented to the user
         * @param  description
         *         The description of the channel, must not be longer than {@value #MAX_DESCRIPTION_LENGTH}
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If the channel is null</li>
         *             <li>If the description is null, blank, or longer than {@value #MAX_DESCRIPTION_LENGTH}</li>
         *         </ul>
         *
         * @return The new welcome channel
         */
        @Nonnull
        static Channel of(@Nonnull StandardGuildChannel channel, @Nonnull String description)
        {
            return of(channel, description, null);
        }

        /**
         * Constructs a new welcome channel.
         *
         * @param  channel
         *         The Discord channel to be presented the user
         * @param  description
         *         The description of the channel, must not be longer than {@value #MAX_DESCRIPTION_LENGTH}
         * @param  emoji
         *         The emoji to show beside the channel
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If the channel is null</li>
         *             <li>If the description is null, blank, or longer than {@value #MAX_DESCRIPTION_LENGTH}</li>
         *         </ul>
         *
         * @return The new welcome channel
         */
        @Nonnull
        static Channel of(@Nonnull StandardGuildChannel channel, @Nonnull String description, @Nullable Emoji emoji)
        {
            Checks.notNull(channel, "Channel");
            Checks.notBlank(description, "Description");
            Checks.notLonger(description, MAX_DESCRIPTION_LENGTH, "Description");

            return new GuildWelcomeScreenImpl.ChannelImpl(channel.getGuild(), channel.getIdLong(), description, (EmojiUnion) emoji);
        }

        /**
         * The {@link Guild Guild}, or {@code null} if this welcome channel came from an {@link Invite}
         *
         * @return The Guild, or {@code null}
         */
        @Nullable
        Guild getGuild();

        /**
         * The id of this recommended channel.
         *
         * @return The id of this recommended channel
         */
        @Override
        long getIdLong();

        /**
         * Returns the {@link GuildChannel} that is linked to this recommended channel.
         * <br>This will be {@code null} if the linked channel was deleted, or if the welcome screen comes from an {@link Invite.Guild invite guild}.
         *
         * @return The {@link GuildChannel} that is linked to this recommended channel or {@code null}
         */
        @Nullable
        GuildChannel getChannel();

        /**
         * The description of this recommended channel shown in the welcome screen.
         *
         * @return The description of this recommended channel
         */
        @Nonnull
        String getDescription();

        /**
         * The emoji that is used for this recommended channel.
         * <br>This will return {@code null} if no emoji was set
         *
         * <p>The emoji will always be from this guild, if not a unicode emoji
         * <br><b>{@link CustomEmoji#isAnimated()} will always return {@code false} if:</b>
         * <ul>
         *     <li>This welcome screen came from an {@link Invite.Guild invite's guild}</li>
         *     <li>{@link net.dv8tion.jda.api.utils.cache.CacheFlag#EMOJI CacheFlag.EMOJI} is disabled</li>
         * </ul>
         *
         * @return The emoji that is used for this recommended channel or {@code null}
         */
        @Nullable
        EmojiUnion getEmoji();
    }
}
