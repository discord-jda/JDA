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
package net.dv8tion.jda.api.utils

import net.dv8tion.jda.annotations.Incubating
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.utils.cache.LRUMemberCachePolicy
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.Nonnull

/**
 * Policy which decides whether a member (and respective user) should be kept in cache.
 * <br></br>This will be called throughout JDA when a member gets constructed or modified and allows for a dynamically
 * adjusting cache of users.
 *
 *
 * When [Guild.pruneMemberCache] is called, the configured policy will be used to unload any members that the policy
 * has decided not to cache.
 *
 *
 * If [GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] intent is disabled you should not use [.ALL] or [.ONLINE].
 * This intent enables guild member leave events which are required to remove members from cache properly.
 *
 *
 * This can be configured with [JDABuilder.setMemberCachePolicy(MemberCachePolicy)][net.dv8tion.jda.api.JDABuilder.setMemberCachePolicy].
 *
 *
 * **Example Policy**<br></br>
 * <pre>`MemberCachePolicy.VOICE                         // Keep in cache if currently in voice (skip LRU and ONLINE)
 * .or(MemberCachePolicy.ONLINE)               // Otherwise, only add to cache if online
 * .and(MemberCachePolicy.lru(1000)            // keep 1000 recently active members
 * .unloadUnless(MemberCachePolicy.VOICE)) // only unload if they are not in voice/guild owner
`</pre> *
 *
 * @see .DEFAULT
 *
 * @see .NONE
 *
 * @see .ALL
 *
 * @see .OWNER
 *
 * @see .VOICE
 *
 * @see .ONLINE
 *
 *
 * @see .or
 * @see .and
 * @see .any
 * @see .all
 * @since 4.2.0
 */
