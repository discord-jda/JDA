/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.events.message;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.message.PinMessage;

/**
 * Fired when a message was pinned to a channel and Discord reacts by
 * sending a system message to the specific channel.
 */
public class PinMessageEvent extends GenericSystemMessageEvent
{
    public PinMessageEvent(JDA api, long responseNumber, PinMessage message, MessageChannel channel)
    {
        super(api, responseNumber, message, channel);
    }

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} that this message was sent in.
     * <br><b>This is only valid if the Message was actually sent in a PrivateChannel.</b> This will return {@code null}
     * if it was not sent from a PrivateChannel.
     * <br>You can check the type of channel this message was sent from using {@link #isFromType(net.dv8tion.jda.core.entities.ChannelType)} or {@link #getChannelType()}.
     *
     * <p>Use {@link #getChannel()} for an ambiguous {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     * if you do not need functionality specific to {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}.
     *
     * @return The PrivateChannel this message was sent in, or {@code null} if it was not sent from a PrivateChannel.
     */
    public PrivateChannel getPrivateChannel()
    {
        return isFromType(ChannelType.PRIVATE) ? (PrivateChannel) channel : null;
    }

    /**
     * Returns the {@link net.dv8tion.jda.client.entities.Group Group} that this message was sent in.
     * <br><b>This is only valid if the Message was actually sent in a Group.</b> This will return {@code null}
     * if it was not sent from a Group.
     * <br>You can check the type of channel this message was sent from using {@link #isFromType(ChannelType)} or {@link #getChannelType()}.
     *
     * <p>Use {@link #getChannel()} for an ambiguous {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     * if you do not need functionality specific to {@link net.dv8tion.jda.client.entities.Group Group}.
     *
     * @return The Group this message was sent in, or {@code null} if it was not sent from a Group.
     */
    public Group getGroup()
    {
        return isFromType(ChannelType.GROUP) ? (Group) channel : null;
    }

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that this message was sent in.
     * <br><b>This is only valid if the Message was actually sent in a TextChannel.</b> This will return {@code null}
     * if it was not sent from a TextChannel.
     * <br>You can check the type of channel this message was sent from using {@link #isFromType(ChannelType)} or {@link #getChannelType()}.
     *
     * <p>Use {@link #getChannel()} for an ambiguous {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     * if you do not need functionality specific to {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     *
     * @return The TextChannel this message was sent in, or {@code null} if it was not sent from a TextChannel.
     */
    public TextChannel getTextChannel()
    {
        return isFromType(ChannelType.TEXT) ? (TextChannel) channel : null;
    }

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.Guild Guild} that this message was sent in.
     * <br>This is just a shortcut to {@link #getTextChannel()}{@link net.dv8tion.jda.core.entities.TextChannel#getGuild() .getGuild()}.
     * <br><b>This is only valid if the Message was actually sent in a TextChannel.</b> This will return {@code null}
     * if it was not sent from a TextChannel.
     * <br>You can check the type of channel this message was sent from using {@link #isFromType(ChannelType)} or {@link #getChannelType()}.
     *
     * @return The Guild this message was sent in, or {@code null} if it was not sent from a TextChannel.
     */
    public Guild getGuild()
    {
        return isFromType(ChannelType.TEXT) ? getTextChannel().getGuild() : null;
    }

    @Override
    public PinMessage getMessage()
    {
        return (PinMessage) message;
    }
}
