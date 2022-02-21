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

package net.dv8tion.jda.api.utils;

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.internal.utils.AllowedMentionsImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumSet;

/**
 * Represents the operations used to whitelist/blacklist mentions.
 *
 * @param <R>
 *        The entity that implements this interface, used for fluid interface returns
 */
public interface AllowedMentions<R>
{
    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Message.MentionType MentionTypes} that should be parsed by default.
     * This just sets the default for all RestActions and can be overridden on a per-action basis using {@link #allowedMentions(Collection)}.
     * <br>If a message is sent with an empty Set of MentionTypes, then it will not ping any User, Role or {@code @everyone}/{@code @here},
     * while still showing up as mention tag.
     *
     * <p>If {@code null} is provided to this method, then all Types will be pingable
     * (unless whitelisting via one of the {@code mention*} methods is used).
     *
     * <h4>Example</h4>
     * <pre>{@code
     * // Disable EVERYONE and HERE mentions by default (to avoid mass ping)
     * EnumSet<Message.MentionType> deny = EnumSet.of(Message.MentionType.EVERYONE, Message.MentionType.HERE);
     * AllowedMentions.setDefaultMentions(EnumSet.complementOf(deny));
     * }</pre>
     *
     * @param  allowedMentions
     *         MentionTypes that are allowed to being parsed and pinged. {@code null} to disable and allow all mentions.
     */
    static void setDefaultMentions(@Nullable Collection<Message.MentionType> allowedMentions)
    {
        AllowedMentionsImpl.setDefaultMentions(allowedMentions);
    }

    /**
     * Returns the default {@link net.dv8tion.jda.api.entities.Message.MentionType MentionTypes} previously set by
     * {@link #setDefaultMentions(Collection) AllowedMentions.setDefaultMentions(Collection)}.
     *
     * @return Default mentions set by AllowedMentions.setDefaultMentions(Collection)
     */
    @Nonnull
    static EnumSet<Message.MentionType> getDefaultMentions()
    {
        return AllowedMentionsImpl.getDefaultMentions();
    }

    /**
     * Sets the default value for {@link #mentionRepliedUser(boolean)}
     *
     * <p>Default: <b>true</b>
     *
     * @param mention
     *        True, if replies should mention by default
     */
    static void setDefaultMentionRepliedUser(boolean mention)
    {
        AllowedMentionsImpl.setDefaultMentionRepliedUser(mention);
    }

    /**
     * Returns the default mention behavior for replies.
     * <br>If this is {@code true} then all replies will mention the author of the target message by default.
     * You can specify this individually with {@link #mentionRepliedUser(boolean)} for each message.
     *
     * <p>Default: <b>true</b>
     *
     * @return True, if replies mention by default
     */
    static boolean isDefaultMentionRepliedUser()
    {
        return AllowedMentionsImpl.isDefaultMentionRepliedUser();
    }

    /**
     * Whether to mention the used, when replying to a message.
     * <br>This only matters in combination with {@link net.dv8tion.jda.api.requests.restaction.MessageAction#reference(Message)} and {@link net.dv8tion.jda.api.requests.restaction.MessageAction#referenceById(long)}!
     *
     * <p>This is true by default but can be configured using {@link #setDefaultMentionRepliedUser(boolean)}!
     *
     * @param  mention
     *         True, to mention the author if the referenced message
     *
     * @return Updated Action for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    R mentionRepliedUser(boolean mention);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Message.MentionType MentionTypes} that should be parsed.
     * <br>If a message is sent with an empty Set of MentionTypes, then it will not ping any User, Role or {@code @everyone}/{@code @here},
     * while still showing up as mention tag.
     * <p>
     * If {@code null} is provided to this method, then all Types will be pingable
     * (unless whitelisting via one of the {@code mention*} methods is used).
     * <p>
     * Note: A default for this can be set using {@link #setDefaultMentions(Collection) AllowedMentions.setDefaultMentions(Collection)}.
     *
     * @param  allowedMentions
     *         MentionTypes that are allowed to being parsed and pinged. {@code null} to disable and allow all mentions.
     *
     * @return Updated Action for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    R allowedMentions(@Nullable Collection<Message.MentionType> allowedMentions);

    /**
     * Used to provide a whitelist for {@link net.dv8tion.jda.api.entities.User Users}, {@link net.dv8tion.jda.api.entities.Member Members}
     * and {@link net.dv8tion.jda.api.entities.Role Roles} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     * <br>On other types of {@link net.dv8tion.jda.api.entities.IMentionable IMentionable}, this does nothing.
     *
     * <p><b>Note:</b> When a User/Member is whitelisted this way, then parsing of User mentions is automatically disabled (same applies to Roles).
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link #setDefaultMentions(Collection)} or {@link #allowedMentions(Collection)}.
     *
     * @param  mentions
     *         Users, Members and Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return Updated Action for chaining convenience
     *
     * @see    #allowedMentions(Collection)
     * @see    #setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    R mention(@Nonnull IMentionable... mentions);

    /**
     * Used to provide a whitelist for {@link net.dv8tion.jda.api.entities.User Users}, {@link net.dv8tion.jda.api.entities.Member Members}
     * and {@link net.dv8tion.jda.api.entities.Role Roles} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     * <br>On other types of {@link net.dv8tion.jda.api.entities.IMentionable IMentionable}, this does nothing.
     *
     * <p><b>Note:</b> When a User/Member is whitelisted this way, then parsing of User mentions is automatically disabled (same applies to Roles).
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link #setDefaultMentions(Collection)} or {@link #allowedMentions(Collection)}.
     *
     * @param  mentions
     *         Users, Members and Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return Updated Action for chaining convenience
     *
     * @see    #allowedMentions(Collection)
     * @see    #setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    default R mention(@Nonnull Collection<? extends IMentionable> mentions)
    {
        Checks.noneNull(mentions, "Mention");
        return mention(mentions.toArray(new IMentionable[0]));
    }

    /**
     * Used to provide a whitelist of {@link net.dv8tion.jda.api.entities.User Users} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     * <p><b>Note:</b> When a User is whitelisted this way, then parsing of User mentions is automatically disabled.
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link #setDefaultMentions(Collection)} or {@link #allowedMentions(Collection)}.
     *
     * @param  userIds
     *         Ids of Users that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return Updated Action for chaining convenience
     *
     * @see    #allowedMentions(Collection)
     * @see    #setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    R mentionUsers(@Nonnull String... userIds);

    /**
     * Used to provide a whitelist of {@link net.dv8tion.jda.api.entities.User Users} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     * <p><b>Note:</b> When a User is whitelisted this way, then parsing of User mentions is automatically disabled.
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link #setDefaultMentions(Collection)} or {@link #allowedMentions(Collection)}.
     *
     * @param  userIds
     *         Ids of Users that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return Updated Action for chaining convenience
     *
     * @see    #allowedMentions(Collection)
     * @see    #setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    default R mentionUsers(@Nonnull long... userIds)
    {
        Checks.notNull(userIds, "UserId array");
        String[] stringIds = new String[userIds.length];
        for (int i = 0; i < userIds.length; i++)
        {
            stringIds[i] = Long.toUnsignedString(userIds[i]);
        }
        return mentionUsers(stringIds);
    }

    /**
     * Used to provide a whitelist of {@link net.dv8tion.jda.api.entities.Role Roles} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     * <p><b>Note:</b> When a Role is whitelisted this way, then parsing of Role mentions is automatically disabled.
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link #setDefaultMentions(Collection)} or {@link #allowedMentions(Collection)}.
     *
     * @param  roleIds
     *         Ids of Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return Updated Action for chaining convenience
     *
     * @see    #allowedMentions(Collection)
     * @see    #setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    R mentionRoles(@Nonnull String... roleIds);

    /**
     * Used to provide a whitelist of {@link net.dv8tion.jda.api.entities.Role Roles} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     * <p><b>Note:</b> When a Role is whitelisted this way, then parsing of Role mentions is automatically disabled.
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link #setDefaultMentions(Collection)} or {@link #allowedMentions(Collection)}.
     *
     * @param  roleIds
     *         Ids of Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return Updated Action for chaining convenience
     *
     * @see    #allowedMentions(Collection)
     * @see    #setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    default R mentionRoles(@Nonnull long... roleIds)
    {
        Checks.notNull(roleIds, "RoleId array");
        String[] stringIds = new String[roleIds.length];
        for (int i = 0; i < roleIds.length; i++)
        {
            stringIds[i] = Long.toUnsignedString(roleIds[i]);
        }
        return mentionRoles(stringIds);
    }
}
