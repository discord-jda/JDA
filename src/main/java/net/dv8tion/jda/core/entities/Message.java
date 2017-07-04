/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import okhttp3.Request;
import okhttp3.Response;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Formattable;
import java.util.List;

/**
 * Represents a Text message received from Discord.
 * <br>This represents messages received from {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannels}.
 *
 * <h1>Formattable</h1>
 * This interface extends {@link java.util.Formattable Formattable} and can be used with a {@link java.util.Formatter Formatter}
 * such as used by {@link String#format(String, Object...) String.format(String, Object...)}
 * or {@link java.io.PrintStream#printf(String, Object...) PrintStream.printf(String, Object...)}.
 *
 * <p>This will use {@link #getContent()} rather than {@link Object#toString()}!
 * <br>Supported Features:
 * <ul>
 *     <li><b>Alternative</b>
 *     <br>   - Using {@link #getRawContent()}
 *              (Example: {@code %#s} - uses {@link #getContent()})</li>
 *
 *     <li><b>Width/Left-Justification</b>
 *     <br>   - Ensures the size of a format
 *              (Example: {@code %20s} - uses at minimum 20 chars;
 *              {@code %-10s} - uses left-justified padding)</li>
 *
 *     <li><b>Precision</b>
 *     <br>   - Cuts the content to the specified size
 *              (replacing last 3 chars with {@code ...}; Example: {@code %.20s})</li>
 * </ul>
 *
 * <p>More information on formatting syntax can be found in the {@link java.util.Formatter format syntax documentation}!
 */
public interface Message extends ISnowflake, Formattable
{
    int MAX_FILE_SIZE = 8 << 20; // 8mb
    /**
     * A immutable list of all mentioned users. if no user was mentioned, this list is empty.
     * <br>In {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel's}, this always returns an empty List
     *
     * @return immutable list of mentioned users
     */
    List<User> getMentionedUsers();

    /**
     * Checks if given user was mentioned in this message in any way (@User, @everyone, @here).
     *
     * @param  user
     *         The user to check on.
     *
     * @return True if the given user was mentioned in this message.
     */
    boolean isMentioned(User user);

    /**
     * A immutable list of all mentioned {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}. If none were mentioned, this list is empty.
     * <br>In {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannels} and {@link net.dv8tion.jda.client.entities.Group Groups},
     * this always returns an empty List.
     *
     * @return immutable list of mentioned TextChannels
     */
    List<TextChannel> getMentionedChannels();

    /**
     * A immutable list of all mentioned {@link net.dv8tion.jda.core.entities.Role Roles}. If none were mentioned, this list is empty.
     * <br>In {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannels} and {@link net.dv8tion.jda.client.entities.Group Groups},
     * this always returns an empty List.
     *
     * @return immutable list of mentioned Roles
     */
    List<Role> getMentionedRoles();

    /**
     * Indicates if this Message mentions everyone using @everyone or @here.
     * In {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel's}, this always returns false.
     *
     * @return True if message is mentioning everyone
     */
    boolean mentionsEveryone();

    /**
     * Returns whether or not this Message has been edited before.
     *
     * @return True if this message has been edited.
     */
    boolean isEdited();

    /**
     * Provides the {@link java.time.OffsetDateTime OffsetDateTime} defining when this Message was last
     * edited. If this Message has not been edited ({@link #isEdited()} is {@code false}), then this method
     * will return {@code null}.
     *
     * @return Time of the most recent edit, or {@code null} if the Message has never been edited.
     */
    OffsetDateTime getEditedTime();

    /**
     * The author of this Message
     *
     * @return Message author
     */
    User getAuthor();

    /**
     * Returns the author of this Message as a {@link net.dv8tion.jda.core.entities.Member member}.
     * <br>This is just a shortcut to {@link #getGuild()}{@link net.dv8tion.jda.core.entities.Guild#getMember(User) .getMember(getAuthor())}.
     * <br><b>This is only valid if the Message was actually sent in a TextChannel.</b> This will return {@code null}
     * if it was not sent from a TextChannel.
     * <br>You can check the type of channel this message was sent from using {@link #isFromType(ChannelType)} or {@link #getChannelType()}.
     *
     * @return Message author, or {@code null} if the message was not sent from a TextChannel.
     */
    Member getMember();

