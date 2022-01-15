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

import net.dv8tion.jda.api.JDA;

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
    private final String description;
    private final List<GuildWelcomeScreen.Channel> channels;

    public GuildWelcomeScreen(String description, List<GuildWelcomeScreen.Channel> channels)
    {
        this.description = description;
        this.channels = channels;
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
    public static class Channel implements ISnowflake {
        private final JDA api;
        private final long id;
        private final String description;
        private final String emoteId;
        private final String emojiName;

        public Channel(JDA api, long id, String description, String emoteId, String emojiName)
        {
            this.api = api;
            this.id = id;
            this.description = description;
            this.emoteId = emoteId;
            this.emojiName = emojiName;
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
         * Returns the {@link net.dv8tion.jda.api.entities.BaseGuildMessageChannel} that is linked to this recommended channel.
         * <br>This will be {@code null} if the linked channel was deleted.
         *
         * @return The {@link net.dv8tion.jda.api.entities.BaseGuildMessageChannel} that is linked to this recommended channel or {@code null}
         */
        @Nullable
        public BaseGuildMessageChannel getChannel()
        {
            return (BaseGuildMessageChannel) api.getGuildChannelById(id);
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
         * The id of the emote that is used for this recommended channel.
         * <br><b>This will return {@code null} if the emoji is a unicode emoji.</b>
         *
         * @return The id of the emote that is used for this recommended channel or {@code null}
         */
        @Nullable
        public String getEmoteId()
        {
            return emoteId;
        }

        /**
         * The name of the emoji that is used for this recommended channel.
         * <br>This will return the emote name if {@link #getEmoteId()} is not null, otherwise the unicode emoji or {@code null}
         * is returned if no emoji was set.
         *
         * @return The name of the emoji that is used for this recommended channel or {@code null}
         */
        @Nullable
        public String getEmojiName()
        {
            return emojiName;
        }
    }
}
