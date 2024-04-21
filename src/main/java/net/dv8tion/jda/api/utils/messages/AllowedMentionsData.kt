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

package net.dv8tion.jda.api.utils.messages;

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

class AllowedMentionsData implements SerializableData
{
    private static EnumSet<Message.MentionType> defaultParse = EnumSet.allOf(Message.MentionType.class);
    private static boolean defaultMentionRepliedUser = true;

    private EnumSet<Message.MentionType> mentionParse = getDefaultMentions();
    private final Set<String> mentionUsers = new HashSet<>();
    private final Set<String> mentionRoles = new HashSet<>();
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

    public void clear()
    {
        mentionParse = getDefaultMentions();
        mentionUsers.clear();
        mentionRoles.clear();
        mentionRepliedUser = defaultMentionRepliedUser;
    }

    public AllowedMentionsData copy()
    {
        AllowedMentionsData copy = new AllowedMentionsData();
        copy.mentionParse = mentionParse;
        copy.mentionUsers.addAll(mentionUsers);
        copy.mentionRoles.addAll(mentionRoles);
        copy.mentionRepliedUser = mentionRepliedUser;
        return copy;
    }

    public void mentionRepliedUser(boolean mention)
    {
        mentionRepliedUser = mention;
    }

    public void setAllowedMentions(@Nullable Collection<Message.MentionType> allowedMentions)
    {
        this.mentionParse = allowedMentions == null
                ? EnumSet.allOf(Message.MentionType.class)
                : Helpers.copyEnumSet(Message.MentionType.class, allowedMentions);
    }

    public void mention(@Nonnull Collection<? extends IMentionable> mentions)
    {
        Checks.noneNull(mentions, "Mentionables");
        for (IMentionable mentionable : mentions)
        {
            if (mentionable instanceof UserSnowflake)
                mentionUsers.add(mentionable.getId());
            else if (mentionable instanceof Role)
                mentionRoles.add(mentionable.getId());
        }
    }

    public void mentionUsers(@Nonnull Collection<String> userIds)
    {
        Checks.noneNull(userIds, "User Id");
        mentionUsers.addAll(userIds);
    }

    public void mentionRoles(@Nonnull Collection<String> roleIds)
    {
        Checks.noneNull(roleIds, "Role Id");
        mentionRoles.addAll(roleIds);
    }

    @Nonnull
    public Set<String> getMentionedUsers()
    {
        return Collections.unmodifiableSet(new HashSet<>(mentionUsers));
    }

    @Nonnull
    public Set<String> getMentionedRoles()
    {
        return Collections.unmodifiableSet(new HashSet<>(mentionRoles));
    }

    @Nonnull
    public EnumSet<Message.MentionType> getAllowedMentions()
    {
        return mentionParse.clone();
    }

    public boolean isMentionRepliedUser()
    {
        return mentionRepliedUser;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject allowedMentionsObj = DataObject.empty();
        DataArray parsable = DataArray.empty();
        if (mentionParse != null)
        {
            // Add parsing options
            mentionParse.stream()
                    .map(Message.MentionType::getParseKey)
                    .filter(Objects::nonNull)
                    .distinct()
                    .forEach(parsable::add);
        }
        if (!mentionUsers.isEmpty())
        {
            // Whitelist certain users
            parsable.remove(Message.MentionType.USER.getParseKey());
            allowedMentionsObj.put("users", DataArray.fromCollection(mentionUsers));
        }
        if (!mentionRoles.isEmpty())
        {
            // Whitelist certain roles
            parsable.remove(Message.MentionType.ROLE.getParseKey());
            allowedMentionsObj.put("roles", DataArray.fromCollection(mentionRoles));
        }
        allowedMentionsObj.put("replied_user", mentionRepliedUser);
        return allowedMentionsObj.put("parse", parsable);
    }
}
