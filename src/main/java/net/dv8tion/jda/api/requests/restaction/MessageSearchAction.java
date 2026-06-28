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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.messages.MessageSearchResponse;
import net.dv8tion.jda.api.requests.FluentRestAction;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.Range;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Extension of {@link RestAction} specifically designed to search messages.
 *
 * @see Guild#searchMessages()
 */
public interface MessageSearchAction extends FluentRestAction<MessageSearchResponse, MessageSearchAction> {
    /** The maximum amount of messages that can be skipped at once */
    int MAX_OFFSET = 9975;
    /** The maximum amount of messages that can be returned at once */
    int MAX_LIMIT = 25;
    /** The maximum amount of words that can be skipped when matching content */
    int MAX_SLOP = 100;
    /** The maximum length of the content to search for */
    int MAX_CONTENT_LENGTH = 1024;
    /** The maximum amount of channels this search can be limited to */
    int MAX_CHANNELS = 500;
    /** The maximum amount of authors this search can be limited to */
    int MAX_AUTHORS = 100;

    /** The maximum amount of user mentions this search can be filtered on */
    int MAX_USER_MENTIONS = 100;
    /** The maximum amount of role mentions this search can be filtered on */
    int MAX_ROLE_MENTIONS = 100;

    /** The maximum amount of users which must be replied to in order to include the message */
    int MAX_REPLIED_TO_USERS = 100;
    /** The maximum amount of messages which must be replied to in order to include the message */
    int MAX_REPLIED_TO_MESSAGES = 100;

    /** The maximum amount of embed providers */
    int MAX_EMBED_PROVIDERS = 100;
    /** The maximum length of an embed provider */
    int MAX_EMBED_PROVIDER_LENGTH = 256;

    /** The maximum amount of link hostnames */
    int MAX_LINK_HOSTNAMES = 100;
    /** The maximum length of an embed provider */
    int MAX_LINK_HOSTNAME_LENGTH = 256;

    /** The maximum amount of attachment filenames */
    int MAX_ATTACHMENT_FILENAMES = 100;
    /** The maximum length of an attachment filename */
    int MAX_ATTACHMENT_FILENAME_LENGTH = 256;

    /** The maximum amount of attachment extensions */
    int MAX_ATTACHMENT_EXTENSIONS = 100;
    /** The maximum length of an attachment extension */
    int MAX_ATTACHMENT_EXTENSION_LENGTH = 256;

