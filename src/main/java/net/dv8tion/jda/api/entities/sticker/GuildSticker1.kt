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
package net.dv8tion.jda.api.entities.sticker

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.managers.GuildStickerManager
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.api.requests.restaction.CacheRestAction
import org.jetbrains.annotations.Contract
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Custom guild sticker created by a user.
 */
interface GuildSticker : RichSticker {
    @get:Nonnull
    override val type: Sticker.Type?
        get() = Sticker.Type.GUILD

    /**
     * Whether this sticker is currently available.
     * <br></br>A sticker becomes unavailable when the boost level of a guild drops and the slot becomes unusable.
     *
     * @return True, if this sticker is available
     */
    @JvmField
    val isAvailable: Boolean

    /**
     * The ID of the guild this sticker belongs to.
     *
     * @return The guild id
     */
    @JvmField
    val guildIdLong: Long

    @get:Nonnull
    val guildId: String?
        /**
         * The ID of the guild this sticker belongs to.
         *
         * @return The guild id
         */
        get() = java.lang.Long.toUnsignedString(guildIdLong)

    /**
     * The [Guild] this sticker belongs to.
     * <br></br>This is null if the guild is not cached on creation,
     * Which is often the case for [JDA.retrieveSticker(...)][net.dv8tion.jda.api.JDA.retrieveSticker].
     *
     * @return Possibly-null guild of the sticker
     */
    val guild: Guild?

    /**
     * The user who created this sticker.
     * <br></br>This is null if the sticker is retrieved from cache,
     * since the owner is only provided for explicitly requested stickers.
     *
     * @return Possibly-null sticker owner
     *
     * @see .retrieveOwner
     */
    val owner: User?

    /**
     * Retrieves the sticker owner.
     * <br></br>If [.getOwner] is present, this will directly return the owner in a completed [RestAction] without making a request.
     * The user information might be outdated, you can use [action.useCache(false)][CacheRestAction.useCache] to force an update.
     *
     *
     * Possible [ErrorResponses][ErrorResponse] include:
     *
     *  * [MISSING_PERMISSIONS][ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost [Permission.MANAGE_GUILD_EXPRESSIONS] in the guild
     *
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_GUILD_EXPRESSIONS] in the guild.
     *
     * @return [CacheRestAction] - Type: [User]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveOwner(): CacheRestAction<User?>?

    /**
     * Deletes this sticker from the guild.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [MANAGE_GUILD_EXPRESSIONS][Permission.MANAGE_GUILD_EXPRESSIONS] in the guild.
     *
     * @return [AuditableRestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun delete(): AuditableRestAction<Void?>?

    @JvmField
    @get:Contract("->new")
    @get:CheckReturnValue
    @get:Nonnull
    val manager: GuildStickerManager?
}
