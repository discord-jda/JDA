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
package net.dv8tion.jda.api.events.message.guild;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a Message is received in a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.
 * 
 * <p>Can be used to retrieve the affected TextChannel and Message.
 *
 * <h2>Requirements</h2>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MESSAGES GUILD_MESSAGES} intent to be enabled.
 */
//TODO-v5: directly relies on TextChannel but actually should rely on GuildMessageChannel. However, we're likely killing the Guild/Private specific events, so not gonna update for now.
public class GuildMessageReceivedEvent extends GenericGuildMessageEvent
{
    private final Message message;

    public GuildMessageReceivedEvent(@Nonnull JDA api, long responseNumber, @Nonnull Message message)
    {
        super(api, responseNumber, message.getIdLong(), message.getTextChannel());
        this.message = message;
    }

    /**
     * The received {@link net.dv8tion.jda.api.entities.Message Message} object.
     *
     * @return The received {@link net.dv8tion.jda.api.entities.Message Message} object.
     */
    @Nonnull
    public Message getMessage()
    {
        return message;
    }

    /**
     * The Author of the Message received as {@link net.dv8tion.jda.api.entities.User User} object.
     * <br>This will be never-null but might be a fake User if Message was sent via Webhook.
     * See {@link Webhook#getDefaultUser()}.
     *
     * @return The Author of the Message.
     *
     * @see    #isWebhookMessage()
     */
    @Nonnull
    public User getAuthor()
    {
        return message.getAuthor();
    }

    /**
     * The Author of the Message received as {@link net.dv8tion.jda.api.entities.Member Member} object.
     * <br>This will be {@code null} in case of {@link #isWebhookMessage() isWebhookMessage()} returning {@code true}.
     *
     * @return The Author of the Message as Member object.
     *
     * @see    #isWebhookMessage()
     */
    @Nullable
    public Member getMember()
    {
        return message.getMember();
    }

    /**
     * Whether or not the Message received was sent via a Webhook.
     * <br>This is a shortcut for {@code getMessage().isWebhookMessage()}.
     *
     * @return Whether or not the Message was sent via Webhook
     */
    public boolean isWebhookMessage()
    {
        return getMessage().isWebhookMessage();
    }
}
