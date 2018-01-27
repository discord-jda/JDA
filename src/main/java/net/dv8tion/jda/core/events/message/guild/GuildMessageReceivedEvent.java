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
package net.dv8tion.jda.core.events.message.guild;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

/**
 * <b><u>GuildMessageReceivedEvent</u></b><br>
 * Fired if a Message is received in a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
 * <p>
 * Use: Retrieve affected TextChannel and Message.
 */
public class GuildMessageReceivedEvent extends GenericGuildMessageEvent
{
    private final Message message;

    public GuildMessageReceivedEvent(JDA api, long responseNumber, Message message)
    {
        super(api, responseNumber, message.getIdLong(), message.getTextChannel());
        this.message = message;
    }

    /**
     * Returns the received {@link net.dv8tion.jda.core.entities.Message Message} object.
     *
     * @return The received {@link net.dv8tion.jda.core.entities.Message Message} object.
     */
    @NonNull
    public Message getMessage()
    {
        return message;
    }

    /**
     * Returns the Author of the Message received as {@link net.dv8tion.jda.core.entities.User User} object.
     * <br>This will be never-null but might be a fake User if Message was sent via Webhook
     *
     * @return The Author of the Message.
     *
     * @see #isWebhookMessage()
     * @see net.dv8tion.jda.core.entities.User#isFake()
     */
    @NonNull
    public User getAuthor()
    {
        return message.getAuthor();
    }

    /**
     * Returns the Author of the Message received as {@link net.dv8tion.jda.core.entities.Member Member} object.
     * <br>This will be {@code null} in case of {@link #isWebhookMessage() isWebhookMessage()} returning {@code true}.
     *
     * @return The Author of the Message as Member object.
     *
     * @see #isWebhookMessage()
     */
    public Member getMember()
    {
        return isWebhookMessage() ? null : getGuild().getMember(getAuthor());
    }

    /**
     * Returns whether or not the Message received was sent via a Webhook.
     * <br>This is a shortcut for {@code getMessage().isWebhookMessage()}.
     *
     * @return Whether or not the Message was sent via Webhook
     */
    public boolean isWebhookMessage()
    {
        return getMessage().isWebhookMessage();
    }
}
