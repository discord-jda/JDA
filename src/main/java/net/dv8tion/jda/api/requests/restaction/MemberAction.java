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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * {@link net.dv8tion.jda.api.requests.RestAction RestAction} extension
 * specifically designed to allow bots to add {@link net.dv8tion.jda.api.entities.User Users} to Guilds.
 * <br>This requires an <b>OAuth2 Access Token</b> with the scope {@code guilds.join} to work!
 *
 * @since  3.7.0
 *
 * @see    Guild#addMember(String, UserSnowflake)
 * @see    <a href="https://discord.com/developers/docs/topics/oauth2" target="_blank">Discord OAuth2 Documentation</a>
 */
public interface MemberAction extends RestAction<Void>
{
    @Nonnull
    @Override
    MemberAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    MemberAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    MemberAction deadline(long timestamp);

    /**
     * The access token
     *
     * @return The access token
     */
    @Nonnull
    String getAccessToken();

    /**
     * The id of the user who will be added by this task
     *
     * @return The id of the user
     */
    @Nonnull
    String getUserId();

    /**
     * The user associated with the id
     *
     * @return Possibly-null user associated with the id
     */
    @Nullable
    User getUser();

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} to which the
     * user will be added.
     *
     * @return The Guild
     */
    @Nonnull
    Guild getGuild();

    /**
     * Sets the nickname of the user for the guild.
     * <br>This will then be visible with {@link net.dv8tion.jda.api.entities.Member#getNickname() Member.getNickname()}.
     *
     * @param  nick
     *         The nickname, or {@code null}
     *
     * @throws IllegalArgumentException
     *         If the provided nickname is longer than 32 characters
     *
     * @return The current MemberAction for chaining
     */
    @Nonnull
    @CheckReturnValue
    MemberAction setNickname(@Nullable String nick);

    /**
     * Sets the roles of the user for the guild.
     * <br>This will then be visible with {@link net.dv8tion.jda.api.entities.Member#getRoles() Member.getRoles()}.
     *
     * @param  roles
     *         The roles, or {@code null}
     *
     * @throws IllegalArgumentException
     *         If one of the provided roles is null or not from the same guild
     *
     * @return The current MemberAction for chaining
     */
    @Nonnull
    @CheckReturnValue
    MemberAction setRoles(@Nullable Collection<Role> roles);

    /**
     * Sets the roles of the user for the guild.
     * <br>This will then be visible with {@link net.dv8tion.jda.api.entities.Member#getRoles() Member.getRoles()}.
     *
     * @param  roles
     *         The roles, or {@code null}
     *
     * @throws IllegalArgumentException
     *         If one of the provided roles is null or not from the same guild
     *
     * @return The current MemberAction for chaining
     */
    @Nonnull
    @CheckReturnValue
    MemberAction setRoles(@Nullable Role... roles);

    /**
     * Whether the user should be voice muted in the guild.
     * <br>Default: {@code false}
     *
     * @param  mute
     *         Whether the user should be voice muted in the guild.
     *
     * @return The current MemberAction for chaining
     */
    @Nonnull
    @CheckReturnValue
    MemberAction setMute(boolean mute);

    /**
     * Whether the user should be voice deafened in the guild.
     * <br>Default: {@code false}
     *
     * @param  deaf
     *         Whether the user should be voice deafened in the guild.
     *
     * @return The current MemberAction for chaining
     */
    @Nonnull
    @CheckReturnValue
    MemberAction setDeafen(boolean deaf);
}
