package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumSet;

public interface Sku extends ISnowflake
{

    /**
     * The type of the Sku.
     *
     * @return the {@link Sku Sku} type
     */
    @Nonnull
    SkuType getType();

    /**
     * The id of the parent application of this {@link Sku Sku}
     *
     * @return The id of the parent application of this {@link Sku Sku}
     */
    long getApplicationIdLong();

    /**
     * The id of the parent application of this {@link Sku Sku}
     *
     * @return The id of the parent application of this {@link Sku Sku}
     */
    @Nonnull
    default String getApplicationId()
    {
        return Long.toUnsignedString(getApplicationIdLong());
    }

    /**
     * The name of this {@link Sku Sku}
     *
     * @return The name of this {@link Sku Sku}
     */
    @Nonnull
    String getName();

    /**
     * System-generated URL slug based on the SKU's name
     *
     * @return The slug of the {@link Sku SKU}
     */
    @Nonnull
    String getSlug();

    /**
     * The raw {@link SkuFlag flags} bitset for this Sku.
     *
     * @return The raw flag bitset
     */
    int getFlagsRaw();

    /**
     * The {@link SkuFlag flags} for this member as an {@link EnumSet}.
     * <br>Modifying this set will not update the member, it is a copy of existing flags.
     *
     * @return The flags
     */
    @Nonnull
    default EnumSet<SkuFlag> getFlags()
    {
        return SkuFlag.fromRaw(getFlagsRaw());
    }

    /**
     * Create a purchase discount for a user on the next purchase.
     * <br> The discount will be automatically removed after the ttl expires or the user makes a purchase.
     *
     * @param userId
     *        The id of the user to create the discount for
     * @param percentOff
     *        The percentage off the next purchase
     * @param ttl
     *        The time to live for the discount in seconds
     *
     * @throws IllegalArgumentException
     *         If the ttl is not between 60 and 3600 seconds
     *
     * @return {@link RestAction} - Type: Void
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> createPurchaseDiscount(long userId, int percentOff, int ttl);

    /**
     * Delete the purchase discount for a user.
     *
     * @param userId
     *        The id of the user to delete the discount for
     *
     * @return {@link RestAction} - Type: Void
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> deletePurchaseDiscount(long userId);

    enum SkuType
    {
        DURABLE(2),
        CONSUMABLE(3),
        SUBSCRIPTION(5),
        SUBSCRIPTION_GROUP(6),
        UNKNOWN(-1);

        private final int key;

        SkuType(int key)
        {
            this.key = key;
        }

        /**
         * The Discord defined id key for this Sku.
         *
         * @return the id key.
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Gets the SkuType related to the provided key.
         * <br>If an unknown key is provided, this returns {@link #UNKNOWN}
         *
         * @param  key
         *         The Discord key referencing a SkuType.
         *
         * @return The SkuType that has the key provided, or {@link #UNKNOWN} for unknown key.
         */
        @Nonnull
        public static SkuType fromKey(int key)
        {
            for (SkuType type : values())
            {
                if (type.getKey() == key)
                    return type;
            }
            return UNKNOWN;
        }
    }

    enum SkuFlag
    {
        /**
         * Sku is available for purchase
         */
        AVAILABLE(1 << 2),
        /**
         * 	Recurring Sku that can be purchased by a user and applied to a single server. Grants access to every user in that server.
         */
        GUILD_SUBSCRIPTION(1 << 7),
        /**
         * Recurring Sku purchased by a user for themselves. Grants access to the purchasing user in every server.
         */
        STARTED_ONBOARDING(1 << 8),
        ;

        private final int raw;


        SkuFlag(int raw)
        {
            this.raw = raw;
        }

        /**
         * The raw value used by Discord for this flag
         *
         * @return The raw value
         */
        public int getRaw()
        {
            return raw;
        }

        /**
         * The {@link SkuFlag Flags} represented by the provided raw value.
         * <br>If the provided raw value is {@code 0} this will return an empty {@link java.util.EnumSet EnumSet}.
         *
         * @param  raw
         *         The raw value
         *
         * @return EnumSet containing the flags represented by the provided raw value
         */
        @Nonnull
        public static EnumSet<SkuFlag> fromRaw(int raw)
        {
            EnumSet<SkuFlag> flags = EnumSet.noneOf(SkuFlag.class);
            for (SkuFlag flag : values())
            {
                if ((raw & flag.raw) == flag.raw)
                    flags.add(flag);
            }
            return flags;
        }

        /**
         * The raw value of the provided {@link SkuFlag Flags}.
         * <br>If the provided set is empty this will return {@code 0}.
         *
         * @param  flags
         *         The flags
         *
         * @return The raw value of the provided flags
         */
        public static int toRaw(@Nonnull Collection<SkuFlag> flags)
        {
            Checks.noneNull(flags, "Flags");
            int raw = 0;
            for (SkuFlag flag : flags)
                raw |= flag.raw;
            return raw;
        }
    }
}
