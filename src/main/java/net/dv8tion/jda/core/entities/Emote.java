/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
 */
public interface Emote extends ISnowflake, IMentionable, IFakeable
{

    /**
     * {@link net.dv8tion.jda.core.entities.Guild Guild} this emote is attached to.<p>
     * <b>This is null if the emote is fake (retrieved from a Message)</b>
     *
     * @return
     *      Guild of this emote or null if it is a fake entity
     */
    Guild getGuild();

    /**
     * Roles this emote is active for (<a href="https://discordapp.com/developers/docs/resources/guild#emoji-object">source</a>)
     *
     * @return
     *      An immutable list of the roles this emote is active for (all roles if empty)
     * @throws IllegalStateException
     *      if this emotes is fake
     */
    List<Role> getRoles();

    /**
     * The name of this emote
     *
     * @return
     *      String representation of this emote's name
     */
    String getName();

    /**
     * Whether this emote is managed (<a href="https://discordapp.com/developers/docs/resources/guild#emoji-object">source</a>)
     *
     * @return
     *      True, if this emote is managed
     */
    boolean isManaged();

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Emote
     *
     * @return
     *      The JDA instance of this Emote
     */
    JDA getJDA();

    /**
     * Deletes this Emote.
     *
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: {@link java.lang.Void}<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: None
     * @throws IllegalStateException
     *      if this Emote is fake
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *      if the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_EMOTES MANAGE_EMOTES} is not given
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *      if the current account is not from {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType#CLIENT}
     */
    RestAction<Void> delete();

    /**
     * The {@link net.dv8tion.jda.client.managers.EmoteManager Manager} for this emote<p>
     * This will only work for the client account type.<br>
     * With the EmoteManager returned you can modify this Emote's properties or delete it.
     *
     * @return
     *      The EmoteManager for this Emote
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *      if this is not used with {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType#CLIENT}
     * @throws IllegalStateException
     *      if this emote is fake
     */
    EmoteManager getManager();

    /**
     * An <b>updatable</b> manager for this Emote.<p>
     * This will only work for the client account type.<br>
     * With the EmoteManager returned you can modify this Emote's properties or delete it.<p>
     * This specific Manager is used to modify multiple properties at once by setting the property and calling {@link EmoteManagerUpdatable#update()}
     *
     * @return
     *      The EmoteManagerUpdatable for this Emote
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *      if this is not used with {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType#CLIENT}
     * @throws IllegalStateException
     *      if this emote is fake
     */
    EmoteManagerUpdatable getManagerUpdatable();

    /**
     * A String representation of the URL which leads to image displayed within the official Discord&trade; client
     * when this Emote is used
     *
     * @return
     *      Discord CDN link to the Emote's image
     */
    default String getImageUrl()
    {
        return "https://cdn.discordapp.com/emojis/" + getId() + ".png";
    }

    /**
     * Usable representation of this Emote (used to display in the client just like mentions with a specific format)
     * <br>
     * Emotes are used with the format <code>&lt;:{@link #getName getName()}:{@link #getId getId()}&gt;</code>
     *
     * @return
     *      A usable String representation for this Emote
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#message-formatting">Message Formatting</a>
     */
    @Override
    default String getAsMention()
    {
        return "<:" + getName() + ":" + getId() + ">";
    }

    /**
     * Whether the specified Member can interact with this Emote
     *
     * @param issuer
     *      The User to test
     * @return
     *      True, if the provided Member can use this Emote
     * @see net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Emote)
     * @see net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Emote, MessageChannel)
     */
    default boolean canInteract(Member issuer)
    {
        return PermissionUtil.canInteract(issuer, this);
    }

    /**
     * Whether the specified Member can interact with this Emote within the provided MessageChannel
     *
     * @param issuer
     *      The User to test
     * @param channel
     *      The MessageChannel to test
     * @return
     *      True, if the provided Member can use this Emote
     * @see net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Emote)
     * @see net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Emote, MessageChannel)
     */
    default boolean canInteract(User issuer, MessageChannel channel)
    {
        return PermissionUtil.canInteract(issuer, this, channel);
    }
}
