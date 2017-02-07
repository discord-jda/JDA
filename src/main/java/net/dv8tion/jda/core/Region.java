/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
    AMSTERDAM("amsterdam", "Amsterdam", false),
    BRAZIL("brazil", "Brazil", false),
    EU_WEST("eu-west", "EU West", false),
    EU_CENTRAL("eu-central", "EU Central", false),
    FRANKFURT("frankfurt", "Frankfurt", false),
    LONDON("london", "London", false),
    SINGAPORE("singapore", "Singapore", false),
    SYDNEY("sydney", "Sydney", false),
    US_EAST("us-east", "US East", false),
    US_WEST("us-west", "US West", false),
    US_CENTRAL("us-central", "US Central", false),
    US_SOUTH("us-south", "US South", false),

    VIP_AMSTERDAM("vip-amsterdam", "Amsterdam (VIP)", true),
    VIP_BRAZIL("vip-brazil", "Brazil (VIP)", true),
    VIP_EU_WEST("vip-eu-west", "EU West (VIP)", true),
    VIP_EU_CENTRAL("vip-eu-central", "EU Central (VIP)", true),
    VIP_FRANKFURT("vip-frankfurt", "Frankfurt (VIP)", true),
    VIP_LONDON("vip-london", "London (VIP)", true),
    VIP_SINGAPORE("vip-singapore", "Singapore (VIP)", true),
    VIP_SYDNEY("vip-sydney", "Sydney (VIP)", true),
    VIP_US_EAST("vip-us-east", "US East (VIP)", true),
    VIP_US_WEST("vip-us-west", "US West (VIP)", true),
    VIP_US_CENTRAL("vip-us-central", "US Central (VIP)", true),
    VIP_US_SOUTH("vip-us-south", "US South (VIP)", true),

    UNKNOWN("", "Unknown Region", false);

    private final String key;
    private final String name;
    private final boolean vip;

    Region(String key, String name, boolean vip)
    {
        this.key = key;
        this.name = name;
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
