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

package net.dv8tion.jda.api.entities.emoji;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.managers.CustomEmojiManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a Custom Emoji.
 *
 * <p><b>This does not represent unicode emojis like they are used in the official client!
 * The format {@code :smiley:} is a client-side alias which is replaced by the unicode emoji, not a custom emoji.</b>
 *
 * @see    Guild#getEmojiCache()
 * @see    Guild#getEmojiById(long)
 * @see    Guild#getEmojisByName(String, boolean)
 * @see    Guild#getEmojis()
 *
 * @see    JDA#getEmojiCache()
 * @see    JDA#getEmojiById(long)
 * @see    JDA#getEmojisByName(String, boolean)
 * @see    JDA#getEmojis()
 */
public interface RichCustomEmoji extends CustomEmoji
{
    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} this emoji is attached to.
     *
     * @return Guild of this emoji
     */
    @Nonnull
    Guild getGuild();

    /**
     * Roles this emoji is active for.
     * <br><a href="https://discord.com/developers/docs/resources/emoji#emoji-object" target="_blank">Learn More</a>
     *
     * @return An immutable list of the roles this emoji is active for (all roles if empty)
     */
    @Nonnull
    List<Role> getRoles();

    /**
     * Whether this emoji is managed. A managed emoji is controlled by Discord, not the Guild administrator, typical
     * via a service like BTTV in conjunction with Twitch.
     * <br><a href="https://discord.com/developers/docs/resources/emoji#emoji-object" target="_blank">Learn More</a>
     *
     * @return True, if this emoji is managed
     */
    boolean isManaged();

    /**
     * Whether this emoji is available. When an emoji becomes unavailable, it cannot be used in messages. An emoji becomes
     * unavailable when the {@link net.dv8tion.jda.api.entities.Guild.BoostTier BoostTier} of the guild drops such that
     * the maximum allowed emojis is lower than the total amount of emojis added to the guild.
     * 
     * <p>If an emoji is added to the guild when the boost tier allows for more than 50 normal and 50 animated emojis
     * (BoostTier is at least {@link net.dv8tion.jda.api.entities.Guild.BoostTier#TIER_1 TIER_1}) and the emoji is at least
     * the 51st one added, then the emoji becomes unavailable when the BoostTier drops below a level that allows those emojis
     * to be used.
     * <br>emojis that where added as part of a lower BoostTier (i.e. the 51st emoji on BoostTier 2) will remain available,
     * as long as the BoostTier stays above the required level.
     * 
     * @return True, if this emoji is available
     */
    boolean isAvailable();

    /**
     * The {@link net.dv8tion.jda.api.JDA JDA} instance of this emoji
     *
     * @return The JDA instance of this emoji
     */
    @Nonnull
    JDA getJDA();

    /**
     * The user who created this emoji
     *
     * <p>This is only available for manually retrieved emojis from {@link Guild#retrieveEmojis()}
     * and {@link Guild#retrieveEmojiById(long)}.
     * <br>Requires {@link net.dv8tion.jda.api.Permission#MANAGE_GUILD_EXPRESSIONS Permission.MANAGE_GUILD_EXPRESSIONS}.
     *
     * @throws IllegalStateException
     *         If this emoji does not have user information
     *
     * @return The user who created this emoji, or null if not provided
     *
     * @see    #retrieveOwner()
     */
    @Nullable
    User getOwner();

    /**
     * Retrieves the owner of this emoji.
     * <br>If {@link #getOwner()} is present, this will directly return the owner in a completed {@link RestAction} without making a request.
     * The user information might be outdated, you can use {@link CacheRestAction#useCache(boolean) action.useCache(false)} to force an update.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_GUILD_EXPRESSIONS Permission.MANAGE_GUILD_EXPRESSIONS}
     *         in this guild
     *
     * @return {@link RestAction} - Type: {@link User}
     *
     * @see    #getOwner()
     */
    @Nonnull
    @CheckReturnValue
    CacheRestAction<User> retrieveOwner();

    /**
     * Deletes this emoji.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>If this emoji was already removed</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_GUILD UNKNOWN_GUILD}
     *     <br>If the Guild of this emoji was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the Guild</li>
     * </ul>
     *
     * @throws java.lang.UnsupportedOperationException
     *         If this emoji is managed by discord ({@link #isManaged()})
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if the Permission {@link net.dv8tion.jda.api.Permission#MANAGE_GUILD_EXPRESSIONS MANAGE_GUILD_EXPRESSIONS} is not given
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *         The RestAction to delete this emoji.
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> delete();

    /**
     * The {@link CustomEmojiManager Manager} for this emoji, used to modify
     * properties of the emoji like name and role restrictions.
     * <br>You modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction.queue()}.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_GUILD_EXPRESSIONS Permission.MANAGE_GUILD_EXPRESSIONS}
     *
     * @return The CustomEmojiManager for this emoji
     */
    @Nonnull
    @CheckReturnValue
    CustomEmojiManager getManager();

    /**
     * Whether the specified Member can interact with this emoji
     *
     * @param  issuer
     *         The User to test
     *
     * @return True, if the provided Member can use this emoji
     */
    default boolean canInteract(Member issuer)
    {
        return PermissionUtil.canInteract(issuer, this);
    }

    /**
     * Whether the specified User can interact with this emoji within the provided MessageChannel
     * <br>Same logic as {@link #canInteract(User, net.dv8tion.jda.api.entities.channel.middleman.MessageChannel, boolean) canInteract(issuer, channel, true)}!
     *
     * @param  issuer
     *         The User to test
     * @param  channel
     *         The MessageChannel to test
     *
     * @return True, if the provided Member can use this emoji
     */
    default boolean canInteract(User issuer, MessageChannel channel)
    {
        return PermissionUtil.canInteract(issuer, this, channel);
    }

    /**
     * Whether the specified User can interact with this emoji within the provided MessageChannel
     * <br>Special override to exclude elevated bot permissions in case of (for instance) reacting to messages.
     *
     * @param  issuer
     *         The User to test
     * @param  channel
     *         The MessageChannel to test
     * @param  botOverride
     *         Whether bots can use non-managed emojis in other guilds
     *
     * @return True, if the provided Member can use this emoji
     */
    default boolean canInteract(User issuer, MessageChannel channel, boolean botOverride)
    {
        return PermissionUtil.canInteract(issuer, this, channel, botOverride);
    }
}
