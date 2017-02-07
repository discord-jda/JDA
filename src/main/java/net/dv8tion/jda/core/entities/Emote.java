/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
import net.dv8tion.jda.client.managers.EmoteManagerUpdatable;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.List;

/**
 * Represents a Custom Emote. (Emoji in official Discord API terminology)
 *
 * @since  2.2
 * @author Florian Spieß
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
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         if the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_EMOTES MANAGE_EMOTES} is not given
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *         if the current account is not from {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType#CLIENT}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Void
     *         The RestAction to delete this Emote.
     */
    RestAction<Void> delete();

    /**
     * The {@link net.dv8tion.jda.client.managers.EmoteManager Manager} for this emote, used to modify
     * properties of the emote like name and role restrictions.
     *
     * <p>This will only work for {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT}.
     *
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *         if this is not used with {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType#CLIENT}
     * @throws IllegalStateException
     *         if this emote is fake
     *
     * @return The EmoteManager for this Emote
     */
    EmoteManager getManager();

    /**
     * An <b>updatable</b> manager for this Emote, used to modify properties of the emote like name and role restrictions.
     *
     * <p>This will only work for the client account type.
     *
     * <p>This specific Manager is used to modify multiple properties at once
     * by setting the property and calling {@link EmoteManagerUpdatable#update()}
     *
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *         if this is not used with {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType#CLIENT}
     * @throws IllegalStateException
     *         if this emote is fake
     *
     * @return The EmoteManagerUpdatable for this Emote
     */
    EmoteManagerUpdatable getManagerUpdatable();

    /**
     * A String representation of the URL which leads to image displayed within the official Discord&trade; client
     * when this Emote is used
     *
     * @return Discord CDN link to the Emote's image
     */
    default String getImageUrl()
    {
        return "https://cdn.discordapp.com/emojis/" + getId() + ".png";
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
        return "<:" + getName() + ":" + getId() + ">";
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
}
