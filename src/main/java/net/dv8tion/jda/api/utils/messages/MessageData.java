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

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.tree.MessageComponentTree;
import net.dv8tion.jda.api.utils.AttachedFile;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Represents data relevant to all message requests.
 */
public interface MessageData
{
    /**
     * The configured message content, this is the opposite for {@link MessageRequest#setContent(String)} and only returns what was set using that setter.
     *
     * <p>For message edit requests, this will not be the current content of the message.
     *
     * @return The currently configured content, or an empty string if none was set yet
     *
     * @see    MessageRequest#setContent(String)
     */
    @Nonnull
    String getContent();

    /**
     * The configured message embeds, this is the opposite of {@link MessageRequest#setEmbeds(Collection)} and only returns what was set using that setter.
     *
     * <p>For message edit requests, this will not be the current embeds of the message.
     *
     * @return The currently configured embeds, or an empty list if none were set yet
     *
     * @see    MessageRequest#setEmbeds(Collection)
     */
    @Nonnull
    List<MessageEmbed> getEmbeds();

    /**
     * The configured message components, this is the opposite of {@link MessageRequest#setComponents(Collection)} and only returns what was set using that setter.
     *
     * <p>For message edit requests, this will not be the current components of the message.
     *
     * @return The currently configured components, or an empty list if none were set yet
     *
     * @see    MessageRequest#setEmbeds(Collection)
     */
    @Nonnull
    List<MessageTopLevelComponentUnion> getComponents();

    // TODO-components-v2 - docs
    @Nonnull
    default MessageComponentTree getComponentTree()
    {
        return MessageComponentTree.of(getComponents());
    }

    /**
     * Whether this message is using components V2.
     *
     * @return {@code true} if this is using components V2
     *
     * @see MessageRequest#useComponentsV2()
     * @see MessageRequest#useComponentsV2(boolean)
     */
    boolean isUsingComponentsV2();

    // Returns attachment interface for abstraction purposes, however you can only abstract the setter to allow FileUploads

    /**
     * The configured message attachments as {@link AttachedFile}, this is the opposite of {@link MessageRequest#setFiles(Collection)} and only returns what was set using that setter.
     *
     * <p>For message edit requests, this will not be the current file attachments of the message.
     *
     * @return The currently configured attachments, or an empty list if none were set yet
     *
     * @see    MessageRequest#setFiles(Collection)
     */
    @Nonnull
    List<? extends AttachedFile> getAttachments();

    /**
     * Whether embeds will be suppressed on this message.
     *
     * @return True, if embeds are suppressed
     */
    boolean isSuppressEmbeds();

    /**
     * The IDs for users which are allowed to be mentioned, or an empty list.
     *
     * @return The user IDs which are mention whitelisted
     */
    @Nonnull
    Set<String> getMentionedUsers();

    /**
     * The IDs for roles which are allowed to be mentioned, or an empty list.
     *
     * @return The role IDs which are mention whitelisted
     */
    @Nonnull
    Set<String> getMentionedRoles();

    /**
     * The mention types which are whitelisted.
     *
     * @return The mention types which can be mentioned by this message
     */
    @Nonnull
    EnumSet<Message.MentionType> getAllowedMentions();

    /**
     * Whether this message would mention a user, if it is sent as a reply.
     *
     * @return True, if this would mention with the reply
     */
    boolean isMentionRepliedUser();
}
