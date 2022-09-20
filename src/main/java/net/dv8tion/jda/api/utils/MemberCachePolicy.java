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

package net.dv8tion.jda.api.utils;

import net.dv8tion.jda.annotations.Incubating;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

/**
 * Policy which decides whether a member (and respective user) should be kept in cache.
 * <br>This will be called throughout JDA when a member gets constructed or modified and allows for a dynamically
 * adjusting cache of users.
 *
 * <p>When {@link Guild#pruneMemberCache()} is called, the configured policy will be used to unload any members that the policy
 * has decided not to cache.
 *
 * <p>If {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent is disabled you should not use {@link #ALL} or {@link #ONLINE}.
 * This intent enables guild member leave events which are required to remove members from cache properly.
 *
 * <p>This can be configured with {@link net.dv8tion.jda.api.JDABuilder#setMemberCachePolicy(MemberCachePolicy) JDABuilder.setMemberCachePolicy(MemberCachePolicy)}.
 *
 * @see #DEFAULT
 * @see #NONE
 * @see #ALL
 * @see #OWNER
 * @see #VOICE
 * @see #ONLINE
 *
 * @see #or(MemberCachePolicy)
 * @see #and(MemberCachePolicy)
 * @see #any(MemberCachePolicy, MemberCachePolicy...)
 * @see #all(MemberCachePolicy, MemberCachePolicy...)
 *
 * @since 4.2.0
 */
@FunctionalInterface
public interface MemberCachePolicy
{
    /**
     * Disable all member caching
     */
    MemberCachePolicy NONE = (member) -> false;
    /**
     * Enable all member caching.
     *
     * <p>Not recommended without {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent enabled.
     * The api will only send the guild member leave events when this intent is enabled. Without those events the members will stay in cache indefinitely.
     */
    MemberCachePolicy ALL = (member) -> true;
    /**
     * Cache owner of the guild. This simply checks {@link Member#isOwner()}.
     */
    MemberCachePolicy OWNER = Member::isOwner;
    /**
     * Cache online/idle/dnd users.
     * <br>Requires {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_PRESENCES GatewayIntent.GUILD_PRESENCES} and {@link net.dv8tion.jda.api.utils.cache.CacheFlag#ONLINE_STATUS CacheFlag.ONLINE_STATUS} to be enabled.
     *
     * <p>This cannot cache online members immediately when they come online, due to discord limitations.
     * Discord only sends presence information without member details so the member will be cached once they become active.
     *
     * <p>Not recommended without {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent enabled.
     * The api will only send the guild member leave events when this intent is enabled. Without those events the members will stay in cache indefinitely.
     */
    MemberCachePolicy ONLINE = (member) -> member.getOnlineStatus() != OnlineStatus.OFFLINE && member.getOnlineStatus() != OnlineStatus.UNKNOWN;
    /**
     * Cache members who are connected to a voice channel.
     * <br>Requires {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_VOICE_STATES GatewayIntent.GUILD_VOICE_STATES} and {@link net.dv8tion.jda.api.utils.cache.CacheFlag#VOICE_STATE CacheFlag.VOICE_STATE} to be enabled.
     */
    MemberCachePolicy VOICE = (member) -> {
        GuildVoiceState voiceState = member.getVoiceState();
        return voiceState != null && voiceState.getChannel() != null;
    };
    /**
     * Cache members who are boosting the guild. This checks {@link Member#isBoosting()}
     * <br>Requires {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} to be enabled.
     * */
    MemberCachePolicy BOOSTER = Member::isBoosting;
    /**
     * Caches members who haven't passed Membership Screening.
     *
     * <p>Not recommended without {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent enabled.
     * The api will only send the guild member update events when this intent is enabled. Without those events the members will stay in cache indefinitely.
     *
     * @incubating Discord is still trying to figure this out
     */
    @Incubating
    MemberCachePolicy PENDING = Member::isPending;
    /**
     * The default policy to use with {@link net.dv8tion.jda.api.JDABuilder#createDefault(String)}.
     * <br>This is identical to {@code VOICE.or(OWNER)}.
     *
     * @see #VOICE
     * @see #OWNER
     */
    MemberCachePolicy DEFAULT = VOICE.or(OWNER);

    /**
     * Idempotent (ideally pure) function which decided whether to cache the provided member or not.
     * <br>The function should avoid throwing any exceptions or blocking.
     *
     * @param  member
     *         The member
     *
     * @return True, if the member should be cached
     */
    boolean cacheMember(@Nonnull Member member);

    /**
     * Convenience method to concatenate another policy.
     * <br>This is identical to {@code (member) -> policy1.cacheMember(member) || policy2.cacheMember(member)}.
     *
     * @param  policy
     *         The policy to concat
     *
     * @throws IllegalArgumentException
     *         If the provided policy is null
     *
     * @return New policy which combines both using a logical OR
     */
    @Nonnull
    default MemberCachePolicy or(@Nonnull MemberCachePolicy policy)
    {
        Checks.notNull(policy, "Policy");
        return (member) -> cacheMember(member) || policy.cacheMember(member);
    }

    /**
     * Convenience method to require another policy.
     * <br>This is identical to {@code (member) -> policy1.cacheMember(member) && policy2.cacheMember(member)}.
     *
     * @param  policy
     *         The policy to require in addition to this one
     *
     * @throws IllegalArgumentException
     *         If the provided policy is null
     *
     * @return New policy which combines both using a logical AND
     */
    @Nonnull
    default MemberCachePolicy and(@Nonnull MemberCachePolicy policy)
    {
        return (member) -> cacheMember(member) && policy.cacheMember(member);
    }

    /**
     * Composes a policy by concatenating multiple other policies.
     * <br>This is logically identical to {@code policy1 || policy2 || policy3 || ... || policyN}.
     *
     * @param  policy
     *         The first policy
     * @param  policies
     *         The other policies
     *
     * @return New policy which combines all provided polices using a logical OR
     */
    @Nonnull
    static MemberCachePolicy any(@Nonnull MemberCachePolicy policy, @Nonnull MemberCachePolicy... policies)
    {
        Checks.notNull(policy, "Policy");
        Checks.notNull(policies, "Policy");
        for (MemberCachePolicy p : policies)
            policy = policy.or(p);
        return policy;
    }

    /**
     * Composes a policy which requires multiple other policies.
     * <br>This is logically identical to {@code policy1 && policy2 && policy3 && ... && policyN}.
     *
     * @param  policy
     *         The first policy
     * @param  policies
     *         The other policies
     *
     * @return New policy which combines all provided polices using a logical AND
     */
    @Nonnull
    static MemberCachePolicy all(@Nonnull MemberCachePolicy policy, @Nonnull MemberCachePolicy... policies)
    {
        Checks.notNull(policy, "Policy");
        Checks.notNull(policies, "Policy");
        for (MemberCachePolicy p : policies)
            policy = policy.and(p);
        return policy;
    }
}
