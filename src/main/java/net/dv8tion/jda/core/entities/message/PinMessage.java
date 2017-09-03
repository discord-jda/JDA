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

package net.dv8tion.jda.core.entities.message;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.entities.*;

public class PinMessage extends SystemMessage
{
    public PinMessage(User author, MessageChannel channel, long messageId, String content)
    {
        super(author, channel, messageId, content);
    }

    /**
     * The responsible {@link net.dv8tion.jda.core.entities.User User} for pinning the message.
     *
     * @return The responsible {@link net.dv8tion.jda.core.entities.User User} for pinning the message.
     */
    @Override
    public User getAuthor()
    {
        return super.getAuthor();
    }

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} that this message was sent in.
     * <br><b>This is only valid if the Message was actually sent in a PrivateChannel.</b> This will return {@code null}
     * if it was not sent from a PrivateChannel.
     * <br>You can check the type of channel this message was sent from using {@link #isFromType(ChannelType)} or {@link #getChannelType()}.
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
    public MessageType getType()
    {
        return MessageType.CHANNEL_PINNED_ADD;
    }
}
