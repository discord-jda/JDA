package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.Nonnull;

/**
 * Extension of {@link net.dv8tion.jda.api.requests.RestAction RestAction} specifically
 * designed to create a {@link Entitlement Entitlement}.
 * This extension allows setting properties before executing the action.
 *
 * @see    net.dv8tion.jda.api.JDA
 * @see    net.dv8tion.jda.api.JDA#createTestEntitlement(long, long, OwnerType)
 */
public interface TestEntitlementCreateAction extends RestAction<Entitlement>
{

    /**
     * Set the SKU's id to create the entitlement in
     *
     * @param skuId
     *        The id of the SKU
     */
    void setSkuId(long skuId);

    /**
     * Set the owner's id to create the entitlement for
     *
     * @param ownerId
     *        The id of the owner - either guild id or user id
     */
    void setOwnerId(long ownerId);

    /**
     * Set the owner type to create the entitlement for
     *
     * @param type
     *        The type of the owner
     */
    void setOwnerType(OwnerType type);

    enum OwnerType
    {
        GUILD_SUBSCRIPTION(1),
        USER_SUBSCRIPTION(2),
        UNKNOWN(-1);

        private final int key;

        OwnerType(int key)
        {
            this.key = key;
        }

        /**
         * The Discord defined id key for this OwnerType.
         *
         * @return the id key.
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Gets the OwnerType related to the provided key.
         * <br>If an unknown key is provided, this returns {@link #UNKNOWN}
         *
         * @param  key
         *         The Discord key referencing a OwnerType.
         *
         * @return The OwnerType that has the key provided, or {@link #UNKNOWN} for unknown key.
         */
        @Nonnull
        public static OwnerType fromKey(int key)
        {
            for (OwnerType type : values())
            {
                if (type.getKey() == key)
                    return type;
            }
            return UNKNOWN;
        }
    }
}