    /**
     * The textual content of this message in the format that would be shown to the Discord client. All
     * {@link net.dv8tion.jda.core.entities.IMentionable IMentionable} entities will be resolved to the format
     * shown by the Discord client instead of the {@literal <id>} format.
     *
     * <p>This includes resolving:
     * <br>{@link net.dv8tion.jda.core.entities.User Users} / {@link net.dv8tion.jda.core.entities.Member Members}
     * to their @Username/@Nickname format,
     * <br>{@link net.dv8tion.jda.core.entities.TextChannel TextChannels} to their #ChannelName format,
     * <br>{@link net.dv8tion.jda.core.entities.Role Roles} to their @RoleName format
     * <br>{@link net.dv8tion.jda.core.entities.Emote Emotes} (not emojis!) to their {@code :name:} format.
     *
     * <p>If you want the actual Content (mentions as {@literal <@id>}), use {@link #getRawContent()} instead
     *
     * @return The textual content of the message with mentions resolved to be visually like the Discord client.
     */
    String getContent();

    /**
     * The raw textual content of this message. Does not resolve {@link net.dv8tion.jda.core.entities.IMentionable IMentionable}
     * entities like {@link #getContent()} does. This means that this is the completely raw textual content of the message
     * received from Discord and can contain mentions specified by
     * <a href="https://discordapp.com/developers/docs/resources/channel#message-formatting" target="_blank">Discord's Message Formatting</a>.
     *
     * @return The raw textual content of the message, containing unresolved Discord message formatting.
     */
    String getRawContent();

    /**
     * Gets the textual content of this message using {@link #getContent()} and then strips it of all markdown characters
     * like {@literal *, **, __, ~~} that provide text formatting. Any characters that match these but are not being used
     * for formatting are escaped to prevent possible formatting.
     *
     * @return The textual content from {@link #getContent()} with all text formatting characters removed or escaped.
     */
    String getStrippedContent();

    /**
     * Used to determine if this Message was received from a {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     * of the {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} specified.
     * <br>This will always be false for {@link net.dv8tion.jda.core.entities.ChannelType#VOICE} as Messages can't be sent to
     * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}.
     *
     * <p>Useful for restricting functionality to a certain type of channels.
     *
     * @param  type
     *         The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} to check against.
     *
     * @return True if the {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} which this message was received
     *         from is the same as the one specified by {@code type}.
     */
    boolean isFromType(ChannelType type);

    /**
     * Gets the {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} that this message was received from.
     * <br>This will never be {@link net.dv8tion.jda.core.entities.ChannelType#VOICE} as Messages can't be sent to
     * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}.
     *
     * @return The ChannelType which this message was received from.
     */
    ChannelType getChannelType();

