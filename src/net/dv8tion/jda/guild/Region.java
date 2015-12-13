package net.dv8tion.jda.guild;

/**
 * Represents the Regions that Discord has servers in.
 * This is set in a Guild, however the information is provided both when dealing with
 *   Guilds and Voice Channels.
 */
public enum Region
{
    AMSTERDAM("amsterdam", "Amsterdam"),
    FRANKFURT("frankfurt", "Frankfurt"),
    LONDON("london", "London"),
    SINGAPORE("singapore", "Singapore"),
    SYDNEY("sydney", "Sydney"),
    US_EAST("us-east", "US East"),
    US_WEST("us-west", "US West"),
    UNKNOWN("", "Unknown Region");

    private String id;
    private String name;
    private Region(String id, String name)
    {
        this.id = id;
        this.name = name;
    }

    /**
     * The human readable region name.
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * The regionId as defined by Discord.
     * @return
     */
    public String getId()
    {
        return id;
    }

    /**
     * Retrieves the Region based on the provided regionId.
     *
     * @param regionId
     *          The regionId, most likely provided from a READY event, Guild information or Voice Channel information.
     * @return
     *          The Region matching the regionId. Should no region match, this will return Region.UNKNOWN.
     */
    public static Region getRegion(String regionId)
    {
        for (Region region : values())
        {
            if (region.getId().equals(regionId))
            {
                return region;
            }
        }
        return UNKNOWN;
    }
}
