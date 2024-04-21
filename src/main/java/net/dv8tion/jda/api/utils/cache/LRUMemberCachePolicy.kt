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

import gnu.trove.map.TObjectIntMap
import gnu.trove.map.hash.TObjectIntHashMap
import net.dv8tion.jda.api.entities.Guild.unloadMember
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import java.util.function.Consumer
import javax.annotation.Nonnull
import kotlin.math.max

/**
 * An implementation of a Least-Recently-Used cache.
 * <br></br>When the cache capacity exceeds the configured maximum, the eldest cache entry is evicted.
 *
 *
 * You can use [.unloadUnless], to configure a conditional unloading.
 * If the configured sub-policy evaluates to `true`, the member will not be unloaded even when it is an old cache entry.
 *
 *
 * This is implemented using a queue and counter algorithm, to achieve amortized O(1) performance.
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
 * @see MemberCachePolicy.lru
 */
class LRUMemberCachePolicy private constructor(maxMembers: Int, @Nonnull subPolicy: MemberCachePolicy) :
    MemberCachePolicy {
    private val maxMembers: Int

    // Low activity members (usage based, trades memory for cpu time)
    private val counters: TObjectIntMap<Member>
    private val queue: ArrayDeque<MemberNode>

    // High activity members (time based, trades cpu time for memory)
    private var activeMemberCache: LinkedHashMap<Member, Int>
    private var subPolicy: MemberCachePolicy
    private var useActiveMemberCache: Int

    /**
     * Creates a new instance of the LRU cache policy with the configured maximum capacity.
     *
     * @param  maxMembers
     * The maximum amount members to cache
     *
     * @throws IllegalArgumentException
     * If the provided maximum is not positive
     */
    constructor(maxMembers: Int) : this(maxMembers, MemberCachePolicy.NONE)

    init {
        Checks.positive(maxMembers, "Max members")
        Checks.notNull(subPolicy, "MemberCachePolicy")
        this.maxMembers = maxMembers
        counters = TObjectIntHashMap(maxMembers)
        queue = ArrayDeque(maxMembers)
        useActiveMemberCache = max(10.0, (this.maxMembers / 10).toDouble()).toInt()
        activeMemberCache = LinkedHashMap()
        this.subPolicy = subPolicy
    }

    /**
     * Configure when to unload a member.
     * <br></br>The provided policy will prevent a member from being uncached, if the policy returns true.
     * This can be useful to have a pool of least-recently-used members cached,
     * while also keeping members required for certain situations in cache.
     *
     * @param  subPolicy
     * The policy to decide when to keep members cached, even when they are old cache entries
     *
     * @throws IllegalArgumentException
     * If the provided policy is null
     *
     * @return The same cache policy instance, with the new sub-policy
     */
    @Nonnull
    fun unloadUnless(@Nonnull subPolicy: MemberCachePolicy): LRUMemberCachePolicy {
        Checks.notNull(subPolicy, "MemberCachePolicy")
        this.subPolicy = subPolicy
        return this
    }

    @Nonnull
    @Synchronized
    fun withActiveMemberCache(enabled: Boolean): LRUMemberCachePolicy {
        return withActiveMemberCache(if (enabled) maxMembers / 10 else 0)
    }

    @Nonnull
    @Synchronized
    fun withActiveMemberCache(activityCount: Int): LRUMemberCachePolicy {
        useActiveMemberCache = activityCount
        if (useActiveMemberCache < 1) // disabled if 0
        {
            // Move them all into the low activity cache
            val moved: Set<Member> = activeMemberCache.keys

            // Add them in insertion order to the queue, since the insertion order represents oldest to newest
            moved.forEach(Consumer { member: Member -> cacheMember(member) })
        }
        activeMemberCache = LinkedHashMap()
        return this
    }

    @Synchronized
    override fun cacheMember(@Nonnull member: Member): Boolean {
        val currentCount = counters.adjustOrPutValue(member, 1, 1)
        if (useActiveMemberCache > 0) {
            // Check if this member is a high activity member or low activity member

            // If the active member cache already tracks this member, update their timestamp
            if (activeMemberCache.containsKey(member)) {
                activeMemberCache[member] = now()
                return true
            }

            // If the member is not tracked yet, promote them to high activity cache if they take up 10% of the queue
            if (currentCount > useActiveMemberCache) {
                // This step has O(n) time complexity because it needs to iterate the entire queue
                // Worst-case: 10 x maxMembers operations
                queue.removeIf { node: MemberNode -> member == node.member }
                counters.remove(member)
                activeMemberCache[member] = now()
                return true
            }
        }


        // Otherwise use the queue, which has O(1) time complexity
        queue.add(MemberNode(member))
        evictOldest()
        trimQueue()
        return true
    }

    /**
     * Removes the head of the queue, with a counter equal to 1.
     */
    private fun evictOldest() {
        var unloadable: Member? = null
        while (counters.size() + activeMemberCache.size > maxMembers) {
            val activeMemberIterator: MutableIterator<Map.Entry<Member, Int>> = activeMemberCache.entries.iterator()
            val oldestActive = if (activeMemberIterator.hasNext()) activeMemberIterator.next() else null
            val removed = queue.poll()
            if (removed == null || oldestActive != null && oldestActive.value < removed.insertionTime) {
                activeMemberIterator.remove()
                unloadable = oldestActive!!.key
                if (removed != null) queue.addFirst(removed)
            } else {
                if (counters[removed.member] <= 1) {
                    counters.remove(removed.member)
                    unloadable = removed.member
                } else {
                    counters.adjustValue(removed.member, -1)
                }
            }
            if (unloadable != null && !subPolicy.cacheMember(unloadable)) {
                unloadable.guild.unloadMember(unloadable.idLong)
            }
        }
    }

    /**
     * Trims the queue by removing all elements with a count higher than 1.
     */
    private fun trimQueue() {
        while (!queue.isEmpty()) {
            val head = queue.peek()
            if (counters[head] > 1) {
                counters.adjustValue(head.member, -1)
                queue.poll()
            } else {
                break
            }
        }
    }

    private class MemberNode(val member: Member) {
        val insertionTime: Int

        init {
            insertionTime = now()
        }
    }

    companion object {
        private val EPOCH_SECONDS = System.currentTimeMillis() / 1000
        private fun now(): Int {
            return (System.currentTimeMillis() / 1000 - EPOCH_SECONDS).toInt()
        }
    }
}