fun interface MemberCachePolicy {
    /**
     * Idempotent (ideally pure) function which decided whether to cache the provided member or not.
     * <br></br>The function should avoid throwing any exceptions or blocking.
     *
     * @param  member
     * The member
     *
     * @return True, if the member should be cached
     */
    fun cacheMember(@Nonnull member: Member?): Boolean

    /**
     * Convenience method to concatenate another policy.
     * <br></br>This is identical to `(member) -> policy1.cacheMember(member) || policy2.cacheMember(member)`.
     *
     * @param  policy
     * The policy to concat
     *
     * @throws IllegalArgumentException
     * If the provided policy is null
     *
     * @return New policy which combines both using a logical OR
     */
    @Nonnull
    fun or(@Nonnull policy: MemberCachePolicy): MemberCachePolicy {
        Checks.notNull(policy, "Policy")
        return MemberCachePolicy { member: Member? -> cacheMember(member) || policy.cacheMember(member) }
    }

    /**
     * Convenience method to require another policy.
     * <br></br>This is identical to `(member) -> policy1.cacheMember(member) && policy2.cacheMember(member)`.
     *
     * @param  policy
     * The policy to require in addition to this one
     *
     * @throws IllegalArgumentException
     * If the provided policy is null
     *
     * @return New policy which combines both using a logical AND
     */
    @Nonnull
    fun and(@Nonnull policy: MemberCachePolicy): MemberCachePolicy {
        return MemberCachePolicy { member: Member? -> cacheMember(member) && policy.cacheMember(member) }
    }

    companion object {
        /**
         * Composes a policy by concatenating multiple other policies.
         * <br></br>This is logically identical to `policy1 || policy2 || policy3 || ... || policyN`.
         *
         * @param  policy
         * The first policy
         * @param  policies
         * The other policies
         *
         * @return New policy which combines all provided polices using a logical OR
         */
        @Nonnull
        fun any(@Nonnull policy: MemberCachePolicy, @Nonnull vararg policies: MemberCachePolicy): MemberCachePolicy? {
            var policy = policy
            Checks.notNull(policy, "Policy")
            Checks.notNull(policies, "Policy")
            for (p in policies) policy = policy.or(p)
            return policy
        }

        /**
         * Composes a policy which requires multiple other policies.
         * <br></br>This is logically identical to `policy1 && policy2 && policy3 && ... && policyN`.
         *
         * @param  policy
         * The first policy
         * @param  policies
         * The other policies
         *
         * @return New policy which combines all provided polices using a logical AND
         */
        @Nonnull
        fun all(@Nonnull policy: MemberCachePolicy, @Nonnull vararg policies: MemberCachePolicy): MemberCachePolicy? {
            var policy = policy
            Checks.notNull(policy, "Policy")
            Checks.notNull(policies, "Policy")
            for (p in policies) policy = policy.and(p)
            return policy
        }

        /**
         * Implementation using a Least-Recently-Used (LRU) cache strategy.
         *
         *
         * **Example**<br></br>
         * <pre>`MemberCachePolicy.ONLINE.and( // only cache online members
         * MemberCachePolicy.lru(1000) // of those online members, track the 1000 most active members
         * .unloadUnless(MemberCachePolicy.VOICE) // always keep voice members cached regardless of age
         * )
        `</pre> *
         *
         * This policy would add online members into the pool of cached members.
         * The cached members are limited to 1000 active members, which are handled by the LRU policy.
         * When the LRU cache exceeds the maximum, it will evict the least recently active member from cache.
         * If the sub-policy, in this case [MemberCachePolicy.VOICE], evaluates to `true`, the member is retained in cache.
         * Otherwise, the member is unloaded using [Guild.unloadMember].
         *
         *
         * Note that the LRU policy itself always returns `true` for [.cacheMember], since that makes the member the **most recently used** instead.
         *
         * @param  maxSize
         * The maximum cache capacity of the LRU cache
         *
         * @return [LRUMemberCachePolicy]
         */
        @Nonnull
        fun lru(maxSize: Int): LRUMemberCachePolicy? {
            return LRUMemberCachePolicy(maxSize)
        }

        /**
         * Disable all member caching
         */
        @JvmField
        val NONE = MemberCachePolicy { member: Member? -> false }

        /**
         * Enable all member caching.
         *
         *
         * Not recommended without [GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] intent enabled.
         * The api will only send the guild member leave events when this intent is enabled. Without those events the members will stay in cache indefinitely.
         */
        @JvmField
        val ALL = MemberCachePolicy { member: Member? -> true }

        /**
         * Cache owner of the guild. This simply checks [Member.isOwner].
         */
        val OWNER: MemberCachePolicy = Member::isOwner

        /**
         * Cache online/idle/dnd users.
         * <br></br>Requires [GatewayIntent.GUILD_PRESENCES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_PRESENCES] and [CacheFlag.ONLINE_STATUS][net.dv8tion.jda.api.utils.cache.CacheFlag.ONLINE_STATUS] to be enabled.
         *
         *
         * This cannot cache online members immediately when they come online, due to discord limitations.
         * Discord only sends presence information without member details so the member will be cached once they become active.
         *
         *
         * Not recommended without [GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] intent enabled.
         * The api will only send the guild member leave events when this intent is enabled. Without those events the members will stay in cache indefinitely.
         */
        val ONLINE =
            MemberCachePolicy { member: Member -> member.onlineStatus !== OnlineStatus.OFFLINE && member.onlineStatus !== OnlineStatus.UNKNOWN }

        /**
         * Cache members who are connected to a voice channel.
         * <br></br>Requires [GatewayIntent.GUILD_VOICE_STATES][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES] and [CacheFlag.VOICE_STATE][net.dv8tion.jda.api.utils.cache.CacheFlag.VOICE_STATE] to be enabled.
         */
        val VOICE = MemberCachePolicy { member: Member ->
            val voiceState = member.voiceState
            voiceState != null && voiceState.channel != null
        }

        /**
         * Cache members who are boosting the guild. This checks [Member.isBoosting]
         * <br></br>Requires [GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] to be enabled.
         */
        val BOOSTER: MemberCachePolicy = Member::isBoosting

        /**
         * Caches members who haven't passed Membership Screening.
         *
         *
         * Not recommended without [GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] intent enabled.
         * The api will only send the guild member update events when this intent is enabled. Without those events the members will stay in cache indefinitely.
         *
         * @incubating Discord is still trying to figure this out
         */
        @Incubating
        val PENDING: MemberCachePolicy = Member::isPending

        /**
         * The default policy to use with [net.dv8tion.jda.api.JDABuilder.createDefault].
         * <br></br>This is identical to `VOICE.or(OWNER)`.
         *
         * @see .VOICE
         *
         * @see .OWNER
         */
        @JvmField
        val DEFAULT = VOICE.or(OWNER)
    }
}
