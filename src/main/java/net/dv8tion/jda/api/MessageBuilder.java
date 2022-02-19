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
package net.dv8tion.jda.api;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.internal.entities.DataMessage;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Builder system used to build {@link net.dv8tion.jda.api.entities.Message Messages}.
 * <br>Internally the builder uses a {@link java.lang.StringBuilder} to take advantage of the efficiencies offered by the
 * StringBuilder, and the methods provided by this class are a combination of those offered by the StringBuilder and
 * {@link String#format(String, Object...)}.
 *
 * @since  1.0
 * @author Michael Ritter
 * @author Aljoscha Grebe
 */
public class MessageBuilder implements Appendable
{
    protected final StringBuilder builder = new StringBuilder();

    protected final List<MessageEmbed> embeds = new ArrayList<>();
    protected final List<LayoutComponent> components = new ArrayList<>();
    protected boolean isTTS = false;
    protected String nonce;
    protected EnumSet<Message.MentionType> allowedMentions = null;
    protected Set<String> mentionedUsers = new HashSet<>();
    protected Set<String> mentionedRoles = new HashSet<>();

    public MessageBuilder() {}

    public MessageBuilder(@Nullable CharSequence content)
    {
        if (content != null)
            builder.append(content);
    }

    public MessageBuilder(@Nullable Message message)
    {
        if (message != null)
        {
            isTTS = message.isTTS();
            builder.append(message.getContentRaw());
            List<MessageEmbed> embeds = message.getEmbeds();
            if (embeds != null)
                embeds.stream().filter(it -> it.getType() == EmbedType.RICH).forEach(this.embeds::add);
            components.addAll(message.getActionRows());
            if (message instanceof DataMessage)
            {
                DataMessage data = (DataMessage) message;
                if (data.getAllowedMentions() != null)
                    this.allowedMentions = Helpers.copyEnumSet(Message.MentionType.class, data.getAllowedMentions());
                Collections.addAll(this.mentionedUsers, data.getMentionedUsersWhitelist());
                Collections.addAll(this.mentionedRoles, data.getMentionedRolesWhitelist());
            }
        }
    }

    public MessageBuilder(@Nullable MessageBuilder builder)
    {
        if (builder != null)
        {
            this.isTTS = builder.isTTS;
            this.builder.append(builder.builder);
            this.nonce = builder.nonce;
            this.embeds.addAll(builder.embeds);
            this.components.addAll(builder.components);
            if (builder.allowedMentions != null)
                this.allowedMentions = Helpers.copyEnumSet(Message.MentionType.class, builder.allowedMentions);
            this.mentionedRoles.addAll(builder.mentionedRoles);
            this.mentionedUsers.addAll(builder.mentionedUsers);
        }
    }

    public MessageBuilder(@Nullable EmbedBuilder builder)
    {
        if (builder != null)
            this.embeds.add(builder.build());
    }

    public MessageBuilder(@Nullable MessageEmbed embed)
    {
        if (embed != null)
            this.embeds.add(embed);
    }

    /**
     * Makes the created Message a TTS message.
     * <br>TTS stands for Text-To-Speech. When a TTS method is received by the Discord client,
     * it is vocalized so long as the user has not disabled TTS.
     *
     * @param  tts
     *         whether the created Message should be a tts message
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder setTTS(boolean tts)
    {
        this.isTTS = tts;
        return this;
    }

    /**
     * Adds up to 10 {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} to the Message. Embeds can be built using
     * the {@link net.dv8tion.jda.api.EmbedBuilder} and offer specialized formatting.
     *
     * @param  embeds
     *         the embeds to add, or empty array to remove
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided MessageEmbeds is null or not sendable according to {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() MessageEmbed.isSendable()}!
     *         The sum of all {@link MessageEmbed#getLength()} must not be greater than {@link MessageEmbed#EMBED_MAX_LENGTH_BOT}!
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder setEmbeds(@Nonnull MessageEmbed... embeds)
    {
        Checks.noneNull(embeds, "MessageEmbeds");
        return setEmbeds(Arrays.asList(embeds));
    }

    /**
     * Adds up to 10 {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} to the Message. Embeds can be built using
     * the {@link net.dv8tion.jda.api.EmbedBuilder} and offer specialized formatting.
     *
     * @param  embeds
     *         the embeds to add, or empty list to remove
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided MessageEmbeds is null or not sendable according to {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() MessageEmbed.isSendable()}!
     *         The sum of all {@link MessageEmbed#getLength()} must not be greater than {@link MessageEmbed#EMBED_MAX_LENGTH_BOT}!
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder setEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {

        Checks.noneNull(embeds, "MessageEmbeds");
        embeds.forEach(embed ->
            Checks.check(embed.isSendable(),
                "Provided Message contains an empty embed or an embed with a length greater than %d characters, which is the max for bot accounts!",
                MessageEmbed.EMBED_MAX_LENGTH_BOT)
        );
        Checks.check(embeds.size() <= 10, "Cannot have more than 10 embeds in a message!");
        Checks.check(embeds.stream().mapToInt(MessageEmbed::getLength).sum() <= MessageEmbed.EMBED_MAX_LENGTH_BOT, "The sum of all MessageEmbeds may not exceed %d!", MessageEmbed.EMBED_MAX_LENGTH_BOT);
        this.embeds.clear();
        this.embeds.addAll(embeds);
        return this;
    }

    /**
     * Set the action rows for the message.
     *
     * @param  rows
     *         The new action rows, or null to reset the components
     *
     * @throws IllegalArgumentException
     *         If null is provided in the collection or more than 5 actions rows are provided
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder setActionRows(@Nullable Collection<? extends ActionRow> rows)
    {
        if (rows == null)
        {
            this.components.clear();
            return this;
        }
        Checks.noneNull(rows, "ActionRows");
        Checks.check(rows.size() <= 5, "Can only have 5 action rows per message!");
        this.components.clear();
        this.components.addAll(rows);
        return this;
    }

    /**
     * Set the action rows for the message.
     *
     * @param  rows
     *         The new action rows, or null to reset the components
     *
     * @throws IllegalArgumentException
     *         If null is provided in the array or more than 5 actions rows are provided
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder setActionRows(@Nullable ActionRow... rows)
    {
        if (rows == null)
        {
            this.components.clear();
            return this;
        }
        return setActionRows(Arrays.asList(rows));
    }

    /**
     * Sets the <a href="https://en.wikipedia.org/wiki/Cryptographic_nonce" target="_blank">nonce</a>
     * of the built message(s). It is recommended to have only 100% unique strings to validate messages via this nonce.
     * <br>The nonce will be available from the resulting message via {@link net.dv8tion.jda.api.entities.Message#getNonce() Message.getNonce()}
     * in message received by events and RestAction responses.
     * <br>When {@code null} is provided no nonce will be used.
     *
     * @param  nonce
     *         Validation nonce string
     *
     * @return The MessageBuilder instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.api.entities.Message#getNonce()
     * @see    <a href="https://en.wikipedia.org/wiki/Cryptographic_nonce" target="_blank">Cryptographic Nonce - Wikipedia</a>
     */
    @Nonnull
    public MessageBuilder setNonce(@Nullable String nonce)
    {
        this.nonce = nonce;
        return this;
    }

    /**
     * Sets the content of the resulting Message
     * <br>This will replace already added content.
     *
     * @param  content
     *         The content to use, or {@code null} to reset the content
     *
     * @return The MessageBuilder instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.api.entities.Message#getContentRaw()
     */
    @Nonnull
    public MessageBuilder setContent(@Nullable String content)
    {
        if (content == null)
        {
            builder.setLength(0);
        }
        else
        {
            final int newLength = Math.max(builder.length(), content.length());
            builder.replace(0, newLength, content);
        }
        return this;
    }

    @Nonnull
    @Override
    public MessageBuilder append(@Nullable CharSequence text)
    {
        builder.append(text);
        return this;
    }

    @Nonnull
    @Override
    public MessageBuilder append(@Nullable CharSequence text, int start, int end)
    {
        builder.append(text, start, end);
        return this;
    }

    @Nonnull
    @Override
    public MessageBuilder append(char c)
    {
        builder.append(c);
        return this;
    }

    /**
     * Appends the string representation of an object to the Message.
     * <br>This is the same as {@link #append(CharSequence) append(String.valueOf(object))}
     * 
     * @param  object
     *         the object to append
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder append(@Nullable Object object)
    {
        return append(String.valueOf(object));
    }

    /**
     * Appends a mention to the Message.
     * <br>Typical usage would be providing an {@link net.dv8tion.jda.api.entities.IMentionable IMentionable} like
     * {@link net.dv8tion.jda.api.entities.User User} or {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.
     *
     * <p>This will not add a rule to mention a {@link User} or {@link Role}. You have to use {@link #mention(IMentionable...)}
     * in addition to this method.
     *
     * @param  mention
     *         the mention to append
     *
     * @throws java.lang.IllegalArgumentException
     *         If provided with null
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder append(@Nonnull IMentionable mention)
    {
        Checks.notNull(mention, "Mentionable");
        builder.append(mention.getAsMention());
        return this;
    }

    /**
     * Appends a String using the specified chat {@link net.dv8tion.jda.api.MessageBuilder.Formatting Formatting(s)}.
     *
     * @param  text
     *         the text to append.
     * @param  format
     *         the format(s) to apply to the text.
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder append(@Nullable CharSequence text, @Nonnull Formatting... format)
    {
        boolean blockPresent = false;
        for (Formatting formatting : format)
        {
            if (formatting == Formatting.BLOCK)
            {
                blockPresent = true;
                continue;
            }
            builder.append(formatting.getTag());
        }
        if (blockPresent)
            builder.append(Formatting.BLOCK.getTag());

        builder.append(text);

        if (blockPresent)
            builder.append(Formatting.BLOCK.getTag());
        for (int i = format.length - 1; i >= 0; i--)
        {
            if (format[i] == Formatting.BLOCK) continue;
            builder.append(format[i].getTag());
        }
        return this;
    }

    /**
     * This method is an extended form of {@link String#format(String, Object...)}. It allows for all of
     * the token replacement functionality that String.format(String, Object...) supports.
     * <br>A lot of JDA entities implement {@link java.util.Formattable Formattable} and will provide
     * specific format outputs for their specific type.
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.entities.IMentionable IMentionable}
     *     <br>These will output their {@link net.dv8tion.jda.api.entities.IMentionable#getAsMention() getAsMention} by default,
     *         some implementations have alternatives such as {@link net.dv8tion.jda.api.entities.User User} and {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *     <li>{@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}
     *     <br>All message channels format to {@code "#" + getName()} by default, TextChannel has special handling
     *         and uses the getAsMention output by default and the MessageChannel output as alternative ({@code #} flag).</li>
     *     <li>{@link net.dv8tion.jda.api.entities.Message Message}
     *     <br>Messages by default output their {@link net.dv8tion.jda.api.entities.Message#getContentDisplay() getContentDisplay()} value and
     *         as alternative use the {@link net.dv8tion.jda.api.entities.Message#getContentRaw() getContentRaw()} value</li>
     * </ul>
     *
     * <p>Example:
     * <br>If you placed the following code in an method handling a
     * {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent MessageReceivedEvent}
     * <br><pre>{@code
     * User user = event.getAuthor();
     * MessageBuilder builder = new MessageBuilder();
     * builder.appendFormat("%#s is really cool!", user);
     * builder.build();
     * }</pre>
     *
     * It would build a message that mentions the author and says that he is really cool!. If the user's
     * name was "Minn" and his discriminator "6688", it would say:
     * <br><pre>  "Minn#6688 is really cool!"</pre>
     * <br>Note that this uses the {@code #} flag to utilize the alternative format for {@link net.dv8tion.jda.api.entities.User User}.
     * <br>By default it would fallback to {@link net.dv8tion.jda.api.entities.IMentionable#getAsMention()}
     *
     * @param  format
     *         a format string.
     * @param  args
     *         an array objects that will be used to replace the tokens, they must be
     *         provided in the order that the tokens appear in the provided format string.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided format string is {@code null} or empty
     * @throws java.util.IllegalFormatException
     *         If a format string contains an illegal syntax,
     *         a format specifier that is incompatible with the given arguments,
     *         insufficient arguments given the format string, or other illegal conditions.
     *         For specification of all possible formatting errors,
     *         see the <a href="../util/Formatter.html#detail">Details</a>
     *         section of the formatter class specification.
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder appendFormat(@Nonnull String format, @Nonnull Object... args)
    {
        Checks.notEmpty(format, "Format String");
        this.append(String.format(format, args));
        return this;
    }
    
    /**
     * Appends a code-line to the Message.
     * Code Lines are similar to code-blocks, however they are displayed in-line and do not support language specific highlighting.
     *
     * @param  text
     *         the code to append
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder appendCodeLine(@Nullable CharSequence text)
    {
        this.append(text, Formatting.BLOCK);
        return this;
    }

    /**
     * Appends a code-block to the Message.
     * <br>Discord uses <a href="https://highlightjs.org/">Highlight.js</a> for its language highlighting support. You can find out what
     * specific languages are supported <a href="https://github.com/isagalaev/highlight.js/tree/master/src/languages">here</a>.
     *
     * @param  text
     *         the code to append
     * @param  language
     *         the language of the code. If unknown use an empty string
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder appendCodeBlock(@Nullable CharSequence text, @Nullable CharSequence language)
    {
        builder.append("```").append(language).append('\n').append(text).append("\n```");
        return this;
    }

    /**
     * Returns the current length of the content that will be built into a {@link net.dv8tion.jda.api.entities.Message Message}
     * when {@link #build()} is called.
     * <br>If this value is {@code 0} (and there is no embed) or greater than {@code 2000} when {@link #build()} is called, an exception
     * will be raised as you cannot send an empty message to Discord and Discord has a hard limit of 2000 characters per message.
     *
     * <p><b>Hint:</b> You can use {@link #build(int, int)} or
     * {@link #buildAll(net.dv8tion.jda.api.MessageBuilder.SplitPolicy...) buildAll(SplitPolicy...)} as possible ways to
     * deal with the 2000 character cap.
     *
     * @return the current length of the content that will be built into a Message.
     */
    public int length()
    {
        return builder.length();
    }

    /**
     * Checks if the message contains any contend. This includes text as well as embeds.
     * 
     * @return whether the message contains content
     */
    public boolean isEmpty() {
        return builder.length() == 0 && embeds.isEmpty();
    }

    /**
     * Replaces each substring that matches the target string with the specified replacement string.
     * The replacement proceeds from the beginning of the string to the end, for example, replacing
     * "aa" with "b" in the message "aaa" will result in "ba" rather than "ab".
     *
     * @param  target
     *         the sequence of char values to be replaced
     * @param  replacement
     *         the replacement sequence of char values
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder replace(@Nonnull String target, @Nonnull String replacement)
    {
        int index = builder.indexOf(target);
        while (index != -1)
        {
            builder.replace(index, index + target.length(), replacement);
            index = builder.indexOf(target, index + replacement.length());
        }
        return this;
    }

    /**
     * Replaces the first substring that matches the target string with the specified replacement string.
     *
     * @param  target
     *         the sequence of char values to be replaced
     * @param  replacement
     *         the replacement sequence of char values
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder replaceFirst(@Nonnull String target, @Nonnull String replacement)
    {
        int index = builder.indexOf(target);
        if (index != -1)
        {
            builder.replace(index, index + target.length(), replacement);
        }
        return this;
    }

    /**
     * Replaces the last substring that matches the target string with the specified replacement string.
     *
     * @param  target
     *         the sequence of char values to be replaced
     * @param  replacement
     *         the replacement sequence of char values
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder replaceLast(@Nonnull String target, @Nonnull String replacement)
    {
        int index = builder.lastIndexOf(target);
        if (index != -1)
        {
            builder.replace(index, index + target.length(), replacement);
        }
        return this;
    }

    /**
     * Removes the whitelist of mentioned users.
     * <br>If {@link #setAllowedMentions(Collection)} does not contain {@link net.dv8tion.jda.api.entities.Message.MentionType#USER MentionType.USER}
     * then no user will be mentioned.
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder clearMentionedUsers()
    {
        mentionedUsers.clear();
        return this;
    }

    /**
     * Removes the whitelist of mentioned roles.
     * <br>If {@link #setAllowedMentions(Collection)} does not contain {@link net.dv8tion.jda.api.entities.Message.MentionType#ROLE MentionType.ROLE}
     * then no role will be mentioned.
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder clearMentionedRoles()
    {
        mentionedRoles.clear();
        return this;
    }

    /**
     * Combination of {@link #clearMentionedRoles()} and {@link #clearMentionedUsers()}.
     *
     * <p>This will not affect {@link #setAllowedMentions(Collection)}. You can reset those to default
     * by using {@code setAllowedMentions(null)}.
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder clearMentions()
    {
        return clearMentionedUsers().clearMentionedRoles();
    }

    /**
     * Sets which {@link net.dv8tion.jda.api.entities.Message.MentionType MentionTypes} should be parsed from
     * the input. This will use {@link MessageAction#getDefaultMentions()} by default, or if {@code null} is provided.
     *
     * @param  mentionTypes
     *         Collection of allowed mention types, or null to use {@link MessageAction#getDefaultMentions()}.
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder setAllowedMentions(@Nullable Collection<Message.MentionType> mentionTypes)
    {
        this.allowedMentions = mentionTypes == null
                ? MessageAction.getDefaultMentions()
                : Helpers.copyEnumSet(Message.MentionType.class, mentionTypes);
        return this;
    }

    /**
     * Adds the provided {@link net.dv8tion.jda.api.entities.Message.MentionType MentionTypes} to the whitelist.
     *
     * @param  types
     *         The mention types to allow
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder allowMentions(@Nonnull Message.MentionType... types)
    {
        Checks.noneNull(types, "MentionTypes");
        if (types.length > 0)
        {
            if (allowedMentions == null)
                allowedMentions = MessageAction.getDefaultMentions();
            Collections.addAll(allowedMentions, types);
        }
        return this;
    }

    /**
     * Removes the provided {@link net.dv8tion.jda.api.entities.Message.MentionType MentionTypes} from the whitelist.
     *
     * @param  types
     *         The mention types to deny
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder denyMentions(@Nonnull Message.MentionType... types)
    {
        Checks.noneNull(types, "MentionTypes");
        if (types.length > 0)
        {
            if (allowedMentions == null)
                allowedMentions = MessageAction.getDefaultMentions();
            for (Message.MentionType type : types)
                allowedMentions.remove(type);
        }
        return this;
    }

    /**
     * Adds the provided {@link IMentionable IMentionable} instance to the whitelist of mentions.
     * <br>This will only affect instances of {@link User}, {@link Member}, and {@link Role}.
     * <br>The content will not be affected by this. To append a mention use {@link #append(IMentionable)}.
     *
     * <p>See {@link MessageAction#mention(IMentionable...)} for more details.
     *
     * @param  mentions
     *         Whitelist of mentions to apply
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The MessageBuilder instance. Useful for chaining.
     *
     * @see    #clearMentions()
     * @see    MessageAction#mention(IMentionable...)
     */
    @Nonnull
    public MessageBuilder mention(@Nonnull IMentionable... mentions)
    {
        Checks.noneNull(mentions, "Mentions");

        for (IMentionable mention : mentions)
        {
            if (mention instanceof User || mention instanceof Member)
                mentionedUsers.add(mention.getId());
            else if (mention instanceof Role)
                mentionedRoles.add(mention.getId());
        }
        return this;
    }

    /**
     * Adds the provided {@link IMentionable IMentionable} instance to the whitelist of mentions.
     * <br>This will only affect instances of {@link User}, {@link Member}, and {@link Role}.
     * <br>The content will not be affected by this. To append a mention use {@link #append(IMentionable)}.
     *
     * <p>See {@link MessageAction#mention(IMentionable...)} for more details.
     *
     * @param  mentions
     *         Whitelist of mentions to apply
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The MessageBuilder instance. Useful for chaining.
     *
     * @see    #clearMentions()
     * @see    MessageAction#mention(IMentionable...)
     */
    @Nonnull
    public MessageBuilder mention(@Nonnull Collection<? extends IMentionable> mentions)
    {
        Checks.noneNull(mentions, "Mentions");
        return mention(mentions.toArray(new IMentionable[0]));
    }

    /**
     * Adds the provided {@link User Users} to the whitelist of mentions.
     * <br>The provided list must only contain IDs of users.
     * <br>The content will not be affected by this.
     * To append a mention use {@code append("<@").append(id).append(">")}.
     *
     * <p>See {@link MessageAction#mentionUsers(String...)} for more details.
     *
     * @param  users
     *         Whitelist of user IDs to apply
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The MessageBuilder instance. Useful for chaining.
     *
     * @see    #clearMentionedUsers()
     * @see    MessageAction#mentionUsers(String...)
     */
    @Nonnull
    public MessageBuilder mentionUsers(@Nonnull String... users)
    {
        Checks.noneNull(users, "Users");
        Collections.addAll(mentionedUsers, users);
        return this;
    }

    /**
     * Adds the provided {@link Role Roles} to the whitelist of mentions.
     * <br>The provided list must only contain IDs of roles.
     * <br>The content will not be affected by this.
     * To append a mention use {@code append("<@&").append(id).append(">")}.
     *
     * <p>See {@link MessageAction#mentionRoles(String...)} for more details.
     *
     * @param  roles
     *         Whitelist of role IDs to apply
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The MessageBuilder instance. Useful for chaining.
     *
     * @see    #clearMentionedRoles()
     * @see    MessageAction#mentionRoles(String...)
     */
    @Nonnull
    public MessageBuilder mentionRoles(@Nonnull String... roles)
    {
        Checks.noneNull(roles, "Roles");
        Collections.addAll(mentionedRoles, roles);
        return this;
    }

    /**
     * Adds the provided {@link User Users} to the whitelist of mentions.
     * <br>The provided list must only contain IDs of users.
     * <br>The content will not be affected by this.
     * To append a mention use {@code append("<@").append(id).append(">")}.
     *
     * <p>See {@link MessageAction#mentionUsers(long...)} for more details.
     *
     * @param  users
     *         Whitelist of user IDs to apply
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The MessageBuilder instance. Useful for chaining.
     *
     * @see    #clearMentionedUsers()
     * @see    MessageAction#mentionUsers(long...)
     */
    @Nonnull
    public MessageBuilder mentionUsers(@Nonnull long... users)
    {
        Checks.notNull(users, "Users");
        return mentionUsers(toStringArray(users));
    }

    /**
     * Adds the provided {@link Role Roles} to the whitelist of mentions.
     * <br>The provided list must only contain IDs of roles.
     * <br>The content will not be affected by this.
     * To append a mention use {@code append("<@&").append(id).append(">")}.
     *
     * <p>See {@link MessageAction#mentionRoles(long...)} for more details.
     *
     * @param  roles
     *         Whitelist of role IDs to apply
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The MessageBuilder instance. Useful for chaining.
     *
     * @see    #clearMentionedRoles()
     * @see    MessageAction#mentionRoles(long...)
     */
    @Nonnull
    public MessageBuilder mentionRoles(@Nonnull long... roles)
    {
        Checks.notNull(roles, "Roles");
        return mentionRoles(toStringArray(roles));
    }

    /**
     * Returns the underlying {@link StringBuilder}.
     *
     * @return The {@link StringBuilder} used by this {@link MessageBuilder}
     */
    @Nonnull
    public StringBuilder getStringBuilder()
    {
        return this.builder;
    }

    /**
     * Clears the current builder. Useful for mass message creation.
     *
     * <p>This will not clear the allowed mentions.
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    @Nonnull
    public MessageBuilder clear() {
        this.builder.setLength(0);
        this.embeds.clear();
        this.isTTS = false;
        return this;
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring between the specified indices.
     *
     * <p>If no such value of {@code target} exists, then {@code -1} is returned.
     *
     * @param  target
     *         the substring to search for.
     * @param  fromIndex
     *         the index from which to start the search.
     * @param  endIndex
     *         the index at which to end the search.
     *
     * @throws java.lang.IndexOutOfBoundsException
     *         <ul>
     *             <li>If the {@code fromIndex} is outside of the range of {@code 0} to {@link #length()}</li>
     *             <li>If the {@code endIndex} is outside of the range of {@code 0} to {@link #length()}</li>
     *             <li>If the {@code fromIndex} is greater than {@code endIndex}</li>
     *         </ul>
     *
     * @return the index of the first occurrence of the specified substring between
     *         the specified indices or {@code -1} if there is no such occurrence.
     */
    public int indexOf(@Nonnull CharSequence target, int fromIndex, int endIndex)
    {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("index out of range: " + fromIndex);
        if (endIndex < 0)
            throw new IndexOutOfBoundsException("index out of range: " + endIndex);
        if (fromIndex > length())
            throw new IndexOutOfBoundsException("fromIndex > length()");
        if (fromIndex > endIndex)
            throw new IndexOutOfBoundsException("fromIndex > endIndex");

        if (endIndex >= builder.length())
        {
            endIndex = builder.length() - 1;
        }

        int targetCount = target.length();
        if (targetCount == 0)
        {
            return fromIndex;
        }

        char strFirstChar = target.charAt(0);
        int max = endIndex + targetCount - 1;

        lastCharSearch:
        for (int i = fromIndex; i <= max; i++)
        {
            if (builder.charAt(i) == strFirstChar)
            {
                for (int j = 1; j < targetCount; j++)
                {
                    if (builder.charAt(i + j) != target.charAt(j))
                    {
                        continue lastCharSearch;
                    }
                }
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index within this string of the last occurrence of the
     * specified substring between the specified indices.
     *
     * If no such value of {@code target} exists, then {@code -1} is returned.
     *
     * @param  target
     *         the substring to search for.
     * @param  fromIndex
     *         the index from which to start the search.
     * @param  endIndex
     *         the index at which to end the search.
     *
     * @throws java.lang.IndexOutOfBoundsException
     *         <ul>
     *             <li>If the {@code fromIndex} is outside of the range of {@code 0} to {@link #length()}</li>
     *             <li>If the {@code endIndex} is outside of the range of {@code 0} to {@link #length()}</li>
     *             <li>If the {@code fromIndex} is greater than {@code endIndex}</li>
     *         </ul>
     *
     * @return the index of the last occurrence of the specified substring between
     *         the specified indices or {@code -1} if there is no such occurrence.
     */
    public int lastIndexOf(@Nonnull CharSequence target, int fromIndex, int endIndex)
    {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("index out of range: " + fromIndex);
        if (endIndex < 0)
            throw new IndexOutOfBoundsException("index out of range: " + endIndex);
        if (fromIndex > length())
            throw new IndexOutOfBoundsException("fromIndex > length()");
        if (fromIndex > endIndex)
            throw new IndexOutOfBoundsException("fromIndex > endIndex");

        if (endIndex >= builder.length())
        {
            endIndex = builder.length() - 1;
        }

        int targetCount = target.length();
        if (targetCount == 0)
        {
            return endIndex;
        }

        int rightIndex = endIndex - targetCount;

        if (fromIndex > rightIndex)
        {
            fromIndex = rightIndex;
        }

        int strLastIndex = targetCount - 1;
        char strLastChar = target.charAt(strLastIndex);

        int min = fromIndex + targetCount - 1;

        lastCharSearch:
        for (int i = endIndex; i >= min; i--)
        {
            if (builder.charAt(i) == strLastChar)
            {
                for (int j = strLastIndex - 1, k = 1; j >= 0; j--, k++)
                {
                    if (builder.charAt(i - k) != target.charAt(j))
                    {
                        continue lastCharSearch;
                    }
                }
                return i - target.length() + 1;
            }
        }
        return -1;
    }

    /**
     * Creates a {@link net.dv8tion.jda.api.entities.Message Message} object from this MessageBuilder
     *
     * <p><b>Hint:</b> You can use {@link #build(int, int)} or
     * {@link #buildAll(net.dv8tion.jda.api.MessageBuilder.SplitPolicy...) buildAll(SplitPolicy...)} as possible ways to
     * deal with the 2000 character cap.
     *
     * @throws java.lang.IllegalStateException
     *         <ul>
     *             <li>If you attempt to build() an empty Message ({@link #length()} is {@code 0} and no
     *             {@link net.dv8tion.jda.api.entities.MessageEmbed} was provided to {@link #setEmbeds(MessageEmbed...)} </li>
     *             <li>If you attempt to build() a Message with more than 2000 characters of content.</li>
     *         </ul>
     *
     * @return the created {@link net.dv8tion.jda.api.entities.Message Message}
     */
    @Nonnull
    public Message build()
    {
        String message = builder.toString();
        if (this.isEmpty())
            throw new IllegalStateException("Cannot build a Message with no content. (You never added any content to the message)");
        if (message.length() > Message.MAX_CONTENT_LENGTH)
            throw new IllegalStateException("Cannot build a Message with more than " + Message.MAX_CONTENT_LENGTH + " characters. Please limit your input.");

        String[] ids = new String[0];
        return new DataMessage(isTTS, message, nonce, embeds,
                allowedMentions, mentionedUsers.toArray(ids), mentionedRoles.toArray(ids), components.toArray(new LayoutComponent[0]));
    }

    /**
     * Creates a {@link java.util.Queue Queue} of {@link net.dv8tion.jda.api.entities.Message Message} objects from this MessageBuilder.
     *
     * <p>This method splits the content if it exceeds 2000 chars. The splitting behaviour can be customized using {@link SplitPolicy SplitPolicies}.
     * The method will try the policies in the order they are passed to it.
     * <br>If no SplitPolicy is provided each message will be split after exactly 2000 chars.
     *
     * <p><b>This is not Markdown safe.</b> An easy workaround is to include <a href="https://en.wikipedia.org/wiki/Zero-width_space">Zero Width Spaces</a>
     * as predetermined breaking points to the message and only split on them.
     *
     * @param  policy
     *         The {@link net.dv8tion.jda.api.MessageBuilder.SplitPolicy} defining how to split the text in the
     *         MessageBuilder into different, individual messages.
     *
     * @return the created {@link net.dv8tion.jda.api.entities.Message Messages}
     */
    @Nonnull
    public Queue<Message> buildAll(@Nullable SplitPolicy... policy)
    {
        if (this.isEmpty())
            throw new UnsupportedOperationException("Cannot build a Message with no content. (You never added any content to the message)");

        LinkedList<Message> messages = new LinkedList<>();

        if (builder.length() <= Message.MAX_CONTENT_LENGTH)
        {
            messages.add(this.build());
            return messages;
        }

        if (policy == null || policy.length == 0)
            policy = new SplitPolicy[]{ SplitPolicy.ANYWHERE };

        int currentBeginIndex = 0;

        messageLoop:
        while (currentBeginIndex < builder.length() - Message.MAX_CONTENT_LENGTH)
        {
            for (SplitPolicy splitPolicy : policy)
            {
                int currentEndIndex = splitPolicy.nextMessage(currentBeginIndex, this);
                if (currentEndIndex != -1)
                {
                    messages.add(build(currentBeginIndex, currentEndIndex));
                    currentBeginIndex = currentEndIndex;
                    continue messageLoop;
                }
            }
            throw new IllegalStateException("Failed to split the messages");
        }

        if (currentBeginIndex < builder.length())
            messages.add(build(currentBeginIndex, builder.length()));

        if (!this.embeds.isEmpty())
            ((DataMessage) messages.get(messages.size() - 1)).setEmbeds(embeds);

        return messages;
    }

    @Nonnull
    protected DataMessage build(int beginIndex, int endIndex)
    {
        String[] ids = new String[0];
        return new DataMessage(isTTS, builder.substring(beginIndex, endIndex), null, null,
                allowedMentions, mentionedUsers.toArray(ids), mentionedRoles.toArray(ids), components.toArray(new LayoutComponent[0]));
    }

    private String[] toStringArray(long[] users)
    {
        String[] ids = new String[users.length];
        for (int i = 0; i < ids.length; i++)
            ids[i] = Long.toUnsignedString(users[i]);
        return ids;
    }

    /**
     * Interface to allow custom implementation of Splitting rules for
     * {@link #buildAll(net.dv8tion.jda.api.MessageBuilder.SplitPolicy...) MessageBuilder.buildAll(SplitPolicy...)}.
     */
    public interface SplitPolicy
    {
        /**
         * Splits on newline chars {@code `\n`}.
         */
        SplitPolicy NEWLINE = new CharSequenceSplitPolicy("\n", true);

        /**
         * Splits on space chars {@code `\u0020`}.
         */
        SplitPolicy SPACE = new CharSequenceSplitPolicy(" ", true);

        /**
         * Splits exactly after 2000 chars.
         */
        SplitPolicy ANYWHERE = (i, b) -> Math.min(i + Message.MAX_CONTENT_LENGTH, b.length());

        /**
         * Creates a new {@link SplitPolicy} splitting on the specified chars.
         *
         * @param  chars
         *         the chars to split on
         * @param  remove
         *         whether to remove the chars when splitting on them
         *
         * @return a new {@link SplitPolicy}
         */
        @Nonnull
        static SplitPolicy onChars(@Nonnull CharSequence chars, boolean remove)
        {
            return new CharSequenceSplitPolicy(chars, remove);
        }

        /**
         * Default {@link SplitPolicy} implementation. Splits on a specified {@link CharSequence}.
         */
        class CharSequenceSplitPolicy implements SplitPolicy
        {
            private final boolean remove;
            private final CharSequence chars;

            private CharSequenceSplitPolicy(@Nonnull final CharSequence chars, final boolean remove)
            {
                this.chars = chars;
                this.remove = remove;
            }

            @Override
            public int nextMessage(final int currentBeginIndex, final MessageBuilder builder)
            {
                int currentEndIndex = builder.lastIndexOf(this.chars, currentBeginIndex, currentBeginIndex + 2000 - (this.remove ? this.chars.length() : 0));
                if (currentEndIndex < 0)
                {
                    return -1;
                }
                else
                {
                    return currentEndIndex + this.chars.length();
                }
            }
        }

        /**
         * Calculates the endIndex for the next {@link net.dv8tion.jda.api.entities.Message Message}.
         *
         * @param  currentBeginIndex
         *         the index the next {@link net.dv8tion.jda.api.entities.Message Message} should start from
         * @param  builder
         *         the {@link net.dv8tion.jda.api.MessageBuilder MessageBuilder}
         *
         * @return the end Index of the next {@link net.dv8tion.jda.api.entities.Message Message}
         *
         * @throws java.lang.IllegalStateException when splitting fails
         *
         */
        int nextMessage(int currentBeginIndex, MessageBuilder builder);
    }

    /**
     * Holds the available formatting used in {@link MessageBuilder#append(java.lang.CharSequence, net.dv8tion.jda.api.MessageBuilder.Formatting...)}
     */
    public enum Formatting
    {
        ITALICS("*"),
        BOLD("**"),
        STRIKETHROUGH("~~"),
        UNDERLINE("__"),
        BLOCK("`");

        private final String tag;

        Formatting(String tag)
        {
            this.tag = tag;
        }

        @Nonnull
        private String getTag()
        {
            return tag;
        }
    }
}
