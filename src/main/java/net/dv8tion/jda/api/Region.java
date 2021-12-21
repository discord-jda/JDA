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
package net.dv8tion.jda.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the Regions used for Audio connections.
 * <br>This is used by {@link net.dv8tion.jda.api.entities.AudioChannel AudioChannels} to define where the audio server that hosts the
 * {@link net.dv8tion.jda.api.entities.AudioChannel} is located.
 */
public enum Region
{
    BRAZIL("brazil", "Brazil", "\uD83C\uDDE7\uD83C\uDDF7", false),
    HONG_KONG("hongkong", "Hong Kong", "\uD83C\uDDED\uD83C\uDDF0", false),
    INDIA("india", "India", "\uD83C\uDDEE\uD83C\uDDF3", false),
    JAPAN("japan", "Japan", "\uD83C\uDDEF\uD83C\uDDF5", false),
    MILAN("milan", "Milan", "\uD83C\uDDEE\uD83C\uDDF9", false),
    ROTTERDAM("rotterdam", "Rotterdam", "\uD83C\uDDF3\uD83C\uDDF1", false),
    RUSSIA("russia", "Russia", "\uD83C\uDDF7\uD83C\uDDFA", false),
    SINGAPORE("singapore", "Singapore", "\uD83C\uDDF8\uD83C\uDDEC", false),
    SOUTH_AFRICA("southafrica", "South Africa", "\uD83C\uDDFF\uD83C\uDDE6", false),
    SOUTH_KOREA("south-korea", "South Korea", "\uD83C\uDDF0\uD83C\uDDF7", false),
    SYDNEY("sydney", "Sydney", "\uD83C\uDDE6\uD83C\uDDFA", false),
    US_CENTRAL("us-central", "US Central", "\uD83C\uDDFA\uD83C\uDDF8", false),
    US_EAST("us-east", "US East", "\uD83C\uDDFA\uD83C\uDDF8", false),
    US_SOUTH("us-south", "US South", "\uD83C\uDDFA\uD83C\uDDF8", false),
    US_WEST("us-west", "US West", "\uD83C\uDDFA\uD83C\uDDF8", false),

    VIP_BRAZIL("vip-brazil", "Brazil (VIP)", "\uD83C\uDDE7\uD83C\uDDF7", true),
    VIP_HONG_KONG("vip-hongkong", "Hong Kong (VIP)", "\uD83C\uDDED\uD83C\uDDF0", true),
    VIP_INDIA("vip-india", "India (VIP)", "\uD83C\uDDEE\uD83C\uDDF3", true),
    VIP_JAPAN("vip-japan", "Japan (VIP)", "\uD83C\uDDEF\uD83C\uDDF5", true),
    VIP_MILAN("vip-milan", "Milan (VIP)", "\uD83C\uDDEE\uD83C\uDDF9", true),
    VIP_ROTTERDAM("vip-rotterdam", "Rotterdam (VIP)", "\uD83C\uDDF3\uD83C\uDDF1", true),
    VIP_RUSSIA("vip-russia", "Russia (VIP)", "\uD83C\uDDF7\uD83C\uDDFA", true),
    VIP_SINGAPORE("vip-singapore", "Singapore (VIP)", "\uD83C\uDDF8\uD83C\uDDEC", true),
    VIP_SOUTH_AFRICA("vip-southafrica", "South Africa (VIP)", "\uD83C\uDDFF\uD83C\uDDE6", true),
    VIP_SOUTH_KOREA("vip-south-korea", "South Korea (VIP)", "\uD83C\uDDF0\uD83C\uDDF7", true),
    VIP_SYDNEY("vip-sydney", "Sydney (VIP)", "\uD83C\uDDE6\uD83C\uDDFA", true),
    VIP_US_CENTRAL("vip-us-central", "US Central (VIP)", "\uD83C\uDDFA\uD83C\uDDF8", true),
    VIP_US_EAST("vip-us-east", "US East (VIP)", "\uD83C\uDDFA\uD83C\uDDF8", true),
    VIP_US_SOUTH("vip-us-south", "US South (VIP)", "\uD83C\uDDFA\uD83C\uDDF8", true),
    VIP_US_WEST("vip-us-west", "US West (VIP)", "\uD83C\uDDFA\uD83C\uDDF8", true),

    UNKNOWN("", "Unknown Region", null, false),

    AUTOMATIC("automatic", "Automatic", null, false);

    private final String key;
    private final String name;
    private final String emoji;
    private final boolean vip;

    Region(String key, String name, String emoji, boolean vip)
    {
        this.key = key;
        this.name = name;
        this.emoji = emoji;
        this.vip = vip;
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
    @Nullable
    public String getEmoji()
    {
        return emoji;
    }

    /**
     * Whether or not this Region is a VIP region.
     * <br>VIP regions have special perks like higher bitrate in VoiceChannels and priority during times
     * of high Discord usage.
     *
     * @return True if this region is a VIP audio region.
     */
    public boolean isVip()
    {
        return vip;
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
