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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.interactions.commands.SlashCommandReference;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.BagUtils;
import org.apache.commons.collections4.bag.HashBag;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class SelectMenuMentions implements Mentions
{
    private final DataObject resolved;
    private final JDAImpl jda;
    private final Guild guild;
    private final List<String> values;

    private List<User> cachedUsers;
    private List<Member> cachedMembers;
    private List<Role> cachedRoles;
    private List<GuildChannel> cachedChannels;

    public SelectMenuMentions(JDAImpl jda, GuildImpl guild, DataObject resolved, DataArray values)
    {
        this.jda = jda;
        this.guild = guild;
        this.resolved = resolved;
        this.values = values.stream(DataArray::getString).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return jda;
    }

    @Override
    public boolean mentionsEveryone()
    {
        return false;
    }

    @Nonnull
    @Override
    public List<User> getUsers()
    {
        if (cachedUsers != null)
            return cachedUsers;

        DataObject userMap = resolved.optObject("users").orElseGet(DataObject::empty);
        EntityBuilder builder = jda.getEntityBuilder();

        return cachedUsers = values.stream()
                .map(id -> userMap.optObject(id).orElse(null))
                .filter(Objects::nonNull)
                .map(builder::createUser)
                .collect(Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public Bag<User> getUsersBag()
    {
        return new HashBag<>(getUsers());
    }

    @Nonnull
    @Override
    public List<GuildChannel> getChannels()
    {
        if (cachedChannels != null)
            return cachedChannels;

        DataObject channelMap = resolved.optObject("channels").orElseGet(DataObject::empty);

        return cachedChannels = values.stream()
                .map(id -> channelMap.optObject(id).orElse(null))
                .filter(Objects::nonNull)
                .map(json -> jda.getGuildChannelById(ChannelType.fromId(json.getInt("type", -1)), json.getUnsignedLong("id")))
                .filter(Objects::nonNull)
                .collect(Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public Bag<GuildChannel> getChannelsBag()
    {
        return new HashBag<>(getChannels());
    }

    @Nonnull
    @Override
    public <T extends GuildChannel> List<T> getChannels(@Nonnull Class<T> clazz)
    {
        return getChannels().stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public <T extends GuildChannel> Bag<T> getChannelsBag(@Nonnull Class<T> clazz)
    {
        return new HashBag<>(getChannels(clazz));
    }

    @Nonnull
    @Override
    public List<Role> getRoles()
    {
        if (cachedRoles != null)
            return cachedRoles;

        DataObject roleMap = resolved.optObject("roles").orElseGet(DataObject::empty);

        return cachedRoles = values.stream()
                .filter(roleMap::hasKey)
                .map(jda::getRoleById)
                .filter(Objects::nonNull)
                .collect(Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public Bag<Role> getRolesBag()
    {
        return new HashBag<>(getRoles());
    }

    @Nonnull
    @Override
    public List<CustomEmoji> getCustomEmojis()
    {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public Bag<CustomEmoji> getCustomEmojisBag()
    {
        return BagUtils.emptyBag();
    }

    @Nonnull
    @Override
    public List<SlashCommandReference> getSlashCommands()
    {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public Bag<SlashCommandReference> getSlashCommandsBag()
    {
        return BagUtils.emptyBag();
    }

    @Nonnull
    @Override
    public List<Member> getMembers()
    {
        if (cachedMembers != null)
            return cachedMembers;

        DataObject memberMap = resolved.optObject("members").orElseGet(DataObject::empty);
        DataObject userMap = resolved.optObject("users").orElseGet(DataObject::empty);
        EntityBuilder builder = jda.getEntityBuilder();

        return cachedMembers = values.stream()
                .map(id -> memberMap.optObject(id).map(m -> m.put("id", id)).orElse(null))
                .filter(Objects::nonNull)
                .map(json -> json.put("user", userMap.getObject(json.getString("id"))))
                .map(json -> builder.createMember(guild, json))
                .filter(Objects::nonNull)
                .filter(member -> {
                    builder.updateMemberCache(member);
                    return true;
                })
                .collect(Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public Bag<Member> getMembersBag()
    {
        return new HashBag<>(getMembers());
    }

    @Nonnull
    @Override
    public List<IMentionable> getMentions(@Nonnull Message.MentionType... types)
    {
        if (types.length == 0)
            return getMentions(Message.MentionType.values());
        List<IMentionable> mentions = new ArrayList<>();
        // Convert to set to avoid duplicates
        EnumSet<Message.MentionType> set = EnumSet.of(types[0], types);
        for (Message.MentionType type : set)
        {
            switch (type)
            {
            case USER:
                List<Member> members = getMembers();
                List<User> users = getUsers();
                mentions.addAll(members);
                users.stream()
                     .filter(u -> members.stream().noneMatch(m -> m.getIdLong() == u.getIdLong()))
                     .forEach(mentions::add);
                break;
            case ROLE:
                mentions.addAll(getRoles());
                break;
            case CHANNEL:
                mentions.addAll(getChannels());
                break;
            }
        }

        mentions.sort(Comparator.comparingInt(it -> values.indexOf(it.getId())));
        return Collections.unmodifiableList(mentions);
    }

    @Override
    public boolean isMentioned(@Nonnull IMentionable mentionable, @Nonnull Message.MentionType... types)
    {
        Checks.notNull(types, "Mention Types");
        if (types.length == 0)
            return isMentioned(mentionable, Message.MentionType.values());

        String id = mentionable.getId();
        for (Message.MentionType type : types)
        {
            switch (type)
            {
            case USER:
                if (mentionable instanceof UserSnowflake)
                {
                    boolean mentioned = resolved.optObject("users").map(obj -> obj.hasKey(id)).orElse(false);
                    if (mentioned)
                        return true;
                }
                break;
            case ROLE:
                if (mentionable instanceof Member)
                {
                    boolean mentioned = ((Member) mentionable).getRoles().stream().anyMatch(role -> isMentioned(role, Message.MentionType.ROLE));
                    if (mentioned)
                        return true;
                }
                else if (mentionable instanceof User)
                {
                    boolean mentioned = getMembers().stream()
                            .filter(it -> it.getIdLong() == mentionable.getIdLong())
                            .findFirst()
                            .map(member -> isMentioned(member, Message.MentionType.ROLE))
                            .orElse(false);
                    if (mentioned)
                        return true;
                }
                else if (mentionable instanceof Role)
                {
                    boolean mentioned = resolved.optObject("roles").map(obj -> obj.hasKey(id)).orElse(false);
                    if (mentioned)
                        return true;
                }
                break;
            case CHANNEL:
                if (mentionable instanceof GuildChannel && getChannels().contains(mentionable))
                    return true;
                break;
            }
        }
        return false;
    }
}
