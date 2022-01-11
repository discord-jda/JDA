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

package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.api.entities.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manager providing functionality to update one or more fields for a {@link net.dv8tion.jda.api.entities.Webhook Webhook}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setName("GitHub Webhook")
 *        .setChannel(channel)
 *        .queue();
 * manager.reset(WebhookManager.NAME | WebhookManager.AVATAR)
 *        .setName("Meme Feed")
 *        .setAvatar(null)
 *        .queue();
 * }</pre>
 *
 * @see net.dv8tion.jda.api.entities.Webhook#getManager()
 */
public interface WebhookManager extends Manager<WebhookManager>
{
    /** Used to reset the name field */
    long NAME    = 1;
    /** Used to reset the channel field */
    long CHANNEL = 1 << 1;
    /** Used to reset the avatar field */
    long AVATAR  = 1 << 2;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(WebhookManager.CHANNEL | WebhookManager.NAME);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #AVATAR}</li>
     *     <li>{@link #CHANNEL}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return WebhookManager for chaining convenience
     */
    @Nonnull
    @Override
    WebhookManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(WebhookManager.CHANNEL, WebhookManager.NAME);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #AVATAR}</li>
     *     <li>{@link #CHANNEL}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return WebhookManager for chaining convenience
     */
    @Nonnull
    @Override
    WebhookManager reset(long... fields);

    /**
     * The target {@link net.dv8tion.jda.api.entities.Webhook Webhook}
     * that will be modified by this manager
     *
     * @return The target {@link net.dv8tion.jda.api.entities.Webhook Webhook}
     */
    @Nonnull
    Webhook getWebhook();

    /**
     * The {@link net.dv8tion.jda.api.entities.BaseGuildMessageChannel BaseGuildMessageChannel} this Manager's
     * {@link net.dv8tion.jda.api.entities.Webhook Webhook} is in.
     * <br>This is logically the same as calling {@code getWebhook().getChannel()}
     *
     * @return The parent {@link net.dv8tion.jda.api.entities.BaseGuildMessageChannel BaseGuildMessageChannel}
     */
    @Nonnull
    default BaseGuildMessageChannel getChannel()
    {
        return getWebhook().getChannel();
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} this Manager's
     * {@link net.dv8tion.jda.api.entities.Webhook Webhook} is in.
     * <br>This is logically the same as calling {@code getWebhook().getGuild()}
     *
     * @return The parent {@link net.dv8tion.jda.api.entities.Guild Guild}
     */
    @Nonnull
    default Guild getGuild()
    {
        return getWebhook().getGuild();
    }

    /**
     * Sets the <b><u>default name</u></b> of the selected {@link net.dv8tion.jda.api.entities.Webhook Webhook}.
     *
     * <p>A webhook name <b>must not</b> be {@code null} or blank!
     *
     * @param  name
     *         The new default name for the selected {@link net.dv8tion.jda.api.entities.Webhook Webhook}
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or blank
     *
     * @return WebhookManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    WebhookManager setName(@Nonnull String name);

    /**
     * Sets the <b><u>default avatar</u></b> of the selected {@link net.dv8tion.jda.api.entities.Webhook Webhook}.
     *
     * @param  icon
     *         The new default avatar {@link net.dv8tion.jda.api.entities.Icon Icon}
     *         for the selected {@link net.dv8tion.jda.api.entities.Webhook Webhook}
     *         or {@code null} to reset
     *
     * @return WebhookManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    WebhookManager setAvatar(@Nullable Icon icon);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} of the selected {@link net.dv8tion.jda.api.entities.Webhook Webhook}.
     *
     * <p>A webhook channel <b>must not</b> be {@code null} and <b>must</b> be in the same {@link net.dv8tion.jda.api.entities.Guild Guild}!
     *
     * @param  channel
     *         The new {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *         for the selected {@link net.dv8tion.jda.api.entities.Webhook Webhook}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.api.Permission#MANAGE_WEBHOOKS MANAGE_WEBHOOKS}
     *         in the specified TextChannel
     * @throws IllegalArgumentException
     *         If the provided channel is {@code null} or from a different Guild
     *
     * @return WebhookManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    WebhookManager setChannel(@Nonnull TextChannel channel);
}
