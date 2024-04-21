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
package net.dv8tion.jda.api.entities

import java.time.OffsetDateTime
import javax.annotation.Nonnull

/**
 * Represents a user or guild that has access to a premium offering in your application.
 *
 * @see [Discord Docs about Entitlements](https://discord.com/developers/docs/monetization/entitlements)
 */
interface Entitlement : ISnowflake {
    /**
     * The id of the SKU related to this [Entitlement]
     *
     * @return The id of the SKU related to this [Entitlement]
     */
    val skuIdLong: Long

    @get:Nonnull
    val skuId: String?
        /**
         * The id of the SKU related to this [Entitlement]
         *
         * @return The id of the SKU related to this [Entitlement]
         */
        get() = java.lang.Long.toUnsignedString(skuIdLong)

    /**
     * The id of the parent application of this [Entitlement]
     *
     * @return The id of the parent application of this [Entitlement]
     */
    val applicationIdLong: Long

    @get:Nonnull
    val applicationId: String?
        /**
         * The id of the parent application of this [Entitlement]
         *
         * @return The id of the parent application of this [Entitlement]
         */
        get() = java.lang.Long.toUnsignedString(applicationIdLong)

    /**
     * The id of the user that purchased the [Entitlement]
     *
     * @return The id of the user that purchased the [Entitlement]
     */
    val userIdLong: Long
    val userId: String?
        /**
         * The id of the user that purchased the [Entitlement]
         *
         * @return The id of the user that purchased the [Entitlement]
         */
        get() = java.lang.Long.toUnsignedString(userIdLong)

    /**
     * The guild id that is granted access to the [Entitlement]s SKU
     *
     * @return The id of the guild that purchased the [Entitlement] or 0 if this is not a guild subscription
     */
    val guildIdLong: Long
    val guildId: String?
        /**
         * The guild id that is granted access to the [Entitlement]s SKU
         *
         * @return The id of the guild that purchased the [Entitlement] or `null` if this is not a guild subscription
         */
        get() = if (guildIdLong == 0L) null else java.lang.Long.toUnsignedString(guildIdLong)

    @get:Nonnull
    val type: EntitlementType?

    /**
     * Whether the [Entitlement] has been deleted or not.
     *
     * @return True if the [Entitlement] was deleted, False otherwise
     *
     * @see net.dv8tion.jda.api.events.entitlement.EntitlementDeleteEvent
     */
    val isDeleted: Boolean

    /**
     * The start date at which the [Entitlement] is valid.
     *
     * @return Start date at which the [Entitlement] is valid. Not present when using test entitlements.
     */
    val timeStarting: OffsetDateTime?

    /**
     * Date at which the [Entitlement] is no longer valid.
     *
     * @return Date at which the [Entitlement] is no longer valid. Not present when using test entitlements.
     */
    @JvmField
    val timeEnding: OffsetDateTime?

    /**
     * Represents the type of this Entitlement
     */
    enum class EntitlementType(
        /**
         * The Discord defined id key for this EntitlementType.
         *
         * @return the id key.
         */
        val key: Int
    ) {
        APPLICATION_SUBSCRIPTION(8),

        /**
         * Placeholder for unsupported types.
         */
        UNKNOWN(-1);

        companion object {
            /**
             * Gets the EntitlementType related to the provided key.
             * <br></br>If an unknown key is provided, this returns [.UNKNOWN]
             *
             * @param  key
             * The Discord key referencing a EntitlementType.
             *
             * @return The EntitlementType that has the key provided, or [.UNKNOWN] for unknown key.
             */
            @JvmStatic
            @Nonnull
            fun fromKey(key: Int): EntitlementType {
                for (type in entries) {
                    if (type.key == key) return type
                }
                return UNKNOWN
            }
        }
    }
}
