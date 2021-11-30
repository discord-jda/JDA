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

import net.dv8tion.jda.api.entities.BaseGuildMessageChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Webhook;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * {@link net.dv8tion.jda.api.entities.Webhook Webhook} Builder system created as an extension of {@link net.dv8tion.jda.api.requests.RestAction}
 * <br>Provides an easy way to gather and deliver information to Discord to create {@link net.dv8tion.jda.api.entities.Webhook Webhooks}.
 *
 * @see net.dv8tion.jda.api.entities.TextChannel#createWebhook(String)
 */
public interface WebhookAction extends AuditableRestAction<Webhook>
{
    @Nonnull
    @Override
    WebhookAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    WebhookAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    WebhookAction deadline(long timestamp);

    /**
     * The {@link net.dv8tion.jda.api.entities.BaseGuildMessageChannel BaseGuildMessageChannel} to create this webhook in.
     *
     * @return The channel
     */
    @Nonnull
    BaseGuildMessageChannel getChannel();

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} to create this webhook in
     *
     * @return The guild
     */
    @Nonnull
    default Guild getGuild()
    {
        return getChannel().getGuild();
    }

    /**
     * Sets the <b>Name</b> for the custom Webhook User
     *
     * @param  name
     *         A not-null String name for the new Webhook user.
     *
     * @throws IllegalArgumentException
     *         If the specified name is not in the range of 2-100.
     *
     * @return The current WebhookAction for chaining convenience.
     */
    @Nonnull
    @CheckReturnValue
    WebhookAction setName(@Nonnull String name);

    /**
     * Sets the <b>Avatar</b> for the custom Webhook User
     *
     * @param  icon
     *         An {@link net.dv8tion.jda.api.entities.Icon Icon} for the new avatar.
     *         Or null to use default avatar.
     *
     * @return The current WebhookAction for chaining convenience.
     */
    @Nonnull
    @CheckReturnValue
    WebhookAction setAvatar(@Nullable Icon icon);
}
