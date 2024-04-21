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
package net.dv8tion.jda.api.entities.emoji

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.managers.CustomEmojiManager
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.api.requests.restaction.CacheRestAction
import net.dv8tion.jda.internal.utils.PermissionUtil
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a Custom Emoji.
 *
 *
 * **This does not represent unicode emojis like they are used in the official client!
 * The format `:smiley:` is a client-side alias which is replaced by the unicode emoji, not a custom emoji.**
 *
 * @see Guild.getEmojiCache
 * @see Guild.getEmojiById
 * @see Guild.getEmojisByName
 * @see Guild.getEmojis
 * @see JDA.getEmojiCache
 * @see JDA.getEmojiById
 * @see JDA.getEmojisByName
 * @see JDA.getEmojis
 */
interface RichCustomEmoji : CustomEmoji {
    @JvmField
    @get:Nonnull
    val guild: Guild?

    @JvmField
    @get:Nonnull
    val roles: List<Role?>?

    /**
     * Whether this emoji is managed. A managed emoji is controlled by Discord, not the Guild administrator, typical
     * via a service like BTTV in conjunction with Twitch.
     * <br></br>[Learn More](https://discord.com/developers/docs/resources/emoji#emoji-object)
     *
     * @return True, if this emoji is managed
     */
    @JvmField
    val isManaged: Boolean

    /**
     * Whether this emoji is available. When an emoji becomes unavailable, it cannot be used in messages. An emoji becomes
     * unavailable when the [BoostTier][net.dv8tion.jda.api.entities.Guild.BoostTier] of the guild drops such that
     * the maximum allowed emojis is lower than the total amount of emojis added to the guild.
     *
     *
     * If an emoji is added to the guild when the boost tier allows for more than 50 normal and 50 animated emojis
     * (BoostTier is at least [TIER_1][net.dv8tion.jda.api.entities.Guild.BoostTier.TIER_1]) and the emoji is at least
     * the 51st one added, then the emoji becomes unavailable when the BoostTier drops below a level that allows those emojis
     * to be used.
     * <br></br>emojis that where added as part of a lower BoostTier (i.e. the 51st emoji on BoostTier 2) will remain available,
     * as long as the BoostTier stays above the required level.
     *
     * @return True, if this emoji is available
     */
    val isAvailable: Boolean

    @get:Nonnull
    val jDA: JDA?

    /**
     * The user who created this emoji
     *
     *
     * This is only available for manually retrieved emojis from [Guild.retrieveEmojis]
     * and [Guild.retrieveEmojiById].
     * <br></br>Requires [Permission.MANAGE_GUILD_EXPRESSIONS][net.dv8tion.jda.api.Permission.MANAGE_GUILD_EXPRESSIONS].
     *
     * @throws IllegalStateException
     * If this emoji does not have user information
     *
     * @return The user who created this emoji, or null if not provided
     *
     * @see .retrieveOwner
     */
    @JvmField
    val owner: User?

    /**
     * Retrieves the owner of this emoji.
     * <br></br>If [.getOwner] is present, this will directly return the owner in a completed [RestAction] without making a request.
     * The user information might be outdated, you can use [action.useCache(false)][CacheRestAction.useCache] to force an update.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_GUILD_EXPRESSIONS][net.dv8tion.jda.api.Permission.MANAGE_GUILD_EXPRESSIONS]
     * in this guild
     *
     * @return [RestAction] - Type: [User]
     *
     * @see .getOwner
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveOwner(): CacheRestAction<User?>?

    /**
     * Deletes this emoji.
     *
     *
     * Possible ErrorResponses include:
     *
     *  * [UNKNOWN_EMOJI][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_EMOJI]
     * <br></br>If this emoji was already removed
     *
     *  * [UNKNOWN_GUILD][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_GUILD]
     * <br></br>If the Guild of this emoji was deleted
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>If we were removed from the Guild
     *
     *
     * @throws java.lang.UnsupportedOperationException
     * If this emoji is managed by discord ([.isManaged])
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * if the Permission [MANAGE_GUILD_EXPRESSIONS][net.dv8tion.jda.api.Permission.MANAGE_GUILD_EXPRESSIONS] is not given
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     * The RestAction to delete this emoji.
     */
    @Nonnull
    @CheckReturnValue
    fun delete(): AuditableRestAction<Void?>?

    @JvmField
    @get:CheckReturnValue
    @get:Nonnull
    val manager: CustomEmojiManager?

    /**
     * Whether the specified Member can interact with this emoji
     *
     * @param  issuer
     * The User to test
     *
     * @return True, if the provided Member can use this emoji
     */
    fun canInteract(issuer: Member?): Boolean {
        return PermissionUtil.canInteract(issuer, this)
    }

    /**
     * Whether the specified User can interact with this emoji within the provided MessageChannel
     * <br></br>Same logic as [canInteract(issuer, channel, true)][.canInteract]!
     *
     * @param  issuer
     * The User to test
     * @param  channel
     * The MessageChannel to test
     *
     * @return True, if the provided Member can use this emoji
     */
    fun canInteract(issuer: User?, channel: MessageChannel?): Boolean {
        return PermissionUtil.canInteract(issuer, this, channel)
    }

    /**
     * Whether the specified User can interact with this emoji within the provided MessageChannel
     * <br></br>Special override to exclude elevated bot permissions in case of (for instance) reacting to messages.
     *
     * @param  issuer
     * The User to test
     * @param  channel
     * The MessageChannel to test
     * @param  botOverride
     * Whether bots can use non-managed emojis in other guilds
     *
     * @return True, if the provided Member can use this emoji
     */
    fun canInteract(issuer: User?, channel: MessageChannel?, botOverride: Boolean): Boolean {
        return PermissionUtil.canInteract(issuer, this, channel, botOverride)
    }
}
