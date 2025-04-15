package net.dv8tion.jda.api.entities;

/**
 * SKU Types represent the type of the offered SKU.
 *
 * <br>For subscriptions, SKUs will have a type of either {@link #SUBSCRIPTION} or {@link #SUBSCRIPTION_GROUP}.
 * For any current implementations, you will want to use the SKU type {@link #SUBSCRIPTION}.
 * A {@link #SUBSCRIPTION_GROUP} is automatically created for each SUBSCRIPTION SKU and are not used at this time.
 */
public enum SKUType
{
    /**
     * Durable one-time purchase
     */
    DURABLE(2),
    /**
     * Consumable one-time purchase
     */
    CONSUMABLE(3),
    /**
     * Represents a recurring subscription
     */
    SUBSCRIPTION(5),
    /**
     * System-generated group for each SUBSCRIPTION SKU created
     */
    SUBSCRIPTION_GROUP(6),
    /**
     * Unknown type.
     */
    UNKNOWN(-1);

    private final int id;

    SKUType(int id)
    {
        this.id = id;
    }

    /**
     * Parse a sku type from the id
     * @param type type id
     * @return sku type or {@link #UNKNOWN} if the SKU is not known.
     */
    public static SKUType fromId(int type)
    {
        for (SKUType value : values())
        {
            if (value.id == type) return value;
        }
        return UNKNOWN;
    }

    /**
     * The discord id of the type
     * @return discord id
     */
    public int id()
    {
        return id;
    }
}
