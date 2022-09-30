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
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
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
public class GuildWelcomeScreen
{
    public static final int MAX_DESCRIPTION_LENGTH = 140;

    private final Guild guild;
    private final String description;
    private final List<GuildWelcomeScreen.Channel> channels;

    public GuildWelcomeScreen(Guild guild, String description, List<GuildWelcomeScreen.Channel> channels)
    {
        this.guild = guild;
        this.description = description;
        this.channels = channels;
    }

    /**
     * The {@link Guild Guild}
     *
     * @return The Guild
     */
    @Nullable
    public Guild getGuild()
    {
        return guild;
    }

    /**
     * The server description that is shown in the Welcome screen.
     * <br>This will be {@code null} if the welcome screen has no description.
     *
     * @return The server description that is shown in the Welcome screen or {@code null}
     */
    @Nullable
    public String getDescription()
    {
        return description;
    }

    /**
     * The channels that are shown in the Welcome screen.
     *
     * @return Possibly-empty, unmodifiable list of the channels that are shown in the Welcome screen
     */
    @Nonnull
    public List<GuildWelcomeScreen.Channel> getChannels()
    {
        return channels;
    }

    /**
     * POJO for the recommended channels information provided by a Welcome screen.
     * <br>Recommended channels are shown in the Welcome screen after joining a server.
     *
     * @see GuildWelcomeScreen#getChannels()
     */
    public static class Channel implements ISnowflake, SerializableData
    {
        public static final int MAX_DESCRIPTION_LENGTH = 42;

        private final Guild guild;
        private final long id;
        private final String description;
        private final EmojiUnion emoji;

        public Channel(Guild guild, long id, String description, EmojiUnion emoji)
        {
            this.guild = guild;
            this.id = id;
            this.description = description;
            this.emoji = emoji;
        }

        /**
         * Constructs a new welcome channel.
         *
         * @param  channel
         *         The Discord channel to be presented the user
         * @param  description
         *         The description of the channel, must not be longer than {@value MAX_DESCRIPTION_LENGTH}
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If the channel is null</li>
         *             <li>If the description is null, blank, or longer than {@value MAX_DESCRIPTION_LENGTH}</li>
         *         </ul>
         *
         * @return The new welcome channel
         */
        @Nonnull
        public static Channel of(@Nonnull StandardGuildChannel channel, @Nonnull String description)
        {
            return of(channel, description, null);
        }

        /**
         * Constructs a new welcome channel.
         *
         * @param  channel
         *         The Discord channel to be presented the user
         * @param  description
         *         The description of the channel, must not be longer than {@value MAX_DESCRIPTION_LENGTH}
         * @param  emoji
         *         The emoji to show beside the channel
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If the channel is null</li>
         *             <li>If the description is null, blank, or longer than {@value MAX_DESCRIPTION_LENGTH}</li>
         *         </ul>
         *
         * @return The new welcome channel
         */
        @Nonnull
        public static Channel of(@Nonnull StandardGuildChannel channel, @Nonnull String description, @Nullable Emoji emoji)
        {
            Checks.notBlank(description, "Description");
            Checks.notLonger(description, MAX_DESCRIPTION_LENGTH, "Description");

            return new Channel(channel.getGuild(), channel.getIdLong(), description, (EmojiUnion) emoji);
        }

        /**
         * The {@link Guild Guild}, or {@code null} if this welcome screen came from an {@link net.dv8tion.jda.api.entities.Invite}
         *
         * @return The Guild
         */
        @Nullable
        public Guild getGuild()
        {
            return guild;
        }

        /**
         * The id of this recommended channel.
         *
         * @return The id of this recommended channel
         */
        @Override
        public long getIdLong()
        {
            return id;
        }

        /**
         * Returns the {@link GuildChannel} that is linked to this recommended channel.
         * <br>This will be {@code null} if the linked channel was deleted, or if the welcome screen comes from an {@link Invite.Guild invite guild}.
         *
         * @return The {@link GuildChannel} that is linked to this recommended channel or {@code null}
         */
        @Nullable
        public GuildChannel getChannel()
        {
            if (guild == null)
                return null;

            return guild.getGuildChannelById(id);
        }

        /**
         * The description of this recommended channel that is shown in the Welcome screen.
         *
         * @return The description of this recommended channel
         */
        @Nonnull
        public String getDescription()
        {
            return description;
        }

        /**
         * The emoji that is used for this recommended channel.
         * <br><b>This will return {@code null} if no emoji was set</b>
         *
         * <p>The emoji will always be from this guild, if not a unicode emoji
         * <br><b>{@link CustomEmoji#isAnimated()} will always return {@code false} if:</b>
         * <ul>
         *     <li>This welcome screen came from an {@link net.dv8tion.jda.api.entities.Invite.Guild invite's guild}</li>
         *     <li>{@link net.dv8tion.jda.api.utils.cache.CacheFlag#EMOJI CacheFlag.EMOJI} is disabled</li>
         * </ul>
         *
         * @return The emoji that is used for this recommended channel or {@code null}
         */
        @Nullable
        public EmojiUnion getEmoji()
        {
            return emoji;
        }

        @Nonnull
        @Override
        public DataObject toData()
        {
            final DataObject data = DataObject.empty();
            data.put("channel_id", id);
            data.put("description", description);
            if (emoji != null)
            {
                if (emoji.getType() == Emoji.Type.CUSTOM)
                    data.put("emoji_id", ((CustomEmoji) emoji).getId());
                data.put("emoji_name", emoji.getName());
            }

            return data;
        }
    }
}
