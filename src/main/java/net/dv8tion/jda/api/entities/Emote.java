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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.managers.EmoteManager;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a Custom Emote. (Custom Emoji in official Discord API terminology)
 *
 * <p>You can retrieve the creator of an emote by using {@link Guild#retrieveEmote(Emote)} followed
 * by using {@link ListedEmote#getUser()}.
 *
 * <p><b>This does not represent unicode emojis like they are used in the official client! (:smiley: is not a custom emoji)</b>
 *
 * @since  2.2
 *
 * @see    net.dv8tion.jda.api.entities.ListedEmote ListedEmote
 *
 * @see    Guild#getEmoteCache()
 * @see    Guild#getEmoteById(long)
 * @see    Guild#getEmotesByName(String, boolean)
 * @see    Guild#getEmotes()
 *
 * @see    JDA#getEmoteCache()
 * @see    JDA#getEmoteById(long)
 * @see    JDA#getEmotesByName(String, boolean)
 * @see    JDA#getEmotes()
 */
public interface Emote extends IMentionable
{
    /** Template for {@link #getImageUrl()} */
    String ICON_URL = "https://cdn.discordapp.com/emojis/%s.%s";

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} this emote is attached to.
     *
     * <p><b>This is null if the emote is created from a message</b>
     *
     * @return Guild of this emote or null if it is created from a message
     */
    @Nullable
    Guild getGuild();

    /**
     * Roles this emote is active for.
     * <br><a href="https://discord.com/developers/docs/resources/emoji#emoji-object" target="_blank">Learn More</a>
     *
     * @throws IllegalStateException
     *         If this Emote does not have attached roles according to {@link #canProvideRoles()}
     *
     * @return An immutable list of the roles this emote is active for (all roles if empty)
     *
     * @see    #canProvideRoles()
     */
    @Nonnull
    List<Role> getRoles();

    /**
     * Whether this Emote has an attached roles list. This might not be the case when the emote
     * is retrieved through special cases like audit-logs.
     *
     * <p>If this is not true then {@link #getRoles()} will throw {@link IllegalStateException}.
     *
     * @return True, if this emote has an attached roles list
     */
    boolean canProvideRoles();

    /**
     * The name of this emote.
     * <br>Does not include colons.
     *
     * @return String representation of this emote's name
     */
    @Nonnull
    String getName();

    /**
     * Whether this emote is managed. A managed Emote is controlled by Discord, not the Guild administrator, typical
     * via a service like BTTV in conjunction with Twitch.
     * <br><a href="https://discord.com/developers/docs/resources/emoji#emoji-object" target="_blank">Learn More</a>
     *
     * @return True, if this emote is managed
     */
    boolean isManaged();

    /**
     * Whether this emote is available. When an emote becomes unavailable, it cannot be used in messages. An emote becomes
     * unavailable when the {@link net.dv8tion.jda.api.entities.Guild.BoostTier BoostTier} of the guild drops such that
     * the maximum allowed emotes is lower than the total amount of emotes added to the guild.
     * 
     * <p>If an emote is added to the guild when the boost tier allows for more than 50 normal and 50 animated emotes
     * (BoostTier is at least {@link net.dv8tion.jda.api.entities.Guild.BoostTier#TIER_1 TIER_1}) and the emote is at least
     * the 51st one added, then the emote becomes unavailable when the BoostTier drops below a level that allows those emotes
     * to be used.
     * <br>Emotes that where added as part of a lower BoostTier (i.e. the 51st emote on BoostTier 2) will remain available,
     * as long as the BoostTier stays above the required level.
     * 
     * @return True, if this emote is available
     *
     * @since  4.2.1
     */
    boolean isAvailable();

    /**
     * The {@link net.dv8tion.jda.api.JDA JDA} instance of this Emote
     *
     * @return The JDA instance of this Emote
     */
    @Nonnull
    JDA getJDA();

    /**
     * Deletes this Emote.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOTE}
     *     <br>If this Emote was already removed</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_GUILD UNKNOWN_GUILD}
     *     <br>If the Guild of this Emote was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the Guild</li>
     * </ul>
     *
     * @throws java.lang.UnsupportedOperationException
     *         If this emote is managed by discord ({@link #isManaged()})
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if the Permission {@link net.dv8tion.jda.api.Permission#MANAGE_EMOTES_AND_STICKERS MANAGE_EMOTES_AND_STICKERS} is not given
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *         The RestAction to delete this Emote.
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> delete();

    /**
     * The {@link EmoteManager Manager} for this emote, used to modify
     * properties of the emote like name and role restrictions.
     * <br>You modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction.queue()}.
     *
     * <p>This is a lazy idempotent getter. The manager is retained after the first call.
     * This getter is not thread-safe and would require guards by the user.
     *
     * @throws IllegalStateException
     *         if this emote is created from a message or the bot does not have access to the emote
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_EMOTES_AND_STICKERS Permission.MANAGE_EMOTES_AND_STICKERS}
     *
     * @return The EmoteManager for this Emote
     */
    @Nonnull
    EmoteManager getManager();

    /**
     * Whether or not this Emote is animated.
     *
     * <p>Animated Emotes are available to Discord Nitro users as well as Bot accounts.
     *
     * @return Whether the Emote is animated or not.
     */
    boolean isAnimated();

    /**
     * A String representation of the URL which leads to image displayed within the official Discord&trade; client
     * when this Emote is used
     *
     * @return Discord CDN link to the Emote's image
     */
    @Nonnull
    default String getImageUrl()
    {
        return String.format(ICON_URL, getId(), isAnimated() ? "gif" : "png");
    }

    /**
     * Usable representation of this Emote (used to display in the client just like mentions with a specific format)
     * <br>Emotes are used with the format <code>&lt;:{@link #getName getName()}:{@link #getId getId()}&gt;</code>
     *
     * @return A usable String representation for this Emote
     *
     * @see    <a href="https://discord.com/developers/docs/resources/channel#message-formatting">Message Formatting</a>
     */
    @Nonnull
    @Override
    default String getAsMention()
    {
        return (isAnimated() ? "<a:" : "<:") + getName() + ":" + getId() + ">";
    }

    /**
     * Whether the specified Member can interact with this Emote
     *
     * @param  issuer
     *         The User to test
     *
     * @return True, if the provided Member can use this Emote
     */
    default boolean canInteract(Member issuer)
    {
        return PermissionUtil.canInteract(issuer, this);
    }

    /**
     * Whether the specified User can interact with this Emote within the provided MessageChannel
     * <br>Same logic as {@link #canInteract(User, MessageChannel, boolean) canInteract(issuer, channel, true)}!
     *
     * @param  issuer
     *         The User to test
     * @param  channel
     *         The MessageChannel to test
     *
     * @return True, if the provided Member can use this Emote
     */
    default boolean canInteract(User issuer, MessageChannel channel)
    {
        return PermissionUtil.canInteract(issuer, this, channel);
    }

    /**
     * Whether the specified User can interact with this Emote within the provided MessageChannel
     * <br>Special override to exclude elevated bot permissions in case of (for instance) reacting to messages.
     *
     * @param  issuer
     *         The User to test
     * @param  channel
     *         The MessageChannel to test
     * @param  botOverride
     *         Whether bots can use non-managed emotes in other guilds
     *
     * @return True, if the provided Member can use this Emote
     */
    default boolean canInteract(User issuer, MessageChannel channel, boolean botOverride)
    {
        return PermissionUtil.canInteract(issuer, this, channel, botOverride);
    }
}
