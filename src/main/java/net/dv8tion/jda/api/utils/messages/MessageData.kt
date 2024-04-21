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

import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.utils.AttachedFile
import java.util.*
import javax.annotation.Nonnull

/**
 * Represents data relevant to all message requests.
 */
open interface MessageData {
    @get:Nonnull
    val content: String

    @get:Nonnull
    val embeds: List<MessageEmbed?>

    @get:Nonnull
    val components: List<LayoutComponent?>

    // Returns attachment interface for abstraction purposes, however you can only abstract the setter to allow FileUploads
    @JvmField
    @get:Nonnull
    val attachments: List<AttachedFile?>?

    /**
     * Whether embeds will be suppressed on this message.
     *
     * @return True, if embeds are suppressed
     */
    val isSuppressEmbeds: Boolean

    @get:Nonnull
    val mentionedUsers: Set<String?>?

    @get:Nonnull
    val mentionedRoles: Set<String?>?

    @get:Nonnull
    val allowedMentions: EnumSet<MentionType?>?

    /**
     * Whether this message would mention a user, if it is sent as a reply.
     *
     * @return True, if this would mention with the reply
     */
    val isMentionRepliedUser: Boolean
}
