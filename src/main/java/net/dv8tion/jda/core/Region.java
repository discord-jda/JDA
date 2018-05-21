/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.core;

/**
 * Represents the Regions used for Audio connections.
 * <br>This is used by {@link net.dv8tion.jda.core.entities.Guild Guild} to where the server that hosts the
 * {@link net.dv8tion.jda.core.entities.Guild Guild} is located.
 */
public enum Region
{
    JAPAN("japan", "Japan", "\uD83C\uDDEF\uD83C\uDDF5", false),
    AMSTERDAM("amsterdam", "Amsterdam", "\uD83C\uDDF3\uD83C\uDDF1", false),
    BRAZIL("brazil", "Brazil", "\uD83C\uDDE7\uD83C\uDDF7", false),
    EU_WEST("eu-west", "EU West", "\uD83C\uDDEA\uD83C\uDDFA", false),
    EU_CENTRAL("eu-central", "EU Central", "\uD83C\uDDEA\uD83C\uDDFA", false),
    FRANKFURT("frankfurt", "Frankfurt", "\uD83C\uDDE9\uD83C\uDDEA", false),
    HONG_KONG("hongkong", "Hong Kong", "\uD83C\uDDED\uD83C\uDDF0", false),
    LONDON("london", "London", "\uD83C\uDDEC\uD83C\uDDE7", false),
    RUSSIA("russia", "Russia", "\uD83C\uDDF7\uD83C\uDDFA", false),
    SINGAPORE("singapore", "Singapore", "\uD83C\uDDF8\uD83C\uDDEC", false),
    SYDNEY("sydney", "Sydney", "\uD83C\uDDE6\uD83C\uDDFA", false),
    US_EAST("us-east", "US East", "\uD83C\uDDFA\uD83C\uDDF8", false),
    US_WEST("us-west", "US West", "\uD83C\uDDFA\uD83C\uDDF8", false),
    US_CENTRAL("us-central", "US Central", "\uD83C\uDDFA\uD83C\uDDF8", false),
    US_SOUTH("us-south", "US South", "\uD83C\uDDFA\uD83C\uDDF8", false),

    VIP_JAPAN("vip-japan", "Japan (VIP)", "\uD83C\uDDEF\uD83C\uDDF5", true),
    VIP_AMSTERDAM("vip-amsterdam", "Amsterdam (VIP)", "\uD83C\uDDF3\uD83C\uDDF1", true),
    VIP_BRAZIL("vip-brazil", "Brazil (VIP)", "\uD83C\uDDE7\uD83C\uDDF7", true),
    VIP_EU_WEST("vip-eu-west", "EU West (VIP)", "\uD83C\uDDEA\uD83C\uDDFA", true),
    VIP_EU_CENTRAL("vip-eu-central", "EU Central (VIP)", "\uD83C\uDDEA\uD83C\uDDFA", true),
    VIP_FRANKFURT("vip-frankfurt", "Frankfurt (VIP)", "\uD83C\uDDE9\uD83C\uDDEA", true),
    VIP_LONDON("vip-london", "London (VIP)", "\uD83C\uDDEC\uD83C\uDDE7", true),
    VIP_SINGAPORE("vip-singapore", "Singapore (VIP)", "\uD83C\uDDF8\uD83C\uDDEC", true),
    VIP_SYDNEY("vip-sydney", "Sydney (VIP)", "\uD83C\uDDE6\uD83C\uDDFA", true),
    VIP_US_EAST("vip-us-east", "US East (VIP)", "\uD83C\uDDFA\uD83C\uDDF8", true),
    VIP_US_WEST("vip-us-west", "US West (VIP)", "\uD83C\uDDFA\uD83C\uDDF8", true),
    VIP_US_CENTRAL("vip-us-central", "US Central (VIP)", "\uD83C\uDDFA\uD83C\uDDF8", true),
    VIP_US_SOUTH("vip-us-south", "US South (VIP)", "\uD83C\uDDFA\uD83C\uDDF8", true),

    UNKNOWN("", "Unknown Region", null, false);

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
    public String getName()
    {
        return name;
    }

    /**
     * The Region key as defined by Discord.
     *
     * @return The key (internal name) of this region
     */
    public String getKey()
    {
        return key;
    }
    
    /**
     * The unicode flag representative of this Region.
     * 
     * @return Possibly-null unicode for the region's flag
     */
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
     * Retrieves the {@link net.dv8tion.jda.core.Region Region} based on the provided key.
     *
     * @param  key
     *         The key relating to the {@link net.dv8tion.jda.core.Region Region} we wish to retrieve.
     *
     * @return The {@link net.dv8tion.jda.core.Region Region} matching the key. If there is no match,
     *         returns {@link net.dv8tion.jda.core.Region#UNKNOWN UNKNOWN}.
     */
    public static Region fromKey(String key)
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
