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

import javax.annotation.Nonnull;
import java.util.EnumSet;

/**
 * Enum used to differentiate between the different types of Discord channels.
 */
public enum ChannelType
{
    /**
     * A {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}, Guild-Only.
     */
    TEXT(0, 0, true),
    /**
     * A {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}.
     */
    PRIVATE(1, -1),
    /**
     * A {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}, Guild-Only.
     */
    VOICE(2, 1, true),
    /**
     * A Group. (unused)
     */
    GROUP(3, -1),
    /**
     * A {@link net.dv8tion.jda.api.entities.Category Category}, Guild-Only.
     */
    CATEGORY(4, 2, true),
    /**
     * A {@link net.dv8tion.jda.api.entities.NewsChannel NewsChannel}, Guild-Only.
     */
    NEWS(5, 0, true),
    /**
     * A {@link net.dv8tion.jda.api.entities.StoreChannel StoreChannel}, Guild-Only.
     */
    STORE(6, 0, true),
    /**
     * A {@link StageChannel StageChannel}, Guild-Only.
     */
    STAGE(13, 1, true),

    GUILD_NEWS_THREAD(10, -1, true),
    GUILD_PUBLIC_THREAD(11, -1, true),
    GUILD_PRIVATE_THREAD(12, -1, true),

    /**
     * Unknown Discord channel type. Should never happen and would only possibly happen if Discord implemented a new
     * channel type and JDA had yet to implement support for it.
     */
    UNKNOWN(-1, -2);

    private final int sortBucket;
    private final int id;
    private final boolean isGuild;

    ChannelType(int id, int sortBucket)
    {
        this(id, sortBucket, false);
    }

    ChannelType(int id, int sortBucket, boolean isGuild)
    {
        this.id = id;
        this.sortBucket = sortBucket;
        this.isGuild = isGuild;
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
     * Whether this ChannelType is present for a {@link GuildChannel GuildChannel}
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
            case NEWS:
            case PRIVATE:
            case GROUP:
                return true;
            default:
                return false;
        }
    }

    /**
     * Whether channels of this type are {@link ThreadChannel ThreadChannels}.
     * This mostly exists to make handling threads simpler than having to check 3 separate ChannelTypes every time.
     *
     * @return True, if channels of this type are {@link ThreadChannel ThreadChannel}
     */
    public boolean isThread() {
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
