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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.MessageActivity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class DataMessage extends AbstractMessage
{
    private final EnumSet<MentionType> allowedMentions;
    private final String[] mentionedRoles;
    private final String[] mentionedUsers;
    private final LayoutComponent[] components;
    private Collection<? extends MessageEmbed> embeds;

    public DataMessage(boolean tts, String content, String nonce, Collection<? extends MessageEmbed> embeds,
                       EnumSet<MentionType> allowedMentions, String[] mentionedUsers, String[] mentionedRoles, LayoutComponent[] components)
    {
        super(content, nonce, tts);
        this.embeds = embeds;
        this.allowedMentions = allowedMentions;
        this.mentionedUsers = mentionedUsers;
        this.mentionedRoles = mentionedRoles;
        this.components = components;
    }

    public DataMessage(boolean tts, String content, String nonce, Collection<? extends MessageEmbed> embeds)
    {
        this(tts, content, nonce, embeds, null, new String[0], new String[0], new LayoutComponent[0]);
    }

    public EnumSet<MentionType> getAllowedMentions()
    {
        return allowedMentions;
    }

    public String[] getMentionedRolesWhitelist()
    {
        return mentionedRoles;
    }

    public String[] getMentionedUsersWhitelist()
    {
        return mentionedUsers;
    }

    @Nonnull
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
            && Objects.equals(other.embeds, embeds);
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

    public DataMessage setEmbeds(Collection<? extends MessageEmbed> embeds)
    {
        this.embeds = embeds;
        return this;
    }

    @Nonnull
    @Override
    public List<MessageEmbed> getEmbeds()
    {
        return embeds == null ? Collections.emptyList() : new ArrayList<>(embeds);
    }

    @Nonnull
    @Override
    public List<ActionRow> getActionRows()
    {
        return components == null ? Collections.emptyList()
                : Arrays.stream(components)
                    .filter(ActionRow.class::isInstance)
                    .map(ActionRow.class::cast)
                    .collect(Collectors.toList());
    }

    // UNSUPPORTED OPERATIONS ON MESSAGE BUILDER OUTPUT

    @Override
    protected void unsupported()
    {
        throw new UnsupportedOperationException("This operation is not supported for Messages that were created by a MessageBuilder!");
    }

    @Nullable
    @Override
    public MessageActivity getActivity()
    {
        unsupported();
        return null;
    }

    @Override
    public long getIdLong()
    {
        unsupported();
        return 0;
    }
}