    /**
     * Indicates if this Message was sent by a {@link net.dv8tion.jda.core.entities.Webhook Webhook} instead of a
     * {@link net.dv8tion.jda.core.entities.User User}.
     * <br>Useful if you want to ignore non-users.
     *
     * @return True if this message was sent by a {@link net.dv8tion.jda.core.entities.Webhook Webhook}.
     */
    boolean isWebhookMessage();

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} that this message was sent in.
     *
     * @return The MessageChannel of this Message
     */
    MessageChannel getChannel();

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} that this message was sent in.
     * <br><b>This is only valid if the Message was actually sent in a PrivateChannel.</b> This will return {@code null}
     * if it was not sent from a PrivateChannel.
     * <br>You can check the type of channel this message was sent from using {@link #isFromType(ChannelType)} or {@link #getChannelType()}.
     *
     * <p>Use {@link #getChannel()} for an ambiguous {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     * if you do not need functionality specific to {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}.
     *
     * @return The PrivateChannel this message was sent in, or {@code null} if it was not sent from a PrivateChannel.
     */
    PrivateChannel getPrivateChannel();

    /**
     * Returns the {@link net.dv8tion.jda.client.entities.Group Group} that this message was sent in.
     * <br><b>This is only valid if the Message was actually sent in a Group.</b> This will return {@code null}
     * if it was not sent from a Group.
     * <br>You can check the type of channel this message was sent from using {@link #isFromType(ChannelType)} or {@link #getChannelType()}.
     *
     * <p>Use {@link #getChannel()} for an ambiguous {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     * if you do not need functionality specific to {@link net.dv8tion.jda.client.entities.Group Group}.
     *
     * @return The Group this message was sent in, or {@code null} if it was not sent from a Group.
     */
    Group getGroup();

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that this message was sent in.
     * <br><b>This is only valid if the Message was actually sent in a TextChannel.</b> This will return {@code null}
     * if it was not sent from a TextChannel.
     * <br>You can check the type of channel this message was sent from using {@link #isFromType(ChannelType)} or {@link #getChannelType()}.
     *
     * <p>Use {@link #getChannel()} for an ambiguous {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     * if you do not need functionality specific to {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     *
     * @return The TextChannel this message was sent in, or {@code null} if it was not sent from a TextChannel.
     */
    TextChannel getTextChannel();

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.Guild Guild} that this message was sent in.
     * <br>This is just a shortcut to {@link #getTextChannel()}{@link net.dv8tion.jda.core.entities.TextChannel#getGuild() .getGuild()}.
     * <br><b>This is only valid if the Message was actually sent in a TextChannel.</b> This will return {@code null}
     * if it was not sent from a TextChannel.
     * <br>You can check the type of channel this message was sent from using {@link #isFromType(ChannelType)} or {@link #getChannelType()}.
     *
     * @return The Guild this message was sent in, or {@code null} if it was not sent from a TextChannel.
     */
    Guild getGuild();

    /**
     * An unmodifiable list of {@link net.dv8tion.jda.core.entities.Message.Attachment Attachments} that are attached to this message.
     * <br>Most likely this will only ever be 1 {@link net.dv8tion.jda.core.entities.Message.Attachment Attachment} at most.
     *
     * @return Unmodifiable list of {@link net.dv8tion.jda.core.entities.Message.Attachment Attachments}.
     */
    List<Attachment> getAttachments();

    /**
     * An unmodifiable list of {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds} that are part of this
     * Message.
     *
     * @return Unmodifiable list of all given MessageEmbeds.
     */
    List<MessageEmbed> getEmbeds();

    /**
     * All {@link net.dv8tion.jda.core.entities.Emote Emotes} used in this Message.
     * <br><b>This only includes Custom Emotes, not UTF8 Emojis.</b> JDA classifies Emotes as the Custom Emojis uploaded
     * to a Guild and retrievable with {@link net.dv8tion.jda.core.entities.Guild#getEmotes()}. These are not the same
     * as the UTF8 emojis that Discord also supports.
     * <p>
     * <b>This may or may not contain fake Emotes which means they can be displayed but not used by the logged in account.</b>
     * To check whether an Emote is fake you can test if {@link Emote#isFake()} returns true.
     *
     * <p><b><u>Unicode emojis are not included as {@link net.dv8tion.jda.core.entities.Emote Emote}!</u></b>
     *
     * @return An immutable list of the Emotes used in this message (example match {@literal <:jda:230988580904763393>})
     */
    List<Emote> getEmotes();

    /**
     * All {@link net.dv8tion.jda.core.entities.MessageReaction MessageReactions} that are on this Message.
     *
     * @return immutable list of all MessageReactions on this message.
     */
    List<MessageReaction> getReactions();

    /**
     * Defines whether or not this Message triggers TTS (Text-To-Speech).
     *
     * @return If this message is TTS.
     */
    boolean isTTS();

    /**
     * Edits this Message's content to the provided String.
     * <br><b>Messages can only be edited by the account that sent them!</b>.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The edit was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The edit was attempted after the account lost {@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE} in
     *         the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The edit was attempted after the Message had been deleted.</li>
     * </ul>
     *
     * @param  newContent
     *         the new content of the Message
     *
     * @throws java.lang.IllegalStateException
     *         If the message attempting to be edited was not created by the currently logged in account, or if
     *         {@code newContent}'s length is 0 or greater than 2000.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Message Message}
     *     <br>The {@link net.dv8tion.jda.core.entities.Message Message} with the updated content
     */
    RestAction<Message> editMessage(String newContent);

    /**
     * Edits this Message's content to the provided {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}.
     * <br><b>Messages can only be edited by the account that sent them!</b>.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The edit was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The edit was attempted after the account lost {@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE} in
     *         the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The edit was attempted after the Message had been deleted.</li>
     * </ul>
     *
     * @param  newContent
     *         the new content of the Message
     *
     * @throws java.lang.IllegalStateException
     *         If the message attempting to be edited was not created by the currently logged in account, or
     *         if the passed-in embed is {@code null}
     *         or not {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) sendable}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Message Message}
     *     <br>The {@link net.dv8tion.jda.core.entities.Message Message} with the updated content
     */
    RestAction<Message> editMessage(MessageEmbed newContent);

    /**
     * Edits this Message's content to the provided {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}.
     * <br>Shortcut for {@link net.dv8tion.jda.core.MessageBuilder#appendFormat(String, Object...)}.
     * <br><b>Messages can only be edited by the account that sent them!</b>.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The edit was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The edit was attempted after the account lost {@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE} in
     *         the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The edit was attempted after the Message had been deleted.</li>
     * </ul>
     *
     * @param  format
     *         Format String used to generate the Message's content via
     *         {@link net.dv8tion.jda.core.MessageBuilder#appendFormat(String, Object...)} specification
     * @param  args
     *         The arguments to use in order to be converted in the format string
     *
     * @throws IllegalArgumentException
     *         If the provided format String is {@code null} or blank, or if
     *         the created message exceeds the 2000 character limit
     * @throws IllegalStateException
     *         If the message attempting to be edited was not created by the currently logged in account
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Message Message}
     *     <br>The {@link net.dv8tion.jda.core.entities.Message Message} with the updated content
     */
    RestAction<Message> editMessageFormat(String format, Object... args);

    /**
     * Edits this Message's content to the provided {@link net.dv8tion.jda.core.entities.Message Message}.
     * <br><b>Messages can only be edited by the account that sent them!</b>.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The edit was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The edit was attempted after the account lost {@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE} in
     *         the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The edit was attempted after the Message had been deleted.</li>
     * </ul>
     *
     * @param  newContent
     *         the new content of the Message
     *
     * @throws java.lang.IllegalStateException
     *         <ul>
     *             <li>If the message attempting to be edited was not created by the currently logged in account</li>
     *             <li>If the message contains a MessageEmebd that is not
     *                 {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) sendable}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Message Message}
     *     <br>The {@link net.dv8tion.jda.core.entities.Message Message} with the updated content
     */
    RestAction<Message> editMessage(Message newContent);

    /**
     * Deletes this Message from Discord.
     * <br>If this Message was not sent by the currently logged in account, then this will fail unless the Message is from
     * a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the current account has
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the channel.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The delete was attempted after the account lost access to the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         due to {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} being revoked, or the
     *         account lost access to the {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The delete was attempted after the account lost {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in
     *         the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} when deleting another Member's message
     *         or lost {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *         The pin was attempted after the Message had been deleted.</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this Message was not sent by the currently logged in account, the Message was sent in a
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}, and the currently logged in account
     *         does not have {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in
     *         the channel.
     * @throws java.lang.IllegalStateException
     *         If this Message was not sent by the currently logged in account and it was <b>not</b> sent in a
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    AuditableRestAction<Void> delete();

    /**
     * Returns the {@link net.dv8tion.jda.core.JDA JDA} instance related to this Message.
     *
     * @return  the corresponding JDA instance
     */
    JDA getJDA();

    /**
     * Whether or not this Message has been pinned in its parent channel.
     *
     * @return True - if this message has been pinned.
     */
    boolean isPinned();

    /**
     * Used to add the Message to the {@link #getChannel() MessageChannel's} pinned message list.
     * <br>This is a shortcut method to {@link MessageChannel#pinMessageById(String)}.
     *
     * <p>The success or failure of this action will not affect the return of {@link #isPinned()}.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The pin request was attempted after the account lost access to the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         due to {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} being revoked, or the
     *         account lost access to the {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The pin request was attempted after the account lost {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in
     *         the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *         The pin request was attempted after the Message had been deleted.</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this Message is from a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and:
     *         <br><ul>
     *             <li>Missing {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}.
     *             <br>The account needs access the the channel to pin a message in it.</li>
     *             <li>Missing {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}.
     *             <br>Required to actually pin the Message.</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link java.lang.Void}
     */
    RestAction<Void> pin();

    /**
     * Used to remove the Message from the {@link #getChannel() MessageChannel's} pinned message list.
     * <br>This is a shortcut method to {@link MessageChannel#unpinMessageById(String)}.
     *
     * <p>The success or failure of this action will not affect the return of {@link #isPinned()}.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The unpin request was attempted after the account lost access to the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         due to {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} being revoked, or the
     *         account lost access to the {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The unpin request was attempted after the account lost {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in
     *         the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *         The unpin request was attempted after the Message had been deleted.</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this Message is from a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and:
     *         <br><ul>
     *             <li>Missing {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}.
     *             <br>The account needs access the the channel to pin a message in it.</li>
     *             <li>Missing {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}.
     *             <br>Required to actually pin the Message.</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link java.lang.Void}
     */
    RestAction<Void> unpin();

    /**
     * Adds a reaction to this Message using an {@link net.dv8tion.jda.core.entities.Emote Emote}.
     *
     * <p>Reactions are the small emoji/emotes below a message that have a counter beside them
     * showing how many users have reacted with same emoji/emote.
     *
     * <p><b>Neither success nor failure of this request will affect this Message's {@link #getReactions()} return as Message is immutable.</b>
     *
     * <p><b><u>Unicode emojis are not included as {@link net.dv8tion.jda.core.entities.Emote Emote}!</u></b>
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The reaction request was attempted after the account lost access to the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         due to {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} being revoked, or the
     *         account lost access to the {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed.
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The reaction request was attempted after the account lost {@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION}
     *         in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} when adding the reaction.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *         The reaction request was attempted after the Message had been deleted.</li>
     * </ul>
     *
     * @param emote
     *        The {@link net.dv8tion.jda.core.entities.Emote Emote} to add as a reaction to this Message.
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the MessageChannel this message was sent in was a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided {@link net.dv8tion.jda.core.entities.Emote Emote} is null.</li>
     *             <li>If the provided {@link net.dv8tion.jda.core.entities.Emote Emote} is fake {@link net.dv8tion.jda.core.entities.Emote#isFake() Emote.isFake()}.</li>
     *             <li>If the provided {@link net.dv8tion.jda.core.entities.Emote Emote} cannot be used in the current channel.
     *                 See {@link Emote#canInteract(User, MessageChannel)} or {@link Emote#canInteract(Member)} for more information.</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link java.lang.Void}
     */
    RestAction<Void> addReaction(Emote emote);

    /**
     * Adds a reaction to this Message using a UTF8 emoji.
     * <br>A reference of UTF8 emojis can be found here:
     * <a href="http://unicode.org/emoji/charts/full-emoji-list.html" target="_blank">Emoji Table</a>.
     *
     * <p>Reactions are the small emoji/emotes below a message that have a counter beside them
     * showing how many users have reacted with same emoji/emote.
     *
     * <p><b>Neither success nor failure of this request will affect this Message's {@link #getReactions()} return as Message is immutable.</b>
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The reaction request was attempted after the account lost access to the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         due to {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} being revoked, or the
     *         account lost access to the {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed.
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The reaction request was attempted after the account lost {@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION}
     *         in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} when adding the reaction.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *         The reaction request was attempted after the Message had been deleted.</li>
     * </ul>
     *
     * @param unicode
     *        The UTF8 emoji to add as a reaction to this Message.
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the MessageChannel this message was sent in was a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the channel.
     * @throws java.lang.IllegalArgumentException
     *         If the provided unicode emoji is null or empty.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link java.lang.Void}
     */
    RestAction<Void> addReaction(String unicode);

    /**
     * Removes all reactions from this Message.
     * <br>This is useful for moderator commands that wish to remove all reactions at once from a specific message.
     *
     * <p><b>Neither success nor failure of this request will affect this Message's {@link #getReactions()} return as Message is immutable.</b>
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The clear-reactions request was attempted after the account lost access to the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         due to {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} being revoked, or the
     *         account lost access to the {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The clear-reactions request was attempted after the account lost {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}
     *         in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} when adding the reaction.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *         The clear-reactions request was attempted after the Message had been deleted.</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the MessageChannel this message was sent in was a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         and the currently logged in account does not have
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the channel.
     * @throws java.lang.IllegalStateException
     *         If this message was <b>not</b> sent in a
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link java.lang.Void}
     */
    RestAction<Void> clearReactions();

    /**
     * This specifies the {@link net.dv8tion.jda.core.entities.MessageType MessageType} of this Message.
     *
     * <p>Messages can represent more than just simple text sent by Users, they can also be special messages that
     * inform about events occurs. A few examples are the system message informing that a message has been pinned.
     * Another would be the system message informing that a call has been started or ended in a group.
     *
     * @return The {@link net.dv8tion.jda.core.entities.MessageType MessageType} of this message.
     */
    MessageType getType();

    /**
     * Represents a {@link net.dv8tion.jda.core.entities.Message Message} file attachment.
     */
    class Attachment
    {
        private final String id;
        private final String url;
        private final String proxyUrl;
        private final String fileName;
        private final int size;
        private final int height;
        private final int width;
        private final JDAImpl jda;

        public Attachment(String id, String url, String proxyUrl, String fileName, int size, int height, int width, JDA jda)
        {
            this.id = id;
            this.url = url;
            this.proxyUrl = proxyUrl;
            this.fileName = fileName;
            this.size = size;
            this.height = height;
            this.width = width;
            this.jda = (JDAImpl) jda;
        }

        /**
         * The id of the attachment. This is not the id of the message that the attachment was attached to.
         *
         * @return Non-null String containing the Attachment ID.
         */
        public String getId()
        {
            return id;
        }

        /**
         * The url of the Attachment, most likely on the Discord servers.
         *
         * @return Non-null String containing the Attachment URL.
         */
        public String getUrl()
        {
            return url;
        }

        /**
         * The url of the Attachment, proxied by Discord.
         * <br>Url to the resource proxied by https://images.discordapp.net
         * <br><b>Note: </b> This URL will most likely only work for images. ({@link #isImage()})
         *
         * @return Non-null String containing the proxied Attachment url.
         */
        public String getProxyUrl()
        {
            return proxyUrl;
        }

        /**
         * The file name of the Attachment when it was first uploaded.
         *
         * @return Non-null String containing the Attachment file name.
         */
        public String getFileName()
        {
            return fileName;
        }

        /**
         * Downloads this attachment to given File
         *
         * @param  file
         *         The file, where the attachment will get downloaded to
         *
         * @return boolean true, if successful, otherwise false
         */
        public boolean download(File file)
        {
            InputStream in = null;
            try
            {
                Request request = new Request.Builder().addHeader("user-agent", Requester.USER_AGENT).url(getUrl()).build();
                Response response = jda.getRequester().getHttpClient().newCall(request).execute();
                in = response.body().byteStream();
                Files.copy(in, Paths.get(file.getAbsolutePath()));
                return true;
            }
            catch (Exception e)
            {
                JDAImpl.LOG.log(e);
            }
            finally
            {
                if (in != null)
                {
                    try {in.close();} catch(Exception ignored) {}
                }
            }
            return false;
        }

        /**
         * The size of the attachment in bytes.
         * <br>Example: if {@link #getSize() getSize()} returns 1024, then the attachment is 1024 bytes, or 1KB, in size.
         *
         * @return Positive int containing the size of the Attachment.
         */
        public int getSize()
        {
            return size;
        }

        /**
         * The height of the Attachment if this Attachment is an image.
         * <br>If this Attachment is not an image, this returns 0.
         *
         * @return Never-negative int containing image Attachment height.
         */
        public int getHeight()
        {
            return height;
        }

        /**
         * The width of the Attachment if this Attachment is an image.
         * <br>If this Attachment is not an image, this returns 0.
         *
         * @return Never-negative int containing image Attachment width.
         */
        public int getWidth()
        {
            return width;
        }

        /**
         * Whether or not this attachment is an Image.
         * <br>Based on the values of getHeight and getWidth being larger than zero.
         *
         * @return True if width and height are greater than zero.
         */
        public boolean isImage()
        {
            return height > 0 && width > 0;
        }
    }
}