    /**
     * Sets the maximum number of messages to return.
     *
     * <p><b>Note:</b> The search may return fewer results when messages have not been accessed for a long time,
     * as such, you should not rely on the length of the messages list to paginate results.
     * Use {@link #minId(long)} and/or {@link #maxId(long)} instead.
     *
     * @param  limit
     *         Max number of messages to return, between 1 and {@value #MAX_LIMIT}, or {@code null} to use the default (25)
     *
     * @throws IllegalArgumentException
     *         If the provided limit is not between 1 and {@value #MAX_LIMIT} and not {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction limit(@Nullable @Range(from = 1, to = MAX_LIMIT) Integer limit);

    /**
     * Sets the offset of the returned messages.
     *
     * <p><b>Note:</b> You must not offset the search by the number of messages received from a previous request,
     * to implement some pagination, you should instead specify {@link #minId(long)} or {@link #maxId(long)},
     * depending on the desired direction.
     *
     * @param  offset
     *         Offset of messages, between 1 and {@value #MAX_OFFSET}, or {@code null} to remove the offset
     *
     * @throws IllegalArgumentException
     *         If the provided offset is not between 1 and {@value #MAX_OFFSET} and not {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction offset(@Nullable @Range(from = 1, to = MAX_OFFSET) Integer offset);

    /**
     * Sets the ID of the message to start searching from.
     * In other words, the results will only include messages newer than the specified ID.
     *
     * <p>This doesn't need to be a real message's ID,
     * this filter is based on the {@linkplain net.dv8tion.jda.api.utils.TimeUtil#getDiscordTimestamp(long) timestamp encoded in the snowflake},
     * meaning you can use this method to limit messages to a certain time period.
     *
     * <p><b>Tip:</b> If you want to include the message in the results, you can decrement the ID.
     *
     * @param  minId
     *         The minimum message ID to search from (excluded)
     *
     * @throws IllegalArgumentException
     *         If the provided ID is negative
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction minId(long minId);

    /**
     * Sets the ID of the message to start searching from.
     * In other words, the results will only include messages newer than the specified ID.
     *
     * <p>This doesn't need to be a real message's ID,
     * this filter is based on the {@linkplain net.dv8tion.jda.api.utils.TimeUtil#getDiscordTimestamp(long) timestamp encoded in the snowflake},
     * meaning you can use this method to limit messages to a certain time period.
     *
     * <p><b>Tip:</b> If you want to include the message in the results, you can decrement the ID.
     *
     * @param  minId
     *         The minimum message ID to search from (excluded), or {@code null} to remove the min ID
     *
     * @throws IllegalArgumentException
     *         If the provided ID is not {@code null} and is not a valid snowflake
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction minId(@Nullable String minId);

    /**
     * Sets the ID of the message to end the search at.
     * In other words, the results will only include messages older than the specified ID.
     *
     * <p>This doesn't need to be a real message's ID,
     * this filter is based on the {@linkplain net.dv8tion.jda.api.utils.TimeUtil#getDiscordTimestamp(long) timestamp encoded in the snowflake},
     * meaning you can use this method to limit messages to a certain time period.
     *
     * <p><b>Tip:</b> If you want to include the message in the results, you can increment the ID.
     *
     * @param  maxId
     *         The message ID to stop at (excluded)
     *
     * @throws IllegalArgumentException
     *         If the provided ID is negative
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction maxId(long maxId);

    /**
     * Sets the ID of the message to end the search at.
     * In other words, the results will only include messages older than the specified ID.
     *
     * <p>This doesn't need to be a real message's ID,
     * this filter is based on the {@linkplain net.dv8tion.jda.api.utils.TimeUtil#getDiscordTimestamp(long) timestamp encoded in the snowflake},
     * meaning you can use this method to limit messages to a certain time period.
     *
     * <p><b>Tip:</b> If you want to include the message in the results, you can increment the ID.
     *
     * @param  maxId
     *         The message ID to stop at (excluded), or {@code null} to remove the max ID
     *
     * @throws IllegalArgumentException
     *         If the provided ID is not {@code null} and is not a valid snowflake
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction maxId(@Nullable String maxId);

    /**
     * Sets the maximum number of words to skip between matching tokens in the search {@linkplain #content(String) content}.
     *
     * @param  slop
     *         Maximum numbers of words to skip when matching content, up to {@value #MAX_SLOP},
     *         or {@code null} to set to the default (2)
     *
     * @throws IllegalArgumentException
     *         If the provided value is not between 0 and {@value #MAX_SLOP} and not {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction slop(@Nullable @Range(from = 0, to = MAX_SLOP) Integer slop);

    /**
     * Sets the content to search for.
     *
     * @param  content
     *         The content to search for, up to {@value #MAX_CONTENT_LENGTH} characters,
     *         or {@code null} to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the content is longer than {@value #MAX_CONTENT_LENGTH} characters and not {@code null}
     *
     * @return This action for chaining
     *
     * @see #slop(Integer)
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction content(@Nullable String content);

    /**
     * Keeps messages from <b>any</b> of the provided channels. Threads, archived or not, can also be passed.
     *
     * <p><b>Note:</b> This implicitly includes child threads!
     *
     * @param  channels
     *         The channels to search messages in, up to {@value #MAX_CHANNELS},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the collection or one of its element is {@code null}, or the collection has more than {@value #MAX_CHANNELS} elements
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
     * Keeps messages from <b>any</b> of the provided channels. Threads, archived or not, can also be passed.
     *
     * <p><b>Note:</b> This implicitly includes child threads!
     *
     * @param  channels
     *         The channels to search messages in, up to {@value #MAX_CHANNELS},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the array or an element is {@code null}, or the array has more than {@value #MAX_CHANNELS} elements
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
     * Keeps messages from <b>any</b> of the provided channels. Threads, archived or not, can also be passed.
     *
     * <p><b>Note:</b> This implicitly includes child threads!
     *
     * @param  channels
     *         The channels to search messages in, up to {@value #MAX_CHANNELS},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the array is {@code null}, or has more than {@value #MAX_CHANNELS} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction channels(@Nonnull long... channels);

    /**
     * Keeps messages from <b>any</b> of the provided channels. Threads, archived or not, can also be passed.
     *
     * <p><b>Note:</b> This implicitly includes child threads!
     *
     * @param  channels
     *         The channels to search messages in, up to {@value #MAX_CHANNELS},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the array or an element is {@code null}, an element isn't a valid snowflake,
     *         or the array has more than {@value #MAX_CHANNELS} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction channels(@Nonnull String... channels);

    /**
     * Includes messages which are sent by <b>any</b> of the provided {@linkplain AuthorType author types}.
     * <br><b>This overrides {@linkplain #excludeAuthorTypes(Collection) exclusions}!</b>
     *
     * @param  authorTypes
     *         The type of authors to keep, leave empty to remove the inclusion filter
     *
     * @throws IllegalArgumentException
     *         If the collection or an element is {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction includeAuthorTypes(@Nonnull Collection<AuthorType> authorTypes);

    /**
     * Includes messages which are sent by <b>any</b> of the provided {@linkplain AuthorType author types}.
     * <br><b>This overrides exclusions!</b>
     *
     * @param  authorTypes
     *         The type of authors to keep, leave empty to remove the inclusion filter
     *
     * @throws IllegalArgumentException
     *         If the array or an element is {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction includeAuthorTypes(@Nonnull AuthorType... authorTypes) {
        Checks.noneNull(authorTypes, "Author types");
        return includeAuthorTypes(Arrays.asList(authorTypes));
    }

    /**
     * Excludes messages which are sent by <b>any</b> of the provided {@linkplain AuthorType author types}.
     * <br><b>This overrides inclusions!</b>
     *
     * @param  authorTypes
     *         The type of authors to avoid, leave empty to remove the exclusion filter
     *
     * @throws IllegalArgumentException
     *         If the collection or an element is {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction excludeAuthorTypes(@Nonnull Collection<AuthorType> authorTypes);

    /**
     * Excludes messages which are sent by <b>any</b> of the provided {@linkplain AuthorType author types}.
     * <br><b>This overrides inclusions!</b>
     *
     * @param  authorTypes
     *         The type of authors to avoid, leave empty to remove the exclusion filter
     *
     * @throws IllegalArgumentException
     *         If the array or an element is {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction excludeAuthorTypes(@Nonnull AuthorType... authorTypes) {
        Checks.noneNull(authorTypes, "Author types");
        return excludeAuthorTypes(Arrays.asList(authorTypes));
    }

    /**
     * Keeps messages which are sent by <b>any</b> of the provided authors.
     *
     * @param  authors
     *         The authors to keep messages from, up to {@value #MAX_AUTHORS},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the collection or an element is {@code null},
     *         or the collection has more than {@value #MAX_AUTHORS} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction authors(@Nonnull Collection<? extends UserSnowflake> authors);

    /**
     * Keeps messages which are sent by <b>any</b> of the provided authors.
     *
     * @param  authors
     *         The authors to keep messages from, up to {@value #MAX_AUTHORS},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the array or an element is {@code null},
     *         or the array has more than {@value #MAX_AUTHORS} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction authors(@Nonnull UserSnowflake... authors) {
        Checks.noneNull(authors, "Authors");
        return authors(Arrays.asList(authors));
    }

    /**
     * Keeps messages which are sent by <b>any</b> of the provided author IDs.
     *
     * @param  authors
     *         The author IDs to keep messages from, up to {@value #MAX_AUTHORS}, leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the array or an element is {@code null},
     *         or one of the author IDs is not a valid snowflake,
     *         or the array has more than {@value #MAX_AUTHORS} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction authors(@Nonnull String... authors) {
        Checks.noneNull(authors, "Authors");
        return authors(Arrays.stream(authors).map(UserSnowflake::fromId).collect(Collectors.toList()));
    }

    /**
     * Keeps messages which are sent by <b>any</b> of the provided author IDs.
     *
     * @param  authors
     *         The author IDs to keep messages from, up to {@value #MAX_AUTHORS},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the array is {@code null},
     *         or the array has more than {@value #MAX_AUTHORS} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction authors(@Nonnull long... authors) {
        Checks.notNull(authors, "Authors");
        return authors(Arrays.stream(authors).mapToObj(UserSnowflake::fromId).collect(Collectors.toList()));
    }

    /**
     * Keeps messages which mention <b>any</b> of the provided users.
     *
     * <p>A "mention" here includes anything that makes a "ping" (highlighted for user):
     * <ul>
     *     <li>{@linkplain UserSnowflake#getAsMention() Direct mentions}</li>
     *     <li>{@linkplain net.dv8tion.jda.api.utils.messages.MessageCreateRequest#mentionRepliedUser(boolean) Reply mentions}</li>
     * </ul>
     *
     * <p>Mentions which have been {@linkplain net.dv8tion.jda.api.utils.messages.MessageRequest#setAllowedMentions(Collection) disabled}
     * and not {@linkplain net.dv8tion.jda.api.utils.messages.MessageRequest#mentionUsers(long...) allowlisted}, will not match.
     *
     * @param  mentions
     *         The users which must be mentioned in the messages, up to {@value #MAX_USER_MENTIONS},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the collection is, or contains {@code null},
     *         or the collection has more than {@value #MAX_USER_MENTIONS} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction mentionsUsers(@Nonnull Collection<? extends UserSnowflake> mentions);

    /**
     * Keeps messages which mention <b>any</b> of the provided users.
     *
     * <p>A "mention" here includes anything that makes a "ping" (creates a special background on the client):
     * <ul>
     *     <li>{@linkplain UserSnowflake#getAsMention() Direct mentions}</li>
     *     <li>{@linkplain net.dv8tion.jda.api.utils.messages.MessageCreateRequest#mentionRepliedUser(boolean) Reply mentions}</li>
     * </ul>
     *
     * <p>Mentions which have been {@linkplain net.dv8tion.jda.api.utils.messages.MessageRequest#setAllowedMentions(Collection) disabled}
     * and not {@linkplain net.dv8tion.jda.api.utils.messages.MessageRequest#mentionUsers(long...) allowlisted}, will not match.
     *
     * @param  mentions
     *         The users which must be mentioned in the messages, up to {@value #MAX_USER_MENTIONS},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the array is, or contains {@code null},
     *         or the array has more than {@value #MAX_USER_MENTIONS} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction mentionsUsers(@Nonnull UserSnowflake... mentions) {
        Checks.noneNull(mentions, "Mentions");
        return mentionsUsers(Arrays.asList(mentions));
    }

    /**
     * Keeps messages which mention <b>any</b> of the provided users.
     *
     * <p>A "mention" here includes anything that makes a "ping" (creates a special background on the client):
     * <ul>
     *     <li>{@linkplain UserSnowflake#getAsMention() Direct mentions}</li>
     *     <li>{@linkplain net.dv8tion.jda.api.utils.messages.MessageCreateRequest#mentionRepliedUser(boolean) Reply mentions}</li>
     * </ul>
     *
     * <p>Mentions which have been {@linkplain net.dv8tion.jda.api.utils.messages.MessageRequest#setAllowedMentions(Collection) disabled}
     * and not {@linkplain net.dv8tion.jda.api.utils.messages.MessageRequest#mentionUsers(long...) allowlisted}, will not match.
     *
     * @param  mentions
     *         The IDs of the users which must be mentioned in the messages, up to {@value #MAX_USER_MENTIONS},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the array is, or contains {@code null}, or is not a valid snowflake,
     *         or the array has more than {@value #MAX_USER_MENTIONS} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction mentionsUsers(@Nonnull String... mentions) {
        Checks.noneNull(mentions, "Mentions");
        return mentionsUsers(Arrays.stream(mentions).map(UserSnowflake::fromId).collect(Collectors.toList()));
    }

    /**
     * Keeps messages which mention <b>any</b> of the provided users.
     *
     * <p>A "mention" here includes anything that makes a "ping" (creates a special background on the client):
     * <ul>
     *     <li>{@linkplain UserSnowflake#getAsMention() Direct mentions}</li>
     *     <li>{@linkplain net.dv8tion.jda.api.utils.messages.MessageCreateRequest#mentionRepliedUser(boolean) Reply mentions}</li>
     * </ul>
     *
     * <p>Mentions which have been {@linkplain net.dv8tion.jda.api.utils.messages.MessageRequest#setAllowedMentions(Collection) disabled}
     * and not {@linkplain net.dv8tion.jda.api.utils.messages.MessageRequest#mentionUsers(long...) allowlisted}, will not match.
     *
     * @param  mentions
     *         The IDs of the users which must be mentioned in the messages, up to {@value #MAX_USER_MENTIONS},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the array is {@code null},
     *         or the array has more than {@value #MAX_USER_MENTIONS} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction mentionsUsers(@Nonnull long... mentions) {
        Checks.notNull(mentions, "Mentions");
        return mentionsUsers(
                Arrays.stream(mentions).mapToObj(UserSnowflake::fromId).collect(Collectors.toList()));
    }

    /**
     * Keeps messages which {@linkplain Role#getAsMention() mention} <b>any</b> of the provided roles.
     *
     * <p>Mentions which have been {@linkplain net.dv8tion.jda.api.utils.messages.MessageRequest#setAllowedMentions(Collection) disabled}
     * and not {@linkplain net.dv8tion.jda.api.utils.messages.MessageRequest#mentionRoles(long...) allowlisted}, will not match.
     *
     * <p><b>Note:</b> If the {@code @everyone} role is included, it will only match those that were created from the mention,
     * but not when the raw content is "@everyone", use {@link #mentionsEveryone(Boolean)} for those instead.
     *
     * @param  mentions
     *         The roles which must be mentioned in the messages, up to {@value #MAX_ROLE_MENTIONS},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the collection is, or contains {@code null},
     *         or the collection has more than {@value #MAX_ROLE_MENTIONS} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction mentionsRoles(@Nonnull Collection<? extends Role> mentions);

    /**
     * Keeps messages which {@linkplain Role#getAsMention() mention} <b>any</b> of the provided roles.
     *
     * <p>Mentions which have been {@linkplain net.dv8tion.jda.api.utils.messages.MessageRequest#setAllowedMentions(Collection) disabled}
     * and not {@linkplain net.dv8tion.jda.api.utils.messages.MessageRequest#mentionRoles(long...) allowlisted}, will not match.
     *
     * <p><b>Note:</b> If the {@code @everyone} role is included, it will only match those that were created from the mention,
     * but not when the raw content is "@everyone", use {@link #mentionsEveryone(Boolean)} for those instead.
     *
     * @param  mentions
     *         The roles which must be mentioned in the messages, up to {@value #MAX_ROLE_MENTIONS},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the array is, or contains {@code null},
     *         or the array has more than {@value #MAX_ROLE_MENTIONS} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction mentionsRoles(@Nonnull Role... mentions) {
        return mentionsRoles(Arrays.asList(mentions));
    }

    /**
     * Filters messages by whether they are or are not mentioning {@linkplain Guild#getPublicRole() @everyone}.
     * <br>When set to {@code true}, this will keep messages that mention everyone,
     * when {@code false}, this will exclude them.
     *
     * <p>Mentions which have been {@linkplain net.dv8tion.jda.api.utils.messages.MessageRequest#setAllowedMentions(Collection) disabled},
     * or done by a user missing {@link net.dv8tion.jda.api.Permission#MESSAGE_MENTION_EVERYONE Permission.MESSAGE_MENTION_EVERYONE},
     * will not match.
     *
     * <p><b>Note:</b> This will only match message which raw content is "@everyone",
     * mentions of the `@everyone` role will not match, use {@link #mentionsRoles(Role...)} for those instead.
     *
     * @param  mentionsEveryone
     *         {@code true} to search messages mentioning everyone, {@code false} to exclude them, {@code null} to keep both
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction mentionsEveryone(@Nullable Boolean mentionsEveryone);

    /**
     * Keeps messages which {@linkplain net.dv8tion.jda.api.requests.restaction.MessageCreateAction#setMessageReference(Message) replies}
     * to <b>any</b> of the provided users.
     *
     * @param  repliedTo
     *         The users which must be replied to, up to {@value #MAX_REPLIED_TO_USERS},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the collection is, or contains {@code null},
     *         or the collection has more than {@value #MAX_REPLIED_TO_USERS} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction repliesToUsers(@Nonnull Collection<? extends UserSnowflake> repliedTo);

    /**
     * Keeps messages which {@linkplain net.dv8tion.jda.api.requests.restaction.MessageCreateAction#setMessageReference(Message) replies}
     * to <b>any</b> of the provided users.
     *
     * @param  repliedTo
     *         The users which must be replied to, up to {@value #MAX_REPLIED_TO_USERS},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the array is, or contains {@code null},
     *         or the array has more than {@value #MAX_REPLIED_TO_USERS} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction repliesToUsers(@Nonnull UserSnowflake... repliedTo) {
        Checks.noneNull(repliedTo, "Users");
        return repliesToUsers(Arrays.asList(repliedTo));
    }

    /**
     * Keeps messages which {@linkplain net.dv8tion.jda.api.requests.restaction.MessageCreateAction#setMessageReference(Message) replies}
     * to <b>any</b> of the provided user IDs.
     *
     * @param  repliedTo
     *         The user IDs which must be replied to, up to {@value #MAX_REPLIED_TO_USERS},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the array is, or contains {@code null}, or is not a valid snowflake,
     *         or the array has more than {@value #MAX_REPLIED_TO_USERS} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction repliesToUsers(@Nonnull String... repliedTo) {
        Checks.noneNull(repliedTo, "Users");
        return repliesToUsers(
                Arrays.stream(repliedTo).map(UserSnowflake::fromId).collect(Collectors.toList()));
    }

    /**
     * Keeps messages which {@linkplain net.dv8tion.jda.api.requests.restaction.MessageCreateAction#setMessageReference(Message) replies}
     * to <b>any</b> of the provided user IDs.
     *
     * @param  repliedTo
     *         The user IDs which must be replied to, up to {@value #MAX_REPLIED_TO_USERS},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the array is {@code null},
     *         or the array has more than {@value #MAX_REPLIED_TO_USERS} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction repliesToUsers(@Nonnull long... repliedTo) {
        Checks.notNull(repliedTo, "Users");
        return repliesToUsers(
                Arrays.stream(repliedTo).mapToObj(UserSnowflake::fromId).collect(Collectors.toList()));
    }

    /**
     * Keeps messages which {@linkplain net.dv8tion.jda.api.requests.restaction.MessageCreateAction#setMessageReference(Message) replies}
     * to <b>any</b> of the provided message IDs.
     *
     * @param  repliedTo
     *         The message IDs which must be replied to, up to {@value #MAX_REPLIED_TO_MESSAGES},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the collection is, or contains {@code null},
     *         or the collection has more than {@value #MAX_REPLIED_TO_MESSAGES} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction repliesToMessages(@Nonnull Collection<String> repliedTo);

    /**
     * Keeps messages which {@linkplain net.dv8tion.jda.api.requests.restaction.MessageCreateAction#setMessageReference(Message) replies}
     * to <b>any</b> of the provided message IDs.
     *
     * @param  repliedTo
     *         The message IDs which must be replied to, up to {@value #MAX_REPLIED_TO_MESSAGES},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the array is {@code null},
     *         or the array has more than {@value #MAX_REPLIED_TO_MESSAGES} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction repliesToMessages(@Nonnull long... repliedTo) {
        Checks.notNull(repliedTo, "Messages");
        return repliesToMessages(
                Arrays.stream(repliedTo).mapToObj(Long::toUnsignedString).collect(Collectors.toList()));
    }

    /**
     * Keeps messages which {@linkplain net.dv8tion.jda.api.requests.restaction.MessageCreateAction#setMessageReference(Message) replies}
     * to <b>any</b> of the provided message IDs.
     *
     * @param  repliedTo
     *         The message IDs which must be replied to, up to {@value #MAX_REPLIED_TO_MESSAGES},
     *         leave empty to remove the filter
     *
     * @throws IllegalArgumentException
     *         If the array is, or contains {@code null}, or is not a valid snowflake,
     *         or the array has more than {@value #MAX_REPLIED_TO_MESSAGES} elements
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction repliesToMessages(@Nonnull String... repliedTo) {
        Checks.noneNull(repliedTo, "Messages");
        return repliesToMessages(Arrays.asList(repliedTo));
    }

    /**
     * Filters messages by whether they are or are not pinned.
     * <br>When set to {@code true}, this will keep messages that are pinned,
     * when {@code false}, this will exclude them.
     *
     * @param  pinned
     *         {@code true} to search pinned messages, {@code false} to exclude them, {@code null} to keep both
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction pinned(@Nullable Boolean pinned);

    /**
     * Includes messages which include content matching <b>any</b> of the provided {@linkplain HasType content types}.
     * <br><b>This overrides exclusions!</b>
     *
     * @param  hasTypes
     *         The content types to keep, leave empty to remove the inclusion filter
     *
     * @throws IllegalArgumentException
     *         If the collection or an element is {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction includeHasTypes(@Nonnull Collection<HasType> hasTypes);

    /**
     * Includes messages which include content matching <b>any</b> of the provided {@linkplain HasType content types}.
     * <br><b>This overrides exclusions!</b>
     *
     * @param  hasTypes
     *         The content types to keep, leave empty to remove the inclusion filter
     *
     * @throws IllegalArgumentException
     *         If the array or an element is {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction includeHasTypes(@Nonnull HasType... hasTypes) {
        Checks.noneNull(hasTypes, "HasTypes");
        return includeHasTypes(Arrays.asList(hasTypes));
    }

    /**
     * Excludes messages which include content matching <b>any</b> of the provided {@linkplain HasType content types}.
     * <br><b>This overrides inclusions!</b>
     *
     * @param  hasTypes
     *         The content types to exclude, leave empty to remove the inclusion filter
     *
     * @throws IllegalArgumentException
     *         If the collection or an element is {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction excludeHasTypes(@Nonnull Collection<HasType> hasTypes);

    /**
     * Excludes messages which include content matching <b>any</b> of the provided {@linkplain HasType content types}.
     * <br><b>This overrides inclusions!</b>
     *
     * @param  hasTypes
     *         The content types to exclude, leave empty to remove the inclusion filter
     *
     * @throws IllegalArgumentException
     *         If the array or an element is {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction excludeHasTypes(@Nonnull HasType... hasTypes) {
        Checks.noneNull(hasTypes, "HasTypes");
        return excludeHasTypes(Arrays.asList(hasTypes));
    }

    /**
     * Keeps messages which have embeds from <b>any</b> of the provided {@linkplain EmbedType embed types}.
     *
     * @param  embedTypes
     *         The embed types, leave empty to remove the inclusion filter
     *
     * @throws IllegalArgumentException
     *         If the collection or an element is {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction embedTypes(@Nonnull Collection<EmbedType> embedTypes);

    /**
     * Keeps messages which have embeds from <b>any</b> of the provided {@linkplain EmbedType embed types}.
     *
     * @param  embedTypes
     *         The embed types, leave empty to remove the inclusion filter
     *
     * @throws IllegalArgumentException
     *         If the array or an element is {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction embedTypes(@Nonnull EmbedType... embedTypes) {
        return embedTypes(Arrays.asList(embedTypes));
    }

    /**
     * Keeps messages which have embeds created from <b>any</b> of the provided embed providers,
     * such as {@code Tenor} or {@code Giphy}, as retrievable from {@link MessageEmbed.Provider#getName()}.
     *
     * @param  embedProviders
     *         The embed provider names, case-sensitive, max {@value #MAX_EMBED_PROVIDER_LENGTH} characters per provider, up to {@value #MAX_EMBED_PROVIDERS},
     *         leave empty to remove the inclusion filter
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the collection or an element is {@code null}</li>
     *             <li>If a provider is larger than {@value #MAX_EMBED_PROVIDER_LENGTH} characters</li>
     *             <li>If there is more than {@value #MAX_EMBED_PROVIDERS} providers</li>
     *         </ul>
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction embedProvider(@Nonnull Collection<String> embedProviders);

    /**
     * Keeps messages which have embeds created from <b>any</b> of the provided embed providers,
     * such as {@code Tenor} or {@code Giphy}, as retrievable from {@link MessageEmbed.Provider#getName()}.
     *
     * @param  embedProviders
     *         The embed provider names, case-sensitive, max {@value #MAX_EMBED_PROVIDER_LENGTH} characters per provider, up to {@value #MAX_EMBED_PROVIDERS},
     *         leave empty to remove the inclusion filter
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the array or an element is {@code null}</li>
     *             <li>If a provider is larger than {@value #MAX_EMBED_PROVIDER_LENGTH} characters</li>
     *             <li>If there is more than {@value #MAX_EMBED_PROVIDERS} providers</li>
     *         </ul>
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction embedProvider(@Nonnull String... embedProviders) {
        Checks.noneNull(embedProviders, "Embed providers");
        return embedProvider(Arrays.asList(embedProviders));
    }

    /**
     * Keeps messages which have links from <b>any</b> of the provided hostnames,
     * such as {@code media.discordapp.com} or {@code jda.wiki}.
     *
     * @param  linkHostnames
     *         The link hostnames, max {@value #MAX_LINK_HOSTNAME_LENGTH} characters per hostname, up to {@value #MAX_LINK_HOSTNAMES},
     *         leave empty to remove the inclusion filter
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the collection or an element is {@code null}</li>
     *             <li>If a hostname is larger than {@value #MAX_LINK_HOSTNAME_LENGTH} characters</li>
     *             <li>If there is more than {@value #MAX_LINK_HOSTNAMES} hostnames</li>
     *         </ul>
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction linkHostnames(@Nonnull Collection<String> linkHostnames);

    /**
     * Keeps messages which have links from <b>any</b> of the provided hostnames,
     * such as {@code media.discordapp.com} or {@code jda.wiki}.
     *
     * @param  linkHostnames
     *         The link hostnames, max {@value #MAX_LINK_HOSTNAME_LENGTH} characters per hostname, up to {@value #MAX_LINK_HOSTNAMES},
     *         leave empty to remove the inclusion filter
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the array or an element is {@code null}</li>
     *             <li>If a hostname is larger than {@value #MAX_LINK_HOSTNAME_LENGTH} characters</li>
     *             <li>If there is more than {@value #MAX_LINK_HOSTNAMES} hostnames</li>
     *         </ul>
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction linkHostnames(@Nonnull String... linkHostnames) {
        Checks.noneNull(linkHostnames, "Link hostnames");
        return linkHostnames(Arrays.asList(linkHostnames));
    }

    /**
     * Keeps messages which have attachments named after <b>any</b> of the provided names,
     * must include the extension.
     *
     * @param  attachmentFilenames
     *         The attachment file names, max {@value #MAX_ATTACHMENT_FILENAME_LENGTH} characters per file name, up to {@value #MAX_ATTACHMENT_FILENAMES},
     *         leave empty to remove the inclusion filter
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the collection or an element is {@code null}</li>
     *             <li>If a file name is larger than {@value #MAX_ATTACHMENT_FILENAME_LENGTH} characters</li>
     *             <li>If there is more than {@value #MAX_ATTACHMENT_FILENAMES} file names</li>
     *         </ul>
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction attachmentFilenames(@Nonnull Collection<String> attachmentFilenames);

    /**
     * Keeps messages which have attachments named after <b>any</b> of the provided names,
     * must include the extension.
     *
     * @param  attachmentFilenames
     *         The attachment file names, max {@value #MAX_ATTACHMENT_FILENAME_LENGTH} characters per file name, up to {@value #MAX_ATTACHMENT_FILENAMES},
     *         leave empty to remove the inclusion filter
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the array or an element is {@code null}</li>
     *             <li>If a file name is larger than {@value #MAX_ATTACHMENT_FILENAME_LENGTH} characters</li>
     *             <li>If there is more than {@value #MAX_ATTACHMENT_FILENAMES} file names</li>
     *         </ul>
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction attachmentFilenames(@Nonnull String... attachmentFilenames) {
        Checks.noneNull(attachmentFilenames, "Attachment filenames");
        return attachmentFilenames(Arrays.asList(attachmentFilenames));
    }

    /**
     * Keeps messages which have attachments with an extension equal to <b>any</b> of the provided extensions.
     *
     * @param  attachmentExtensions
     *         The attachment extensions, max {@value #MAX_ATTACHMENT_EXTENSION_LENGTH} characters per extension, up to {@value #MAX_ATTACHMENT_EXTENSIONS},
     *         leave empty to remove the inclusion filter
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the collection or an element is {@code null}</li>
     *             <li>If an extension is larger than {@value #MAX_ATTACHMENT_EXTENSION_LENGTH} characters</li>
     *             <li>If there is more than {@value #MAX_ATTACHMENT_EXTENSIONS} extensions</li>
     *         </ul>
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction attachmentExtensions(@Nonnull Collection<String> attachmentExtensions);

    /**
     * Keeps messages which have attachments with an extension equal to <b>any</b> of the provided extensions.
     *
     * @param  attachmentExtensions
     *         The attachment extensions, max {@value #MAX_ATTACHMENT_EXTENSION_LENGTH} characters per extension, up to {@value #MAX_ATTACHMENT_EXTENSIONS},
     *         leave empty to remove the inclusion filter
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the array or an element is {@code null}</li>
     *             <li>If an extension is larger than {@value #MAX_ATTACHMENT_EXTENSION_LENGTH} characters</li>
     *             <li>If there is more than {@value #MAX_ATTACHMENT_EXTENSIONS} extensions</li>
     *         </ul>
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    default MessageSearchAction attachmentExtensions(@Nonnull String... attachmentExtensions) {
        Checks.noneNull(attachmentExtensions, "Attachment extensions");
        return attachmentExtensions(Arrays.asList(attachmentExtensions));
    }

    /**
     * Sorts messages by the provided sorting algorithm.
     * <br>The default is {@link SortType#TIMESTAMP TIMESTAMP}.
     *
     * @param  sortType
     *         The sorting algorithm to use
     *
     * @throws IllegalArgumentException
     *         If the argument is {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction sortBy(@Nonnull SortType sortType);

    /**
     * Sorts messages by the provided sort order.
     * This is only relevant when {@link SortType#TIMESTAMP SortType.TIMESTAMP} is used (the default).
     * <br>The default is {@link SortOrder#DESC DESC}.
     *
     * @param  sortOrder
     *         The sort order to use
     *
     * @throws IllegalArgumentException
     *         If the argument is {@code null}
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction sortOrder(@Nonnull SortOrder sortOrder);

    /**
     * Whether to include results from age-restricted channels.
     * <br>Default: {@code false}
     *
     * @param  includeNsfw
     *         {@code true} to include age-restricted channels
     *
     * @return This action for chaining
     */
    @Nonnull
    @CheckReturnValue
    MessageSearchAction includeNsfw(boolean includeNsfw);

