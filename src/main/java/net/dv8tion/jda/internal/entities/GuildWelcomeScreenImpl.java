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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildWelcomeScreen;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.managers.GuildWelcomeScreenManager;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class GuildWelcomeScreenImpl implements GuildWelcomeScreen
{
    private final Guild guild;
    private final String description;
    private final List<Channel> channels;

    public GuildWelcomeScreenImpl(@Nullable Guild guild, @Nullable String description, @Nonnull List<Channel> channels)
    {
        this.guild = guild;
        this.description = description;
        this.channels = channels;
    }

    @Nullable
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Nonnull
    @Override
    public GuildWelcomeScreenManager getManager()
    {
        if (guild == null)
            throw new IllegalStateException("Cannot modify a guild welcome screen from an Invite");
        return guild.modifyWelcomeScreen();
    }

    @Nullable
    @Override
    public String getDescription()
    {
        return description;
    }

    @Nonnull
    @Override
    public List<Channel> getChannels()
    {
        return channels;
    }

    /**
     * POJO for the recommended channels information provided by a welcome screen.
     * <br>Recommended channels are shown in the welcome screen after joining a server.
     *
     * @see GuildWelcomeScreen#getChannels()
     */
    public static class ChannelImpl implements GuildWelcomeScreen.Channel
    {
        private final Guild guild;
        private final long id;
        private final String description;
        private final EmojiUnion emoji;

        public ChannelImpl(@Nullable Guild guild, long id, @Nonnull String description, @Nullable EmojiUnion emoji)
        {
            this.guild = guild;
            this.id = id;
            this.description = description;
            this.emoji = emoji;
        }

        @Nullable
        @Override
        public Guild getGuild()
        {
            return guild;
        }

        @Override
        public long getIdLong()
        {
            return id;
        }

        @Nullable
        @Override
        public GuildChannel getChannel()
        {
            if (guild == null)
                return null;

            return guild.getGuildChannelById(id);
        }

        @Nonnull
        @Override
        public String getDescription()
        {
            return description;
        }

        @Nullable
        @Override
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
