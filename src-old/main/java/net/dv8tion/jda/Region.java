/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda;

/**
 * Represents the Regions that Discord has servers in.<br>
 * This is used by {@link net.dv8tion.jda.entities.Guild Guild} to where the server that hosts the
 * {@link net.dv8tion.jda.entities.Guild Guild} is located.
 */
public enum Region
{
    AMSTERDAM("amsterdam", "Amsterdam"),
    BRAZIL("brazil", "Brazil"),
    EU_WEST("eu-west", "EU West"),
    EU_CENTRAL("eu-central", "EU Central"),
    FRANKFURT("frankfurt", "Frankfurt"),
    LONDON("london", "London"),
    SINGAPORE("singapore", "Singapore"),
    SYDNEY("sydney", "Sydney"),
    US_EAST("us-east", "US East"),
    US_WEST("us-west", "US West"),
    US_CENTRAL("us-central", "US Central"),
    US_SOUTH("us-south", "US South"),
        
    VIP_AMSTERDAM("vip-amsterdam", "Amsterdam (VIP)"),
    VIP_BRAZIL("vip-brazil", "Brazil (VIP)"),
    VIP_EU_WEST("vip-eu-west", "EU West (VIP)"),
    VIP_EU_CENTRAL("vip-eu-central", "EU Central (VIP)"),
    VIP_FRANKFURT("vip-frankfurt", "Frankfurt (VIP)"),
    VIP_LONDON("vip-london", "London (VIP)"),
    VIP_SINGAPORE("vip-singapore", "Singapore (VIP)"),
    VIP_SYDNEY("vip-sydney", "Sydney (VIP)"),
    VIP_US_EAST("vip-us-east", "US East (VIP)"),
    VIP_US_WEST("vip-us-west", "US West (VIP)"),
    VIP_US_CENTRAL("vip-us-central", "US Central (VIP)"),
    VIP_US_SOUTH("vip-us-south", "US South (VIP)"),
    UNKNOWN("", "Unknown Region");

    private final String key;
    private final String name;

    Region(String key, String name)
    {
        this.key = key;
        this.name = name;
    }

    /**
     * The human readable region name.
     * @return
     *      The name of this region
     */
    public String getName()
    {
        return name;
    }

    /**
     * The Region key as defined by Discord.
     * @return
     *      The key (internal name) of this region
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Retrieves the {@link net.dv8tion.jda.Region Region} based on the provided key.
     *
     * @param key
     *          The key relating to the {@link net.dv8tion.jda.Region Region} we wish to retrieve.
     * @return
     *          The {@link net.dv8tion.jda.Region Region} matching the key. If there is no match, returns {@link net.dv8tion.jda.Region#UNKNOWN UNKNOWN}.
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