    /**
     * Represents a type of message author.
     *
     * @see MessageSearchAction#includeAuthorTypes(AuthorType...)
     * @see MessageSearchAction#excludeAuthorTypes(AuthorType...)
     */
    enum AuthorType {
        /**
         * Messages sent by regular users.
         */
        USER("user"),
        /**
         * Messages sent by bots. Use {@link #WEBHOOK} for interaction replies.
         */
        BOT("bot"),
        /**
         * Messages sent by webhooks and interactions.
         */
        WEBHOOK("webhook");

        private final String value;

        AuthorType(String value) {
            this.value = value;
        }

        /**
         * Returns the raw value Discord expects.
         *
         * @return The raw value
         */
        @Nonnull
        public String getValue() {
            return value;
        }
    }

    /**
     * Represents a type of content in a message, a message can have multiple of them at once.
     *
     * @see MessageSearchAction#includeHasTypes(HasType...)
     * @see MessageSearchAction#excludeHasTypes(HasType...)
     */
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

        /**
         * Returns the raw value Discord expects.
         *
         * @return The raw value
         */
        @Nonnull
        public String getValue() {
            return value;
        }
    }

    /**
     * This is different from {@linkplain net.dv8tion.jda.api.entities.EmbedType Message's embed type},
     * this encompasses a wider range of embed types.
     *
     * @see MessageSearchAction#embedTypes(EmbedType...)
     */
    enum EmbedType {
        IMAGE("image"),
        VIDEO("video"),
        /**
         * <b>Note:</b> Messages sent before February 24, 2026, may not be properly indexed.
         */
        GIF("gif"),
        SOUND("sound"),
        ARTICLE("article"),
        ;

        private final String value;

        EmbedType(String value) {
            this.value = value;
        }

        /**
         * Returns the raw value Discord expects.
         *
         * @return The raw value
         */
        @Nonnull
        public String getValue() {
            return value;
        }
    }

    /**
     * The sorting algorithm with which search results are sorted.
     *
     * @see MessageSearchAction#sortBy(SortType)
     */
    enum SortType {
        TIMESTAMP("timestamp"),
        RELEVANCE("relevance"),
        ;

        private final String value;

        SortType(String value) {
            this.value = value;
        }

        /**
         * Returns the raw value Discord expects.
         *
         * @return The raw value
         */
        @Nonnull
        public String getValue() {
            return value;
        }
    }

    /**
     * The order in which search results are sorted.
     *
     * @see MessageSearchAction#sortOrder(SortOrder)
     */
    enum SortOrder {
        DESC("desc"),
        ASC("asc");

        private final String value;

        SortOrder(String value) {
            this.value = value;
        }

        /**
         * Returns the raw value Discord expects.
         *
         * @return The raw value
         */
        @Nonnull
        public String getValue() {
            return value;
        }
    }
}
