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
package net.dv8tion.jda.api.utils.cache

import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.*
import javax.annotation.Nonnull

/**
 * Flags used to enable cache services for JDA.
 * <br></br>Check the flag descriptions to see which [intents][net.dv8tion.jda.api.requests.GatewayIntent] are required to use them.
 */
enum class CacheFlag @JvmOverloads constructor(
    /**
     * The required [GatewayIntent] for this cache flag.
     *
     * @return The required intent, or null if no intents are required.
     */
    @JvmField val requiredIntent: GatewayIntent? = null
) {
    /**
     * Enables cache for [Member.getActivities]
     *
     *
     * Requires [GUILD_PRESENCES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_PRESENCES] intent to be enabled.
     */
    ACTIVITY(GatewayIntent.GUILD_PRESENCES),

    /**
     * Enables cache for [Member.getVoiceState]
     * <br></br>This will always be cached for self member.
     *
     *
     * Requires [GUILD_VOICE_STATES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES] intent to be enabled.
     */
    VOICE_STATE(GatewayIntent.GUILD_VOICE_STATES),

    /**
     * Enables cache for [Guild.getEmojiCache]
     *
     *
     * Requires [GUILD_EMOJIS_AND_STICKERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_EMOJIS_AND_STICKERS] intent to be enabled.
     */
    EMOJI(GatewayIntent.GUILD_EMOJIS_AND_STICKERS),

    /**
     * Enables cache for [Guild.getStickerCache]
     *
     *
     * Requires [GUILD_EMOJIS_AND_STICKERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_EMOJIS_AND_STICKERS] intent to be enabled.
     */
    STICKER(GatewayIntent.GUILD_EMOJIS_AND_STICKERS),

    /**
     * Enables cache for [Member.getOnlineStatus(ClientType)][Member.getOnlineStatus]
     *
     *
     * Requires [GUILD_PRESENCES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_PRESENCES] intent to be enabled.
     */
    CLIENT_STATUS(GatewayIntent.GUILD_PRESENCES),

    /**
     * Enables cache for [net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer.getMemberPermissionOverrides]
     */
    MEMBER_OVERRIDES,

    /**
     * Enables cache for [Role.getTags]
     */
    ROLE_TAGS,

    /**
     * Enables cache for [IPostContainer.getAvailableTagCache] and [ThreadChannel.getAppliedTags]
     */
    FORUM_TAGS,

    /**
     * Enables cache for [Member.getOnlineStatus]
     * <br></br>This is enabled implicitly by [.ACTIVITY] and [.CLIENT_STATUS].
     *
     *
     * Requires [GUILD_PRESENCES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_PRESENCES] intent to be enabled.
     *
     * @since 4.3.0
     */
    ONLINE_STATUS(GatewayIntent.GUILD_PRESENCES),

    /**
     * Enables cache for [Guild.getScheduledEventCache]
     *
     *
     * Requires [SCHEDULED_EVENTS][net.dv8tion.jda.api.requests.GatewayIntent.SCHEDULED_EVENTS] intent to be enabled.
     */
    SCHEDULED_EVENTS(GatewayIntent.SCHEDULED_EVENTS);

    val isPresence: Boolean
        /**
         * Whether this cache flag is for presence information of a member.
         *
         * @return True, if this is for presences
         */
        get() = requiredIntent == GatewayIntent.GUILD_PRESENCES

    companion object {
        @JvmStatic
        @get:Nonnull
        val privileged = EnumSet.of(ACTIVITY, CLIENT_STATUS, ONLINE_STATUS)
            /**
             * Collects all cache flags that require privileged intents
             *
             * @return [EnumSet] of the cache flags that require the privileged intents
             */
            get() = EnumSet.copyOf(field)
    }
}
