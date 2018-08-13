/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.client.managers.EmoteManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.utils.PermissionUtil;

import javax.annotation.CheckReturnValue;
import java.util.List;

/**
 * Represents a Custom Emote. (Custom Emoji in official Discord API terminology)
 *
 * <p><b>This does not represent unicode emojis like they are used in the official client! (:smiley: is not a custom emoji)</b>
 *
 * @since  2.2
 */
public interface Emote extends ISnowflake, IMentionable, IFakeable
{

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} this emote is attached to.
     *
     * <p><b>This is null if the emote is fake (retrieved from a Message)</b>
     *
     * @return Guild of this emote or null if it is a fake entity
     */
    Guild getGuild();

    /**
     * Roles this emote is active for
     * <br><a href="https://discordapp.com/developers/docs/resources/guild#emoji-object" target="_blank">Learn More</a>
     *
     * @throws IllegalStateException
     *         If this Emote is fake ({@link #isFake()})
     *
     * @return An immutable list of the roles this emote is active for (all roles if empty)
     */
    List<Role> getRoles();

    /**
     * The name of this emote
     *
     * @return String representation of this emote's name
     */
    String getName();

    /**
     * Whether this emote is managed. A managed Emote is controlled by Discord, not the Guild administrator, typical
     * via a service like BBTV in conjunction with Twitch.
     * <br><a href="https://discordapp.com/developers/docs/resources/guild#emoji-object" target="_blank">Learn More</a>
     *
     * @return True, if this emote is managed
     */
    boolean isManaged();

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Emote
     *
     * @return The JDA instance of this Emote
     */
    JDA getJDA();

    /**
     * Deletes this Emote.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOTE}
     *     <br>If this Emote was already removed</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_GUILD UNKNOWN_GUILD}
     *     <br>If the Guild of this Emote was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the Guild</li>
     * </ul>
     *
     * @throws IllegalStateException
     *         if this Emote is fake ({@link #isFake()})
     * @throws java.lang.UnsupportedOperationException
     *         If this emote is managed by discord ({@link #isManaged()})
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         if the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_EMOTES MANAGE_EMOTES} is not given
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         The RestAction to delete this Emote.
     */
    @CheckReturnValue
    AuditableRestAction<Void> delete();

    /**
     * The {@link net.dv8tion.jda.client.managers.EmoteManager Manager} for this emote, used to modify
     * properties of the emote like name and role restrictions.
     * <br>You modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction.queue()}.
     *
     * @throws IllegalStateException
     *         if this emote is fake
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_EMOTES Permission.MANAGE_EMOTES}
     *
     * @return The EmoteManager for this Emote
     */
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
    default String getImageUrl()
    {
        return "https://cdn.discordapp.com/emojis/" + getId() + (isAnimated() ? ".gif" : ".png");
    }

    /**
     * Usable representation of this Emote (used to display in the client just like mentions with a specific format)
     * <br>Emotes are used with the format <code>&lt;:{@link #getName getName()}:{@link #getId getId()}&gt;</code>
     *
     * @return A usable String representation for this Emote
     *
     * @see    <a href="https://discordapp.com/developers/docs/resources/channel#message-formatting">Message Formatting</a>
     */
    @Override
    default String getAsMention()
    {
        return (isAnimated() ? "<a:" : "<:") + getName() + ":" + getIdLong() + ">";
    }

    /**
     * Whether the specified Member can interact with this Emote
     *
     * @param  issuer
     *         The User to test
     *
     * @return True, if the provided Member can use this Emote
     *
     * @see    net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Emote)
     * @see    net.dv8tion.jda.core.utils.PermissionUtil#canInteract(User, Emote, MessageChannel)
     */
    default boolean canInteract(Member issuer)
    {
        return PermissionUtil.canInteract(issuer, this);
    }

    /**
     * Whether the specified Member can interact with this Emote within the provided MessageChannel
     * <br>Same logic as {@link #canInteract(User, MessageChannel, boolean) canInteract(issuer, channel, true)}!
     *
     * @param  issuer
     *         The User to test
     * @param  channel
     *         The MessageChannel to test
     *
     * @return True, if the provided Member can use this Emote
     *
     * @see    net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Emote)
     * @see    net.dv8tion.jda.core.utils.PermissionUtil#canInteract(User, Emote, MessageChannel)
     */
    default boolean canInteract(User issuer, MessageChannel channel)
    {
        return PermissionUtil.canInteract(issuer, this, channel);
    }

    /**
     * Whether the specified Member can interact with this Emote within the provided MessageChannel
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
     *
     * @see    net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Emote)
     * @see    net.dv8tion.jda.core.utils.PermissionUtil#canInteract(User, Emote, MessageChannel, boolean)
     */
    default boolean canInteract(User issuer, MessageChannel channel, boolean botOverride)
    {
        return PermissionUtil.canInteract(issuer, this, channel, botOverride);
    }
}
