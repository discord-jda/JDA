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

package net.dv8tion.jda.api.utils.cache;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * An implementation of a Least-Recently-Used cache.
 * <br>When the cache capacity exceeds the configured maximum, the eldest cache entry is evicted.
 *
 * <p>You can use {@link #unloadUnless(MemberCachePolicy)}, to configure a conditional unloading.
 * If the configured sub-policy evaluates to {@code true}, the member will not be unloaded even when it is an old cache entry.
 *
 * <p>This is implemented using a queue and counter algorithm, to achieve amortized O(1) performance.
 *
 * <p><b>Example</b><br>
 * <pre>{@code
 * MemberCachePolicy.ONLINE.and( // only cache online members
 *   MemberCachePolicy.lru(1000) // of those online members, track the 1000 most active members
 *     .unloadUnless(MemberCachePolicy.VOICE) // always keep voice members cached regardless of age
 * )
 * }</pre>
 *
 * This policy would add online members into the pool of cached members.
 * The cached members are limited to 1000 active members, which are handled by the LRU policy.
 * When the LRU cache exceeds the maximum, it will evict the least recently active member from cache.
 * If the sub-policy, in this case {@link MemberCachePolicy#VOICE}, evaluates to {@code true}, the member is retained in cache.
 * Otherwise, the member is unloaded using {@link Guild#unloadMember(long)}.
 *
 * <p>Note that the LRU policy itself always returns {@code true} for {@link #cacheMember(Member)}, since that makes the member the <b>most recently used</b> instead.
 *
 * @see MemberCachePolicy#lru(int)
 */
public class LRUMemberCachePolicy implements MemberCachePolicy
{
    private static final long EPOCH_SECONDS = System.currentTimeMillis() / 1000;

    private final int maxMembers;

    // Low activity members (usage based, trades memory for cpu time)
    private final TObjectIntMap<Member> counters;
    private final ArrayDeque<MemberNode> queue;

    // High activity members (time based, trades cpu time for memory)
    private LinkedHashMap<Member, Integer> activeMemberCache;

    private MemberCachePolicy subPolicy;
    private int useActiveMemberCache;

    /**
     * Creates a new instance of the LRU cache policy with the configured maximum capacity.
     *
     * @param  maxMembers
     *         The maximum amount members to cache
     *
     * @throws IllegalArgumentException
     *         If the provided maximum is not positive
     */
    public LRUMemberCachePolicy(int maxMembers)
    {
        this(maxMembers, MemberCachePolicy.NONE);
    }

    private LRUMemberCachePolicy(int maxMembers, @Nonnull MemberCachePolicy subPolicy)
    {
        Checks.positive(maxMembers, "Max members");
        Checks.notNull(subPolicy, "MemberCachePolicy");
        this.maxMembers = maxMembers;
        this.counters = new TObjectIntHashMap<>(maxMembers);
        this.queue = new ArrayDeque<>(maxMembers);
        this.useActiveMemberCache = Math.max(10, this.maxMembers / 10);
        this.activeMemberCache = new LinkedHashMap<>();
        this.subPolicy = subPolicy;
    }

    /**
     * Configure when to unload a member.
     * <br>The provided policy will prevent a member from being uncached, if the policy returns true.
     * This can be useful to have a pool of least-recently-used members cached,
     * while also keeping members required for certain situations in cache.
     *
     * @param  subPolicy
     *         The policy to decide when to keep members cached, even when they are old cache entries
     *
     * @throws IllegalArgumentException
     *         If the provided policy is null
     *
     * @return The same cache policy instance, with the new sub-policy
     */
    @Nonnull
    public LRUMemberCachePolicy unloadUnless(@Nonnull MemberCachePolicy subPolicy)
    {
        Checks.notNull(subPolicy, "MemberCachePolicy");
        this.subPolicy = subPolicy;
        return this;
    }

    @Nonnull
    public synchronized LRUMemberCachePolicy withActiveMemberCache(boolean enabled)
    {
        return withActiveMemberCache(enabled ? this.maxMembers / 10 : 0);
    }

    @Nonnull
    public synchronized LRUMemberCachePolicy withActiveMemberCache(int activityCount)
    {
        this.useActiveMemberCache = activityCount;

        if (this.useActiveMemberCache < 1) // disabled if 0
        {
            // Move them all into the low activity cache
            Set<Member> moved = this.activeMemberCache.keySet();

            // Add them in insertion order to the queue, since the insertion order represents oldest to newest
            moved.forEach(this::cacheMember);
        }

        this.activeMemberCache = new LinkedHashMap<>();

        return this;
    }

    @Override
    public synchronized boolean cacheMember(@Nonnull Member member)
    {
        int currentCount = this.counters.adjustOrPutValue(member, 1, 1);

        if (this.useActiveMemberCache > 0)
        {
            // Check if this member is a high activity member or low activity member

            // If the active member cache already tracks this member, update their timestamp
            if (this.activeMemberCache.containsKey(member))
            {
                this.activeMemberCache.put(member, now());
                return true;
            }

            // If the member is not tracked yet, promote them to high activity cache if they take up 10% of the queue
            if (currentCount > this.useActiveMemberCache)
            {
                // This step has O(n) time complexity because it needs to iterate the entire queue
                // Worst-case: 10 x maxMembers operations
                this.queue.removeIf((node) -> member.equals(node.member));
                this.counters.remove(member);
                this.activeMemberCache.put(member, now());
                return true;
            }
        }


        // Otherwise use the queue, which has O(1) time complexity
        this.queue.add(new MemberNode(member));

        evictOldest();
        trimQueue();

        return true;
    }

    /**
     * Removes the head of the queue, with a counter equal to 1.
     */
    private void evictOldest()
    {
        Member unloadable = null;
        while (this.counters.size() + this.activeMemberCache.size() > this.maxMembers)
        {
            Iterator<Map.Entry<Member, Integer>> activeMemberIterator = this.activeMemberCache.entrySet().iterator();
            Map.Entry<Member, Integer> oldestActive = activeMemberIterator.hasNext() ? activeMemberIterator.next() : null;

            MemberNode removed = this.queue.poll();
            if (removed == null || oldestActive != null && oldestActive.getValue() < removed.insertionTime)
            {
                activeMemberIterator.remove();
                unloadable = oldestActive.getKey();
                if (removed != null)
                    this.queue.addFirst(removed);
            }
            else
            {
                if (this.counters.get(removed.member) <= 1)
                {
                    this.counters.remove(removed.member);
                    unloadable = removed.member;
                }
                else
                {
                    this.counters.adjustValue(removed.member, -1);
                }
            }

            if (unloadable != null && !this.subPolicy.cacheMember(unloadable))
            {
                unloadable.getGuild().unloadMember(unloadable.getIdLong());
            }
        }
    }

    /**
     * Trims the queue by removing all elements with a count higher than 1.
     */
    private void trimQueue()
    {
        while (!this.queue.isEmpty())
        {
            MemberNode head = this.queue.peek();
            if (this.counters.get(head) > 1)
            {
                this.counters.adjustValue(head.member, -1);
                this.queue.poll();
            }
            else
            {
                break;
            }
        }
    }

    private static int now()
    {
        return (int) (System.currentTimeMillis() / 1000 - EPOCH_SECONDS);
    }

    private static class MemberNode
    {
        public final int insertionTime;
        public final Member member;

        private MemberNode(Member member)
        {
            this.member = member;
            this.insertionTime = now();
        }
    }
}
