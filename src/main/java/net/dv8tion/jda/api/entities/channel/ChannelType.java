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
package net.dv8tion.jda.api.entities.channel;

import net.dv8tion.jda.api.entities.channel.concrete.*;

import javax.annotation.Nonnull;
import java.util.EnumSet;

/**
 * Enum used to differentiate between the different types of Discord channels.
 */
public enum ChannelType
{
    /**
     * A {@link TextChannel TextChannel}, Guild-Only.
     */
    TEXT(TextChannel.class, 0, 0, true),
    /**
     * A {@link PrivateChannel PrivateChannel}.
     */
    PRIVATE(PrivateChannel.class, 1, -1),
    /**
     * A {@link VoiceChannel VoiceChannel}, Guild-Only.
     */
    VOICE(VoiceChannel.class, 2, 1, true),
    /**
     * A {@link GroupChannel GroupChannel}, used only in user apps.
     */
    GROUP(GroupChannel.class, 3, -1),
    /**
     * A {@link Category Category}, Guild-Only.
     */
    CATEGORY(Category.class, 4, 2, true),
    /**
     * A {@link NewsChannel NewsChannel}, Guild-Only.
     */
    NEWS(NewsChannel.class, 5, 0, true),
    /**
     * A {@link StageChannel StageChannel}, Guild-Only.
     */
    STAGE(StageChannel.class, 13, 1, true),

    GUILD_NEWS_THREAD(ThreadChannel.class, 10, -1, true),
    GUILD_PUBLIC_THREAD(ThreadChannel.class, 11, -1, true),
    GUILD_PRIVATE_THREAD(ThreadChannel.class, 12, -1, true),

    /**
     * A {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel ForumChannel}, Guild-Only.
     */
    FORUM(ForumChannel.class, 15, 0, true),

    /**
     * A {@link MediaChannel}, Guild-Only.
     */
    MEDIA(MediaChannel.class, 16, 0, true),

    /**
     * Unknown Discord channel type.
     *
     * <p>This might be used in the case when a channel is not available in cache, like when sending webhook messages.
     */
    UNKNOWN(Channel.class, -1, -2);

    private final int sortBucket;
    private final int id;
    private final boolean isGuild;
    private final Class<? extends Channel> clazz;

    ChannelType(Class<? extends Channel> clazz, int id, int sortBucket)
    {
        this(clazz, id, sortBucket, false);
    }

    ChannelType(Class<? extends Channel> clazz, int id, int sortBucket, boolean isGuild)
    {
        this.clazz = clazz;
        this.id = id;
        this.sortBucket = sortBucket;
        this.isGuild = isGuild;
    }

    /**
     * The interface this channel type corresponds to.
     *
     * @return This channel type's interface
     */
    @Nonnull
    public Class<? extends Channel> getInterface()
    {
        return this.clazz;
    }

    /**
     * The sorting bucket for this channel type.
     *
     * @return The sorting bucket
     */
    public int getSortBucket()
    {
        return sortBucket;
    }

    /**
     * The Discord id key used to represent the channel type.
     *
     * @return The id key used by discord for this channel type.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Whether this ChannelType is present for a {@link net.dv8tion.jda.api.entities.channel.middleman.GuildChannel GuildChannel}
     *
     * @return Whether or not this a GuildChannel
     */
    public boolean isGuild()
    {
        return isGuild;
    }

    /**
     * Whether channels of this type support audio connections.
     *
     * @return True, if channels of this type support audio
     */
    public boolean isAudio()
    {
        switch (this)
        {
            case VOICE:
            case STAGE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Whether channels of this type support message sending.
     *
     * @return True, if channels of this type support messages
     */
    public boolean isMessage()
    {
        switch (this)
        {
            case TEXT:
            case VOICE:
            case STAGE:
            case NEWS:
            case PRIVATE:
            case GROUP:
                return true;
            default:
                return isThread();
        }
    }

    /**
     * Whether channels of this type are {@link ThreadChannel ThreadChannels}.
     * This mostly exists to make handling threads simpler than having to check 3 separate ChannelTypes every time.
     *
     * @return True, if channels of this type are {@link ThreadChannel ThreadChannel}
     */
    public boolean isThread()
    {
        switch (this)
        {
            case GUILD_NEWS_THREAD:
            case GUILD_PUBLIC_THREAD:
            case GUILD_PRIVATE_THREAD:
                return true;
            default:
                return false;
        }
    }

    /**
     * All the channel types for a {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @return {@link EnumSet} of {@link ChannelType}
     */
    @Nonnull
    public static EnumSet<ChannelType> guildTypes()
    {
        return EnumSet.complementOf(EnumSet.of(PRIVATE, GROUP, UNKNOWN));
    }

    /**
     * Static accessor for retrieving a channel type based on its Discord id key.
     *
     * @param  id
     *         The id key of the requested channel type.
     *
     * @return The ChannelType that is referred to by the provided key. If the id key is unknown, {@link #UNKNOWN} is returned.
     */
    @Nonnull
    public static ChannelType fromId(int id)
    {
        for (ChannelType type : values())
        {
            if (type.id == id)
                return type;
        }
        return UNKNOWN;
    }

    /**
     * An {@link java.util.EnumSet} populated with all channel types using the provided sorting bucket.
     *
     * @param  bucket
     *         The sorting bucket
     *
     * @return Possibly-empty {@link java.util.EnumSet} for the bucket
     */
    @Nonnull
    public static EnumSet<ChannelType> fromSortBucket(int bucket)
    {
        EnumSet<ChannelType> types = EnumSet.noneOf(ChannelType.class);
        for (ChannelType type : values())
        {
            if (type.getSortBucket() == bucket)
                types.add(type);
        }
        return types;
    }
}
