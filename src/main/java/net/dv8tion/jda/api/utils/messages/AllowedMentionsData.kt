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
package net.dv8tion.jda.api.utils.messages

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.Helpers
import java.util.*
import javax.annotation.Nonnull

class AllowedMentionsData : SerializableData {
    private var mentionParse: EnumSet<MentionType>? = defaultMentions
    private val mentionUsers: MutableSet<String?> = HashSet()
    private val mentionRoles: MutableSet<String?> = HashSet()
    var isMentionRepliedUser = isDefaultMentionRepliedUser
        private set

    fun clear() {
        mentionParse = defaultMentions
        mentionUsers.clear()
        mentionRoles.clear()
        isMentionRepliedUser = isDefaultMentionRepliedUser
    }

    fun copy(): AllowedMentionsData {
        val copy = AllowedMentionsData()
        copy.mentionParse = mentionParse
        copy.mentionUsers.addAll(mentionUsers)
        copy.mentionRoles.addAll(mentionRoles)
        copy.isMentionRepliedUser = isMentionRepliedUser
        return copy
    }

    fun mentionRepliedUser(mention: Boolean) {
        isMentionRepliedUser = mention
    }

    fun mention(@Nonnull mentions: Collection<IMentionable?>) {
        Checks.noneNull(mentions, "Mentionables")
        for (mentionable in mentions) {
            if (mentionable is UserSnowflake) mentionUsers.add(mentionable.id) else if (mentionable is Role) mentionRoles.add(
                mentionable.id
            )
        }
    }

    fun mentionUsers(@Nonnull userIds: Collection<String?>?) {
        Checks.noneNull(userIds, "User Id")
        mentionUsers.addAll(userIds!!)
    }

    fun mentionRoles(@Nonnull roleIds: Collection<String?>?) {
        Checks.noneNull(roleIds, "Role Id")
        mentionRoles.addAll(roleIds!!)
    }

    @get:Nonnull
    val mentionedUsers: Set<String?>
        get() = Collections.unmodifiableSet(HashSet(mentionUsers))

    @get:Nonnull
    val mentionedRoles: Set<String?>
        get() = Collections.unmodifiableSet(HashSet(mentionRoles))

    @get:Nonnull
    var allowedMentions: Collection<MentionType?>?
        get() = mentionParse!!.clone()
        set(allowedMentions) {
            mentionParse = if (allowedMentions == null) EnumSet.allOf(MentionType::class.java) else Helpers.copyEnumSet(
                MentionType::class.java, allowedMentions
            )
        }

    @Nonnull
    override fun toData(): DataObject {
        val allowedMentionsObj = DataObject.empty()
        val parsable = DataArray.empty()
        if (mentionParse != null) {
            // Add parsing options
            mentionParse!!.stream()
                .map<Any>(MentionType::getParseKey)
                .filter { obj: Any? -> Objects.nonNull(obj) }
                .distinct()
                .forEach { value: Any? -> parsable.add(value) }
        }
        if (!mentionUsers.isEmpty()) {
            // Whitelist certain users
            parsable.remove(MentionType.USER.parseKey)
            allowedMentionsObj.put("users", DataArray.fromCollection(mentionUsers))
        }
        if (!mentionRoles.isEmpty()) {
            // Whitelist certain roles
            parsable.remove(MentionType.ROLE.parseKey)
            allowedMentionsObj.put("roles", DataArray.fromCollection(mentionRoles))
        }
        allowedMentionsObj.put("replied_user", isMentionRepliedUser)
        return allowedMentionsObj.put("parse", parsable)
    }

    companion object {
        private var defaultParse = EnumSet.allOf(MentionType::class.java)
        var isDefaultMentionRepliedUser = true

        @get:Nonnull
        var defaultMentions: Collection<MentionType>?
            get() = defaultParse.clone()
            set(allowedMentions) {
                defaultParse = if (allowedMentions == null) EnumSet.allOf(
                    MentionType::class.java
                ) // Default to all mentions enabled
                else Helpers.copyEnumSet(MentionType::class.java, allowedMentions)
            }
    }
}
