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

package net.dv8tion.jda.api.entities.messages;

import net.dv8tion.jda.annotations.Incubating;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.Range;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface MessageSearchAction extends RestAction<MessageSearchResponse> {
    /**
     * Sets the maximum number of messages to return.
     *
     * <p><b>Note:</b> The search may return fewer results when messages have not been accessed for a long time,
     * as such, you should not rely on the length of the messages list to paginate results.
     *
     * @param  limit
     *         Max number of messages to return, between 1 and 25, or {@code null} to use the default (25)
     *
     * @throws IllegalArgumentException
     *         If the provided limit is not between 1 and 25 and not {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction limit(@Nullable @Range(from = 1, to = 25) Integer limit);

    /**
     * Sets the offset of the returned messages
     *
     * @param  offset
     *         Offset of messages, between 1 and 9975, or {@code null} to remove the offset
     *
     * @throws IllegalArgumentException
     *         If the provided offset is not between 1 and 9975 and not {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction offset(@Nullable @Range(from = 1, to = 9975) Integer offset);

    /**
     * Sets the ID of the message to start searching from.
     * In other words, the searched messages will be after the specified ID.
     *
     * <p><b>Tip:</b> If you want to include the message in the results, you can decrement the ID.
     *
     * @param  minId
     *         The minimum message ID to search from (excluded), or {@code null} to remove the min ID
     *
     * @throws IllegalArgumentException
     *         If the provided ID is negative and not {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction minId(@Nullable Long minId);

    /**
     * Sets the ID of the message to end the search at.
     * In other words, the searched messages will be before the specified ID.
     *
     * <p><b>Tip:</b> If you want to include the message in the results, you can increment the ID.
     *
     * @param  maxId
     *         The message ID to stop at (excluded), or {@code null} to remove the max ID
     *
     * @throws IllegalArgumentException
     *         If the provided ID is negative and not {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction maxId(@Nullable Long maxId);

    /**
     * Sets the maximum number of words to skip between matching tokens in the search {@linkplain #content(String) content}.
     *
     * @param  slop
     *         Maximum numbers of words to skip when matching content, up to 100,
     *         or {@code null} to set to the default (2)
     *
     * @throws IllegalArgumentException
     *         If the provided value is not between 0 and 100 and not {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction slop(@Nullable @Range(from = 0, to = 100) Integer slop);

    /**
     * Sets the content to search for.
     *
     * @param  content
     *         The content to search for, up to 1024 characters,
     *         or {@code null} to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the content is longer than 1024 characters and not {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction content(@Nullable String content);

    /**
     * Sets the channels to search messages in.
     *
     * @param  channels
     *         The channels to search messages in, leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the collection or one of its element is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.MissingAccessException
     *         If the {@linkplain Guild#getSelfMember() current member} does not have the access to one of the channels
     *         <ul>
     *              <li>For text channels, this requires {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *              <li>For voice channels, this requires {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} and {@link net.dv8tion.jda.api.Permission#VOICE_CONNECT Permission.VOICE_CONNECT}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the {@linkplain Guild#getSelfMember() current member} does not have the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY} permission in one of the channels
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction channels(@Nonnull Collection<? extends GuildMessageChannel> channels);

    /**
     * Sets the channels to search messages in.
     *
     * @param  channels
     *         The channels to search messages in, leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the array or an element is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.MissingAccessException
     *         If the {@linkplain Guild#getSelfMember() current member} does not have the access to one of the channels
     *         <ul>
     *              <li>For text channels, this requires {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *              <li>For voice channels, this requires {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} and {@link net.dv8tion.jda.api.Permission#VOICE_CONNECT Permission.VOICE_CONNECT}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the {@linkplain Guild#getSelfMember() current member} does not have the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY} permission in one of the channels
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction channels(@Nonnull GuildMessageChannel... channels) {
        Checks.noneNull(channels, "Channels");
        return channels(Arrays.asList(channels));
    }

    /**
     * Filters the messages by {@linkplain AuthorType author type}.
     *
     * @param  authorTypes
     *         The type of authors to filter by, leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the collection or an element is {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction authorTypes(@Nonnull Collection<AuthorType> authorTypes);

    /**
     * Filters the messages by {@linkplain AuthorType author type}.
     *
     * @param  authorTypes
     *         The type of authors to filter by, leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the array or an element is {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction authorTypes(@Nonnull AuthorType... authorTypes) {
        Checks.noneNull(authorTypes, "Author types");
        return authorTypes(Arrays.asList(authorTypes));
    }

    @Nonnull
    @CheckReturnValue
    MessageSearchAction authors(@Nonnull Collection<? extends UserSnowflake> authors);

    @Nonnull
    @CheckReturnValue
    default MessageSearchAction authors(@Nonnull UserSnowflake... authors) {
        Checks.noneNull(authors, "Authors");
        return authors(Arrays.asList(authors));
    }

    @Nonnull
    @CheckReturnValue
    default MessageSearchAction authors(@Nonnull String... authors) {
        Checks.noneNull(authors, "Authors");
        return authors(Arrays.stream(authors).map(UserSnowflake::fromId).collect(Collectors.toList()));
    }

    @Nonnull
    @CheckReturnValue
    default MessageSearchAction authors(@Nonnull long... authors) {
        Checks.notNull(authors, "Authors");
        return authors(Arrays.stream(authors).mapToObj(UserSnowflake::fromId).collect(Collectors.toList()));
    }

    @Nonnull
    @CheckReturnValue
    MessageSearchAction mentionsUsers(@Nonnull Collection<? extends UserSnowflake> mentions);

    @Nonnull
    @CheckReturnValue
    default MessageSearchAction mentionsUsers(@Nonnull UserSnowflake... mentions) {
        Checks.noneNull(mentions, "Mentions");
        return mentionsUsers(Arrays.asList(mentions));
    }

    @Nonnull
    @CheckReturnValue
    default MessageSearchAction mentionsUsers(@Nonnull String... mentions) {
        Checks.noneNull(mentions, "Mentions");
        return mentionsUsers(Arrays.stream(mentions).map(UserSnowflake::fromId).collect(Collectors.toList()));
    }

    @Nonnull
    @CheckReturnValue
    default MessageSearchAction mentionsUsers(@Nonnull long... mentions) {
        Checks.notNull(mentions, "Mentions");
        return mentionsUsers(
                Arrays.stream(mentions).mapToObj(UserSnowflake::fromId).collect(Collectors.toList()));
    }

    @Nonnull
    @CheckReturnValue
    MessageSearchAction mentionsRoles(@Nonnull Collection<? extends Role> mentions);

    @Nonnull
    @CheckReturnValue
    default MessageSearchAction mentionsRoles(@Nonnull Role... mentions) {
        return mentionsRoles(Arrays.asList(mentions));
    }

    @Nonnull
    @CheckReturnValue
    MessageSearchAction mentionsEveryone(boolean mentionsEveryone);

    @Nonnull
    @CheckReturnValue
    MessageSearchAction repliesToUsers(@Nonnull Collection<? extends UserSnowflake> repliedTo);

    @Nonnull
    @CheckReturnValue
    default MessageSearchAction repliesToUsers(@Nonnull UserSnowflake... repliedTo) {
        Checks.noneNull(repliedTo, "Users");
        return repliesToUsers(Arrays.asList(repliedTo));
    }

    @Nonnull
    @CheckReturnValue
    default MessageSearchAction repliesToUsers(@Nonnull String... repliedTo) {
        Checks.noneNull(repliedTo, "Users");
        return repliesToUsers(
                Arrays.stream(repliedTo).map(UserSnowflake::fromId).collect(Collectors.toList()));
    }

    @Nonnull
    @CheckReturnValue
    default MessageSearchAction repliesToUsers(@Nonnull long... repliedTo) {
        Checks.notNull(repliedTo, "Users");
        return repliesToUsers(
                Arrays.stream(repliedTo).mapToObj(UserSnowflake::fromId).collect(Collectors.toList()));
    }

    @Nonnull
    @CheckReturnValue
    MessageSearchAction repliesToMessages(@Nonnull Collection<String> repliedTo);

    @Nonnull
    @CheckReturnValue
    default MessageSearchAction repliesToMessages(@Nonnull long... repliedTo) {
        Checks.notNull(repliedTo, "Messages");
        return repliesToMessages(
                Arrays.stream(repliedTo).mapToObj(Long::toUnsignedString).collect(Collectors.toList()));
    }

    @Nonnull
    @CheckReturnValue
    default MessageSearchAction repliesToMessages(@Nonnull String... repliedTo) {
        Checks.noneNull(repliedTo, "Messages");
        return repliesToMessages(Arrays.asList(repliedTo));
    }

    @Nonnull
    @CheckReturnValue
    MessageSearchAction pinned(boolean pinned);

    @Nonnull
    @CheckReturnValue
    MessageSearchAction hasTypes(@Nonnull Collection<HasType> hasTypes);

    @Nonnull
    @CheckReturnValue
    default MessageSearchAction hasTypes(@Nonnull HasType... hasTypes) {
        Checks.noneNull(hasTypes, "HasTypes");
        return hasTypes(Arrays.asList(hasTypes));
    }

    @Nonnull
    @CheckReturnValue
    MessageSearchAction embedTypes(@Nonnull Collection<EmbedType> embedTypes);

    @Nonnull
    @CheckReturnValue
    default MessageSearchAction embedTypes(@Nonnull EmbedType... embedTypes) {
        return embedTypes(Arrays.asList(embedTypes));
    }

    @Nonnull
    @CheckReturnValue
    MessageSearchAction embedProvider(@Nonnull Collection<String> embedProviders);

    @Nonnull
    @CheckReturnValue
    default MessageSearchAction embedProvider(@Nonnull String... embedProviders) {
        return embedProvider(Arrays.asList(embedProviders));
    }

    @Nonnull
    @CheckReturnValue
    MessageSearchAction linkHostnames(@Nonnull Collection<String> linkHostnames);

    @Nonnull
    @CheckReturnValue
    default MessageSearchAction linkHostnames(@Nonnull String... linkHostnames) {
        Checks.noneNull(linkHostnames, "Link hostnames");
        return linkHostnames(Arrays.asList(linkHostnames));
    }

    // TODO docs when stable
    @Incubating
    @Nonnull
    @CheckReturnValue
    MessageSearchAction attachmentFilenames(@Nonnull Collection<String> attachmentFilenames);

    // TODO docs when stable
    @Incubating
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction attachmentFilenames(@Nonnull String... attachmentFilenames) {
        return attachmentFilenames(Arrays.asList(attachmentFilenames));
    }

    @Nonnull
    @CheckReturnValue
    MessageSearchAction attachmentExtensions(@Nonnull Collection<String> attachmentExtensions);

    @Nonnull
    @CheckReturnValue
    default MessageSearchAction attachmentExtensions(@Nonnull String... attachmentExtensions) {
        Checks.noneNull(attachmentExtensions, "Attachment extensions");
        return attachmentExtensions(Arrays.asList(attachmentExtensions));
    }

    @Nonnull
    @CheckReturnValue
    MessageSearchAction sortBy(@Nullable SortType sortType);

    @Nonnull
    @CheckReturnValue
    MessageSearchAction sortOrder(@Nullable SortOrder sortOrder);

    @Nonnull
    @CheckReturnValue
    MessageSearchAction includeNsfw(boolean includeNsfw);

    enum AuthorType {
        USER("user"),
        BOT("bot"),
        WEBHOOK("webhook");

        private final String value;

        AuthorType(String value) {
            this.value = value;
        }

        @Nonnull
        public String getValue() {
            return value;
        }
    }

    enum HasType {
        IMAGE("image"),
        SOUND("sound"),
        VIDEO("video"),
        FILE("file"),
        STICKER("sticker"),
        EMBED("embed"),
        LINK("link"),
        POLL("poll"),
        SNAPSHOT("snapshot"),
        ;

        private final String value;

        HasType(String value) {
            this.value = value;
        }

        @Nonnull
        public String getValue() {
            return value;
        }
    }

    enum EmbedType {
        IMAGE("image"),
        VIDEO("video"),
        GIF("gif"),
        SOUND("sound"),
        ARTICLE("article"),
        ;

        private final String value;

        EmbedType(String value) {
            this.value = value;
        }

        @Nonnull
        public String getValue() {
            return value;
        }
    }

    enum SortType {
        TIMESTAMP("timestamp"),
        RELEVANCE("relevance"),
        ;

        private final String value;

        SortType(String value) {
            this.value = value;
        }

        @Nonnull
        public String getValue() {
            return value;
        }
    }

    enum SortOrder {
        DESC("desc"),
        ASC("asc");

        private final String value;

        SortOrder(String value) {
            this.value = value;
        }

        @Nonnull
        public String getValue() {
            return value;
        }
    }
}
