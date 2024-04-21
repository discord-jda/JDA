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

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.utils.MiscUtil
import javax.annotation.Nonnull

/**
 * [CacheView][net.dv8tion.jda.api.utils.cache.CacheView] implementation
 * specifically to combine [Member][net.dv8tion.jda.api.entities.Member] cache views.
 *
 *
 * This is done because Members do not implement [ISnowflake][net.dv8tion.jda.api.entities.ISnowflake] as
 * they are not globally unique but only unique per [Guild][net.dv8tion.jda.api.entities.Guild]!
 *
 * @see CacheView CacheView for details on Efficient Memory Usage
 */
interface UnifiedMemberCacheView : CacheView<Member?> {
    /**
     * Retrieves all member represented by the provided ID.
     *
     * @param  id
     * The ID of the members
     *
     * @return Possibly-empty unmodifiable list of member for the specified ID
     */
    @Nonnull
    fun getElementsById(id: Long): List<Member?>?

    /**
     * Retrieves all member represented by the provided ID.
     *
     * @param  id
     * The ID of the members
     *
     * @throws java.lang.NumberFormatException
     * If the provided String is `null` or
     * cannot be resolved to an unsigned long id
     *
     * @return Possibly-empty unmodifiable list of member for the specified ID
     */
    @Nonnull
    fun getElementsById(@Nonnull id: String?): List<Member?>? {
        return getElementsById(MiscUtil.parseSnowflake(id))
    }

    /**
     * Creates an immutable list of all members matching the given username.
     * <br></br>This will check the name of the wrapped user.
     *
     * @param  name
     * The name to check
     * @param  ignoreCase
     * Whether to ignore case when comparing usernames
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided name is `null`
     *
     * @return Immutable list of members with the given username
     */
    @Nonnull
    fun getElementsByUsername(@Nonnull name: String?, ignoreCase: Boolean): List<Member?>?

    /**
     * Creates an immutable list of all members matching the given username.
     * <br></br>This will check the name of the wrapped user.
     *
     * @param  name
     * The name to check
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided name is `null`
     *
     * @return Immutable list of members with the given username
     */
    @Nonnull
    fun getElementsByUsername(@Nonnull name: String?): List<Member?>? {
        return getElementsByUsername(name, false)
    }

    /**
     * Creates an immutable list of all members matching the given nickname.
     * <br></br>This will check the nickname of the member.
     * If provided with `null` this will check for members
     * that have no nickname set.
     *
     * @param  name
     * The nullable nickname to check
     * @param  ignoreCase
     * Whether to ignore case when comparing nicknames
     *
     * @return Immutable list of members with the given nickname
     */
    @Nonnull
    fun getElementsByNickname(name: String?, ignoreCase: Boolean): List<Member?>?

    /**
     * Creates an immutable list of all members matching the given nickname.
     * <br></br>This will check the nickname of the member.
     * If provided with `null` this will check for members
     * that have no nickname set.
     *
     * @param  name
     * The nullable nickname to check
     *
     * @return Immutable list of members with the given nickname
     */
    @Nonnull
    fun getElementsByNickname(name: String?): List<Member?>? {
        return getElementsByNickname(name, false)
    }

    /**
     * Creates an immutable list of all members that hold all
     * of the provided roles.
     *
     * @param  roles
     * Roles the members should have
     *
     * @throws java.lang.IllegalArgumentException
     * If provided with `null`
     *
     * @return Immutable list of members with the given roles
     */
    @Nonnull
    fun getElementsWithRoles(@Nonnull vararg roles: Role?): List<Member?>?

    /**
     * Creates an immutable list of all members that hold all
     * of the provided roles.
     *
     * @param  roles
     * Roles the members should have
     *
     * @throws java.lang.IllegalArgumentException
     * If provided with `null`
     *
     * @return Immutable list of members with the given roles
     */
    @Nonnull
    fun getElementsWithRoles(@Nonnull roles: Collection<Role?>?): List<Member?>?
}
