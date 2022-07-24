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

package net.dv8tion.jda.internal.utils;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class AllowedMentionsImpl implements SerializableData, AllowedMentions<AllowedMentionsImpl>
{
    private static EnumSet<Message.MentionType> defaultParse = EnumSet.allOf(Message.MentionType.class);
    private static boolean defaultMentionRepliedUser = true;
    private EnumSet<Message.MentionType> parse = getDefaultMentions();
    private final Set<String> users = new HashSet<>();
    private final Set<String> roles = new HashSet<>();
    private boolean mentionRepliedUser = defaultMentionRepliedUser;

    public static void setDefaultMentions(@Nullable Collection<Message.MentionType> allowedMentions)
    {
        defaultParse = allowedMentions == null
                ? EnumSet.allOf(Message.MentionType.class) // Default to all mentions enabled
                : Helpers.copyEnumSet(Message.MentionType.class, allowedMentions);
    }

    @Nonnull
    public static EnumSet<Message.MentionType> getDefaultMentions()
    {
        return defaultParse.clone();
    }

    public static void setDefaultMentionRepliedUser(boolean mention)
    {
        defaultMentionRepliedUser = mention;
    }

    public static boolean isDefaultMentionRepliedUser()
    {
        return defaultMentionRepliedUser;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject allowedMentionsObj = DataObject.empty();
        DataArray parsable = DataArray.empty();
        if (parse != null)
        {
            // Add parsing options
            parse.stream()
                 .map(Message.MentionType::getParseKey)
                 .filter(Objects::nonNull)
                 .distinct()
                 .forEach(parsable::add);
        }
        if (!users.isEmpty())
        {
            // Whitelist certain users
            parsable.remove(Message.MentionType.USER.getParseKey());
            allowedMentionsObj.put("users", DataArray.fromCollection(users));
        }
        if (!roles.isEmpty())
        {
            // Whitelist certain roles
            parsable.remove(Message.MentionType.ROLE.getParseKey());
            allowedMentionsObj.put("roles", DataArray.fromCollection(roles));
        }
        allowedMentionsObj.put("replied_user", mentionRepliedUser);
        return allowedMentionsObj.put("parse", parsable);
    }

    @Nonnull
    @Override
    public AllowedMentionsImpl mentionRepliedUser(boolean mention)
    {
        this.mentionRepliedUser = mention;
        return this;
    }

    @Nonnull
    @Override
    public AllowedMentionsImpl allowedMentions(@Nullable Collection<Message.MentionType> allowedMentions)
    {
        this.parse = allowedMentions == null
                ? EnumSet.allOf(Message.MentionType.class)
                : Helpers.copyEnumSet(Message.MentionType.class, allowedMentions);
        return this;
    }

    @Nonnull
    @Override
    public AllowedMentionsImpl mention(@Nonnull IMentionable... mentions)
    {
        Checks.noneNull(mentions, "Mentionables");
        for (IMentionable mentionable : mentions)
        {
            if (mentionable instanceof User || mentionable instanceof Member)
                users.add(mentionable.getId());
            else if (mentionable instanceof Role)
                roles.add(mentionable.getId());
        }
        return this;
    }

    @Nonnull
    @Override
    public AllowedMentionsImpl mentionUsers(@Nonnull String... userIds)
    {
        Checks.noneNull(userIds, "User Id");
        Collections.addAll(users, userIds);
        return this;
    }

    @Nonnull
    @Override
    public AllowedMentionsImpl mentionRoles(@Nonnull String... roleIds)
    {
        Checks.noneNull(roleIds, "Role Id");
        Collections.addAll(roles, roleIds);
        return this;
    }

    public EnumSet<Message.MentionType> getAllowedMentions()
    {
        return parse;
    }

    public Set<String> getUsers()
    {
        return users;
    }

    public Set<String> getRoles()
    {
        return roles;
    }

    public boolean isMentionRepliedUser()
    {
        return mentionRepliedUser;
    }

    public AllowedMentionsImpl copy()
    {
        AllowedMentionsImpl copy = new AllowedMentionsImpl();
        copy.parse = parse;
        copy.mentionRepliedUser = mentionRepliedUser;
        copy.users.addAll(users);
        copy.roles.addAll(roles);
        return copy;
    }
}
