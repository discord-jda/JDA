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
package net.dv8tion.jda.api.requests.restaction

import net.dv8tion.jda.api.entities.Invite
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.internal.utils.Checks
import java.util.concurrent.TimeUnit
import java.util.function.BooleanSupplier
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * [Invite][net.dv8tion.jda.api.entities.Invite] Builder system created as an extension of [net.dv8tion.jda.api.requests.RestAction]
 * <br></br>Provides an easy way to gather and deliver information to Discord to create [Invites][net.dv8tion.jda.api.entities.Invite].
 *
 * @see net.dv8tion.jda.api.entities.channel.attribute.IInviteContainer.createInvite
 */
interface InviteAction : AuditableRestAction<Invite?> {
    @Nonnull
    override fun setCheck(checks: BooleanSupplier?): InviteAction?
    @Nonnull
    override fun timeout(timeout: Long, @Nonnull unit: TimeUnit): InviteAction?
    @Nonnull
    override fun deadline(timestamp: Long): InviteAction?

    /**
     * Sets the max age in seconds for the invite. Set this to `0` if the invite should never expire. Default is `86400` (24 hours).
     * `null` will reset this to the default value.
     *
     * @param  maxAge
     * The max age for this invite or `null` to use the default value.
     *
     * @throws IllegalArgumentException
     * If maxAge is negative.
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    fun setMaxAge(maxAge: Int?): InviteAction?

    /**
     * Sets the max age for the invite. Set this to `0` if the invite should never expire. Default is `86400` (24 hours).
     * `null` will reset this to the default value.
     *
     * @param  maxAge
     * The max age for this invite or `null` to use the default value.
     * @param  timeUnit
     * The [TimeUnit][java.util.concurrent.TimeUnit] type of `maxAge`.
     *
     * @throws IllegalArgumentException
     * If maxAge is negative or maxAge is positive and timeUnit is null.
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    fun setMaxAge(maxAge: Long?, @Nonnull timeUnit: TimeUnit?): InviteAction?

    /**
     * Sets the max uses for the invite. Set this to `0` if the invite should have unlimited uses. Default is `0`.
     * `null` will reset this to the default value.
     *
     * @param  maxUses
     * The max uses for this invite or `null` to use the default value.
     *
     * @throws IllegalArgumentException
     * If maxUses is negative.
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    fun setMaxUses(maxUses: Int?): InviteAction?

    /**
     * Sets whether the invite should only grant temporary membership. Default is `false`.
     *
     * @param  temporary
     * Whether the invite should only grant temporary membership or `null` to use the default value.
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    fun setTemporary(temporary: Boolean?): InviteAction?

    /**
     * Sets whether discord should reuse a similar invite. Default is `false`.
     *
     * @param  unique
     * Whether discord should reuse a similar invite or `null` to use the default value.
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    fun setUnique(unique: Boolean?): InviteAction?

    /**
     * Sets the id of the targeted application.
     * <br></br>The invite has to point to a voice channel.
     * The invite will have the [Invite.TargetType.EMBEDDED_APPLICATION] target.
     *
     * @param applicationId
     * The id of the embedded application to target or `0` to remove
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    fun setTargetApplication(applicationId: Long): InviteAction?

    /**
     * Sets the id of the targeted application.
     * <br></br>The invite has to point to a voice channel.
     * The invite will have the [Invite.TargetType.EMBEDDED_APPLICATION] target.
     *
     * @param applicationId
     * The id of the embedded application to target
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided ID is null
     * @throws java.lang.NumberFormatException
     * If the provided ID is not a snowflake
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    fun setTargetApplication(@Nonnull applicationId: String?): InviteAction? {
        return setTargetApplication(MiscUtil.parseSnowflake(applicationId))
    }

    /**
     * Sets the user whose stream to target for this invite.
     * <br></br>The user must be streaming in the same channel.
     * The invite will have the [Invite.TargetType.STREAM] target.
     *
     * @param userId
     * The id of the user whose stream to target or `0` to remove.
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    fun setTargetStream(userId: Long): InviteAction?

    /**
     * Sets the user whose stream to display for this invite.
     * <br></br>The user must be streaming in the same channel.
     * The invite will have the [Invite.TargetType.STREAM] target.
     *
     * @param userId
     * The id of the user whose stream to target.
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided ID is null
     * @throws java.lang.NumberFormatException
     * If the provided ID is not a snowflake
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    fun setTargetStream(@Nonnull userId: String?): InviteAction? {
        return setTargetStream(MiscUtil.parseSnowflake(userId))
    }

    /**
     * Sets the user whose stream to display for this invite.
     * <br></br>The user must be streaming in the same channel.
     * The invite will have the [Invite.TargetType.STREAM] target.
     *
     * @param user
     * The user whose stream to target.
     *
     * @throws IllegalArgumentException
     * If the provided user is `null`
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    fun setTargetStream(@Nonnull user: User): InviteAction? {
        Checks.notNull(user, "User")
        return setTargetStream(user.idLong)
    }

    /**
     * Sets the user whose stream to display for this invite.
     * <br></br>The user must be streaming in the same channel.
     * The invite will have the [Invite.TargetType.STREAM] target.
     *
     * @param member
     * The member whose stream to target.
     *
     * @throws IllegalArgumentException
     * If the provided member is `null`
     *
     * @return The current InviteAction for chaining.
     */
    @Nonnull
    @CheckReturnValue
    fun setTargetStream(@Nonnull member: Member): InviteAction? {
        Checks.notNull(member, "Member")
        return setTargetStream(member.idLong)
    }
}
