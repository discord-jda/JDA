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
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * The welcome screen of a {@link net.dv8tion.jda.api.entities.Guild}.
 * This welcome screen will be shown to all members after joining the Guild.
 *
 * @see Guild#retrieveWelcomeScreen()
 * @see Invite.Guild#getWelcomeScreen()
 */
public class GuildWelcomeScreen
{
    private final Guild guild;
    private final String description;
    private final List<GuildWelcomeScreen.Channel> channels;

    public GuildWelcomeScreen(Guild guild, String description, List<GuildWelcomeScreen.Channel> channels)
    {
        this.guild = guild;
        this.description = description;
        this.channels = channels;
    }

    //TODO docs
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
    public static class Channel implements ISnowflake
    {
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

        //TODO docs
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
         * <br>This will be {@code null} if the linked channel was deleted.
         *
         * TODO add throw
         *
         * @return The {@link GuildChannel} that is linked to this recommended channel or {@code null}
         */
        @Nullable
        public GuildChannel getChannel()
        {
            if (guild == null)
                throw new IllegalStateException("Cannot retrieve channel from invites");

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
         * TODO update docs, "animated" is always false
         * The id of the emote that is used for this recommended channel.
         * <br><b>This will return {@code null} if the emoji is a unicode emoji.</b>
         *
         * @return The id of the emote that is used for this recommended channel or {@code null}
         */
        @Nullable
        public EmojiUnion getEmoji()
        {
            return emoji;
        }
    }
}