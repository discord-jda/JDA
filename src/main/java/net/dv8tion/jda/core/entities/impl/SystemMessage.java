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

package net.dv8tion.jda.core.entities.impl;

import gnu.trove.set.TLongSet;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

import java.time.OffsetDateTime;
import java.util.List;

public class SystemMessage extends ReceivedMessage
{
    public SystemMessage(
        long id, MessageChannel channel, MessageType type,
        boolean fromWebhook, boolean mentionsEveryone, TLongSet mentionedUsers, TLongSet mentionedRoles,
        boolean tts, boolean pinned,
        String content, String nonce, User author, OffsetDateTime editTime,
        List<MessageReaction> reactions, List<Attachment> attachments, List<MessageEmbed> embeds)
    {
        super(id, channel, type, fromWebhook, mentionsEveryone, mentionedUsers, mentionedRoles,
            tts, pinned, content, nonce, author, editTime, reactions, attachments, embeds);
    }

    @Override
    public RestAction<Void> pin()
    {
        throw new UnsupportedOperationException("Cannot pin message of this Message Type. MessageType: " + getType());
    }

    @Override
    public RestAction<Void> unpin()
    {
        throw new UnsupportedOperationException("Cannot unpin message of this Message Type. MessageType: " + getType());
    }

    @Override
    public RestAction<Void> addReaction(Emote emote)
    {
        throw new UnsupportedOperationException("Cannot add reactions to message of this Message Type. MessageType: " + getType());
    }

    @Override
    public RestAction<Void> addReaction(String unicode)
    {
        throw new UnsupportedOperationException("Cannot add reactions to message of this Message Type. MessageType: " + getType());
    }

    @Override
    public RestAction<Void> clearReactions()
    {
        throw new UnsupportedOperationException("Cannot clear reactions for message of this Message Type. MessageType: " + getType());
    }

    @Override
    public MessageAction editMessage(CharSequence newContent)
    {
        throw new UnsupportedOperationException("Cannot edit message of this Message Type. MessageType: " + getType());
    }

    @Override
    public MessageAction editMessage(MessageEmbed newContent)
    {
        throw new UnsupportedOperationException("Cannot edit message of this Message Type. MessageType: " + getType());
    }

    @Override
    public MessageAction editMessageFormat(String format, Object... args)
    {
        throw new UnsupportedOperationException("Cannot edit message of this Message Type. MessageType: " + getType());
    }

    @Override
    public MessageAction editMessage(Message newContent)
    {
        throw new UnsupportedOperationException("Cannot edit message of this Message Type. MessageType: " + getType());
    }

    @Override
    public String toString()
    {
        return "M:[" + type + ']' + author + '(' + id + ')';
    }
}
