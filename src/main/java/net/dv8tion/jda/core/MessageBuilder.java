/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
package net.dv8tion.jda.core;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.DataMessage;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import net.dv8tion.jda.core.utils.Checks;

import javax.annotation.CheckReturnValue;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;

/**
 * Builder system used to build {@link net.dv8tion.jda.core.entities.Message Messages}.
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

    protected boolean isTTS = false;
    protected String nonce;
    protected MessageEmbed embed;

    public MessageBuilder() {}

    public MessageBuilder(CharSequence content)
    {
        if (content != null)
            builder.append(content);
    }

    public MessageBuilder(Message message)
    {
        if (message != null)
        {
            isTTS = message.isTTS();
            builder.append(message.getContentRaw());
            List<MessageEmbed> embeds = message.getEmbeds();
            if (embeds != null && !embeds.isEmpty())
                embed = embeds.get(0);
        }
    }

    public MessageBuilder(MessageBuilder builder)
    {
        if (builder != null)
        {
            this.isTTS = builder.isTTS;
            this.builder.append(builder.builder);
            this.nonce = builder.nonce;
            this.embed = builder.embed;
        }
    }

    public MessageBuilder(EmbedBuilder builder)
    {
        if (builder != null)
            this.embed = builder.build();
    }

    public MessageBuilder(MessageEmbed embed)
    {
        this.embed = embed;
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
    public MessageBuilder setTTS(boolean tts)
    {
        this.isTTS = tts;
        return this;
    }
    
    /**
     * Adds a {@link net.dv8tion.jda.core.entities.MessageEmbed} to the Message. Embeds can be built using
     * the {@link net.dv8tion.jda.core.EmbedBuilder} and offer specialized formatting.
     *
     * @param  embed
     *         the embed to add, or null to remove
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    public MessageBuilder setEmbed(MessageEmbed embed)
    {
        this.embed = embed;
        return this;
    }

    /**
     * Sets the <a href="https://en.wikipedia.org/wiki/Cryptographic_nonce" target="_blank">nonce</a>
     * of the built message(s). It is recommended to have only 100% unique strings to validate messages via this nonce.
     * <br>The nonce will be available from the resulting message via {@link net.dv8tion.jda.core.entities.Message#getNonce() Message.getNonce()}
     * in message received by events and RestAction responses.
     * <br>When {@code null} is provided no nonce will be used.
     *
     * @param  nonce
     *         Validation nonce string
     *
     * @return The MessageBuilder instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.entities.Message#getNonce()
     * @see    <a href="https://en.wikipedia.org/wiki/Cryptographic_nonce" target="_blank">Cryptographic Nonce - Wikipedia</a>
     */
    public MessageBuilder setNonce(String nonce)
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
     * @throws java.lang.IllegalArgumentException
     *         If the provided content exceeds {@link net.dv8tion.jda.core.entities.Message#MAX_CONTENT_LENGTH Message.MAX_CONTENT_LENGTH}
     *
     * @return The MessageBuilder instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.core.entities.Message#getContentRaw()
     */
    public MessageBuilder setContent(String content)
    {
        if (content == null)
        {
            builder.setLength(0);
        }
        else
        {
            Checks.check(content.length() <= Message.MAX_CONTENT_LENGTH, "Content length may not exceed %d!", Message.MAX_CONTENT_LENGTH);
            final int newLength = Math.max(builder.length(), content.length());
            builder.replace(0, newLength, content);
        }
        return this;
    }

    @Override
    public MessageBuilder append(CharSequence text)
    {
        builder.append(text);
        return this;
    }

    @Override
    public MessageBuilder append(CharSequence text, int start, int end)
    {
        builder.append(text, start, end);
        return this;
    }

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
    public MessageBuilder append(Object object)
    {
        return append(String.valueOf(object));
    }

    /**
     * Appends a mention to the Message.
     * <br>Typical usage would be providing an {@link net.dv8tion.jda.core.entities.IMentionable IMentionable} like
     * {@link net.dv8tion.jda.core.entities.User User} or {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     *
     * @param  mention
     *         the mention to append
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    public MessageBuilder append(IMentionable mention)
    {
        builder.append(mention.getAsMention());
        return this;
    }

    /**
     * Appends a String using the specified chat {@link net.dv8tion.jda.core.MessageBuilder.Formatting Formatting(s)}.
     *
     * @param  text
     *         the text to append.
     * @param  format
     *         the format(s) to apply to the text.
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    public MessageBuilder append(CharSequence text, Formatting... format)
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
     *     <li>{@link net.dv8tion.jda.core.entities.IMentionable IMentionable}
     *     <br>These will output their {@link net.dv8tion.jda.core.entities.IMentionable#getAsMention() getAsMention} by default,
     *         some implementations have alternatives such as {@link net.dv8tion.jda.core.entities.User User} and {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *     <li>{@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     *     <br>All message channels format to {@code "#" + getName()} by default, TextChannel has special handling
     *         and uses the getAsMention output by default and the MessageChannel output as alternative ({@code #} flag).</li>
     *     <li>{@link net.dv8tion.jda.core.entities.Message Message}
     *     <br>Messages by default output their {@link net.dv8tion.jda.core.entities.Message#getContentDisplay() getContentDisplay()} value and
     *         as alternative use the {@link net.dv8tion.jda.core.entities.Message#getContentRaw() getContentRaw()} value</li>
     * </ul>
     *
     * <p>Example:
     * <br>If you placed the following code in an method handling a
     * {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
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
     * <br>Note that this uses the {@code #} flag to utilize the alternative format for {@link net.dv8tion.jda.core.entities.User User}.
     * <br>By default it would fallback to {@link net.dv8tion.jda.core.entities.IMentionable#getAsMention()}
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
    public MessageBuilder appendFormat(String format, Object... args)
    {
        Checks.notEmpty(format, "Format String");
        this.append(String.format(format, args));
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
    public MessageBuilder appendCodeBlock(CharSequence text, CharSequence language)
    {
        builder.append("```").append(language).append('\n').append(text).append("\n```");
        return this;
    }

    /**
     * Returns the current length of the content that will be built into a {@link net.dv8tion.jda.core.entities.Message Message}
     * when {@link #build()} is called.
     * <br>If this value is {@code 0} (and there is no embed) or greater than {@code 2000} when {@link #build()} is called, an exception
     * will be raised as you cannot send an empty message to Discord and Discord has a hard limit of 2000 characters per message.
     *
     * <p><b>Hint:</b> You can use {@link #build(int, int)} or
     * {@link #buildAll(net.dv8tion.jda.core.MessageBuilder.SplitPolicy...) buildAll(SplitPolicy...)} as possible ways to
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
     * @return weather the message contains content
     */
    public boolean isEmpty() {
        return builder.length() == 0 && embed == null;
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
    public MessageBuilder replace(String target, String replacement)
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
    public MessageBuilder replaceFirst(String target, String replacement)
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
    public MessageBuilder replaceLast(String target, String replacement)
    {
        int index = builder.lastIndexOf(target);
        if (index != -1)
        {
            builder.replace(index, index + target.length(), replacement);
        }
        return this;
    }

    /**
     * Removes all mentions and replaces them with the closest looking textual representation.
     *
     * <p>Use this over {@link #stripMentions(Guild)} if {@link net.dv8tion.jda.core.entities.User User} mentions
     * should be replaced with their {@link net.dv8tion.jda.core.entities.User#getName()} instead of their Nicknames.
     *
     * @param jda
     *        The JDA instance used to resolve the mentions.
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    public MessageBuilder stripMentions(JDA jda)
    {
        // Note: Users can rename to "everyone" or "here", so those
        // should be stripped after the USER mention is stripped.
        return this.stripMentions(jda, null, Message.MentionType.values());
    }

    /**
     * Removes all mentions and replaces them with the closest looking textual representation.
     *
     * <p>Use this over {@link #stripMentions(JDA)} if {@link net.dv8tion.jda.core.entities.User User} mentions should
     * be replaced with their nicknames in a specific guild based.
     * <br>Uses {@link net.dv8tion.jda.core.entities.Member#getEffectiveName()}
     *
     * @param  guild
     *         the guild for {@link net.dv8tion.jda.core.entities.User User} mentions
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    public MessageBuilder stripMentions(Guild guild)
    {
        // Note: Users can rename to "everyone" or "here", so those
        // should be stripped after the USER mention is stripped.
        return this.stripMentions(guild.getJDA(), guild, Message.MentionType.values());
    }

    /**
     * Removes all mentions of the specified types and replaces them with the closest looking textual representation.
     *
     * <p>Use this over {@link #stripMentions(JDA, Message.MentionType...)} if {@link net.dv8tion.jda.core.entities.User User} mentions should
     * be replaced with their nicknames in a specific guild based.
     * <br>Uses {@link net.dv8tion.jda.core.entities.Member#getEffectiveName()}
     *
     * @param  guild
     *         the guild for {@link net.dv8tion.jda.core.entities.User User} mentions
     * @param  types
     *         the {@link net.dv8tion.jda.core.entities.Message.MentionType MentionTypes} that should be stripped
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    public MessageBuilder stripMentions(Guild guild, Message.MentionType... types)
    {
        return this.stripMentions(guild.getJDA(), guild, types);
    }

    /**
     * Removes all mentions of the specified types and replaces them with the closest looking textual representation.
     *
     * <p>Use this over {@link #stripMentions(Guild, Message.MentionType...)} if {@link net.dv8tion.jda.core.entities.User User}
     * mentions should be replaced with their {@link net.dv8tion.jda.core.entities.User#getName()}.
     *
     * @param  jda
     *         The JDA instance used to resolve the mentions.
     * @param  types
     *         the {@link net.dv8tion.jda.core.entities.Message.MentionType MentionTypes} that should be stripped
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    public MessageBuilder stripMentions(JDA jda, Message.MentionType... types)
    {
        return this.stripMentions(jda, null, types);
    }

    private MessageBuilder stripMentions(JDA jda, Guild guild, Message.MentionType... types)
    {
        if (types == null)
            return this;

        String string = null;

        for (Message.MentionType mention : types)
        {
            if (mention != null)
            {
                switch (mention)
                {
                    case EVERYONE:
                        replace("@everyone", "@\u0435veryone");
                        break;
                    case HERE:
                        replace("@here", "@h\u0435re");
                        break;
                    case CHANNEL:
                    {
                        if (string == null)
                        {
                            string = builder.toString();
                        }

                        Matcher matcher = Message.MentionType.CHANNEL.getPattern().matcher(string);
                        while (matcher.find())
                        {
                            TextChannel channel = jda.getTextChannelById(matcher.group(1));
                            if (channel != null)
                            {
                                replace(matcher.group(), "#" + channel.getName());
                            }
                        }
                        break;
                    }
                    case ROLE:
                    {
                        if (string == null)
                        {
                            string = builder.toString();
                        }

                        Matcher matcher = Message.MentionType.ROLE.getPattern().matcher(string);
                        while (matcher.find())
                        {
                            for (Guild g : jda.getGuilds())
                            {
                                Role role = g.getRoleById(matcher.group(1));
                                if (role != null)
                                {
                                    replace(matcher.group(), "@"+role.getName());
                                    break;
                                }
                            }
                        }
                        break;
                    }
                    case USER:
                    {
                        if (string == null)
                        {
                            string = builder.toString();
                        }

                        Matcher matcher = Message.MentionType.USER.getPattern().matcher(string);
                        while (matcher.find())
                        {
                            User user = jda.getUserById(matcher.group(1));
                            String replacement;

                            if (user == null)
                                continue;

                            Member member;

                            if (guild != null && (member = guild.getMember(user)) != null)
                                replacement = member.getEffectiveName();
                            else
                                replacement = user.getName();

                            replace(matcher.group(), "@" + replacement);
                        }
                        break;
                    }
                }
            }
        }

        return this;
    }

    /**
     * Returns the underlying {@link StringBuilder}.
     *
     * @return The {@link StringBuilder} used by this {@link MessageBuilder}
     */
    public StringBuilder getStringBuilder()
    {
        return this.builder;
    }

    /**
     * Clears the current builder. Useful for mass message creation.
     *
     * @return The MessageBuilder instance. Useful for chaining.
     */
    public MessageBuilder clear() {
        this.builder.setLength(0);
        this.embed = null;
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
    public int indexOf(CharSequence target, int fromIndex, int endIndex)
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
    public int lastIndexOf(CharSequence target, int fromIndex, int endIndex)
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
     * Creates a {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     * with the current settings without building a {@link net.dv8tion.jda.core.entities.Message Message} instance first.
     *
     * @param  channel
     *         The not-null target {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided channel is {@code null}
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have permission to send or read messages in this channel,
     *         or if this is a PrivateChannel and both users (sender and receiver) are bots.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     */
    @CheckReturnValue
    public MessageAction sendTo(MessageChannel channel)
    {
        Checks.notNull(channel, "Target Channel");
        switch (channel.getType())
        {
            case TEXT:
                final TextChannel text = (TextChannel) channel;
                final Member self = text.getGuild().getSelfMember();
                if (!self.hasPermission(text, Permission.MESSAGE_READ))
                    throw new InsufficientPermissionException(Permission.MESSAGE_READ);
                if (!self.hasPermission(text, Permission.MESSAGE_WRITE))
                    throw new InsufficientPermissionException(Permission.MESSAGE_WRITE);
                break;
            case PRIVATE:
                final PrivateChannel priv = (PrivateChannel) channel;
                if (priv.getUser().isBot() && channel.getJDA().getAccountType() == AccountType.BOT)
                    throw new UnsupportedOperationException("Cannot send a private message between bots.");
        }
        final Route.CompiledRoute route = Route.Messages.SEND_MESSAGE.compile(channel.getId());
        final MessageAction action = new MessageAction(channel.getJDA(), route, channel, builder);
        return action.tts(isTTS).embed(embed).nonce(nonce);
    }

    /**
     * Creates a {@link net.dv8tion.jda.core.entities.Message Message} object from this MessageBuilder
     *
     * <p><b>Hint:</b> You can use {@link #build(int, int)} or
     * {@link #buildAll(net.dv8tion.jda.core.MessageBuilder.SplitPolicy...) buildAll(SplitPolicy...)} as possible ways to
     * deal with the 2000 character cap.
     *
     * @throws java.lang.IllegalStateException
     *         <ul>
     *             <li>If you attempt to build() an empty Message ({@link #length()} is {@code 0} and no
     *             {@link net.dv8tion.jda.core.entities.MessageEmbed} was provided to {@link #setEmbed(net.dv8tion.jda.core.entities.MessageEmbed)})</li>
     *             <li>If you attempt to build() a Message with more than 2000 characters of content.</li>
     *         </ul>
     *
     * @return the created {@link net.dv8tion.jda.core.entities.Message Message}
     */
    public Message build()
    {
        String message = builder.toString();
        if (this.isEmpty())
            throw new IllegalStateException("Cannot build a Message with no content. (You never added any content to the message)");
        if (message.length() > Message.MAX_CONTENT_LENGTH)
            throw new IllegalStateException("Cannot build a Message with more than 2000 characters. Please limit your input.");

        return new DataMessage(isTTS, message, nonce, embed);
    }

    /**
     * Creates a {@link java.util.Queue Queue} of {@link net.dv8tion.jda.core.entities.Message Message} objects from this MessageBuilder.
     *
     * <p>This method splits the content if it exceeds 2000 chars. The splitting behaviour can be customized using {@link SplitPolicy SplitPolicies}.
     * The method will try the policies in the order they are passed to it.
     * <br>If no SplitPolicy is provided each message will be split after exactly 2000 chars.
     *
     * <p><b>This is not Markdown safe.</b> An easy workaround is to include <a href="https://en.wikipedia.org/wiki/Zero-width_space">Zero Width Spaces</a>
     * as predetermined breaking points to the message and only split on them.
     *
     * @param  policy
     *         The {@link net.dv8tion.jda.core.MessageBuilder.SplitPolicy} defining how to split the text in the
     *         MessageBuilder into different, individual messages.
     *
     * @return the created {@link net.dv8tion.jda.core.entities.Message Messages}
     */
    public Queue<Message> buildAll(SplitPolicy... policy)
    {
        if (this.isEmpty())
            throw new UnsupportedOperationException("Cannot build a Message with no content. (You never added any content to the message)");

        LinkedList<Message> messages = new LinkedList<>();

        if (builder.length() <= 2000) {
            messages.add(this.build());
            return messages;
        }

        if (policy == null || policy.length == 0)
        {
            policy = new SplitPolicy[]{ SplitPolicy.ANYWHERE };
        }

        int currentBeginIndex = 0;

        messageLoop:
        while (currentBeginIndex < builder.length() - 2001)
        {
            for (int i = 0; i < policy.length; i++)
            {
                int currentEndIndex = policy[i].nextMessage(currentBeginIndex, this);
                if (currentEndIndex != -1)
                {
                    messages.add(build(currentBeginIndex, currentEndIndex));
                    currentBeginIndex = currentEndIndex;
                    continue messageLoop;
                }
            }
            throw new IllegalStateException("Failed to split the messages");
        }

        if (currentBeginIndex < builder.length() - 1)
        {
            messages.add(build(currentBeginIndex, builder.length() - 1));
        }

        if (this.embed != null)
        {
            ((DataMessage) messages.get(messages.size() - 1)).setEmbed(embed);
        }

        return messages;
    }

    protected DataMessage build(int beginIndex, int endIndex)
    {
        return new DataMessage(isTTS, builder.substring(beginIndex, endIndex), null, null);
    }

    /**
     * Interface to allow custom implementation of Splitting rules for
     * {@link #buildAll(net.dv8tion.jda.core.MessageBuilder.SplitPolicy...) MessageBuilder.buildAll(SplitPolicy...)}.
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
        SplitPolicy ANYWHERE = (i, b) -> Math.min(i + 2000, b.length());

        /**
         * Creates a new {@link SplitPolicy} splitting on the specified chars.
         *
         * @param  chars
         *         the chars to split on
         * @param  remove
         *         weather to remove the chars when splitting on them
         *
         * @return a new {@link SplitPolicy}
         */
        static SplitPolicy onChars(CharSequence chars, boolean remove)
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

            private CharSequenceSplitPolicy(final CharSequence chars, final boolean remove)
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
         * Calculates the endIndex for the next {@link net.dv8tion.jda.core.entities.Message Message}.
         * 
         * @param  currentBeginIndex
         *         the index the next {@link net.dv8tion.jda.core.entities.Message Message} should start from
         * @param  builder
         *         the {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder}
         *
         * @return the end Index of the next {@link net.dv8tion.jda.core.entities.Message Message}
         * 
         * @throws java.lang.IllegalStateException when splitting fails
         * 
         */
        int nextMessage(int currentBeginIndex, MessageBuilder builder);
    }

    /**
     * Holds the available formatting used in {@link MessageBuilder#append(java.lang.CharSequence, net.dv8tion.jda.core.MessageBuilder.Formatting...)}
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

        private String getTag()
        {
            return tag;
        }
    }
}
