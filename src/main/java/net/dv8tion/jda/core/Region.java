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
    AMSTERDAM("amsterdam", "Amsterdam", "NL", false),
    BRAZIL("brazil", "Brazil", "BR", false),
    EU_WEST("eu-west", "EU West", null, false),
    EU_CENTRAL("eu-central", "EU Central", null, false),
    FRANKFURT("frankfurt", "Frankfurt", "DE", false),
    HONG_KONG("hongkong", "Hong Kong", "HK", false),
    LONDON("london", "London", "GB", false),
    RUSSIA("russia", "Russia", "RU", false),
    SINGAPORE("singapore", "Singapore", "SG", false),
    SYDNEY("sydney", "Sydney", "AU", false),
    JAPAN("japan", "Japan", "JP", false),
    US_EAST("us-east", "US East", "US", false),
    US_WEST("us-west", "US West", "US", false),
    US_CENTRAL("us-central", "US Central", "US", false),
    US_SOUTH("us-south", "US South", "US", false),

    VIP_AMSTERDAM("vip-amsterdam", "Amsterdam (VIP)", "NL", true),
    VIP_BRAZIL("vip-brazil", "Brazil (VIP)", "BR", true),
    VIP_EU_WEST("vip-eu-west", "EU West (VIP)", null, true),
    VIP_EU_CENTRAL("vip-eu-central", "EU Central (VIP)", null, true),
    VIP_FRANKFURT("vip-frankfurt", "Frankfurt (VIP)", "DE", true),
    VIP_HONG_KONG("vip-hongkong", "Hong Kong (VIP)", "HK", true),
    VIP_LONDON("vip-london", "London (VIP)", "GB", true),
    VIP_RUSSIA("vip-russia", "Russia (VIP)", "RU", true),
    VIP_SINGAPORE("vip-singapore", "Singapore (VIP)", "SG", true),
    VIP_SYDNEY("vip-sydney", "Sydney (VIP)", "AU", true),
    VIP_JAPAN("vip-japan", "Japan (VIP)", "JP", true),
    VIP_US_EAST("vip-us-east", "US East (VIP)", "US", true),
    VIP_US_WEST("vip-us-west", "US West (VIP)", "US", true),
    VIP_US_CENTRAL("vip-us-central", "US Central (VIP)", "US", true),
    VIP_US_SOUTH("vip-us-south", "US South (VIP)", "US", true),

    UNKNOWN("", "Unknown Region", null, false);

    private final String key;
    private final String name;
    private final String country;
    private final boolean vip;

    Region(String key, String name, String country, boolean vip)
    {
        this.key = key;
        this.name = name;
        this.country = country;
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
    * The ISO 3166-1 alpha-2 code for the country (if any) of this region
    *
    * @return The ISO 2 code for the country (if any) of this region 
    */
    public String getCountry()
    {
        return country;
    }
    
    /**
    * Whether or not this Region is a country or in a country
    *
    * @return True if this region is a or in a country.
    */
    public boolean isCountry() 
    {
        return country != null;
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
