/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
package net.dv8tion.jda.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the Regions used for Audio connections.
 * <br>This is used by {@link net.dv8tion.jda.api.entities.Guild Guild} to where the server that hosts the
 * {@link net.dv8tion.jda.api.entities.Guild Guild} is located.
 */
public enum Region
{
    AMSTERDAM("amsterdam", "Amsterdam", "\uD83C\uDDF3\uD83C\uDDF1"),
    BRAZIL("brazil", "Brazil", "\uD83C\uDDE7\uD83C\uDDF7"),
    EUROPE("europe", "Europe", "\uD83C\uDDEA\uD83C\uDDFA"),
    EU_CENTRAL("eu-central", "EU Central", "\uD83C\uDDEA\uD83C\uDDFA"),
    EU_WEST("eu-west", "EU West", "\uD83C\uDDEA\uD83C\uDDFA"),
    FRANKFURT("frankfurt", "Frankfurt", "\uD83C\uDDE9\uD83C\uDDEA"),
    HONG_KONG("hongkong", "Hong Kong", "\uD83C\uDDED\uD83C\uDDF0"),
    JAPAN("japan", "Japan", "\uD83C\uDDEF\uD83C\uDDF5"),
    LONDON("london", "London", "\uD83C\uDDEC\uD83C\uDDE7"),
    RUSSIA("russia", "Russia", "\uD83C\uDDF7\uD83C\uDDFA"),
    INDIA("india", "India", "\uD83C\uDDEE\uD83C\uDDF3"),
    SINGAPORE("singapore", "Singapore", "\uD83C\uDDF8\uD83C\uDDEC"),
    SOUTH_AFRICA("southafrica", "South Africa", "\uD83C\uDDFF\uD83C\uDDE6"),
    SYDNEY("sydney", "Sydney", "\uD83C\uDDE6\uD83C\uDDFA"),
    US_CENTRAL("us-central", "US Central", "\uD83C\uDDFA\uD83C\uDDF8"),
    US_EAST("us-east", "US East", "\uD83C\uDDFA\uD83C\uDDF8"),
    US_SOUTH("us-south", "US South", "\uD83C\uDDFA\uD83C\uDDF8"),
    US_WEST("us-west", "US West", "\uD83C\uDDFA\uD83C\uDDF8"),

    UNKNOWN("", "Unknown Region", null);

    private final String key;
    private final String name;
    private final String emoji;

    Region(String key, String name, String emoji)
    {
        this.key = key;
        this.name = name;
        this.emoji = emoji;
    }

    /**
     * The human readable region name.
     *
     * @return The name of this region
     */
    @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     * The Region key as defined by Discord.
     *
     * @return The key (internal name) of this region
     */
    @Nonnull
    public String getKey()
    {
        return key;
    }
    
    /**
     * The unicode flag representative of this Region.
     * 
     * @return Possibly-null unicode for the region's flag
     */
    @Nonnull
    public String getEmoji()
    {
        return emoji;
    }

    /**
     * Retrieves the {@link net.dv8tion.jda.api.Region Region} based on the provided key.
     *
     * @param  key
     *         The key relating to the {@link net.dv8tion.jda.api.Region Region} we wish to retrieve.
     *
     * @return The {@link net.dv8tion.jda.api.Region Region} matching the key. If there is no match,
     *         returns {@link net.dv8tion.jda.api.Region#UNKNOWN UNKNOWN}.
     */
    @Nonnull
    public static Region fromKey(@Nullable String key)
    {
        for (Region region : values())
        {
            if (region.getKey().equals(key))
            {
                return region;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString()
    {
        return getName();
    }
}
