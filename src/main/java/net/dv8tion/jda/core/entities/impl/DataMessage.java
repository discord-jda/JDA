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

package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

public class DataMessage extends ReceivedMessage implements Message
{
    private static final String UNSUPPORTED = "This operation is not supported for Messages that were created by a MessageBuilder!";
    private MessageEmbed embed;

    public DataMessage(boolean tts, String content, String nonce, MessageEmbed embed)
    {
        super(0, null, null, false, content.contains("@everyone"), tts, false,
            content, nonce, null, null, Collections.emptyList(), Collections.emptyList(),
            embed != null ? Collections.singletonList(embed) : Collections.emptyList());
        this.embed = embed;
    }

    @Override
    public MessageType getType()
    {
        return MessageType.DEFAULT;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof DataMessage))
            return false;
        DataMessage other = (DataMessage) o;
        return other.getContentRaw().equals(getContentRaw());
    }

    @Override
    public int hashCode()
    {
        return System.identityHashCode(this);
    }

    @Override
    public String toString()
    {
        return String.format("DataMessage(%.30s)", getContentRaw());
    }

    public DataMessage setEmbed(MessageEmbed embed)
    {
        this.embed = embed;
        return this;
    }

    @Override
    public JSONObject toJSONObject()
    {
        JSONObject obj = new JSONObject();
        obj.put("content", content);
        obj.put("tts", isTTS);
        if (embed != null)
            obj.put("embed", embed.toJSONObject());
        if (nonce != null)
            obj.put("nonce", nonce);
        return obj;
    }

    // UNSUPPORTED OPERATIONS ON MESSAGE BUILDER OUTPUT


    @Override
    public synchronized List<User> getMentionedUsers()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public synchronized List<TextChannel> getMentionedChannels()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public synchronized List<Role> getMentionedRoles()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public synchronized List<Emote> getEmotes()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public List<Member> getMentionedMembers(Guild guild)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public List<Member> getMentionedMembers()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public List<IMentionable> getMentions(MentionType... types)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public boolean isMentioned(IMentionable mentionable, MentionType... types)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public User getAuthor()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public Member getMember()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public String getContentDisplay()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public boolean isFromType(ChannelType type)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public ChannelType getChannelType()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public MessageChannel getChannel()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public PrivateChannel getPrivateChannel()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public Group getGroup()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public TextChannel getTextChannel()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public Guild getGuild()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public MessageAction editMessage(CharSequence newContent)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public MessageAction editMessage(MessageEmbed newContent)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public MessageAction editMessageFormat(String format, Object... args)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public MessageAction editMessage(Message newContent)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public AuditableRestAction<Void> delete()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public JDA getJDA()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public RestAction<Void> pin()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public RestAction<Void> unpin()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public RestAction<Void> addReaction(Emote emote)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public RestAction<Void> addReaction(String unicode)
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public AuditableRestAction<Void> clearReactions()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public long getIdLong()
    {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }
}
