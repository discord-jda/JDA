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

import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DataMessage extends AbstractMessage
{
    private MessageEmbed embed;
    private Activity activity;

    public DataMessage(boolean tts, String content, String nonce, MessageEmbed embed, Activity activity)
    {
        super(content, nonce, tts);
        this.embed = embed;
        this.activity = activity;
    }

    @Override
    public MessageType getType()
    {
        return MessageType.DEFAULT;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof DataMessage))
            return false;
        DataMessage other = (DataMessage) o;
        return isTTS == other.isTTS
            && other.content.equals(content)
            && Objects.equals(other.nonce, nonce)
            && Objects.equals(other.embed, embed);
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

    public DataMessage setActivity(Activity activity)
    {
        this.activity = activity;
        return this;
    }

    @Override
    public List<MessageEmbed> getEmbeds()
    {
        return embed == null ? Collections.emptyList() : Collections.singletonList(embed);
    }

    @Override
    public Activity getActivity()
    {
        return activity;
    }

    // UNSUPPORTED OPERATIONS ON MESSAGE BUILDER OUTPUT

    @Override
    protected void unsupported()
    {
        throw new UnsupportedOperationException("This operation is not supported for Messages that were created by a MessageBuilder!");
    }

    @Override
    public long getIdLong()
    {
        unsupported();
        return 0;
    }
}
