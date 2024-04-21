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

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.requests.RestAction
import java.util.concurrent.*
import java.util.function.BooleanSupplier
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * [RestAction][net.dv8tion.jda.api.requests.RestAction] extension
 * specifically designed to allow bots to add [Users][net.dv8tion.jda.api.entities.User] to Guilds.
 * <br></br>This requires an **OAuth2 Access Token** with the scope `guilds.join` to work!
 *
 * @since  3.7.0
 *
 * @see Guild.addMember
 * @see [Discord OAuth2 Documentation](https://discord.com/developers/docs/topics/oauth2)
 */
interface MemberAction : RestAction<Void?> {
    @Nonnull
    override fun setCheck(checks: BooleanSupplier?): MemberAction?
    @Nonnull
    override fun timeout(timeout: Long, @Nonnull unit: TimeUnit): MemberAction?
    @Nonnull
    override fun deadline(timestamp: Long): MemberAction?

    @get:Nonnull
    val accessToken: String?

    @get:Nonnull
    val userId: String?

    /**
     * The user associated with the id
     *
     * @return Possibly-null user associated with the id
     */
    val user: User?

    @get:Nonnull
    val guild: Guild?

    /**
     * Sets the nickname of the user for the guild.
     * <br></br>This will then be visible with [Member.getNickname()][net.dv8tion.jda.api.entities.Member.getNickname].
     *
     * @param  nick
     * The nickname, or `null`
     *
     * @throws IllegalArgumentException
     * If the provided nickname is longer than 32 characters
     *
     * @return The current MemberAction for chaining
     */
    @Nonnull
    @CheckReturnValue
    fun setNickname(nick: String?): MemberAction?

    /**
     * Sets the roles of the user for the guild.
     * <br></br>This will then be visible with [Member.getRoles()][net.dv8tion.jda.api.entities.Member.getRoles].
     *
     * @param  roles
     * The roles, or `null`
     *
     * @throws IllegalArgumentException
     * If one of the provided roles is null or not from the same guild
     *
     * @return The current MemberAction for chaining
     */
    @Nonnull
    @CheckReturnValue
    fun setRoles(roles: Collection<Role?>?): MemberAction?

    /**
     * Sets the roles of the user for the guild.
     * <br></br>This will then be visible with [Member.getRoles()][net.dv8tion.jda.api.entities.Member.getRoles].
     *
     * @param  roles
     * The roles, or `null`
     *
     * @throws IllegalArgumentException
     * If one of the provided roles is null or not from the same guild
     *
     * @return The current MemberAction for chaining
     */
    @Nonnull
    @CheckReturnValue
    fun setRoles(vararg roles: Role?): MemberAction?

    /**
     * Whether the user should be voice muted in the guild.
     * <br></br>Default: `false`
     *
     * @param  mute
     * Whether the user should be voice muted in the guild.
     *
     * @return The current MemberAction for chaining
     */
    @Nonnull
    @CheckReturnValue
    fun setMute(mute: Boolean): MemberAction?

    /**
     * Whether the user should be voice deafened in the guild.
     * <br></br>Default: `false`
     *
     * @param  deaf
     * Whether the user should be voice deafened in the guild.
     *
     * @return The current MemberAction for chaining
     */
    @Nonnull
    @CheckReturnValue
    fun setDeafen(deaf: Boolean): MemberAction?
}
