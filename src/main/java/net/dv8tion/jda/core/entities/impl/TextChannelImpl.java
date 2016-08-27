/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageHistory;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.RestAction;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TextChannelImpl implements TextChannel
{
    private final String id;
    private final Guild guild;
    private final HashMap<Member, PermissionOverride> memberOverrides = new HashMap<>();
    private final HashMap<Role, PermissionOverride> roleOverrides = new HashMap<>();

    private String name;
    private String topic;
    private int rawPosition;

    public TextChannelImpl(String id, Guild guild)
    {
        this.id = id;
        this.guild = guild;
    }

    @Override
    public String getAsMention()
    {
        return "<#" + getId() + '>';
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public RestAction deleteMessages(Collection<Message> messages)
    {
        return null;
    }

    @Override
    public RestAction deleteMessagesByIds(Collection<String> messageIds)
    {
        return null;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getTopic()
    {
        return topic;
    }

    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    public List<Member> getMembers()
    {
        return Collections.unmodifiableList(
        ((GuildImpl) getGuild()).getMembersMap().values().stream()
                .filter(m -> m.getPermissions(this).contains(Permission.MESSAGE_READ))
                .collect(Collectors.toList()));
    }

    @Override
    public int getPosition()
    {
        List<TextChannel> channels = guild.getTextChannels();
        for (int i = 0; i < channels.size(); i++)
        {
            if (channels.get(i) == this)
                return i;
        }
        throw new RuntimeException("Somehow when determining position we never found the TextChannel in the Guild's channels? wtf?");
    }

    @Override
    public int getPositionRaw()
    {
        return rawPosition;
    }

    @Override
    public boolean checkPermission(User user, Permission permission)
    {
        return false;
    }

    @Override
    public JDA getJDA()
    {
        return guild.getJDA();
    }

    @Override
    public RestAction sendMessage(String text)
    {
        return null;
    }

    @Override
    public RestAction sendMessage(Message msg)
    {
        return null;
    }

    @Override
    public RestAction sendFile(File file, Message message)
    {
        return null;
    }

    @Override
    public RestAction getMessageById(String messageId)
    {
        return null;
    }

    @Override
    public RestAction deleteMessageById(String messageId)
    {
        return null;
    }

    @Override
    public MessageHistory getHistory()
    {
        return null;
    }

    @Override
    public RestAction sendTyping()
    {
        return null;
    }

    @Override
    public RestAction pinMessageById(String messageId)
    {
        return null;
    }

    @Override
    public RestAction unpinMessageById(String messageId)
    {
        return null;
    }

    @Override
    public RestAction getPinnedMessages()
    {
        return null;
    }

    @Override
    public PermissionOverride getOverrideForMember(Member member)
    {
        return memberOverrides.get(member);
    }

    @Override
    public PermissionOverride getOverrideForRole(Role role)
    {
        return roleOverrides.get(role);
    }

    @Override
    public List<PermissionOverride> getPermissionOverrides()
    {
        List<PermissionOverride> overrides = new ArrayList<>();
        overrides.addAll(memberOverrides.values());
        overrides.addAll(roleOverrides.values());
        return Collections.unmodifiableList(overrides);
    }

    @Override
    public List<PermissionOverride> getMemberPermissionOverrides()
    {
        return Collections.unmodifiableList(new ArrayList<>(memberOverrides.values()));
    }

    @Override
    public List<PermissionOverride> getRolePermissionOverrides()
    {
        return Collections.unmodifiableList(new ArrayList<>(roleOverrides.values()));
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof TextChannel))
            return false;
        TextChannel oTChannel = (TextChannel) o;
        return this == oTChannel || this.getId().equals(oTChannel.getId());
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }

    @Override
    public String toString()
    {
        return "TC:" + getName() + '(' + getId() + ')';
    }

    @Override
    public int compareTo(TextChannel chan)
    {
        if (this == chan)
            return 0;

        if (this.getGuild() != chan.getGuild())
            throw new IllegalArgumentException("Cannot compare TextChannels that aren't from the same guild!");

        if (this.getPositionRaw() != chan.getPositionRaw())
            return chan.getPositionRaw() - this.getPositionRaw();

        OffsetDateTime thisTime = this.getCreationTime();
        OffsetDateTime chanTime = chan.getCreationTime();

        //We compare the provided channel's time to this's time instead of the reverse as one would expect due to how
        // discord deals with hierarchy. The more recent a channel was created, the lower its hierarchy ranking when
        // it shares the same position as another channel.
        return chanTime.compareTo(thisTime);
    }

    // -- Setters --

    public TextChannelImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public TextChannelImpl setTopic(String topic)
    {
        this.topic = topic;
        return this;
    }

    public TextChannelImpl setRawPosition(int rawPosition)
    {
        this.rawPosition = rawPosition;
        return this;
    }

    // -- Map Getters --

    public HashMap<Member, PermissionOverride> getMemberOverrideMap()
    {
        return memberOverrides;
    }

    public HashMap<Role, PermissionOverride> getRoleOverrideMap()
    {
        return roleOverrides;
    }
}
